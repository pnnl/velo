/*******************************************************************************
 * Copyright (c) 2006, 2013 IBM Corporation and others.
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
 * Javier Montalvo Orus (Symbian) - Bug 140348 - FTP did not use port number
 * Javier Montalvo Orus (Symbian) - Bug 161209 - Need a Log of ftp commands
 * Javier Montalvo Orus (Symbian) - Bug 169680 - [ftp] FTP files subsystem and service should use passive mode
 * David Dykstal (IBM) - 168977: refactoring IConnectorService and ServerLauncher hierarchies
 * Martin Oberhuber (Wind River) - [cleanup] move FTPSubsystemResources out of core
 * Javier Montalvo Orus (Symbian) - Fixing 176216 - [api] FTP sould provide API to allow clients register their own FTPListingParser
 * Javier Montalvo Orus (Symbian) - [187531] Improve exception thrown when Login Failed on FTP
 * David Dykstal (IBM) - added RESID_FTP_SETTINGS_LABEL
 * David McKnight (IBM) - [196632] [ftp] Passive mode setting does not work
 * Martin Oberhuber (Wind River) - [204669] Fix ftp path concatenation on systems using backslash separator
 * Martin Oberhuber (Wind River) - [203500] Support encodings for FTP paths
 * Martin Oberhuber (Wind River) - [235463][ftp][dstore] Incorrect case sensitivity reported on windows-remote
 * Martin Oberhuber (Wind River) - [269442][ftp] Set passive mode by default
 *******************************************************************************/

package org.eclipse.rse.internal.subsystems.files.globusonline.connectorservice;



import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.model.ILabeledObject;
import org.eclipse.rse.core.model.IPropertySet;
import org.eclipse.rse.core.model.PropertyType;
import org.eclipse.rse.core.model.SystemSignonInformation;
import org.eclipse.rse.internal.services.files.globusonline.GlobusOnlineService;
import org.eclipse.rse.internal.subsystems.files.globusonline.GlobusOnlineSubsystemResources;
import org.eclipse.rse.internal.subsystems.files.globusonline.parser.GlobusOnlineClientConfigFactory;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.eclipse.rse.services.files.RemoteFileException;
import org.eclipse.rse.ui.subsystems.StandardConnectorService;
import gov.pnnl.gotransfer.service.GlobusOnlineTransferer;
import org.eclipse.rse.globusonline.IGlobusOnlineConnectorService;
import org.globusonline.transfer.JSONTransferAPIClient;


public class GlobusOnlineConnectorService extends StandardConnectorService implements IGlobusOnlineConnectorService
{
	protected GlobusOnlineService _globusOnlineService;
	private GlobusOnlineTransferer transferer = new GlobusOnlineTransferer();
	private JSONTransferAPIClient globusOnlineClient;
	boolean connected = false;
	String endpointId;
	String myProxyHost;

	/** Indicates the default string encoding on this platform */
	private static String _defaultEncoding = new java.io.InputStreamReader(new java.io.ByteArrayInputStream(new byte[0])).getEncoding();

