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

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * This class provides functionality to open/close an SSH2 connection to a
 * remote machine.
 * 
 * A session can be opened using one the following authentication protocols -
 * public-key - password - keyboard-interactive
 * 
 * This class should be used through SshSessionFactory class, which puts
 * sessions into a hashtable so you can refer to them with user@host later
 * 
 * There are three ways to enter the password. By default, a pop-up dialog is
 * used to enter it, but the stdin can be used for that as well or a socket
 * server. The password input method can be chosen through an environment
 * variable: KEPLER_PWD_INPUT_METHOD=[ POPUP | STDIN | SOCKET] default if not
 * defined: POPUP POPUP: pop-up dialog, good for runs within Vergil STDIN: print
 * on stdout, read pwd on stdin. Good for command-line runs. SOCKET: print to a
 * socket and read (plain) pwd from it. KEPLER_PWD_TERM_HOST=host and
 * KEPLER_PWD_TERM_PORT=port define the socket server which should send back the
 * pwd. Used when kepler is submitted as a job from a script on a cluster.
 * 
 * 
 * <p>
 * 
 * @author Norbert Podhorszki
 * 
 *         Based on Ssh2Exec Kepler actor and JSch examples Author of the Kepler
 *         actor: Ilkay Altintas, Xiaowen Xin
 */

public class SshSession {
  
	private Session session = null;
	private JSch jsch;
	private String user;
	private String host;
	private int port = 22; // ssh port, default is 22
	private Properties configuration;
	protected UserInfo userInfo = null;
	
