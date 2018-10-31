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
 * '$Author: kulkarni $'
 * '$Date: 2010-10-05 14:46:36 -0700 (Tue, 05 Oct 2010) $'
 * '$Revision: 26020 $'
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

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kepler.util.FilenameFilter_RegularPattern;

/*
 * This class provides functionality to execute command and copy files to
 * and from a remote host.The remote host can be accessed either using SSH
 * or GSISSH. This class is extended by SshExec and GsiSshExec.
 */
public abstract class RemoteExec implements ExecInterface {
  // Hack to be able to pass the parent window to the JOptionPane used for password prompt so that the dialog
  // is appropriately centered/locked to the parent window (so JOptionPane doesn't get lost)
  // TODO: instead the ExecInterface object should take in an AuthenticationHandler class that can
  // control the UI and prompt the user however it likes...
  private static Component parentComponent = null;
  private static Logger logger = Logger.getLogger(RemoteExec.class);
  
  protected String host;
  protected String user;
  protected int port;

  public abstract String getHost();
  
  public abstract String getUser();
  
  public abstract int getPort();
	/**
	 * Method openConnection.
	 * @return boolean
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#openConnection()
	 */
	public abstract boolean openConnection() throws SshException;

	/**
	 * Method closeConnection.
	 * @see org.kepler.ssh.ExecInterface#closeConnection()
	 */
	public abstract void closeConnection();

	/**
	 * Method getForcedCleanUp.
	 * @return boolean
	 */
	public abstract boolean getForcedCleanUp();

	/**
	 * Method getTimeout.
	 * @return int
	 */
	public abstract int getTimeout();

	/**
	 * Method _copyTo.
	 * @param lfile File
	 * @param targetPath String
	 * @param recursive boolean
	 * @return int
	 * @throws SshException
	 */
	protected abstract int _copyTo(File lfile, String targetPath, boolean recursive)
	throws SshException ;
	/**
	 * Method copyFrom.
	 * @param rfile String
	 * @param localPath File
	 * @param recursive boolean
	 * @return int
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyFrom(String, File, boolean)
	 */
	public abstract int copyFrom(String rfile, File localPath, boolean recursive)
	throws SshException;

	/*
	 * Kill a remote process or its group. ProcessID should be given as a
	 * string. Returns nothing: it succeeds if it succeeds, otherwise just give
	 * it up here.
	 */
	/**
	 * Method kill.
	 * @param pid String
	 * @param group boolean
	 */
	protected void kill(String pid, boolean group) {

		if (pid == null || pid.trim().length() == 0)
			return;
		String command;
		if (group)
			command = new String("kill -9 -" + pid);
		else
			command = new String("kill -9 " + pid);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		ByteArrayOutputStream streamErr = new ByteArrayOutputStream();
		int exitCode = 0;

		// backup and set timeout and forcedCleanUp
		int timeout_save = getTimeout();
		boolean forcedCleanUp_save = getForcedCleanUp();
		setTimeout(60, false, false); // give a minute for clean-up as max. We
										// must not hang
		// here
		setForcedCleanUp(false); // would be stupid to kill the kill process

		try {
			exitCode = executeCmd(command, streamOut, streamErr);
		} catch (ExecTimeoutException ex) {
			logger.error("Remote process killing (" + command + ") timeout", ex);
			exitCode = 0;
		} catch (ExecException ex) {
			exitCode = -1;
		}

		if (exitCode != 0 /* OK */&& exitCode != 1 /* NOEXISTS */) {
			logger.warn("Remote process killing (" + command + ") failed:\n"
					+ streamErr);
		}

		// restore original timeout and forcedCleanUp values
		setTimeout(timeout_save);
		setForcedCleanUp(forcedCleanUp_save);

	}

	/**
	 * Create directory on the remote site with "mkdir" or "mkdir -p" command.
	 * It should be relative to the user's home dir, or an absolute path. If
	 * parentflag is true, the -p flag is used in the command so that an
	 * existing directory will not throw an error.
	 *
	
	 * @param dir String
	 * @param parentflag boolean
	 * @return true if succeeded throws ExecException (instance of SshExecption
	 *         or ExecTimeoutException) * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#createDir(String, boolean)
	 */
	public boolean createDir(String dir, boolean parentflag)
			throws ExecException {

		if (dir == null || dir.trim().length() == 0)
			throw new SshException("Directory name not given");
		String command;
		if (parentflag)
			command = new String("mkdir -p " + dir);
		else
			command = new String("mkdir " + dir);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		ByteArrayOutputStream streamErr = new ByteArrayOutputStream();

		int exitCode = executeCmd(command, streamOut, streamErr);

		if (exitCode != 0) {
			throw new SshException("Remote directory creation (" + command
					+ ") failed:\n" + streamErr);
		}
		return true;
	}

