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

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 */
public class CleanResizeListener extends ControlAdapter {
    private Rectangle oldRect = null;
    /**
     * Method controlResized.
     * @param e ControlEvent
     * @see org.eclipse.swt.events.ControlListener#controlResized(ControlEvent)
     */
    public void controlResized(ControlEvent e) {
        assert e != null;
        assert Display.getCurrent() != null;     // On SWT event thread
        
        // Prevent garbage from Swing lags during resize. Fill exposed areas 
        // with background color. 
        Composite composite = (Composite)e.widget;
        Rectangle newRect = composite.getClientArea();
        if (oldRect != null) {
            int heightDelta = newRect.height - oldRect.height;
            int widthDelta = newRect.width - oldRect.width;
            if ((heightDelta > 0) || (widthDelta > 0)) {
                GC gc = new GC(composite);
                try {
                    gc.fillRectangle(newRect.x, oldRect.height, newRect.width, heightDelta);
                    gc.fillRectangle(oldRect.width, newRect.y, widthDelta, newRect.height);
                } finally {
                    gc.dispose();
                }
            }
        }
        oldRect = newRect;
    }
}
