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

import java.util.EventListener;

/**
 * A listener that is notified on events related to 
 * the size of the embedded Swing control 
 * @version $Revision: 1.0 $
 */
public interface SizeListener extends EventListener {
    /**
     * See {@link SwingControl#preferredSizeChanged(org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Point, org.eclipse.swt.graphics.Point)}
     * for information on when this method is called. 
     * 
     * @param event
     */
    void preferredSizeChanged(SizeEvent event);
}
