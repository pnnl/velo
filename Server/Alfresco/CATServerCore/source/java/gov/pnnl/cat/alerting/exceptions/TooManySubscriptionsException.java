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
package gov.pnnl.cat.alerting.exceptions;

/**
 * Thrown if a user tries to create a new subscription, and they have already
 * exceeded the maximum limit.
 *
 * @version $Revision: 1.0 $
 */
public class TooManySubscriptionsException extends Exception {

  public TooManySubscriptionsException() {
    this("You have exceeded the maximum number of subscriptions.");
  }

  /**
   * Constructor for TooManySubscriptionsException.
   * @param message String
   */
  public TooManySubscriptionsException(String message) {
    super(message);
  }
}
