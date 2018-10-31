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
package org.kepler.ssh;

import gov.pnnl.velo.tif.service.TifServiceLocator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * The method of displaying user interactive prompts will be provided by the 
 * CredentialsPrompter class
 */
public class VeloUserInfo implements UserInfo, UIKeyboardInteractive {

	private static final Log log = LogFactory.getLog(VeloUserInfo.class.getName());
	public static final String TITLE = "Compute Server Authentication";

	/*set to true if pwd/passphrase/passcode was used okay*/
	protected boolean authSucc = false;
	
	 /* Support for Passphrase protected PUBLICKEY Authentication */
  protected String passphrase = null;

  /* Support for PASSWORD Authentication */
  protected String passwd = null;

  /* Support for KEYBOARD-INTERACTIVE Authentication */
  // Norbert Podhorszki pnorbert@cs.ucdavis.edu
  // Taken from example
  // http://www.jcraft.com/jsch/examples/UserAuthKI.java
  protected String passpki = null; // LIMITATION: only the very last password typed, not all!
  
	/**
	 * 
	 */
	public VeloUserInfo() {
		super();
	}
	
	/**
	 * 
	 */
	public void authWasSuccessful() {
		authSucc = true;
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	@Override
	public boolean promptYesNo(String str) {
		// This method gets called to answer the question similar to
		// "are you sure you want to connect to host whose key
		// is not in database ..."
		return true;
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UserInfo#getPassphrase()
	 */
	@Override
	public String getPassphrase() {
		return passphrase;
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
	 */
	@Override
	public boolean promptPassphrase(String message) {
		log.debug("VeloUserInfo:promptPassphrase");
		if (passphrase != null && authSucc) {
			return true;
		}

		String errMessage = null;
		if(passphrase != null) {
		  // this is a second pass because in the first pass the password failed
		  errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
		}
		String dlgMessage = "";
		String[] prompts = {message};
		boolean[] echo = {false};
		passphrase = TifServiceLocator.getCredentialsPrompter().promptForCredentials(TITLE, dlgMessage, errMessage, prompts, echo);
		
		if(passphrase != null) {
		  return true;
		} else {
		  return false;
		}

	}

	/**
	 * Method getPassword.
	 * @return String
	 * @see com.jcraft.jsch.UserInfo#getPassword()
	 */
	public String getPassword() {
		return passwd;
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
	 */
	@Override
	public boolean promptPassword(String message) {
		log.debug("VeloUserInfo:promptPassword");
		if (passwd != null && authSucc) {
			return true;
		}

    String errMessage = null;
    if(passwd != null) {
      // this is a second pass because in the first pass the password failed
      errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
    }
    String dlgMessage = "";
    String[] prompts = {message};
    boolean[] echo = {false};
    passwd = TifServiceLocator.getCredentialsPrompter().promptForCredentials(TITLE, dlgMessage, errMessage, prompts, echo);
    
    if(passwd != null) {
      return true;
    } else {
      return false;
    }
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
	 */
	@Override
	public void showMessage(String message) {
		// This method gets called when the server sends over a MOTD.
		// JOptionPane.showMessageDialog(null, message);
	  log.debug(message);
	}

	/**
	 * Return whatever credentials were provided by the keyboard interactive
	 * prompt.  This could be a password or a passcode
	 * Method getPassPKI.
	 * @return String
	 */
	public String getPassPKI() {
		return passpki;
	}

	/* (non-Javadoc)
	 * @see com.jcraft.jsch.UIKeyboardInteractive#promptKeyboardInteractive(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], boolean[])
	 */
	@Override
	public String[] promptKeyboardInteractive(String destination, String name,
	    String instruction, String[] prompts, boolean[] echo) throws Exception {

	  log.debug("VeloUserInfo:promptKeyboardInteractive");
	  String errMessage = null;
	  if(passpki != null) {
	    // this is a second pass because in the first pass the password failed
	    errMessage = "Authentication failed.  Invalid username or password.  Please try again.";
	  }
	  
	  String message = "Authenticating to: " + destination;
	  passpki = TifServiceLocator.getCredentialsPrompter().promptForCredentials(TITLE, message, errMessage, prompts, echo);

	  if(passpki == null) {
	    return null; // cancel
	  } else {
	    return new String[]{passpki};
	  }

	}

}
