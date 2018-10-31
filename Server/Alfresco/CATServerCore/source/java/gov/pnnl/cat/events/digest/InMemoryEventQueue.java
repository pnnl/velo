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
package gov.pnnl.cat.events.digest;

import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;

/**
 * A simple EventQueue that stores all of the queued events in a RepositoryEventList
 * object (extends Arraylist).  All methods of this class are synchronized, to avoid 
 * concurrent modifications to the queue.
 * @author d3g574
 *
 * @version $Revision: 1.0 $
 */
public class InMemoryEventQueue implements EventQueue {
	private RepositoryEventList bundle;
	
	/* (non-Javadoc)
	 * @see gov.pnnl.cat.events.digest.EventQueue2#addRepositoryEventList(gov.pnl.dmi.policy.notifiable.message.RepositoryEventList)
	 */
	public synchronized void addRepositoryEventList(RepositoryEventList newList) {
		if (bundle == null) {
			bundle = new RepositoryEventList();
		}
		bundle.addAll(newList);
	}
	
	/* (non-Javadoc)
	 * @see gov.pnnl.cat.events.digest.EventQueue2#getQueuedEvents(boolean)
	 */
	public synchronized RepositoryEventList getQueuedEvents(boolean clearQueue) {
		if ((bundle == null) || (bundle.size() == 0)) {
			return null;
		}
		RepositoryEventList newList = new RepositoryEventList();
		newList.addAll(bundle);
		if (clearQueue) {
			bundle = null;
		}
		return newList;
	}
	
	/* (non-Javadoc)
	 * @see gov.pnnl.cat.events.digest.EventQueue2#clearQueuedEvents()
	 */
	public synchronized void clearQueuedEvents() {
		bundle = null;
	}
}
