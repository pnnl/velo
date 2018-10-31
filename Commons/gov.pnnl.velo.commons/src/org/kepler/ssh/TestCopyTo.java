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

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

/** Scp several local files to a remote site */
public class TestCopyTo {

	/**
	 * Method main.
	 * @param arg String[]
	 * @throws ExecException
	 */
	public static void main(String[] arg) throws ExecException {

		// process arguments
		if (arg.length < 3) {
			System.err
					.println("Usage: ... [user@]host localFile1 [localFile2 ... localFileN] remotePath");
			System.err
					.println("localFile can be a directory or file pattern as well");
			System.err
					.println("If there is a directory, the copy will be recursively done");
			System.exit(1);
		}

		System.out.println("arg length = " + arg.length);
		String target = arg.length > 0 ? arg[0] : "pnorbert@localhost";

		boolean recursive = false;

		Vector files = new Vector();
		for (int i = 1; i < arg.length - 1; i++) {
			File f = new File(arg[i]);
			if (f.isDirectory())
				recursive = true;
			// else if ( ! f.isFile() ) {
			// System.err.println("Argument source file " + arg[i] +
			// " is not a file");
			// System.exit(2);
			// }
			files.add(f);
		}
		String rpath = arg[arg.length - 1];

		// determine user and host from argument 'target'
		String user, host;
		int atPos = target.indexOf('@');
		if (atPos >= 0)
			user = target.substring(0, target.indexOf('@'));
		else
			user = System.getProperty("user.name");

		host = target.substring(atPos + 1);

		// print what is to be done
		if (host.equals("local"))
			System.out.print("cp ");
		else
			System.out.print("scp ");
		Iterator it = files.iterator();
		while (it.hasNext())
			System.out.print(it.next() + " ");
		System.out.println(target + ":" + rpath);
		System.out.println("Recursive copy: " + recursive);

		// open ssh connection
		ExecInterface eo;
		if (host.equals("local"))
			eo = new LocalExec();
		else {
			SshExec ssh = new SshExec(user, host);
			ssh.addIdentity(System.getProperty("user.home") + File.separator
					+ ".ssh" + File.separator + "id_dsa");
			ssh.openConnection();
			eo = ssh;
		}

		// do copy
		int n = eo.copyTo(files, rpath, recursive);
		System.out.println(n + " files have been copied successfully");
		if (!host.equals("local"))
			((SshExec) eo).closeConnection();

		System.exit(0);

	}

}