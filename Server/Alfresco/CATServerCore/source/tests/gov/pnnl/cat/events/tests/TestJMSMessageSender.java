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

import gov.pnnl.cat.jms.common.IMessageSender;
import gov.pnnl.cat.jms.common.MessagePayload;

 
public class TestJMSMessageSender extends CATSpringJUnitTest  {


	protected String getApplicationContextXml() {
		return "config/jmsMessageSenderTest-context.xml";
	}
	
	public void testSendAsync() {
		IMessageSender testasyncsender = (IMessageSender)getBean("testAsyncMessageSender");
		
		TestRequestMessage request = new TestRequestMessage();
		
		testasyncsender.send(request, null);
		assertTrue(true);

	}

	public void testSendSyncEcho() {
		IMessageSender echosyncsender = (IMessageSender)getBean("echoSyncMessageSender");
		
		TestRequestMessage request = new TestRequestMessage();
				
		MessagePayload response = (MessagePayload)echosyncsender.sendSynchronous(request, null);
	
		assertNotNull(response);
		assertTrue(response instanceof TestRequestMessage);
		TestRequestMessage responseMsg2= (TestRequestMessage)response;
		assertTrue(responseMsg2.getMsg().contains("Test Request"));
 
	}

	public void testSendSyncTest() {
		IMessageSender testsyncsender = (IMessageSender)getBean("testSyncMessageSender");
		
		TestRequestMessage request = new TestRequestMessage();

		MessagePayload response = (MessagePayload)testsyncsender.sendSynchronous(request, null);
		
		assertNotNull(response);
		assertTrue(response instanceof TestResponseMessage);
		TestResponseMessage responseMsg2= (TestResponseMessage)response;
		assertTrue(responseMsg2.getMsg().contains("Test Response"));
		
	}
		
	

}
