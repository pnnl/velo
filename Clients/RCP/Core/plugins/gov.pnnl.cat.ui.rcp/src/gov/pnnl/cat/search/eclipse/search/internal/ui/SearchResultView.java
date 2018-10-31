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


import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.search.eclipse.search.ui.IActionGroupFactory;
import gov.pnnl.cat.search.eclipse.search.ui.IContextMenuContributor;
import gov.pnnl.cat.search.eclipse.search.ui.IGroupByKeyComputer;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.CellEditorActionHandler;
import org.eclipse.ui.part.ViewPart;


/**
 */
public class SearchResultView extends ViewPart implements ISearchResultView {


	private static Map fgLabelProviders= new HashMap(5);
	
	private SearchResultViewer fViewer;
	private Map fResponse;
	private IMemento fMemento;
	private IPropertyChangeListener fPropertyChangeListener;
	private CellEditorActionHandler fCellEditorActionHandler;
	private SelectAllAction fSelectAllAction;

	/*
	 * Implements method from IViewPart.
	 */
	/**
	 * Method init.
	 * @param site IViewSite
	 * @param memento IMemento
	 * @throws PartInitException
	 * @see org.eclipse.ui.IViewPart#init(IViewSite, IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento= memento;
	}

	/*
	 * Implements method from IViewPart.
	 */
	/**
	 * Method saveState.
	 * @param memento IMemento
	 * @see org.eclipse.ui.IViewPart#saveState(IMemento)
	 */
	public void saveState(IMemento memento) {
		if (fViewer == null) {
			// part has not been created
			if (fMemento != null) //Keep the old state;
				memento.putMemento(fMemento);
			return;
		}
		fViewer.saveState(memento);
	}	

	/**
	 * Creates the search list inner viewer.
	 * @param parent Composite
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		Assert.isTrue(fViewer == null);
		fViewer= new SearchResultViewer(this, parent);
		if (fMemento != null)
			fViewer.restoreState(fMemento);
		fMemento= null;
		SearchManager.getDefault().addSearchChangeListener(fViewer);
		fViewer.init();

		// Add selectAll action handlers.
		fCellEditorActionHandler = new CellEditorActionHandler(getViewSite().getActionBars());
		fSelectAllAction= new SelectAllAction();
		fSelectAllAction.setViewer(fViewer);
		fCellEditorActionHandler.setSelectAllAction(fSelectAllAction);

		fillActionBars(getViewSite().getActionBars());
		
		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (SearchPreferencePage.POTENTIAL_MATCH_FG_COLOR.equals(event.getProperty()) || SearchPreferencePage.EMPHASIZE_POTENTIAL_MATCHES.equals(event.getProperty()))
					if (fViewer != null)
						fViewer.updatedPotentialMatchFgColor();
			}
		};
		
		CatRcpPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(fViewer.getControl(), CatRcpPlugin.getDefault().getSearchPlugin().getSearchViewHelpContextId());
	}
	
	/**
	 * Returns the search result viewer.
	 * @return SearchResultViewer
	 */
	public SearchResultViewer getViewer() {
		return fViewer;
	}
	
	//---- IWorkbenchPart ------------------------------------------------------


	/**
	 * Method setFocus.
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		fViewer.getControl().setFocus();
	}
	
	/**
	 * Method dispose.
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (fViewer != null) {
			SearchManager.getDefault().removeSearchChangeListener(fViewer);
			fViewer= null;
		}
		if (fPropertyChangeListener != null)
			CatRcpPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		if (fCellEditorActionHandler != null) {
			fCellEditorActionHandler.dispose();
			fCellEditorActionHandler= null;
		}
		super.dispose();
	}
	
	/**
	 * Method setContentDescription.
	 * @param title String
	 */
	protected void setContentDescription(String title) {
		super.setContentDescription(title);
	}
	
	/**
	 * Method setTitleToolTip.
	 * @param text String
	 */
	protected void setTitleToolTip(String text) {
		super.setTitleToolTip(text);
	}
	
	//---- Adding Action to Toolbar -------------------------------------------
	
