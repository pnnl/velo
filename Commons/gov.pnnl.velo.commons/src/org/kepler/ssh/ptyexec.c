/*

 Execute the command given as argument through a pseudo-terminal 

 Arguments: command args

 The command will be executed and all stdin/stdout of the command will be 
 transferred back and forth to the stdin/stdout of the master process.

 Useful to force some applications to use stdin/stdout for password request
 instead of on the terminal device. It is used from Java apps to be able to 
 feed passwords to such external programs.

 examples that do not ask for password on the stdin:
    ssh <host> <cmd>
    python -c "import getpass; s=getpass.getpass('Password:'); print 'You typed:',s"
    sudo ls    
 Note: sudo --stdin ls  will force it to read from stdin, so you do not need this program
       for sudo.
 Just try to run the python command by ssh-ing to a host; you will get an exception 
 from termios.py

 To use for the above examples, simply put ptyexec in front of the command
    ptyexec ssh <host> <cmd>
    ptyexec python -c "import getpass; s=getpass.getpass('Password:'); print 'You typed:',s"
    
 Works on Linux. Not tested on anything else.
 Compile: gcc -lutil -o ptyexec ptyexec.c
 
   Copyright Oak Ridge National Laboratory 1/2008
   Author: Norbert Podhorszki, pnorbert@ornl.gov


 More sophisticated, standalone softwares to control 
 such programs from batch systems if you need such:
   empty    / C, at empty.sourceforge.net
   expect   / C, TCL, perl
   pexpect  / python
   
**/

#ifndef _GNU_SOURCE
#   define _GNU_SOURCE
#endif


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

#include <unistd.h>
/*#include <sys/types.h>*/
/*#include <sys/ioctl.h>*/
/*#include <sys/stat.h>*/
/*#include <termios.h>*/
#include <pty.h>
/*#include <utmp.h>*/
/*#include <time.h>*/
#include <sys/time.h>
#include <fcntl.h>
#include <signal.h>

#include <errno.h>

#include <getopt.h>


/** Prototypes */
int ptyexec(int argc, char *argv[]); 
void mediate(int master_fd);
void signalHandler(int sig);
void cleanUp(void); 
void logstr(const char *fmt, ...);
void logbuf(char *buf, int count);
void log_args(int argc, char *argv[]);


/** Global variables */
int verbose = 0;
FILE *logfile = NULL;    /* log file */
char *prgname; /* argv[0] */
/* optional argument variables */
char *logFileName = NULL;

struct option options[] = {
    {"help",       no_argument,       NULL, 'h'},
    {"verbose",    no_argument,       NULL, 'v'},
    {"log",        required_argument, NULL, 'l'},
    {NULL,         0,                 NULL, 0}
};

static const char *optstring = "+hvl:";



void display_help() {
   printf(
"Usage: %s [-log logfile] command [arg1...]   \n"
"\nArguments \n"
"  log           Print master's log to log file. By default there is no logging.\n"
"\nLong options (with - or --)\n"
"  Above arguments can be defined with - and -- instead of the one character option)\n"
"  --help        Print this help.\n"
"  --verbose     Print more log about what this program is doing. \n",
prgname
);

}

void check_var(char *var, char* name) {
   /* Check if required variables are defined */
   if (var == NULL) {
      fprintf(stderr, "\nptyexec error: %s is not defined!\n\n", name);
      display_help();
      exit (1);
   }
}

/** Process the options on command line.
 *  return optind, so argc and argv can be modified in the caller.
 *  logFileName will be set if -l option is used 
 */
int processOptions(int argc, char *argv[]) {
    char c, last_c='_';
    int idx = 1;
    /* Process the arguments */
    while ((c = getopt_long_only(argc, argv, optstring, options, NULL))  > 1) {
        /*printf ("  --  Option %c (%d)\n", c, c);*/
        switch (c) {

            case 'h':
                /*case ’\?’:*/
                display_help();
                exit(0);
            case 'v':
                verbose = 1;
                break;
            case 'l':
                logFileName = strndup(optarg, 256);
                break;
            default:
                printf ("\nptyexec error: Unknown command line option: %s\n\n", argv[optind-1]);
                display_help();
                exit (1);
                break;
        } /* end switch */
        last_c = c;
        idx++;
    } /* end while */
    /*printf(" -- Option index = %d\n", optind);*/
    return optind;
}

