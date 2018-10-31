/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Michael Berger (IBM) - Fixing 140408 - FTP upload does not work
 * Javier Montalvo Orús (Symbian) - Migrate to apache commons net FTP client
 * Javier Montalvo Orus (Symbian) - Fixing 161211 - Cannot expand /pub folder as anonymous on ftp.wacom.com
 * Javier Montalvo Orus (Symbian) - Fixing 161238 - [ftp] connections to VMS servers are not usable
 * Javier Montalvo Orus (Symbian) - Fixing 176216 - [api] FTP sould provide API to allow clients register their own FTPListingParser
 * Javier Montalvo Orus (Symbian) - [197758] Unix symbolic links are not classified as file vs. folder   
 * Javier Montalvo Orus (Symbian) - [198272] FTP should return classification for symbolic links so they show a link overlay
 * Martin Oberhuber (Wind River) - [204669] Fix ftp path concatenation on systems using backslash separator
 * Javier Montalvo Orus (Symbian) - [198692] FTP should mark files starting with "." as hidden
 * David McKnight   (IBM)         - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh][local] Return proper "Root" IHostFile
 * Samuel Wu        (IBM)         - [398988] [ftp] FTP Only support to zVM
 *******************************************************************************/

package org.eclipse.rse.internal.services.files.globusonline;


import java.io.File;

import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.archiveutils.ArchiveHandlerManager;
import org.eclipse.rse.services.files.HostFilePermissions;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;

public class GlobusOnlineHostFile implements IHostFile, IHostFilePermissionsContainer
{

	private String _name;
	private String _parentPath;
	private boolean _isDirectory;
	private boolean _isLink;
	private boolean _isArchive;
	private long _lastModified;
	private long _size;
	private boolean _canRead = true;
	private boolean _canWrite = true;
	private boolean _isRoot;
	private boolean _exists;
	private IHostFilePermissions _permissions;
	private GlobusOnlineFile _globusOnlineFile;

