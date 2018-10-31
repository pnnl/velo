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
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Lets you create multiple remote links at once by reading the posted
 * file which details which links to create, one link per line.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class AdvancedCreateRemoteLink extends AbstractVeloWebScript {
  
  
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
    // Read the request body to get the links to create
    // Body is a file of link params, one remote link per line
    boolean useAlfrescoPaths = false;
    String param = req.getParameter("useAlfrescoPaths");
    if(param != null) {
      useAlfrescoPaths = Boolean.valueOf(param);
    }
    
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
    
      advCreateRemoteLink(reader, useAlfrescoPaths);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }
  
  /**
   * Method advCreateRemoteLink.
   * @param reader BufferedReader
   * @throws Exception
   */
  public void advCreateRemoteLink(BufferedReader reader, boolean useAlfrescoPaths) throws Exception{
    
    String line;
    logger.debug("Trying to read request body");
    
    while ( (line = reader.readLine()) != null) {
      String[] parts = line.split("\t");
      String parentPath = parts[0];
      String linkName = parts[1];
      String linkUrl = parts[2];
      String linkTitle = parts[3];
      String linkDescription = parts[4];
      String linkType = parts[5];
      boolean ignoreWiki = Boolean.valueOf(parts[6]);
      
      // Get the parent node
      if(!parentPath.endsWith("/"))  {
        parentPath = parentPath + "/";
      }
      if(!useAlfrescoPaths) {
        parentPath = WikiUtils.getAlfrescoNamePath(parentPath);
      }
      logger.debug("parentPath = " + parentPath);
      
      // (will throw exception if node does not exist)
      NodeRef parent = WikiUtils.getNodeByName(parentPath, nodeService);

      createRemoteLink(parent, linkName, linkUrl, linkTitle, linkDescription, linkType, ignoreWiki);
    }
    
    logger.debug("Done reading request body");
    
  }
  
  /**
   * Method createRemoteLink.
   * @param parent NodeRef
   * @param linkName String
   * @param linkUrl String
   * @param linkTitle String
   * @param linkDescription String
   * @param linkType String
   * @param ignoreWiki boolean
   */
  private void createRemoteLink(NodeRef parent, String linkName, String linkUrl, String linkTitle, String linkDescription, String linkType,
      boolean ignoreWiki) {
    
    QName linkTypeQname = ContentModel.TYPE_CONTENT;
    if(linkType != null){
      if(linkType.equalsIgnoreCase("folder")){
        linkTypeQname = ContentModel.TYPE_FOLDER;
      }
    }
    
    if(linkTitle != null && linkTitle.equals("null")) {
      linkTitle = null;
    }
    
    if(linkDescription != null && linkDescription.equals("null")) {
      linkDescription = null;
    }
    
    // Add the link
    NodeRef remoteLink = NodeUtils.createRemoteLink(parent, linkName, linkUrl, linkTitle, linkDescription, linkTypeQname, nodeService, contentService);
    String wikiPath = WikiUtils.getWikiPath(remoteLink, nodeService);
    if(ignoreWiki) {
      nodeService.addAspect(remoteLink, VeloServerConstants.ASPECT_WIKI_IGNORE, null);
    }
    
  }

}
