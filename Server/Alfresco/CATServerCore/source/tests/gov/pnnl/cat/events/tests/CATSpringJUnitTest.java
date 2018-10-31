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

import junit.framework.TestCase;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class CATSpringJUnitTest extends TestCase {

	protected ConfigurableApplicationContext applicationContext;

	protected void setUp() throws Exception {
		applicationContext = createApplicationContext();
		assertNotNull("Should have an ApplicationContext", applicationContext);
	}

	protected void tearDown() throws Exception {
		if (applicationContext != null) {
			applicationContext.close();
		}
	}

	protected ConfigurableApplicationContext createApplicationContext() {
		return new ClassPathXmlApplicationContext(getApplicationContextXml());
	}

	protected abstract String getApplicationContextXml();

	/**
	 * Finds the mandatory bean in the application context failing if its not
	 * there
	 */
	protected Object getBean(String name) {
		Object answer = applicationContext.getBean(name);
		assertNotNull("Could not find bean in ApplicationContext called: " + name, answer);
		return answer;
	}
}

