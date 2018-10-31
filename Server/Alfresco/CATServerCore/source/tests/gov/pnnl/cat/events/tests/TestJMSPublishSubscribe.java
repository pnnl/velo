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

import gov.pnnl.cat.jms.common.UnmanagedListenerRegistrar;

import javax.jms.MessageConsumer;
import javax.jms.Topic;
 
public class TestJMSPublishSubscribe extends CATSpringJUnitTest  {


	protected String getApplicationContextXml() {
		return "config/jmsPublishSubscribeTest-context.xml";
	}
	
	
	public void testUnmanagedSubscribe() {
		TestMessageListener listener = new TestMessageListener();
		UnmanagedListenerRegistrar registrar = (UnmanagedListenerRegistrar)getBean("unmanagedListenerRegistrar");
		Topic source = (Topic)getBean("publishDestination");
		
		MessageConsumer consumer = registrar.newMessageListener(listener, source);

		System.out.println("Please wait... this test takes around 30 seconds....");
		try {
			Thread.sleep(20000); // we should receive 4 messages in 20 seconds
		} catch (Exception e) {
			;
		}
		int count = listener.getMessageCount();
		assertTrue(count >= 4);
		registrar.removeMessageListener(consumer);
		
		try {
			Thread.sleep(7000); // we shouldn't receive any more messages
		} catch (Exception e) {
			;
		}
		int count2 = listener.getMessageCount();

		// it is possible that one more message snuck in between the last count and the unsubscribe
		assertTrue((count2 - count) <= 1);
		
		
	}
	
	public void testManagedSubscribe() {
		// our listener was registered by Jencks
		// so get a handle to it and see how it is doing
		
		TestMessageListener listener = (TestMessageListener)getBean("testListener");
	
		int count = listener.getMessageCount();
		assertTrue(count >= 0);

		System.out.println("Please wait... this test takes around 10 seconds....");

		try {
			Thread.sleep(10000); // we should get 2 more messages in this period
		} catch (Exception e) {
			;
		}
		int count2 = listener.getMessageCount();
		assertTrue((count2 - count) == 2);


	}
	
	

}
