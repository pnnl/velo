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
 * Copyright (c) 2010 The Regents of the University of California.
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

/** Textual password query class. Open source code taken from 
 *  http://java.sun.com/developer/technicalArticles/Security/pwordmask/
 *
 *    '$RCSfile$'
 *
 *     '$Author: welker $'
 *       '$Date: 2010-05-05 22:21:26 -0700 (Wed, 05 May 2010) $'
 *   '$Revision: 24234 $'
 *
 *  For Details: http://www.kepler-project.org
 *
 */

package org.kepler.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

/**
 * This class prompts the user for a password and attempts to mask input with
 * "*"
 */

public class MaskedTextPasswordField {

	/**
	 *
	 *@param prompt
	 *            The prompt to display to the user.
	 *
	 * @param in InputStream
	@return The password as entered by the user. * @throws IOException
	 */

	public static final char[] getPassword(InputStream in, String prompt)
			throws IOException {
		MaskingThread maskingthread = new MaskingThread(prompt);
		Thread thread = new Thread(maskingthread);
		thread.start();

		char[] lineBuffer;
		char[] buf;
		int i;

		buf = lineBuffer = new char[128];

		int room = buf.length;
		int offset = 0;
		int c;

		loop: while (true) {
			switch (c = in.read()) {
			case -1:
			case '\n':
				break loop;

			case '\r':
				int c2 = in.read();
				if ((c2 != '\n') && (c2 != -1)) {
					if (!(in instanceof PushbackInputStream)) {
						in = new PushbackInputStream(in);
					}
					((PushbackInputStream) in).unread(c2);
				} else {
					break loop;
				}

			default:
				if (--room < 0) {
					buf = new char[offset + 128];
					room = buf.length - offset - 1;
					System.arraycopy(lineBuffer, 0, buf, 0, offset);
					Arrays.fill(lineBuffer, ' ');
					lineBuffer = buf;
				}
				buf[offset++] = (char) c;
				break;
			}
		}
		maskingthread.stopMasking();
		if (offset == 0) {
			return null;
		}
		char[] ret = new char[offset];
		System.arraycopy(buf, 0, ret, 0, offset);
		Arrays.fill(buf, ' ');
		return ret;
	}

	/**
	 * This class attempts to erase characters echoed to the console. Taken from
	 * http://java.sun.com/developer/technicalArticles/Security/pwordmask/
	 * Source is open.
	 */
	private static class MaskingThread extends Thread {
		private volatile boolean dowork;
		private char echochar = ' ';

		/**
		 *@param prompt
		 *            The prompt displayed to the user
		 */
		public MaskingThread(String prompt) {
			System.out.print(prompt);
		}

		/**
		 * Begin masking until asked to stop.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			int priority = Thread.currentThread().getPriority();
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			try {
				dowork = true;
				while (dowork) {
					System.out.print("\010" + echochar);
					try {
						// attempt masking at this rate
						Thread.currentThread().sleep(1);
					} catch (InterruptedException iex) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			} finally { // restore the original priority
				Thread.currentThread().setPriority(priority);
			}
		}

		/**
		 * Instruct the thread to stop masking.
		 */
		public void stopMasking() {
			this.dowork = false;
		}
	}

}
