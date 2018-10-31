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
package gov.pnnl.cat.policy.ignore;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Make the cat:ignore aspect inheritable.  This will perpetuate the cat:ignore aspect for nodes
 *  created under nodes with cat:ignore aspect.  
 *
 * @version $Revision: 1.0 $
 */
public class CatIgnoreBehavior extends ExtensiblePolicyAdapter implements CopyServicePolicies.OnCopyNodePolicy {

  //Logger
  private static final Log logger = LogFactory.getLog(CatIgnoreBehavior.class);

  /**
   * Bind to catIgnore aspect only (could be set in extensible-policy-context.xml if we want to)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    logger.debug("initializing");
    
    this.policyComponent.bindClassBehaviour(
        QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
        CatConstants.ASPECT_IGNORE,
        new JavaBehaviour(this, "getCopyCallback"));      
    
    this.policyComponent.bindAssociationBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"), 
        CatConstants.ASPECT_IGNORE, new JavaBehaviour(this, "onCreateChildAssociation", NotificationFrequency.TRANSACTION_COMMIT));


  }
  
  /**
   * In general, we want to copy this aspect EXCEPT if this aspect is being copied
   * from a template in the data dictionary (since all dictionary folders are stamped
   * with the ignore aspect).
   * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#getCopyCallback(org.alfresco.service.namespace.QName, org.alfresco.repo.copy.CopyDetails)
   */
  @Override
  public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
    NodeRef sourceNodeRef = copyDetails.getSourceNodeRef(); 
    String path = nodeService.getPath(sourceNodeRef).toString();

    if(path.startsWith(CatConstants.QPATH_SPACE_TEMPLATES)) {
      // don't copy this aspect, because it is coming from a space template
      return DoNothingCopyBehaviourCallback.getInstance();

    } else {
      // copy this aspect as normal
      return DefaultCopyBehaviourCallback.getInstance();
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
    // pass the aspect on to the new child
    if(nodeService.exists(childAssocRef.getChildRef())) {
      nodeService.addAspect(childAssocRef.getChildRef(), CatConstants.ASPECT_IGNORE, null);
    }
  }

}
