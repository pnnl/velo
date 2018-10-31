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
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExceptionHandler;
import gov.pnnl.cat.search.eclipse.search.ui.IActionGroupFactory;
import gov.pnnl.cat.search.eclipse.search.ui.IContextMenuContributor;
import gov.pnnl.cat.search.eclipse.search.ui.IGroupByKeyComputer;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class Search extends Object {
	private String fPageId;
	private String fSingularLabel;
	private String fPluralLabelPattern;
	private ImageDescriptor fImageDescriptor;
	private ILabelProvider fLabelProvider;
	private ISelection fSelection;
	private ArrayList fResults;
	private IAction fGotoMarkerAction;
	private IContextMenuContributor fContextMenuContributor;
	private IActionGroupFactory fActionGroupFactory;
	private IGroupByKeyComputer	fGroupByKeyComputer;
	private IRunnableWithProgress fOperation;


	/**
	 * Constructor for Search.
	 * @param pageId String
	 * @param singularLabel String
	 * @param pluralLabelPattern String
	 * @param labelProvider ILabelProvider
	 * @param imageDescriptor ImageDescriptor
	 * @param gotoMarkerAction IAction
	 * @param groupFactory IActionGroupFactory
	 * @param groupByKeyComputer IGroupByKeyComputer
	 * @param operation IRunnableWithProgress
	 */
	public Search(String pageId, String singularLabel, String pluralLabelPattern, ILabelProvider labelProvider, ImageDescriptor imageDescriptor, IAction gotoMarkerAction, IActionGroupFactory groupFactory, IGroupByKeyComputer groupByKeyComputer, IRunnableWithProgress operation) {
		fPageId= pageId;
		fSingularLabel= singularLabel;
		fPluralLabelPattern= pluralLabelPattern;
		fImageDescriptor= imageDescriptor;
		fLabelProvider= labelProvider;
		fGotoMarkerAction= gotoMarkerAction;
		fActionGroupFactory= groupFactory;
		fGroupByKeyComputer= groupByKeyComputer;
		fOperation= operation;
		
		if (fPluralLabelPattern == null)
			fPluralLabelPattern= ""; //$NON-NLS-1$
	}

	/**
	 * Constructor for Search.
	 * @param pageId String
	 * @param singularLabel String
	 * @param pluralLabelPattern String
	 * @param labelProvider ILabelProvider
	 * @param imageDescriptor ImageDescriptor
	 * @param gotoMarkerAction IAction
	 * @param contextMenuContributor IContextMenuContributor
	 * @param groupByKeyComputer IGroupByKeyComputer
	 * @param operation IRunnableWithProgress
	 */
	public Search(String pageId, String singularLabel, String pluralLabelPattern, ILabelProvider labelProvider, ImageDescriptor imageDescriptor, IAction gotoMarkerAction, IContextMenuContributor contextMenuContributor, IGroupByKeyComputer groupByKeyComputer, IRunnableWithProgress operation) {
		fPageId= pageId;
		fSingularLabel= singularLabel;
		fPluralLabelPattern= pluralLabelPattern;
		fImageDescriptor= imageDescriptor;
		fLabelProvider= labelProvider;
		fGotoMarkerAction= gotoMarkerAction;
		fContextMenuContributor= contextMenuContributor;
		fGroupByKeyComputer= groupByKeyComputer;
		fOperation= operation;
		
		if (fPluralLabelPattern == null)
			fPluralLabelPattern= ""; //$NON-NLS-1$
	}

	/**
	 * Returns the full description of the search.
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 * @return String
	 */
	String getFullDescription() {
		if (fSingularLabel != null && getItemCount() == 1)
			return fSingularLabel;

		// try to replace "{0}" with the match count
		int i= fPluralLabelPattern.lastIndexOf("{0}"); //$NON-NLS-1$
		if (i < 0)
			return fPluralLabelPattern;
		else
			return fPluralLabelPattern.substring(0, i) + getItemCount()+ fPluralLabelPattern.substring(Math.min(i + 3, fPluralLabelPattern.length()));
	}

	/**
	 * Returns a short description of the search.
	 * Cuts off after 30 characters and adds ...
	 * The description set by the client where
	 * {0} will be replaced by the match count.
	 * @return String
	 */
	String getShortDescription() {
		String text= getFullDescription();
		int separatorPos= text.indexOf(" - "); //$NON-NLS-1$
		if (separatorPos < 1)
			return text.substring(0, Math.min(50, text.length())) + "..."; // use first 50 characters //$NON-NLS-1$
		if (separatorPos < 30)
			return text;	// don't cut
		if (text.charAt(0) == '"')  //$NON-NLS-1$
			return text.substring(0, Math.min(30, text.length())) + "...\" - " + text.substring(Math.min(separatorPos + 3, text.length())); //$NON-NLS-1$
		else
			return text.substring(0, Math.min(30, text.length())) + "... - " + text.substring(Math.min(separatorPos + 3, text.length())); //$NON-NLS-1$
	}
	/** Image used when search is displayed in a list * @return ImageDescriptor
	 */
	ImageDescriptor getImageDescriptor() {
		return fImageDescriptor;
	}

	/**
	 * Method getItemCount.
	 * @return int
	 */
	int getItemCount() {
		int count= 0;
		Iterator iter= getResults().iterator();
		while (iter.hasNext())
			count += ((ISearchResultViewEntry)iter.next()).getMatchCount();
		return count;
	}

	/**
	 * Method getResults.
	 * @return List
	 */
	List getResults() {
		if (fResults == null)
			return new ArrayList();
		return fResults;
	}

	/**
	 * Method getLabelProvider.
	 * @return ILabelProvider
	 */
	ILabelProvider getLabelProvider() {
		return fLabelProvider;
	}

	void searchAgain() {
		if (fOperation == null)
			return;
		Shell shell= CatSearchPlugin.getActiveWorkbenchShell();
		
		try {
			new ProgressMonitorDialog(shell).run(true, true, fOperation);
		} catch (InvocationTargetException ex) {
			ExceptionHandler.handle(ex, shell, SearchMessages.Search_Error_search_title, SearchMessages.Search_Error_search_message); 
		} catch(InterruptedException e) {
		} 
	}
	
	/**
	 * Method isSameSearch.
	 * @param search Search
	 * @return boolean
	 */
	boolean isSameSearch(Search search) {
		return search != null && search.getOperation() == fOperation && fOperation != null;
	}
	
	void backupMarkers() {
		Iterator iter= getResults().iterator();
		while (iter.hasNext()) {
			((SearchResultViewEntry)iter.next()).backupMarkers();
		}
	}

	/**
	 * Method getPageId.
	 * @return String
	 */
	public String getPageId() {
		return fPageId;
	}
	
	/**
	 * Method getGroupByKeyComputer.
	 * @return IGroupByKeyComputer
	 */
	IGroupByKeyComputer getGroupByKeyComputer() {
		return fGroupByKeyComputer;
	}

	/**
	 * Method getOperation.
	 * @return IRunnableWithProgress
	 */
	public IRunnableWithProgress getOperation() {
		return fOperation;
	}

	/**
	 * Method getGotoMarkerAction.
	 * @return IAction
	 */
	IAction getGotoMarkerAction() {
		return fGotoMarkerAction;
	}

	/**
	 * Method getContextMenuContributor.
	 * @return IContextMenuContributor
	 */
	IContextMenuContributor getContextMenuContributor() {
		return fContextMenuContributor;
	}
	
	/**
	 * Method getActionGroupFactory.
	 * @return IActionGroupFactory
	 */
	IActionGroupFactory getActionGroupFactory() {
		return fActionGroupFactory;
	}
	
	public void removeResults() {
		fResults= null;
	}
	
	/**
	 * Method setResults.
	 * @param results ArrayList
	 */
	void setResults(ArrayList results) {
		Assert.isNotNull(results);
		fResults= results;
	}

	/**
	 * Method getSelection.
	 * @return ISelection
	 */
	ISelection getSelection() {
		return fSelection;
	}

	/**
	 * Method setSelection.
	 * @param selection ISelection
	 */
	void setSelection(ISelection selection) {
		fSelection= selection;
	}
}

