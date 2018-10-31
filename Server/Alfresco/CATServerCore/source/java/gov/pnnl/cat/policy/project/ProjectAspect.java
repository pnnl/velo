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
package gov.pnnl.cat.policy.project;


import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 */
public class ProjectAspect extends ExtensiblePolicyAdapter {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    //  don't need to bind policy here, as it is done in ExtensiblePolicy    
  }

  /**
   * Enforce integrity constrains on projects.  They can't be
   * created inside a taxonomy.
   * @param nodeRef NodeRef
   * @param aspectTypeQName QName
   * @see org.alfresco.repo.node.NodeServicePolicies$OnAddAspectPolicy#onAddAspect(NodeRef, QName)
   */
  @Override
  public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {

    // if node is not a folder, throw an exception
    QName type = nodeService.getType(nodeRef);
    if (!type.equals(ContentModel.TYPE_FOLDER)) {
      throwIntegrityException("Can only apply project aspect to a cm:folder");
    }

    // if node is already a taxonomyRoot or taxonomyFolder, throw an exception
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      throwIntegrityException("Project aspect may not be added to a taxonomy node!");
    }

    // if node is already a homeFolder, throw an exception
    if (nodeService.hasAspect(nodeRef, CatConstants.ASPECT_HOME_FOLDER)) {
      throwIntegrityException("Project aspect may not be added to a home folder!");
    }

    // if parent node is a taxonomyRoot or taxonomyFolder, throw an exception
    // this means projects can't be created or copied into a taxonomy
    NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
    if (nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_ROOT) ||
        nodeService.hasAspect(parent, CatConstants.ASPECT_TAXONOMY_FOLDER)) {
      throwIntegrityException("Projects may not be created inside a taxonomy!");
    }

  }

}