/** Main */
int main( int argc, char *argv[] ) {

    /* argument variables necessary to be defined */
    char *cmd      = NULL;

    /* other variables */
    int excode;
    int optidx;

    prgname = strdup(argv[0]);

    optidx = processOptions( argc, argv);

    argc-=optidx;
    argv+=optidx;

    if (argc < 1) {
	fprintf(stderr, "\nptyexec error: command is not defined!\n\n");
	display_help();
	exit(1);
    }


    /* Create log file if requested */
    if (logFileName != NULL) {
	if ( (logfile = fopen(logFileName, "w")) == NULL) {
	    fprintf(stderr, "\nptyexec error: Cannot open file %s: %s\n", logFileName, strerror(errno));
	}
	if (verbose) logstr("Logfile created\n");
    } else if (verbose) {
	fprintf(stderr,"\nptyexec warning: no log file specified. no logging\n");
    }

    
    log_args(argc, argv);
    excode = ptyexec(argc, argv);

    /*fprintf(stderr, "\nptyexec error: This code should have never been reached!\n\n");*/
    if (verbose) logstr("Reached the exit line with child exit code %d\n", excode);
    cleanUp();
    return excode; 
}

/** Variables for execpty, but made global to be able to clean up anytime */
int master_fd = -1;
int slave_fd  = -1;

/** Variables about the child process (to be used in parent) */
int child_exited = 0;   /* will be set in signalHandler when child exits */
int child_exitcode = 0; /* will be set in signalHandler when child exits and also in mediate for safety */
int child_pid = 0;      /* will be set in ptyexec at fork */

/** Execute the provided command with a pseudo-terminal connection. 
  A child process will mediate all stdin/stdout of the master through the
  original stdin/stdout of the master process.
  It is expected the argv contains at least the command and optionally the arguments.
 */
int ptyexec(int argc, char *argv[]) {
    struct  winsize winsz; 
    struct  termios tt;
    int     pid, ppid;

    /* get terminal and winsize settings from the current stdin */
    tcgetattr(STDIN_FILENO, &tt);
    ioctl(STDIN_FILENO, TIOCGWINSZ, &winsz);

    /* create pseudo-terminal */
    if (openpty(&master_fd, &slave_fd, NULL, &tt, &winsz) == -1) {
	fprintf(stderr, "\nptyexec error: pseudo-terminal cannot be created: %s\n", strerror(errno));
	cleanUp();
	exit (3);
    }

    ppid = getpid(); /* to send the child the parent process id */
    logstr("Parent process PID=%d\n", ppid);

    /* fork a child, the mediator */
    if ((child_pid = fork()) < 0) {
	fprintf(stderr, "\nptyexec error: cannot fork a child process: %s\n", strerror(errno));
	cleanUp();
	exit (3);
    }

    /* child will execute the command, parent becomes the mediator */
    if (child_pid == 0) { /* child process */
	close(master_fd);       /* this belongs to the other process */
	/*
	if (setsid() == -1) {
	    fprintf(stderr, "\nptyexec error: setsid() failed: %s:\n", strerror(errno));
	}
	*/
	if (logfile != NULL) 
	    fclose(logfile);    /* log file is written by the mediator, not by this process */
	login_tty(slave_fd);    /* make the slave part of the pseudo-tty the controlling terminal */
	execvp(argv[0], argv);  /* Exec and quit if successful */

	/* failed to run the command */
	fprintf(stderr, "\nptyexec error: cannot execute %s: %s\n", argv[0], strerror(errno));
	cleanUp();
	exit (3);

    } else { /* parent  process*/
	/* set our signal handler to catch when child exits */
	signal(SIGCHLD, signalHandler);
	/*
	if (setsid() == -1) {
	    fprintf(stderr, "\nptyexec error: setsid() failed: %s:\n", strerror(errno));
	}
	*/
	close(slave_fd);     /* this belongs to the other process */
	mediate(master_fd);  /* infinite loop until parent exits */
    }
    if (verbose) logstr("return from ptyexec with %d ...\n", child_exitcode);
    return child_exitcode;   /* should have been set in signalHandler */
}

