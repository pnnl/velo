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
 * '$Author: welker $'
 * '$Date: 2010-05-05 22:21:26 -0700 (Wed, 05 May 2010) $'
 * '$Revision: 24234 $'
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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides a factory to give you an object implementing the
 * ExecInterface. It provides either an LocalExec object or an RemoteExec object
 * based on the target you provide. RemoteExec object can either be an instance of
 * SshExec or GsiSshExec based on whether the remote host supports grid authentication
 * or not. The reference to a target is with "user@host:port". If port is not given,
 * first an attempt is made to connect using gssapi at port 2222. If that fails,
 * default to ssh at port 22.
 *
 * <p>
 *
 * @author Norbert Podhorszki
 */

//TODO refactor velo commons to either use this class instead of creating an sshExec object ourselves, or remove this class
@Deprecated 
public class ExecFactory {

    private static final String GSSAPI = "gssapi";
    private static final String SSH ="SSH";
    private ExecFactory() {
    }

    private static final Log log = LogFactory.getLog(ExecFactory.class.getName());
    private static final boolean isDebugging = log.isDebugEnabled();

    private static Hashtable<String,String> authenticationMethod = new Hashtable<String,String>();

    /**
     * Helper method to provide the target as a complete string. This method
     * calls the other one after processing the input string.
     * @param target String
     * @return ExecInterface
     * @throws ExecException */
    public static ExecInterface getExecObject(String target) throws ExecException 
    {
        // get USER
        String user, host;
        int port = -1;

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
                log.error("The port should be a number or omitted in "+ target);
            }
        } else
            host = target.substring(atPos + 1);
        return ExecFactory.getExecObject(user, host, port);
    }

    /**
     * Method getExecObject.
     * @param user String
     * @param host String
     * @return ExecInterface
     * @throws ExecException
     */
    @SuppressWarnings("unchecked")
    public static ExecInterface getExecObject(String user, String host)
        throws ExecException 
    {
        return getExecObject(user, host, -1);
    }

    /**
     * Return an object with ExecInterface, based on the specified target. It
     * will be a LocalExec if host is null, empty or equals 'local', otherwise
     * it will be an SshExec or GsiSshExec object based on the supported
     * authentication mechanism. Grid authentication is given priority over
     * ssh authentication. If no valid port is provided, first tries
     * grid authentication at port 2222, if that fails defaults to ssh
     * at port 22. The check for available authentications method is done only
     * once per remote host and cached for later use.
     * @param user String
     * @param host String
     * @param port int
     * @return ExecInterface
     * @throws ExecException
     */
    @SuppressWarnings("unchecked")
    public static ExecInterface getExecObject(String user, String host, int port)
        throws ExecException 
    {

        if (host == null || host.trim().equals("")
                || host.equals("localhost") || host.equals("local") ) {
            // local execution
            if (isDebugging) {
                log.debug("Provide LocalExec");
            }
            return  new LocalExec();
        } else {
        	InetAddress localHost = null;
			try {
				localHost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				//if unable to get localhost address
				//use remoteexec
			}
        	if(localHost!=null && 
        			(host.equals(localHost.getCanonicalHostName())
        			|| host.equals(localHost.getHostAddress()))        			
        	){
        		return new LocalExec();
        	}
            return getRemoteExec(user, host, port);
        }
    }

    /**
     * Method getExecByEarlierMethod.
     * @param user String
     * @param host String
     * @param port int
     * @return ExecInterface
     * @throws ExecException
     */
    @SuppressWarnings("unchecked")
    private static ExecInterface getExecByEarlierMethod(String user, String host, int port) 
        throws ExecException 
    {

        String am = authenticationMethod.get(user + "@" + host + ":" + port);
        if (GSSAPI.equals(am)) {
            log.info("Providing new GsiSshExec for " + user + "@" + host + ":" + port);
            return new GsiSshExec(user,host,port);
        } else if (SSH.equals(am)) {
            log.info("Providing new SshExec for " + user + "@" + host + ":" + port);
            return new SshExec(user,host,port);
        }
        return null;
    }

    /**
     * Method getRemoteExec.
     * @param user String
     * @param host String
     * @param port int
     * @return ExecInterface
     * @throws ExecException
     */
    @SuppressWarnings("unchecked")
    private static ExecInterface getRemoteExec(String user, String host, int port) 
        throws ExecException 
    {

        //REMOTE HOST
        //Check if the server support grid authentication
        ExecInterface execObj = null;
        String target = user + "@" + host + ":" + port;
        String cert = System.getProperty("X509_USER_CERT");
        String proxy = System.getProperty("X509_USER_PROXY");
        boolean trygsi = (cert != null || proxy != null); 
                
        // check if we had a factory call for this target before
        if (port>0) {
            execObj = getExecByEarlierMethod (user, host, port);
        } else {
            if (trygsi)
                execObj = getExecByEarlierMethod (user, host, 2222);
            if (execObj == null)
                execObj = getExecByEarlierMethod (user, host, 22);
        }
        if (execObj != null) 
            return execObj;

        // try the GSI options first
        if (trygsi) {
            int testport = port;
            if (port > 0) {
                execObj = getGsisshExec(user, host, port);
            } else {
                // Try port 2222 
                testport = 2222;
                execObj = getGsisshExec(user, host, testport);
                if (execObj == null) {
                    // Try port 22
                    testport = 22;
                    execObj = getGsisshExec(user, host, testport);
                }
            }
            
            if (execObj != null) {
                // we have a GSI-SSH server
                authenticationMethod.put(user + "@" + host + ":" + testport, GSSAPI);
                log.info("Providing new GsiSshExec for " + user + "@" + host + ":" + testport);
                return execObj;
            }
        } // end if (trygsi)

        // last chance is a traditional SSH connection
        if (port>0) {
            log.info("Providing new SshExec for " + user + "@" + host + ":" + port);
            execObj = new SshExec(user, host, port);
            authenticationMethod.put(user + "@" + host + ":" + port, SSH);
        } else {
            log.info("Providing new SshExec for " + user + "@" + host + ":22");
            execObj = new SshExec(user, host, 22);
            authenticationMethod.put(user + "@" + host + ":22", SSH);
        }
        return execObj;
    }

    /**
     * Method getGsisshExec.
     * @param user String
     * @param host String
     * @param port int
     * @return GsiSshExec
     */
    private static GsiSshExec getGsisshExec(String user, String host, int port) {
        log.debug("Try GSI server at " + user + "@" + host + ":"+ port);
        GsiSshExec result = null;
        GsiSshExec temp = new GsiSshExec(user,host,port);
        List availableMethods =  new ArrayList();
        try {
            availableMethods = temp.getAvailableAuthMethods();
            log.info("Available authentication mechanism for "+ user + "@" + host + ":"+ port +" -- " + availableMethods );
        } catch(SshException e) {
            temp.closeConnection();
            log.warn("Unable to retrieve available authentication mechanisms.");
        }
        if(availableMethods==null){
        	log.warn("Unable to retrieve available authentication mechanisms.");
        	temp.closeConnection();
        }
        else if (availableMethods.contains(GSSAPI)) {
            result = temp;
        } else {
            temp.closeConnection();
        }
        return result;
    }



}
