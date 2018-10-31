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
 * Thrown if the subscription is missing required parameters or the parameters
 * are not of the right type or do not have the proper values, as defined
 * by the parameter constraints.
 *
 * @version $Revision: 1.0 $
 */
public class InvalidSubscriptionException extends Exception {

  public InvalidSubscriptionException() {
    this("Your subscription could not be validated against its SubscriptionType.");
  }

  /**
   * Constructor for InvalidSubscriptionException.
   * @param message String
   */
  public InvalidSubscriptionException(String message) {
    super(message);
  }
}
