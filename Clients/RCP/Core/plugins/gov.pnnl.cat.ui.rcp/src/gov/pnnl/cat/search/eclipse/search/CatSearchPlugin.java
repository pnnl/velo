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
package gov.pnnl.cat.search.eclipse.search;



import gov.pnnl.cat.search.eclipse.search.internal.ui.ISearchHelpContextIds;
import gov.pnnl.cat.search.eclipse.search.internal.ui.Search;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchManager;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPageDescriptor;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchPreferencePage;
import gov.pnnl.cat.search.eclipse.search.internal.ui.SorterDescriptor;
import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ExceptionHandler;
import gov.pnnl.cat.search.eclipse.search.ui.IContextMenuConstants;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultView;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.ui.CatSearchPageDescriptor;
import gov.pnnl.cat.ui.rcp.CatRcpPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * The plug-in runtime class for Search plug-in
 * @version $Revision: 1.0 $
 */
public class CatSearchPlugin {
	
	public static final String SEARCH_PAGE_EXTENSION_POINT= "searchPages"; //$NON-NLS-1$
	public static final String CAT_SEARCH_PAGE_EXTENSION_POINT= "catSearchPages"; //$NON-NLS-1$
	public static final String SORTER_EXTENSION_POINT= "searchResultSorters"; //$NON-NLS-1$

  public static final String SEARCH_RESULT_VIEW_ID= "org.eclipse.search.SearchResultView"; //$NON-NLS-1$

	public static final String FILTERED_SEARCH_MARKER=  NewSearchUI.PLUGIN_ID + ".filteredsearchmarker"; //$NON-NLS-1$

	/** Status code describing an internal error */
	public static final int INTERNAL_ERROR= 1;
	
	private List fPageDescriptors;
	
	private List fSorterDescriptors;
	
	private static CatRcpPlugin rcpPlugin;
	

	/**
	 * Constructor for CatSearchPlugin.
	 * @param rcpPlugin Activator
	 */
	public CatSearchPlugin(CatRcpPlugin rcpPlugin) {
	  this.rcpPlugin = rcpPlugin;
	}
	
	/**
	 * Returns the active workbench window.
	 * <code>null</code> if the active window is not a workbench window
	 * @return IWorkbenchWindow
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		IWorkbenchWindow window= rcpPlugin.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			final WindowRef windowRef= new WindowRef();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					setActiveWorkbenchWindow(windowRef);
				}
			});
			return windowRef.window;
		}
		else
			return window;
	}

	/**
	 */
	private static class WindowRef {
		public IWorkbenchWindow window;
	}

	/**
	 * Method setActiveWorkbenchWindow.
	 * @param windowRef WindowRef
	 */
	private static void setActiveWorkbenchWindow(WindowRef windowRef) {
		windowRef.window= null;
		Display display= Display.getCurrent();
		if (display == null)
			return;
		Control shell= display.getActiveShell();
		while (shell != null) {
			Object data= shell.getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
			shell= shell.getParent();
		}
		Shell shells[]= display.getShells();
		for (int i= 0; i < shells.length; i++) {
			Object data= shells[i].getData();
			if (data instanceof IWorkbenchWindow) {
				windowRef.window= (IWorkbenchWindow)data;
				return;
			}
		}
	}

	/**
	 * Returns the shell of the active workbench window.
	 * @return Shell
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null)
			return window.getShell();
		return null;
	}

	/**
	 * Beeps using the display of the active workbench window.
	 */
	public static void beep() {
		getActiveWorkbenchShell().getDisplay().beep();
	}

	/**
	 * Returns the active workbench window's currrent page.
	 * @return IWorkbenchPage
	 */
	public static IWorkbenchPage getActivePage() {
		return getActiveWorkbenchWindow().getActivePage();
	} 

	/**
	 * Activates the search result view in the active page.
	 * This call has no effect, if the search result view is
	 * already activated.
	 *
	
	 * @return <code>true</code> if the search result view could be activated */
	public static boolean activateSearchResultView() {

		try {
			IViewPart viewPart= getActivePage().findView(SEARCH_RESULT_VIEW_ID);
			if (viewPart == null || SearchPreferencePage.isViewBroughtToFront()) {
				return (getActivePage().showView(SEARCH_RESULT_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE) != null);
			}
			return true;
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.Search_Error_openResultView_title, SearchMessages.Search_Error_openResultView_message); 
			return false;
		}	
	}

	/**
	 * Returns the search result view of the active workbench window. Returns <code>
	 * null</code> if the active workbench window doesn't have any search result
	 * view.
	 * @return ISearchResultView
	 */
	public static ISearchResultView getSearchResultView() {
		IViewPart part= getActivePage().findView(SEARCH_RESULT_VIEW_ID);
		if (part instanceof ISearchResultView)
			return (ISearchResultView) part;
		return null;	
	}

	/**
	 * Returns all search pages contributed to the workbench.
	 * @return List
	 */
	public List getSearchPageDescriptors() {
		if (fPageDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(NewSearchUI.PLUGIN_ID, SEARCH_PAGE_EXTENSION_POINT);
			fPageDescriptors= createSearchPageDescriptors(elements);
		}	
		return fPageDescriptors;
	} 

