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
/**
 * 
 */
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class RdfExtractionProcessor extends AbstractFileProcessor {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "RDF Extraction";
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {
    NodeRef nodeRef = fileInfo.getNodeToExtract();
    
    Action action = actionService.createAction("calaisAction");
    action.setParameterValue("calaisKey", "m493y76xtd5mrpdazspwauvt");
    action.setParameterValue("saveRDF", Boolean.TRUE);
    action.setParameterValue("saveJSON", Boolean.TRUE);
    action.setParameterValue("autoTag", Boolean.TRUE);
    actionService.executeAction(action, nodeRef, false, false);
  }

}
