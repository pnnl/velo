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

import java.util.Date;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 */
public class TestNotificationFloodAction extends ActionExecuterAbstractBase {

	private static final String ITERATIONS_PARAM = "iterations";
	private static final String DISPLAY_URL_LIST_PARAM = "string containing a number of loop iterations to perform";

	private static final Log logger = LogFactory.getLog(TestNotificationFloodAction.class);

	private JmsTemplate template;
	private Destination destination;
	private NodeService nodeService;
	private NamespacePrefixResolver namespacePrefixResolver;

//	private List localUrlBaseList;


	/**
	 * Method setTemplate.
	 * @param template JmsTemplate
	 */
	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}
	
	/**
	 * Method setDestination.
	 * @param destination Destination
	 */
	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Method setNamespacePrefixResolver.
	 * @param namespacePrefixResolver NamespacePrefixResolver
	 */
	public void setNamespacePrefixResolver(
			NamespacePrefixResolver namespacePrefixResolver) {
		this.namespacePrefixResolver = namespacePrefixResolver;
	}

	/**
	 * Method addParameterDefinitions.
	 * @param paramList List<ParameterDefinition>
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		ParameterDefinitionImpl param = new ParameterDefinitionImpl(
				ITERATIONS_PARAM, 
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
	 * @param targetNode NodeRef
	 */
	@Override
	protected void executeImpl(Action ruleAction, NodeRef targetNode) {
		String iterationsParam = (String)ruleAction.getParameterValue(ITERATIONS_PARAM);
		final int iterations = Integer.parseInt(iterationsParam);
		final String nodeId = targetNode.getId();
		final String nodePath = nodeService.getPath(targetNode).toString();
		final Date now = new Date();

		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
					logger.debug("Start");
					for (int i=0; i<iterations; i++) {
						String xml = "<gov.pnl.dmi.policy.notifiable.message.RepositoryEventMessage>" +
						"<events serialization=\"custom\">" +
						"<list>" +
						"<default>" +
						"<size>1</size>" +
						"</default>" +
						"<int>10</int>" +
						"<gov.pnl.dmi.policy.notifiable.message.PropertyChangedEvent>" +
						"<nodePath>" + nodePath + "</nodePath>" +
						"<nodeId>" + nodeId + "</nodeId>" +
						"<eventPerpetrator>admin</eventPerpetrator>" +
						"<eventTimestamp>1200941019667</eventTimestamp>" +
						"<propertyName>" + ContentModel.PROP_MODIFIED.toString() + "</propertyName>" +
						"<propertyValue>" + now + "</propertyValue>" + 
						"</gov.pnl.dmi.policy.notifiable.message.PropertyChangedEvent>" +
						"</list>" +
						"</events>" +
						"</gov.pnl.dmi.policy.notifiable.message.RepositoryEventMessage>";
						sendEvents(xml);
					}
					logger.debug("end");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		};
		new Thread(r).start();

	}
	
	/**
	 * Method sendEvents.
	 * @param xml String
	 */
	private void sendEvents(final String xml) {    


		template.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage(xml);
				message.setStringProperty("next", "foo");
				message.setLongProperty("sent", System.currentTimeMillis());
				return message;
			}
		});

		if(logger.isDebugEnabled())
			logger.debug(xml);
		logger.info("message generated");      
	}



}
