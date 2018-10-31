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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Local file delete according to a regular expression. This class can delete
 * files and directories recursively. E.g /home/dummy/dir?/sub*d??/f* Relative
 * pathes are handled as relative to the current dir.
 */
public class LocalDelete {
	public LocalDelete() {
	}

	/**
	 * Method deleteFiles.
	 * @param mask String
	 * @param recursive boolean
	 * @return boolean
	 * @throws ExecException
	 */
	public boolean deleteFiles(String mask, boolean recursive)
			throws ExecException {

		if (mask == null)
			return true;
		if (mask.trim() == "")
			return true;

		// pre-test to conform to 'rm -rf': if the mask ends for . or ..
		// it must throw an error
		String name = new File(mask).getName();
		if (name.equals(".") || name.equals("..")) {
			throw new ExecException(
					"Directories like . or ..  are not allowed to be removed: "
							+ mask);
		}

		// split the mask into a vector of single masks
		// e.g. a/b*d/c?? into (a, b*d, c??)
		Vector splittedMask = splitMask(mask);
		// sure it has at least one element: .
		String path = (String) splittedMask.firstElement();
		splittedMask.remove(0);

		return delete(new File(path), splittedMask, recursive);
	}

	/********************/
	/* Private methods */
	/********************/

	private static final Log log = LogFactory.getLog(LocalDelete.class
			.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	/**
	 * Return the first position of * or ? in the string. returns -1 if none
	 * found or the string is null.
	 * @param mask String
	 * @return int
	 */
	private static int wildcardPos(String mask) {
		if (mask == null)
			return -1;
		int firstStarIdx = mask.indexOf("*");
		int firstQmIdx = mask.indexOf("?");
		if (firstStarIdx == -1)
			return firstQmIdx;
		if (firstQmIdx == -1)
			return firstStarIdx;
		return Math.min(firstStarIdx, firstQmIdx);
	}

	/**
	 * Method wildcarded.
	 * @param mask String
	 * @return boolean
	 */
	private static boolean wildcarded(String mask) {
		return (wildcardPos(mask) > -1);
	}

	/**
	 * Split the mask on the file separators. Instead of the String.split()
	 * method, we use the File.getParentFile() method to split the mask string.
	 * This works on Windows, where the separator can be both / and \\. The
	 * first element of the vector will be the wildcardless head of the mask. If
	 * there is no leading wildcardless element, the first part (of the path)
	 * will be either . or / according to the mask's type (relative/absolute
	 * path). The vector size will be at least 1, containing . or the full mask
	 * if the mask is entirely wildcardless.
	 * @param mask String
	 * @return Vector
	 */
	private static Vector splitMask(String mask) {
		Vector result = new Vector();

		File f = new File(mask);
		File p = f.getParentFile();
		while (wildcarded(f.getPath()) && p != null) {
			if (isDebugging)
				log.debug("Parent of file: " + f.getPath() + " is "
						+ p.getPath());
			// insert in front the substring of the mask related to this dir
			result.add(0, f.getName());
			f = p;
			p = p.getParentFile();
		}
		// insert the full path of the wildcardless mask in front
		result.add(0, f.getPath());

		if (wildcarded(f.getPath())) { // mask does not contain a wildcardless
										// head
			// the very first part is wildcarded
			// we have to add a . or / as first element
			if (f.isAbsolute())
				result.add(0, File.separator);
			else {
				// Because of later tests for symbolic links, simply adding .
				// here
				// leads to problems.
				// Instead, we add the canonical path of the . here
				String dot;
				try {
					dot = new File(".").getCanonicalPath();
				} catch (IOException e) {
					log.debug("Cannot get canonical filename of .");
					dot = new String(".");
				}

				result.add(0, dot);

			}
		}

		if (isDebugging)
			log.debug("The splitted vector is " + result.size() + " long.:");
		for (int i = 0; i < result.size(); i++) {
			if (isDebugging)
				log.debug("    " + result.get(i));
		}
		return result;
	}

	/**
	 * Recursively traverse the directories looking for matches on each level to
	 * the relevant part of the mask. Matched files will be deleted. Matched
	 * directories will be deleted only if 'recursive' is true.
	 * @param node File
	 * @param masks Vector
	 * @param recursive boolean
	 * @return boolean
	 */
	private boolean delete(File node, Vector masks, boolean recursive) {

		if (isDebugging)
			log.debug(">>> " + node.getPath() + " with masks length = "
					+ masks.size() + ": " + masks.toString());

		// the query is for a single file/dir --> it will be deleted now
		if (masks.isEmpty()) {
			return deleteNode(node, recursive, "Delete ");
		}

		// handle the case where path is not a directory but something else
		if (!node.isDirectory()) {
			if (node.isFile()) {
				// single file
				// this file cannot match the rest of the query mask
				return true; // this is not an error, just skip
			} else {
				// wildcardless mask referred to a non-existing file/dir
				log.error("Path " + node.getPath() + " is not a directory!");
				return false;
			}
		}

		// path refers to an existing dir.
		// Let's list its content with the appropriate mask
		String localMask = null;
		Vector restMask = (Vector) masks.clone();
		if (!masks.isEmpty()) {
			localMask = (String) masks.firstElement(); // first element as local
														// mask
			restMask.remove(0); // the rest
		}

		boolean result = true; // will become false if at least one file removal
								// fails

		// handle special masks . and .. separately
		if (localMask.equals(".") || localMask.equals("..")) {

			// we just need to call this method again with the next mask
			File newNode = new File(node, localMask);
			if (isDebugging)
				log.debug("Special case of " + localMask
						+ " --> Call delete() with " + newNode.getPath());
			result = delete(newNode, restMask, recursive);

		} else {
			// meaningful mask... so list the directory and recursively traverse
			// directories
			MyLocalFilter localFilter = new MyLocalFilter(localMask);

			// Get files matching the localMask in the dir 'node'
			File[] files = node.listFiles(localFilter);

			if (isDebugging)
				log.debug("Found " + files.length + " matching files in "
						+ node);

			for (int i = 0; i < files.length; i++) {
				// recursive call with the rest of the masks
				boolean succ = delete(files[i], restMask, recursive);
				if (!succ && isDebugging)
					log.debug("Failed removal of " + files[i].getPath());
				result = result && succ;
			}
		}

		if (isDebugging)
			log.debug("<<< " + node.getPath());
		return result;
	}

	/**
	 * Recursively delete a file or directory. If the directory is a symbolic
	 * link, it is not followed, but only the link will be deleted.
	 * @param f File
	 * @param recursive boolean
	 * @param indent String
	 * @return boolean
	 */
	private boolean deleteNode(File f, boolean recursive, String indent) {
		boolean result = false;
		if (!f.isDirectory()) {
			// single file
			log.info(indent + f);
			result = f.delete();
		} else if (isSymbolicLink(f)) {
			// This directory is a symbolic link, and there's no reason for us
			// to
			// follow it, because then we might be deleting something outside of
			// the directory we were told to delete.
			// Delete the link only.
			log.info(indent + f + "@");
			result = f.delete();
		} else if (recursive) {
			// directory and recursive is on
			File[] files = f.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteNode(files[i], recursive, indent + "    ");
			}
			// finally, delete the directory itself
			log.info(indent + f + File.separator);
			result = f.delete();
		}
		return result;
	}

