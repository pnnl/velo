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
package gov.pnnl.cat.util;

import org.alfresco.service.namespace.QName;

/**
 */
public interface TagTimerConstants {
	public static final String NAMESPACE_TAGTIMER = "http://www.pnl.gov/cat/model/tagtimer/1.0";
	public static final String JUSTIFICATION_REASON_FILENAME  = "Justification_Reasons.txt";
	public static final String TYPE_REFERNCE = "reference";
	public static final String TYPE_INTEL = "intel";
	  
	public static final QName ASPECT_US_PRESONS_DATA = QName.createQName(NAMESPACE_TAGTIMER, "usPersonsData");
	public static final QName ASPECT_US_PRESONS_DATA_REMOVED = QName.createQName(NAMESPACE_TAGTIMER, "usPersonsDataRemoved");

  public static final QName PROP_MATERIAL_TYPE = QName.createQName(NAMESPACE_TAGTIMER, "materialType");
  public static final QName PROP_EXPIRE_DATE = QName.createQName(NAMESPACE_TAGTIMER, "expireDate");
  public static final QName PROP_JUSTIFICATION_REASON = QName.createQName(NAMESPACE_TAGTIMER, "justificationReason");
  public static final QName PROP_JUSTIFICATION_DATE = QName.createQName(NAMESPACE_TAGTIMER, "justificationDate");
  public static final QName PROP_JUSTIFICATION_USER = QName.createQName(NAMESPACE_TAGTIMER, "justificationUser");
  public static final QName PROP_REMOVED_REASON = QName.createQName(NAMESPACE_TAGTIMER, "removedReason");
  public static final QName PROP_REMOVED_DATE = QName.createQName(NAMESPACE_TAGTIMER, "removedDate");
  public static final QName PROP_REMOVED_USER = QName.createQName(NAMESPACE_TAGTIMER, "removedUser");
  
  
// criminal intel fields, much like tag and timer so keeping them together...??
  public static final String NAMESPACE_CRIMINAL_INTEL = "http://www.pnl.gov/cat/model/criminal/1.0";
  public static final String INTEL_TYPES_FILENAME  = "Intel_Types_Options.xml";
  public static final String INTEL_SECURITY_LEVELS_FILENAME  = "Intel_Security_levels_Options.xml";
    
  public static final QName ASPECT_CRIMINAL_INTEL = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "criminalIntelData");
  public static final QName ASPECT_CRIMINAL_INTEL_REMOVED = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "criminalIntelDataRemoved");
  public static final QName ASPECT_VALIDITABLE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "validitable");
  
  public static final QName PROP_INTEL_MATERIAL_TYPE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "materialType");
  public static final QName PROP_INTEL_SECURITY_LEVEL = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "securityLevel");
  public static final QName PROP_INTEL_EXPIRE_DATE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "expireDate");
  public static final QName PROP_INTEL_JUSTIFICATION_COMMENT = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "justificationComment");
  public static final QName PROP_INTEL_JUSTIFICATION_DATE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "justificationDate");
  public static final QName PROP_INTEL_JUSTIFICATION_USER = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "justificationUser");
  public static final QName PROP_INTEL_REMOVED_REASON = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "removedReason");
  public static final QName PROP_INTEL_REMOVED_DATE = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "removedDate");
  public static final QName PROP_INTEL_REMOVED_USER = QName.createQName(NAMESPACE_CRIMINAL_INTEL, "removedUser");
}
