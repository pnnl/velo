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

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sshtools.j2ssh.SshClient;


/**
 * This class provides a factory to store gsi ssh sessions. The reference to a
 * session is with "user@host:port". It should be used by the GsiSshExec class only.
 * This is based on SshSessionFactory written by Norbert Podhorszki
 * <p>
 * @author Chandrika Sivaramakrishnan
 */
public class GsiSshSessionFactory {

	/* Singleton object */
	protected final static GsiSshSessionFactory instance = new GsiSshSessionFactory();

	/* Private variables */
	private static Hashtable<String,SshClient> sessionClients = new Hashtable<String, SshClient>();

	private static final Log log = LogFactory.getLog(GsiSshSessionFactory.class
			.getName());
	private GsiSshSessionFactory() {
	}

	// protected synchronized static SshSession getSession( String user, String
	// host ) {
	// return getSession(user, host, 22); // default port 22
	// }

	/**
	 * Method getSshClient.
	 * @param user String
	 * @param host String
	 * @param port int
	 * @return SshClient
	 */
	protected synchronized static SshClient getSshClient(String user,
			String host, int port) {
		SshClient sshClient;
		if (port <= 0)
			port = 22;
		log.debug(" ++ GsisshSessionFactory.getSshClient() called for " +
		 user + "@" + host + ":" + port);
		sshClient = (SshClient) sessionClients
				.get(user + "@" + host + ":" + port);
		if (sshClient == null) {
			log.debug("SshClient does NOT exists in hashtable for "+
					 user + "@" + host + ":" + port);
			sshClient = new SshClient();

			sessionClients.put(user + "@" + host + ":" + port, sshClient);
		}else {
			log.debug("Session EXISTS in hashtable for "+
					 user + "@" + host + ":" + port);
		}
		return sshClient;
	}
}
