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

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.util.FilenameFilter_RegularPattern;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * Class to handle file transfer based on sftp protocol. It uses the Jsch
 * package to transfer files from local to remote host or remote host to local
 * machine. It extends <code>SshExec</code> and uses its method to execute a
 * sftp command in batch mode to transfer files between two remote machines
 * <p>
 * 
 * @see org.kepler.ssh.SshExec
 * @author Chandrika Sivaramakrishnan
 * 
 *         Based on - JSch examples http://www.jcraft.com/jsch and SshExec code
 *         written by Norbert Podhorszki
 * 
 */
public class SftpExec extends SshExec {

	// private variables
	private static final Log log = LogFactory.getLog(SftpExec.class.getName());
	private static final boolean isDebugging = log.isDebugEnabled();

	/**
	 * Creates session with give user and host name and default port
	 * 
	 * @param user
	 * @param host
	 */
	public SftpExec(String user, String host) {
		super(user, host);
	}

	/**
	 * Creates a session with given user name, host and port
	 * 
	 * @param user
	 * @param host
	 * @param port
	 */
	public SftpExec(String user, String host, int port) {
		super(user, host, port);
	}

	/**
	 * Copies file/directory from a remote machine to local machine using Jsch
	 * sftp channel. Since the sftp command is not directly used any command
	 * line options set in the super class will be ignored.
	 * 
	 * @param rfile
	 *            - remote file that should be copied to local machine
	 * @param localPath
	 *            - path into to which the source file should be copied
	 * @param recursive
	 *            - flag to represent recursive copy of a directory
	 * @return int
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyFrom(String, File, boolean)
	 */
	@Override
	public int copyFrom(String rfile, File localPath, boolean recursive)
			throws SshException {
		int exitCode = 0;

		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");

		// at this point we have a living, opened session to the remote machine
		if (isDebugging)
			log.debug(" %   Copy " + rfile + " to " + localPath);

		// Validate local path
		/*if (recursive && (!localPath.isDirectory())) {
			throw new SshException("Destination " + localPath
					+ " is missing or not a directory");
		}*/

		try {
			ChannelSftp sftpChannel;
			synchronized (session) { // for thread safety here we need to sync
				sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
				sftpChannel.connect();
			}

			if (isRegularOrLinkFile(rfile)) {
				// If source is a regular file
				sftpChannel.get(rfile, localPath.getAbsolutePath());
			} else if (rfile.contains("*") || rfile.contains("+")){
				//If the source file is a wildcard
				try{
					if (rfile.contains("\\")) {
						copyWildcardFrom(rfile, localPath, "\\", sftpChannel );
					} else {
						copyWildcardFrom(rfile, localPath, "/", sftpChannel );							
					}
				}catch (Exception e){
					sftpChannel.disconnect();
					throw new SshException(e.getMessage());
				}
			}else {//Source is directory
				try {
					sftpChannel.cd(rfile); // to check if remote file is
										// directory
				} catch (SftpException e) {
					throw new SshException("Source " + rfile
							+ " is missing or not a directory");
				}
				if (rfile.contains("\\")) {
					copyDirectoryFrom(rfile, localPath, "\\", sftpChannel);
				} else {
					copyDirectoryFrom(rfile, localPath, "/", sftpChannel);
				}
			}
			// close channel
			sftpChannel.disconnect();
		} catch (Exception e) {
			log.error("Exception doing put: " + e);
			throw new SshException("Unable to copy " + rfile + " to "
					+ localPath + "\n" + e.getMessage());
		}
		return exitCode;
	}
/**
 * Anand: This function takes in remote file name, and local destination dir name.
 * It scans the remote parent directory (of the wildcard), for all the matching file names.
 * And the copies all the files from remote directory to local destination dir.
 * 
 * @param rfile  
 * 			- remote wildcard pattern with full path
 * @param localPath 
 * 			- Local directory path
 * @param seperator 
 * 			- Path seperator to extract remote parent directory from rfile
 * @param sftpChannel 
 * 			- SFTP connection channel
 * @throws Exception 
 * 			- Exception code to indicate failure in operation */
	private void copyWildcardFrom(String rfile, File localPath, String seperator, 
			ChannelSftp sftpChannel) throws Exception {
			
		Vector<String> list = getWildcardFileListing(rfile, seperator);
		for (String curFile : list) {
			try{
				sftpChannel.get(curFile, localPath.getAbsolutePath());
			}catch (Exception e)
			{
				throw new SshException("Error in copying remote file " + curFile + 
						" to local directory " + localPath.getAbsolutePath());
			}
		}
	}

