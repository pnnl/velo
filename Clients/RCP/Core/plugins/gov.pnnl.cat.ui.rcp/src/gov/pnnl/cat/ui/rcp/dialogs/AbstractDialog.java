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
package gov.pnnl.cat.ui.rcp.dialogs;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


/**
 */
public abstract class AbstractDialog {

  // we only want one dialog open at a time
  protected static Object lock = new Object();

  /**
   * Method getShell.
   * @param control Control
   * @return Shell
   */
  public static Shell getShell(Control control){
    if(control != null){
      return control.getShell();
      
    } else {
      Display display = Display.getCurrent();
      if (display == null) {
        display = Display.getDefault();
      }
 
      if(display.getActiveShell() != null){
        return display.getActiveShell();
      } else{
        return ((Shell[])(display.getShells()))[0];
      }          
    }   
  }
}
