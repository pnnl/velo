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
package gov.pnnl.cat.actions;

import gov.pnnl.cat.imprt.ImportService;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class UrlImportAction extends ActionExecuterAbstractBase {

  private static final String URL_LIST_PARAM = "url-list-as-xml";
  private static final String DISPLAY_URL_LIST_PARAM = "java.util.ArrayList of String urls, serialized with xstream";

  private static final Log logger = LogFactory.getLog(UrlImportAction.class);

  private ImportService importService;
  private NodeService nodeService;

  /**
   * Set the node service
   * 
   * @param nodeService  set the node service
   */
  public void setNodeService(NodeService nodeService) 
  {
	this.nodeService = nodeService;
  }

  /**
   * Method setImportService.
   * @param importService ImportService
   */
  public void setImportService(ImportService importService) {
    this.importService = importService;
  }



  /**
   * Method addParameterDefinitions.
   * @param paramList List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	ParameterDefinitionImpl param = new ParameterDefinitionImpl(
		URL_LIST_PARAM, 
		DataTypeDefinition.TEXT,
		true,
		DISPLAY_URL_LIST_PARAM);
	paramList.add(param);
  }

  /**
   * I redesigned this so that we can break the create links into their own transactions, so we
   * don't get weird crashes when the tx gets too big, and so we don't deadlock the server.
   * 
   * -CSL
   * @param ruleAction Action
   * @param folderNode NodeRef
   */
  @Override
  protected void executeImpl(Action ruleAction, NodeRef folderNode) {
	String urlListStringXml = (String)ruleAction.getParameterValue(URL_LIST_PARAM);
	logger.debug("--- start");
	logger.debug(URL_LIST_PARAM  + " = " + urlListStringXml);
	logger.debug("nodeActedUpon = " + nodeService.getPath(folderNode).toString());

	importService.importUrlListAsXml(urlListStringXml, folderNode);
  }

}
