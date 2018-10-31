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
package gov.pnnl.cat.patches;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.util.Set;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This patch changes the property that stores the team's home folder to use cat:homefolder instead of cm:homefolder
 * @version $Revision: 1.0 $
 */
public class TeamHomeFolderPropertyPatch extends AbstractPatch {

  // Logger
  private static final Log logger = LogFactory.getLog(TeamHomeFolderPropertyPatch.class);
 
  protected AuthorityService authorityService;

  protected NodeUtils nodeUtils;
  /**
   * @param authorityService the authorityService to set
   */
  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Ensure that required properties have been set
   * @throws Exception
   */
  protected void checkRequiredProperties() throws Exception
  {
      checkPropertyNotNull(nodeService, "nodeService");
      checkPropertyNotNull(nodeUtils, "nodeUtils");
      checkPropertyNotNull(authorityService, "authorityService");
  }
  
  
  /**
   * Method applyInternal.
   * @return String
   * @throws Exception
   */
  @Override
  protected String applyInternal() throws Exception {
    Set<String> groups = authorityService.getAllAuthorities(AuthorityType.GROUP);
    for (String groupName : groups) {
      String profilePath = CatConstants.XPATH_TEAM_CONTAINER + "/cm:" + ISO9075.encode(groupName);
      NodeRef profile = nodeUtils.getNodeByXPath(profilePath);
      //we're moving team's home folders from the alfresco property to the cat one. Alfresco's home folder property is still being used for user homes
      NodeRef teamHome = (NodeRef)nodeService.getProperty(profile, CatConstants.PROP_USER_HOME_FOLDER);
      nodeService.setProperty(profile, CatConstants.PROP_TEAM_HOME_FOLDER, teamHome);
      nodeService.removeProperty(profile, CatConstants.PROP_USER_HOME_FOLDER);
    }

    return "OK";
  }
 
}
