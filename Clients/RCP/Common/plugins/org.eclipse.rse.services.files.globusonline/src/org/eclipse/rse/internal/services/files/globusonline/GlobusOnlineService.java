/********************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir,
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 * Contributors:
 * Michael Berger (IBM) - Fixing 140408 - FTP upload does not work
 * Javier Montalvo Orus (Symbian) - Fixing 140323 - provided implementation for delete, move and rename.
 * Javier Montalvo Orus (Symbian) - Bug 140348 - FTP did not use port number
 * Michael Berger (IBM) - Fixing 140404 - FTP new file creation does not work
 * Javier Montalvo Orus (Symbian) - Migrate to apache commons net FTP client
 * Javier Montalvo Orus (Symbian) - Fixing 161211 - Cannot expand /pub folder as anonymous on ftp.wacom.com
 * Javier Montalvo Orus (Symbian) - Fixing 161238 - [ftp] expand "My Home" node on ftp.ibiblio.org as anonymous fails
 * Javier Montalvo Orus (Symbian) - Fixing 160922 - create folder/file fails for FTP service
 * David Dykstal (IBM) - Fixing 162511 - FTP file service does not process filter strings correctly
 * Javier Montalvo Orus (Symbian) - Fixing 162511 - FTP file service does not process filter strings correctly
 * Javier Montalvo Orus (Symbian) - Fixing 162782 - File filter does not display correct result in RC3
 * Javier Montalvo Orus (Symbian) - Fixing 162878 - New file and new folder dialogs don't work in FTP in a folder with subfolders
 * Javier Montalvo Orus (Symbian) - Fixing 162585 - [FTP] fetch children cannot be cancelled
 * Javier Montalvo Orus (Symbian) - Fixing 161209 - Need a Log of ftp commands
 * Javier Montalvo Orus (Symbian) - Fixing 163264 - FTP Only can not delete first subfolder
 * Michael Scharf (Wind River) - Fix 164223 - Wrong call for setting binary transfer mode
 * Martin Oberhuber (Wind River) - Add Javadoc for getFTPClient(), modify move() to use single connected session
 * Javier Montalvo Orus (Symbian) - Fixing 164009 - FTP connection shows as connected when login fails
 * Javier Montalvo Orus (Symbian) - Fixing 164306 - [ftp] FTP console shows plaintext passwords
 * Javier Montalvo Orus (Symbian) - Fixing 161238 - [ftp] connections to VMS servers are not usable
 * Javier Montalvo Orus (Symbian) - Fixing 164304 - [ftp] cannot connect to wftpd server on Windows
 * Javier Montalvo Orus (Symbian) - Fixing 165471 - [ftp] On wftpd-2.0, "." and ".." directory entries should be hidden
 * Javier Montalvo Orus (Symbian) - Fixing 165476 - [ftp] On warftpd-1.65 in MSDOS mode, cannot expand drives
 * Javier Montalvo Orus (Symbian) - Fixing 168120 - [ftp] root filter resolves to home dir
 * Javier Montalvo Orus (Symbian) - Fixing 169680 - [ftp] FTP files subsystem and service should use passive mode
 * Javier Montalvo Orus (Symbian) - Fixing 174828 - [ftp] Folders are attempted to be removed as files
 * Javier Montalvo Orus (Symbian) - Fixing 176216 - [api] FTP should provide API to allow clients register their own FTPListingParser
 * Martin Oberhuber (Wind River) - [186128] Move IProgressMonitor last in all API
 * Javier Montalvo Orus (Symbian) - improved autodetection of FTPListingParser
 * Javier Montalvo Orus (Symbian) - [187096] Drag&Drop + Copy&Paste shows error message on FTP connection
 * Javier Montalvo Orus (Symbian) - [187531] Improve exception thrown when Login Failed on FTP
 * Javier Montalvo Orus (Symbian) - [187862] Incorrect Error Message when creating new file in read-only directory
 * Javier Montalvo Orus (Symbian) - [194204] Renaming Files/Folders moves them sometimes
 * Javier Montalvo Orus (Symbian) - [192724] New Filter with Show Files Only still shows folders
 * Martin Oberhuber (Wind River) - [192724] Fixed logic to filter folders if FILE_TYPE_FOLDERS
 * Javier Montalvo Orus (Symbian) - [191048] Remote files locally listed and being removed by other users should be reported as missing
 * Javier Montalvo Orus (Symbian) - [195677] Rename fails on WFTPD-2.03
 * Javier Montalvo Orus (Symbian) - [197105] Directory listing fails on Solaris when special devices are in a directory
 * Javier Montalvo Orus (Symbian) - [197758] Unix symbolic links are not classified as file vs. folder
 * Javier Montalvo Orus (Symbian) - [198182] FTP export problem: RSEF8057E: Error occurred while exporting FILENAME: Operation failed. File system input or output error
 * Javier Montalvo Orus (Symbian) - [192610] EFS operations on an FTP connection make Eclipse freeze
 * Javier Montalvo Orus (Symbian) - [195830] RSE performs unnecessary remote list commands
 * Martin Oberhuber (Wind River) - [198638] Fix invalid caching
 * Martin Oberhuber (Wind River) - [198645] Fix case sensitivity issues
 * Martin Oberhuber (Wind River) - [192610] Fix thread safety for delete(), upload(), setReadOnly() operations
 * Martin Oberhuber (Wind River) - [199548] Avoid touching files on setReadOnly() if unnecessary
 * Javier Montalvo Orus (Symbian) - [199243] Renaming a file in an FTP-based EFS folder hangs all of Eclipse
 * Martin Oberhuber (Wind River) - [203306] Fix Deadlock comparing two files on FTP
 * Martin Oberhuber (Wind River) - [204669] Fix ftp path concatenation on systems using backslash separator
 * Martin Oberhuber (Wind River) - [203490] Fix NPE in FTPService.getUserHome()
 * Martin Oberhuber (Wind River) - [203500] Support encodings for FTP paths
 * Javier Montalvo Orus (Symbian) - [196351] Delete a folder should do recursive Delete
 * Javier Montalvo Orus (Symbian) - [187096] Drag&Drop + Copy&Paste shows error message on FTP connection
 * David McKnight   (IBM)        - [207178] changing list APIs for file service and subsystems
 * Javier Montalvo Orus (Symbian) - [208912] Cannot expand /C on a VxWorks SSH Server
 * David McKnight   (IBM)        - [210109] store constants in IFileService rather than IFileServiceConstants
 * Kevin Doyle		(IBM)		 - [208778] [efs][api] RSEFileStore#getOutputStream() does not support EFS#APPEND
 * David McKnight   (IBM)        - [209593] [api] add support for "file permissions" and "owner" properties for unix files
 * Martin Oberhuber (Wind River) - [216351] Improve cancellation of SystemFetchOperation for files
 * David McKnight   (IBM)        - [216252] [api][nls] Resource Strings specific to subsystems should be moved from rse.ui into files.ui / shells.ui / processes.ui where possible
 * Javier Montalvo Orus (Symbian) - [212382] additional "initCommands" slot for ftpListingParsers extension point
 * Martin Oberhuber (Wind River) - [226262] Make IService IAdaptable
 * Radoslav Gerganov (ProSyst) - [230919] IFileService.delete() should not return a boolean
 * Martin Oberhuber (Wind River) - [218040] FTP should support permission modification
 * Martin Oberhuber (Wind River) - [234045] FTP Permission Error Handling
 * Martin Oberhuber (Wind River) - [235463][ftp][dstore] Incorrect case sensitivity reported on windows-remote
 * Martin Oberhuber (Wind River) - [235360][ftp][ssh][local] Return proper "Root" IHostFile
 * Martin Oberhuber (Wind River) - [240738][ftp] Incorrect behavior on getFile for non-existing folder
 * David McKnight   (IBM)        - [243921] FTP subsystem timeout causes error when expanding folders
 * Martin Oberhuber (Wind River) - [217472][ftp] Error copying files with very short filenames
 * Martin Oberhuber (Wind River) - [285942] Throw exception when listing a non-folder
 * Martin Oberhuber (Wind River) - [285948] Avoid recursive deletion over symbolic links
 * Martin Oberhuber (Wind River) - [300398] Avoid product hang-up on isConnected()
 * Martin Oberhuber (Wind River) - [305986] NPE due to race condition in isConnected()
 * Martin Oberhuber (Wind River) - [408092] Fix incorrect parsing of "PWD" with Commons Net 3.2
 ********************************************************************************/

