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

/**
 * This test connects to a host and deletes files matching the given mask. The
 * code must refuse deleting all files in / or . or .. However, be very careful
 * with the mask!!! This is rm -rf on remote machines. If host is 'local', the
 * delete will be performed with java File operations. Relative pathes in the
 * mask refers to home in remote machines, or to the current directory on local
 * machine.
 * 
 * Arguments: user@host mask string (enclose in "" if contains space)
 */
public class TestDelete {

	private static ExecInterface execObj;
	private static String user;
	private static String host;

	/**
	 * Method main.
	 * @param arg String[]
	 * @throws SshException
	 * @throws InterruptedException
	 */
	public static void main(String[] arg) throws SshException,
			InterruptedException {

		System.out.println("arg length = " + arg.length);
		String target = arg.length > 0 ? arg[0] : "local";
		String mask = arg.length > 1 ? arg[1] : "";

		System.out.println("target machine = " + target + "\nmask = " + mask);

		int atPos = target.indexOf('@');
		if (atPos >= 0)
			user = target.substring(0, target.indexOf('@'));
		else
			user = System.getProperty("user.name");

		host = target.substring(atPos + 1);

		boolean localExec = false;
		if (host.equals("local") || host.trim().equals("")) {
			// local exec
			System.out.println("Execution mode: local using Java Runtime");
			execObj = new LocalExec();
			localExec = true;
		} else {
			// create an SshExec object
			System.out.println("Execution mode: remote using ssh");
			execObj = new SshExec(user, host);
			// execObj.addIdentity(System.getProperty("user.home") +
			// File.separator +
			// ".ssh" + File.separator + "id_dsa");
		}

		try {
			boolean result = execObj.deleteFile(mask, true, true);
			System.out.println("Result: " + result);
		} catch (ExecException e) {
			System.out.println("Error: " + e);
		}

		// if (!LocalExec) execObj.closeConnection();
		System.exit(0);

	}

}