	/**
	 */
	private class MyLocalFilter implements FilenameFilter {
		Pattern p;

		/**
		 * Constructor for MyLocalFilter.
		 * @param filemask String
		 */
		MyLocalFilter(String filemask) {
			String pattern;
			// convert file mask pattern to regular expression
			if (filemask != null) {
				String p1 = filemask.replaceAll("\\.", "\\\\.");
				String p2 = p1.replaceAll("\\*", ".*");
				String p3 = p2.replaceAll("\\?", ".");
				// System.out.println("pattern conversion: [" + p3 + "] = [" +
				// p1 + "] -> [" + p2 + "]");
				p = Pattern.compile(p3);
			} else
				p = null;
		}

		/**
		 * Method accept.
		 * @param dir File
		 * @param name String
		 * @return boolean
		 * @see java.io.FilenameFilter#accept(File, String)
		 */
		public boolean accept(File dir, String name) {
			if (p != null) {
				Matcher m = p.matcher(name);
				return m.matches();
			} else
				return true;
		}
	}

	/**
	 * Test if a File is a symbolic link. It returns true if the file is a
	 * symbolic link, false otherwise. Exception: if the symlink points to
	 * itself, it returns false. Sorry. How does it work: it compares the
	 * canonical path and the absolute path of the file. The former gives the
	 * referred file of a link and thus differs from the absolute path of the
	 * link itself.
	 * @param f File
	 * @return boolean
	 */
	private static boolean isSymbolicLink(File f) {

		if (f == null)
			return false;
		if (!f.exists())
			return false;

		// special case: path ends with .., which can never be a symlink, right?
		// Note: symlink/.. refers to the directory containing the symlink and
		// not the link itself
		// special case: path ends with ., it is the same
		// Note: symlink/. refers to the pointed directory and not the link
		// itself
		// They are handled here because they would result in true in later
		// tests.
		if (f.getName().equals("..") || f.getName().equals("."))
			return false;

		// to see if this file is actually a symbolic link to a directory,
		// we want to get its canonical path - that is, we follow the link to
		// the file it's actually linked to
		File canf;
		try {
			canf = f.getCanonicalFile();
		} catch (IOException e) {
			log.error("Cannot get canonical filename of file " + f.getPath());
			return true;
		}

		// we need to get the absolute path
		// Unfortunately File.getAbsolutePath() does not eliminate . and ..
		// thus the equality test fails for paths containing them.
		// Let's do some magic with the parent dir name and get the absolute
		// path this way.

		File absf;
		File parent = f.getParentFile();
		if (parent == null) {
			// no problem, we have a single (relative) file name
			absf = f.getAbsoluteFile();
		} else {
			// eliminate . and .. from the parent path, using getCanonicalFile()
			try {
				parent = parent.getCanonicalFile();
			} catch (IOException e) {
				log.error("Cannot get canonical filename of file "
						+ parent.getPath());
			}
			// recreate the absolute filename
			// Note: if f's name is .., this would not be eliminated here. See
			// pre-test above
			absf = new File(parent, f.getName());
		}

		if (isDebugging)
			log.debug("File " + f.getPath() + "\nCanonical =  "
					+ canf.getPath() + "\nAbsolute = " + absf.getPath());

		// a symbolic link has a different canonical path than its actual path,
		// unless it's a link to itself
		return (!canf.equals(absf));
	}

}