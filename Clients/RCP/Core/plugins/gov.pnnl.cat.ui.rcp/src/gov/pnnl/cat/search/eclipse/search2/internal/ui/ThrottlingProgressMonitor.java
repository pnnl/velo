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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ProgressMonitorWrapper;

/**
 */
public class ThrottlingProgressMonitor extends ProgressMonitorWrapper {
	private float fThrottleRatio;
	private long fLastCalled;
	private long fSubMilis;

	/**
	 * Constructor for ThrottlingProgressMonitor.
	 * @param wrapped IProgressMonitor
	 * @param throttleRatio float
	 */
	public ThrottlingProgressMonitor(IProgressMonitor wrapped, float throttleRatio) {
		super(wrapped);
		fThrottleRatio= throttleRatio;
		fSubMilis= 0;
		fLastCalled= 0;
	}

	/**
	 * Method internalWorked.
	 * @param work double
	 */
	public void internalWorked(double work) {
		super.internalWorked(work);
		if (fLastCalled != 0) {
			long sleepTime= System.currentTimeMillis()-fLastCalled;
			sleepTime *= fThrottleRatio;
			sleepTime= Math.min(100, sleepTime);
			if (sleepTime < 1) {
				fSubMilis++;
				if (fSubMilis > 50) {
					sleepTime= 1;
					fSubMilis= 0;
				}
			}
			fLastCalled= System.currentTimeMillis();
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				Thread.yield();
			}
		} else {
			fLastCalled= System.currentTimeMillis();
		}
	}
}