package org.eclipse.rse.internal.services.files.globusonline;

import gov.pnnl.gotransfer.service.GlobusOnlineTransferer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ProtocolCommandListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.internal.files.ui.resources.SystemRemoteEditManager;
import org.eclipse.rse.internal.services.files.globusonline.parser.IGlobusOnlineClientConfigFactory;
import org.eclipse.rse.internal.services.files.globusonline.parser.IGlobusOnlineClientConfigProxy;
import org.eclipse.rse.services.Mutex;
import org.eclipse.rse.services.clientserver.FileTypeMatcher;
import org.eclipse.rse.services.clientserver.IMatcher;
import org.eclipse.rse.services.clientserver.NamePatternMatcher;
import org.eclipse.rse.services.clientserver.PathUtility;
import org.eclipse.rse.services.clientserver.messages.SystemElementNotFoundException;
import org.eclipse.rse.services.clientserver.messages.SystemLockTimeoutException;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.clientserver.messages.SystemNetworkIOException;
import org.eclipse.rse.services.clientserver.messages.SystemOperationCancelledException;
import org.eclipse.rse.services.clientserver.messages.SystemUnsupportedOperationException;
import org.eclipse.rse.services.files.AbstractFileService;
import org.eclipse.rse.services.files.IFilePermissionsService;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.IHostFilePermissions;
import org.eclipse.rse.services.files.IHostFilePermissionsContainer;
import org.eclipse.rse.services.files.RemoteFileIOException;
import org.eclipse.rse.services.files.RemoteFileSecurityException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.globusonline.transfer.APIError;
import org.globusonline.transfer.JSONTransferAPIClient;
import org.globusonline.transfer.JSONTransferAPIClient.Result;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.rse.ui.SystemBasePlugin;

public class GlobusOnlineService extends AbstractFileService implements IGlobusOnlineService, IFilePermissionsService
{
	private GlobusOnlineTransferer transferer = new GlobusOnlineTransferer();
	private JSONTransferAPIClient _globusOnlineClient;
	private static long GLOBUS_ONLINE_CONNECTION_CHECK_TIMEOUT = 30000; //msec before checking connection with NOOP
	private GlobusOnlineFile[] _globusOnlineFiles = new GlobusOnlineFile[0];

	private Mutex _commandMutex = new Mutex();

	private String userHome = new String();
    
	private boolean   _caseSensitive = true;
	private transient String _globusEndpoint;
	private transient static String _globusSrcEndpoint;
	private transient String _controlEncoding; //Encoding to be used for file and path names
	private transient String _directory; 

//	private OutputStream _globusOnlineLoggingOutputStream;
	private ProtocolCommandListener _globusOnlineProtocolCommandListener;
	private IPropertySet _globusOnlinePropertySet;
	private Exception _exception;

	private boolean _isBinaryFileType = true;
	private boolean _isPassiveDataConnectionMode = false;
	private IGlobusOnlineClientConfigFactory _entryParserFactory;
	private IGlobusOnlineClientConfigProxy _clientConfigProxy;

	//workaround to access FTPHostFile objects previously retrieved from the server
	//to avoid accessing the remote target when not necessary (bug 195830)
	//In the future, it would be better that the IHostFile object were passed from
	//the upper layer instead of the folder and file name.
	//See bug 162950.
	private String _fCachePreviousParent;
	private long _fCachePreviousTimestamp;
	private Map _fCachePreviousFiles = new HashMap();
	private static long GLOBUS_ONLINE_STATCACHE_TIMEOUT = 200; //msec

  public void setUserHome(String userHome) {
    this.userHome = userHome;
  }

  private static class GlobusOnlineBufferedInputStream extends BufferedInputStream {

//		private GlobusOnlineClient client;


//		public GlobusOnlineBufferedInputStream(InputStream in, GlobusOnlineClient client) {
		public GlobusOnlineBufferedInputStream(InputStream in) {
			super(in);
//			this.client = client;
		}

//		public GlobusOnlineBufferedInputStream(InputStream in, int size, GlobusOnlineClient client) {
		public GlobusOnlineBufferedInputStream(InputStream in, int size) {
			super(in, size);
//			this.client = client;
		}

		public void close() throws IOException {
			super.close();
//			client.completePendingCommand();
//			client.logout();
		}
	}

	private class GlobusOnlineBufferedOutputStream extends BufferedOutputStream {

//		private GlobusOnlineClient client;

//		public GlobusOnlineBufferedOutputStream(OutputStream out, GlobusOnlineClient client) {
		public GlobusOnlineBufferedOutputStream(OutputStream out) {
			super(out);
//			this.client = client;
		}

//		public GlobusOnlineBufferedOutputStream(OutputStream out, int size, GlobusOnlineClient client) {
		public GlobusOnlineBufferedOutputStream(OutputStream out, int size) {
			super(out, size);
//			this.client = client;
		}

		public void close() throws IOException {
			super.close();
//			client.completePendingCommand();
//			client.logout();
		}
	}


