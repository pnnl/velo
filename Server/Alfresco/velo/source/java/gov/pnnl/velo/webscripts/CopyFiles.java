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
import gov.pnnl.velo.policy.FileNamePolicy;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Copies files based on velo wiki paths
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CopyFiles extends AbstractVeloWebScript {  
  public static final String PARAM_OVERWRITE = "overwrite";  
  
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
    boolean overwrite = Boolean.valueOf(req.getParameter(PARAM_OVERWRITE));
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
      copyFiles(overwrite, reader);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }  
  
  /**
   * Method copyFiles.
   * @param overwrite boolean
   * @param reader BufferedReader
   * @throws Exception
   */
  public void copyFiles(boolean overwrite, BufferedReader reader) throws Exception{
    // Read the request body to get the files to copy.
    // Body is tab delimited file, one node to copy per line
    // sourceNodePath /t destFolderNodePath /t new name (optional)
    // Assumes source node and dest folder node already exist .
   
    String line;
    logger.debug("Trying to read request body");
    
    while ( (line = reader.readLine()) != null) {
      parseLine(line, overwrite);
    }
    
    logger.debug("Done reading request body");
  }
  
  /**
   * Method parseLine.
   * @param line String
   * @param overwrite boolean
   * @throws Exception
   */
  protected void parseLine(String line, boolean overwrite) throws Exception {
    String[] parts = line.split("\t");
    String sourceNodePath = parts[0];
    String destFolderNodePath = parts[1];
    String newName = null;
    if(parts.length > 2) {
      newName = parts[2];
    }

    logger.debug("Trying to copy " + sourceNodePath + " to " + destFolderNodePath);
    sourceNodePath = WikiUtils.getAlfrescoNamePath(sourceNodePath);
    destFolderNodePath = WikiUtils.getAlfrescoNamePath(destFolderNodePath);
    
    // Find the nodes from the path (will throw exception if node does not exist)
    NodeRef srcNode = WikiUtils.getNodeByName(sourceNodePath, nodeService);
    NodeRef destFolderNode = WikiUtils.getNodeByName(destFolderNodePath, nodeService);
    
    ChildAssociationRef childAssoc = nodeService.getPrimaryParent(srcNode);
    QName childAssocName = childAssoc.getQName();
    if(newName == null) {
      newName = (String)nodeService.getProperty(srcNode, ContentModel.PROP_NAME);
    
    }
    if(WikiUtils.isRenamableWikiNode(destFolderNode, nodeService, namespaceService)) {
      // we might have to fix the name because of wiki rename policy if we are copying
      // the node into a wiki-controlled folder
      newName = FileNamePolicy.getFixedName(newName);
    }
    childAssocName = QName.createQName(childAssocName.getNamespaceURI(), newName);
    
    // If you are trying to copy a file to itself (not sure why you would do that),
    // just return
    NodeRef existingChild = nodeService.getChildByName(destFolderNode, childAssoc.getTypeQName(), newName);
    QName srcNodeType = nodeService.getType(srcNode);
    if(existingChild != null && existingChild.equals(srcNode)) {
      return;
      
    }
  
    if(overwrite) {
      // if we are copying content, just copy the file content from src to dest
      if(srcNodeType.equals(ContentModel.TYPE_CONTENT)&& existingChild != null) {
        // Get the content reader for the src file
        ContentReader srcContentReader = contentService.getReader(srcNode, ContentModel.PROP_CONTENT);
        
        // Update the content property for the dest file
        if(srcContentReader != null) {  // make sure we don't have null content, which could happen in rare cases
          InputStream inputStream = srcContentReader.getContentInputStream();
          try {
            NodeUtils.updateFileContents(existingChild, inputStream, nodeService, contentService);
          } finally {
            IOUtils.closeQuietly(inputStream);
          }
        }
        
      } else {
        if(existingChild != null) {
          // we are copying non-content node, so we need to delete it first
          logger.debug("File of same name already exists.  Deleting existing file before copy.");
          nodeService.deleteNode(existingChild);
        }
        copyService.copyAndRename(srcNode, destFolderNode, childAssoc.getTypeQName(), childAssocName, true);        
      }

    } else {
      // just try a regular copy, which will fail if the dest node already exists
      copyService.copyAndRename(srcNode, destFolderNode, childAssoc.getTypeQName(), childAssocName, true);
    }
  }


}
