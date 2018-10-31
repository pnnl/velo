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
package gov.pnnl.velo.policy;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.velo.util.VeloServerConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Whenever a new user home folder is created, add the typedCollection aspect
 * and mimetype cmsfile/user to match what the wiki expects.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class UserHomeFolderPolicy extends ExtensiblePolicyAdapter {


  @Override
  public void init() {
    // bind policy
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
        this,
        new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));     
  }

  /**
   * Method onCreateNode.
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef nodeRef = childAssocRef.getChildRef();
    NodeRef parent = childAssocRef.getParentRef();

    if(nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, CatConstants.ASPECT_USER_HOME_FOLDER)) {
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
      properties.put(VeloServerConstants.PROP_MIMEYPE, "cmsfile/user");
      nodeService.addAspect(nodeRef, VeloServerConstants.ASPECT_TYPED_COLLECTION, properties); 

      // make sure parent also has right mimetype
      if(!nodeService.hasAspect(parent, VeloServerConstants.ASPECT_TYPED_COLLECTION)) {
        Map<QName, Serializable> parentProps = new HashMap<QName, Serializable>(1);
        parentProps.put(VeloServerConstants.PROP_MIMEYPE, "cmsfile/users");
        nodeService.addAspect(parent, VeloServerConstants.ASPECT_TYPED_COLLECTION, parentProps);  
      }
    }

  }


}
