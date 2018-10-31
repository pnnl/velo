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
package gov.pnnl.velo.actions;


import gov.pnnl.cat.actions.crawler.AbstractNodeVisitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class VersioningNodeVisitor extends AbstractNodeVisitor {
  private static Log logger = LogFactory.getLog(VersioningNodeVisitor.class); 
  private NodeService nodeService;
  private VersionService versionService;

  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

	/**
	 * Method getName.
	 * @return String
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getName()
	 */
	@Override
	public String getName() {
		return "ignorableNodeVisitor";
	}

	/**
	 * Method getNodeChildren.
	 * @param nodeRef NodeRef
	 * @return List<ChildAssociationRef>
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#getNodeChildren(NodeRef)
	 */
	@Override
  public List<ChildAssociationRef> getNodeChildren(NodeRef nodeRef) {
	   // Node may not exist by the time the children the next down are fetched
    if(!unprotectedNodeService.exists(nodeRef)) {
      return new ArrayList<ChildAssociationRef>();
    }
	  
	  // Don't visit files' children
	  if(unprotectedNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
	    return new ArrayList<ChildAssociationRef>();
	  }
	  return unprotectedNodeService.getChildAssocs(nodeRef);
  }
	
	/**
	 * don't create a transaction here, because the crawler does it for us
	 * @param nodeRef NodeRef
	 * @see gov.pnnl.cat.actions.crawler.INodeVisitor#visitNode(NodeRef)
	 */
	@Override
	public void visitNode(NodeRef nodeRef) {
	  
	   //NodeRef nodeRef = fileInfo.getNodeToExtract();

//	    if(autoVersion == false ) {
//	      return;
//	    }
	  
	    if(!unprotectedNodeService.getType(nodeRef).equals(ContentModel.TYPE_CONTENT)) {
	      return;
	    }

	    // add the versionable aspect if it's not there
	    if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {

	      // Explicitly set the autoversion and initial version properties to 
	      // false, so even if the default aspect definition gets changed in
	      // contentModel.xml, it won't affect our policy
	      Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	      properties.put(ContentModel.PROP_INITIAL_VERSION, false);
	      properties.put(ContentModel.PROP_AUTO_VERSION, false);
	      properties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
	      nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, properties);       
	    }
	    
	    // increment the version
	    // assume every change is major for now
	    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1, 1.0f);
	    versionProperties.put(Version.PROP_DESCRIPTION, "Version updated via automated policy.");
	    versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
	    versionService.createVersion(nodeRef, versionProperties);
          
	}
	
}
