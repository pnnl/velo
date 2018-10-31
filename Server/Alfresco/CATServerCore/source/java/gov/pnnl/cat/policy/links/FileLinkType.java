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
package gov.pnnl.cat.policy.links;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Do not allow users to create multi-level links.  If a link is created that points to another 
 * link, then change the target to point to the original file.
 * 
 * Force link name to end in .url, so Windows will allow it to be opened via CIFS.
 *
 * @version $Revision: 1.0 $
 */
public class FileLinkType extends ExtensiblePolicyAdapter {
  private BehaviourFilter policyBehaviourFilter;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  @Override
  public void init() {
    //  don't need to bind policy here, as it is done in ExtensiblePolicy       
  }
  
  /**
   * Method setPolicyBehaviourFilter.
   * @param policyBehaviourFilter BehaviourFilter
   */
  public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
    this.policyBehaviourFilter = policyBehaviourFilter;
  }

  /**
   * Method onUpdateProperties.
   * @param nodeRef NodeRef
   * @param before Map<QName,Serializable>
   * @param after Map<QName,Serializable>
   * @see org.alfresco.repo.node.NodeServicePolicies$OnUpdatePropertiesPolicy#onUpdateProperties(NodeRef, Map<QName,Serializable>, Map<QName,Serializable>)
   */
  @Override
  public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
    policyBehaviourFilter.disableBehaviour();

    try { 
      // This link could have been deleted by other policy, so we need to make sure it still exists before we continue
      if(!nodeService.exists(nodeRef)) {
        return;
      }

      // Check file name
      String nameBefore = (String)before.get(ContentModel.PROP_NAME);
      String nameAfter = (String) after.get(ContentModel.PROP_NAME);
      if(nameBefore == null || !nameBefore.equals(nameAfter)) {
        String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        if(!name.endsWith(".url")) {
          name = name.concat(".url");
          nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, name);
        }        
      }

      // Check target
      NodeRef targetBefore = (NodeRef)before.get(ContentModel.PROP_LINK_DESTINATION);
      NodeRef targetAfter = (NodeRef)after.get(ContentModel.PROP_LINK_DESTINATION);
      if(targetBefore == null || !targetBefore.equals(targetAfter)) {
        NodeRef target = (NodeRef)nodeService.getProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION);

        // if target is a link
        if( nodeService.getType(target).equals(ApplicationModel.TYPE_FILELINK) ||
            nodeService.getType(target).equals(ContentModel.TYPE_LINK)) {
          // only allow one level deep links
          NodeRef originalFile = (NodeRef)nodeService.getProperty(target, ContentModel.PROP_LINK_DESTINATION);
          nodeService.setProperty(nodeRef, ContentModel.PROP_LINK_DESTINATION, originalFile);
          target = originalFile;
        }      
      }
    } finally {
      policyBehaviourFilter.enableBehaviour();
    }
  }
}