	/**
	 * Anand: Receives the remote wildcard and returns list of files matching the pattern 
	 * @param remoteFile : Wildcard pattern with path
	 * @param seperator : OS path seperator
	 * @return String array containing list of files matching pattern
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public Vector<String> getWildcardFileListing(String remoteFile, String seperator) throws Exception {
		//string to store pattern
		String filePattern = "";
		//string to store path
		String filePath = "";
		//list of files which match pattern
		Vector<String> fileList = new Vector();
		ChannelSftp sftpChannel = null;
		
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");
		
		try{
		synchronized (session) { // for thread safety here we need to sync
			sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
			sftpChannel.connect();
		}
		}catch (Exception e){
			e.printStackTrace();
			throw new SshException("Failed acquire SFTP Channel \n " + e.getMessage());
		}
		
		while(!sftpChannel.isConnected()){}
		//check if wildcard pattern is provided
		if (remoteFile.indexOf("*") > 0 || remoteFile.indexOf("?") > 0) {

			//list to store list of files from remote directory
			Vector<ChannelSftp.LsEntry> list;
			
			//extract pattern and directory name from rfile
			int index = remoteFile.lastIndexOf(seperator);
			if (index != -1) {
				filePattern = remoteFile.substring(index + 1);
				filePath = remoteFile.substring(0, index);
			}
			
			//Get the list of files from directory
			try {
				list = sftpChannel.ls(filePath.trim());
			} catch (SftpException e) {
				e.printStackTrace();
				throw new SshException("Unable to list files in remote directory "
						+ filePath + " \n " + e.getMessage());
			}
			if (list.size() < 1) {
				throw new SshException("Unable to retrieve contents of directory "
						+ filePath
						+ "\nPlease check the permissions on this directory.");
			}
		
			//create the wildcard pattern
			String pattern = filePattern.replaceAll("\\.", "\\\\.").replaceAll("\\*",
			".*").replaceAll("\\?", ".");
			Pattern p = Pattern.compile(pattern);
			for (ChannelSftp.LsEntry curFile : list) {
				//match the input pattern(wildcard) to each file in the list
				if (curFile.getFilename().equals(".")
						|| curFile.getFilename().equals("..") 
						|| curFile.getAttrs().isDir()) {
					continue;
				}
				Matcher m = p.matcher(curFile.getFilename());
				if (m.matches()){
					String newfile = filePath + seperator + curFile.getFilename();
					//add newfile to list of files to be copied
					fileList.add(newfile.trim());
				}
			}
		}else
		{
			throw new SshException("Cannot find file "
					+ remoteFile
					+ ".\n");
		}
		sftpChannel.disconnect();
		return fileList;
	}
	
	/**
	 * Copies files/directories from local machine to remote host
	 * 
	 * @param lfile
	 *            - local file that should be copied to remote machine
	 * @param targetPath
	 *            - path on remote host into to which the file should be copied
	 * @param recursive
	 *            - flag to represent recursive copy of a directory
	 * 
	 * @return int
	 * @throws SshException
	 * @see org.kepler.ssh.ExecInterface#copyTo(File, String, boolean)
	 */
	@Override
	public int copyTo(File lfile, String targetPath, boolean recursive)
			throws SshException {

		ChannelSftp sftpChannel;
		File[] files = null;
		
		//Connection Setup - Start
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");
		// at this point we have a living, opened session to the remote machine

		if (isDebugging)
			log.debug(" %   Copy " + lfile + " to " + targetPath);
		
		try {
			synchronized (session) { // for thread safety here we need to sync
				sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
				sftpChannel.connect();
			}
		} catch (JSchException e) {
			e.printStackTrace();
			throw new SshException("Unable to create connection to copy " + lfile + " to "
					+ targetPath + "\n" + e.getMessage());
		}
		//Connection Setup - End
		
		if (lfile.isDirectory()) { // if file is a directory
			// recursive is not set
			if (!recursive)
				throw new SshException("File " + lfile + " is a directory. Set recursive copy!");
			// Validation of remote dir
			try {
					sftpChannel.cd(targetPath); // to check if remote file is
												// directory
				} catch (SftpException e) {
					throw new SshException("Destination " + targetPath
							+ " is missing or not a directory");
				}
				log.debug("Calling dir copy");
				try{
					if (targetPath.contains("\\")) {
						// Anand: for windows - file seperator is \\
						copyDirectoryTo(lfile, targetPath, "\\", sftpChannel);
					} else {
						// Anand: for linx file seperator is /
						copyDirectoryTo(lfile, targetPath, "/", sftpChannel);
					}
				} catch (Exception e){
					throw new SshException("Error while copying directory" + "\n" + e.getMessage());
				}
			}
		 else { //file is a single file or wildcard
			// Anand: Get the complete file name
			String name = lfile.getPath();
			// if file contains a wildcard
			if (name.indexOf("*") >=0  || name.indexOf("?") >= 0) {
System.out.println("**********found wildcard pattern for SFTP copyTo");				
				name = lfile.getName();
				// create a pattern for wildcard, and form a filter
				String pattern = name.replaceAll("\\.", "\\\\.").replaceAll("\\*",
						".*").replaceAll("\\?", ".");
				FilenameFilter_RegularPattern filter = new FilenameFilter_RegularPattern(
						pattern);
				// search for file matching pattern in the directory
				String dirname = lfile.getParent();
				if (dirname == null || dirname == "")
					dirname = ".";
				File dir = new File(dirname);
				files = dir.listFiles(filter);
				try {
					// Copy files from wildcard using a loop
					for (int i = 0; i < files.length; i++)
						sftpChannel.put(files[i].getAbsolutePath(), targetPath);
				} catch (SftpException e) {
					e.printStackTrace();
					throw new SshException(
							"SFTP Channel failed to copy files to remote machine.");
				}
		}else {// Only a single file is to be copied. 
			//Check if the file exists
			if (!lfile.exists()) {
				throw new SshException("Source file " + lfile
						+ " doesn't exists");
			}
			files = new File[1];
			files[0] = lfile;
		}
			//copy the files -  includes wildcard files or single file
			log.debug("Calling file copy");
			try {
				for (int i = 0; i < files.length; i++)
					sftpChannel.put(files[i].getAbsolutePath(), targetPath);
				} catch (Exception e) {
					throw new SshException("Unable to copy " + lfile + " to "
							+ targetPath + "\n" + e.getMessage());
				}
		}//else for file or directory ends
		// close channel
		sftpChannel.disconnect();
		return 0;
	}