	public GlobusOnlineHostFile(String parentPath, String name, boolean isDirectory, boolean isRoot, long lastModified, long size, boolean exists)
	{
		_parentPath = parentPath;
		_name = name;
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException();
		}
		_isDirectory = isDirectory;
		_lastModified = lastModified;
		_size = size;
		_isArchive = internalIsArchive();
		_canRead = true;
		_canWrite = true;
		_isRoot = isRoot;
		_exists = exists;
	}

	public GlobusOnlineHostFile(String parentPath, GlobusOnlineFile globusOnlineFile)
	{
		_parentPath = parentPath;
		_globusOnlineFile = globusOnlineFile;

		_name = globusOnlineFile.getName();

		_isDirectory = globusOnlineFile.isDirectory();
		_isLink = globusOnlineFile.isSymbolicLink();
		_lastModified = globusOnlineFile.getTimestamp().getTimeInMillis();
		_size = globusOnlineFile.getSize();
		_isArchive = internalIsArchive();

		_canRead = globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.READ_PERMISSION);
		_canWrite = globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.WRITE_PERMISSION);

		_isRoot = false;
		_exists = true;

		initPermissions(globusOnlineFile);
	}




	public long getSize()
	{
		return _size;
	}

	public boolean isDirectory()
	{
		return _isDirectory;
	}

	public boolean isFile()
	{
		return !(_isDirectory || _isRoot);
	}

	public boolean isLink()
	{
		return _isLink;
	}

	public String getName()
	{
		return _name;
	}

	public boolean canRead() {
		return _canRead;
	}

	public boolean canWrite() {
		return _canWrite;
	}

	public boolean exists() {
		return _exists;
	}

	public String getAbsolutePath()
	{
		if (isRoot() || _parentPath==null) {
			return getName();
		} else {
			String parentPath = getParentPath();
			StringBuffer path = new StringBuffer(parentPath);
			if (!parentPath.endsWith("/") && !parentPath.endsWith("\\"))//$NON-NLS-1$ //$NON-NLS-2$
			{
				//TODO IFileService should have a method for this
				String sep = PathUtility.getSeparator(parentPath);
				if (!parentPath.endsWith(sep)) {
					path.append(sep);
				}
			}
			path.append(getName());
			return path.toString();
		}
	}

	public long getModifiedDate()
	{
		return _lastModified;
	}

	public String geParentPath() {
		return _parentPath;
	}

	public boolean isArchive() {
		return _isArchive;
	}

	public boolean isHidden() {
		String name = getName();
		return name.charAt(0) == '.';
	}

	public boolean isRoot() {
		return _isRoot;

	}

	public String getParentPath() {
		return _parentPath;
	}

	public void renameTo(String newAbsolutePath)
	{
		int i = newAbsolutePath.lastIndexOf("/"); //$NON-NLS-1$
		if (i == -1) {
			//Rename inside the same parent folder.
			//FIXME is this really what was desired here? Or would we rename Roots?
			//Renaming Roots isn't possible, I'd think.
			_name = newAbsolutePath;
		}
		else if (i == 0) {
			// Renaming a root folder
			if (newAbsolutePath.length()==1) {
				//rename to root "/" -- should this work?
				_parentPath = null;
				_isRoot = true;
				_name = newAbsolutePath;
			} else {
				_parentPath = "/"; //$NON-NLS-1$
				_name = newAbsolutePath.substring(i + 1);
			}
		} else {
			_parentPath = newAbsolutePath.substring(0, i);
			_name = newAbsolutePath.substring(i+1);
		}
		_isArchive = internalIsArchive();
	}

	public int getUserPermissions()
	{
		int userRead = 0;
		int userWrite = 0;
		int userExec = 0;

		//user
		if(_globusOnlineFile!=null)
		{
			userRead = _globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.READ_PERMISSION) ? 1 : 0;
			userWrite = _globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.WRITE_PERMISSION) ? 1 : 0;
			userExec = _globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION) ? 1 : 0;
		}
		else
		{
			userRead = _canRead ? 1 : 0;
			userWrite = _canWrite ? 1 : 0;
			userExec = 0;

		}

		return userRead << 2  | userWrite << 1  | userExec;
	}

	public int getGroupPermissions()
	{
		int groupRead = 0;
		int groupWrite = 0;
		int groupExec = 0;

		//group
		if(_globusOnlineFile!=null)
		{
			groupRead = _globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.READ_PERMISSION) ? 1 : 0;
			groupWrite = _globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.WRITE_PERMISSION) ? 1 : 0;
			groupExec = _globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION) ? 1 : 0;
		}

		return groupRead << 2 | groupWrite << 1 | groupExec;
	}

	public int getOtherPermissions()
	{
		int otherRead = 0;
		int otherWrite = 0;
		int otherExec = 0;

		//other
		if(_globusOnlineFile!=null)
		{
			otherRead = _globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.READ_PERMISSION) ? 1 : 0;
			otherWrite = _globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.WRITE_PERMISSION) ? 1 : 0;
			otherExec = _globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION) ? 1 : 0;
		}

		return otherRead << 2 | otherWrite << 1 | otherExec;
	}




	protected boolean internalIsArchive()
	{
		return ArchiveHandlerManager.getInstance().isArchive(new File(getAbsolutePath()))
		&& !ArchiveHandlerManager.isVirtual(getAbsolutePath());
	}

	public void setIsDirectory(boolean isDirectory)
	{
		_isDirectory = isDirectory;
	}

	public String getClassification() {
		String result;
		String linkTarget;

		if (isLink()) {
			result = "symbolic link"; //$NON-NLS-1$
			if ((linkTarget = _globusOnlineFile.getLink()) !=null) {
				if(isDirectory()) {
					result += "(directory):" + linkTarget; //$NON-NLS-1$
				} else if((getUserPermissions() & 0x01) == 0x01) {
					result += "(executable):" +  linkTarget; //$NON-NLS-1$
				} else {
					result += "(file):" +  linkTarget; //$NON-NLS-1$
				}
			}
		} else if (isFile()) {
			if ((getUserPermissions() & 0x01) == 0x01) {
				result = "executable"; //$NON-NLS-1$
			} else {
				result = "file"; //$NON-NLS-1$
			}
		} else if (isDirectory()) {
			result = "directory"; //$NON-NLS-1$
		} else {
			result = "unknown"; //default-fallback //$NON-NLS-1$
		}
		return result;
	}

	private void initPermissions(GlobusOnlineFile globusOnlineFile){
		_permissions = new HostFilePermissions();
		_permissions.setPermission(IHostFilePermissions.PERM_USER_READ, globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.READ_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_USER_WRITE, globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.WRITE_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_USER_EXECUTE, globusOnlineFile.hasPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_GROUP_READ, globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.READ_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_GROUP_WRITE, globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.WRITE_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_GROUP_EXECUTE, globusOnlineFile.hasPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_OTHER_READ, globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.READ_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_OTHER_WRITE, globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.WRITE_PERMISSION));
		_permissions.setPermission(IHostFilePermissions.PERM_OTHER_EXECUTE, globusOnlineFile.hasPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION));


		_permissions.setUserOwner(globusOnlineFile.getUser());
		_permissions.setGroupOwner(globusOnlineFile.getGroup());
	}

	public IHostFilePermissions getPermissions() {
		return _permissions;
	}

	public void setPermissions(IHostFilePermissions permissions) {
		_permissions = permissions;
	}

}
