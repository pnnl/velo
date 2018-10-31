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
package gov.pnnl.cat.criminalintel;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 */
public class CriminalIntelOptionsBean implements InitializingBean {
	private static final Log logger = LogFactory.getLog(CriminalIntelOptionsBean.class);
	
private Map<String, String> intelTypes;
private Map<String, String> securityLevels;
  
  /**
   * Method getIntelTypes.
   * @return Map<String,String>
   */
  public Map<String, String> getIntelTypes() {
    return intelTypes;
  }

  /**
   * Method setIntelTypes.
   * @param intelTypes Map<String,String>
   */
  public void setIntelTypes(Map<String, String> intelTypes){
    logger.info("setIntelTypes: " + intelTypes);
    this.intelTypes = intelTypes;
  }
  
  /**
   * Method getSecurityLevels.
   * @return Map<String,String>
   */
  public Map<String, String> getSecurityLevels() {
    return securityLevels;
  }

  /**
   * Method setSecurityLevels.
   * @param securityLevels Map<String,String>
   */
  public void setSecurityLevels(Map<String, String> securityLevels){
    logger.info("securityLevels: " + securityLevels);
    this.securityLevels = securityLevels;
  }

  /**
   * Method getAllowedIntelTypeValues.
   * @return Collection<String>
   */
  public Collection<String> getAllowedIntelTypeValues(){
    return this.intelTypes.keySet();
  }
  /**
   * Method getAllowedSecurityLevelValues.
   * @return Collection<String>
   */
  public Collection<String> getAllowedSecurityLevelValues(){
    return this.securityLevels.keySet();
  }

	/**
	 * Method afterPropertiesSet.
	 * @throws Exception
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		//after this bean is instantiated and properties have been set, give it to the contraint implementation
		//doing this after reading this article (very similar) http://blogs.alfresco.com/wp/jbarmash/category/ui-customization/
		MaterialTypeConstraint.criminalIntelOptionsBean = this;
		SecurityLevelConstraint.criminalIntelOptionsBean = this;
	}

}