	/**
	 * Delete file or directory on the remote site with "rm -rf" command! BE
	 * CAREFUL It should be relative to the user's home dir, or an absolute path
	 * For safety, * and ? is allowed in filename string only if explicitely
	 * asked with allowFileMask = true If you want to delete a directory,
	 * recursive should be set to true
	 *
	
	 * @param fname String
	 * @param recursive boolean
	 * @param allowFileMask boolean
	 * @return true if succeeded throws SshException * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#deleteFile(String, boolean, boolean)
	 */
	public boolean deleteFile(String fname, boolean recursive,
			boolean allowFileMask) throws ExecException {

		if (fname == null || fname.trim().length() == 0)
			throw new SshException("File name not given");

		String command;
		if (recursive)
			command = new String("rm -rf " + fname);
		else
			command = new String("rm -f " + fname);

		// some error checking to avoid malicious removals
		if (!allowFileMask) {
			if (fname.indexOf('*') != -1 || fname.indexOf('?') != -1)
				throw new SshException(
						"File name contains file mask, but this was not allowed: "
								+ fname);
		}

		String temp = fname;
		if (temp.length() > 1 && temp.endsWith(File.separator)) {
			temp = temp.substring(0, temp.length() - 1);
			logger.debug("  %  " + fname + " -> " + temp);
		}

		if (temp.equals(".") || temp.equals("..") || temp.equals("/"))
			throw new SshException(
					"Directories like . .. / are not allowed to be removed: "
							+ fname);

		if (temp.equals("*") || temp.equals("./*") || temp.equals("../*")
				|| temp.equals("/*"))
			throw new SshException(
					"All files in directories like . .. / are not allowed to be removed: "
							+ fname);

		// end of error checking

		logger.debug("deleteDir command: " + command);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		ByteArrayOutputStream streamErr = new ByteArrayOutputStream();

		int exitCode = executeCmd(command, streamOut, streamErr);

		switch (exitCode) {
		case 0:
			break;
		case -1:
			/* Possible bug!!! */
			/*
			 * Current knowledge: we may receive -1 (for unknown reason) but the
			 * file removal command actually succeeded. So here we ignore this
			 * until someone hits this as a bug.
			 */
			logger.warn("deleteFile(): -1 received as exit code from execution "
							+ "but the removal probably succeeded. Check stdout and stderr:\n"
							+ streamOut + " " + streamErr);
			break;
		default:
			throw new SshException("Remote file removal failed: " + command
					+ "\nexit code: " + exitCode + "\nstdout:\n" + streamOut
					+ "\nstderr:\n" + streamErr + "\n-----------");
		}
		return true;
	}

	/**
	 * Copy local files to a remote directory Input: files is a Collection of
	 * files of type File, targetPath is either a directory in case of several
	 * files, or it is either a dir or filename in case of one single local file
	 * recursive: true if you want traverse directories
	 *
	
	 * @param files Collection
	 * @param targetPath String
	 * @param recursive boolean
	 * @return number of files copied successfully SshException is thrown in
	 *         case of error. * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyTo(Collection, String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public int copyTo(Collection files, String targetPath, boolean recursive)
			throws SshException {

		int numberOfCopiedFiles = 0;

		Iterator fileIt = files.iterator();
		while (fileIt.hasNext()) {
			File lfile = (File) fileIt.next();
			numberOfCopiedFiles += copyTo(lfile, targetPath, recursive);
		}
		return numberOfCopiedFiles;
	}

	/**
	 * Copy a local file to a remote directory/path Input: file of type File
	 * (which can be a directory). The file name can be wildcarded too (but not
	 * the path elements!). targetPath is either a directory or filename
	 *
	
	 * @param lfile File
	 * @param targetPath String
	 * @param recursive boolean
	 * @return number of files copied successfully (i.e either returns true or
	 *         an exception is raised) * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyTo(File, String, boolean)
	 */
	public int copyTo(File lfile, String targetPath, boolean recursive)
			throws SshException {

		File[] files = null;
		
		// if the file is wildcarded, we need the list of files
		String name = lfile.getName();
		
		if (name.indexOf("*") != -1 || name.indexOf("?") != -1) {
			
			name = lfile.getName();
			String pattern = name.replaceAll("\\.", "\\\\.").replaceAll("\\*",
					".*").replaceAll("\\?", ".");
			FilenameFilter_RegularPattern filter = new FilenameFilter_RegularPattern(
					pattern);
			String dirname = lfile.getParent();
			if (dirname == null || dirname == "")
				dirname = ".";
			File dir = new File(dirname);
			files = dir.listFiles(filter);

//Anand: Debug: Print the files being copied
			logger.debug("**** In " + this.getClass().getName() + " copyTo function *****");
			logger.debug("List of files in directory : " + dir.toString());
			for (int i = 0; i < files.length; i++)
				logger.debug(files[i].toString());
//Anand: Debug end
			
		} else { // no wildcards
			files = new File[1];
			files[0] = lfile;
		}

		int numberOfCopiedFiles = 0;
		for (int i = 0; i < files.length; i++)
			numberOfCopiedFiles += _copyTo(files[i], targetPath, recursive);

		return numberOfCopiedFiles;
	}

