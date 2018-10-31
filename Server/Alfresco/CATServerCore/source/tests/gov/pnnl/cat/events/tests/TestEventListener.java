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
package gov.pnnl.cat.events.tests;

import gov.pnnl.cat.jms.common.IEventMessageListener;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;

import java.util.ArrayList;
import java.util.List;


public class TestEventListener implements IEventMessageListener {
	
	private ArrayList list;
	
	public List getList() {
		return list;
	}

	public void handleEventMessage(RepositoryEventMessage message) {
		if (list == null) {
			list = new ArrayList();
		}
		list.add(message);
	}

}
