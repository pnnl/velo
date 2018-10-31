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

import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * Drop down action that holds the currently registered sort actions.
 * @version $Revision: 1.0 $
 */
class SortDropDownAction extends Action implements IMenuCreator {

	// Persistance tags.
	private static final String TAG_SORTERS= "sorters"; //$NON-NLS-1$
	private static final String TAG_DEFAULT_SORTERS= "defaultSorters"; //$NON-NLS-1$	
	private static final String TAG_ELEMENT= "element"; //$NON-NLS-1$	
	private static final String TAG_PAGE_ID= "pageId"; //$NON-NLS-1$
	private static final String TAG_SORTER_ID= "sorterId"; //$NON-NLS-1$

	private static Map fgLastCheckedForType= new HashMap(5);

	private SearchResultViewer fViewer;
	private String fPageId;
	private Menu fMenu;
	private Map fLastCheckedForType;

	/**
	 * Constructor for SortDropDownAction.
	 * @param viewer SearchResultViewer
	 */
	public SortDropDownAction(SearchResultViewer viewer) {
		super(SearchMessages.SortDropDownAction_label); 
		SearchPluginImages.setImageDescriptors(this, SearchPluginImages.T_LCL, SearchPluginImages.IMG_LCL_SEARCH_SORT);
		fViewer= viewer;
		setToolTipText(SearchMessages.SortDropDownAction_tooltip); 
		setMenuCreator(this);
		fLastCheckedForType= new HashMap(5);
	}

	/**
	 * Method dispose.
	 * @see org.eclipse.jface.action.IMenuCreator#dispose()
	 */
	public void dispose() {
		if (fMenu != null && !fMenu.isDisposed())
			fMenu.dispose();
		fMenu= null;
	}

	/**
	 * Method getMenu.
	 * @param parent Control
	 * @return Menu
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		return null;
	}

	/**
	 * Method setPageId.
	 * @param pageId String
	 */
	void setPageId(String pageId) {
		fPageId= pageId;
		SorterDescriptor sorterDesc= (SorterDescriptor)fLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= (SorterDescriptor)fgLastCheckedForType.get(pageId);
		if (sorterDesc == null)
			sorterDesc= findSorter(fPageId);
		if (sorterDesc != null) {
			setChecked(sorterDesc);
			fViewer.setSorter(sorterDesc.createObject());
		} else {
			// Use default sort workbench viewer sorter
			fViewer.setSorter(new WorkbenchViewerSorter());
		}
	}

