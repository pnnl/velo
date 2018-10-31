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
import org.alfresco.service.namespace.QNamePattern;

/**
 * Finds an exact match on a node name, disregarding the namespace
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class NodeNameQNamePattern implements QNamePattern {

  private String name;
  
  /**
   * Constructor for NodeNameQNamePattern.
   * @param name String
   */
  public NodeNameQNamePattern(String name) {
    this.name = name;
  }
  
  /**
   * Method isMatch.
   * @param qname QName
   * @return boolean
   * @see org.alfresco.service.namespace.QNamePattern#isMatch(QName)
   */
  @Override
  public boolean isMatch(QName qname) {
    return qname.getLocalName().equalsIgnoreCase(name);
  }

}
