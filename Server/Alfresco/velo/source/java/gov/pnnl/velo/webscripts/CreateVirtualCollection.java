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
package gov.pnnl.velo.webscripts;


import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to create a new taxonomy (i.e., virtual folder).
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateVirtualCollection extends AbstractVeloWebScript {
  public static final String PARAM_NAME = "name";  
  public static final String PARAM_PARENT_PATH = "parentPath";  
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String name = req.getParameter(PARAM_NAME);
    String parentPath = req.getParameter(PARAM_PARENT_PATH);
    
    FileReader fileReader = new FileReader(requestContent);
    BufferedReader reader = null;
    try{
      reader = new BufferedReader(fileReader);
      createVirtualCollection(name, parentPath, reader);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }

  /**
   * Method createVirtualCollection.
   * @param name String
   * @param parentPath String
   * @param reader BufferedReader
   * @throws Exception
   */
  public void createVirtualCollection(String name, String parentPath, BufferedReader reader ) throws Exception {
 // Convert the parent path to alfresco format
    if(!parentPath.endsWith("/"))  {
      parentPath = parentPath + "/";
    }
    parentPath = WikiUtils.getAlfrescoNamePath(parentPath);
    logger.debug("parentPath = " + parentPath);
    
    // Find the parent node from the path (will throw exception if node does not exist)
    NodeRef parent = WikiUtils.getNodeByName(parentPath, nodeService);
    
    // Create the virtual collection as a taxonomy
    NodeRef virtualCollection = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
    // If the taxonomy doesn't exist, create it
    if(virtualCollection == null) {
      virtualCollection = NodeUtils.createTaxonomy(parent, name, nodeService);
      nodeService.setProperty(virtualCollection, VeloServerConstants.PROP_MIMEYPE, VeloConstants.MIMETYPE_VIRTUAL_COLLECTION);
    } else {
      // If the taxonomy already exists, then wipe all the children
      List<ChildAssociationRef> children = nodeService.getChildAssocs(virtualCollection);
      for(ChildAssociationRef child : children) {
        nodeService.deleteNode(child.getChildRef());
      }
    }

    // Read the request body and see if we need to create any links
      String line;
      logger.debug("Trying to read request body");
      while ( (line = reader.readLine()) != null) {
        String targetPath = WikiUtils.getAlfrescoNamePath(line);
        logger.debug("Line: " + targetPath);
        NodeRef target = WikiUtils.getNodeByName(targetPath, nodeService);
        NodeUtils.createLinkedFile(target, virtualCollection, nodeService);
      }
      logger.debug("Done reading request body");

    
        
  }

}
