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
 * A simple interface for handling a queue of events.  This queue can
 * be used by the MessageDigester implementations to quete messages
 * that aew awaiting digesting.
 * 
 * NOTE: Specific implementations of this class may not be synchronized.
 * The add and clear methods should be synchronized externally to prevent
 * errors.  getQueuedEvents(true) should only clear events from the queue
 * that were returned.
 * @author d3g574
 *
 * @version $Revision: 1.0 $
 */
public interface EventQueue {

	/**
	 * Given a list of new events, add them to the queue
	 * @param newList Events to be added to the queue.
	 */
	public void addRepositoryEventList(RepositoryEventList newList);

	/**
	 * Return a list of events that have been queued, optionally 
	 * removing them after returning them.  If clearQueue is true
	 * then only events returned by this method will be cleared
	 * from the queue.  New events added by another thread during this
	 * method invocation will remain in the queue.
	 * @param clearQueue Delete all returned entries from the queue
	
	 * @return RepositoryEventList
	 */
	public RepositoryEventList getQueuedEvents(boolean clearQueue);

	/**
	 * Clear all events from the queue.
	 */
	public void clearQueuedEvents();

}
