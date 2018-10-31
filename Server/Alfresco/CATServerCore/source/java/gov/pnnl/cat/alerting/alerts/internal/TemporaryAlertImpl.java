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
package gov.pnnl.cat.alerting.alerts.internal;

import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.TemporaryAlert;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 */
public class TemporaryAlertImpl implements TemporaryAlert {

	private Event event;
	private NodeRef nodeRef;
	
	/**
	 * Method setEvent.
	 * @param event Event
	 * @see gov.pnnl.cat.alerting.alerts.TemporaryAlert#setEvent(Event)
	 */
	public void setEvent(Event event) {
		this.event = event;
	}

	/**
	 * Method setNodeRef.
	 * @param nodeRef NodeRef
	 * @see gov.pnnl.cat.alerting.alerts.TemporaryAlert#setNodeRef(NodeRef)
	 */
	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * Method getEvent.
	 * @return Event
	 * @see gov.pnnl.cat.alerting.alerts.TemporaryAlert#getEvent()
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * Method getNodeRef.
	 * @return NodeRef
	 * @see gov.pnnl.cat.alerting.alerts.TemporaryAlert#getNodeRef()
	 */
	public NodeRef getNodeRef() {
		return nodeRef;
	}
	

}