	/**
	 * Method getUserInfo.
	 * @return UserInfo
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}

	public Session getJschSession() {
	  return session;
	}
	
	/**
	 * Method setUserInfo.
	 * @param userInfo UserInfo
	 */
	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}


	private final static int timeout = 60000;

	// port forwarding variables
	Vector<String> Rfwds = new Vector<String>();
	Vector<String> Lfwds = new Vector<String>();
	
	Set<Integer> LFwdPorts = new HashSet<Integer>();
	Set<Integer> RFwdPorts = new HashSet<Integer>();


	private static final Log log = LogFactory
			.getLog(SshSession.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	/**
	 * Constructor for SshSession.
	 * @param user String
	 * @param host String
	 * @param port int
	 */
	protected SshSession(String user, String host, int port) {
		/* get the single instance of JSch object */
		this.jsch = new JSch();
		this.user = user;
		this.host = host;
		if (port > 0)
			this.port = port;
		
		configuration = new java.util.Properties();
		configuration.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
		configuration.put("StrictHostKeyChecking", "no");
	}

	/**
	 * Set a local port forwarding before the connection is made. Format of the
	 * specification: lport:rhost:rport (int:string:int). lport on this host
	 * will be forwarded through this session and the remote host to the
	 * specified rhost:rport address.
	 * @param spec String
	 * @throws SshException
	 */
	protected void setPortForwardingL(String spec) throws SshException {
		if (spec == null || spec.length() == 0)
			throw new SshException("Port forwarding specification is empty: "
					+ spec);
		Lfwds.add(spec);
	}

	/**
	 * Set a remote port forwarding before the connection is made. Format of the
	 * specification: rport:lhost:lport (int:string:int). rport on remote host
	 * will be forwarded through this session and our local host to the
	 * specified lhost:lport address.
	 * @param spec String
	 * @throws SshException
	 */
	protected void setPortForwardingR(String spec) throws SshException {
		if (spec == null || spec.length() == 0)
			throw new SshException("Port forwarding specification is empty: "
					+ spec);
		Rfwds.add(spec);
	}

	/**
	 * Get an existing opened session or open a new session to user@host
	 * 
	 * @return Session
	 * @throws SshException
	 */
	public synchronized Session open() throws SshException {
		// System.out.println(" ++ SshSession.open() called");
		if (session == null || !session.isConnected()) {
			if (isDebugging)
				log.debug("SSH session " + user + "@" + host + ":" + port
						+ " is not connected; should be (re)connected now.");
     
			connect();
      
			SshEventRegistry.instance.notifyListeners(new SshEvent(
					SshEvent.SESSION_OPENED, user + "@" + host + ":" + port));

			// forward ports
			if (isDebugging)
				log.debug("Forward local ports:");
			for (String foo : Lfwds) {
				addPortForwardL(foo);
			}

			if (isDebugging)
				log.debug("Forward remote ports:");
			for (String foo : Rfwds) {
				addPortForwardR(foo);
			}

		}
		return session;
	}
	
	/**
	 * Add a local port forwarding to an open connection. Format of the
	 * specification: lport:rhost:rport (int:string:int). lport on this host
	 * will be forwarded through this session and the remote host to the
	 * specified rhost:rport address.
	 * @param spec String
	 */
	protected synchronized void addPortForwardL(String spec) {
		if(session != null && session.isConnected()) {
			int lport = Integer.parseInt(spec.substring(0, spec.indexOf(':')));
			String foo = spec.substring(spec.indexOf(':') + 1);
			String host = foo.substring(0, foo.indexOf(':'));
			int rport = Integer.parseInt(foo.substring(foo.indexOf(':') + 1));
			if (isDebugging)
				log.debug(" --> local " + lport
						+ " forwarded through remote host to " + host + ":"
						+ rport + ".");
 
			try {
				session.setPortForwardingL(lport, host, rport);
				LFwdPorts.add(lport);
			} catch (JSchException e) {
				log.warn("Port forwarding request failed on session " + user
						+ "@" + host + ": " + e);
			}
		}
	}
	
	/**
	 * Add a remote port forwarding to an open connection. Format of the
	 * specification: rport:lhost:lport (int:string:int). rport on remote host
	 * will be forwarded through this session and our local host to the
	 * specified lhost:lport address.
	 * @param spec String
	 */
	protected synchronized void addPortForwardR(String spec) {
		if(session != null && session.isConnected()) {
			int rport = Integer.parseInt(spec.substring(0, spec.indexOf(':')));
			String foo = spec.substring(spec.indexOf(':') + 1);
			String host = foo.substring(0, foo.indexOf(':'));
			int lport = Integer.parseInt(foo.substring(foo.indexOf(':') + 1));
			if (isDebugging)
				log.debug(" --> remote " + rport
						+ " forwarded through localhost to " + host + ":" + lport
						+ ".");
			try {
				session.setPortForwardingR(rport, host, lport);
				RFwdPorts.add(rport);
			} catch (JSchException e) {
				log.warn("Port forwarding request failed on session " + user + "@"
						+ host + ": " + e);
			}
		}
	}
	
	/**
	 * Remove a local port forwarding.
	 * @param port the local port that was forwarded.
	 * @param closeIfLast If true, and there are no additional local ports
	 * forwarded, close the connection.
	 * @throws SshException
	 */
	protected synchronized void removePortForwardL(int port, boolean closeIfLast) throws SshException {
		if(session != null && session.isConnected() && LFwdPorts.contains(port)) {
		   
		    // close the forwarded port
		    try {
                session.delPortForwardingL(port);
            } catch (JSchException e) {
                String msg = "Error stopping local forwarded port " + port +
                    " on session " + user + "@" + host + ": " + e;
                log.error(msg);
                throw new SshException(msg);
            }
            // remove port of set of forwarded ports and 
            // see if we should close connection
			LFwdPorts.remove(port);
			if(closeIfLast && LFwdPorts.size() == 0) {
				close();
			}
		}
	}

	/**
	 * Remove a remote port forwarding.
	 * @param port the remote port that was forwarded.
	 * @param closeIfLast If true, and there are no additional remote ports
	 * forwarded, close the connection.
	 * @throws SshException
	 */
	protected synchronized void removePortForwardR(int port, boolean closeIfLast) throws SshException {
		if(session != null && session.isConnected() && RFwdPorts.contains(port)) {
	        // close the forwarded port
            try {
                session.delPortForwardingR(port);
            } catch (JSchException e) {
                String msg = "Error stopping remote forwarded port " + port +
                    " on session " + user + "@" + host + ": " + e;
                log.error(msg);
                throw new SshException(msg);
            }
            // remove port of set of forwarded ports and 
            // see if we should close connection
			RFwdPorts.remove(port);
			if(closeIfLast && RFwdPorts.size() == 0) {
				close();
			}
		}
	}

	protected synchronized void close() {
		if (session != null && session.isConnected()) {
			try {
				session.disconnect();
			} catch (Exception e) {
				System.err
						.println("Exception caught in disconnecting the SSH session to "
								+ user + "@" + host);
				e.printStackTrace();
			}
			SshEventRegistry.instance.notifyListeners(new SshEvent(
					SshEvent.SESSION_CLOSED, user + "@" + host + ":" + port));
		}
		session = null;
		LFwdPorts.clear();
		RFwdPorts.clear();
	}

	/**
	 * Method getUser.
	 * @return String
	 */
	protected String getUser() {
		return user;
	}

	/**
	 * Method getHost.
	 * @return String
	 */
	protected String getHost() {
		return host;
	}

	/** Add an identity file, that can be used at ssh connections * @param identity String
	 * @throws SshException
	 */
	protected void addIdentity(String identity) throws SshException {
		String strIdentity = identity;
		if (strIdentity != null && strIdentity.length() > 0) {
			// Hack the path because we can't deal with "file:" or "file://"
			if (strIdentity.startsWith("file:")) {
				strIdentity = strIdentity.substring(5);
				if (strIdentity.startsWith("//")) {
					strIdentity = strIdentity.substring(2);
				}
			}

			// Add identity file string to the JSch object
			strIdentity = strIdentity.trim();
			if (!strIdentity.equals("")) {
				try {
					jsch.addIdentity(strIdentity);
				} catch (JSchException e) {
					log.error("Exception caught for file " + strIdentity + ": "
							+ e);
					throw new SshException(
							"Exception caught in JschSingleton.addIdentity of file "
									+ strIdentity + "\n("
									+ e.getClass().getName() + ")\n"
									+ e.getMessage());
				}
			}
		}
	}

	/**
	 * Get the password/passphrase/passcode for the specified third party
	 * machine.
	 * @param target String
	 * @return String
	 * @throws SshException
	 */
	protected static String getPwdToThirdParty(String target)
			throws SshException {

		if (target == null || target.trim().length() == 0)
			return null;

		// Get a session to the third party host
		String user, host;
		int port = 22;

		int atPos = target.indexOf('@');
		if (atPos >= 0)
			user = target.substring(0, target.indexOf('@'));
		else
			user = System.getProperty("user.name");

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
		} else
			host = target.substring(atPos + 1);

		// Open the session to the third party host, so it will be authenticated
		// at last here
		// if not yet authenticated
		SshSession thirdpartysession = SshSessionFactory.getSession(user, host,
				port);
		Session jschSession = thirdpartysession.open();
		try {
			// send alive messages every 30 seconds to avoid
			// drops when running long-lasting commands without much stdout
			jschSession.setServerAliveInterval(30000);
		} catch (JSchException ex) {
			log.warn("ServerAliveInterval could not be set for session "
					+ thirdpartysession.getUser() + "@"
					+ thirdpartysession.getHost() + ": " + ex);
		}

		// TODO: now close it if we do not use this session in the workflow
		// elsewhere

		// Now squeeze the secret out from the session
    String pwd = thirdpartysession.userInfo.getPassword();
    if (pwd == null)
      pwd = thirdpartysession.userInfo.getPassphrase();
    if (pwd == null) {
      if(thirdpartysession.userInfo instanceof VeloUserInfo)
        pwd = ((VeloUserInfo)thirdpartysession.userInfo).getPassPKI();
    }
    if (pwd == null) {
			log
					.error("Tried to use a third party authentication to "
							+ target
							+ " but there is no password, no private-key passphrase and no passcode known to it");
			return null;
		}

		return pwd;
	}

	// //////////////// Private Methods ///////////////////////

	/**
	
	 * @throws SshException
	 * @throws Exception
	 *             If the connection fails.
	 */
	private void connect() throws AuthCancelException, SshException {
		log.info("Connecting to " + user + "@" + host + ":" + port);
		try {
			session = jsch.getSession(user, host, port);
			// check whether ui is already set
			if (userInfo == null) {
			  // TODO: we need to get the user info from a provider
				//userInfo = new MyUserInfo();
				userInfo = new VeloUserInfo();
			}
			session.setUserInfo(userInfo);
			session.setConfig(configuration);
			session.connect(timeout);
		
		} catch (AuthFailedException e) {
		  // if authentication failed, throw it back up
		  throw e;
		  
		} catch (Exception e) {
      
			//first check if user cancelled auth prompt
		  if(e instanceof JSchException && e.getMessage().equals("Auth cancel")){
		    throw new AuthCancelException("User cancelled authentication");
		  }
		  
		  // a couple of possible exception messages that could happen here:
			// 1. java.io.FileNotFoundException
			// 2. session is down
		  log.error("Exception caught in SshSession.connect to " + user + "@"
					+ host + ":" + port + ". " + e);
			e.printStackTrace();
			throw new SshException("Exception caught in SshSession.connect to "
					+ user + "@" + host + ":" + port + "\n("
					+ e.getClass().getName() + ")\n" + e.getMessage());
		}
		// Connection succeeded, password/passhphrase was okay (do not know what
		// was used)
		if(userInfo instanceof VeloUserInfo) {
			((VeloUserInfo)userInfo).authWasSuccessful();
		} 
	}

}
