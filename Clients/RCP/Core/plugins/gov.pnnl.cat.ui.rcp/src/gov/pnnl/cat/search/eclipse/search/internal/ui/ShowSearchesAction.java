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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ListDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;

/**
 * Invoke the resource creation wizard selection Wizard.
 * This action will retarget to the active view.
 * @version $Revision: 1.0 $
 */
class ShowSearchesAction extends Action {

	/**
	 */
	private static final class SearchesLabelProvider extends LabelProvider {
		
		private ArrayList fImages= new ArrayList();
		
		/**
		 * Method getText.
		 * @param element Object
		 * @return String
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			if (!(element instanceof ShowSearchAction))
				return ""; //$NON-NLS-1$
			return ((ShowSearchAction)element).getText();
		}
		/**
		 * Method getImage.
		 * @param element Object
		 * @return Image
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
		 */
		public Image getImage(Object element) {
			if (!(element instanceof ShowSearchAction))
				return null;

			ImageDescriptor imageDescriptor= ((ShowSearchAction)element).getImageDescriptor(); 
			if (imageDescriptor == null)
				return null;
			
			Image image= imageDescriptor.createImage();
			fImages.add(image);

			return image;
		}
		
		/**
		 * Method dispose.
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			Iterator iter= fImages.iterator();
			while (iter.hasNext())
				((Image)iter.next()).dispose();
			
			fImages= null;
		}
	}

	/**
	 *	Create a new instance of this class
	 */
	public ShowSearchesAction() {
		super(SearchMessages.ShowOtherSearchesAction_label); 
		setToolTipText(SearchMessages.ShowOtherSearchesAction_tooltip); 
	}
	/*
	 * Overrides method from Action
	 */
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		run(false);
	}
	 
	/**
	 * Method run.
	 * @param showAll boolean
	 */
	public void run(boolean showAll) {
		Iterator iter= SearchManager.getDefault().getPreviousSearches().iterator();
		int cutOffSize;
		if (showAll)
			cutOffSize= 0;
		else
			cutOffSize= SearchDropDownAction.RESULTS_IN_DROP_DOWN;
		int size= SearchManager.getDefault().getPreviousSearches().size() - cutOffSize;
		Search selectedSearch= SearchManager.getDefault().getCurrentSearch();
		Action selectedAction = null;
		ArrayList input= new ArrayList(size);
		int i= 0;
		while (iter.hasNext()) {
			Search search= (Search)iter.next();
			if (i++ < cutOffSize)
				continue;
			Action action= new ShowSearchAction(search);
			input.add(action);
			if (selectedSearch == search)
				selectedAction= action;
		}

		// Open a list dialog.
		String title;
		String message;
		if (showAll) {
			title= SearchMessages.PreviousSearchesDialog_title; 
			message= SearchMessages.PreviousSearchesDialog_message; 
		}
		else {
			title= SearchMessages.OtherSearchesDialog_title; 
			message= SearchMessages.OtherSearchesDialog_message; 
		}
		
		LabelProvider labelProvider=new SearchesLabelProvider();

		ListDialog dlg= new ListDialog(CatSearchPlugin.getActiveWorkbenchShell(),input, title, message, new SearchResultContentProvider(), labelProvider);
		if (selectedAction != null) {
			Object[] selected= new Object[1];
			selected[0]= selectedAction;
			dlg.setInitialSelections(selected);
		}
		if (dlg.open() == Window.OK) {
			List result= Arrays.asList(dlg.getResult());
			if (result != null && result.size() == 1) {
				((ShowSearchAction)result.get(0)).run();
			}
		}
	}
}
