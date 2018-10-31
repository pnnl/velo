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
package gov.pnnl.cat.search.eclipse.search2.internal.ui.text;

import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;

import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 */
public class AnnotationManagers {
	static {
//		fgManagerMap = new HashMap();
		IWindowListener listener = new IWindowListener() {
			public void windowActivated(IWorkbenchWindow window) {
				// ignore
			}

			public void windowDeactivated(IWorkbenchWindow window) {
				// ignore
			}

			public void windowClosed(IWorkbenchWindow window) {
				disposeAnnotationManager(window);
			}

			public void windowOpened(IWorkbenchWindow window) {
				// ignore
			}
		};
		PlatformUI.getWorkbench().addWindowListener(listener);
	}

//	private static HashMap fgManagerMap;


	/**
	 * Method disposeAnnotationManager.
	 * @param window IWorkbenchWindow
	 */
	private static void disposeAnnotationManager(IWorkbenchWindow window) {
/*		WindowAnnotationManager mgr = (WindowAnnotationManager) fgManagerMap.remove(window);
		if (mgr != null)
			mgr.dispose();*/
	}

	/**
	 * Method searchResultActivated.
	 * @param window IWorkbenchWindow
	 * @param result AbstractTextSearchResult
	 */
	public static void searchResultActivated(IWorkbenchWindow window, AbstractTextSearchResult result) {
/*		WindowAnnotationManager mgr= (WindowAnnotationManager) fgManagerMap.get(window);
		if (mgr == null) {
			mgr= new WindowAnnotationManager(window);
			fgManagerMap.put(window, mgr);
		}
		mgr.setSearchResult(result);*/
	}

}
