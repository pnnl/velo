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
package gov.pnnl.cat.ui.rcp.handlers;


import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;


/**
 */
public class CutHandler extends CopyHandler {
  private static Logger logger = CatLogger.getLogger(CutHandler.class);
 
//  public void init(){
//    setText("Cu&t");
//    setToolTipText("Cut");
//    setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_CUT, SharedImages.CAT_IMG_SIZE_16));
//    setActionDefinitionId(CatActionIDs.CUT_COMMAND);
//  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CopyHandler#deleteSource()
   */
  public boolean deleteSource(){
    return true;
  }
  
}
