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

import java.util.EventObject;

import org.eclipse.swt.graphics.Point;

/**
 * Instances of this class are sent as a result of
 * size changes for an embedded Swing control
 *
 * @see SizeListener
 * @version $Revision: 1.0 $
 */
public class SizeEvent extends EventObject {
    /** the minimum size for this control, as reported by AWT. */
    public final Point minimum;
    /** the preferred size for this control, as reported by AWT. */
    public final Point preferred;
    /** the maximum size for this control, as reported by AWT. */
    public final Point maximum;
    
    /**
     * Constructor for SizeEvent.
     * @param source SwingControl
     * @param min Point
     * @param pref Point
     * @param max Point
     */
    public SizeEvent(SwingControl source, Point min, Point pref, Point max) {
        super(source);
        minimum = min;
        preferred = pref;
        maximum = max;
    }
    
    /**
     * Returns the SwingControl that is the source of this event
    
     * @return SwingControl  */
    public SwingControl getSwingControl() {
        return (SwingControl)source;
    }
    
    /**
     * Returns the name of the event. This is the name of
     * the class without the package name.
     *
    
     * @return the name of the event */
    protected String getName () {
        String string = getClass ().getName ();
        int index = string.lastIndexOf ('.');
        if (index == -1) return string;
        return string.substring (index + 1, string.length ());
    }

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
    
     * @return a string representation of the event */
    public String toString() {
        return getName ()
            + "{" + source //$NON-NLS-1$
            + " min=" + minimum //$NON-NLS-1$
            + " pref=" + preferred //$NON-NLS-1$
            + " max=" + maximum //$NON-NLS-1$
            + "}"; //$NON-NLS-1$
    }}