void mediate(int master_fd) {
    fd_set  set;
    int     n, count, doloop;
    char    buf[BUFSIZ];
    struct timeval timeout;

    /* in infinite loop, read stdin/master_fd and pass it on master_fd/stdout */
    FD_ZERO(&set);
    doloop = 1;
    while(doloop) {
	FD_SET(master_fd, &set);
	FD_SET(STDIN_FILENO, &set);
	timeout.tv_sec = 10;
	timeout.tv_usec = 0;
	n = select(master_fd+1, &set, 0, 0, &timeout);
	if (n > 0 || errno == EINTR) {
	    if (FD_ISSET(STDIN_FILENO, &set)) {
		logstr("read stdin\n");
		if ((count = read(STDIN_FILENO, buf, sizeof(buf))) > 0) {
		    write(master_fd, buf, count);
		    if (logfile != NULL && verbose)
			logbuf(buf, count);
		}
	    }

	    if (FD_ISSET(master_fd, &set)) {
		logstr("read pseudo-terminal\n");
		if ((count = read(master_fd, buf, sizeof(buf))) > 0) {
		    write(STDOUT_FILENO, buf, count);
		    if (logfile != NULL && verbose)
			logbuf(buf, count);
		}
		if (count < 0) {
		    logstr("error on pseudo-terminal, code=%d: %s\n",errno,strerror(errno)); 
		    child_exited = 1;  /* for safety if signal handling is buggy. Exit code unknown here */
		}
	    }
	}
	/*doloop = ( kill(ppid,0) != -1 );*/ /* true if parent is running */
	doloop = !child_exited; /* true if parent is running */
	logstr("select exited with n=%d. Check child %d: %s\n",n, child_pid, (doloop ? "alive" : "gone"));

    }
    if (verbose) logstr("return from mediate...\n");

}

void signalHandler(int sig) {
    int status, pid;
    switch(sig) {
	case SIGTERM:
	case SIGINT:
	case SIGQUIT:
	case SIGSEGV:
	    logstr("Caught signal %d. Do nothing\n", sig);
	    break;
	case SIGCHLD:
	    pid = wait3(&status, WNOHANG, NULL);
	    if (pid == child_pid) { /* we caught our child's exit */
		child_exited = 1;
		if (WIFEXITED(status)) /* exited okayish, so we get an exitcode */
		    child_exitcode = WEXITSTATUS(status);
		else
		    child_exitcode = 0; /* ??? any idea what to report here ??? */
		logstr("Child exited, pid=%d, exit code=%d\n", pid, child_exitcode);
	    }
    }
}


void cleanUp(void) {
    if (logfile != NULL) fclose(logfile);
    if (master_fd != -1) close(master_fd);
    if (slave_fd != -1) close(slave_fd);
}


void logstr(const char *fmt, ...) {
    va_list va;
    struct timeval now;

    if (logfile != NULL) {
	gettimeofday( &now, NULL);
	fprintf(logfile, "%d.%3.3d: ", now.tv_sec, now.tv_usec);

	va_start(va, fmt);
	vfprintf(logfile, fmt, va);
	va_end(va);

	fflush(logfile);
    }
}

void logbuf(char *buf, int count) {
    struct timeval now;
    char swp = buf[count];

    if (logfile != NULL) {
	gettimeofday( &now, NULL);
	fprintf(logfile, "%d.%3.3d: ", now.tv_sec, now.tv_usec);

	buf[count] = '\0';      /* make it string */
	fprintf(logfile, "%s\n", buf);
	buf[count] = swp;
	fflush(logfile);
    }
}


void log_args(int argc, char* argv[]) {
    struct timeval now;
    char buf[BUFSIZ];
    int  i;
    if (logfile != NULL) {
	if (argc > 0) {
	    strncpy(buf, argv[0], BUFSIZ-1);
	    for (i=1; i<argc; i++) {
		strncat(buf, " ", 1);
		strncat(buf, argv[i], BUFSIZ-strlen(buf)-1);
	    }
	} else {
	    sprintf(buf, "-- Impossible case: No arguments given at all --\n");
	}
	gettimeofday( &now, NULL);
	fprintf(logfile, "%d.%3.3d: ", now.tv_sec, now.tv_usec);
	fprintf(logfile, "Command: %s\n", buf);
	fflush(logfile);
    }
}

