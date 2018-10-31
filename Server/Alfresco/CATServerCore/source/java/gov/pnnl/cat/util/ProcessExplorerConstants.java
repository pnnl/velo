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
 * Constants class used by Process Explorer CAT Plugin
 *
 * @version $Revision: 1.0 $
 */
public interface ProcessExplorerConstants {

  /** Namespace constants */
  public static final String NAMESPACE_PE = "http://www.pnl.gov/pe/model/content/1.0";
  
  /** Prefix constants */
  static final String PE_MODEL_PREFIX = "pe";
  
  /** Types */
  
  /** Aspects */
  public static final QName ASPECT_PROCESS_DIAGRAM_ROOT = QName.createQName(NAMESPACE_PE, "processDiagramRoot");
 
  
  /** Properties */
  public static final QName PROP_TAXONOMY_REF = QName.createQName(NAMESPACE_PE, "taxonomyRef");  
  
  /** Name Constants */

  /** XPath Constants */

  /** Asoociation Constants */

}