	/**
	 * Method getMenu.
	 * @param parent Menu
	 * @return Menu
	 * @see org.eclipse.jface.action.IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(final Menu parent) {
		dispose(); // ensure old menu gets disposed
	
		fMenu= new Menu(parent);
		
		Iterator iter= CatRcpPlugin.getDefault().getSearchPlugin().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			Object value= fLastCheckedForType.get(fPageId);
			final String checkedId;
			if (value instanceof SorterDescriptor)
				checkedId= ((SorterDescriptor)value).getId();
			else
				checkedId= ""; //$NON-NLS-1$
			
			final SorterDescriptor sorterDesc= (SorterDescriptor) iter.next();
			if (!sorterDesc.getPageId().equals(fPageId) && !sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				continue;
			final ViewerSorter sorter= sorterDesc.createObject();
			if (sorter != null) {
				final Action action= new Action() {
					public void run() {
						if (!checkedId.equals(sorterDesc.getId())) {
							SortDropDownAction.this.setChecked(sorterDesc);
							BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
								public void run() {
									fViewer.setSorter(sorter);
								}
							});
						}
					}
				};
				action.setText(sorterDesc.getLabel());
				action.setImageDescriptor(sorterDesc.getImage());
				action.setToolTipText(sorterDesc.getToolTipText());
				action.setChecked(checkedId.equals(sorterDesc.getId()));
				addActionToMenu(fMenu, action);
			}
		}
		return fMenu;
	}

	/**
	 * Method addActionToMenu.
	 * @param parent Menu
	 * @param action Action
	 */
	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
		// nothing to do
	    }

	/**
	 * Method findSorter.
	 * @param pageId String
	 * @return SorterDescriptor
	 */
	private SorterDescriptor findSorter(String pageId) {
		Iterator iter= CatRcpPlugin.getDefault().getSearchPlugin().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getPageId().equals(pageId) || sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				return sorterDesc;
		}
		return null;
	}

	/**
	 * Method getSorter.
	 * @param sorterId String
	 * @return SorterDescriptor
	 */
	private SorterDescriptor getSorter(String sorterId) {
		Iterator iter= CatRcpPlugin.getDefault().getSearchPlugin().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getId().equals(sorterId))
				return sorterDesc;
		}
		return null;
	}

	/**
	 * Method setChecked.
	 * @param sorterDesc SorterDescriptor
	 */
	private void setChecked(SorterDescriptor sorterDesc) {
		fLastCheckedForType.put(fPageId, sorterDesc);
		fgLastCheckedForType.put(fPageId, sorterDesc);
	}

	/**
	 * Disposes this action's menu and returns a new unused instance.
	 * @return SortDropDownAction
	 */
	SortDropDownAction renew() {
		SortDropDownAction action= new SortDropDownAction(fViewer);
		action.fLastCheckedForType= fLastCheckedForType;
		action.fPageId= fPageId;
		dispose();
		return action;
	}

	//--- Persistency -------------------------------------------------
	
	/**
	 * Method restoreState.
	 * @param memento IMemento
	 */
	void restoreState(IMemento memento) {
		if (fLastCheckedForType.isEmpty())
			restoreState(memento, fLastCheckedForType, TAG_SORTERS);
		if (fgLastCheckedForType.isEmpty())
			restoreState(memento, fgLastCheckedForType, TAG_DEFAULT_SORTERS);
	}

	/**
	 * Method restoreState.
	 * @param memento IMemento
	 * @param map Map
	 * @param mapName String
	 */
	private void restoreState(IMemento memento, Map map, String mapName) {
		memento= memento.getChild(mapName);
		if (memento == null)
			return;
		IMemento[] mementoElements= memento.getChildren(TAG_ELEMENT);
		for (int i= 0; i < mementoElements.length; i++) {
			String pageId= mementoElements[i].getString(TAG_PAGE_ID);
			String sorterId= mementoElements[i].getString(TAG_SORTER_ID);
			SorterDescriptor sorterDesc= getSorter(sorterId);
			if (sorterDesc != null)
				map.put(pageId, sorterDesc);
		}
	}
	
	/**
	 * Method saveState.
	 * @param memento IMemento
	 */
	void saveState(IMemento memento) {
		saveState(memento, fgLastCheckedForType, TAG_DEFAULT_SORTERS);
		saveState(memento, fLastCheckedForType, TAG_SORTERS);
	}
	
	/**
	 * Method saveState.
	 * @param memento IMemento
	 * @param map Map
	 * @param mapName String
	 */
	private void saveState(IMemento memento, Map map, String mapName) {
		Iterator iter= map.entrySet().iterator();
		memento= memento.createChild(mapName);
		while (iter.hasNext()) {
			IMemento mementoElement= memento.createChild(TAG_ELEMENT);
			Map.Entry entry= (Map.Entry)iter.next();
			mementoElement.putString(TAG_PAGE_ID, (String)entry.getKey());
			mementoElement.putString(TAG_SORTER_ID, ((SorterDescriptor)entry.getValue()).getId());
		}
	}

	/**
	 * Method getSorterCount.
	 * @return int
	 */
	int getSorterCount() {
		int count= 0;
		Iterator iter= CatRcpPlugin.getDefault().getSearchPlugin().getSorterDescriptors().iterator();
		while (iter.hasNext()) {
			SorterDescriptor sorterDesc= (SorterDescriptor)iter.next();
			if (sorterDesc.getPageId().equals(fPageId) || sorterDesc.getPageId().equals("*")) //$NON-NLS-1$
				count++;
		}
		return count;
	}
}