/*Every new search window needs its own copy of the pages
 * so when a window is disposed it doesn't delete the pages
 * the other window is using, therefore the cat page descriptors 
 * should not be cached in the plug-in
 */
  /**
 * Method getCatSearchPageDescriptors.
 * @return List<CatSearchPageDescriptor>
 */
public List<CatSearchPageDescriptor> getCatSearchPageDescriptors() {
    List<CatSearchPageDescriptor>  fCatPageDescriptors;
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IConfigurationElement[] elements= registry.getConfigurationElementsFor(NewSearchUI.PLUGIN_ID, CAT_SEARCH_PAGE_EXTENSION_POINT);
    fCatPageDescriptors = createCatSearchPageDescriptors(elements);

    return fCatPageDescriptors;
  }

	/**
	 * Returns all search pages contributed to the workbench.
	 * @param pageId String
	 * @return List
	 */
	public List getEnabledSearchPageDescriptors(String pageId) {
		Iterator iter= getSearchPageDescriptors().iterator();
		List enabledDescriptors= new ArrayList(5);
		while (iter.hasNext()) {
			SearchPageDescriptor desc= (SearchPageDescriptor)iter.next();
			if (desc.isEnabled() || desc.getId().equals(pageId))
				enabledDescriptors.add(desc);
		}
		return enabledDescriptors;
	} 

	/**
	 * Returns the help context ID for the Search view
	 * as provided by the current search page extension.
	 * 
	 * @since 3.0
	 * @return String
	 */
	public String getSearchViewHelpContextId() {
		Search currentSearch= SearchManager.getDefault().getCurrentSearch();
		if (currentSearch != null) {
			String pageId= currentSearch.getPageId();
			Iterator iter= getSearchPageDescriptors().iterator();
			while (iter.hasNext()) {
				SearchPageDescriptor desc= (SearchPageDescriptor)iter.next();
				if (desc.getId().equals(pageId)) {
					String helpId= desc.getSearchViewHelpContextId();
					if (helpId == null)
						return ISearchHelpContextIds.SEARCH_VIEW;
					else
						return desc.getSearchViewHelpContextId();
				}
			}
		}
		return ISearchHelpContextIds.SEARCH_VIEW;
	} 

	/**
	 * Creates all necessary search page nodes.
	 * @param elements IConfigurationElement[]
	 * @return List
	 */
	private List createSearchPageDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SearchPageDescriptor.PAGE_TAG.equals(element.getName())) {
				SearchPageDescriptor desc= new SearchPageDescriptor(element);
				result.add(desc);
			}
		}
		Collections.sort(result);
		return result;
	}

	/**
	 * Creates all necessary CUE search page nodes.
	 * @param elements IConfigurationElement[]
	 * @return List
	 */
	private List createCatSearchPageDescriptors(IConfigurationElement[] elements) {
	  List result= new ArrayList(5);
	  for (int i= 0; i < elements.length; i++) {
	    IConfigurationElement element= elements[i];
	    if (CatSearchPageDescriptor.PAGE_TAG.equals(element.getName())) {
        CatSearchPageDescriptor desc= new CatSearchPageDescriptor(element);
	      result.add(desc);
	    }
	  }
	  Collections.sort(result);
	  return result;
	}

	/**
	 * Returns all sorters contributed to the workbench.
	 * @return List
	 */
	public List getSorterDescriptors() {
		if (fSorterDescriptors == null) {
			IPluginRegistry registry= Platform.getPluginRegistry();
			IConfigurationElement[] elements= registry.getConfigurationElementsFor(rcpPlugin.PLUGIN_ID, SORTER_EXTENSION_POINT);
			fSorterDescriptors= createSorterDescriptors(elements);
		}	
		return fSorterDescriptors;
	} 

	/**
	 * Creates all necessary sorter description nodes.
	 * @param elements IConfigurationElement[]
	 * @return List
	 */
	private List createSorterDescriptors(IConfigurationElement[] elements) {
		List result= new ArrayList(5);
		for (int i= 0; i < elements.length; i++) {
			IConfigurationElement element= elements[i];
			if (SorterDescriptor.SORTER_TAG.equals(element.getName()))
				result.add(new SorterDescriptor(element));
		}
		return result;
	}

	/**
	 * Log status to platform log
	 * @param status IStatus
	 */	
	public static void log(IStatus status) {
		rcpPlugin.getLog().log(status);
	}

	/**
	 * Method log.
	 * @param e Throwable
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, INTERNAL_ERROR, SearchMessages.SearchPlugin_internal_error, e)); 
	}
	
	/**
	 * Method getID.
	 * @return String
	 */
	public static String getID() {
		return rcpPlugin.getDescriptor().getUniqueIdentifier();
	}

	/**
	 * Creates the Search plugin standard groups in a context menu.
	 * @param menu IMenuManager
	 */
	public static void createStandardGroups(IMenuManager menu) {
		if (!menu.isEmpty())
			return;
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_OPEN));
		menu.add(new Separator(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_REMOVE_MATCHES));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}
}
