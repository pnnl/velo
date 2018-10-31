package gov.pnnl.velo.dataset.util;

import java.util.List;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.editors.VeloEditorUtil;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.dataset.DatasetsPlugin;
import gov.pnnl.velo.dataset.perspectives.DatasetEditing;
import gov.pnnl.velo.dataset.views.DatasetNavigator;
import gov.pnnl.velo.dataset.views.NavigationView;
import gov.pnnl.velo.model.CmsPath;

public class DatasetUtil {

  public static String getDatasetLandingPageURL(String datasetUUID) {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    String baseUrl = mgr.getRepositoryUrlBase();
    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
    // TODO - match the expected URL
    return baseUrl + "/datasets/?UUID=" + datasetUUID;
  }

  /**
   * @param dataset
   */
  public static void setSelectedDataset(IFolder dataset) {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IWorkbenchPage activePage = window.getActivePage();
    IViewReference[] views = activePage.getViewReferences();

    for (IViewReference viewReference : views) {
      IViewPart view = viewReference.getView(true);

      if (view instanceof DatasetNavigator) {
        DatasetNavigator datasetNavigator = (DatasetNavigator) view;
        datasetNavigator.setRoot(dataset);
      
      } else if (view instanceof NavigationView) {
        ((NavigationView)view).setResource(dataset);
      }
    }

    // also open the metadata editors but minimize them to start
    CmsPath metadataFolderPath = dataset.getPath().append("Metadata");
    IResourceManager mgr = ResourcesPlugin.getDefault().getResourceManager();
    IFolder metadataFolder = (IFolder) mgr.getResource(metadataFolderPath);
    for (IResource child : metadataFolder.getChildren()) {
      if (child instanceof IFile) {
        IFile metadataFile = (IFile) child;
        VeloEditorUtil.openFilesInDefaultEditor(false, window, metadataFile);
      }
    }

    IEditorReference[] editors = window.getActivePage().getEditorReferences();
    for (IEditorReference ref : editors) {
      window.getActivePage().setPartState(ref, IWorkbenchPage.STATE_MINIMIZED);
    }
  }

  /**
   * @return
   */
  public static IFolder getSelectedDatasetInActiveWindow() {
    IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    return getSelectedDataset(window);
  }

  public static IFolder getSelectedDataset(IWorkbenchWindow window) {
    // fist just try the active page
    IWorkbenchPage activePage = window.getActivePage();

    // activePage.getOpenPerspectives()
    // activePage.setPerspective(perspective);

    IFolder dataset = getSelectedDataset(activePage);
    if (dataset == null) {
      // TODO this code isn't working, getPages is only returning the active page. So if someone
      // has the dataset editing perspective opened, but is on the my workspace perspective and tries
      // to open the same dataset again, it opens it up again in another instance of the dataset editing perspective
      // stupid eclipse doesn't have in their API a way to get all 'opened' perspetives :(
      // see https://www.eclipse.org/forums/index.php/t/86121/
      // so I need to see if I can instead find all instances of the dataset navigator view, and if one has
      // this dataset as the root, see if I can switch to that perspective

      // for (IWorkbenchPage page : window.getPages()) {
      // dataset = getSelectedDataset(page);
      // if(dataset != null){
      // return dataset;
      // }
      // }

      List<IWorkbenchPage> pages = DatasetsPlugin.getDefault().getOpenedDatasetEditorPages();
      for (IWorkbenchPage page : pages) {
        dataset = getSelectedDataset(page);
        if (dataset != null) {
          return dataset;
        }
      }

    } else {
      return dataset;
    }
    
    //if dataset is still null, return the last one that was selected to open as the dataset explorer view might be loaded yet
    return DatasetsPlugin.getLastOpenedDataset();
  }

  private static IFolder getSelectedDataset(IWorkbenchPage page) {
    IViewPart view = findDatasetNaviagtorView(page);

    if (view != null) {
      DatasetNavigator datasetNavigator = (DatasetNavigator) view;
      if (datasetNavigator.getTreeExplorer() != null && datasetNavigator.getTreeExplorer().getRoot() != null) {
        return (IFolder)datasetNavigator.getTreeExplorer().getRoot();
      }
    }
    return null;
  }

  public static IViewPart findDatasetNaviagtorView(IWorkbenchPage page) {
    IViewReference[] views = page.getViewReferences();

    for (IViewReference viewReference : views) {
      IViewPart view = viewReference.getView(true);

      if (view instanceof DatasetNavigator) {
        return (DatasetNavigator) view;
      }
    }
    return null;
  }

  public static IWorkbenchWindow getDatasetWindow(IFolder dataset) {
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    for (IWorkbenchWindow openedWindow : windows) {
      IFolder windowDataset = DatasetUtil.getSelectedDataset(openedWindow);
      if (dataset.equals(windowDataset)) {
        return openedWindow;
      }
    }
    return null;
  }

  public static void openDatasetInNewWindow(IFolder dataset) {
    try {
      //have to temporarily house the selected dataset somewhere.  Views in dataset editing perspective need it when they load
      DatasetsPlugin.setLastOpenedDataset(dataset);
      // open editor perspective in new window
      IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      window = PlatformUI.getWorkbench().openWorkbenchWindow(DatasetEditing.ID, null);
      window.getShell().forceActive();
    } catch (WorkbenchException e) {
      ToolErrorHandler.handleError("An error occurred opening dataset.", e, true);
    }

    DatasetUtil.setSelectedDataset(dataset);
  }

}
