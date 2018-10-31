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

import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExceptionHandler;
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExtendedDialogWindow;
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ListContentProvider;
import gov.pnnl.cat.search.eclipse.search.ui.IReplacePage;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchPage;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchPageScoreComputer;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 */
public class SearchDialog extends ExtendedDialogWindow implements ISearchPageContainer, IPageChangeProvider {

	/**
	 */
	private class TabFolderLayout extends Layout {
		/**
		 * Method computeSize.
		 * @param composite Composite
		 * @param wHint int
		 * @param hHint int
		 * @param flushCache boolean
		 * @return Point
		 */
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);

			int x= 0; 
			int y= 0;				
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				Point size= children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
				x= Math.max(x, size.x);
				y= Math.max(y, size.y);
			}
			
			Point minSize= getMinSize();
			x= Math.max(x, minSize.x);
			y= Math.max(y, minSize.y);
			
			if (wHint != SWT.DEFAULT)
				x= wHint;
			if (hHint != SWT.DEFAULT)
				y= hHint;
			return new Point(x, y);		
		}
		/**
		 * Method layout.
		 * @param composite Composite
		 * @param flushCache boolean
		 */
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle rect= composite.getClientArea();
			
			Control[] children= composite.getChildren();
			for (int i= 0; i < children.length; i++) {
				children[i].setBounds(rect);
			}
		}
	}
	
	
	private static final int SEARCH_ID= IDialogConstants.CLIENT_ID + 1;
	private static final int REPLACE_ID= SEARCH_ID + 1;
	private static final int CUSTOMIZE_ID= REPLACE_ID + 1;
	
	private ISearchPage fCurrentPage;
	private String fInitialPageId;
	private int fCurrentIndex;
	private ISelection fSelection;
	private IEditorPart fEditorPart;
	private List fDescriptors;
	private Point fMinSize;
	private ScopePart[] fScopeParts;
	private boolean fLastEnableState;
	private Button fCustomizeButton;
	private Button fReplaceButton;
	private ListenerList fPageChangeListeners;


	/**
	 * Constructor for SearchDialog.
	 * @param shell Shell
	 * @param selection ISelection
	 * @param editor IEditorPart
	 * @param pageId String
	 */
	public SearchDialog(Shell shell, ISelection selection, IEditorPart editor, String pageId) {
		super(shell);
		fSelection= selection;
		fEditorPart= editor;
		fDescriptors= filterByActivities(CatRcpPlugin.getDefault().getSearchPlugin().getEnabledSearchPageDescriptors(pageId));
		fInitialPageId= pageId;
		fPageChangeListeners= null;
		setUseEmbeddedProgressMonitorPart(false);
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	/**
	 * Method configureShell.
	 * @param shell Shell
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(SearchMessages.SearchDialog_title); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, ISearchHelpContextIds.SEARCH_DIALOG);
	}

	
	/**
	 * Method getSelection.
	 * @return ISelection
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#getSelection()
	 */
	public ISelection getSelection() {
		return fSelection;
	}
	
	/**
	 * Method getEditorPart.
	 * @return IEditorPart
	 */
	public IEditorPart getEditorPart() {
		return fEditorPart;
	}
	
	//---- Page Handling -------------------------------------------------------

	/*
	 * Overrides method from Window
	 */
	public void create() {
		super.create();
		if (fCurrentPage != null) {
			fCurrentPage.setVisible(true);
		}
	}

	private void handleCustomizePressed() {
		List input= CatRcpPlugin.getDefault().getSearchPlugin().getSearchPageDescriptors();
		input= filterByActivities(input);

		final ArrayList createdImages= new ArrayList(input.size());
		ILabelProvider labelProvider= new LabelProvider() {
			public String getText(Object element) {
				if (element instanceof SearchPageDescriptor) {
					String label= ((SearchPageDescriptor)element).getLabel();
					int i= label.indexOf('&');
					while (i >= 0) {
						label= label.substring(0, i) + label.substring(i+1);
						i= label.indexOf('&');
					}
					return label;
				}
				return null;
			}
			public Image getImage(Object element) {
				if (element instanceof SearchPageDescriptor) {
					ImageDescriptor imageDesc= ((SearchPageDescriptor)element).getImage();
					if (imageDesc == null)
						return null;
					Image image= imageDesc.createImage();
					if (image != null)
						createdImages.add(image);
					return image;
				}
				return null;
			}
		};

		String message= SearchMessages.SearchPageSelectionDialog_message; 
		
		ListSelectionDialog dialog= new ListSelectionDialog(getShell(), input, new ListContentProvider(), labelProvider, message) {
			public void create() {
				super.create();
				final CheckboxTableViewer viewer= getViewer();
				final Button okButton= this.getOkButton();
				viewer.addCheckStateListener(new ICheckStateListener() {
					public void checkStateChanged(CheckStateChangedEvent event) {
						okButton.setEnabled(viewer.getCheckedElements().length > 0);
					}
				});
				SelectionListener listener = new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						okButton.setEnabled(viewer.getCheckedElements().length > 0);
					}
				};
				this.getButton(IDialogConstants.SELECT_ALL_ID).addSelectionListener(listener);
				this.getButton(IDialogConstants.DESELECT_ALL_ID).addSelectionListener(listener);
			}
		};
		dialog.setTitle(SearchMessages.SearchPageSelectionDialog_title); 
		dialog.setInitialSelections(CatRcpPlugin.getDefault().getSearchPlugin().getEnabledSearchPageDescriptors(fInitialPageId).toArray());
		if (dialog.open() == Window.OK) {
			SearchPageDescriptor.setEnabled(dialog.getResult());
			Display display= getShell().getDisplay();
			close();			
			if (display != null && !display.isDisposed()) {
				display.asyncExec(
						new Runnable() {
							public void run() {
								new OpenSearchDialogAction().run();
							}
						});
			}
		}
		destroyImages(createdImages);		
	}

	/**
	 * Method filterByActivities.
	 * @param input List
	 * @return List
	 */
	private List filterByActivities(List input) {
		ArrayList filteredList= new ArrayList(input.size());
		for (Iterator descriptors= input.iterator(); descriptors.hasNext();) {
			SearchPageDescriptor descriptor= (SearchPageDescriptor) descriptors.next();
			if (!WorkbenchActivityHelper.filterItem(descriptor))
			    filteredList.add(descriptor);
			
		}
		return filteredList;
	}

	/**
	 * Method destroyImages.
	 * @param images List
	 */
	private void destroyImages(List images) {
		Iterator iter= images.iterator();
		while (iter.hasNext()) {
			Image image= (Image)iter.next();
			if (image != null && !image.isDisposed())
				image.dispose();
		}
	}
	
	/**
	 * Method createPageArea.
	 * @param parent Composite
	 * @return Control
	 */
	protected Control createPageArea(Composite parent) {
		int numPages= fDescriptors.size();
		fScopeParts= new ScopePart[numPages];
		
		if (numPages == 0) {
			Label label= new Label(parent, SWT.CENTER | SWT.WRAP);
			label.setText(SearchMessages.SearchDialog_noSearchExtension); 
			return label;
		}
		
		fCurrentIndex= getPreferredPageIndex();
		final SearchPageDescriptor currentDesc= getDescriptorAt(fCurrentIndex);
				
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		TabFolder folder= new TabFolder(composite, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		folder.setFont(composite.getFont());
		
		for (int i= 0; i < numPages; i++) {			
			SearchPageDescriptor descriptor= getDescriptorAt(i);
			if (WorkbenchActivityHelper.filterItem(descriptor))
			    continue;
			
			final TabItem item= new TabItem(folder, SWT.NONE);
			item.setData("descriptor", descriptor); //$NON-NLS-1$
			item.setText(descriptor.getLabel());
			item.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					item.setData("descriptor", null); //$NON-NLS-1$
					if (item.getImage() != null)
						item.getImage().dispose();
				}
			});
			ImageDescriptor imageDesc= descriptor.getImage();
			if (imageDesc != null)
				item.setImage(imageDesc.createImage());
			
			if (i == fCurrentIndex) {
				Control pageControl= createPageControl(folder, descriptor);
				pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				item.setControl(pageControl);
				fCurrentPage= currentDesc.getPage();
			}
		}
		
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				turnToPage(event);
			}
		});

		folder.setSelection(fCurrentIndex);
		
		return composite;
	}
	
	/**
	 * Method createButtonBar.
	 * @param parent Composite
	 * @return Control
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 0;   // create 
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		fCustomizeButton= createButton(composite, CUSTOMIZE_ID, SearchMessages.SearchDialog_customize, true); 
		
		Label filler= new Label(composite, SWT.NONE);
		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		layout.numColumns++;
		
		fReplaceButton= createActionButton(composite, REPLACE_ID, SearchMessages.SearchDialog_replaceAction, true); 
		fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
		Button searchButton= createActionButton(composite, SEARCH_ID, SearchMessages.SearchDialog_searchAction, true); 
		searchButton.setEnabled(fDescriptors.size() > 0);
		super.createButtonsForButtonBar(composite);  // cancel button
		
		return composite;
	}

	/**
	 * Method performAction.
	 * @param actionID int
	 * @return boolean
	 */
	protected boolean performAction(int actionID) {
		switch (actionID) {
			case CUSTOMIZE_ID:
				handleCustomizePressed();
				return false;
			case CANCEL:
				return true;
			case SEARCH_ID:
				if (fCurrentPage != null) {
					return fCurrentPage.performAction();
				}
				return true;
			case REPLACE_ID:
				
				try {
					fCustomizeButton.setEnabled(false);

					// safe cast, replace button is only visible when the current page is 
					// a replace page.
					return ((IReplacePage)fCurrentPage).performReplace();
				} finally {
					fCustomizeButton.setEnabled(true);
						
				}
			default:
				return false;
		}	
	}

	/**
	 * Method getDescriptorAt.
	 * @param index int
	 * @return SearchPageDescriptor
	 */
	private SearchPageDescriptor getDescriptorAt(int index) {
		return (SearchPageDescriptor) fDescriptors.get(index);
	}
	
	/**
	 * Method getMinSize.
	 * @return Point
	 */
	private Point getMinSize() {
		if (fMinSize != null)
			return fMinSize;
			
		int x= 0;
		int y= 0;
		int length= fDescriptors.size();
		for (int i= 0; i < length; i++) {
			Point size= getDescriptorAt(i).getPreferredSize();
			if (size.x != SWT.DEFAULT)
				x= Math.max(x, size.x);
			if (size.y != SWT.DEFAULT)
				y= Math.max(y, size.y);
		}
		
		fMinSize= new Point(x, y);
		return fMinSize;	
	}
	
	/**
	 * Method turnToPage.
	 * @param event SelectionEvent
	 */
	private void turnToPage(SelectionEvent event) {
		final TabItem item= (TabItem) event.item;
		TabFolder folder= item.getParent();
		
		SearchPageDescriptor descriptor= (SearchPageDescriptor) item.getData("descriptor"); //$NON-NLS-1$
		
		if (item.getControl() == null) {
			item.setControl(createPageControl(folder, descriptor));
		}
		
		Control oldControl= folder.getItem(fCurrentIndex).getControl();
		Point oldSize= oldControl.getSize();
		Control newControl= item.getControl();
		Point newSize= newControl.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		resizeDialogIfNeeded(oldSize, newSize);
		
		ISearchPage oldPage= fCurrentPage;
		if (oldPage != null) {
			oldPage.setVisible(false);
		}
		
		fCurrentPage= descriptor.getPage();
		fCurrentIndex= folder.getSelectionIndex();
		
		setPerformActionEnabled(fCurrentPage != null);
		if (fCurrentPage != null) {
			fCurrentPage.setVisible(true);
		}
		fReplaceButton.setVisible(fCurrentPage instanceof IReplacePage);
		notifyPageChanged();
	}
	
	/**
	 * Method getPreferredPageIndex.
	 * @return int
	 */
	private int getPreferredPageIndex() {
		Object element= null;
		if (fSelection instanceof IStructuredSelection)
			element= ((IStructuredSelection)fSelection).getFirstElement();
		if (element == null && fEditorPart != null) {
			element= fEditorPart.getEditorInput();
      /*
			if (element instanceof IFileEditorInput)
				element= ((IFileEditorInput)element).getFile();
      */
		}
		int result= 0;
		int level= ISearchPageScoreComputer.LOWEST;
		int size= fDescriptors.size();
		for (int i= 0; i < size; i++) {
			SearchPageDescriptor descriptor= (SearchPageDescriptor)fDescriptors.get(i);
			if (fInitialPageId != null && fInitialPageId.equals(descriptor.getId()))
				return i;
			
			int newLevel= descriptor.computeScore(element);
			if (newLevel > level) {
				level= newLevel;
				result= i;
			}
		}
		return result;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	/**
	 * Method getRunnableContext.
	 * @return IRunnableContext
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#getRunnableContext()
	 */
	public IRunnableContext getRunnableContext() {
		return this;
	}

	/*
	 * Implements method from ISearchPageContainer
	 */	
	/**
	 * Method getSelectedScope.
	 * @return int
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#getSelectedScope()
	 */
	public int getSelectedScope() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return ISearchPageContainer.WORKSPACE_SCOPE;
		
		return fScopeParts[fCurrentIndex].getSelectedScope();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	/**
	 * Method getSelectedWorkingSets.
	 * @return IWorkingSet[]
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#getSelectedWorkingSets()
	 */
	public IWorkingSet[] getSelectedWorkingSets() {
		if (fScopeParts[fCurrentIndex] == null)
			// safe code - should not happen
			return null;
		
		return fScopeParts[fCurrentIndex].getSelectedWorkingSets();
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	/**
	 * Method setSelectedScope.
	 * @param scope int
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#setSelectedScope(int)
	 */
	public void setSelectedScope(int scope) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedScope(scope);
	}

	/*
	 * Implements method from ISearchPageContainer
	 */
	/**
	 * Method hasValidScope.
	 * @return boolean
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#hasValidScope()
	 */
	public boolean hasValidScope() {
		return getSelectedScope() != WORKING_SET_SCOPE || getSelectedWorkingSets() != null;
	}
	
	/*
	 * Implements method from ISearchPageContainer
	 */
	/**
	 * Method setSelectedWorkingSets.
	 * @param workingSets IWorkingSet[]
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#setSelectedWorkingSets(IWorkingSet[])
	 */
	public void setSelectedWorkingSets(IWorkingSet[] workingSets) {
		if (fScopeParts[fCurrentIndex] != null)
			fScopeParts[fCurrentIndex].setSelectedWorkingSets(workingSets);
	}

	/*
	 * Overrides method from ExtendedDialogWindow
	 */
	/**
	 * Method setPerformActionEnabled.
	 * @param state boolean
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchPageContainer#setPerformActionEnabled(boolean)
	 */
	public void setPerformActionEnabled(boolean state) {
		fLastEnableState= state;
		super.setPerformActionEnabled(state && hasValidScope());
	} 

	/**
	 * Notify that the scope selection has changed
	 * <p>
	 * Note: This is a special method to be called only from the ScopePart
	 * </p>
	 */
	public void notifyScopeSelectionChanged() {
		setPerformActionEnabled(fLastEnableState);
	}

	/**
	 * Method createPageControl.
	 * @param parent Composite
	 * @param descriptor SearchPageDescriptor
	 * @return Control
	 */
	private Control createPageControl(Composite parent, final SearchPageDescriptor descriptor) {
		
		// Page wrapper
		final Composite pageWrapper= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		pageWrapper.setLayout(layout);
		
		applyDialogFont(pageWrapper);
		
		BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
			public void run() {
				Platform.run(new ISafeRunnable() {
					public void run() throws Exception {
						// create page and control
						ISearchPage page= descriptor.createObject(SearchDialog.this);
						if (page != null) {
							page.createControl(pageWrapper);
						}
					}
					public void handleException(Throwable ex) {
						if (ex instanceof CoreException) {
							ExceptionHandler.handle((CoreException) ex, getShell(), SearchMessages.Search_Error_createSearchPage_title, Messages.format(SearchMessages.Search_Error_createSearchPage_message, descriptor.getLabel())); 
						} else {
							ExceptionHandler.displayMessageDialog(ex, getShell(), SearchMessages.Search_Error_createSearchPage_title, Messages.format(SearchMessages.Search_Error_createSearchPage_message, descriptor.getLabel())); 
						}
					}
				});
			}
		});
		
		ISearchPage page= descriptor.getPage();
		if (page == null || page.getControl() == null) {
			Composite container= new Composite(parent, SWT.NONE);
			Label label= new Label(container, SWT.WRAP);
			label.setText(Messages.format(SearchMessages.SearchDialog_error_pageCreationFailed, descriptor.getLabel())); 
			container.setLayout(new GridLayout());
			label.setLayoutData(new GridData());
			return container;
		}
		
		page.getControl().setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		
		// Search scope
		boolean showScope= descriptor.showScopeSection();
		if (showScope) {
			Composite c= new Composite(pageWrapper, SWT.NONE);
			c.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			c.setLayout(new GridLayout());
			
			int index= fDescriptors.indexOf(descriptor);
			fScopeParts[index]= new ScopePart(this, descriptor.canSearchInProjects());
			Control part= fScopeParts[index].createPart(c);
			applyDialogFont(part);
			part.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
			fScopeParts[index].setVisible(true);
		}
		
		return pageWrapper;
	}
	
	/**
	 * Method resizeDialogIfNeeded.
	 * @param oldSize Point
	 * @param newSize Point
	 */
	private void resizeDialogIfNeeded(Point oldSize, Point newSize) {
		if (oldSize == null || newSize == null)
			return;
			Shell shell= getShell();
		Point shellSize= shell.getSize();
		if (mustResize(oldSize, newSize)) {
			if (newSize.x > oldSize.x)
				shellSize.x+= (newSize.x-oldSize.x);
			if (newSize.y > oldSize.y)
				shellSize.y+= (newSize.y-oldSize.y);
			shell.setSize(shellSize);
					shell.layout(true);
		}
	}
	
	/**
	 * Method mustResize.
	 * @param currentSize Point
	 * @param newSize Point
	 * @return boolean
	 */
	private boolean mustResize(Point currentSize, Point newSize) {
		return currentSize.x < newSize.x || currentSize.y < newSize.y;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		for (int i= 0; i < fDescriptors.size(); i++) {
			SearchPageDescriptor desc= (SearchPageDescriptor) fDescriptors.get(i);
			desc.dispose();
		}
		return super.close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#getSelectedPage()
	 */
	public Object getSelectedPage() {
		return fCurrentPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 */
	public void addPageChangedListener(IPageChangedListener listener) {
		if (fPageChangeListeners == null) {
			fPageChangeListeners= new ListenerList(3);
		}
		fPageChangeListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#removePageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 */
	public void removePageChangedListener(IPageChangedListener listener) {
		fPageChangeListeners.remove(listener);
	}
	
	private void notifyPageChanged() {
		if (fPageChangeListeners != null && !fPageChangeListeners.isEmpty()) {
			// Fires the page change event
			final PageChangedEvent event= new PageChangedEvent(this, getSelectedPage());
			Object[] listeners= fPageChangeListeners.getListeners();
			for (int i= 0; i < listeners.length; ++i) {
				final IPageChangedListener l= (IPageChangedListener) listeners[i];
				Platform.run(new SafeRunnable() {
					public void run() {
						l.pageChanged(event);
					}
				});
			}
		}
	}
}