	/**
	 * Returns the list of sftp commands (combination of "mkdir" and "put"
	 * commands) that are required to recursively copy a directory.
	 * 
	 * @param srcFile
	 *            - source file to be copied
	 * @param destFile
	 *            - destination directory
	 * @param isConnectionOrigin 
	 * @return command - String representing the list of commands seperated by
	 *         \n character
	 * @throws SshException
	 */
	public String getRecursiveCopyCmd(String srcFile, String destFile, boolean isConnectionOrigin)
			throws SshException {
		StringBuffer cmd = new StringBuffer(100);
		String destseperator = "/";
		String srcseperator = "/";
		try {

			if (!openConnection())
				throw new SshException(
						"Ssh connection could not be opened for copying.");

			ChannelSftp sftpChannel;
			synchronized (session) { // for thread safety here we need to sync
				sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
				sftpChannel.connect();
			}

			try {
				log.debug("changing dir to " + srcFile);
				sftpChannel.cd(srcFile.trim()); // to check if remote file is directory
			} catch (SftpException e) {
				throw new SshException("Source " + srcFile
						+ " is missing or not a directory");
			}

			if (destFile.indexOf("\\") > -1) {
				destseperator = "\\";
			}
			if (srcFile.indexOf("\\") > -1) {
				srcseperator = "\\";
			}

			cmd.append(getDirCopyCmd(srcFile, destFile, srcseperator,
					destseperator, sftpChannel, isConnectionOrigin));
			cmd.append("exit");

			// close channel
			sftpChannel.disconnect();
		} catch (Exception e) {
			throw new SshException(e.getMessage());
		}
		if (isDebugging) {
			log.debug("Returning cmd= " + cmd);
		}
		return cmd.toString();
	}

