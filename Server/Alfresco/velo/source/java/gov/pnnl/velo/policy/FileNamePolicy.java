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
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.WikiUtils;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: whenever a cm:name property is changed, if the node is
 * under wiki control, make sure there are no spaces in the name, since
 * the wiki cannot handle this.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class FileNamePolicy extends ExtensiblePolicyAdapter {

  private static String badFileNameRegex = "[^a-zA-Z0-9\\-_.]+";
  private static Pattern badFileNamePattern;
  protected static final Log logger = LogFactory.getLog(FileNamePolicy.class);
  
  /**
   * Method setBadFileNameRegex.
   * @param badFileNameRegex String
   */
  public void setBadFileNameRegex(String badFileNameRegex) {
    FileNamePolicy.badFileNameRegex = badFileNameRegex;
  }

  @Override
  public void init() {

    // First check to see if we are running on a machine with a wiki installed

    if(WikiUtils.getWikiHome() == null) {
      logger.info("wiki.home not specified.  Not implementing wiki integration policy.");

    } else {

      policyComponent.bindClassBehaviour(
          QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
          this,
          new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT)); 

      // compile the regex
      badFileNamePattern = Pattern.compile(badFileNameRegex);
    }
  }

  /**
   * Method getBadFileNamePattern.
   * @return Pattern
   */
  public Pattern getBadFileNamePattern() {
    return badFileNamePattern;
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
    // if this node is under wiki control and should be renamed
    if(WikiUtils.isRenamableWikiNode(nodeRef, nodeService, namespaceService)) {
      
      String name = (String)after.get(ContentModel.PROP_NAME);

      // if the file name has spaces, replace them with underscore
      if(name != null && badFileNamePattern.matcher(name).find()) {
        String newName = getFixedName(name);

        // make sure both the name property and the assocation name are changed
        NodeUtils.renameNode(nodeRef, newName, nodeService);
      }
    }
  }
  
  /**
   * Method getFixedName.
   * @param oldName String
   * @return String
   */
  public static String getFixedName(String oldName) {
    if(WikiUtils.getWikiHome() == null) {
      return oldName;
    
    } else {
      String newName = badFileNamePattern.matcher(oldName).replaceAll("--");
      return newName;
    }
  }

}
