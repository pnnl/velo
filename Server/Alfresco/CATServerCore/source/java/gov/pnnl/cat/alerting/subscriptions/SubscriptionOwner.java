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
package gov.pnnl.cat.alerting.subscriptions;

/**
 * We are abstracting the owner so we can change its
 * attributes in the future (i.e., add groups too).  This is similar to the abstraction
 * for Actor.
 *
 * @version $Revision: 1.0 $
 */
public interface SubscriptionOwner {
  
  /**
   * User account type.  Other possible options could be
   * "group".
   */
  public static final String ACCOUNT_USER = "user";
  
  /**
   * The identifier of the repository account.  For now, this will
   * return the username, since we aren't handling group accounts.
  
   * @return null if owner doesn't have an account */
  public String getAccountId();
  
  /**
   * For now, will always return ACCOUNT_USER, since we are not
   * handling group accounts.
  
   * @return null if owner doesn't have an account */
  public String getAccountType();
}
