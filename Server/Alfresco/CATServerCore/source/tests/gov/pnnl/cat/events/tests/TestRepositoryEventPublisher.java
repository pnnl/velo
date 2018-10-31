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

import gov.pnnl.cat.jms.common.RepositoryEventMessageSender;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventMessage;


public class TestRepositoryEventPublisher implements Runnable {


	private RepositoryEventMessageSender sender;

	public TestRepositoryEventPublisher() {
		Thread t = new Thread(this);
		t.start();
	}

	public void setSender(RepositoryEventMessageSender sender) {
		this.sender = sender;
	}

	public void run() {
		// wait for sender to be set
		while (sender == null) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				;
			}
		}
		// publish a repository event once a second for 30 seconds
		for (int i=0; i<30; i++) {
			RepositoryEventMessage message = new RepositoryEventMessage();
			sender.sendMessage(message);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				;
			}
		}

	}

}
