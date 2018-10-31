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
package gov.pnnl.cat.policy.notifiable.message;

import gov.pnnl.cat.jms.common.MessagePayload;


/**
 * The actual payload of Repository Event messages
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class RepositoryEventMessage extends MessagePayload {
	
	public static final String JMSPROP_DMI_NODEPATH = "dmiNodepath";
	private static final String JMSPROP_EVENT_PERPETRATOR = "dmiEventPerpetrator";
	private static final String JSMPROP_EVENT_TIME_MILLIS = "dmiEventTimeMillis";
	
	private RepositoryEventList events;

	/**
	 * Method getEvents.
	 * @return RepositoryEventList
	 */
	public RepositoryEventList getEvents() {
		return events;
	}

	/**
	 * Method setEvents.
	 * @param events RepositoryEventList
	 */
	public void setEvents(RepositoryEventList events) {
		this.events = events;
	}
	/*
	public String getEventPerpetrator() {
		String eventPerpetrator = getProperties().get(JMSPROP_EVENT_PERPETRATOR);
		if (eventPerpetrator == null) {
			return ("Unknown");
		}
		return eventPerpetrator;		
	}
	
	public void setEventPerpetrator(String perpetrator) {
		setProperty(JMSPROP_EVENT_PERPETRATOR, perpetrator);
	}
	
	public void setEventDateTime(String dateTimeString) {
		setProperty(JSMPROP_EVENT_TIME_MILLIS, dateTimeString);	
	}
	
	public void setEventDateTime(long dateTime) {
		setProperty(JSMPROP_EVENT_TIME_MILLIS, "" + dateTime);		
	}
	
	public String getEventDateTime() {
		String eventDateTime = getProperties().get(JSMPROP_EVENT_TIME_MILLIS);
		if (eventDateTime == null) {
			return ("" + System.currentTimeMillis());
		}
		return eventDateTime;
	}*/
	
	/**
	 * Method getNodePath.
	 * @return String
	 */
	public String getNodePath() {
		String nodePath = getProperties().get(JMSPROP_DMI_NODEPATH);
		return nodePath;
	}
	
	/**
	 * Method setNodePath.
	 * @param nodePath String
	 */
	public void setNodePath(String nodePath) {
		setProperty(JMSPROP_DMI_NODEPATH, nodePath);
	}
	
}
