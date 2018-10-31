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
package gov.pnnl.cat.alerting.subscriptions.internal;

import gov.pnnl.cat.alerting.subscriptions.Frequency;
import gov.pnnl.cat.alerting.subscriptions.SubscriptionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * See SubscriptionServiceImpl.getSubscriptionTypes() for how these obejcts are created
 * @author d3g574
 *
 * @version $Revision: 1.0 $
 */
public class SubscriptionTypeImpl implements SubscriptionType {

	private List<Frequency> allowedFrequencies = new ArrayList<Frequency>();
	private QName name;
	private String mimetype;
	private Map<QName, PropertyDefinition> parameterDefinitions = new HashMap<QName, PropertyDefinition>();
	
	public SubscriptionTypeImpl() {
	  
	}
	
	/**
	 * Constructor for SubscriptionTypeImpl.
	 * @param qname QName
	 */
	public SubscriptionTypeImpl(QName qname) {
	  this.name = qname;
	}

	/**
	 * Method getMimetype.
	 * @return String
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionType#getMimetype()
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * Method setMimetype.
	 * @param mimetype String
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * Method setAllowedFrequencies.
	 * @param allowedFrequencies List<Frequency>
	 */
	public void setAllowedFrequencies(List<Frequency> allowedFrequencies) {
		this.allowedFrequencies = allowedFrequencies;
	}

	/**
	 * Method setName.
	 * @param name QName
	 */
	public void setName(QName name) {
		this.name = name;
	}

	/**
	 * Method setParameterDefinitions.
	 * @param parameterDefinitions Map<QName,PropertyDefinition>
	 */
	public void setParameterDefinitions(
			Map<QName, PropertyDefinition> parameterDefinitions) {
		this.parameterDefinitions = parameterDefinitions;
	}

	/**
	 * Method getAllowedFrequencies.
	 * @return List<Frequency>
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionType#getAllowedFrequencies()
	 */
	public List<Frequency> getAllowedFrequencies() {
		return allowedFrequencies;
	}

	/**
	 * Method getName.
	 * @return QName
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionType#getName()
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Method getParameterDefinitions.
	 * @return Map<QName,PropertyDefinition>
	 * @see gov.pnnl.cat.alerting.subscriptions.SubscriptionType#getParameterDefinitions()
	 */
	public Map<QName, PropertyDefinition> getParameterDefinitions() {
		return parameterDefinitions;
	}
	
	/**
	 * Method toString.
	 * @return String
	 */
	@Override
	public String toString() {
	  if(name == null) {
	    return "no qname";
	  } else {
	    return name.toString();
	  }
	}

  /**
   * Method hashCode.
   * @return int
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Method equals.
   * @param obj Object
   * @return boolean
   */
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof SubscriptionTypeImpl) {
      return toString().equals(obj.toString());
    }
    return false;
  }
	
	

}
