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
package gov.pnnl.cat.ui.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.notification.NotifierDialog;

/**
 * This class needs to be merged with RCPUtil once we merge
 * gov.pnnl.cat.ui.rcp with gov.pnnl.cat.ui
 */
public class CatUIUtil {
  
  private static Logger logger = CatLogger.getLogger(CatUIUtil.class);
  
  /**
   * Method startWith.
   * @param file File
   * @throws IOException
   */
  public static void startWith(File file) throws IOException {
    BufferedReader r = new BufferedReader(new FileReader(file));
    String perspectiveId = r.readLine();
    String viewId = r.readLine();
    Object selectedItem = r.readLine();

    openView(perspectiveId, false, viewId, selectedItem);
  }
  
  /**
   * Will attempt to change perspective, show a view, and select an item in that view.
   * 
   * @param perspectiveId
   * @param viewId
   * @param selectedItem
   */
  public static void openView(String perspectiveId, boolean newWindow, String viewId, Object selectedItem) {

    try {
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      // open in new window
      if(newWindow){
        window = PlatformUI.getWorkbench().openWorkbenchWindow(perspectiveId, null);
        window.getShell().forceActive();
      
      }

      IWorkbenchPage page = window.getActivePage();
      if (perspectiveId != null) {
        IPerspectiveDescriptor descriptor = PlatformUI.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(perspectiveId);
        page.setPerspective(descriptor);
      }

      if (viewId != null) {
        IViewPart showView = page.showView(viewId);

        if (selectedItem != null) {
          if (showView instanceof ISelectionProvider) {
            ISelectionProvider p = (ISelectionProvider) showView;
            p.setSelection(new StructuredSelection(selectedItem));
          }
        }
        showView.setFocus();
      }
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Displays a notification dialog in the bottom right corner of the application
   * 
   * @param control The current active control that the notification dialog will run in sync with. So you don't 
   * get an Invalid Thread access.
   * @param title The title of the notification 
   * @param message The message for the notification
   */
  public static void showNotificationMessage(Control control,final String title, final String message){
	  control.getDisplay().syncExec(new Runnable() {
			public void run() {
				NotifierDialog.notify(title, message);
			}
		});
  }
  
  /**
   * Reads the contents from the InputStreamReader
   * @param input InputStreamReader
   * @param maxSize Maximum buffer size
  
  
   * @return a StringBuffer with the contents of the input * @throws IOException */
  public static StringBuffer readStreamFully(InputStreamReader input, int maxSize) throws IOException {
	    BufferedReader bufReader = null;
	    StringBuffer buf = new StringBuffer();

	    try {
	      final int BUF_SIZE = 10485760;
	      bufReader = new BufferedReader(input);
	      char[] buffer = new char[BUF_SIZE];
	      int charsRead = -1;
	      int charsToRead = Math.min(BUF_SIZE, maxSize);

	      while ((charsRead = bufReader.read(buffer, 0, charsToRead)) != -1 && charsToRead > 0) {
	        buf.append(new String(buffer, 0, charsRead));
	        charsToRead = Math.min(BUF_SIZE, maxSize - buf.length());
	      }

	      return buf;
	    } finally {
	      if (bufReader != null) {
	        try {
	          bufReader.close();
	        } catch (IOException e) {}
	      }
	    }
	  }
}
