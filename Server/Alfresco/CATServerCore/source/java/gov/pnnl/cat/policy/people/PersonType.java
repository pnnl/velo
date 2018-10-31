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

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Behavior used to keep user profiles and home folders correctly in synch.
 * Right now, the home folder property could be changed if you are an admin
 * and go through the Alfresco UI.  TODO: We may want to support onUpdateProperties
 * and not ever allow this property to be changed if we think this could be
 * a problem.
 *
 * @version $Revision: 1.0 $
 */
public class PersonType extends ExtensiblePolicyAdapter {

  /** The logger */
  @SuppressWarnings("unused")
  private static Log logger = LogFactory.getLog(PersonType.class); 

  /**
   * Spring init method used to register the policy behaviors
   */
  public void init() {
    // done in ExtensiblePolicy
  }

  /**
   * Give the user full permissions on his node so he can
   * edit his profile with both the user web service and
   * the content web service (for changing his picture)
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */ 
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef profile = childAssocRef.getChildRef();
    String user = (String)nodeService.getProperty(profile, ContentModel.PROP_USERNAME);
    permissionService.setPermission(profile, user, 
        permissionService.getAllPermission(), true);

  }



  /**
   * Remove the corresponding user's home folder if it is in the CAT
   * users area.  Since the user profile can only be deleted by an admin, 
   * this method should be running with admin permissions, so deleting
   * this folder should work.  HomeFolderManager does not clean up
   * home folders, so we have to do this in our own policy.
   * 
   * HomeFolderManager doesn't clean up home folders, so we have to do
   * this ourselves.
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.node.NodeServicePolicies$BeforeDeleteNodePolicy#beforeDeleteNode(NodeRef)
   */
  @Override
  public void beforeDeleteNode(NodeRef nodeRef) {

    NodeRef personProfile = nodeRef;

    // Get the current home folder reference
    NodeRef homeFolder = (NodeRef)nodeService.getProperty(personProfile, ContentModel.PROP_HOMEFOLDER);

    if(homeFolder != null && nodeService.exists(homeFolder)) {
      // See if homeFolder is in the users area - if it is, then delete it
      NodeRef parent = nodeService.getPrimaryParent(homeFolder).getParentRef();
      NodeRef userDocs = getUserDocsFolder();

      if(parent.equals(userDocs)) {
        nodeService.deleteNode(homeFolder);      
      }
    }
  }

  /**
   * Gets a NodeRef to the user documents area
  
   * @return NodeRef
   */
  protected NodeRef getUserDocsFolder() {

    NodeRef userDocs = nodeUtils.getNodeByName(CatConstants.PATH_USER_DOCUMENTS);

    if(userDocs == null) {
      NodeRef companyHome = nodeUtils.getCompanyHome();
      userDocs = NodeUtils.createFolder(companyHome, CatConstants.NAME_USER_DOCUMENTS.getLocalName(), null, nodeService);
    }

    return userDocs;
  }

}
