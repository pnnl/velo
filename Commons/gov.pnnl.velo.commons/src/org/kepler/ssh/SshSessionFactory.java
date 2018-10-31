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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides a factory to store SSH sessions. The reference to a
 * session is with "user@host". It should be used by the SshExec class only
 * 
 * <p>
 * 
 * @author Norbert Podhorszki
 * @version $Revision: 1.0 $
 */

public class SshSessionFactory {

	/* Private variables */
	private static Hashtable<String, SshSession> sessionsTable = new Hashtable<String, SshSession>();
	private static final Log log = LogFactory.getLog(SshSessionFactory.class.getName());
	
	/**
	 * Method getSessionKey.
	 * @param user String
	 * @param host String
	 * @param port int
	 * @return String
	 */
	private static String getSessionKey(String user, String host, int port) {
	  return user + "@" + host + ":" + port;
	}
	
  /**
   * Method getSession.
   * @param user String
   * @param host String
   * @param port int
   * @return SshSession
   */
  public synchronized static SshSession getSession(String user, String host, int port) {
    // by default, create new session if it does not already exist in the cache
    return getSession(user,host,port,true);
  }
  
	/**
	 * @param user
	 * @param host
	 * @param port
	 * @param createNew - set to true if you want to create a session automatically if it doesn't exist
	
	 * @return SshSession
	 */
	public synchronized static SshSession getSession(String user, String host, int port, boolean createNew) {
	  // System.out.println(" ++ SshSessionFactory.getSession() called for " +
	  // user + "@" + host + ":" + port);

	  SshSession session;
		String sessionKey = getSessionKey(user,host,port);
		if (port <= 0) {
			port = 22;
		}
		session = sessionsTable.get(sessionKey);
		if (session == null) {
			log.debug("Session DOES NOT exists in hashtable for " + sessionKey);
			if(createNew) {
			  session = new SshSession(user, host, port);
			  sessionsTable.put(sessionKey, session);
			}
		} else {
			log.debug("Session EXISTS in hashtable for " + sessionKey);
		}
		return session;
	}

}
