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
/**
 * Notice: This computer software was prepared by Battelle Memorial Institute,
 * hereinafter the Contractor for the Department of Homeland Security under the
 * terms and conditions of the U.S. Department of Energy's Operating Contract
 * DE-AC06-76RLO with Battelle Memorial Institute, Pacific Northwest Division.
 * All rights in the computer software are reserved by DOE on behalf of the
 * United States Government and the Contractor as provided in the Contract. You
 * are authorized to use this computer software for Governmental purposes but it
 * is not to be released or distributed to the public. NEITHER THE GOVERNMENT
 * NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
 * LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
 * must appear on any copies of this computer software.
 */
package gov.pnnl.cat.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 */
public class ExtensiblePolicy implements ExtensiblePolicyInterface {

  /* So we can bind policies */
  protected PolicyComponent policyComponent;

  /* Behavior implementations */
  protected List<ExtensiblePolicyInterface> extensiblePolicies;

  /* The type name to bind to (either node type or aspect type) */
  protected String typeQName;

  /**
   * Method setExtensiblePolicies.
   * @param extensiblePolicies List<ExtensiblePolicyInterface>
   */
  public void setExtensiblePolicies(List<ExtensiblePolicyInterface> extensiblePolicies) {
    this.extensiblePolicies = extensiblePolicies;
  }

  /**
   * Method setTypeQName.
   * @param typeQName String
   */
  public void setTypeQName(String typeQName) {
    this.typeQName = typeQName;
  }

  /**
   * Method setPolicyComponent.
   * @param policyComponent PolicyComponent
   */
  public void setPolicyComponent(PolicyComponent policyComponent) {
    this.policyComponent = policyComponent;
  }

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {
    QName type = QName.createQName(typeQName);

    // Register the policy behaviours
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"), type, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), type, new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveAspect"), type, new JavaBehaviour(this, "beforeRemoveAspect", NotificationFrequency.FIRST_EVENT));

    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), type, new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onMoveNode"), type, new JavaBehaviour(this, "onMoveNode", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), type, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"), type, new JavaBehaviour(this, "onCopyComplete", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindClassBehaviour(ContentServicePolicies.ON_CONTENT_UPDATE, type, new JavaBehaviour(this, "onContentUpdate"));
    this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateAssociation"), type, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), type, new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));
    this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteAssociation"), type, new JavaBehaviour(this, "onDeleteAssociation", NotificationFrequency.FIRST_EVENT));
  }

  /**
   * Method onMoveNode.
   * @param oldChildAssocRef ChildAssociationRef
   * @param newChildAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnMoveNodePolicy#onMoveNode(ChildAssociationRef, ChildAssociationRef)
   */
  public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onMoveNode(oldChildAssocRef, newChildAssocRef);
      }
    }
  }

  /**
   * Method beforeDeleteNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  public void beforeDeleteNode(NodeRef nodeRef) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.beforeDeleteNode(nodeRef);
      }
    }
  }

  /**
   * Method onAddAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onAddAspect(nodeRef, aspectTypeQName);
      }
    }
  }

  /**
   * Method beforeRemoveAspect.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeRemoveAspectPolicy#beforeRemoveAspect(NodeRef, QName)
   */
  public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.beforeRemoveAspect(nodeRef, aspectTypeQName);
      }
    }
  }

  /**
   * Method onUpdateProperties.
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onUpdateProperties(nodeRef, before, after);
      }
    }
  }

  /**
   * Method onCopyComplete.
   * @param classRef QName
   * @param sourceNodeRef NodeRef
   * @param destinationRef NodeRef
   * @param copyToNewNode boolean
   * @param copyMap Map<NodeRef,NodeRef>
   * @see org.alfresco.repo.copy.CopyServicePolicies$OnCopyCompletePolicy#onCopyComplete(QName, NodeRef, NodeRef, boolean, Map<NodeRef,NodeRef>)
   */
  public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onCopyComplete(classRef, sourceNodeRef, destinationRef, copyToNewNode, copyMap);
      }
    }
  }

  /**
   * Method onCreateNode.
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onCreateNode(childAssocRef);
      }
    }
  }

  /**
   * Method onContentUpdate.
   * @param nodeRef NodeRef
   * @param newContent boolean
   * @see org.alfresco.repo.content.ContentServicePolicies$OnContentUpdatePolicy#onContentUpdate(NodeRef, boolean)
   */
  public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onContentUpdate(nodeRef, newContent);
      }
    }
  }

  /**
   * Method onCreateAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateAssociationPolicy#onCreateAssociation(AssociationRef)
   */
  public void onCreateAssociation(AssociationRef nodeAssocRef) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onCreateAssociation(nodeAssocRef);
      }
    }
  }

  /**
   * Method onDeleteAssociation.
   * @param nodeAssocRef AssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnDeleteAssociationPolicy#onDeleteAssociation(AssociationRef)
   */
  public void onDeleteAssociation(AssociationRef nodeAssocRef) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onDeleteAssociation(nodeAssocRef);
      }
    }
  }

  /**
   * Method onCreateChildAssociation.
   * @param childAssocRef ChildAssociationRef
   * @param isNewNode boolean
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateChildAssociationPolicy#onCreateChildAssociation(ChildAssociationRef, boolean)
   */
  @Override
  public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode) {
    if (extensiblePolicies != null) {
      for (ExtensiblePolicyInterface adapter : extensiblePolicies) {
        adapter.onCreateChildAssociation(childAssocRef, isNewNode);
      }
    }
  }

}
