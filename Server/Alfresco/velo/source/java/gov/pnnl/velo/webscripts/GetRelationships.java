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


import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Called by VELO wiki to get list of all nodes connected to the given node
 * via any relationships other than "contains"
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetRelationships extends AbstractVeloWebScript {
  public static final String PARAM_WIKI_PATH = "wikiPath";  
  
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
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    // write the results to the output stream
    PrintStream printStream = null;
    try{
      printStream = new PrintStream(res.getOutputStream());
      getRelationships(wikiPath, printStream);
    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
    return null;
  }

  /**
   * Method getRelationships.
   * @param wikiPath String
   * @param printStream PrintStream
   * @throws Exception
   */
  public void getRelationships(String wikiPath, PrintStream printStream) throws Exception{
 // Convert the path to alfresco format
    String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    
    // look up node (will throw exception if it doesn't exist)
    NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    
    // Associations are directional
    // Get all associations where I am the source (i.e, me->node)
    List<AssociationRef> targetAssocs =  nodeService.getTargetAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
    
    // Get all associations where I am the target (i.e., node->me)
    List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(nodeRef, RegexQNamePattern.MATCH_ALL);
    
    try {
      // Write one assoc per line in a tab-delimited text file
      // first do the relationships where i am the source
      for (AssociationRef assoc : targetAssocs) {
        NodeRef target = assoc.getTargetRef();
        
        // We can't catch access denied exceptions on the public node service because the transaction
        // interceptor will automatically roll back the tx if an exception is thrown
        AccessStatus access = permissionService.hasPermission(target, PermissionService.READ);
        if(access.equals(AccessStatus.DENIED)) {
          // ignore these - we just won't show a relationship if the user doesn't have access
          logger.error("Acess denied to association target: " + target + "  Ingoring.");
        } else {
          String targetPath = WikiUtils.getWikiPath(target, nodeService);
          // for some reason, we had relationships pointing to nodes in the archive, so they have
          // no wiki path - not sure how that happened
          if(targetPath != null) {
            String relationship = assoc.getTypeQName().toString();
            //String mimetype = (String)nodeService.getProperty(target, VeloConstants.PROP_MIMEYPE);

            printStream.println(wikiPath + "\t" + relationship + "\t" + targetPath);
          }
        }
      }
      
      // now do the relationships where i am the target
      for (AssociationRef assoc : sourceAssocs) {
        NodeRef source = assoc.getSourceRef();

        // We can't catch access denied exceptions on the public node service because the transaction
        // interceptor will automatically roll back the tx if an exception is thrown
        AccessStatus access = permissionService.hasPermission(source, PermissionService.READ);
        if(access.equals(AccessStatus.DENIED)) {
          // ignore these - we just won't show a relationship if the user doesn't have access
          logger.error("Acess denied to association source: " + source + "  Ingoring.");
        } else {
          String sourcePath = WikiUtils.getWikiPath(source, nodeService);
          // for some reason, we had relationships pointing to nodes in the archive, so they have
          // no wiki path - not sure how that happened
          if(sourcePath != null) {

            String relationship = assoc.getTypeQName().toString();
            //String mimetype = (String)nodeService.getProperty(source, VeloConstants.PROP_MIMEYPE);

            printStream.println(sourcePath + "\t" + relationship + "\t" + wikiPath);
          }
        }
      }
      
      printStream.flush();

    } catch (Throwable t) {
      logger.error("Could not print message to response stream.", t);
    }
  }
}
