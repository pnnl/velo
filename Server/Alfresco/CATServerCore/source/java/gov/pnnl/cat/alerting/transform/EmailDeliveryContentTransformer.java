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
import gov.pnnl.cat.alerting.delivery.internal.EmailUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 */
public class EmailDeliveryContentTransformer extends AbstractDeliveryContentTransformer {
	
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
	 * alert being delivered over the Email Delivery Channel.  If we ever have 
	 * a transformer that takes into consideration a specific sourceMimeType,
	 * it should return 100% to ensure it gets called over this one.
	 * Return 0 otherwise.
	 * @param sourceMimetype String
	 * @param targetMimetype String
	 * @return double
	 */
	public double getReliability(String sourceMimetype, String targetMimetype) {
		return targetMimetype.equals(AlertingConstants.MIME_TYPE_DELIVERY_EMAIL) ? 0.8 : 0.0;
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
    return targetMimetype.equals(AlertingConstants.MIME_TYPE_DELIVERY_EMAIL) ? true : false;
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
		StringBuffer body = new StringBuffer();
		if (events.size() == 0) {
			writer.putContent("");
			return;
		}

		List<Event> aboutToExpire = new ArrayList<Event>();
		List<Event> expired = new ArrayList<Event>();

		
		for (Event event : events) {
			if(!event.getChangeType().equalsIgnoreCase(AlertingConstants.CHANGE_TYPE_TAGTIMER_ABOUT_TO_EXPIRE) && 
				!event.getChangeType().equalsIgnoreCase(AlertingConstants.CHANGE_TYPE_TAGTIMER_EXPIRED)){
				body.append(event.getResourceName()).append(EmailUtils.LINEFEED);
				if (event.getResourceURL() != null) {
					body.append(event.getResourceURL().toExternalForm()).append(EmailUtils.LINEFEED);
				}
				String changeType = "Changed";
				if (event.getChangeType().equals(AlertingConstants.CHANGE_TYPE_NEW)) {
					changeType = "Created";
				} else if (event.getChangeType().equals(AlertingConstants.CHANGE_TYPE_DELETED)) {
					changeType = "Deleted";
				}
				body.append(changeType).append(" by ").append(event.getEventPerpetrator().getAccountId());
				SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				String eventDateString = format.format(event.getEventTime());
				body.append(" at ").append(eventDateString).append(EmailUtils.LINEFEED);
				body.append(EmailUtils.LINEFEED);
			}else {
				if(event.getChangeType().equalsIgnoreCase(AlertingConstants.CHANGE_TYPE_TAGTIMER_ABOUT_TO_EXPIRE)){
					aboutToExpire.add(event);
				}else{
					expired.add(event);
				}
			}
		}
		
		if(expired.size() > 0){
			body.append(EmailUtils.LINEFEED);
			body.append("Documents Expired:");
			body.append(EmailUtils.LINEFEED);
			for(Event expire : expired){
				body.append(expire.getResourceName());
				body.append(EmailUtils.LINEFEED);
			}
			body.append(EmailUtils.LINEFEED);
		}
		if(aboutToExpire.size() > 0){
			body.append(EmailUtils.LINEFEED);
			body.append("Documents About to Expire:");
			body.append(EmailUtils.LINEFEED);
			for(Event expire : aboutToExpire){
				body.append(expire.getResourceName());
				body.append(EmailUtils.LINEFEED);
			}
			body.append(EmailUtils.LINEFEED);
		}

		writer.putContent(body.toString());
		
	}



}
