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
package gov.pnnl.cat.alerts.model;

/**
 */
public class SearchSubscription extends AbstractSubscription {
  /**
   */
  public enum Trigger {
    NEW,
    MODIFIED,
    DELETED
  }

  private String query;
  private Trigger[] triggers;

  public SearchSubscription() {
  }

  /**
  
   * @return the query */
  public String getQuery() {
    return query;
  }
  /**
   * @param query the query to set
   */
  public void setQuery(String query) {
    this.query = query;
  }
  /**
  
   * @return the trigger */
  public Trigger[] getTriggers() {
    return triggers;
  }
  /**
  
   * @param triggers Trigger[]
   */
  public void setTriggers(Trigger... triggers) {
    this.triggers = triggers;
  }

  /**
   * Method getType.
   * @return Type
   * @see gov.pnnl.cat.alerts.model.ISubscription#getType()
   */
  @Override
  public Type getType() {
    return Type.SEARCH;
  }
}
