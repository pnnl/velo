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

import java.io.File;
import java.io.PrintStream;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 */
public class GetContentUrls extends AbstractVeloWebScript {
  protected Log logger = LogFactory.getLog(this.getClass());

  public static final String PARAM_ROOT_UUID = "rootUuid";
  
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    String rootUuid = req.getParameter(PARAM_ROOT_UUID);
    NodeRef rootNode = null;
    if(rootUuid != null){
      rootNode = NodeUtils.getNodeByUuid(rootUuid);
    }else{  
      rootNode = nodeUtils.getCompanyHome();
    }
    PrintStream printStream = null;
    try{
      printStream = new PrintStream(res.getOutputStream(), true, "UTF-8");
      recursivelyVisitNode(rootNode, printStream);
      printStream.flush();
    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
    return null;
  }
  
  /**
   * Method recursivelyVisitNode.
   * @param nodeRef NodeRef
   * @param printStream PrintStream
   */
  private void recursivelyVisitNode(NodeRef nodeRef, PrintStream printStream) {
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
      printStream.println(DownloadContentServlet.generateDownloadURL(nodeRef, (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME)));
    }
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef childAssoc : children) {
      recursivelyVisitNode(childAssoc.getChildRef(), printStream);
    }

  }

}
