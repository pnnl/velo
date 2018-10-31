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


import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to get list of children
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetCollection extends AbstractVeloWebScript {
  public static final String PARAM_WIKI_PATH = "wikiPath";  
  
  private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
  
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
    String wikiPath = req.getParameter(PARAM_WIKI_PATH);
    
    PrintStream printStream = null;
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    
    try {
      // write the results to the output stream
      printStream = new PrintStream(res.getOutputStream());
      getCollection(wikiPath, printStream);
    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
    return null;
  }

  /**
   * Method getCollection.
   * @param wikiPath String
   * @param printStream PrintStream
   * @throws Exception
   */
  public void getCollection(String wikiPath, PrintStream printStream) throws Exception {
 // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    
    // look up node (will throw exception if it doesn't exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    if(!nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      throw new RuntimeException(wikiPath + " is not a collection!");
    }
    
    List<FileInfo> children = fileFolderService.list(nodeRef);
    
    try {
      
      // Properties we are capturing for wiki
      // TODO: add hashmap of properties and write output in json format so we can
      // specify any set of properties to return
      String childPath;
      String size;
      String mimetype;
      String owner;
      String lastModified;
      String revision;
      
      for (FileInfo child : children) {  
          
        String linkType = "none";
        if(child.isLink()) {
          // Get the target of the link instead
          NodeRef target = (NodeRef)nodeService.getProperty(child.getNodeRef(), ContentModel.PROP_LINK_DESTINATION);
          child = fileFolderService.getFileInfo(target);
          linkType = "local";
        } 
        
        if(nodeService.hasAspect(child.getNodeRef(), CatConstants.ASPECT_REMOTE_LINK)) {
          linkType = "remote";
        }
        
        NodeRef childRef = child.getNodeRef();
        childPath = WikiUtils.getWikiPath(childRef, nodeService);
        mimetype = "";        
        boolean isCollection = false;
        if(child.isFolder()) {
          mimetype = (String)nodeService.getProperty(childRef, VeloServerConstants.PROP_MIMEYPE);
          isCollection = true;
          size = "0"; // folders have no size
          revision = "1.0";  // folders have no versions in Alfresco
          
        } else {
          mimetype = child.getContentData().getMimetype();
          size = String.valueOf(child.getContentData().getSize());
          
          // get current Alfresco version
          revision = (String) nodeService.getProperty(childRef, ContentModel.PROP_VERSION_LABEL);
        }
        
        // Owner
        owner = ownableService.getOwner(childRef);
        if(owner == null) {
          // If we have no owner, use the creator as the owner
          owner = (String)nodeService.getProperty(childRef, ContentModel.PROP_CREATOR);
        }
        
        // Last Modified Date
        Date nodeLastModified = (Date)nodeService.getProperty(childRef, ContentModel.PROP_MODIFIED);
        lastModified = dateFormat.format(nodeLastModified);
          
        printStream.println(childPath + "\t" + size + "\t" + mimetype + "\t" + owner + "\t" + lastModified + "\t" + revision + "\t" + isCollection + "\t" + linkType);
      }                                                                 
      
      printStream.flush();

    } catch (Throwable t) {
      logger.error("Could not print message to response stream.", t);
    }
        
  }

  
}
