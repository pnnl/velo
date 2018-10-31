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
 * '$Author: berkley $'
 * '$Date: 2010-04-27 17:12:36 -0700 (Tue, 27 Apr 2010) $'
 * '$Revision: 24000 $'
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.hosts.DialogKnownHostsKeyVerification;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.EKEYXAuthenticationClient;
import com.sshtools.j2ssh.authentication.GSSAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.connection.ChannelInputStream;
import com.sshtools.j2ssh.session.SessionChannelClient;

/**
 * This class provides functionality to use an GSISSH session to execute remote
 * commands and to transfer files on a remote machine.
 * <p>
 * To create a gsissh session the code needs a valid proxy certificate and a
 * valid CA certificate/ trusted certificate for the server.
 * The code searches for the proxy
 * certificates and CA certificates at default locations based on the
 * specifications of underlying jglobus package --
 * http://www-unix.globus.org/cog/distribution/1.6.0/docs/configuring.html
 * By default it looks for proxy certificates in the system's temp directory
 * and for CA certificates(trusted certificates) in
 * ${user.home}/.globus/certificates and /etc/grid-security/certificates.
 * You can alter the default file and search path by creating appropriate
 * entries in $HOME/.globus/cog.properties. Please refer to
 * http://www-unix.globus.org/cog/distribution/1.6.0/docs/configuring.html
 * for more configuration details.
 * <p>
 *
 * You can create several sessions to different sites and use any of them
 * referring to it with its user@host:port. If port is not specified defaults
 * to 2222. Once instantiated the GsiSsh object can be used by anyone anytime
 * to copy file or execute commands without authenticating again.
 * <p>
 *
 * @author Chandrika Sivaramakrishnan
 *
 *  This uses a modified code of the open source product GSI-SSHTerm.
 *  http://www.ngs.ac.uk/tools/gsisshterm
 *  Since GSI-SSHTerm is a terminal product most of the error scenarios and
 *  user interaction scenarios are handled using GUI popups. This part of
 *  the GSI-SSHTerm code hasn't been modified and hence this code will fail when
 *  user interaction are to be handled through command prompt. For example,
 *  when kepler is run without gui. In such cases use this with an existing
 *  valid proxy certificate
 */
public class GsiSshExec extends RemoteExec {

	// variables specific to GSI SSH
	protected SshClient sshClient;
	protected SshConnectionProperties connectionProfile = new SshToolsConnectionProfile();

	//variables common to SshExec and GsiSshExec
	private int timeout = 0; // timeout in seconds
	private boolean forcedCleanUp = false;
	private static String cleanUpInfoCmd = new String("echo $$; ");
	// restart timer if stdout has data
	private boolean timeoutRestartOnStdout = false;
	// restart timer if stderr has data
	private boolean timeoutRestartOnStderr = false;

	private boolean pseudoTerminal = false;
	private String protocolPath = "";
	private String cmdLineOptions = "";
	private String targetStr="";
    private List availableAuthMethods;

	//Logging
	private static final Log log = LogFactory.getLog(GsiSshExec.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	/**
	 * Constructor for GsiSshExec.
	 * @param user String
	 * @param host String
	 */
	public GsiSshExec(String user, String host) {
	    this.user = user;
	    this.host = host;
	    this.port = 2222;
		//session = SshSessionFactory.getSession(user, host, 22);
		//session is not cached currently
		sshClient  = GsiSshSessionFactory.getSshClient(user, host, 2222);
		connectionProfile.setHost(host);
		connectionProfile.setUsername(user);

		targetStr = connectionProfile.getUsername() + "@"
					+ connectionProfile.getHost();
	}

	/**
	 * Constructor for GsiSshExec.
	 * @param user String
	 * @param host String
	 * @param port int
	 */
	public GsiSshExec(String user, String host, int port) {
  	    this.user = user;
        this.host = host;
        this.port = port;
		sshClient  = GsiSshSessionFactory.getSshClient(user, host, port);
		connectionProfile.setHost(host);
		connectionProfile.setUsername(user);
		connectionProfile.setPort(port);

		targetStr = connectionProfile.getUsername() + "@"
					+ connectionProfile.getHost() + ":"
					+ connectionProfile.getPort();
	}


	/**
	 * Constructor for GsiSshExec.
	 * @param target String
	 */
	public GsiSshExec(String target) {
		// get USER
		String user, host;
		int port = 2222;

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
				log
						.error("The port should be a number or omitted in "
								+ target);
			}
		}else{
			host = target.substring(atPos + 1);
		}
		
