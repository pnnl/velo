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
 * Copyright (c) 2006-2010 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author: welker $'
 * '$Date: 2010-05-05 22:21:26 -0700 (Wed, 05 May 2010) $' 
 * '$Revision: 24234 $'
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

package org.kepler.ssh;

//////////////////////////////////////////////////////////////////////////
//// PortEvent

/**
 * An event that is published by SSH package whenever - a session is opened or
 * closed
 * 
 * @author Norbert Podhorszki
 * @version $Id: SshEvent.java 24234 2010-05-06 05:21:26Z welker $
 */
public class SshEvent {
	/**
	 * Create a new ssh event with the given parameters. This constructor is
	 * used when an ssh activity is performed.
	 * 
	 * @param event
	 *            The type of event.
	 * @param target
	 *            The remote target involved.
	 */
	public SshEvent(int event, String target) {
		_event = event;
		_target = target;
		if (_event < EVENT_BOTTOM)
			event = EVENT_BOTTOM; // should be an exception
		if (_event > EVENT_TOP)
			event = EVENT_TOP; // should be an exception
	}

	// /////////////////////////////////////////////////////////////////
	// // public methods ////

	/**
	 * Return the target as string.
	 * 
	 * @return The target.
	 */
	public String getTarget() {
		return _target;
	}

	/**
	 * Return the type of event.
	 * 
	 * @return The int event.
	 */
	public int getEvent() {
		return _event;
	}

	/**
	 * Return a string representation of this event.
	 * 
	 * @return A user-readable string describing the event.
	 */
	public String toString() {
		return new String("SshEvent for target " + _target + ": "
				+ _eventNames[_event - EVENT_BOTTOM]);
	}

	// event min value
	private static int EVENT_BOTTOM = 1;
	// event type: a session is opened
	public static int SESSION_OPENED = 1;
	// event type: a session is closed
	public static int SESSION_CLOSED = 2;
	// event max value
	private static int EVENT_TOP = 2;

	private String[] _eventNames = { "Session opened", "Session closed" };

	// /////////////////////////////////////////////////////////////////
	// // private variables ////

	// The target
	private String _target;

	// The event type
	private int _event;

}