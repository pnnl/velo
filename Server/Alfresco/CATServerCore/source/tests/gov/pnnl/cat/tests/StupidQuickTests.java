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
package gov.pnnl.cat.tests;

import org.alfresco.service.cmr.repository.datatype.Duration;


public class StupidQuickTests {

	
	public void junk(){
		String oneHour = "PT1H";
		String oneYear = "P1Y";
		String fiveYear = "P5Y";
		String thirtyDays = "P30D";
		String ninetyDays = "P90D";
		Duration duration = new Duration(oneHour);
		System.out.println("one hour "+ duration.formattedString());
		duration = new Duration(oneYear);
		System.out.println("oneYear "+ duration.formattedString());
		duration = new Duration(fiveYear);
		System.out.println("fiveYear "+ duration.formattedString());
		duration = new Duration(thirtyDays);
		System.out.println("thirtyDays "+ duration.formattedString());
		duration = new Duration(ninetyDays);
		System.out.println("ninetyDays "+ duration.formattedString());
//		 Date expiryDate = Duration.add(new Date(), duration);
	}
	
}
