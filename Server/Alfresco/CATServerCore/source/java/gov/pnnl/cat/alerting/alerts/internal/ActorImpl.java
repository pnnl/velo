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
package gov.pnnl.cat.alerting.alerts.internal;

import gov.pnnl.cat.alerting.alerts.Actor;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ActorImpl implements Actor {
  private String username;
  private String accountType;
  private List<String> emailAddresses;

  /**
   * Constructor for ActorImpl.
   * @param username String
   */
  public ActorImpl() {
    //String username
//    this.username = username;
  }

  /**
   * Method getAccountId.
   * @return String
   * @see gov.pnnl.cat.alerting.alerts.Actor#getAccountId()
   */
  public String getAccountId() {
    return username;
  }

  /**
   * Method getAccountType.
   * @return String
   * @see gov.pnnl.cat.alerting.alerts.Actor#getAccountType()
   */
  public String getAccountType() {
    return accountType;
  }

  /**
   * Method getEmailAddresses.
   * @return List<String>
   * @see gov.pnnl.cat.alerting.alerts.Actor#getEmailAddresses()
   */
  public List<String> getEmailAddresses() {
    return emailAddresses;
  }
  
  /**
   * Method getSystemActor.
   * @return Actor
   */
  public static Actor getSystemActor() {
	  ActorImpl actor = new ActorImpl();
	  actor.setUsername("admin");
	  actor.accountType = Actor.ACCOUNT_USER;
	  actor.emailAddresses = new ArrayList<String>();
	  return actor;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
