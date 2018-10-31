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
package gov.pnnl.cat.ui.rcp.wizards;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

/**
 * This class provides a simulation of progress. This is useful
 * for situations where computing the amount of work to do in advance
 * is too costly.  The monitor will accept any number of calls to
 * {@link #worked(int)}, and will scale the actual reported work appropriately
 * so that the progress never quite completes.
 * @version $Revision: 1.0 $
 */
public class InfiniteProgress extends ProgressMonitorWrapper {
	/*
	 * Fields for progress monitoring algorithm.
	 * Initially, give progress for every 4 resources, double
	 * this value at halfway point, then reset halfway point
	 * to be half of remaining work.  (this gives an infinite
	 * series that converges at total work after an infinite
	 * number of resources).
	 */
	private int totalWork;
	private int currentIncrement = 4;
	private int halfWay;
	private int nextProgress = currentIncrement;
	private int worked = 0;

	/**
	 * Constructor for InfiniteProgress.
	 * @param monitor IProgressMonitor
	 */
	public InfiniteProgress(IProgressMonitor monitor) {
		super(monitor);
	}

	/**
	 * Method beginTask.
	 * @param name String
	 * @param work int
	 */
	public void beginTask(String name, int work) {
		super.beginTask(name, work);
		this.totalWork = work;
		this.halfWay = totalWork / 2;
	}

	/**
	 * Method worked.
	 * @param work int
	 */
	public void worked(int work) {
		if (--nextProgress <= 0) {
			//we have exhausted the current increment, so report progress
			super.worked(1);
			worked++;
			if (worked >= halfWay) {
				//we have passed the current halfway point, so double the
				//increment and reset the halfway point.
				currentIncrement *= 2;
				halfWay += (totalWork - halfWay) / 2;
			}
			//reset the progress counter to another full increment
			nextProgress = currentIncrement;
		}
	}

}
