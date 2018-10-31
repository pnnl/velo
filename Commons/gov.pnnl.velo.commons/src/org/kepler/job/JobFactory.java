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
/*
 * Copyright (c) 2004-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: chandrika $'
 * '$Date: 2011-01-03 18:26:26 -0800 (Mon, 03 Jan 2011) $' 
 * '$Revision: 26608 $'
 * 
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package org.kepler.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

/**
 * This class provides a factory to store Job objects. The reference to a job is
 * with a String id. This class should be used in Kepler actors instead of the
 * Job class directly, so that job id's can be sent along.
 * 
 * <p>
 * 
 * @author Norbert Podhorszki
 */

public class JobFactory {

	/* Singleton object */
	public final static JobFactory instance = new JobFactory();

	/* Private variables */
	private static Hashtable jobTable = new Hashtable();
	private static int jobIDCounter = 0;

	private JobFactory() {
		// jobTable = new Hashtable();
	}

	/**
	 * Create a new job object.
	 * 
	 * @return the unique ID for the job. Use method getJob() to get the Job
	 *         object itself.
	 */
	public static synchronized String create() {
		Job job;
		String jobID = createUniqueKey(jobIDCounter);
		job = new Job(jobID);
		jobTable.put(jobID, job);
		jobIDCounter++;
		return jobID;
	}

	/**
	 * Return an existing job.
	 * 
	
	 * @param jobID String
	 * @return the existing job or null. */
	public static Job get(String jobID) {
		return (Job) jobTable.get(jobID);
	}

	/**
	 * Remove an existing job.
   *
	 * @param jobID String
	 * @return true if such job existed, false otherwise
	 */
	public static boolean remove(String jobID) {
		if (jobTable.remove(jobID) == null)
			return false;
		else
			return true;
	}

	/**
	 * Method createUniqueKey.
	 * @param jobIDCounter int
	 * @return String
	 */
	protected synchronized static String createUniqueKey(int jobIDCounter) {
		/** return ( new String("job"+jobIDCounter) ); */
		return (new String(user + "_" + formatter.format(new Date()) + "_"
				+ jobIDCounter));
	}

	private static String user = System.getProperty("user.name", "none");
	private static SimpleDateFormat formatter = new SimpleDateFormat(
			"MMMdd_HHmmssz");
	/* Note that SimpleDateFormat objects must be externally synchronised */

}