	/**
	 * Checks if a given file is a regular file.
	 * 
	 * @param srcFile
	 *            - file that is to be checked for its type
	 * @return flag - true if the input is a regular file, false otherwise
	 * @throws SshException
	 * @throws JSchException
	 */
	@SuppressWarnings("unchecked")
	public boolean isRegularOrLinkFile(String srcFile) throws SshException,
			JSchException {
		if (!openConnection())
			throw new SshException(
					"Ssh connection could not be opened for copying.");

		ChannelSftp sftpChannel;
		Vector<ChannelSftp.LsEntry> list;

		synchronized (session) { // for thread safety here we need to sync
			sftpChannel = (ChannelSftp) jschSession.openChannel("sftp");
			sftpChannel.connect();
		}

		try {
			list = sftpChannel.ls(srcFile);
			sftpChannel.disconnect();
		} catch (SftpException e) {
			e.printStackTrace();
			log.error("Error checking file type- " + e + " :: for file ::" + srcFile);
			throw new SshException(
					"Unable to determine file type of source file " + srcFile
							+ "\n" + e);
		}

		// ls of regular file should have only one entry
		if (list.size() != 1) {
			return false;
		}
		for (ChannelSftp.LsEntry curFile : list) {
			if (curFile.getLongname().startsWith("-")
					|| curFile.getLongname().startsWith("l")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	// /////////////////////////private
	// methods///////////////////////////////////

	/*
	 * Recursively copy directory to target
	 */
	/**
	 * Method copyDirectoryTo.
	 * @param lfile File
	 * @param targetPath String
	 * @param seperator String
	 * @param sftpChannel ChannelSftp
	 * @throws Exception
	 */
	private void copyDirectoryTo(File lfile, String targetPath,
			String seperator, ChannelSftp sftpChannel) throws Exception {

		if (isDebugging) {
			log.debug("Copying directory-" + lfile);
		}
		File[] list = lfile.listFiles();
		if (list == null) {
			throw new Exception(
					"Unable to access local directory "
							+ lfile
							+ "\nPlease check if directory exists and has appropriate permissions for the user");
		}
		try {
			sftpChannel.cd(targetPath);
		} catch (SftpException e) {
			throw new Exception("Unable to access " + targetPath + "\n"
					+ e.getMessage());
		}

		try {
			sftpChannel.mkdir(lfile.getName());
		} catch (SftpException e) {
			StringBuffer message = new StringBuffer(100);
			message.append("Unable to create directory ").append(
					lfile.getName()).append(" in ").append(targetPath).append(
					"\n");
			String exmessage = e.getMessage().trim();
			if (exmessage.equals("") || exmessage.equals("Failure")) {
				message
						.append("Probable error: another file exists with the same name");
			} else {
				message.append(exmessage);
			}
			throw new Exception(message.toString());
		}

		targetPath = targetPath + seperator + lfile.getName();

		// Start copying files in the directory
		for (File curFile : list) {
			if (curFile.isDirectory()) {
				copyDirectoryTo(curFile, targetPath, seperator, sftpChannel);
			} else {
				sftpChannel.put(curFile.getAbsolutePath(), targetPath);
			}
		}

	}

	// Recursively copy directory from remote host to local
	/**
	 * Method copyDirectoryFrom.
	 * @param rfile String
	 * @param localPath File
	 * @param seperator String
	 * @param sftpChannel ChannelSftp
	 * @throws Exception
	 */
	private void copyDirectoryFrom(String rfile, File localPath,
			String seperator, ChannelSftp sftpChannel) throws Exception {

		if (isDebugging) {
			log.debug("Copying remote directory-" + rfile);
		}
		Vector<ChannelSftp.LsEntry> list = null;
		try {
			list = sftpChannel.ls(rfile);
		} catch (SftpException e) {
			throw new SshException("Unable to list files in remote directory "
					+ rfile + " \n " + e.getMessage());
		}
		if (list.size() < 1) {
			throw new SshException("Unable to retrieve contents of directory "
					+ rfile
					+ "\nPlease check the permissions on this directory.");
		}

		// Create a sub directory in localPath with the same name as remote dir
		String filename = rfile;
		int index = rfile.lastIndexOf(seperator);
		if (index != -1) {
			filename = rfile.substring(index + 1);
		}
		File subdir = new File(localPath.getAbsolutePath()
				+ localPath.separator + filename);
		subdir.mkdir();

		for (ChannelSftp.LsEntry curFile : list) {

			if (curFile.getFilename().equals(".")
					|| curFile.getFilename().equals("..")) {
				continue;
			}
			String newfile = rfile + seperator + curFile.getFilename();

			if (curFile.getAttrs().isDir()) {
				copyDirectoryFrom(newfile, subdir, seperator, sftpChannel);
			} else {
				sftpChannel.get(newfile, subdir.getAbsolutePath());
			}
		}
	}

	// Returns the the 'mkdir' command for a given source directory and a list
	// of
	// put command for all the files present in the directory
	// This method is recursively called for every subdir in the source
	// directory
	/**
	 * Method getDirCopyCmd.
	 * @param srcFile String
	 * @param destFile String
	 * @param srcseperator String
	 * @param destseperator String
	 * @param sftpChannel ChannelSftp
	 * @param isConnectionOrigin boolean
	 * @return StringBuffer
	 * @throws SshException
	 */
	private StringBuffer getDirCopyCmd(String srcFile, String destFile,
			String srcseperator, String destseperator, ChannelSftp sftpChannel, boolean isConnectionOrigin)
			throws SshException {

		String curDestDir;
		String curSrcDir;
		Vector<ChannelSftp.LsEntry> list;
		StringBuffer cmd = new StringBuffer();
		try {
			list = sftpChannel.ls(srcFile);
		} catch (SftpException e) {
			throw new SshException("Unable to list files in source directory "
					+ srcFile + " \n " + e.getMessage());
		}
		if (list.size() < 1) {
			throw new SshException("Unable to retrieve contents of directory "
					+ srcFile
					+ "\nPlease check the permissions on this directory.");
		}

		curSrcDir = srcFile + srcseperator;
		curDestDir = destFile + destseperator
				+ srcFile.substring(srcFile.lastIndexOf(srcseperator) + 1);
		if (isConnectionOrigin)
			cmd.append("mkdir ");
		else
			cmd.append("lmkdir ");
		cmd.append("\\\"");
		cmd.append(curDestDir);
		cmd.append("\\\"");
		cmd.append("\\n");

		for (ChannelSftp.LsEntry curFile : list) {

			if (curFile.getFilename().equals(".")
					|| curFile.getFilename().equals("..")) {
				continue;
			}

			if (curFile.getAttrs().isDir()) {
				// recursive call with sub directory as the new source directory
				cmd.append(getDirCopyCmd(curSrcDir + curFile.getFilename(),
						curDestDir, srcseperator, destseperator, sftpChannel, isConnectionOrigin));

			} else {
				if (isConnectionOrigin)
					cmd.append("put ");
				else
					cmd.append("get ");					
				cmd.append("\\\"");
				cmd.append(curSrcDir);
				cmd.append(curFile.getFilename());
				cmd.append("\\\"");
				cmd.append(" ");
				cmd.append("\\\"");;
				cmd.append(curDestDir);
				cmd.append(destseperator);
				cmd.append(curFile.getFilename());
				cmd.append("\\\"");
				cmd.append("\\n");
			}
		}
		return cmd;
	}

}