	/**
	 * Set a IPropertySet containing pairs of keys and values with
	 * the FTP Client preferences<br/>
	 * Supported keys and values are:<br/>
	 * <table border="1">
	 * <tr><th>KEY</th><th>VALUE</th><th>Usage</th></tr>
	 * <tr><th>"passive"</th><th>"true" | "false"</th><th>Enables FTP passive mode</th></tr>
	 * </table>
	 *
	 * @see org.eclipse.rse.core.model.IPropertySet
	 * @param ftpPropertySet FTP Client Preference Properties to set
	 */
	public void setPropertySet(IPropertySet globusOnlinePropertySet)
	{
		_globusOnlinePropertySet = globusOnlinePropertySet;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.IService#getName()
	 */
	public String getName()
	{
		return GlobusOnlineServiceResources.Globus_Online_File_Service_Name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.IService#getDescription()
	 */
	public String getDescription()
	{
		return GlobusOnlineServiceResources.Globus_Online_File_Service_Description;
	}

	public void setGlobusEndpoint(String globusEndpoint) {
		_globusEndpoint = globusEndpoint;
	}
	
	public String getGlobusEndpoint() {
		return _globusEndpoint;
	}
	
	public void setGlobusOnlineClient(JSONTransferAPIClient client) {
	  this._globusOnlineClient = client;
	}


//	public void setLoggingStream(OutputStream  globusOnlineLoggingOutputStream)
//	{
//		 _globusOnlineLoggingOutputStream =  globusOnlineLoggingOutputStream;
//	}

	public void setGlobusOnlineClientConfigFactory(IGlobusOnlineClientConfigFactory entryParserFactory)
	{
		_entryParserFactory = entryParserFactory;
	}

	/**
     * Set the character encoding to be used on the FTP command channel.
     * The encoding must be compatible with ASCII since FTP commands will
     * be sent with the same encoding. Therefore, wide
     * (16-bit) encodings are not supported.
     * @param encoding Encoding to set
     */
	public void setControlEncoding(String encoding)
	{
		_controlEncoding = encoding;
	}

	/**
	 * Check whether the given Unicode String can be properly represented with the
	 * specified control encoding. Throw a SystemMessageException if it turns out
	 * that information would be lost.
	 * @param s String to check
	 * @return the original String or a quoted or re-coded version if possible
	 * @throws SystemMessageException if information is lost
	 */
	protected String checkEncoding(String s) throws SystemMessageException {
		if (s == null || s.length() == 0)
			return s;
		/*
		String encoding = _controlEncoding!=null ? _controlEncoding : getGlobusOnlineClient(false).getControlEncoding();
		try {
			byte[] bytes = s.getBytes(encoding);
			String decoded = new String(bytes, encoding);
			if (!s.equals(decoded)) {
				int i=0;
				int lmax = Math.min(s.length(), decoded.length());
				while( (i<lmax) && (s.charAt(i)==decoded.charAt(i))) {
					i++;
				}
				//String sbad=s.substring(Math.max(i-2,0), Math.min(i+2,lmax));
				char sbad = s.charAt(i);
				//FIXME Need to externalize this message in 3.0
				String msg = "Cannot express character \'"+sbad+"\'(0x"+Integer.toHexString(sbad)  +") with " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "encoding \""+encoding+"\". "; //$NON-NLS-1$ //$NON-NLS-2$
				msg += "Please specify a different encoding in host properties.";  //$NON-NLS-1$
				throw new UnsupportedEncodingException(msg);
			}
			return s;
		} catch(UnsupportedEncodingException e) {
			SystemMessage msg = new SystemMessage("RSE","F","9999",'E',e.getMessage(),""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			throw new SystemMessageException(msg);

		}
		*/
		return s;
	}

	private SystemMessageException makeSystemMessageException(Exception e) {
		if (e instanceof SystemMessageException) {
			// dont wrap SystemMessageException again
			return (SystemMessageException) e;
		} else if (e instanceof IOException) {
			return new SystemNetworkIOException(e);
		}
		return new RemoteFileIOException(e);
	}
	

	public void initService(IProgressMonitor monitor) throws SystemMessageException {
//		Activator.trace("SftpFileService.initService"); //$NON-NLS-1$
		super.initService(monitor);
		//connect();
	}

	public void uninitService(IProgressMonitor monitor) {
//		Activator.trace("SftpFileService.uninitService"); //$NON-NLS-1$
	  clearCache(null);
		super.uninitService(monitor);
	}
	
	public Shell getShell() {
		Shell activeShell = SystemBasePlugin.getActiveWorkbenchShell();
		if (activeShell != null) {
			return activeShell;
		}

		IWorkbenchWindow window = null;
		try {
			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		} catch (Exception e) {
			return null;
		}
		if (window == null) {
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
			.getWorkbenchWindows();
			if (windows != null && windows.length > 0) {
				return windows[0].getShell();
			}
		} else {
			return window.getShell();
		}

		return null;
	}


//	private void chdir(GlobusOnlineClient globusOnlineClient, String remoteFolder) throws SystemMessageException {
	private void chdir(String remoteFolder) throws SystemMessageException {
		_directory = remoteFolder;
		// try to retrieve the file
//		try {
//			if (!globusOnlineClient.changeWorkingDirectory(remoteFolder)) {
//				String reply = globusOnlineClient.getReplyString();
//				if (reply != null && reply.startsWith("550")) { //$NON-NLS-1$
//					if (!reply.trim().endsWith("Not a directory.")) { //$NON-NLS-1$
//						// No such file or directory
//						throw new SystemElementNotFoundException(remoteFolder, "chdir"); //$NON-NLS-1$
//					}
//				}
//				throw new RemoteFileIOException(new Exception(reply + " (" + remoteFolder + ")")); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		} catch (IOException e) {
//			throw new SystemNetworkIOException(e);
//		}
	}


	
	/**
	 * Returns the commons.net FTPClient for this session.
	 *
	 * As a side effect, it also checks the connection
	 * by sending a NOOP to the remote side, and initiates
	 * a connect in case the NOOP throws an exception.
	 * 
	 * In order to avoid race conditions by this sending
	 * of NOOP and its related return code, this sending
	 * of NOOP must always be protected by a command mutex.
	 *
	 * @return The commons.net FTPClient.
	 */
		/*
	public GlobusOnlineClient getGlobusOnlineClient() {
		return getGlobusOnlineClient(true);
	}
	*/
	
	/**
	 * Returns the commons.net FTPClient for this session.
	 *
	 * @param checkConnection <code>true</code> to request
	 *    sending a NOOP command as a side-effect in order
	 *    to check connection or re-connect. 
	 *    When this is done, the call must be protected
	 *    by a command mutex in order to avoid race conditions
	 *    between sending the NOOP command and awaiting its
	 *    response.
	 *
	 * @return The commons.net FTPClient.
	 */
	/*
	public GlobusOnlineClient getGlobusOnlineClient(boolean checkConnection)
	{
		if (_globusOnlineClient == null)
		{
			_globusOnlineClient = new GlobusOnlineClient();
			// Encoding of control connection
			if(_controlEncoding!=null) {
				_globusOnlineClient.setControlEncoding(_controlEncoding);
			}
		}

		if(_hostName!=null && checkConnection)
		{
			long curTime = System.currentTimeMillis();
			if (curTime - _globusOnlineLastCheck > GLOBUS_ONLINE_CONNECTION_CHECK_TIMEOUT) {
				_globusOnlineLastCheck = curTime;
				try{
					_globusOnlineClient.sendNoOp();
				}catch (IOException e){
					try {
						connect();
					} catch (Exception e1) {}
				}
			}
		}

		setDataConnectionMode();

		return _globusOnlineClient;
	}
	*/

	/**
	 * Clones the main FTP client connection, providing a separate client connected to the FTP server.
	 *
	 * @param isBinary true if the FTPClient has to be using binary mode for data transfer, otherwise ASCII mode will be used
	 * @return A new commons.net FTPClient connected to the same server. After usage it has to be disconnected.
	 * @throws IOException
	 */
/*
	private GlobusOnlineClient cloneGlobusOnlineClient(boolean isBinary) throws IOException
	{
		GlobusOnlineClient globusOnlineClient = new GlobusOnlineClient();
		boolean ok=false;
		try {
			globusOnlineClient.setControlEncoding(_globusOnlineClient.getControlEncoding());
			globusOnlineClient.connect(_globusOnlineClient.getRemoteAddress());
			globusOnlineClient.login(_userId,_password);

			if (_clientConfigProxy != null) {
				globusOnlineClient.configure(_clientConfigProxy.getGlobusOnlineClientConfig());
			} else {
				// UNIX parsing by default if no suitable parser found
				globusOnlineClient.configure(new GlobusOnlineClientConfig(GlobusOnlineClientConfig.SYST_UNIX));
			}

			if (_isPassiveDataConnectionMode) {
				globusOnlineClient.enterLocalPassiveMode();
			}

			if (isBinary) {
				globusOnlineClient.setFileType(GLOBUS_ONLINE.BINARY_FILE_TYPE);
			} else {
				globusOnlineClient.setFileType(GLOBUS_ONLINE.ASCII_FILE_TYPE);
			}
			if (_globusOnlineProtocolCommandListener != null) {
				globusOnlineClient.addProtocolCommandListener(_globusOnlineProtocolCommandListener);
			}
			ok=true;
		} finally {
			//disconnect the erroneous ftpClient, but forward the exception
			if (!ok) {
				try {
					globusOnlineClient.disconnect();
				} catch(Throwable t) { }
			}
		}
		return globusOnlineClient;
	}
*/


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getFile(String, String, IProgressMonitor)
	 */
	public IHostFile getFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		return getFileInternal(remoteParent, fileName, monitor);
	}


	/**
	 * Return FTPHostFile object for a given parent dir and file name.
	 * This is different than {@link #getFile(String, String, IProgressMonitor)}
	 * in order to ensure we always return proper FTPHostFile type.
	 *
	 * @see org.eclipse.rse.services.files.IFileService#getFile(String, String, IProgressMonitor)
	 */
	protected GlobusOnlineHostFile getFileInternal(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException
	{
		boolean isRoot = (remoteParent == null || remoteParent.length() == 0);
		if (isRoot) {
			// FTP doesn't really support getting properties of Roots yet. For
			// now, return the root and claim it's existing.
			return new GlobusOnlineHostFile(remoteParent, fileName, true, true, 0, 0, true);
		}
		remoteParent = checkEncoding(remoteParent);
    	fileName = checkEncoding(fileName);
		if (monitor!=null){
			if (monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
		}

		//Try the cache first, perhaps there is no need to acquire the Mutex
		//The cache is case sensitive only on purpose. For case insensitive matches
		//A fresh LIST is required.
		//
	    //In the future, it would be better that the
	    //IHostFile object were passed from the upper layer instead of the
	    //folder and file name (Bug 162950)
		synchronized(_fCachePreviousFiles) {
			if (_fCachePreviousParent == null ? remoteParent==null : _fCachePreviousParent.equals(remoteParent)) {
				Object result = _fCachePreviousFiles.get(fileName);
				if (result!=null) {
					long diff = System.currentTimeMillis() - _fCachePreviousTimestamp;
					//System.out.println("FTPCache: "+diff+", "+remoteParent+", "+fileName); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					if (diff < GLOBUS_ONLINE_STATCACHE_TIMEOUT) {
						return (GlobusOnlineHostFile)result;
					}
				}
			}
		}

		GlobusOnlineHostFile file = null;
		
		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
		{
			try {
				//try to retrieve the file
				//GlobusOnlineClient globusOnlinec = getGlobusOnlineClient();
				//chdir(globusOnlinec, remoteParent);
				chdir(remoteParent);
				if(!listFiles(monitor))
				{
					throw new SystemOperationCancelledException();
				}

				synchronized (_fCachePreviousFiles) {
					cacheFiles(remoteParent);

					// Bug 198645: try exact match first
					Object o = _fCachePreviousFiles.get(fileName);
					if (o!=null) return (GlobusOnlineHostFile)o;

					// try case insensitive match (usually never executed)
					if (!isCaseSensitive()) {
						for (int i = 0; i < _globusOnlineFiles.length; i++) {
							String tempName = _globusOnlineFiles[i].getName();
							if (tempName.equalsIgnoreCase(fileName)) {
								file = (GlobusOnlineHostFile) _fCachePreviousFiles.get(tempName);
								break;
							}
						}
					}
				}

				// if not found, create new object with non-existing flag
				if(file == null)
				{
					file = new GlobusOnlineHostFile(remoteParent,fileName, false, false, 0, 0, false);
				}
			} catch (SystemElementNotFoundException senfe) {
				// Return non-existing file
				file = new GlobusOnlineHostFile(remoteParent, fileName, false, false, 0, 0, false);
			} catch (Exception e) {
				throw makeSystemMessageException(e);
			} finally {
				_commandMutex.release();
		    }
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}

		return file;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#internalFetch(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, int)
	 */
	protected IHostFile[] internalFetch(String parentPath, String fileFilter, int fileType, IProgressMonitor monitor) throws SystemMessageException
	{
    	parentPath = checkEncoding(parentPath);
		if (monitor!=null){
			if (monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
		}

		List results = new ArrayList();

		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
		{
			try
			{
				if (fileFilter == null)
				{
					fileFilter = "*"; //$NON-NLS-1$
				}
				IMatcher filematcher = null;
				if (fileFilter.endsWith(",")) {  //$NON-NLS-1$
					String[] types = fileFilter.split(",");  //$NON-NLS-1$
					filematcher = new FileTypeMatcher(types, true);
				} else {
					filematcher = new NamePatternMatcher(fileFilter, true, true);
				}

//				GlobusOnlineClient globusOnlinec = getGlobusOnlineClient();
//				chdir(globusOnlinec, parentPath);
				chdir(parentPath);
				
				if(!listFiles(monitor))
				{
					throw new SystemOperationCancelledException();
				}

				synchronized (_fCachePreviousFiles) {
					cacheFiles(parentPath);

					for(int i=0; i<_globusOnlineFiles.length; i++)
					{
						if(_globusOnlineFiles[i]==null)
						{
							continue;
						}

//						String rawListLine = _globusOnlineFiles[i].getRawListing()+System.getProperty("line.separator"); //$NON-NLS-1$
//						_globusOnlineLoggingOutputStream.write(rawListLine.getBytes());

						String name = _globusOnlineFiles[i].getName();
						GlobusOnlineHostFile f = (GlobusOnlineHostFile)_fCachePreviousFiles.get(name);

						if (isRightType(fileType,f)) {

							if (name.equals(".") || name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
								//Never return the default directory names
								continue;
							} else if (f.isDirectory() && fileType!=IFileService.FILE_TYPE_FOLDERS) {
								//get ALL directory names (unless looking for folders only)
								results.add(f);
							} else if (filematcher.matches(name)) {
								//filter all others by name.
								results.add(f);
							}
						}
					}
				}
//				_globusOnlineLoggingOutputStream.write(System.getProperty("line.separator").getBytes()); //$NON-NLS-1$
			}
			catch (Exception e)
			{
				throw makeSystemMessageException(e);
			} finally {
				_commandMutex.release();
		    }
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}

		return (IHostFile[])results.toArray(new IHostFile[results.size()]);
	}


	private char getSeparator()
	{
		return PathUtility.getSeparator(userHome).charAt(0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.File, java.lang.String, java.lang.String, boolean, java.lang.String, java.lang.String)
	 */
	public void upload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
    	remoteParent = checkEncoding(remoteParent);
    	remoteFile = checkEncoding(remoteFile);

		if (monitor!=null){
			if (monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
		}
		else{
				monitor = new NullProgressMonitor();
		}

		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		progressMonitor.init(0, localFile.getName(), remoteFile, localFile.length());

		try {
			if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
			{
				try
				{
					internalUpload(localFile, remoteParent, remoteFile, isBinary, srcEncoding, hostEncoding, progressMonitor);
				}
				finally {
					_commandMutex.release();
			    }
			} else {
				throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
			}
		} catch (SystemMessageException e) {
			throw e;
		} catch(Exception e) {
			throw new RemoteFileIOException(e);
		} finally {
			progressMonitor.end();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#upload(org.eclipse.core.runtime.IProgressMonitor, java.io.InputStream, java.lang.String, java.lang.String, boolean, java.lang.String)
	 */
	public void upload(InputStream stream, String remoteParent, String remoteFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
    	remoteParent = checkEncoding(remoteParent);
    	remoteFile = checkEncoding(remoteFile);

		try
		{
			BufferedInputStream bis = new BufferedInputStream(stream);
			File tempFile = File.createTempFile("globusonlineup", "temp"); //$NON-NLS-1$ //$NON-NLS-2$
			FileOutputStream os = new FileOutputStream(tempFile);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			 byte[] buffer = new byte[4096];
			 int readCount;
			 while( (readCount = bis.read(buffer)) > 0)
			 {
			      bos.write(buffer, 0, readCount);
			      if (monitor!=null) {
					if (monitor.isCanceled()) {
						throw new SystemOperationCancelledException();
					}
				  }
			 }
			 bos.close();
			 upload(tempFile, remoteParent, remoteFile, isBinary, null, hostEncoding, monitor);

		}
		catch (Exception e) {
			throw new RemoteFileIOException(e);
	  }

	}

	private void internalUpload(File localFile, String remoteParent, String remoteFile, boolean isBinary, String srcEncoding, String hostEncoding, MyProgressMonitor progressMonitor) throws IOException, RemoteFileIOException, SystemMessageException
	{

		try{

			// parse the file path to get the source information
			
			// 1. remote temp directory
			SystemRemoteEditManager editMgr = SystemRemoteEditManager.getInstance();
			String filePath = editMgr.getRemoteEditProjectLocation().makeAbsolute().toOSString();
			String localFilePath = localFile.getAbsolutePath().replace(filePath, "");

			// 2. get the hostname
//			String srcHostname = localFilePath.substring(1, localFilePath.indexOf('/',1));
//			srcHostname = srcHostname.toLowerCase();
			
			// 2b. translate between hostname and endpoint
//			String srcEndpoint = srcHostname;
			
			// 3. remove the endpoint from the file path.  get the file location on the source machine.
			String srcFilepath = localFilePath.substring(localFilePath.indexOf('/',1),localFilePath.length());
			
            System.out.println("=== Before Transfer ===");
            
            Result r = _globusOnlineClient.getResult("/transfer/submission_id");
            String submissionId = r.document.getString("value");
            JSONObject transfer = new JSONObject();
            transfer.put("DATA_TYPE", "transfer");
            transfer.put("submission_id", submissionId);
            JSONObject item = new JSONObject();
            item.put("DATA_TYPE", "transfer_item");
            item.put("source_endpoint", _globusSrcEndpoint.toLowerCase());
            item.put("source_path", srcFilepath);
            item.put("destination_endpoint", _globusEndpoint.toLowerCase());
            item.put("destination_path", concat(remoteParent, remoteFile));
            transfer.append("DATA", item);

            r = _globusOnlineClient.postResult("/transfer", transfer, null);
            String taskId = r.document.getString("task_id");

            System.out.println("=== After Transfer ===");
            
            listFiles(progressMonitor.fMonitor);

		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#download(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.io.File, boolean, java.lang.String)
	 */
	public void download(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding, IProgressMonitor monitor) throws SystemMessageException
	{
		_globusSrcEndpoint = _globusEndpoint;

		if (monitor!=null){
			if (monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
		}

		IHostFile remoteHostFile = getFile(remoteParent, remoteFile, monitor);
		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		progressMonitor.init(0, remoteFile, localFile.getName(), remoteHostFile.getSize());

		try {
			if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
			{
				try
				{
					internalDownload(remoteParent, remoteFile, localFile, isBinary, hostEncoding, progressMonitor);
				}
				finally
				{
					_commandMutex.release();
				}
			} else {
				throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
			}
		} catch (FileNotFoundException e) {
			throw new RemoteFileIOException(e);
		} catch (IOException e) {
			throw new RemoteFileIOException(e);
		} finally {
			progressMonitor.end();

		}
	}

	private void internalDownload(String remoteParent, String remoteFile, File localFile, boolean isBinary, String hostEncoding, MyProgressMonitor progressMonitor) throws SystemMessageException, IOException
	{
		
		/*
		try{

			while(remoteParent.endsWith("/")) {
				remoteParent = remoteParent.substring(0, remoteParent.length()-1);
			}
			while(remoteParent.startsWith("/")) {
				remoteParent = remoteParent.substring(1, remoteParent.length());
			}
			
			transferer.recursivelyCreateDir(_globusOnlineClient, "pic#dtn", remoteParent);
			_globusOnlineClient = transferer.activateEndpoint(_endPoint.toLowerCase(), _userId, _password);
			
			
            System.out.println("=== Before Transfer ===");
            
            Result r = _globusOnlineClient.getResult("/transfer/submission_id");
            String submissionId = r.document.getString("value");
            JSONObject transfer = new JSONObject();
            transfer.put("DATA_TYPE", "transfer");
            transfer.put("submission_id", submissionId);
            JSONObject item = new JSONObject();
            item.put("DATA_TYPE", "transfer_item");
            item.put("source_endpoint", "pic#dtn");
            item.put("source_path", remoteParent + "/" + remoteFile);
            item.put("destination_endpoint", _endPoint.toLowerCase());
            item.put("destination_path", remoteParent);
            transfer.append("DATA", item);

            r = _globusOnlineClient.postResult("/transfer", transfer, null);
            String taskId = r.document.getString("task_id");

            System.out.println("=== After Transfer ===");


		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

	}



	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getUserHome()
	 */
	public IHostFile getUserHome()
	{
		if (userHome==null) {
			//As per bug 204710, this may be called before we are connected.
			//Returning null in this case is safest, see also GlobusOnlineFileService.
			return null;
		}
		return new GlobusOnlineHostFile(null, userHome, true, true, 0, 0, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#getRoots(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IHostFile[] getRoots(IProgressMonitor monitor)
	{

		IHostFile[] hostFile;

		if(userHome.startsWith("/")) //$NON-NLS-1$
		{
			hostFile = new IHostFile[]{new GlobusOnlineHostFile(null, "/", true, true, 0, 0, true)}; //$NON-NLS-1$
		}
		else
		{
			hostFile = new IHostFile[]{new GlobusOnlineHostFile(null, userHome, true, true, 0, 0, true)};
		}

		return hostFile;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#delete(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public void delete(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException {
		
    	remoteParent = checkEncoding(remoteParent);
    	fileName = checkEncoding(fileName);

		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		progressMonitor.init(GlobusOnlineServiceResources.Globus_Online_File_Service_Deleting_Task + fileName, IProgressMonitor.UNKNOWN);
		try {
			IHostFile file = getFile(remoteParent, fileName, monitor);

			if (_commandMutex.waitForLock(monitor, Long.MAX_VALUE)) {
				try {
					//Try to delete even if it looked like the file doesn't exist,
					//since existence might have been cached and be out-of-date
//					GlobusOnlineClient globusOnlineClient = getGlobusOnlineClient();
//					internalDelete(globusOnlineClient, remoteParent, fileName, file.isFile(), progressMonitor);
					internalDelete(_globusOnlineClient, remoteParent, fileName, file.isFile(), progressMonitor);
				}
				catch (IOException e)
				{
					if (!file.exists())
						throw new SystemElementNotFoundException(file.getAbsolutePath(), "delete"); //$NON-NLS-1$
					throw new RemoteFileIOException(e);
				}
				catch (SystemMessageException e) {
					if (!file.exists())
						throw new SystemElementNotFoundException(file.getAbsolutePath(), "delete"); //$NON-NLS-1$
					throw e;
				}
				finally {
					_commandMutex.release();
				}
			} else {
				throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
			}
		} finally {
			progressMonitor.end();
		}
		
	}

	
	//private void internalDelete(GlobusOnlineClient globusOnlineClient, String parentPath, String fileName, boolean isFile, MyProgressMonitor monitor)
	private void internalDelete(JSONTransferAPIClient globusOnlineClient, String parentPath, String fileName, boolean isFile, MyProgressMonitor monitor)
			throws SystemMessageException, IOException
	{
		if(monitor.isCanceled())
		{
			throw new SystemOperationCancelledException();
		}

		clearCache(parentPath);
        
//		boolean hasSucceeded = GlobusOnlineReply.isPositiveCompletion(globusOnlineClient.cwd(parentPath));
		boolean hasSucceeded = true;
		Result result = null;
		monitor.worked(1);

		if(hasSucceeded)
		{
			try {
				if(isFile)
				{
					result = _globusOnlineClient.deleteResult(concat(parentPath,fileName).replaceAll(" ", "%20"));
					hasSucceeded = result.statusCode == 0;
	//				hasSucceeded = globusOnlineClient.deleteFile(fileName);
					monitor.worked(1);
				}
				else
				{
					result = _globusOnlineClient.deleteResult(concat(parentPath,fileName));
					hasSucceeded = result.statusCode == 0;
	//				hasSucceeded = globusOnlineClient.removeDirectory(fileName);
					monitor.worked(1);
				}
			}catch(Exception exc) {
				
			}
		}

		if(!hasSucceeded){
			if(isFile)
			{
//				throw new RemoteFileIOException(new Exception(globusOnlineClient.getReplyString()+" ("+concat(parentPath,fileName)+")")); //$NON-NLS-1$ //$NON-NLS-2$
				throw new RemoteFileIOException(new Exception(result.statusMessage+" ("+concat(parentPath,fileName)+")")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else //folder recursively
			{
				String newParentPath = concat(parentPath,fileName);

				try {
//					chdir(globusOnlineClient, newParentPath);
					chdir(newParentPath);
					List fileObjectList = transferer.getEndpointFolderContents(globusOnlineClient, _globusEndpoint, newParentPath);
	//				GlobusOnlineFile[] fileNames = globusOnlineClient.listFiles();
					int size = fileObjectList.size();
					for(int idx=0;idx<size;idx++) {
						JSONObject fileObject = (JSONObject)fileObjectList.get(idx);
						String curName = fileObject.getString("name");
	//				for (int i = 0; i < fileNames.length; i++) {
	//					String curName = fileNames[i].getName();
						
						if (curName == null || curName.equals(".") || curName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
							continue;
						}
	//					internalDelete(globusOnlineClient, newParentPath, curName, fileNames[i].isFile() || fileNames[i].isSymbolicLink(), monitor);
						internalDelete(globusOnlineClient, newParentPath, curName, true || false, monitor);
					}
	
					//remove empty folder
	//				chdir(globusOnlineClient, parentPath);
	//				hasSucceeded = globusOnlineClient.removeDirectory(fileName);
					chdir(parentPath);
					result = globusOnlineClient.deleteResult(concat(parentPath,fileName));
					hasSucceeded = result.statusCode == 0;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (!hasSucceeded)
				{
//					throw new RemoteFileIOException(new Exception(globusOnlineClient.getReplyString() + " (" + concat(parentPath, fileName) + ")")); //$NON-NLS-1$ //$NON-NLS-2$
					throw new RemoteFileIOException(new Exception(result.statusMessage + " (" + concat(parentPath, fileName) + ")")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void rename(String remoteParent, String oldName, String newName, IProgressMonitor monitor) throws SystemMessageException {
		
    	remoteParent = checkEncoding(remoteParent);
    	oldName = checkEncoding(oldName);
    	newName = checkEncoding(newName);

		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
		{
			try {
//				GlobusOnlineClient globusOnlineClient = getGlobusOnlineClient();
				clearCache(remoteParent);
//				chdir(globusOnlineClient, remoteParent);
				chdir(remoteParent);
//				boolean success = globusOnlineClient.rename(oldName, newName);

//				if(!success)
//				{
//					throw new Exception(globusOnlineClient.getReplyString());
//				}

			} catch (Exception e) {
				throw makeSystemMessageException(e);
			}finally {
				_commandMutex.release();
			}
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#rename(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, org.eclipse.rse.services.files.IHostFile)
	 */
	public void rename(String remoteParent, String oldName, String newName, IHostFile oldFile, IProgressMonitor monitor) {
		oldFile.renameTo(newName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#move(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void move(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException{
		
    	srcParent = checkEncoding(srcParent);
    	srcName = checkEncoding(srcName);
    	tgtParent = checkEncoding(tgtParent);
    	tgtName = checkEncoding(tgtName);

		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
		{
			try{
//				GlobusOnlineClient globusOnlineClient = getGlobusOnlineClient();

				String source = concat(srcParent,srcName);
				String target = concat(tgtParent,tgtName);

				clearCache(srcParent);
				clearCache(tgtParent);
//				boolean success = globusOnlineClient.rename(source, target);

//				if(!success)
//				{
//					throw new Exception(globusOnlineClient.getReplyString());
//				}

			}catch (Exception e) {
				throw new RemoteFileIOException(e);
			}finally {
				_commandMutex.release();
			}
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#createFolder(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
	 */
	public IHostFile createFolder(String remoteParent, String folderName, IProgressMonitor monitor) throws SystemMessageException
	{
		remoteParent = checkEncoding(remoteParent);
		folderName = checkEncoding(folderName);
		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE))
		{
			try
			{
//				GlobusOnlineClient globusOnlineClient = getGlobusOnlineClient();
				clearCache(remoteParent);
//				chdir(globusOnlineClient, remoteParent);
//				if(!globusOnlineClient.makeDirectory(folderName))
				chdir(remoteParent);
				if(!transferer.createFolder(_globusOnlineClient, _globusEndpoint, folderName))
				{
//					throw new RemoteFileIOException(new Exception(globusOnlineClient.getReplyString()+" ("+folderName+")"));  //$NON-NLS-1$  //$NON-NLS-2$
					throw new RemoteFileIOException(new Exception("Could not create folder, " + folderName));
				}

			}
			catch (Exception e)	{
				throw makeSystemMessageException(e);
			}finally {
				_commandMutex.release();
			}
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}

		return getFile(remoteParent, folderName, monitor);
	}

    /* (non-Javadoc)
     * @see org.eclipse.rse.services.files.IFileService#createFile(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String)
     */
    public IHostFile createFile(String remoteParent, String fileName, IProgressMonitor monitor) throws SystemMessageException{
    	
    	remoteParent = checkEncoding(remoteParent);
    	fileName = checkEncoding(fileName);
    	try {
			File tempFile = File.createTempFile("globusOnline", "temp");  //$NON-NLS-1$  //$NON-NLS-2$
			tempFile.deleteOnExit();
			try {
				upload(tempFile, remoteParent, fileName, _isBinaryFileType, null, null, monitor);
			} catch (SystemMessageException e) {
//				throw new RemoteFileIOException(new Exception(getGlobusOnlineClient().getReplyString()));
				throw new RemoteFileIOException(new Exception("Could not create file, " + fileName));
			}
		}
		catch (Exception e) {
			throw new RemoteFileSecurityException(e);
		}

		return getFile(remoteParent, fileName, monitor);
	}

    /*
     * (non-Javadoc)
     * @see org.eclipse.rse.services.files.IFileService#copy(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void copy(String srcParent, String srcName, String tgtParent, String tgtName, IProgressMonitor monitor) throws SystemMessageException
	{
    	
    	srcParent = checkEncoding(srcParent);
    	srcName = checkEncoding(srcName);
    	tgtParent = checkEncoding(tgtParent);
    	tgtName = checkEncoding(tgtName);

    	if (monitor!=null){
			if (monitor.isCanceled()) {
				throw new SystemOperationCancelledException();
			}
		}

    	IHostFile remoteHostFile = getFile(srcParent, srcName, monitor);
		MyProgressMonitor progressMonitor = new MyProgressMonitor(monitor);
		progressMonitor.init(0, concat(srcParent,srcName), concat(tgtParent,tgtName), remoteHostFile.getSize()*2);


		if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE)) {
			try {

//				internalCopy(getGlobusOnlineClient(), srcParent, srcName, tgtParent, tgtName, remoteHostFile.isDirectory(), progressMonitor);
				internalCopy(_globusOnlineClient, srcParent, srcName, tgtParent, tgtName, remoteHostFile.isDirectory(), progressMonitor);
			}
			catch(IOException e)
			{
//				throw new RemoteFileIOException(new Exception(getGlobusOnlineClient().getReplyString()));
				throw new RemoteFileIOException(new Exception("Could not copy file, " + srcName));
			}
			finally {
				_commandMutex.release();
			}
		} else {
			throw new SystemLockTimeoutException(Activator.PLUGIN_ID);
		}
		
    }

//  private void internalCopy(GlobusOnlineClient globusOnlineClient, String srcParent, String srcName, String tgtParent, String tgtName, boolean isDirectory, MyProgressMonitor monitor) throws SystemMessageException, IOException
    private void internalCopy(JSONTransferAPIClient globusOnlineClient, String srcParent, String srcName, String tgtParent, String tgtName, boolean isDirectory, MyProgressMonitor monitor) throws SystemMessageException, IOException
    {
    	if (monitor.isCanceled())
		{
			throw new SystemOperationCancelledException();
		}

//    	if(isDirectory)
//		{

    		try { 
    			
	    		//create folder
	    		// TODO what happens if the destination folder already exists?
				// Success=true or false?
	    		transferer.createFolder(globusOnlineClient, _globusEndpoint, concat(tgtParent,tgtName));
	//    		globusOnlineClient.makeDirectory(concat(tgtParent,tgtName));
	
	    		//copy contents
	    		String newSrcParentPath = concat(srcParent,srcName);
	    		String newTgtParentPath = concat(tgtParent,tgtName);
	
	//			chdir(globusOnlineClient, newSrcParentPath);
	//			GlobusOnlineFile[] fileNames = globusOnlineClient.listFiles();
	    		chdir(newSrcParentPath);
	    		
	    		
	            System.out.println("=== Before Transfer ===");
	            
	            Result r = _globusOnlineClient.getResult("/transfer/submission_id");
	            String submissionId = r.document.getString("value");
	            JSONObject transfer = new JSONObject();
	            transfer.put("DATA_TYPE", "transfer");
	            transfer.put("submission_id", submissionId);
	            JSONObject item = new JSONObject();
	            item.put("DATA_TYPE", "transfer_item");
	            item.put("source_endpoint", "go#ep1");
	            item.put("source_path", srcParent + "/" + srcName);
	            item.put("destination_endpoint", "go#ep2");
	            item.put("destination_path", tgtParent + "/" + tgtName);
	            transfer.append("DATA", item);

	            r = _globusOnlineClient.postResult("/transfer", transfer, null);
	            String taskId = r.document.getString("task_id");

	            System.out.println("=== After Transfer ===");
	    		
	            
	    		List fileNameList = transferer.getEndpointFolderContents(globusOnlineClient, _globusEndpoint, newSrcParentPath);
	    		int size = fileNameList.size();
	    		for(int idx=0;idx<size;idx++) {
	    			JSONObject object = (JSONObject)fileNameList.get(idx);
	    			String curName = object.getString("name");
	
	//			for (int i = 0; i < fileNames.length; i++) {
	//				String curName = fileNames[i].getName();
					if (curName == null || curName.equals(".") || curName.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
						continue;
					}
					// TODO should we bail out in case a single file fails?
	//				internalCopy(globusOnlineClient, newSrcParentPath, curName, newTgtParentPath, curName, fileNames[i].isDirectory(), monitor);
					internalCopy(globusOnlineClient, newSrcParentPath, curName, newTgtParentPath, curName, false, monitor);
				}
	    		
    		} catch(Exception exc) {
    			exc.printStackTrace();
    		}

//		}
    	
/* 
 * Globus Online doesn't support downloading files
		else
		{
			File tempFile = null;

			try {
				tempFile = File.createTempFile("globusOnlinecp" + String.valueOf(srcParent.hashCode()), "temp"); //$NON-NLS-1$ //$NON-NLS-2$
				tempFile.deleteOnExit();
			} catch (IOException e) {
				throw new RemoteFileIOException(e);
			}

			//Use binary raw transfer since the file will be uploaded again
			try {
				internalDownload(srcParent, srcName, tempFile, true, null, monitor);
				internalUpload(tempFile, tgtParent, tgtName, true, null, null, monitor);
			} finally {
				tempFile.delete();
			}
		}
*/

    }

	public void copyBatch(String[] srcParents, String[] srcNames, String tgtParent, IProgressMonitor monitor) throws SystemMessageException
	{
		for(int i=0; i<srcNames.length; i++)
		{
			copy(srcParents[i], srcNames[i], tgtParent, srcNames[i], monitor);
		}
	}

	public void setIsCaseSensitive(boolean b) {
		_caseSensitive = b;
	}

	public boolean isCaseSensitive()
	{
		return _caseSensitive;
	}

	/**
	 * Internal method to list files.
	 * MUST ALWAYS be called from _commandMutex protected region.
	 */
	private boolean listFiles(IProgressMonitor monitor) throws Exception
	{
		boolean result = true;

		_exception = null;

		Thread listThread = new Thread(new Runnable(){

			public void run() {
				try {

//					_globusOnlineFiles = null;

//					if(_clientConfigProxy!=null)
//					{      
						  
					    Map params = new HashMap();
					    if (_globusEndpoint != null) {
					      params.put("path", _globusEndpoint);
					    }

						List fileList = transferer.getEndpointFolderContents(_globusOnlineClient, _globusEndpoint.toLowerCase(), _directory);
						int length = fileList.size();
						_globusOnlineFiles = new GlobusOnlineFile[length];
						for(int idx = 0; idx<length; idx++) {
							_globusOnlineFiles[idx] = new GlobusOnlineFile();
							String name = ((JSONObject)fileList.get(idx)).getString("name");
							String lastModified = ((JSONObject)fileList.get(idx)).getString("last_modified");
							String permissions = ((JSONObject)fileList.get(idx)).getString("permissions");
							String type = ((JSONObject)fileList.get(idx)).getString("type");
							String size = ((JSONObject)fileList.get(idx)).getString("size");
							
							Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastModified);
							Calendar cal = new GregorianCalendar();
							cal.setTime(date);
							_globusOnlineFiles[idx].setTimestamp(cal);
							
							if("dir".equals(type))
								_globusOnlineFiles[idx].setType(GlobusOnlineFile.DIRECTORY_TYPE);
							if("file".equals(type))
								_globusOnlineFiles[idx].setType(GlobusOnlineFile.FILE_TYPE);

							_globusOnlineFiles[idx].setName(name);
							_globusOnlineFiles[idx].setRawListing(name);
							
							//	r = 4
							//	w = 2
							//	x = 1
							
							// user permissions
							int userPerms = Integer.parseInt("" + permissions.toCharArray()[1]);
							if(userPerms >= 4) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.READ_PERMISSION, true);
								userPerms -= 4;
							} 
							if(userPerms >= 2) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.WRITE_PERMISSION, true);
								userPerms -= 2;
							}
							if(userPerms >= 1) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.USER_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION, true);
								userPerms -= 1;
							}

							// group permissions
							int groupPerms = Integer.parseInt("" + permissions.toCharArray()[2]);
							if(groupPerms >= 4) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.READ_PERMISSION, true);
								groupPerms -= 4;
							} 
							if(groupPerms >= 2) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.WRITE_PERMISSION, true);
								groupPerms -= 2;
							}
							if(groupPerms >= 1) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.GROUP_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION, true);
								groupPerms -= 1;
							}

							// world permissions
							int worldPerms = Integer.parseInt("" + permissions.toCharArray()[3]);
							if(worldPerms >= 4) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.READ_PERMISSION, true);
								worldPerms -= 4;
							} 
							if(worldPerms >= 2) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.WRITE_PERMISSION, true);
								worldPerms -= 2;
							}
							if(worldPerms >= 1) {
								_globusOnlineFiles[idx].setPermission(GlobusOnlineFile.WORLD_ACCESS, GlobusOnlineFile.EXECUTE_PERMISSION, true);
								worldPerms -= 1;
							}
							
							_globusOnlineFiles[idx].setSize(Long.parseLong(size));
						}
						
//						transferer.displayLs(client, _endPoint, _directory);
//						List<JSONObject> fileList = transferer.getEndpointFolderContents(client, _endPoint, _directory);
//						_globusOnlineFiles = _globusOnlineClient.listFiles(_clientConfigProxy.getListCommandModifiers());
//					}
//					else
//					{
//						_globusOnlineFiles = _globusOnlineClient.listFiles();
//					}

				} catch (IOException e) {
					_exception = e;
				} catch (Exception e) {
					_exception = e;
					e.printStackTrace();
				}
			}});

		if(monitor != null)
		{
			if(!monitor.isCanceled())
				listThread.start();
			else
				return false;

			//wait

			while(!monitor.isCanceled() && listThread.isAlive())
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}

			//evaluate result

			if(monitor.isCanceled() && listThread.isAlive())
			{
				Thread killThread = listThread;
				listThread = null;
				killThread.interrupt();

//				_globusOnlineClient.completePendingCommand();

				throw new RemoteFileIOException(_exception);
			}

		}
		else
		{
			listThread.start();
			listThread.join();
			if(_exception!=null)
			{
				throw new RemoteFileIOException(_exception);
			}

		}

		return result;
	}

	private void cacheFiles(String parentPath) {
		synchronized (_fCachePreviousFiles) {
			_fCachePreviousFiles.clear();
			_fCachePreviousTimestamp = System.currentTimeMillis();
			_fCachePreviousParent = parentPath;

			for(int i=0; i<_globusOnlineFiles.length; i++) {
				if(_globusOnlineFiles[i]==null) {
					continue;
				}
				GlobusOnlineHostFile f = new GlobusOnlineHostFile(parentPath, _globusOnlineFiles[i]);
				String name = f.getName();
				if(f.isLink()) {
					if(name.indexOf('.') < 0) {
						//modify FTPHostFile to be shown as a folder
						f.setIsDirectory(true);
					}
				}
				_fCachePreviousFiles.put(name, f);
			}
		}
	}

	/** Clear the statCache.
	 * @param parentPath path to clear. If <code>null, clear
	 *    all caches.
	 */
	private void clearCache(String parentPath) {
		synchronized (_fCachePreviousFiles) {
			if (parentPath==null || parentPath.equals(_fCachePreviousParent)) {
				_fCachePreviousFiles.clear();
			}
		}
	}

	private class MyProgressMonitor
	{
		  private IProgressMonitor fMonitor;
		  private double fWorkPercentFactor;
		  private Long fMaxWorkKB;
		  private long fWorkToDate;

		  public MyProgressMonitor(IProgressMonitor monitor) {
			  if (monitor == null) {
				fMonitor = new NullProgressMonitor();
			} else {
				fMonitor = monitor;
			}
		  }

		  public boolean isCanceled() {
			  // embedded null progress monitor is never canceled
			  return fMonitor.isCanceled();
		  }

		  public void init(int op, String src, String dest, long max){
			  fWorkPercentFactor = 1.0 / max;
			  fMaxWorkKB = new Long(max / 1024L);
			  fWorkToDate = 0;
			  String srcFile = new Path(src).lastSegment();
			  String desc = srcFile;
			  fMonitor.beginTask(desc, (int)max);
		  }

		  public void init(String label, int max){
			  fMonitor.beginTask(label, max);
		  }

		  public boolean count(long count){
			  fWorkToDate += count;
			  Long workToDateKB = new Long(fWorkToDate / 1024L);
			  Double workPercent = new Double(fWorkPercentFactor * fWorkToDate);
			  String subDesc = MessageFormat.format(
						 GlobusOnlineServiceResources.Globus_Online_File_Service_Monitor_Format,
						  new Object[] {
							workToDateKB, fMaxWorkKB, workPercent
						  });
			  fMonitor.subTask(subDesc);
		      fMonitor.worked((int)count);
		      return !(fMonitor.isCanceled());
		  }

		  public void worked(int work){
			  fMonitor.worked(work);
		  }

		  public void end(){
			  fMonitor.done();
		  }
	}


	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#setLastModified(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, long)
	 */
	public void setLastModified(String parent, String name,
			long timestamp, IProgressMonitor monitor) throws SystemMessageException
	{
		throw new SystemUnsupportedOperationException(Activator.PLUGIN_ID, "setLastModified"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.IFileService#setReadOnly(org.eclipse.core.runtime.IProgressMonitor, java.lang.String, java.lang.String, boolean)
	 */
	public void setReadOnly(String parent, String name,
			boolean readOnly, IProgressMonitor monitor) throws SystemMessageException {

		GlobusOnlineHostFile file = getFileInternal(parent,name, monitor);

		int userPermissions = file.getUserPermissions();
		int groupPermissions = file.getGroupPermissions();
		int otherPermissions = file.getOtherPermissions();

		int oldPermissions = userPermissions * 100 + groupPermissions * 10 + otherPermissions;
		if(readOnly) {
			userPermissions &= 5; // & 101b
			groupPermissions &= 5; // & 101b
			otherPermissions &= 5; // & 101b
		} else {
			userPermissions |= 2; // | 010b
		}
		int newPermissions = userPermissions * 100 + groupPermissions * 10 + otherPermissions;

		if (newPermissions == oldPermissions) {
			// do nothing
		} else if(_commandMutex.waitForLock(monitor, Long.MAX_VALUE)) {
			try {
				clearCache(parent);
//				GlobusOnlineClient globusOnlinec = getGlobusOnlineClient();
//				if (!globusOnlinec.sendSiteCommand("CHMOD " + newPermissions + " " + file.getAbsolutePath())) { //$NON-NLS-1$ //$NON-NLS-2$
//					String lastMessage = globusOnlinec.getReplyString();
//					throw new RemoteFileSecurityException(new Exception(lastMessage));
//				}
//			} catch (IOException e) {
//				String pluginId = Activator.getDefault().getBundle().getSymbolicName();
//				String messageText = e.getLocalizedMessage();
//				SystemMessage message = new SimpleSystemMessage(pluginId, IStatus.ERROR, messageText, e);
//				throw new SystemMessageException(message);
			} finally {
				_commandMutex.release();
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#getInputStream(java.lang.String, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public InputStream getInputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {

		if (monitor != null && monitor.isCanceled()){
			throw new SystemOperationCancelledException();
		}
		InputStream stream = null;

		try {
//			GlobusOnlineClient globusOnlineClient = cloneGlobusOnlineClient(isBinary);
//			chdir(globusOnlineClient, remoteParent);
			chdir(remoteParent);
//			stream = new GlobusOnlineBufferedInputStream(globusOnlineClient.retrieveFileStream(remoteFile), globusOnlineClient);
		}
		catch (Exception e) {
			throw makeSystemMessageException(e);
		}
		
		return stream;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#getOutputStream(java.lang.String, java.lang.String, boolean, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, boolean isBinary, IProgressMonitor monitor) throws SystemMessageException {
		int options = isBinary ? IFileService.NONE : IFileService.TEXT_MODE;
    	return getOutputStream(remoteParent, remoteFile, options, monitor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.rse.services.files.AbstractFileService#getOutputStream(java.lang.String, java.lang.String, boolean, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public OutputStream getOutputStream(String remoteParent, String remoteFile, int options, IProgressMonitor monitor) throws SystemMessageException {
		
    	remoteParent = checkEncoding(remoteParent);
    	remoteFile = checkEncoding(remoteFile);

		if (monitor != null && monitor.isCanceled()){
			throw new SystemOperationCancelledException();
		}

		OutputStream stream = null;

		try {
			boolean isBinary = (options & IFileService.TEXT_MODE) == 0 ? true : false;
//			GlobusOnlineClient ftpClient = cloneGlobusOnlineClient(isBinary);
			clearCache(remoteParent);
//			chdir(globusOnlineClient, remoteParent);
			chdir(remoteParent);
			if ((options & IFileService.APPEND) == 0){
//				stream = new GlobusOnlineBufferedOutputStream(globusOnlineClient.storeFileStream(remoteFile), globusOnlineClient);
			} else {
//				stream = new GlobusOnlineBufferedOutputStream(globusOnlineClient.appendFileStream(remoteFile), globusOnlineClient);
			}
		}
		catch (Exception e) {
			throw makeSystemMessageException(e);
		}

		return stream;
	}

	
	private void setDataConnectionMode()
	{
		if(_globusOnlinePropertySet != null)
		{
			if(_globusOnlinePropertySet.getPropertyValue("passive").equalsIgnoreCase("true") && !_isPassiveDataConnectionMode) //$NON-NLS-1$ //$NON-NLS-2$
			{
//				_globusOnlineClient.enterLocalPassiveMode();
				_isPassiveDataConnectionMode = true;
			}
			else if(_globusOnlinePropertySet.getPropertyValue("passive").equalsIgnoreCase("false") && _isPassiveDataConnectionMode) //$NON-NLS-1$ //$NON-NLS-2$
			{
//				_globusOnlineClient.enterLocalActiveMode();
				_isPassiveDataConnectionMode = false;
			}
		}
	}

	private void setFileType(boolean isBinaryFileType) throws IOException
	{
		if(!isBinaryFileType && _isBinaryFileType)
		{
//			_globusOnlineClient.setFileType(GLOBUS_ONLINE.ASCII_FILE_TYPE);
			_isBinaryFileType = isBinaryFileType;
		} else if(isBinaryFileType && !_isBinaryFileType)
		{
//			_globusOnlineClient.setFileType(GLOBUS_ONLINE.BINARY_FILE_TYPE);
			_isBinaryFileType = isBinaryFileType;
		}
	}


	/**
	 * Concatenate a parent directory with a file name to form a new proper path name.
	 * @param parentDir path name of the parent directory.
	 * @param fileName file name to concatenate.
	 * @return path name concatenated from parent directory and file name.
	 *
	 */
	protected String concat(String parentDir, String fileName) {
		StringBuffer path = new StringBuffer(parentDir);
		if (!parentDir.endsWith(String.valueOf(getSeparator())))
		{
			path.append(getSeparator());
		}
		path.append(fileName);
		return path.toString();
	}


	public IHostFilePermissions getFilePermissions(IHostFile file,
			IProgressMonitor monitor) throws SystemMessageException {
		if (file instanceof IHostFilePermissionsContainer)
		{
			return ((IHostFilePermissionsContainer)file).getPermissions();
		}
		return null;
	}

	public void setFilePermissions(IHostFile inFile,
			IHostFilePermissions permissions, IProgressMonitor monitor)
			throws SystemMessageException {

		//see also #setReadOnly()
		String s = Integer.toOctalString(permissions.getPermissionBits());
		if (_commandMutex.waitForLock(monitor, Long.MAX_VALUE)) {
			try {
				clearCache(inFile.getParentPath());
//				GlobusOnlineClient globusOnlinec = getGlobusOnlineClient();
//				if (!globusOnlinec.sendSiteCommand("CHMOD " + s + " " + inFile.getAbsolutePath())) { //$NON-NLS-1$ //$NON-NLS-2$
//					String lastMessage = globusOnlinec.getReplyString();
//					throw new RemoteFileSecurityException(new Exception(lastMessage));
//				}
//			} catch (IOException e) {
//				String pluginId = Activator.getDefault().getBundle().getSymbolicName();
//				String messageText = e.getLocalizedMessage();
//				SystemMessage message = new SimpleSystemMessage(pluginId, IStatus.ERROR, messageText, e);
//				throw new SystemMessageException(message);
			} finally {
				_commandMutex.release();
			}
		}

	}

	public int getCapabilities(IHostFile file) {
		return IFilePermissionsService.FS_CAN_GET_ALL | FS_CAN_SET_PERMISSIONS;
	}

}