		this.user = user;
        this.host = host;
        this.port = port;

		sshClient  = GsiSshSessionFactory.getSshClient(user, host, port);
		connectionProfile.setHost(host);
		connectionProfile.setUsername(user);
		connectionProfile.setPort(port);

		targetStr = connectionProfile.getUsername() + "@"
					+ connectionProfile.getHost() + ":"
					+ connectionProfile.getPort();

	}

	/**
	 * Method copyFrom.
	 * @param sourcePath String
	 * @param localPath File
	 * @param recursive boolean
	 * @return int
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyFrom(String, File, boolean)
	 */
	public int copyFrom(String sourcePath, File localPath, boolean recursive)
			throws SshException {
		int numberOfCopiedFiles = 0;
		if(recursive){
			throw new SshException("Recursive copy is not supported on grid servers");
		}
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");
		try{
			SftpClient sftpclient = new SftpClient(sshClient);
			if(localPath == null){
				sftpclient.get(sourcePath);
			}else{
				String path = localPath.getAbsolutePath();
				int index = -1;
				if(localPath.isDirectory()){
					index = sourcePath.lastIndexOf('/');
					if(index>-1 && index < sourcePath.length()-1){
						path = path + System.getProperty("file.separator") + sourcePath.substring(index+1);
					} else {
						path = path + System.getProperty("file.separator") + sourcePath;
					}
				}
				sftpclient.get(sourcePath,path);
			}
			numberOfCopiedFiles = 1;
		}catch(Exception e){
			throw new SshException("Exception caught at while copying file: " + sourcePath
					+ "\n" + e);
		}
		return 0;
	}

	/**
	 * Copy _one_ local file to a remote directory Input: file of type File
	 * (which can be a directory) Input must not have wildcards. targetPath is
	 * either a directory or filename
	 *
	 * @param lfile File
	 * @param targetPath String
	 * @param recursive boolean
	 * @return number of files copied successfully SshException is thrown in
	 *         case of error. * @throws SshException
	 */
	protected int _copyTo(File lfile, String targetPath, boolean recursive)
			throws SshException {

		if (!lfile.exists()) {
			throw new SshException("File does not exist: " + lfile);
		}

		// check: recursive traversal of directories enabled?
		if (lfile.isDirectory()) {
//			if (!recursive)
//				throw new SshException("File " + lfile
//						+ " is a directory. Set recursive copy!");
			throw new SshException(lfile.getName()
					+ " is a directory. Recursive copy is not supported on grid servers");
		}

		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");

		// at this point we have a living, opened session to the remote machine
		int numberOfCopiedFiles = 0;
    	try{
    	String path = lfile.getAbsolutePath();
    	SftpClient sftpclient = new SftpClient(sshClient);

    	//by default preserves file attributes
    	if(targetPath==null || targetPath.trim().equals("")){
    		sftpclient.put(path);
    	}else{
    		sftpclient.put(path,targetPath);
    	}
    	// if there is no exception return 1
    	numberOfCopiedFiles = 1;
		} catch (Exception e) {
			throw new SshException("Exception caught at while copying file: " + lfile
					+ "\n" + e);
		}

		return numberOfCopiedFiles;
	}

	/**
	 * Method executeCmd.
	 * @param command String
	 * @param streamOut OutputStream
	 * @param streamErr OutputStream
	 * @return int
	 * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#executeCmd(String, OutputStream, OutputStream)
	 */
	public int executeCmd(String command, OutputStream streamOut,
			OutputStream streamErr) throws ExecException {
		return executeCmd(command, streamOut, streamErr, null);
	}

	/**
	 * Method executeCmd.
	 * @param command String
	 * @param streamOut OutputStream
	 * @param streamErr OutputStream
	 * @param thirdPartyTarget String
	 * @return int
	 * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#executeCmd(String, OutputStream, OutputStream, String)
	 */
	public int executeCmd(String command, OutputStream streamOut,
			OutputStream streamErr, String thirdPartyTarget)
			throws ExecException {

		int exitCode = -1;
		SessionChannelClient sessionChannel;
		_streamReaderThread readerThread;
		boolean commandSuccess;
		ChannelInputStream stderrInputStream;

		// get the pwd to the third party if necessary
		String thirdPartyPwd = getPwdToThirdParty(thirdPartyTarget);
		// Form the command
		String updatedCmd = forcedCleanUp ? cleanUpInfoCmd + command : command;

		openConnection();
		log.debug("Connected");

		//int result = copyTo(new File("C:\\Chandrika\\test.txt"),"/home/chandrika",false);
		//return result;

		try {

			// Create piped stream if password has to be fed
			PipedInputStream pis = null;
			PipedOutputStream pos = null;
			if (thirdPartyPwd != null) {
				try {
					pis = new PipedInputStream();
					pos = new PipedOutputStream(pis);
				} catch (IOException ex) {
					log
							.error("Error when creating the piped stream for password feededing: "
									+ ex);
					throw new ExecException(
							"Error when creating the piped stream for password feededing: "
									+ ex);
				}
				// setting pseudo terminal to true so that prompt for password
				// can be read
				// SshExec uses ptyexec.c instead of using pseudo terminal
				pseudoTerminal = true;
			}

			// Open channel and execute command
			synchronized (sshClient) {

				// Once authenticated the user's shell can be started
				// Open a session channel
				sessionChannel = sshClient.openSessionChannel();
				// create pseudo terminal if user has asked for it
 	                        if (pseudoTerminal) {
					sessionChannel.requestPseudoTerminal("vt100", 80, 24, 640,
							480, null);
				}
                                if(thirdPartyPwd!=null){
				sessionChannel.bindInputStream(pis);
                                }
				stderrInputStream = (ChannelInputStream) sessionChannel
						.getStderrInputStream();
				readerThread = new _streamReaderThread(sessionChannel,
						sessionChannel.getInputStream(), streamOut,
						thirdPartyPwd, pos);
				readerThread.start();
				commandSuccess = sessionChannel.executeCommand(updatedCmd);
			}

			log.debug("boolean command excution result is" + commandSuccess);
			// command has finished executing. wait for the reader thread to
			// finish reading output
			// It will timeout at the latest if the command does not finish
			// 3 ways to finish:
			// - command terminates
			// - timeout
			// - IOException when reading the command's output or writing
			// the caller's output
			readerThread.join();

			// on timeout finish here with a nice Exception
			if (readerThread.timeoutHappened()) {
				log.error("Timeout: " + timeout + "s elapsed for command "
						+ command);
				if (sessionChannel.isOpen()) {
					sessionChannel.close(); //closes channels and frees channel
				}
				if (forcedCleanUp) {
					// time for clean-up ;-)
					kill(readerThread.getProcessID(), true);
				}
				throw new ExecTimeoutException(command);
			}

			// if we cannot process output, still wait for the channel to be
			// closed
			// !!! This can lead to hang-up !!!
			while (!sessionChannel.isClosed()) {
				try {
					log.debug("Waiting for channel to close");
					Thread.sleep(500);
				} catch (Exception e) {
				}
			}
			Integer temp = sessionChannel.getExitCode();
			if (temp != null) {
				exitCode = temp.intValue();
			}
			// disconnect for now.
			//Remove this once session cache is implemented. If session is cached,
			//should call closeConnection to close the session
			//closeConnection();

			if (exitCode != 0 && forcedCleanUp) {
				kill(readerThread.getProcessID(), true);
			}
			//update streamErr with contents from stderrInputStream
			byte[] b = new byte[1024];
			int numread = stderrInputStream.read(b , 0, 1024);
			while (numread != -1) {
				streamErr.write(b, 0, numread);
				numread = stderrInputStream.read(b, 0, 1024);
			}

			// FORDEBUG
			/*System.out.println("Output===" + streamOut.toString());
			System.out.println("==Checking for error==");
			System.out.println("Error== " + streamErr.toString());
			System.out.println("Exiting GsiExec " );
			*/// FORDEBUG

		} catch (Exception e) {
			throw new ExecException(
					"Exception occured while executing command " + command
							+ "\n" + e);
		}
		return exitCode;
	}
	
	@Override
	public int executeTail(String command, OutputStream streamOut,
			OutputStream streamErr, String[] env, File workingdir,
			ConcurrentLinkedQueue<String> queue) throws ExecException {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public void setEndTail(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Method getAvailableAuthMethods.
	 * @return List
	 * @throws SshException
	 */
	public List getAvailableAuthMethods() throws SshException{
		try {
			if(sshClient == null){
				return null;
			}
			else if (!sshClient.isConnected()){
				openConnection(false);
			}else{
				log.debug("Connection already opened");
			}
			synchronized(sshClient){
				availableAuthMethods = sshClient.getAvailableAuthMethods(connectionProfile.getUsername());
			}
                        return availableAuthMethods;
		} catch (Exception e) {
			log.error("Error getting the supported authentication mechanism for "
					+ targetStr + "\n" + e.toString());
			throw new SshException("Unable to connect to / get the list of authentication from "
					+targetStr);
		}
	}
	/**
	 * Method openConnection.
	 * @return boolean
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#openConnection()
	 */
	public boolean openConnection() throws SshException{
		return openConnection(true);
	}
	/**
	 * Method openConnection.
	 * @param authenticate boolean
	 * @return boolean
	 * @throws SshException
	 */
	public boolean openConnection(boolean authenticate) throws SshException {
		if (null == sshClient)
			return false;
		if (sshClient.isConnected() && sshClient.isAuthenticated())
			return true;


		//Two step process - 1. Connect 2.authenticate
		synchronized(sshClient){
			//log.debug("BEGIN: Open connection");
			if(!sshClient.isConnected()){
				try {
					// Should pass instance of SshToolsConnectionProfile when using gss
					// authentication
					//System.out.println("Connecting using sshClient.connect()........");
					sshClient.connect(connectionProfile,
							new DialogKnownHostsKeyVerification(null));
					//log.debug("###Connection established successfully");
					// //TODO:Run long running job to test if session would be alive
				} catch (Exception e) {
					//log.debug("END: Open connection.Error connecting to "+ targetStr);
					throw new SshException("Error connecting to " + targetStr + " \n"
							+ e);
				}
			}
			/*
			 * Once this code has executed and returned the connection is ready for
			 * authentication
			 */
			if(authenticate && !sshClient.isAuthenticated()){
				boolean result = authenticate(false);
				//log.debug("END: Open connection. Returning connection after call to authenticate");
				return result;
			} else{
				//log.debug("END: Open connection. Returning connection without authenticating");
				return true;
			}
		}
	}

	/**
	 * Method authenticate.
	 * @param externalkeyx boolean
	 * @return boolean
	 * @throws SshException
	 */
	public boolean authenticate(boolean externalkeyx) throws SshException {
		boolean authenticated = false;
		if (null == sshClient)
			return authenticated;

		SshAuthenticationClient authClient;
		 // If the server supports external key exchange authenticate using that
		//if(externalkeyx){
                if (availableAuthMethods!=null && availableAuthMethods.contains("external-keyx")) {
			authClient =  new EKEYXAuthenticationClient();
			authClient.setHostname(connectionProfile.getHost());
			authClient.setUsername(connectionProfile.getUsername());
			authClient.setProperties(connectionProfile);
			int result;
			try {
                                log.debug("Authenticating using external-keyx");
				result = sshClient.authenticate(authClient, connectionProfile
						.getHost());
                                log.debug("Authentication complete");

			} catch (Exception e) {
				throw new SshException("Exception authenticating to " + targetStr
						+ " using external-keyx \n" + e);
			}
			if (result == AuthenticationProtocolState.COMPLETE) {
				log.debug("Authentication to host " + connectionProfile.getHost()
						+ " using external-keyx was successful");
				return true;
			}
		}

		authClient = new GSSAuthenticationClient();
		// username and host name have to be set explicitly even though they
		// are in properties - bug in GSI-SSHTerm code?
		authClient.setHostname(connectionProfile.getHost());
		authClient.setUsername(connectionProfile.getUsername());
		authClient.setProperties(connectionProfile);

		// Authenticate the user
		// Authentication result could be partial- when does this occur?
		// TODO:Handle partial authentication
		int result;
		try {
                        log.debug("Authenticating using gssapi");
			result = sshClient.authenticate(authClient, connectionProfile
					.getHost());
                        log.debug("Authenticating using gssapi complete");
		} catch (Exception e) {
			throw new SshException("Exception authenticating to " + targetStr
					+ "\n" + e);
		}
		if (result == AuthenticationProtocolState.COMPLETE) {
			log.debug("Authentication to host " + connectionProfile.getHost()
					+ "  using gssapi was successful");
			return true;
		} else {
			throw new SshException("Authentication failed for: " + targetStr);
		}

	}

	/**
	 * Method closeConnection.
	 * @see org.kepler.ssh.ExecInterface#closeConnection()
	 */
	public void closeConnection(){

		if (sshClient!=null && sshClient.isConnected()) {
			sshClient.disconnect();
		}
	}

	/**
	 * Method setForcedCleanUp.
	 * @param forcedCleanUp boolean
	 * @see org.kepler.ssh.ExecInterface#setForcedCleanUp(boolean)
	 */
	public void setForcedCleanUp(boolean forcedCleanUp) {
		this.forcedCleanUp = forcedCleanUp;
	}

	/**
	 * Method setTimeout.
	 * @param seconds int
	 * @param stdout boolean
	 * @param stderr boolean
	 * @see org.kepler.ssh.ExecInterface#setTimeout(int, boolean, boolean)
	 */
	public void setTimeout(int seconds, boolean stdout, boolean stderr) {
		this.timeout = seconds;
		timeoutRestartOnStdout = stdout;
		timeoutRestartOnStderr = stderr;

	}

        /**
         * addIdentity, useless for local exec 
         * @param identity String
         * @see org.kepler.ssh.ExecInterface#addIdentity(String)
         */
        public void addIdentity(String identity) { 
            // do nothing 
        }

	/**
	 * port forwarding not working on local exec
	 * @param spec String
	 * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#setPortForwardingL(String)
	 */
	public void setPortForwardingL(String spec) throws ExecException {
            // do nothing
	}

	/**
	 * port forwarding not working on local exec
	 * @param spec String
	 * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#setPortForwardingR(String)
	 */
	public void setPortForwardingR(String spec) throws ExecException {
            // do nothing
	}

	/**
	 * Method getForcedCleanUp.
	 * @return boolean
	 */
	@Override
	public boolean getForcedCleanUp() {
		return forcedCleanUp;
	}

	/**
	 * Method getTimeout.
	 * @return int
	 */
	@Override
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

	// ////////////Private class///////////////////////
	/**
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
		private PipedOutputStream pos; // the pipe-in to the stdin of the
		// remote
		// command
		private SessionChannelClient channel;

		private StringBuffer processID; // the remote command's shell's process
		// id (to kill if needed)
		private boolean timeoutReached; // becomes true on timeout

		/**
		 * Constructor for _streamReaderThread.
		 * @param sessionChannel SessionChannelClient
		 * @param in InputStream
		 * @param out OutputStream
		 * @param pwd String
		 * @param pos PipedOutputStream
		 */
		public _streamReaderThread(SessionChannelClient sessionChannel,
				InputStream in, OutputStream out, String pwd,
				PipedOutputStream pos) {
			try {
				isr = new InputStreamReader(in, "utf-8");
				osw = new OutputStreamWriter(out, "utf-8");
			} catch (UnsupportedEncodingException ex) {
				// get the default encoding
				isr = new InputStreamReader(in);
				osw = new OutputStreamWriter(out);
			}

			channel = sessionChannel;
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
			log.debug("In run");
			char[] tmp = new char[1024];
			boolean checkForPwd = (pwd != null);
			processID = new StringBuffer();

			// variables for the timeout checking
			long start = System.currentTimeMillis();
			long current = 0;
			long maxtime = timeout * 1000L;

			while (true) { // read command's output until termination or
				// timeout
				int len = 0;
				int j = 0;
				try {
					while (isr.ready()) { // we do not want to block on read
						// because we are counting for
						// timeout
						//System.out.println("In isr.ready");
						len = isr.read(tmp, 0, 1024);

						if (len < 0) {
							if (isDebugging)
								log
										.debug("Read error on stdout stream: "
												+ len);
							break; // break the reading loop
						}
						j = 0;

						// first line is remote process id in case of
						// forcedCleanUp. Filter here
						if (!cleanUpInfoProcessed) {
							// if (isDebugging)
							// log.debug("cleanup info string: " + new
							// String(tmp, 0, len));
							for (; j < len; j++) {
								if (tmp[j] == '\n') {
									cleanUpInfoProcessed = true; // done
									j++;
									if (isDebugging)
										log.debug("Remote process id = "
												+ processID);
									break; // break the reading loop
								}
								processID.append(tmp[j]);
							}
							// Note: j<=len here
						}

						// print the buffer to the output stream
						osw.write(tmp, j, len - j);
						osw.flush(); // send it really if someone is polling
						// it
						// above us
						if (timeoutRestartOnStdout)
							start = System.currentTimeMillis(); // restart
						// timeout timer
						//System.out.println(" %%% "
						//		+ new String(tmp, j, len - j));
						// if (timeoutRestartOnStdout)
						// start = System.currentTimeMillis(); // restart
						// // timeout timer
						String tempStr = new String(tmp, j, len - j);
						if (tempStr.contains("RSA key fingerprint is")
								&& tempStr.trim().endsWith("(yes/no)?")) {
							boolean userInput = promptYesNo(tempStr);
							// boolean userInput = true;
							log.debug("Prompt for host verification: "
									+ tempStr);
							if (userInput) {
								pos.write("yes\n".getBytes());
								pos.flush();
								log
										.info("Added destination server to known_hosts of source");
								continue;
							} else {
								pos.write("no\n".getBytes());
								pos.flush();
								pos.close();
								log
										.error("Failed to accept RSA key fingerprint");
								break;
							}
						}

						if (checkForPwd && containsPasswordRequest(tmp, j, len)) {
							// now feed the password to the process
							try {

								pos.write(pwd.getBytes());
								// log.info("Sent password ");
								pos.write("\n".getBytes());
								// log.info("Sent newline ");
								pos.flush();
								// log.info("Flushed pos ");
								pos.close();
								log.info("Sent password to third party.");
							} catch (IOException ex) {
								log
										.error("Error when feeding the password to the piped stream: "
												+ ex);
							} catch (Exception e) {
								log
										.error("Error when feeding the password to the piped stream: "
												+ e);
							}
							checkForPwd = false;
						}
					} // end while isr.ready
				} catch (IOException ex) {
					log
							.error("Error on the remote streams. Exiting reader thread: "
									+ ex);
					break; // exit the loop
				}
				if (channel.isClosed()) {
					System.out.println("Channel is closed");
					break; // exit the loop
				}
				try {
					Thread.sleep(500);
				} catch (Exception e) {
				}

				// check timeout
				current = System.currentTimeMillis();
				if (timeout > 0 && maxtime < current - start) {
					log.debug("Reader thread detected timeout: " + timeout
							+ "s elapsed");
					timeoutReached = true;
					break; // exit the loop
				}
			} // end of while (true)

			try {
				osw.close();
			} catch (IOException ex) {
				log.error("Cannot flush and close the output stream: " + ex);
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
						log.info("PWDSearch: found request for password.");
						return true;
					} else if (i < endPos - 7
							&& Character.toLowerCase(buf[i + 4]) == 'c'
							&& Character.toLowerCase(buf[i + 5]) == 'o'
							&& Character.toLowerCase(buf[i + 6]) == 'd'
							&& Character.toLowerCase(buf[i + 7]) == 'e') {
						log.info("PWDSearch: found request for passcode.");
						return true;
					} else if (i < endPos - 9
							&& Character.toLowerCase(buf[i + 4]) == 'p'
							&& Character.toLowerCase(buf[i + 5]) == 'h'
							&& Character.toLowerCase(buf[i + 6]) == 'r'
							&& Character.toLowerCase(buf[i + 7]) == 'a'
							&& Character.toLowerCase(buf[i + 8]) == 's'
							&& Character.toLowerCase(buf[i + 9]) == 'e') {
						log.info("PWDSearch: found request for passphrase.");
						return true;
					}
				}
				i = i + 1;
			}
			return false;
		}
	} // end inner class _streamReaderThread

	/**
	 * Method promptYesNo.
	 * @param str String
	 * @return boolean
	 */
	private boolean promptYesNo(String str) {
//		Object[] options = { "yes", "no" };
//		int foo = JOptionPane.showOptionDialog(null, str, "Warning",
//				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null,
//				options, options[0]);
//		return foo == 0;
		return true; // Used for prompts like confirming machine's rsa finger print
	}

	/**
	 * Method getPwdToThirdParty.
	 * @param target String
	 * @return String
	 * @throws ExecException
	 */
	private String getPwdToThirdParty(String target) throws ExecException {
		String pwd = null;
		if (target == null || target.trim().length() == 0)
			return null;
		//check if target is a ssh server
		ExecInterface  temp = ExecFactory.getExecObject(target);
		if(temp instanceof SshExec){
			pwd = SshSession.getPwdToThirdParty(target);
		}
		// For grid servers we are currently supporting
		// grid auth only thro' existing proxy. so return null
		return pwd;
	}

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public String getUser() {
    return user;
  }

  @Override
  public int getPort() {
    return port;
  }



}