	/**
	 * Method fillActionBars.
	 * @param actionBars IActionBars
	 */
	private void fillActionBars(IActionBars actionBars) {
		IToolBarManager toolBar= actionBars.getToolBarManager();
		fillToolBar(toolBar);
		actionBars.updateActionBars();
		
		// Add selectAll action handlers.
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), fSelectAllAction);
	}

	/**
	 * Method fillToolBar.
	 * @param tbm IToolBarManager
	 */
	private void fillToolBar(IToolBarManager tbm) {
		fViewer.fillToolBar(tbm);
	}	

	/**
	 * Method getLabelProvider.
	 * @param pageId String
	 * @return ILabelProvider
	 */
	ILabelProvider getLabelProvider(String pageId) {
		if (pageId != null)
			return (ILabelProvider)fgLabelProviders.get(pageId);
		return null;
	}

	/**
	 * Method getLabelProvider.
	 * @return ILabelProvider
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#getLabelProvider()
	 */
	public ILabelProvider getLabelProvider() {
		if (fViewer == null)
			return null;
		IBaseLabelProvider labelProvider= fViewer.getLabelProvider();
		if (labelProvider == null)
			return null;
		
		return ((SearchResultLabelProvider)labelProvider).getLabelProvider();
	}

	/**
	 * Method setGotoMarkerAction.
	 * @param gotoMarkerAction IAction
	 */
	private void setGotoMarkerAction(final IAction gotoMarkerAction) {
		// Make sure we are doing it in the right thread.
		getDisplay().syncExec(new Runnable() {
			public void run() {
				getViewer().setGotoMarkerAction(gotoMarkerAction);
			}
		});
	}


	/**
	 * Method getDisplay.
	 * @return Display
	 */
	Display getDisplay() {
		return fViewer.getControl().getDisplay();
	}	


	//---- ISearchResultView --------------------------------------------------


	/*
	 * Implements method from ISearchResultView
	 */
	/**
	 * Method getSelection.
	 * @return ISelection
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#getSelection()
	 */
	public ISelection getSelection() {
		return fViewer.getSelection();
	}

	/*
	 * Implements method from ISearchResultView
	 */
	/**
	 * Method searchStarted.
	 * @param groupFactory IActionGroupFactory
	 * @param singularLabel String
	 * @param pluralLabelPattern String
	 * @param imageDescriptor ImageDescriptor
	 * @param pageId String
	 * @param labelProvider ILabelProvider
	 * @param gotoAction IAction
	 * @param groupByKeyComputer IGroupByKeyComputer
	 * @param operation IRunnableWithProgress
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#searchStarted(IActionGroupFactory, String, String, ImageDescriptor, String, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
	 */
	public void searchStarted(
				IActionGroupFactory		groupFactory,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				String					pageId,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {


		Assert.isNotNull(pageId);
		Assert.isNotNull(pluralLabelPattern);
		Assert.isNotNull(gotoAction);		

		fResponse= new HashMap(500);
		setGotoMarkerAction(gotoAction);

		ILabelProvider oldLabelProvider= (ILabelProvider)fgLabelProviders.get(pageId);
		if (oldLabelProvider != null)
			oldLabelProvider.dispose();
		fgLabelProviders.put(pageId, labelProvider);

		SearchManager.getDefault().addNewSearch(		
			new Search(
				pageId,
				singularLabel,
				pluralLabelPattern,
				null,
				imageDescriptor,
				fViewer.getGotoMarkerAction(),
				groupFactory,
				groupByKeyComputer,
				operation));
	}

	/**
	 * Implements method from ISearchResultView
	 * @deprecated	As of build > 20011107, replaced by the new version with additonal parameter
	 * @param pageId String
	 * @param label String
	 * @param imageDescriptor ImageDescriptor
	 * @param contributor IContextMenuContributor
	 * @param labelProvider ILabelProvider
	 * @param gotoAction IAction
	 * @param groupByKeyComputer IGroupByKeyComputer
	 * @param operation IRunnableWithProgress
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#searchStarted(String, String, ImageDescriptor, IContextMenuContributor, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
	 */
	public void searchStarted(
				String					pageId,
				String					label,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {
		
		searchStarted(pageId, null, label, imageDescriptor, contributor, labelProvider, gotoAction, groupByKeyComputer, operation);
	}

	/**
	 * Implements method from ISearchResultView
	 * @deprecated	As of build > 20020514
	 * @param pageId String
	 * @param singularLabel String
	 * @param pluralLabelPattern String
	 * @param imageDescriptor ImageDescriptor
	 * @param contributor IContextMenuContributor
	 * @param labelProvider ILabelProvider
	 * @param gotoAction IAction
	 * @param groupByKeyComputer IGroupByKeyComputer
	 * @param operation IRunnableWithProgress
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#searchStarted(String, String, String, ImageDescriptor, IContextMenuContributor, ILabelProvider, IAction, IGroupByKeyComputer, IRunnableWithProgress)
	 */
	public void searchStarted(
				String					pageId,
				String					singularLabel,
				String					pluralLabelPattern,
				ImageDescriptor			imageDescriptor,
				IContextMenuContributor contributor,
				ILabelProvider			labelProvider,
				IAction					gotoAction,
				IGroupByKeyComputer		groupByKeyComputer,
				IRunnableWithProgress	operation) {


		Assert.isNotNull(pageId);
		Assert.isNotNull(pluralLabelPattern);
		Assert.isNotNull(gotoAction);		

		fResponse= new HashMap(500);
		setGotoMarkerAction(gotoAction);

		ILabelProvider oldLabelProvider= (ILabelProvider)fgLabelProviders.get(pageId);
		if (oldLabelProvider != null)
			oldLabelProvider.dispose();
		fgLabelProviders.put(pageId, labelProvider);

		SearchManager.getDefault().addNewSearch(		
			new Search(
				pageId,
				singularLabel,
				pluralLabelPattern,
				null,
				imageDescriptor,
				fViewer.getGotoMarkerAction(),
				contributor,
				groupByKeyComputer,
				operation));
	}

	/*
	 * Implements method from ISearchResultView
	 */
	/**
	 * Method addMatch.
	 * @param description String
	 * @param groupByKey Object
	 * @param resource IResource
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#addMatch(String, Object, IResource)
	 */
	public void addMatch(String description, Object groupByKey, IResource resource) {
		SearchResultViewEntry entry= (SearchResultViewEntry)fResponse.get(groupByKey);
		if (entry == null) {
			entry= new SearchResultViewEntry(groupByKey, resource);
			fResponse.put(groupByKey, entry);
		}
	}


	/*
	 * Implements method from ISearchResultView
	 */
	/**
	 * Method searchFinished.
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView#searchFinished()
	 */
	public void searchFinished() {
		SearchManager.getDefault().searchFinished(new ArrayList(fResponse.values()));
		fResponse= null;
	}
}
