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

 *  SWTCalendarListener.java  - An interface for notifying for date changed

 *  Mark Bryan Yu

 *  swtcalendar.sourceforge.net

 *

 *  This program is free software; you can redistribute it and/or

 *  modify it under the terms of the GNU Lesser General Public License

 *  as published by the Free Software Foundation; either version 2

 *  of the License, or (at your option) any later version.

 *

 *  This program is distributed in the hope that it will be useful,

 *  but WITHOUT ANY WARRANTY; without even the implied warranty of

 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the

 *  GNU Lesser General Public License for more details.

 *

 *  You should have received a copy of the GNU Lesser General Public License

 *  along with this program; if not, write to the Free Software

 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package org.vafada.swtcalendar;



import org.eclipse.swt.internal.SWTEventListener;



/**
 */
public interface SWTCalendarListener extends SWTEventListener {

    /**
     * Method dateChanged.
     * @param event SWTCalendarEvent
     */
    public void dateChanged(SWTCalendarEvent event);

}

