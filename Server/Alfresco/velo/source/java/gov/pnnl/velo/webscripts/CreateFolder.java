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


import gov.pnnl.velo.policy.FileNamePolicy;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to create a new folder.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateFolder extends AbstractVeloWebScript {
  public static final String PARAM_NAME = "name";  
  public static final String PARAM_PARENT_PATH = "parentPath";  
  public static final String PARAM_MIMETYPE = "mimetype";  
  
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
    String mimetype = req.getParameter(PARAM_MIMETYPE);
    
    createFolder(name, parentPath, mimetype);
    return null;
  }

  /**
   * Method createFolder.
   * @param name String
   * @param parentPath String
   * @param mimetype String
   * @throws Exception
   */
  public void createFolder(String name, String parentPath, String mimetype) throws Exception {
 // Convert the parent path to alfresco format
    if(!parentPath.endsWith("/"))  {
      parentPath = parentPath + "/";
    }
    parentPath = WikiUtils.getAlfrescoNamePath(parentPath);
    logger.debug("parentPath = " + parentPath);
    
    // Find the parent node from the path (will throw exception if node does not exist)
    NodeRef parent = WikiUtils.getNodeByName(parentPath, nodeService);
    
    // first check if the folder already exists - it if does, just return
    if(WikiUtils.isRenamableWikiNode(parent, nodeService, namespaceService)) {
      // we might have to fix the name because of wiki rename policy if we are creating
      // the node into a wiki-controlled folder
      name = FileNamePolicy.getFixedName(name);
    }
    NodeRef existingChild = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
    
    if(existingChild == null) {

      // Create the node properties
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
      properties.put(ContentModel.PROP_NAME, name);
      properties.put(VeloServerConstants.PROP_MIMEYPE, mimetype);

      // Create the folder
      QName folderQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
      nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, folderQName, ContentModel.TYPE_FOLDER, properties);
    }    
  }

  
}
