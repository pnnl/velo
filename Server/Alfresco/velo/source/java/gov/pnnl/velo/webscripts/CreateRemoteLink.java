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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * Create one link to a remote URL.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CreateRemoteLink extends AbstractVeloWebScript {

  public static final String PARAM_PARENT_PATH = "parentPath";  
  public static final String PARAM_LINK_NAME = "linkName";  
  public static final String PARAM_LINK_URL = "linkUrl";  
  public static final String PARAM_LINK_TITLE = "linkTitle";  
  public static final String PARAM_LINK_DESCRIPTION = "linkDescription";  
  public static final String PARAM_LINK_TYPE = "linkType";  
  public static final String PARAM_HAS_CONTENT = "hasContent";  
  public static final String PARAM_IS_COPYRIGHTED = "isCopyrighted";  

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
    String parentPath = req.getParameter(PARAM_PARENT_PATH);
    String linkName = req.getParameter(PARAM_LINK_NAME);
    String linkUrl = req.getParameter(PARAM_LINK_URL);
    String linkTitle = req.getParameter(PARAM_LINK_TITLE);
    String linkDescription = req.getParameter(PARAM_LINK_DESCRIPTION);
    String linkType = req.getParameter(PARAM_LINK_TYPE);
    boolean hasContent = Boolean.valueOf(req.getParameter(PARAM_HAS_CONTENT));
    boolean isCopyrighted = Boolean.valueOf(req.getParameter(PARAM_IS_COPYRIGHTED));

    // Get the parent node
    if(!parentPath.endsWith("/"))  {
      parentPath = parentPath + "/";
    }
    parentPath = WikiUtils.getAlfrescoNamePath(parentPath);
    logger.debug("parentPath = " + parentPath);
    // (will throw exception if node does not exist)
    NodeRef parent = WikiUtils.getNodeByName(parentPath, nodeService);

    QName linkTypeQname = ContentModel.TYPE_CONTENT;
    if(linkType != null){
      if(linkType.equalsIgnoreCase("folder")){
        linkTypeQname = ContentModel.TYPE_FOLDER;
      }
    }

    // Add the link
    Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
    props.put(ContentModel.PROP_NAME, linkName);
    if(hasContent) {
      props.put(VeloServerConstants.PROP_TEMPORARY_REMOTE_URL, linkUrl);
      if(linkTitle != null) {
        props.put(VeloServerConstants.PROP_TEMPORARY_LINK_TITLE, linkTitle);
      }
      if(linkDescription != null) {
        props.put(VeloServerConstants.PROP_TEMPORARY_LINK_DESCRIPTION, linkDescription);
      }
      props.put(VeloServerConstants.PROP_HAS_LINK_CONTENT, true);
      
    } else {
      props.put(CatConstants.PROP_LINK_URL, linkUrl);

      if(linkTitle != null) {
        props.put(CatConstants.PROP_LINK_TITLE, linkTitle);
      } 
      if(linkDescription != null) {
        props.put(CatConstants.PROP_LINK_DESCRIPTION, linkDescription);
      } 
    }
    if(isCopyrighted) {
      // add the copyright aspect to the node
      props.put(VeloServerConstants.PROP_HAS_COPYRIGHT, true);
    }

    ChildAssociationRef linkRef = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, linkName), linkTypeQname, props);
    NodeRef link = linkRef.getChildRef();
    
    if(isCopyrighted) {
      // change the permissions so only current user can see the file
      String username = AuthenticationUtil.getRunAsUser();
      permissionService.setPermission(link, username, PermissionService.ALL_PERMISSIONS, true);

      // do not inherit parent permissions
      permissionService.setInheritParentPermissions(link, false);

    }

    // create some data for the file
    if(linkTypeQname.equals(ContentModel.TYPE_CONTENT)) {
      ContentWriter writer = contentService.getWriter(link, ContentModel.PROP_CONTENT, true);

      if(hasContent) {
        // set specific file content instead of generic one
        // guess the mime type
        final String guessedMimetype = mimetypeService.guessMimetype(linkName);
        writer.setMimetype(guessedMimetype);
        writer.putContent(requestContent);

      } else {
        StringBuilder content = new StringBuilder("Name:");
        content.append(linkName);
        content.append("\n");
        content.append("Remote Link:");
        content.append(linkUrl);
        content.append("\n");
        if(linkDescription != null) {
          content.append("Description:");
          content.append(linkDescription);
        }
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(content.toString());
      }
    }

    return null;
  }

}
