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
package gov.pnnl.cat.policy.notifiable.message;

import java.util.Iterator;

/**
 */
@SuppressWarnings("unchecked")
public class RepositoryEventIterator implements Iterator {
	private Iterator iterator;

	/**
	 * Constructor for RepositoryEventIterator.
	 * @param i Iterator
	 */
	public RepositoryEventIterator(Iterator i) {
		this.iterator = i;
	}

	/**
	 * Method hasNext.
	 * @return boolean
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return iterator.hasNext();
	}

	/**
	 * Method next.
	 * @return Object
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		return iterator.next();
	}

	/**
	 * Method nextEvent.
	 * @return RepositoryEvent
	 */
	public RepositoryEvent nextEvent() {
		return (RepositoryEvent)iterator.next();
	}

	/**
	 * Method remove.
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		iterator.remove();
	}

}
