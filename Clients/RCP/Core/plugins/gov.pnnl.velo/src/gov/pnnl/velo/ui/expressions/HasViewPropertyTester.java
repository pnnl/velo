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
/**
 * 
 */
package gov.pnnl.velo.ui.expressions;

import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author D3K339
 * 
 * @version $Revision: 1.0 $
 */
public class HasViewPropertyTester extends PropertyTester {
	private static final Logger logger = CatLogger
			.getLogger(HasAspectPropertyTester.class);
	
	private String leftPaneViewVisible;

	/**
   * 
   */
	public HasViewPropertyTester() {
		leftPaneViewVisible = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		IStructuredSelection selection;
		boolean retValue = ((Boolean) expectedValue).booleanValue();

		// Get the current selection
		if (!(receiver instanceof IStructuredSelection)) {
			return !retValue;
		}

		try {
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();
			final IWorkbenchPart activeView = page.getActivePart();

			String view_arg = (String) args[0];
			String current_view = activeView.getTitle();
			
			if(current_view.equals("Registry") || current_view.equals("My Workspace")){
				leftPaneViewVisible = current_view;
			} else {
				current_view = leftPaneViewVisible;
			}

			boolean ret = true;
			if (current_view.equals(view_arg)) {
				ret = retValue;
			} else {
				ret = !retValue;
			}
			logger.debug("Returning " + ret);
			return ret;
		} catch (Throwable e) {
			logger.error("Could not determine view.", e);
		}

		return false;
	}

}
