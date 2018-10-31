/********************************************************************************
 * Copyright (c) 2002, 2013 IBM Corporation and others. All rights reserved.
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
 * David Dykstal (IBM) - moved from core package in the UI plugin
 *                     - updated to use new RSEPreferencesManager
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - [177523] Unify singleton getter methods
 * Martin Oberhuber (Wind River) - [186640] Add IRSESystemType.testProperty()
 * Martin Oberhuber (Wind River) - [218655][api] Provide SystemType enablement info in non-UI
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * David Dykstal (IBM) - [210474] Deny save password function missing
 * David Dykstal (IBM) - [225320] Use equinox secure storage for passwords
 * David Dykstal (IBM) - [379787] Fix secure storage usage in org.eclipse.rse.tests
 ********************************************************************************/

package org.eclipse.rse.internal.services.sshglobus;

import java.io.IOException;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Stores passwords for globus online server and endpoints in Eclipse's secure
 * preferences store. 
 */
public class GlobusOnlinePasswordPersistenceManager {

  /*
   * Fields used to cache authentication information in the keyring
   */
	public static final String KEY_USER_NAME = "USERNAME";
	public static final String KEY_PASSWORD = "PASSWORD";

	/*
	 * Singleton instance
	 */
	private static GlobusOnlinePasswordPersistenceManager instance;

	/**
	 * Retrieve the singleton instance of the PasswordPersistenceManger
	 */
	public static final synchronized GlobusOnlinePasswordPersistenceManager getInstance() {
		if (instance == null) {
			instance = new GlobusOnlinePasswordPersistenceManager();
		}
		return instance;
	}
	
	private static String getPasswordKey(String endpointId) {
	  return endpointId + "$$" + KEY_PASSWORD;
	}
	
	private static String getUsernameKey(String endpointId) {
	  return endpointId + "$$" + KEY_USER_NAME;
	}

	private static String getEndpointIDFromKey(String passwordKey) {
		int sepIndex = passwordKey.indexOf("$$");
		return passwordKey.substring(0, sepIndex);
	}

	String userName = System.getProperty("user.name"); //$NON-NLS-1$

	private static ISecurePreferences getNode(String endpointId) {
	  ISecurePreferences endpointNode = null;
	  ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
	  ISecurePreferences rseNode = preferences.node("org.eclipse.rse.core.security");
	  endpointNode = rseNode.node(endpointId);
		return endpointNode;
	}
	
	public static Credentials getCredentials(String endpointId){
	  // first look the username/password up from the secure store
	  Credentials credentials = getCredentialsFromSecureStore(endpointId);
	  
	  // if username or password are missing, then prompt
	  if(credentials.username == null || credentials.password == null) {
	    credentials = promptForCredentials(endpointId);
	  }
	  
	  saveCredentials(endpointId, credentials);
	  return credentials;
	}
	
	private static Shell getShell() {
	    Shell shell = null;
	    IWorkbench workbench = PlatformUI.getWorkbench();
	    if (workbench != null) {
	      IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	      if (window != null) {
	        shell = window.getShell();
	      }
	    }
	    return shell;
	  }

	
  private static Credentials promptForCredentials(String endpointId) throws OperationCanceledException {
    Shell shell = getShell();
    if (shell == null){
      shell = new Shell(); // need this for the case of being prompted during workbench shutdown
    }
    
    final Credentials[] credentials = new Credentials[1];
    Display.getDefault().syncExec(new Runnable() {
      
      @Override
      public void run() {
        // TODO: call the correct UI dialog
//        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
//        
//        SwtPasswordDialog dialog = new SwtPasswordDialog(shell, "Compute Server Authentication", message, errMessage,
//            prompts, echo, null, new NotEmptyStringValidator());
//        if(dialog.open() == Dialog.OK) {
//          
//          credentials[0] = dialog.getValue();
//        }
      }
    });
    return credentials[0];   
  }
  
  public static void saveCredentials(String endpointId, Credentials credentials) {
    ISecurePreferences node= getNode(endpointId);
    if (node == null)
      return;
    
    try {
      if (credentials.username != null)
        node.put(KEY_USER_NAME, credentials.username, true /* store encrypted */);
      else
        node.remove(KEY_USER_NAME);

      if (credentials.password != null)
        node.put(KEY_PASSWORD, credentials.password, true /* store encrypted */);
      else
        node.remove(KEY_PASSWORD);
      
    } catch (StorageException e) {
      e.printStackTrace(); // TODO: add log4j
      return;
    }

    // optional: save it right away in case something crashes later
    try {
      node.flush();
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

  }
  
  public static class Credentials {
    String username;
    String password;
  }
  
  public static Credentials getCredentialsFromSecureStore(String endpointId) {
    Credentials credentials = null;
    ISecurePreferences node = getNode(endpointId);
    if (node != null) {
      try {
        credentials.username = node.get(KEY_USER_NAME, null);
        credentials.password = node.get(KEY_PASSWORD, null);

      } catch (StorageException e) {
        e.printStackTrace(); // TODO: add log4j
      }
    }
    return credentials;
  }


}