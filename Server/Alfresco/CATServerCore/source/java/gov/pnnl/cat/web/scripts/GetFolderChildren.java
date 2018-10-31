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
package gov.pnnl.cat.web.scripts;

import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

import gov.pnnl.cat.util.CatConstants;

/**
 * This web script is meant to feed the left size of a scalable file browser.  Since
 * typically you want to see all subfolders, but you want to be able to page file contents if the folders are large.
 * Hopefully there won't be too many subfolders.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class GetFolderChildren extends GetChildren {
  
  @Override
  protected List<ChildAssociationRef> getChildAssociations(NodeRef nodeRef) {

    // only get the children of type folder
    return nodeService.getChildAssocs(nodeRef, CatConstants.FOLDER_TYPES);
  }


}