	public GlobusOnlineConnectorService(IHost host, int port)
	{
		super(GlobusOnlineSubsystemResources.RESID_GLOBUS_ONLINE_CONNECTORSERVICE_NAME,GlobusOnlineSubsystemResources.RESID_GLOBUS_ONLINE_CONNECTORSERVICE_DESCRIPTION, host, port);
		_globusOnlineService = new GlobusOnlineService();
		if (getHost().getSystemType().isWindows()) {
			// Configured against a Windows-specific system type
			_globusOnlineService.setIsCaseSensitive(false);
		}
		
		// We can't initialize local variables from the cache here because the cache hasn't been loaded yet
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#preConnect()
	 */
	@Override
  protected void preConnect() {
   // see if we can load our properties here
	  IPropertySet propertySet =  getPropertySet();
	  endpointId = getPropertySet().getPropertyValue(IGlobusOnlineConnectorService.PARAMETER_GLOBUS_ENDPOINT_ID);
	  myProxyHost = getPropertySet().getPropertyValue(IGlobusOnlineConnectorService.PARAMTER_GLOBUS_ENDPOINT_MY_PROXY_HOST);
	  System.out.println(myProxyHost);
  }



  @Override
	public IPropertySet setGlobusOnlineProperties(String endpointId, String myProxyHost, String globusAccessiblePath) {
	  this.endpointId = endpointId;
	  this.myProxyHost = myProxyHost;
	  
	  IPropertySet propertySet = getPropertySet();

	  propertySet.addProperty(IGlobusOnlineConnectorService.PARAMETER_GLOBUS_ENDPOINT_ID, endpointId, PropertyType.getStringPropertyType());
	  propertySet.addProperty(IGlobusOnlineConnectorService.PARAMTER_GLOBUS_ENDPOINT_MY_PROXY_HOST, myProxyHost, PropertyType.getIntegerPropertyType());
	  propertySet.addProperty(IGlobusOnlineConnectorService.PARAMETER_GLOBUS_ACCESSIBLE_PATH, globusAccessiblePath, PropertyType.getStringPropertyType());	      

	  if (propertySet instanceof ILabeledObject) {
	    String label = IGlobusOnlineConnectorService.LABEL_GLOBUS_ONLINE_PROPERTIES;
	    ((ILabeledObject)propertySet).setLabel(label);
	  }
	  return propertySet;
	}


  private IPropertySet getPropertySet() {
   
    IPropertySet propertySet = getPropertySet(IGlobusOnlineConnectorService.PROPERTY_SET_GLOBUS); //$NON-NLS-1$

    if(propertySet==null) {
      //Active - passive mode
      propertySet = createPropertySet(IGlobusOnlineConnectorService.PROPERTY_SET_GLOBUS); //$NON-NLS-1$
    }
    return propertySet;
	}
	
  public void connectToGlobusEndpoint() throws OperationCanceledException {
    boolean success = false;

    // first connect to globus online server

    try {
      transferer.authenticateToGlobusOnline("clansing", "Koolcat1");

    } catch (Exception e) {
      throw new RuntimeException("Unable to authenticate to Globus Online", e);
    }

    try {
      globusOnlineClient = transferer.autoActivateEndpoint(endpointId);
      success = globusOnlineClient != null;
      System.out.println("connectToGlobusEndpoint :: autoActivateEndpoint success = " + success);
      
    } catch (Exception e) {
      e.printStackTrace();
    }

   while(!success) {
      SystemSignonInformation info = getSignonInformation();
      
      try {
        globusOnlineClient = transferer.activateEndpoint(
            endpointId, myProxyHost, info.getUserId(), info.getPassword());
        success = (globusOnlineClient != null);
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      if(! success) {
        acquireCredentials(true);
      }
    } 
  }

	protected void internalConnect(IProgressMonitor monitor)  throws RemoteFileException, IOException
	{
	  
		connectToGlobusEndpoint();
		
		IPropertySet propertySet = getPropertySet();
		_globusOnlineService.setGlobusOnlineClient(globusOnlineClient);
		_globusOnlineService.setPropertySet(propertySet);
		_globusOnlineService.setGlobusEndpoint(endpointId);
		_globusOnlineService.setGlobusOnlineClientConfigFactory(GlobusOnlineClientConfigFactory.getParserFactory());
		//TODO this code should be in IHost
		String encoding = getHost().getDefaultEncoding(false);
		if (encoding==null) encoding = getHost().getDefaultEncoding(true);
		//TODO Here, we set the FTP default encoding same as the local encoding.
		//Another alternative would be to set ISO-8859-1, which is the
		//default-default internal to FTP, or keep it "null".
		if (encoding==null) encoding = _defaultEncoding;
		//</code to be in IHost>
		_globusOnlineService.setControlEncoding(encoding);
		_globusOnlineService.setUserHome("/");
		connected = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#getHomeDirectory()
	 */
	public String getHomeDirectory() {
		if (_globusOnlineService!=null) {
			IHostFile f = _globusOnlineService.getUserHome();
			if (f!=null) {
				return f.getAbsolutePath();
			}
		}
		//fallback while not yet connected
		return super.getHomeDirectory();
	}
/*
	private OutputStream getLoggingStream(String hostName,int portNumber)
	{
		MessageConsole messageConsole=null;

		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			if(consoles[i].getName().equals("Globus Online log: "+hostName+":"+portNumber)) { //$NON-NLS-1$ //$NON-NLS-2$
				messageConsole = (MessageConsole)consoles[i];
				break;
			}
		}

		if(messageConsole==null){
			messageConsole = new MessageConsole("Globus Online log: "+hostName+":"+portNumber, null); //$NON-NLS-1$ //$NON-NLS-2$
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ messageConsole });
		}

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(messageConsole);

		return messageConsole.newOutputStream();
	}
*/
	public IFileService getFileService()
	{
		return _globusOnlineService;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.core.subsystems.AbstractConnectorService#internalDisconnect(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void internalDisconnect(IProgressMonitor monitor)
	{
	  _globusOnlineService.uninitService(monitor);
	  connected = false;	  
	}

	public boolean isConnected()
	{
		return (_globusOnlineService != null && connected);
	}

}
