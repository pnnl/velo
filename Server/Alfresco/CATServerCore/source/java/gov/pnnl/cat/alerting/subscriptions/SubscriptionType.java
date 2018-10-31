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

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Contains information describing the subscription parameters.
 *
 * @version $Revision: 1.0 $
 */
public interface SubscriptionType {
 
  /**
   * The qualified name of the subscription type
  
   * @return QName
   */
  public QName getName();
  
  /**
   * The parameter definitions.  Parameters can be used to
   * filter/tune event handling.  Using the type metatmodel
   * framework provided by Alfresco.  Any params with the same namespace.  uniaue things for this subscription type
  
   * @return Map<QName,PropertyDefinition>
   */
  public Map<QName, PropertyDefinition> getParameterDefinitions();
  
  /**
   * Since not all subscriptions may support all frequencies, need
   * to specify which frequencies can be used for this subscription.
   * This is a shortcut method - you could also look this up from 
   * the parameter definitions.
  
   * @return List<Frequency>
   */
  public List<Frequency> getAllowedFrequencies();
    
  /**
   * All subscription types must have a mime type for use wwith message formatting
   * @return String
   */
  public String getMimetype();
}
