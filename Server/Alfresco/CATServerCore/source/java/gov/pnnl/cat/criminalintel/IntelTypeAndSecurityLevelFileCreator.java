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
package gov.pnnl.cat.criminalintel;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.TagTimerConstants;
import gov.pnnl.cat.util.XmlUtility;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 *
 * TODO: this class needs to be moved out of core CAT since it is not a core component
 */
public class IntelTypeAndSecurityLevelFileCreator extends AbstractLifecycleBean {

  // Logger
  private static final Log logger = LogFactory.getLog(IntelTypeAndSecurityLevelFileCreator.class);

  private DictionaryService dictionaryService;

  private NodeService nodeService;

  private ContentService contentService;

  private TransactionService transactionService;

  /**
   * Method onShutdown.
   * @param event ApplicationEvent
   */
  protected void onShutdown(ApplicationEvent event) {

  }

  /**
   * Method onBootstrap.
   * @param event ApplicationEvent
   */
  protected void onBootstrap(ApplicationEvent event) {
    RetryingTransactionCallback<Object> cb = new RetryingTransactionCallback<Object>() {
      public Object execute() throws Throwable {

        AuthenticationUtil.setRunAsUserSystem();

        NodeRef confNodeRef = NodeUtils.getConfFolder(nodeService);
        NodeRef intelTypesFile = NodeUtils.getChildByName(confNodeRef, TagTimerConstants.INTEL_TYPES_FILENAME, nodeService);

        // add the node if it has not been added before
        if (intelTypesFile == null) {
          Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
          properties.put(ContentModel.PROP_NAME, TagTimerConstants.INTEL_TYPES_FILENAME);

          intelTypesFile = nodeService.createNode(NodeUtils.getConfFolder(nodeService), 
              ContentModel.ASSOC_CONTAINS, 
              QName.createQName(TagTimerConstants.NAMESPACE_CRIMINAL_INTEL, TagTimerConstants.INTEL_TYPES_FILENAME), 
              ContentModel.TYPE_CONTENT, properties).getChildRef();
          nodeService.addAspect(intelTypesFile, CatConstants.ASPECT_IGNORE, null);
        }

        NodeRef intelSecurityLevelsFile = NodeUtils.getChildByName(confNodeRef, TagTimerConstants.INTEL_SECURITY_LEVELS_FILENAME, nodeService);

        // add the node if it has not been added before
        if (intelSecurityLevelsFile == null) {
          Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
          properties.put(ContentModel.PROP_NAME, TagTimerConstants.INTEL_SECURITY_LEVELS_FILENAME);

          intelSecurityLevelsFile = nodeService.createNode(NodeUtils.getConfFolder(nodeService), 
              ContentModel.ASSOC_CONTAINS, 
              QName.createQName(TagTimerConstants.NAMESPACE_CRIMINAL_INTEL, TagTimerConstants.INTEL_SECURITY_LEVELS_FILENAME), 
              ContentModel.TYPE_CONTENT, properties).getChildRef();

          nodeService.addAspect(intelSecurityLevelsFile, CatConstants.ASPECT_IGNORE, null);


          PropertyDefinition property = dictionaryService.getProperty(TagTimerConstants.PROP_INTEL_MATERIAL_TYPE);
          List<ConstraintDefinition> constraints = property.getConstraints();
          // only expecting one:
          ConstraintDefinition constraintDefinition = constraints.get(0);
          MaterialTypeConstraint constraint = (MaterialTypeConstraint) constraintDefinition.getConstraint();
          Map<String, String> intelTypes = constraint.getIntelTypes();
          Set<String> labels = intelTypes.keySet();
          Map<String, String> intelTypesAndDescriptions = new HashMap<String, String>();

          for (String label : labels) {
            Duration validDuration = new Duration(intelTypes.get(label));
            intelTypesAndDescriptions.put(label, label + " that has a duration of " + validDuration.formattedString());
          }

          property = dictionaryService.getProperty(TagTimerConstants.PROP_INTEL_SECURITY_LEVEL);
          constraints = property.getConstraints();
          // only expecting one:
          constraintDefinition = constraints.get(0);
          SecurityLevelConstraint levelConstraint = (SecurityLevelConstraint) constraintDefinition.getConstraint();
          Map<String, String> securityLevels = levelConstraint.getSecurityLevels();

          ContentWriter writer = contentService.getWriter(intelTypesFile, ContentModel.PROP_CONTENT, true);
          writer.setMimetype(MimetypeMap.MIMETYPE_XML);
          writer.setEncoding("UTF-8");
          writer.putContent(XmlUtility.serialize(intelTypesAndDescriptions));

          writer = contentService.getWriter(intelSecurityLevelsFile, ContentModel.PROP_CONTENT, true);
          writer.setMimetype(MimetypeMap.MIMETYPE_XML);
          writer.setEncoding("UTF-8");
          writer.putContent(XmlUtility.serialize(securityLevels));
        }
        return null;
      }
    };
    transactionService.getRetryingTransactionHelper().doInTransaction(cb, false, true);
  }

  /**
   * Method setDictionaryService.
   * @param dictionaryService DictionaryService
   */
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }
}
