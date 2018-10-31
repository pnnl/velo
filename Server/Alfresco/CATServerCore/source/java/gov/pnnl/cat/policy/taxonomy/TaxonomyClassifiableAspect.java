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
package gov.pnnl.cat.policy.taxonomy;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class TaxonomyClassifiableAspect extends ExtensiblePolicyAdapter
implements CopyServicePolicies.OnCopyNodePolicy {

  protected static Log logger = LogFactory
  .getLog(TaxonomyClassifiableAspect.class);
  protected PolicyComponent policyComponent;

  /**
   * @param policyComponent
   *            the policyComponent to set
   */
  public void setPolicyComponent(PolicyComponent policyComponent) {
    this.policyComponent = policyComponent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    // onCopyNode needs to be bound by this class
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
        CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, 
        new JavaBehaviour(this, "getCopyCallback"));

  }

  /**
   * Taxonomy categories should ONLY be applied to nodes within a taxonomy. So
   * if this node gets copied outside a taxonomy, we DON'T want to copy this
   * aspect. Nodes copied within taxonomies will get the right categories set
   * due to the other taxonomy policy running.
   *
   * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
   */
  @Override
  public CopyBehaviourCallback getCopyCallback(QName classRef,
      CopyDetails copyDetails) {
    // don't copy this aspect
    return DoNothingCopyBehaviourCallback.getInstance();
  }  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef child = childAssocRef.getChildRef();
    nodeService.addAspect(child,
        CatConstants.ASPECT_TAXONOMY_CLASSIFICATION, null);
  }

}
