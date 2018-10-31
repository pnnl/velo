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

package org.kepler.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A FilenameFilter class for listing directories. It uses an array of regular
 * patterns to filter filenames. Used by DirectoryListing as well as
 * org.kepler.ssh.LocalExec and org.kepler.ssh.SshExec
 */
public class FilenameFilter_RegularPattern implements FilenameFilter {
	private ArrayList patterns;
	private static final Log log = LogFactory
			.getLog(FilenameFilter_RegularPattern.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	public FilenameFilter_RegularPattern() {
		this(null);
	}

	/**
	 * Constructor for FilenameFilter_RegularPattern.
	 * @param pattern String
	 */
	public FilenameFilter_RegularPattern(String pattern) {
		patterns = new ArrayList();
		if (pattern != null) {
			patterns.add(Pattern.compile(pattern));
			if (isDebugging)
				log
						.debug("Pattern " + pattern
								+ " added to the list of masks.");
		}
	}

	/**
	 * Method add.
	 * @param pattern String
	 */
	public void add(String pattern) {
		if (pattern != null) {
			patterns.add(Pattern.compile(pattern));
			if (isDebugging)
				log
						.debug("Pattern " + pattern
								+ " added to the list of masks.");
		}
	}

	/**
	 * Method accept.
	 * @param dir File
	 * @param name String
	 * @return boolean
	 * @see java.io.FilenameFilter#accept(File, String)
	 */
	public boolean accept(File dir, String name) {
		if (patterns.size() == 0)
			return true; // empty pattern means "accept all"
		for (int i = 0; i < patterns.size(); i++) {
			Pattern p = (Pattern) patterns.get(i);
			Matcher m = p.matcher(name);
			if (m.matches()) {
				if (isDebugging)
					log.debug("Name " + name + " matches mask " + i);
				return true;
			}
		}
		return false;
	}
}