	/**
	 * Copy remote files from a remote directory to a local path Input: 'files'
	 * is a Collection of files of type String (! not like at copyTo !),
	 * 'targetPath' is either empty string (or null) in case the 'files' contain
	 * full paths to the individual files, or it should be a remote dir, and in
	 * this case each file name in 'files' will be extended with the remote dir
	 * name before copy. 'localPath' should be a directory name in case of
	 * several files. It can be a filename in case of a single file to be
	 * copied. This is a convenience method for copyFrom on several remote
	 * files. recursive: true if you want traverse directories
	 *
	
	 * @param targetPath String
	 * @param files Collection<String>
	 * @param localPath File
	 * @param recursive boolean
	 * @return number of files copied successfully SshException is thrown in
	 *         case of error. * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyFrom(String, Collection<String>, File, boolean)
	 */
	@Override
	public int copyFrom(String targetPath, Collection<String> files, File localPath,
			boolean recursive) throws SshException {

		int numberOfCopiedFiles = 0;

		String rdir;
		if (targetPath == null || targetPath.trim().equals("")) {
			rdir = "";
		} else {
			rdir = targetPath;
			if (!rdir.endsWith("/"))
				rdir = rdir + "/";
		}

		Iterator<String> fileIt = files.iterator();
		while (fileIt.hasNext()) {
			String rfile = fileIt.next();
			try{
			numberOfCopiedFiles += copyFrom(rdir + rfile, localPath, recursive);
			
			} catch(Exception e) {
			  if(logger.isDebugEnabled()) {
			    logger.error("Failed to copy files.", e);
			  }
				logger.warn("Warning:Skipping copy of file : "+rfile + " : Exception copying : "+ e.toString());
				//Log and move to next file
			}
		}
		return numberOfCopiedFiles;
	}
	
	/**
	 * Method listFiles.
	 * @param sourcePath String
	 * @return Vector<String>
	 * @throws ExecException
	 * @see org.kepler.ssh.ExecInterface#listFiles(String)
	 */
	@Override
	public Vector<String> listFiles(String sourcePath) throws ExecException {
		if (sourcePath == null || sourcePath.trim().length() == 0)
			throw new SshException("No path name provided");
		Vector<String> filelist = new Vector<String>();
		//ZCG 4/11/12 adding -1 (minus one) to ls command to put each item on its own line
		String command = new String("ls -1 " + sourcePath);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		ByteArrayOutputStream streamErr = new ByteArrayOutputStream();

		int exitCode = executeCmd(command, streamOut, streamErr);

		if (exitCode != 0) {
			throw new SshException("Directory listing (" + command
					+ ") failed:\n" + streamErr);
		}
		
		//ZCG 4/11/12 not splitting based on whitespace tokens because this
		//breaks when a file or folder name has a space in it.  Instead, 
		//now that the ls is putting each item on a line, put each line as  
		//a single item in the filelist 
//		StringTokenizer st = new StringTokenizer(streamOut.toString());
//		while(st.hasMoreElements()){
//			filelist.add(st.nextToken());
//		}
		
		
		//ZCG 4/11/12 if the return from the ls is the exact same as the sourcePath, that means there are no
		//children so return an empty vector:
    String outputString = streamOut.toString();
    if(!outputString.trim().equalsIgnoreCase(sourcePath.trim())){
      for(String line :outputString.split("\n")){
        if(!line.trim().isEmpty()){
          filelist.add(line);
        }
      }
    }
		return filelist;
	}

  /**
   * @return the parentComponent
   */
  public static Component getParentComponent() {
    return parentComponent;
  }

  /**
   * @param parentComponent the parentComponent to set
   */
  public static void setParentComponent(Component parentComponent) {
    RemoteExec.parentComponent = parentComponent;
  }

}
