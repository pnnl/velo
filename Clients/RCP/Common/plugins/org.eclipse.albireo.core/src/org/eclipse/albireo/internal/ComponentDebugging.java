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
package org.eclipse.albireo.internal;

import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * This class contains utility functions for debugging issues at Component
 * level, relating to the SWT_AWT bridge.
 * @version $Revision: 1.0 $
 */
public class ComponentDebugging {

    /**
     * Adds a listener for debugging size at Component events.
     * @param comp Component
     */
    public static void addComponentSizeDebugListeners(final Component comp) {
        comp.addComponentListener(
            new ComponentListener() {
                private void log(ComponentEvent event) {
                    System.err.println("Size: "+comp.getWidth()+" x "+comp.getHeight()+" after "+event);
                }
                public void componentHidden(ComponentEvent event) {
                    log(event);
                }
                public void componentMoved(ComponentEvent event) {
                    log(event);
                }
                public void componentResized(ComponentEvent event) {
                    log(event);
                }
                public void componentShown(ComponentEvent event) {
                    log(event);
                }
            });
    }
}
