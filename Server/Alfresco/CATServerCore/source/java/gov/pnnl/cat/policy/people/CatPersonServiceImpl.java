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
package gov.pnnl.cat.policy.people;

import gov.pnnl.cat.policy.ExtensiblePolicyInterface;
import gov.pnnl.cat.util.CatConstants;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

/**
 * We have to reimplement Alfresco's personServiceImpl so we can
 * use it with our extensible policy framework.  Otherwise, we could
 * not bind any other policy to the cm:person type without overwriting
 * this behavior.
 *
 * TODO: every time personServiceImpl changes, we need to merge here!
 * @version $Revision: 1.0 $
 */

public class CatPersonServiceImpl extends PersonServiceImpl implements ExtensiblePolicyInterface {

  protected OwnableService ownableService;

  public CatPersonServiceImpl() {
    super();
  }

  @Override
  public void init() {
    // We have to override the init method so we can use this class with
    // extensible policy framework
    // If we let alfresco do the binding here, we could not bind any other
    // policy (like notifiable aspect behavior) to the cm:person type without
    // overwriting this behavior
    

    PropertyCheck.mandatory(this, "storeUrl", storeRef);
    PropertyCheck.mandatory(this, "transactionService", transactionService);
    PropertyCheck.mandatory(this, "nodeService", nodeService);
    PropertyCheck.mandatory(this, "permissionServiceSPI", permissionServiceSPI);
    PropertyCheck.mandatory(this, "authorityService", authorityService);
    PropertyCheck.mandatory(this, "authenticationService", authenticationService);
    PropertyCheck.mandatory(this, "namespacePrefixResolver", namespacePrefixResolver);
    PropertyCheck.mandatory(this, "policyComponent", policyComponent);
    PropertyCheck.mandatory(this, "personCache", personCache);
    PropertyCheck.mandatory(this, "aclDao", aclDao);
    PropertyCheck.mandatory(this, "homeFolderManager", homeFolderManager);
    PropertyCheck.mandatory(this, "repoAdminService", repoAdminService);

    beforeCreateNodeValidationBehaviour = new JavaBehaviour(this, "beforeCreateNodeValidation");
    this.policyComponent.bindClassBehaviour(
            BeforeCreateNodePolicy.QNAME,
            ContentModel.TYPE_PERSON,
            beforeCreateNodeValidationBehaviour);
    
    beforeDeleteNodeValidationBehaviour = new JavaBehaviour(this, "beforeDeleteNodeValidation");
    this.policyComponent.bindClassBehaviour(
            BeforeDeleteNodePolicy.QNAME,
            ContentModel.TYPE_PERSON,
            beforeDeleteNodeValidationBehaviour);
  
    // CAT Change:  comment out these policies so they get bound by 
    // extensible policy behavior instead
//    this.policyComponent.bindClassBehaviour(
//            OnCreateNodePolicy.QNAME,
//            ContentModel.TYPE_PERSON,
//            new JavaBehaviour(this, "onCreateNode"));
//    
//    this.policyComponent.bindClassBehaviour(
//            BeforeDeleteNodePolicy.QNAME,
//            ContentModel.TYPE_PERSON,
//            new JavaBehaviour(this, "beforeDeleteNode"));
//    
//    this.policyComponent.bindClassBehaviour(
//            OnUpdatePropertiesPolicy.QNAME,
//            ContentModel.TYPE_PERSON,
//            new JavaBehaviour(this, "onUpdateProperties"));
    
    this.policyComponent.bindClassBehaviour(
            OnUpdatePropertiesPolicy.QNAME,
            ContentModel.TYPE_USER,
            new JavaBehaviour(this, "onUpdatePropertiesUser"));

  }

  /* (non-Javadoc)
   * @see org.alfresco.repo.security.person.PersonServiceImpl#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    // first do the person service create node to create the home folder
    super.onCreateNode(childAssocRef);
    
    // CAT Change:  now add our code to recursively change ownership of the folders
    // created by the space tempalte
    NodeRef personNodeRef = childAssocRef.getChildRef();
    Serializable homeFolderProp = nodeService.getProperty(personNodeRef, CatConstants.PROP_USER_HOME_FOLDER);
    if(homeFolderProp == null){
      homeFolderProp = nodeService.getProperty(personNodeRef, CatConstants.PROP_TEAM_HOME_FOLDER);
    }
    NodeRef homeFolderRef = DefaultTypeConverter.INSTANCE.convert(NodeRef.class, 
        homeFolderProp);
    recursiveSetOwner(homeFolderRef, ownableService.getOwner(homeFolderRef));
    
  }
  
  /**
   * Method recursiveSetOwner.
   * @param nodeRef NodeRef
   * @param owner String
   */
  protected void recursiveSetOwner(NodeRef nodeRef, String owner)
  {
    ownableService.setOwner(nodeRef, owner);
    List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef);
    for (ChildAssociationRef child : children)
    {
      recursiveSetOwner(child.getChildRef(), owner);
    }
  }
  
  /**
   * Method setOwnableService.
   * @param ownableService OwnableService
   */
  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

  /**
   * Method onAddAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method beforeRemoveAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  @Override
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onMoveNode.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  @Override
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onCopyComplete.
   * @param classRef QName
   * @param sourceNodeRef NodeRef
   * @param targetNodeRef NodeRef
   * @param copyToNewNode boolean
   * @param copyMap Map<NodeRef,NodeRef>
   * @see org.alfresco.repo.copy.CopyServicePolicies$OnCopyCompletePolicy#onCopyComplete(QName, NodeRef, NodeRef, boolean, Map<NodeRef,NodeRef>)
   */
  @Override
  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onContentUpdate.
   * @param nodeRef NodeRef
   * @param newContent boolean
   * @see org.alfresco.repo.content.ContentServicePolicies$OnContentUpdatePolicy#onContentUpdate(NodeRef, boolean)
   */
  @Override
  public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onCreateAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateAssociationPolicy#onCreateAssociation(AssociationRef)
   */
  @Override
  public void onCreateAssociation(AssociationRef nodeAssocRef) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onCreateChildAssociation.
   * @param childAssocRef ChildAssociationRef
   * @param isNewNode boolean
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef, boolean)
   */
  @Override
  public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method onDeleteAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnDeleteAssociationPolicy#onDeleteAssociation(AssociationRef)
   */
  @Override
  public void onDeleteAssociation(AssociationRef nodeAssocRef) {
    // TODO Auto-generated method stub
    
  }
  
  
}
