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
package gov.pnnl.cat.alerting.alerts;

import java.util.List;

/**
 * Class representing an individual (either a user or group, with a repository account or
 * without) who can interract with alerts (send, recieve, cause).  
 * We are abstracting this out so we can add to it later.  For now, we are assuming it will 
 * only be users with an email address and/or an repository account.  
 * Later we can add groups or external users, if needed.
 * @version $Revision: 1.0 $
 */
public interface Actor {
  
  /**
   * User account type.  Other possible options could be
   * "group".
   */
  public static final String ACCOUNT_USER = "user";
  
  /**
   * The identifier of the repository account.  For now, this will
   * return the username, since we aren't handling group accounts.
  
   * @return null if actor doesn't have an account */
  public String getAccountId();
  
  /**
   * For now, will always return ACCOUNT_USER, since we are not
   * handling group accounts.
  
   * @return null if actor doesn't have an account */
  public String getAccountType();
  
  /**
   * Returns the email address(es) of the actor.  We return
   * a list so in the future we could support more than one
   * email address.  However, for now, there will only be
   * one.
  
   * @return null if no email address exists */
  public List<String> getEmailAddresses();

}
