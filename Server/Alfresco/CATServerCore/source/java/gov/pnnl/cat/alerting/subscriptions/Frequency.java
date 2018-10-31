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

import java.io.Serializable;


/**
 * How often alerts should be sent.  Only the constants defined in this class are valid choices.
 * @version $Revision: 1.0 $
 */
public class Frequency implements Serializable {

  //Don't use immediate - too many chances for spam 
  //public static Frequency IMMEDIATE = new Frequency("Immediate");
  /**
   * Send alerts every hour.
   */
  public static Frequency HOURLY = new Frequency("Hourly");
  
  /**
   * Send alerts once per day.
   */
  public static Frequency DAILY = new Frequency("Daily");
  
  /**
   * Send alerts once per week.
   */
  public static Frequency WEEKLY = new Frequency("Weekly");
 
  private String interval;
  
  /**
   * Constructor for Frequency.
   * @param interval String
   */
  public Frequency (String interval) {
    this.interval = interval;
  }
  
  /**
   * Method toString.
   * @return String
   */
  public String toString() {
	  return interval;
  }
  
  /**
   * Method equals.
   * @param o Object
   * @return boolean
   */
  public boolean equals(Object o) {
	  if (o == null) {
		  return false;
	  }
	  if ((o instanceof Frequency) == false) {
		  return false;
	  }
	  Frequency fo = (Frequency)o;
	  return (fo.interval.equals(interval));
  }
   
}
