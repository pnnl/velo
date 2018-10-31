/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: crawl $'
 * '$Date: 2010-11-18 11:13:42 -0800 (Thu, 18 Nov 2010) $' 
 * '$Revision: 26327 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package org.kepler.ssh;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * This class provides functionality to use an SSH session to execute remote
 * commands and to transfer files on a remote machine.
 * 
 * A session can be opened using one the following authentication protocols: -
 * public-key - password - keyboard-interactive
 * 
 * You can create several sessions to different sites and use any of them
 * referring to it with its user@host.
 * 
 * An Ssh object can be used then by anyone anytime to copy file or execute
 * commands without authenticating again. If the session becomes disconnected,
 * the connection will be reopened at the next use, and authentication will be
 * either automatic (public-key, or stored
 * 
 * <p>
 * 
 * @author Norbert Podhorszki
 * 
 *         Based on - JSch examples http://www.jcraft.com/jsch - OpenSSH 4.3
 *         source code http://www.openssh.com/ - org.sdm.spa.Ssh2Exec Kepler
 *         actor Authors of the Kepler actor: Ilkay Altintas, Xiaowen Xin
 */

public class SshExec extends RemoteExec {

	/* Private variables */
	protected SshSession session;
	protected Session jschSession;

	private static Logger logger = Logger.getLogger(SshExec.class);
	private static final boolean isDebugging = logger.isDebugEnabled();

	// timeout variables
	private int timeout = 0; // timeout in seconds
	private boolean timeoutRestartOnStdout = false; // restart timer if stdout
	// has data
	private boolean timeoutRestartOnStderr = false; // restart timer if stderr
	// has data

	// forced clean-up variables
	private boolean forcedCleanUp = false;
	private static String cleanUpInfoCmd = new String("echo $$; ");

	// variables added to extend SshExec
	private boolean pseudoTerminal = false;
	private String protocolPath = "";
	private String cmdLineOptions = "";
	private boolean endTail;


	/**
	 * Constructor for SshExec.
	 * @param user String
	 * @param host String
	 */
	public SshExec(String user, String host) {
		this(user, host, 22, true);
	}


  /**
   * Constructor for SshExec.
   * @param user String
   * @param host String
   * @param port int
   */
  public SshExec(String user, String host, int port) {
    this(user, host, port, true);
  }

  /**
   * Constructor for SshExec.
   * @param user String
   * @param host String
   * @param cacheSession boolean
   */
  public SshExec(String user, String host, boolean cacheSession) {
    this(user, host, 22, cacheSession);
  }

  /**
   * Constructor for SshExec.
   * @param user String
   * @param host String
   * @param port int
   * @param cacheSession boolean
   */
  public SshExec(String user, String host, int port, boolean cacheSession) {
    this.user = user;
    this.host = host;
    this.port = port;
    if(cacheSession) {
      // use cached session if it already exists
      session = SshSessionFactory.getSession(user, host, port);
    
    } else {
      // do not pull from or put back into cache
      session = new SshSession(user, host, port);
    }
  }
    
  public int getPort() {
    return port;
  }


  /**
   * Method getCredential.
   * @return String
   */
  public String getCredential() {
    if (this.jschSession.getUserInfo().getPassphrase() != null) {
      return this.jschSession.getUserInfo().getPassphrase();
      
    } else if (this.jschSession.getUserInfo().getPassword() != null) {
      return this.jschSession.getUserInfo().getPassword();
      
    } else if (this.jschSession.getUserInfo() instanceof VeloUserInfo) {
      return ((VeloUserInfo) this.jschSession.getUserInfo()).getPassPKI();
    }
    return null;
  }

	/**
	 * Create and SshExec object from a combined target string. Input: string of
	 * format [user@]host[:port]
	 * @param target String
	 */
	public SshExec(String target) {
		// get USER
		String user, host;
		int port = 22;

		int atPos = target.indexOf('@');
		if (atPos >= 0) {
			user = target.substring(0, target.indexOf('@'));
		} else {
			user = System.getProperty("user.name");
		}

		// get the HOST and PORT
		int colonPos = target.indexOf(':');
		if (colonPos >= 0 && colonPos > atPos) {
			host = target.substring(atPos + 1, colonPos);
			String portStr = target.substring(colonPos + 1);
			try {
				port = Integer.parseInt(portStr);
			} catch (java.lang.NumberFormatException ex) {
				logger.error("The port should be a number or omitted in " + target, ex);
			}
		} else
			host = target.substring(atPos + 1);
		session = SshSessionFactory.getSession(user, host, port);
	}

	//Allow users to set a custom UserInfo object to handle authentication
	/**
	 * Method setUserInfo.
	 * @param userInfo UserInfo
	 */
	public void setUserInfo(UserInfo userInfo){
		session.setUserInfo(userInfo);
	}
	
  /**
   * Open the connection to the remote machine now. It is not necessary to
   * call this method! If you do not want to prolong making the connection
   * until the first executeCmd / copyTo / copyFrom, then you need to use this
   * method.
   * @return boolean
   * @throws SshException
   * @see org.kepler.ssh.ExecInterface#openConnection()
   */
	public boolean openConnection() throws SshException {
		if (null == session)
			return false;

		jschSession = session.open();

    try {
      // send alive messages every 30 seconds to avoid
      // drops when running long-lasting commands without much stdout
      if(jschSession.isConnected()){
        jschSession.setServerAliveInterval(30000);
      }
    } catch (JSchException ex) {
      logger.warn("ServerAliveInterval could not be set for session "
          + session.getUser() + "@" + session.getHost() + ": " + ex, ex);
    }

		return true;
	}

	/**
	 * Close the connection This should be called only when there is no more use
	 * of the connection to this user@host Important: the ssh session remains
	 * opened as long as this method is not called, therefore, the the whole
	 * java application will hang at the end until this method is called.
	 * @see org.kepler.ssh.ExecInterface#closeConnection()
	 */
	public void closeConnection() {
		if (null != session)
			session.close();
		// session = null; // disallow reconnection
		jschSession = null;
	}

	/**
	 * Add identity file Public-key authentication can be used if you add
	 * identity files
	 * @param identity String
	 * @see org.kepler.ssh.ExecInterface#addIdentity(String)
	 */
	public void addIdentity(String identity) {
		try {
			if (null != session)
				session.addIdentity(identity);
		} catch (SshException e) {
			logger.error("addIdentity error: ", e);
			; // don't do anything here at this moment
		}
	}

	/**
	 * Method addIdentity.
	 * @param identity File
	 */
	public void addIdentity(File identity) {
		addIdentity(identity.getPath());
	}
	
	/** adds userhome/.ssh/id_rsa and id_dsa **/
	public void addDefaultIdentity(){
		String home = System.getProperty("user.home");
		if (System.getProperty("os.name").startsWith("Windows")) {
		  home = System.getenv().get("HOMEPATH");
		}
		File iddsa = new File(home + File.separator
				+ ".ssh" + File.separator + "id_dsa");
        if (iddsa.exists())
        	addIdentity(iddsa);
		File idrsa = new File(home + File.separator
				+ ".ssh" + File.separator + "id_rsa");
		if (idrsa.exists())
			addIdentity(idrsa);
	}

  /**
   * Set a local port forwarding before the connection is made. Format of the
   * specification: lport:rhost:rport (int:string:int). lport on this host
   * will be forwarded through this session and the remote host to the
   * specified rhost:rport address.
   * @param spec String
   * @throws SshException
   * @see org.kepler.ssh.ExecInterface#setPortForwardingL(String)
   */
	public void setPortForwardingL(String spec) throws SshException {
		session.setPortForwardingL(spec);
	}

	/**
	 * Add a local port forwarding to an open connection. Format of the
	 * specification: lport:rhost:rport (int:string:int). lport on this host
	 * will be forwarded through this session and the remote host to the
	 * specified rhost:rport address.
	 * @param spec String
	 */
	public void addPortForwardL(String spec) {
		session.addPortForwardL(spec);
	}

	/**
	 * Set a remote port forwarding before the connection is made. Format of the
	 * specification: rport:lhost:lport (int:string:int). rport on remote host
	 * will be forwarded through this session and our local host to the
	 * specified lhost:lport address.
	 * @param spec String
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#setPortForwardingR(String)
	 */
	public void setPortForwardingR(String spec) throws SshException {
		session.setPortForwardingR(spec);
	}
	
	/**
	 * Add a remote port forwarding to an open connection. Format of the
	 * specification: rport:lhost:lport (int:string:int). rport on remote host
	 * will be forwarded through this session and our local host to the
	 * specified lhost:lport address.
	 * @param spec String
	 */
	public void addPortForwardR(String spec) {
		session.addPortForwardR(spec);
	}

	/**
	 * Remove a local port forwarding.
	 * 
	 * @param port
	 *            the local port that is forwarded.
	 * @param closeIfLast
	 *            If true, and there are no additional local ports forwarded,
	 *            close the connection.
	 * @throws SshException
	 */
	public void removePortForwardL(int port, boolean closeIfLast)
			throws SshException {
		session.removePortForwardL(port, closeIfLast);
	}
	
	/**
	 * Remove a remote port forwarding.
	 * 
	 * @param port
	 *            the remote port that is forwarded.
	 * @param closeIfLast
	 *            If true, and there are no additional remote ports forwarded,
	 *            close the connection.
	 * @throws SshException
	 */
	public void removePortForwardR(int port, boolean closeIfLast)
			throws SshException {
		session.removePortForwardR(port, closeIfLast);
	}
	
	/**
	 * Set timeout for the operations. Timeout should be given in seconds. If
	 * 'stdout' is set to true, the timer is restarted whenever there is data on
	 * stdout. If 'stderr' is set to true, the timer is restarted whenever there
	 * is data on stderr. executeCmd will throw an ExecException, an instance of
	 * ExecTimeoutException if the timeout limit is reached. 'seconds' = 0 means
	 * no timeout at all. Note: copyTo and copyFrom operations currently do not
	 * support timeout. Note: currently, the timer cannot be restarted on stderr
	 * events in executeCmd
	 * @param seconds int
	 * @param stdout boolean
	 * @param stderr boolean
	 * @see org.kepler.ssh.ExecInterface#setTimeout(int, boolean, boolean)
	 */
	public void setTimeout(int seconds, boolean stdout, boolean stderr) {
		timeout = seconds;
		timeoutRestartOnStdout = stdout;
		timeoutRestartOnStderr = stderr;
	}

	/**
	 * Specify if killing of remote processes (i.e. clean-up) after error or
	 * timeout is required. Unix specific solution is used for clean-up, so do
	 * not use it when connecting to another servers. Default is false, of
	 * course, but use it whenever you can (i.e. connect to unix machines).
	 * @param foo boolean
	 * @see org.kepler.ssh.ExecInterface#setForcedCleanUp(boolean)
	 */
	public void setForcedCleanUp(boolean foo) {
		forcedCleanUp = foo;
	}

	/**
	 * Execute a command on the remote machine. The streams <i>streamOut</i> and
	 * <i>streamErr</i> should be provided to get the output and errors.
	 * 
	 * @param command String
	 * @param streamOut OutputStream
	 * @param streamErr OutputStream
	 * @return exit code of command if execution succeeded, * @throws ExecException
	 * @throws SshException
	 *             if an error occurs for the ssh connection during the command
	 *             execution * @throws ExecTimeoutException
	 *             if the command failed because of timeout * @see org.kepler.ssh.ExecInterface#executeCmd(String, OutputStream, OutputStream)
	 */
	public int executeCmd(String command, OutputStream streamOut,
			OutputStream streamErr) throws ExecException {
		return executeCmd(command, streamOut, streamErr, null);
	} // end-of-method executeCmd

	/**
	 * Execute a command on the remote machine and expect a password/passphrase
	 * question from the command. The stream <i>streamOut</i> should be provided
	 * to get the output and errors merged. <i>streamErr</i> is not used in this
	 * method (it will be empty string finally).
	 * @param command String
	 * @param streamOut OutputStream
	 * @param streamErr OutputStream
	 * @param thirdPartyTarget String
	 * @return exit code of command if execution succeeded, * @throws ExecException
	 * @throws ExecTimeoutException
	 *             if the command failed because of timeout * @throws SshException
	 *             if an error occurs for the ssh connection during the command
	 *             execution Note: in this method, the SSH Channel is forcing a
	 *             pseudo-terminal allocation {see setPty(true)} to allow remote
	 *             commands to read something from their stdin (i.e. from us
	 *             here), thus, (1) remote environment is not set from
	 *             .bashrc/.cshrc and (2) stdout and stderr come back merged in
	 *             one stream. * @see org.kepler.ssh.ExecInterface#executeCmd(String, OutputStream, OutputStream, String)
	 */
	public int executeCmd(String command, OutputStream streamOut,
			OutputStream streamErr, String thirdPartyTarget)
			throws ExecException {

		int exitCode = 0;
		String cmd = forcedCleanUp ? cleanUpInfoCmd + command : command;
		openConnection();
		
		// get the pwd to the third party (and perform authentication if not yet
		// done)
		String pwd = SshSession.getPwdToThirdParty(thirdPartyTarget);

		// create a piped stream to feed the input of the remote exec channel
		PipedInputStream pis = null;
		PipedOutputStream pos = null;
		if (pwd != null) {
			try {
				pis = new PipedInputStream();
				pos = new PipedOutputStream(pis);
			} catch (IOException ex) {
				logger.error("Error when creating the piped stream for password feededing: ", ex);
			}
		}
		// At this point we have an opened session to the remote machine.
		// But session may be down, so this complex trial cycle here.
		
		boolean tryagain = true;
		while (tryagain) {
			tryagain = false;
			InputStream in = null;
			try {
				pwd = SshSession.getPwdToThirdParty(thirdPartyTarget);
				ChannelExec channel = null;
				_streamReaderThread readerThread;
				synchronized (session) {
					channel = (ChannelExec) jschSession.openChannel("exec");
					if (isDebugging)
						logger.debug("pseudoTerminal=" + pseudoTerminal);
					channel.setPty(pseudoTerminal);
					channel.setCommand(cmd);
					// channel.setOutputStream(streamOut); // use rather
					// getInputStream and read it
					channel.setErrStream(streamErr); // Useless!! stderr goes
					// into stdout
					channel.setInputStream(pis); // remote command will read
					// from our stream
					in = channel.getInputStream();

					// start output processing thread
					// it checks for timeout too
					readerThread = new _streamReaderThread(channel, in,
							streamOut, pwd, pos);
					readerThread.start();

					channel.connect(); // command starts here but we get back
					// the control
					// this thread runs further but reading of pis by the remote
					// process may block it
				}
				if (isDebugging) {
					logger.debug("Started remote execution of command: " + command);
				}
				// wait for the reader thread to finish
				// It will timeout at the latest if the command does not finish
				// 3 ways to finish:
				// - command terminates
				// - timeout
				// - IOException when reading the command's output or writing
				// the caller's output
				readerThread.join();

				// on timeout finish here with a nice Exception
				if (readerThread.timeoutHappened()) {
					logger.error("Timeout: " + timeout + "s elapsed for command "
							+ command);
					// BUG?: disconnect does not kill the remote process!!!
					// Clean-up should be done somehow
					channel.disconnect();
					if (forcedCleanUp) {
						// time for clean-up ;-)
						kill(readerThread.getProcessID(), true);
					}
					throw new ExecTimeoutException(command);
				}
				// if we cannot process output, still wait for the channel to be
				// closed
				// !!! This can lead to hang-up !!!
				while (!channel.isClosed()) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
				// command completed successfully
				if (isDebugging)
					logger.debug("ToolCommand execution terminated, now waiting for the channel to be closed.");

				if (isDebugging)
					logger.debug("Channel closed down, now get the exit status.");
				// this is the wrong test if the remote OS is OpenVMS,
				// but there doesn't seem to be a way to detect it.
				// Note: it must be called only when channel.isClosed() becomes
				// true.
				// Otherwise it may return -1, being not yet set to the exit
				// code
				exitCode = channel.getExitStatus();
				if (isDebugging)
					logger.debug("Exit status = " + exitCode
							+ ". Now disconnect channel.");
				// Note: The jsch source suggests that when the exit code is
				// set, the
				// package disconnects the channel, so the extra call to
				// disconnect()
				// may be unnecessary. But I inherited this way and am tired of
				// debugging...
				channel.disconnect();

				if (exitCode != 0 && forcedCleanUp) {
					// what is sure is sure ;-)
					kill(readerThread.getProcessID(), true);
				}

			} catch (JSchException ex) {
				if (ex.toString().indexOf("session is down") > -1) {
					logger.error("Session to " + session.getUser() + "@"
							+ session.getHost() + " is down, try connect again");
					closeConnection();
					openConnection();
					tryagain = true;
				} else {
					throw new SshException("JSchException caught at command: "
							+ command + "\n" + ex, ex);
				}
			} catch (Exception e) {
				throw new SshException("Exception caught at command: "
						+ command + "\n" + e, e);
			}
		} // end while

		return exitCode;

	} // end-of-method executeCmd
	
	
	@Override
	public int executeTail(String filename, OutputStream streamOut,
			OutputStream streamErr, String[] env, File workingdir,
			ConcurrentLinkedQueue<String> queue) throws ExecException {
		
		String command = "tail -n +1 -f " + filename;
		int exitCode = 0;
		String cmd = forcedCleanUp ? cleanUpInfoCmd + command : command;
		openConnection();
		

		// create a piped stream to feed the input of the remote exec channel
		PipedInputStream pis = null;
		PipedOutputStream pos = null;
		
		// At this point we have an opened session to the remote machine.
		// But session may be down, so this complex trial cycle here.
		
		boolean tryagain = true;
		
		while (tryagain) {
			tryagain = false;
			InputStream in = null;
			try {
				
				ChannelExec channel = null;
				_tailThread tailThread;
				synchronized (session) {
					channel = (ChannelExec) jschSession.openChannel("exec");
					if (isDebugging)
						logger.debug("pseudoTerminal=" + pseudoTerminal);
					channel.setPty(pseudoTerminal);
					channel.setCommand(cmd);
					// channel.setOutputStream(streamOut); // use rather
					// getInputStream and read it
					channel.setErrStream(streamErr); // Useless!! stderr goes
					// into stdout
					channel.setInputStream(pis); // remote command will read
					// from our stream
					in = channel.getInputStream();

					// start output processing thread
					// it checks for timeout too
					tailThread = new _tailThread(channel, in,
							streamOut, null, pos, queue);
					tailThread.start();

					channel.connect(); // command starts here but we get back
					// the control
					// this thread runs further but reading of pis by the remote
					// process may block it
				}
				if (isDebugging) {
					logger.debug("Started remote execution of command: "
									+ command);
				}
				// wait for the reader thread to finish
				// It will timeout at the latest if the command does not finish
				// 3 ways to finish:
				// - command terminates
				// - timeout
				// - IOException when reading the command's output or writing
				// the caller's output
				tailThread.join();

				// on timeout finish here with a nice Exception
				if (tailThread.timeoutHappened()) {
					logger.error("Timeout: " + timeout + "s elapsed for command "
							+ command);
					// BUG?: disconnect does not kill the remote process!!!
					// Clean-up should be done somehow
					channel.disconnect();
					if (forcedCleanUp) {
						// time for clean-up ;-)
						kill(tailThread.getProcessID(), true);
					}
					throw new ExecTimeoutException(command);
				}
				
				if(endTail){
					logger.info("Got stop request of tail -f command");
					// BUG?: disconnect does not kill the remote process!!!
					// Clean-up should be done somehow
					channel.disconnect();
				}
				// if we cannot process output, still wait for the channel to be
				// closed
				// !!! This can lead to hang-up !!!
				while (!channel.isClosed()) {
					try {
						Thread.sleep(500);
					} catch (Exception e) {
					}
				}
				// command completed successfully
				if (isDebugging)
					logger.debug("Command execution terminated, now waiting for the channel to be closed.");

				if (isDebugging)
					logger.debug("Channel closed down, now get the exit status.");
				// this is the wrong test if the remote OS is OpenVMS,
				// but there doesn't seem to be a way to detect it.
				// Note: it must be called only when channel.isClosed() becomes
				// true.
				// Otherwise it may return -1, being not yet set to the exit
				// code
				if(endTail){
					//we forcebly ended so set exit code to 0
					exitCode = 0;
					//process seem to be still there on ascem server. So cleaning up process
					kill(tailThread.getProcessID(), true);
				}else{
					exitCode = channel.getExitStatus();
				}
				if (isDebugging)
					logger.debug("Exit status = " + exitCode
							+ ". Now disconnect channel.");
				// Note: The jsch source suggests that when the exit code is
				// set, the
				// package disconnects the channel, so the extra call to
				// disconnect()
				// may be unnecessary. But I inherited this way and am tired of
				// debugging...
				channel.disconnect();

				if (exitCode != 0 && forcedCleanUp) {
					// what is sure is sure ;-)
					kill(tailThread.getProcessID(), true);
				}

			} catch (JSchException ex) {
				if (ex.toString().indexOf("session is down") > -1) {
					logger.error("Session to " + session.getUser() + "@"
									+ session.getHost()
									+ " is down, try connect again");
					closeConnection();
					openConnection();
					tryagain = true;
				} else {
					throw new SshException("JSchException caught at command: "
							+ command + "\n" + ex, ex);
				}
			} catch (Exception e) {
				throw new SshException("Exception caught at command: "
						+ command + "\n" + e, e);
			}
		} // end while

		return exitCode;
	}

	@Override
	public void setEndTail(boolean b) {
		this.endTail = b;
		
	}

	/**
	 * Copy _one_ local file to a remote directory Input: file of type File
	 * (which can be a directory) Input must not have wildcards. targetPath is
	 * either a directory or filename
	 * @param lfile File
	 * @param targetPath String
	 * @param recursive boolean
	 * @return number of files copied successfully SshException is thrown in
	 *         case of error. 
	 * @throws SshException
	 */
	protected int _copyTo(File lfile, String targetPath, boolean recursive)
			throws SshException {

		if (!lfile.exists()) {
			throw new SshException("File does not exist: " + lfile);
		}

		String recursiveFlag = "";
		// check: recursive traversal of directories enabled?
		if (lfile.isDirectory()) {
			if (!recursive)
				throw new SshException("File " + lfile
						+ " is a directory. Set recursive copy!");
			recursiveFlag = "-r ";
		}
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");

		// at this point we have a living, opened session to the remote machine
		int numberOfCopiedFiles = 0;

		if (isDebugging)
			logger.debug(" %   Copy " + lfile + " to " + targetPath);
		String command = protocolPath + "scp " + cmdLineOptions + " "
				+ recursiveFlag + "-p -t " + targetPath;
		OutputStream out;
		InputStream in;

		try {
			ChannelExec channel;

			synchronized (session) { // for thread safety here we need to sync
				channel = (ChannelExec) jschSession.openChannel("exec");
				channel.setCommand(command);
				// get I/O streams for remote scp
				out = channel.getOutputStream();
				in = channel.getInputStream();
				channel.connect();
			}

			if (checkAck(in) != 0)
				throw new SshException("Scp to remote site failed\n");

			// perform the protocol of actual copy of file/directory
			numberOfCopiedFiles = source(lfile, in, out);

			// close channel
			channel.disconnect();

		} catch (Exception e) {
			throw new SshException("Exception caught at command: " + command
					+ "\n" + e, e);
		}

		return numberOfCopiedFiles;
	}

	/**
	 * Copy one file to the remote location. This is the core of the copyTo
	 * method, which sends a content of file to the remote ssh server using the
	 * protocol of scp/rcp. If 'lfile' is a directory, it does this recursively.
	 * @param lfile File
	 * @param in InputStream
	 * @param out OutputStream
	 * @return The number of copied files, including the directories created. * @throws Exception
	 */
	private int source(File lfile, InputStream in, OutputStream out)
			throws Exception {

		// String accessRights="644"; // rw-r--r--
		String accessRights = "755"; // rwxr-xr-x
		byte[] buf = new byte[1024];

		String command;

		// recursive handling of directories
		if (lfile.isDirectory()) {
			File[] files = lfile.listFiles();
			if (files != null) {
				// send "C0755 0 filename", where filename should not include
				// '/'
				command = "D0" + accessRights + " 0 " + lfile.getName() + "\n";
				out.write(command.getBytes());
				out.flush();
				int numberOfCopiedFiles = 0;
				for (int i = 0; i < files.length; i++) {
					if (isDebugging)
						logger.debug(" %     " + files[i]);
					numberOfCopiedFiles += source(files[i], in, out);
				}
				// End of directory: send "E\n"
				buf[0] = 'E';
				buf[1] = '\n';
				out.write(buf, 0, 2);
				out.flush();
				return numberOfCopiedFiles + 1;
			} else {
				throw new SshException(
						"Unable to read directory "
								+ lfile.getPath()
								+ "\n Please check if the directory exists and has appropriate permission");
			}
		}

		// Now we deal with one single file, let's send it
		FileInputStream fis = new FileInputStream(lfile);

		// send "C0644 filesize filename", where filename should not include '/'
		command = "C0" + accessRights + " " + lfile.length() + " "
				+ lfile.getName() + "\n";
		out.write(command.getBytes());
		out.flush();
		// System.out.print(" % sent command: " + command);

		if (checkAck(in) != 0) {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception ee) {
			}
			throw new SshException("Scp of file " + lfile
					+ " to remote site failed\n");
		}

		// send the content of lfile
		int bytesSent = 0;
		int len = 0;
		while (true) {
			len = fis.read(buf, 0, buf.length);
			if (len <= 0)
				break;
			out.write(buf, 0, len);
			out.flush();
			bytesSent += len;
		}
		// logger.debug(" % sent file of bytes: " + bytesSent);

		// Finish copy of file: send '\0'
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();
		// logger.debug(" % sent zero\n");

		fis.close();
		fis = null;

		if (checkAck(in) != 0)
			throw new SshException("Scp to remote site failed\n");

		return 1;

	}

	/**
	 * Method checkAck.
	 * @param in InputStream
	 * @return int
	 * @throws IOException
	 * @throws SshException
	 */
	protected static int checkAck(InputStream in) throws IOException,
			SshException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1

		// System.out.print("    --- checkAck = " + b + (b <= 0 ? "\n" : " "));
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2 || b == 'E') {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				// System.out.print((char)c);
				sb.append((char) c);
			} while (c != '\n');
			// logger.debug(".");
			if (b == 1) { // error
				throw new SshException("Error at acknowledgement: "
						+ sb.toString());
			}
			if (b == 2) { // fatal error
				throw new SshException("Fatal error at acknowledgement: "
						+ sb.toString());
			}
		} else
			; // logger.debug(".");
		return b;
	}
	
  /**
   * Anand: New method added to check if remote file is a directory Run "cd"
   * command on given file. It success, it is a directory, else it is not.
   * 
   * @param fileName : file which is to be checked to see if it is a directory
   * @return : true if it is a directory, else false
   * @throws Exception 
   */
	public boolean isRemoteFileDirectory(String fileName) throws Exception {
		ChannelExec channel;
		OutputStream out;
		InputStream in;
		int exitCode = 5;
		// byte buf[] = new byte[1024];
		String command = "cd \"" + fileName + "\"";
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");

		synchronized (session) { // for thread safety here we need to sync
			channel = (ChannelExec) jschSession.openChannel("exec");
			channel.setCommand(command);
			// get I/O streams for remote scp
			out = channel.getOutputStream();
			in = channel.getInputStream();
			channel.connect();
		}
		while (true) {
			while (in.available() > 0) {
				int i = in.read();
				if (i < 0)
					break;
			}
			if (channel.isClosed()) {
				exitCode = channel.getExitStatus();
				logger.debug("****************exit-status: "
						+ channel.getExitStatus());
				break;
			}
			Thread.sleep(1000);
		}
		channel.disconnect();
		jschSession.disconnect();
		if (exitCode == 0)
			return true;
		else
			return false;
	}

	/**
	 * Copy a remote file into a local file Input: 'rfile' of type String (can
	 * be a directory or filename) 'localPath' is either a directory or filename
	 * Only if 'recursive' is set, will directories copied recursively.
	 * 
	 * @param rfile String
	 * @param localPath File
	 * @param recursive boolean
	 * @return number of files copied successfully SshException is thrown in
	 *         case of error.
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyFrom(String, File, boolean)
	 */
	public int copyFrom(String rfile, File localPath, boolean recursive)
			throws SshException {

		int numberOfCopiedFiles = 0;
		String recursiveFlag = recursive ? "-r " : "";
		String command = "";
		
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");
		// at this point we have a living, opened session to the remote machine
		
		command = protocolPath + "scp " + cmdLineOptions + " " + recursiveFlag
				+ "-f " + rfile;
		
		if (isDebugging) {
			logger.debug(" %   Copy " + rfile + " to " + localPath);
			logger.debug("Command= " + command);
		}
		OutputStream out;
		InputStream in;

		try {
			ChannelExec channel;

			synchronized (session) { // for thread safety here we need to sync
				channel = (ChannelExec) jschSession.openChannel("exec");
				channel.setCommand(command);
				// get I/O streams for remote scp
				out = channel.getOutputStream();
				in = channel.getInputStream();
				channel.connect();
			}
			// Anand: Debug for SCP remote to local copy
			logger.debug("***********SCP : Remote to local : " + command);

			// perform the protocol of actual copy of file/directory
			numberOfCopiedFiles = sink(localPath, recursive, 0, in, out);

			// close channel
			channel.disconnect();

		} catch (Exception e) {
			throw new SshException("Exception caught at command: " + command
					+ "\n" + e, e);
		}

		return numberOfCopiedFiles;
	}

	/**
	 * Copy one file from the remote location. This is the core of the copyFrom
	 * method, which receives a content of file from the remote ssh server using
	 * the protocol of scp/rcp. If the received file is a directory, it does
	 * this recursively. 'level' is used only for formatted log print.
	 * 
	 * @param localPath File
	 * @param recursive boolean
	 * @param level int
	 * @param in InputStream
	 * @param out OutputStream
	 * @return The number of copied files, including the directories created. * @throws Exception
	 */
	private int sink(File localPath, boolean recursive, int level,
			InputStream in, OutputStream out) throws Exception {

		byte[] buf = new byte[1024];
		int numberOfCopiedFiles = 0;
		StringBuffer tab = new StringBuffer(level * 2);

		for (int i = 0; i <= level; i++)
			tab.append("  ");

		// initiate copy with acknowledgement (send a \0 byte)
		buf[0] = 0;
		out.write(buf, 0, 1);
		out.flush();

		boolean finished = false;
		while (!finished) {
			int c = checkAck(in);

			if (c == -1) {
				// no more files
				finished = true;
				break;
			}

			if (c == 'E') {
				// end of receiving a directory
				finished = true;
				break;
			}

			if (c != 'C' && c != 'D') // not a file or a directory to be copied
				// is the response
				throw new SshException("Scp from remote site failed\n");

			if (c == 'D' && !recursive) // directory is to be sent
				throw new SshException(
						"Remote path is a directory, but not recursive copy is requested");

			// read: mode + length + filename
			// read '0644 ' or '0755 ' or something similar
			in.read(buf, 0, 5);

			long filesize = 0L;
			while (true) {
				if (in.read(buf, 0, 1) < 0) {
					// error
					break;
				}
				if (buf[0] == ' ')
					break;
				filesize = filesize * 10L + (long) (buf[0] - '0');
			}

			String rFileName = null;
			for (int i = 0;; i++) {
				in.read(buf, i, 1);
				if (buf[i] == (byte) 0x0a) {
					rFileName = new String(buf, 0, i);
					break;
				}
			}

			// local file is either file localPath, or dir localPath + the
			// received name
			File lfile;
			if (localPath.isDirectory())
				lfile = new File(localPath, rFileName);
			else
				lfile = localPath;

			// if we receive a directory, we have to go recursive
			if (c == 'D') {
				if (!lfile.exists()) {
					if (!lfile.mkdirs()) // create the directory
						throw new SshException(lfile
								+ ": Cannot create such directory");
				} else if (!lfile.isDirectory()) // already exists as a file
					throw new SshException(lfile + ": Not a directory");

				// lfile is now an existing directory, fine
				if (isDebugging)
					logger.debug(" %   " + tab + rFileName + " -> " + lfile);
				numberOfCopiedFiles += 1 + sink(lfile, true, level + 1, in, out);
				// send acknowledgement (send a \0 byte)
				buf[0] = 0;
				out.write(buf, 0, 1);
				out.flush();
				continue;
			}

			if (isDebugging)
				logger.debug(" %   " + tab + rFileName + " -> " + lfile);

			// now we deal with a single file. Let's read it.
			FileOutputStream fos = new FileOutputStream(lfile);

			// send acknowledgement (send a \0 byte)
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();

			int len;
			while (true) {
				if (buf.length < filesize)
					len = buf.length;
				else
					len = (int) filesize;
				len = in.read(buf, 0, len);
				if (len < 0) {
					// error
					break;
				}
				fos.write(buf, 0, len);
				filesize -= len;
				if (filesize == 0L)
					break;
			}
			fos.close();
			fos = null;
			numberOfCopiedFiles++;

			if (checkAck(in) != 0)
				throw new SshException("Scp from remote site to file " + lfile
						+ " failed\n");

			// send acknowledgement (send a \0 byte)
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
		}

		return numberOfCopiedFiles;
	}

	/**
	 * Separate class to process the output stream of the command, so that it
	 * can be run in a separate thread. Note: we use InputStreamReader and not
	 * BufferedReader because when the command asks for a password, it does not
	 * send a newline with the request, so we would be blocked at readLine while
	 * the command is waiting for response. Note: in case of timeout, the output
	 * thread will forcefully disconnect the channel and if forcedCleanUp then
	 * also kill the command process.
	 */

	private class _streamReaderThread extends Thread {
		private InputStreamReader isr; // 'char' reader from the remote command
		private OutputStreamWriter osw; // 'char' writer to the caller's output
		// stream
		private boolean cleanUpInfoProcessed = !forcedCleanUp; // false: will
		// consume first
		// line for
		// process ID
		private String pwd; // the password to be fed to the command
		private PipedOutputStream pos; // the pipe-in to the stdin of the remote
		// command
		private ChannelExec channel;

		private StringBuffer processID; // the remote command's shell's process
		// id (to kill if needed)
		private boolean timeoutReached; // becomes true on timeout

		/**
		 * Constructor for _streamReaderThread.
		 * @param ch ChannelExec
		 * @param in InputStream
		 * @param out OutputStream
		 * @param pwd String
		 * @param pos PipedOutputStream
		 */
		public _streamReaderThread(ChannelExec ch, InputStream in,
				OutputStream out, String pwd, PipedOutputStream pos) {
			try {
				isr = new InputStreamReader(in, "utf-8");
				osw = new OutputStreamWriter(out, "utf-8");
			} catch (UnsupportedEncodingException ex) {
				// get the default encoding
				isr = new InputStreamReader(in);
				osw = new OutputStreamWriter(out);
			}

			channel = ch;
			this.pwd = pwd;
			this.pos = pos;
		}

		/**
		 * Method getProcessID.
		 * @return String
		 */
		public String getProcessID() {
			return processID.toString();
		}

		/**
		 * Method timeoutHappened.
		 * @return boolean
		 */
		public boolean timeoutHappened() {
			return timeoutReached;
		}

		/**
		 * Method run.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			char[] tmp = new char[1024];
			boolean checkForPwd = (pwd != null);
			processID = new StringBuffer();

			// variables for the timeout checking
			long start = System.currentTimeMillis();
			long current = 0;
			long maxtime = timeout * 1000L;

			// variables for the search for password request in the output
			// stream
			// int i; // search index variable
			// boolean foundpwd;

			while (true) { // read command's output until termination or timeout
				int len = 0;
				int j = 0;
				try {
					while (isr.ready()) { // we do not want to block on read
						// because we are counting for
						// timeout
						len = isr.read(tmp, 0, 1024);
						if (len < 0) {
							if (isDebugging)
								logger.debug("Read error on stdout stream: " + len);
							break; // break the reading loop
						}
						j = 0;

						// first line is remote process id in case of
						// forcedCleanUp. Filter here
						if (!cleanUpInfoProcessed) {
							// if (isDebugging)
							// logger.debug("cleanup info string: " + new
							// String(tmp, 0, len));
							for (; j < len; j++) {
								if (tmp[j] == '\n') {
									cleanUpInfoProcessed = true; // done
									j++;
									if (isDebugging)
										logger.debug("Remote process id = "
												+ processID);
									break; // break the reading loop
								}
								processID.append(tmp[j]);
							}
							// Note: j<=len here
						}

						// print the buffer to the output stream
						osw.write(tmp, j, len - j);
						osw.flush(); // send it really if someone is polling it
						// above us
//						logger.debug(" %%% "
//								+ new String(tmp, j, len - j));
						if (timeoutRestartOnStdout)
							start = System.currentTimeMillis(); // restart
						// timeout timer
						String tempStr = new String(tmp, j, len - j);
						// logger.debug("%%%tempstr%%% "+tempStr);
						if (tempStr.contains("RSA key fingerprint is")
								&& tempStr.trim().endsWith("(yes/no)?")) {
							boolean userInput = jschSession.getUserInfo()
									.promptYesNo(tempStr);
							logger.debug("Prompt for host verification: "
									+ tempStr);
							if (userInput) {
								pos.write("yes\n".getBytes());
								pos.flush();
								logger.info("Added destination server to known_hosts of source");
								continue;
							} else {
								pos.write("no\n".getBytes());
								pos.flush();
								pos.close();
								logger.error("Failed to accept RSA key fingerprint");
								break;
							}
						}

						if (checkForPwd && containsPasswordRequest(tmp, j, len)) {
							// now feed the password to the process
							try {
								pos.write(pwd.getBytes());
								// logger.info("Sent password ");
								pos.write("\n".getBytes());
								// logger.info("Sent newline ");
								pos.flush();
								// logger.info("Flushed pos ");
								pos.close();
								logger.info("Sent password to third party.");
							} catch (IOException ex) {
								logger.error("Error when feeding the password to the piped stream: ", ex);
							}
							checkForPwd = false;
						}

					} // end while
				} catch (IOException ex) {
					logger.error("Error on the remote streams. Exiting reader thread: ", ex);
					break; // exit the loop
				}
				if (channel.isClosed())
					break; // exit the loop

				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}

				// check timeout
				current = System.currentTimeMillis();
				if (timeout > 0 && maxtime < current - start) {
					logger.debug("Reader thread detected timeout: " + timeout
							+ "s elapsed");
					timeoutReached = true;
					break; // exit the loop
				}
			} // while (true)

			try {
				osw.close();
			} catch (IOException ex) {
				logger.error("Cannot flush and close the output stream: ", ex);
			}
		} // end method run()

		/**
		 * Look for one of the strings password/passphrase/passcode in the
		 * char[] array. Return true if found any. Case insensitive search.
		 * Possible bug: we do not find the password text if it is broken into
		 * two in two consecutive calls.
		 * @param buf char[]
		 * @param startPos int
		 * @param endPos int
		 * @return boolean
		 */
		private boolean containsPasswordRequest(char[] buf, int startPos,
				int endPos) {
			// look for strings password/passphrase/passcode
			int i = startPos;
			while (i < endPos - 3) {
				if (Character.toLowerCase(buf[i]) == 'p'
						&& Character.toLowerCase(buf[i + 1]) == 'a'
						&& Character.toLowerCase(buf[i + 2]) == 's'
						&& Character.toLowerCase(buf[i + 3]) == 's') {

					// found "pass", look further for word/code/phrase
					if (i < endPos - 7
							&& Character.toLowerCase(buf[i + 4]) == 'w'
							&& Character.toLowerCase(buf[i + 5]) == 'o'
							&& Character.toLowerCase(buf[i + 6]) == 'r'
							&& Character.toLowerCase(buf[i + 7]) == 'd') {
						logger.info("PWDSearch: found request for password.");
						return true;
					} else if (i < endPos - 7
							&& Character.toLowerCase(buf[i + 4]) == 'c'
							&& Character.toLowerCase(buf[i + 5]) == 'o'
							&& Character.toLowerCase(buf[i + 6]) == 'd'
							&& Character.toLowerCase(buf[i + 7]) == 'e') {
						logger.info("PWDSearch: found request for passcode.");
						return true;
					} else if (i < endPos - 9
							&& Character.toLowerCase(buf[i + 4]) == 'p'
							&& Character.toLowerCase(buf[i + 5]) == 'h'
							&& Character.toLowerCase(buf[i + 6]) == 'r'
							&& Character.toLowerCase(buf[i + 7]) == 'a'
							&& Character.toLowerCase(buf[i + 8]) == 's'
							&& Character.toLowerCase(buf[i + 9]) == 'e') {
						logger.info("PWDSearch: found request for passphrase.");
						return true;
					}
				}
				i = i + 1;
			}
			return false;
		}
	} // end inner class _streamReaderThread

	
	
	
	/**
	 * Separate class to process the output stream of the command, so that it
	 * can be run in a separate thread. Note: we use InputStreamReader and not
	 * BufferedReader because when the command asks for a password, it does not
	 * send a newline with the request, so we would be blocked at readLine while
	 * the command is waiting for response. Note: in case of timeout, the output
	 * thread will forcefully disconnect the channel and if forcedCleanUp then
	 * also kill the command process.
	 */

	private class _tailThread extends Thread {
		private InputStreamReader isr; // 'char' reader from the remote command
		private OutputStreamWriter osw; // 'char' writer to the caller's output
		// stream
		private boolean cleanUpInfoProcessed = !forcedCleanUp; // false: will
		// consume first
		// line for
		// process ID
		private String pwd; // the password to be fed to the command
		private PipedOutputStream pos; // the pipe-in to the stdin of the remote
		// command
		private ChannelExec channel;

		private StringBuffer processID; // the remote command's shell's process
		// id (to kill if needed)
		private boolean timeoutReached; // becomes true on timeout
		private ConcurrentLinkedQueue<String> queue;

		public _tailThread(ChannelExec ch, InputStream in,
				OutputStream out, String pwd, PipedOutputStream pos, ConcurrentLinkedQueue<String> queue) {
			try {
				isr = new InputStreamReader(in, "utf-8");
				osw = new OutputStreamWriter(out, "utf-8");
			} catch (UnsupportedEncodingException ex) {
				// get the default encoding
				isr = new InputStreamReader(in);
				osw = new OutputStreamWriter(out);
			}

			channel = ch;
			this.pwd = pwd;
			this.pos = pos;
			this.queue = queue;
		}

		public String getProcessID() {
			return processID.toString();
		}

		public boolean timeoutHappened() {
			return timeoutReached;
		}

		public void run() {
			char[] tmp = new char[1024];
			boolean checkForPwd = (pwd != null);
			processID = new StringBuffer();

			// variables for the timeout checking
			long start = System.currentTimeMillis();
			long current = 0;
			long maxtime = timeout * 1000L;

			// variables for the search for password request in the output
			// stream
			// int i; // search index variable
			// boolean foundpwd;
			String leftover = "";
			boolean lasttry = false;
			while (true) { // read command's output until termination or timeout
				int len = 0;
				int j = 0;
				try {
					while (isr.ready()) { // we do not want to block on read
						// because we are counting for
						// timeout
						len = isr.read(tmp, 0, 1024);
						if (len < 0) {
							if (isDebugging)
								logger.debug("Read error on stdout stream: "
												+ len);
							break; // break the reading loop
						}
						j = 0;

						// first line is remote process id in case of
						// forcedCleanUp. Filter here
						if (!cleanUpInfoProcessed) {
							// if (isDebugging)
							// logger.debug("cleanup info string: " + new
							// String(tmp, 0, len));
							for (; j < len; j++) {
								if (tmp[j] == '\n') {
									cleanUpInfoProcessed = true; // done
									j++;
									if (isDebugging)
										logger.debug("Remote process id = "
												+ processID);
									break; // break the reading loop
								}
								processID.append(tmp[j]);
							}
							// Note: j<=len here
						}

						// print the buffer to the output stream
						osw.write(tmp, j, len - j);
						osw.flush(); // send it really if someone is polling it
						// above us
//						logger.debug(" %%% "
//								+ new String(tmp, j, len - j));
						if (timeoutRestartOnStdout)
							start = System.currentTimeMillis(); // restart
						// timeout timer
						String tempStr = new String(tmp, j, len - j);
						// logger.debug("%%%tempstr%%% "+tempStr);
						if (tempStr.contains("RSA key fingerprint is")
								&& tempStr.trim().endsWith("(yes/no)?")) {
							boolean userInput = jschSession.getUserInfo()
									.promptYesNo(tempStr);
							logger.debug("Prompt for host verification: "
									+ tempStr);
							if (userInput) {
								pos.write("yes\n".getBytes());
								pos.flush();
								logger.info("Added destination server to known_hosts of source");
								continue;
							} else {
								pos.write("no\n".getBytes());
								pos.flush();
								pos.close();
								logger.error("Failed to accept RSA key fingerprint");
								break;
							}
						}else{
							
							
							if (tempStr.contains("\n")||tempStr.contains("\r")){
								String[] split = tempStr.split("\\r?\\n|\\r");	
								if(tempStr.endsWith("\n") || tempStr.endsWith("\r")){
									split[0] = leftover + split[0];
									addToQueue(queue, split[0]);
									for(int k=1;k<split.length;k++){
										addToQueue(queue,split[k]);
									}
									leftover ="";
								}else{
									
									split[0] = leftover + split[0] ;
									addToQueue(queue,split[0]);
									for(int k=1;k<split.length-1;k++){
										addToQueue(queue,split[k]);
									}
									leftover = split[split.length-1];
								}
							}else{
								leftover = leftover + tempStr;
							}
						}
						

					} // end while
				} catch (IOException ex) {
					logger.error("Error on the remote streams. Exiting reader thread: ", ex);
					break; // exit the loop
				}
				if (channel.isClosed())
					break; // exit the loop

				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}

				// check timeout
				current = System.currentTimeMillis();
				if (timeout > 0 && maxtime < current - start) {
					logger.debug("Reader thread detected timeout: " + timeout
							+ "s elapsed");
					timeoutReached = true;
					break; // exit the loop
				}
				if(endTail){
					if(lasttry){
					    break;
					}else{
						lasttry = true;
					}
				}
			} // while (true)

			try {
				osw.close();
			} catch (IOException ex) {
				logger.error("Cannot flush and close the output stream: ", ex);
			}
		} // end method run()
		
		private void addToQueue(Queue<String> queue, String str) {
			while(!queue.offer(str)){
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					
				}
			}
		}

		
	} // end inner class _streamReaderThread
	
	
	/**
	 * Method setPseudoTerminal.
	 * @param pseudoTerminal boolean
	 */	
	public void setPseudoTerminal(boolean pseudoTerminal) {
		this.pseudoTerminal = pseudoTerminal;
	}

	/**
	 * Method setProtocolPath.
	 * @param protocolPath String
	 */
	public void setProtocolPath(String protocolPath) {
		this.protocolPath = protocolPath;
	}

	/**
	 * Method setcmdLineOptions.
	 * @param cmdLineOptions String
	 */
	public void setcmdLineOptions(String cmdLineOptions) {
		this.cmdLineOptions = cmdLineOptions;
	}

	// @Override
	/**
	 * Method getForcedCleanUp.
	 * @return boolean
	 */
	public boolean getForcedCleanUp() {
		return forcedCleanUp;
	}

	// @Override
	/**
	 * Method getTimeout.
	 * @return int
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Method setTimeout.
	 * @param seconds int
	 * @see org.kepler.ssh.ExecInterface#setTimeout(int)
	 */
	public void setTimeout(int seconds) {
		timeout = seconds;

	}

	/**
	 * Method getwildcardFileListingBBCP.
	 * @param srcConn String
	 * @param wildcardPattern String
	 * @return String
	 * @throws ExecException
	 */
	public String getwildcardFileListingBBCP(String srcConn,
			String wildcardPattern) throws ExecException {
		// TODO Auto-generated method stub
		String filePath = "";
		String filePattern = "";
		OutputStream streamOut = new ByteArrayOutputStream();
		OutputStream streamErr = new ByteArrayOutputStream();
		StringBuffer wildcardList = new StringBuffer();
		Vector<String> fileList = new Vector(); 
		int index = wildcardPattern.lastIndexOf("/");
		int exitCode = 1;
		
		//extract pattern and directory name from wildcard pattern
		if (index != -1) {
			filePattern = wildcardPattern.substring(index + 1);
			filePath = wildcardPattern.substring(0, index);
		}
logger.debug("parent dir : " + filePath);
logger.debug("File pattern : " + filePattern);

		try {
			//-Q is used to place file names into quotations
			exitCode = executeCmd("\\ls -Q " + filePath, streamOut,streamErr, srcConn);
		} catch (ExecException e) {
			throw new ExecException("Could not list contents of remote directory " + filePath, e);
		}
		
		if (exitCode != 0){
			throw new ExecException("Failed to execute remote command.");
		}
			
		//TODO Anand:extract filenames from ls entry
		// the character \n does not separate file names in a few scenarios.
		// To make the extraction more efficient - we are using a loop
		// If possible to get a separating character for ls field names use that
		// instead of the loop
		// IN THE LOOP: each filename has format "file_name"
		// We extract string in between " " to get the file name 
		String ls_output = streamOut.toString();
		int f_start = 0;
		int f_end = 0;
		while (f_end < ls_output.length()){
			f_start = ls_output.indexOf("\"");
			ls_output = ls_output.substring(f_start+1);
			f_end = ls_output.indexOf("\"");
			fileList.add(ls_output.substring(0, f_end));
			ls_output = ls_output.substring(f_end+1);
		}

		//match filenames to pattern and create a file list
		String pattern = filePattern.replaceAll("\\.", "\\\\.")
				.replaceAll("\\*", ".*").replaceAll("\\?", ".");
		Pattern p = Pattern.compile(pattern);
		
		for (String curFile : fileList){
			logger.debug(":"+curFile+";");
			curFile = curFile.trim();
			Matcher m = p.matcher(curFile);
			if (m.matches()){
				wildcardList.append(srcConn);
				wildcardList.append(":\"");
				wildcardList.append(filePath);
				wildcardList.append("/");
				wildcardList.append(curFile);
				wildcardList.append("\"");
				wildcardList.append(" ");
			}
		}
logger.debug("wild card list formed is : " + wildcardList);		
	
		return wildcardList.toString();
	}


  @Override
  public String getHost() {
    // TODO Auto-generated method stub
    return host;
  }


  @Override
  public String getUser() {
    // TODO Auto-generated method stub
    return user;
  }

	
} // end-of-class Ssh
