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

import java.awt.Toolkit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;



/**
 * Converter of resources from AWT/Swing to SWT and vice versa. 
 * @version $Revision: 1.0 $
 */
public class ResourceConverter {

    /**
     * Converts a color from SWT to Swing.
     * The argument Color remains owned by the caller.
     * @param c Color
     * @return java.awt.Color
     */
    public java.awt.Color convertColor(Color c) {
        return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Converts a font from SWT to Swing.
     * The argument Font remains owned by the caller.
     * @param swtFont An SWT font.
     * @param swtFontData Result of <code>swtFont.getFontData()</code>,
     *                    obtained on the SWT event thread.
     * @return java.awt.Font
     */
    public java.awt.Font convertFont(Font swtFont, FontData[] swtFontData) {
        FontData fontData0 = swtFontData[0];

        // AWT font sizes assume a 72 dpi resolution, always. The true screen resolution must be
        // used to convert the platform font size into an AWT point size that matches when displayed.
        int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        int awtFontSize = (int)Math.round((double)fontData0.getHeight() * resolution / 72.0);

        // The style constants for SWT and AWT map exactly, and since they are int constants, they should
        // never change. So, the SWT style is passed through as the AWT style.
        return new java.awt.Font(fontData0.getName(), fontData0.getStyle(), awtFontSize);
    }


    // ========================================================================
    // Singleton design pattern

    private static ResourceConverter theInstance = new ResourceConverter();

    /**
     * Returns the currently active singleton of this class.
     * @return ResourceConverter
     */
    public static ResourceConverter getInstance() {
        return theInstance;
    }

    /**
     * Replaces the singleton of this class.
     * @param instance An instance of this class or of a customized subclass.
     */
    public static void setInstance(ResourceConverter instance) {
        theInstance = instance;
    }

}
