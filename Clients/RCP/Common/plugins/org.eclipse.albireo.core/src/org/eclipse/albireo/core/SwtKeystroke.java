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
package org.eclipse.albireo.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Event;

/**
 * A keystroke as reported in a SWT KeyEvent. 
 * @version $Revision: 1.0 $
 */
public class SwtKeystroke {
    private final int eventType;
    private final int keyCode;
    private final int stateMask;
    
    /**
     * Constructor
     * 
     * @param eventType the SWT event type
     * @param keyCode the SWT key code
     * @param stateMask the SWT state mask
     */
    public SwtKeystroke(int eventType, int keyCode, int stateMask) {
        if ((eventType != SWT.KeyDown) && (eventType != SWT.KeyUp)) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        this.eventType = eventType;
        this.keyCode = keyCode;
        this.stateMask = stateMask;
    }
    
    /**
     * Constructor
     * 
     * @param eventType the SWT event type
     * @param e the SWT KeyEvent
     */
    public SwtKeystroke(int eventType, KeyEvent e) {
        this.eventType = eventType;
        keyCode = e.keyCode;
        stateMask = e.stateMask;
    }

    /**
     * Constructor
     * 
     * @param e the SWT Event
     */
    public SwtKeystroke(Event e) {
        eventType = e.type;
        keyCode = e.keyCode;
        stateMask = e.stateMask;
    }

    /**
     * Returns the type of SWT key event represented by this object. 
     * 
    
     * @return {@link SWT#KeyDown} or {@link SWT#KeyUp} */
    public int getEventType() {
        return eventType;
    }

    /**
     * Returns the SWT key code for this keystroke. See the 
     * constants in the {@link SWT} class for the possible
     * values.  
     * 
    
     * @return int key code */
    public int getKeyCode() {
        return keyCode;
    }

    /**
     * Returns the SWT state mask for this keystroke. See the 
     * constants in the {@link SWT} class for the possible
     * values.  
     * 
    
     * @return int state mask */
    public int getStateMask() {
        return stateMask;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + eventType;
        result = prime * result + keyCode;
        result = prime * result + stateMask;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SwtKeystroke other = (SwtKeystroke)obj;
        if (eventType != other.eventType)
            return false;
        if (keyCode != other.keyCode)
            return false;
        if (stateMask != other.stateMask)
            return false;
        return true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "key: type=" + eventType + ", code=" + keyCode + ", stateMask=" + stateMask;
    }
    

}
