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
package gov.pnnl.cat.alerting.transform;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 */
public class RepositoryDeliveryContentTransformer extends
		AbstractDeliveryContentTransformer {


	private NodeService nodeService;
	
	
	/**
	 * Method setNodeService.
	 * @param nodeService NodeService
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	/**
	 * Return 80% if the target mime type matches the mimetype for an
	 * alert being delivered over the Repository Delivery Channel.  If we ever have 
	 * a transformer that takes into consideration a specific sourceMimeType,
	 * it should return 100% to ensure it gets called over this one.
	 * Return 0 otherwise.
	 * @param sourceMimetype String
	 * @param targetMimetype String
	 * @return double
	 */
	public double getReliability(String sourceMimetype, String targetMimetype) {
		return targetMimetype.equals(AlertingConstants.MIME_TYPE_DELIVERY_REPOSITORY) ? 0.8 : 0.0;
	}

  /**
   * Method isTransformable.
   * @param sourceMimetype String
   * @param targetMimetype String
   * @param options TransformationOptions
   * @return boolean
   * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(String, String, TransformationOptions)
   */
  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options) {
    return targetMimetype.equals(AlertingConstants.MIME_TYPE_DELIVERY_REPOSITORY) ? true : false;
  }

	/**
	 * Method transformAlert.
	 * @param alert Alert
	 * @param events List<Event>
	 * @param writer ContentWriter
	 * @param options Map<String,Object>
	 */
	@Override
	protected void transformAlert(Alert alert, List<Event> events,
			ContentWriter writer, Map<String, Object> options) {
		StringBuffer html = new StringBuffer();
		if (events.size() == 0) {
			writer.putContent("");
			return;
		}
		html.append("<TABLE BORDER=\"1\">");
		html.append("<TR><TH>Resource</TH><TH>Change Type</TH><TH>Caused by</TH><TH>Date/Time</TH></TR>");
		for (Event event : events) {
			html.append("<TR>");
			if (event.getResourceURL() != null) {
				html.append("<TD><A HREF=\"").append(event.getResourceURL().toExternalForm()).append("\">").append(event.getResourceName()).append("</A></TD>");
			} else {
				html.append("<TD>").append(event.getResourceName()).append("</TD>");
			}
			html.append("<TD>").append(event.getChangeType()).append("</TD>");
			html.append("<TD>").append(event.getEventPerpetrator().getAccountId()).append("</TD>");
			SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
			String eventDateString = format.format(event.getEventTime());
			html.append("<TD>").append(eventDateString).append("</TD>");
			html.append("</TR>");
		}
		html.append("</TABLE>");
		writer.putContent(html.toString());
		
	}

}
