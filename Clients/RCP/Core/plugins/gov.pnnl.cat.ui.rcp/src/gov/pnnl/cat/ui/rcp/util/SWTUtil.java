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
package gov.pnnl.cat.ui.rcp.util;


import gov.pnnl.cat.search.eclipse.search.internal.ui.util.PixelConverter;
import gov.pnnl.velo.core.util.ThreadUtils;

import java.util.concurrent.Callable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;

/**
 * Utility class to simplify access to some SWT resources. 
 * @version $Revision: 1.0 $
 */
public class SWTUtil {
  
  /**
   * Execute a callback in the SWT thread asynchronously, blocking until it returns.
   * @param cb  The callback containing the unit of work.
   * @return    Returns the result of the unit of work.
   * @throws    RuntimeException
   */
  public static <R> R blockingAsyncExec(final Callable<R> cb) {
    if(!ThreadUtils.isSWTThread()) {
      return execBlockingSwtFromSwing(cb);
    
    } else {
      return execBlockingSwtFromSwt(cb);
    }   
  }
  
  @SuppressWarnings("unchecked")
  public static <R> R execBlockingSwtFromSwt(final Callable<R> cb) {
    final Object[] result = {null};
  
    Display.getDefault().syncExec(new Runnable() {
      @Override
      public void run() {
        try {
          result[0] = cb.call();
          
        } catch(RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new RuntimeException(e);
        } 
      }

    });
    return (R) result[0];
  }
  
  @SuppressWarnings("unchecked")
  public static <R> R execBlockingSwtFromSwing(final Callable<R> cb) {
    final Object[] result = {null};
    final boolean[] done = {false};
  
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          result[0] = cb.call();
          
        } catch(RuntimeException e) {
          throw e;
        } catch (Exception e) {
          throw new RuntimeException(e);
        } finally {
          done[0] = true;
        }
      }

    });

    while (done[0] == false) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return (R) result[0];
  }
  
  public static Rectangle getMainShellDimensions() {
    Rectangle bounds = null;
    
    Display display = getStandardDisplay();
    for(Shell s : display.getShells()) {
      // find the shell for our main window
      String title = s.getText();
      if(title != null && title.contains(Platform.getProduct().getName())) {
        bounds = s.getBounds();
        break;
      }
    }
    return bounds;
  }
  
  /**
   * For a new dialog of the given size, get the location centered on the main
   * Eclipse window.
   * @param dialogWidth
   * @param dialogHeight
   * @return
   */
  public static Point getCenteredDialogBounds(int dialogWidth, int dialogHeight) {
    Point point = null;
    Rectangle mainBounds = getMainShellDimensions();
    if(mainBounds != null) {
      // center the dialog on the main shell
      int x = mainBounds.x + (mainBounds.width - dialogWidth) / 2;
      int y = mainBounds.y + (mainBounds.height - dialogHeight) / 2;
      point = new Point(x, y);    
    }
    return point;
  }
  
  /**
   * If you are popping up a new SWT dialog that will go in front of a swing UI, then
   * you have to create a new shell in order to make sure it goes on top.
   * This method will create a new, centered, on top shell for making a popup dialog.
   * Callers are responsible for disposing of the returned shell.
   * @return
   */
  public static Shell getCenteredDialogShell() {
    return getCenteredDialogShell(400, 600);
  }
  
  /**
   * If you are popping up a new SWT dialog that will go in front of a swing UI, then
   * you have to create a new shell in order to make sure it goes on top.
   * This method will create a new, centered, on top shell for making a popup dialog.
   * Callers are responsible for disposing of the returned shell.
   * @return
   */
  public static Shell getCenteredDialogShell(int width, int height) {
    Shell newShell = new Shell(SWT.ON_TOP);
    newShell.setSize(width, height);
    newShell.forceActive();
    Rectangle newShellBounds = newShell.getBounds();

    Point point = getCenteredDialogBounds(newShellBounds.width, newShellBounds.height);
    if(point != null) {
      newShell.setLocation(point);          
    }
    return newShell;      
  }
  
	
	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated disaply. If so, this
	 * display is returned. Otherwise the method returns the default display.
	
	 * @return Returns the standard display to be used. */
	public static Display getStandardDisplay() {
		Display display;
		display= Display.getCurrent();
		if (display == null)
			display= Display.getDefault();
		return display;		
	}
	
	/**
	 * Returns the shell for the given widget. If the widget doesn't represent
	 * a SWT object that manage a shell, <code>null</code> is returned.
	 *
	 * @param widget The widget to get the shell for
	
	 * @return the shell for the given widget */
	public static Shell getShell(Widget widget) {
		if (widget instanceof Control)
			return ((Control)widget).getShell();
		if (widget instanceof Caret)
			return ((Caret)widget).getParent().getShell();
		if (widget instanceof DragSource)
			return ((DragSource)widget).getControl().getShell();
		if (widget instanceof DropTarget)
			return ((DropTarget)widget).getControl().getShell();
		if (widget instanceof Menu)
			return ((Menu)widget).getParent().getShell();
		if (widget instanceof ScrollBar)
			return ((ScrollBar)widget).getParent().getShell();
							
		return null;	
	}


	/**
	 * Returns a width hint for a button control.
	 * @param button The button to calculate the width for
	
	 * @return The width of the button */
	public static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	/**
	 * Sets width and height hint for the button control.
	 * <b>Note:</b> This is a NOP if the button's layout data is not
	 * an instance of <code>GridData</code>.
	 * 
	 * @param button	the button for which to set the dimension hint
	 */		
	public static void setButtonDimensionHint(Button button) {
	  assert(button != null);
		Object gd= button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint= getButtonWidthHint(button);	
			((GridData)gd).horizontalAlignment = GridData.FILL;	 
		}
	}
	
	/**
	 * Method getTableHeightHint.
	 * @param table Table
	 * @param rows int
	 * @return int
	 */
	public static int getTableHeightHint(Table table, int rows) {
		if (table.getFont().equals(JFaceResources.getDefaultFont()))
			table.setFont(JFaceResources.getDialogFont());
		int result= table.getItemHeight() * rows + table.getHeaderHeight();
		if (table.getLinesVisible())
			result+= table.getGridLineWidth() * (rows - 1);
		return result;		
	}

}
