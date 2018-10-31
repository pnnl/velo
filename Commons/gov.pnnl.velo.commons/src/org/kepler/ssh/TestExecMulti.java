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

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * This test connects to a host and executes two commands at once within the
 * same session. Moreover, it repeats each command N times. You should be asked
 * for password at most once, if the @user.dir@/.ssh/id_dsa private key does not
 * exist or is not valid for the selected host.
 * 
 * Arguments: user@host command1 string (enclose in "" if contains space)
 * command2 string (enclose in "" if contains space) number of iteration
 */
public class TestExecMulti {

	private static SshExec ssh;
	private static SshTest sshtest1;
	private static SshTest sshtest2;
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
		String target = arg.length > 0 ? arg[0] : "pnorbert@localhost";
		String command = arg.length > 1 ? arg[1] : "ls";
		String command2 = arg.length > 2 ? arg[2] : "w";
		String sIteration = arg.length > 3 ? arg[3] : "1";
		int iteration = Integer.parseInt(sIteration);

		System.out.println("remote machine = " + target + "\ncommand1 = "
				+ command + "\ncommand2 = " + command2 + "\niteration = "
				+ iteration);

		int atPos = target.indexOf('@');
		if (atPos >= 0)
			user = target.substring(0, target.indexOf('@'));
		else
			user = System.getProperty("user.name");

		host = target.substring(atPos + 1);

		ssh = new SshExec(user, host);
		ssh.addIdentity(System.getProperty("user.home") + File.separator
				+ ".ssh" + File.separator + "id_dsa");
		ssh.openConnection();

		sshtest1 = new SshTest(command, 1, iteration);
		sshtest2 = new SshTest(command2, 2, iteration);

		// sshtest1.run();
		// sshtest2.run();
		Thread t1 = new Thread(sshtest1);
		Thread t2 = new Thread(sshtest2);

		t1.start();
		t2.start();
		t1.join();
		t2.join();

		ssh.closeConnection();
		System.exit(0);

	}

	public static class SshTest implements Runnable {
		String command;
		int id;
		int iteration;

		/**
		 * Constructor for SshTest.
		 * @param command String
		 * @param id int
		 * @param iteration int
		 */
		public SshTest(String command, int id, int iteration) {
			this.id = id;
			this.command = command;
			this.iteration = iteration;
		}

		public void run2() {
			for (int i = 1; i <= iteration; i++) {
				System.out.println("    " + id + ": Iteration " + i
						+ " command: " + command);

				String streamOut = new String("      The output is " + command
						+ ".");
				String streamErr = new String("      The stderr is " + command
						+ ".");

				System.out.println("    " + id + ":---- output stream -----\n"
						+ streamOut + "\n      ----- error stream ------\n"
						+ streamErr + "\n      ---------------------------\n");
				System.out.println("");
				try {
					Thread.sleep(1000L - id * 200);
				} catch (Exception ex) {
					;
				}
			}
		}

		/**
		 * Method run.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			for (int i = 1; i <= iteration; i++) {
				System.out.println("    " + id + ": Iteration " + i
						+ " command: " + command);

				ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				ByteArrayOutputStream streamErr = new ByteArrayOutputStream();

				try {
					int exitCode = ssh
							.executeCmd(command, streamOut, streamErr);

					if (exitCode != 0)
						System.out.println("    " + id
								+ ":Error when making connection to " + user
								+ "@" + host + "   exit code = " + exitCode);

					System.out
							.println("    " + id + ": exit code = " + exitCode
									+ " ---- output stream -----\n" + streamOut
									+ "      ----- error stream ------\n"
									+ streamErr
									+ "      ---------------------------\n");
				} catch (ExecException e) {
					System.out.println("    " + id + ": " + e);
				}
				System.out.println("");
			}
		}

	}

}