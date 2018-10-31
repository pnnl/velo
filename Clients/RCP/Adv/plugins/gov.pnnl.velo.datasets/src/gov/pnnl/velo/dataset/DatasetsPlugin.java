package gov.pnnl.velo.dataset;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.CatAbstractUIPlugin;
import gov.pnnl.velo.dataset.perspectives.DatasetEditing;

/**
 * The activator class controls the plug-in life cycle
 */
public class DatasetsPlugin extends CatAbstractUIPlugin implements IPerspectiveListener, IPartListener {

  public static final String IMAGE_DATASET_NO_DOI = "index-red.png";
  public static final String IMAGE_DATASET_FINAL_DOI = "index-green.png";
  public static final String IMAGE_DATASET_DRAFT_DOI = "index-yellow.png";
  public static final String IMAGE_CHECKED = "check2.png";
  public static final String IMAGE_HELP = "help2.png";

  // The plug-in ID
  public static final String PLUGIN_ID = "gov.pnnl.velo.datasets"; //$NON-NLS-1$

  // The shared instance
  private static DatasetsPlugin plugin;
  
  //have to temporarily house the selected dataset somewhere.  Views in dataset editing perspective need it when they load
  private static IFolder lastOpenedDataset;

  private static List<IWorkbenchPage> openedDatasetEditorPages = new ArrayList<IWorkbenchPage>();

  /**
   * The constructor
   */
  public DatasetsPlugin() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
    addPerspectiveListener();
  }

  private void addPerspectiveListener() {
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
   */
  public void stop(BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  /**
   * Returns the shared instance
   *
   * @return the shared instance
   */
  public static DatasetsPlugin getDefault() {
    return plugin;
  }

  public static IFolder getLastOpenedDataset() {
    return lastOpenedDataset;
  }
  
  public static void setLastOpenedDataset(IFolder dataset){
    lastOpenedDataset = dataset;
  }
  
  public static List<IWorkbenchPage> getOpenedDatasetEditorPages() {
    return openedDatasetEditorPages;
  }

  // listening to perspective/part changes in attempt to hide/show views programmatically in Dataset Editing Perspective

  @Override
  public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    // System.out.println("perspectiveActivated " + perspective.getId());

    // when the perspective first opens, set the selected dataset as the root and hide the editor area
    // this code does not always work...
    // if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID) && page.isEditorAreaVisible() && page.getEditorReferences().length == 0) {
    // System.out.println("perspectiveActivated setting editor area visible to false");
    // page.setEditorAreaVisible(false);
    // }

    // only works if at least one file is opened in an editor
    // IEditorReference[] editors = page.getEditorReferences();
    // for (IEditorReference ref : editors) {
    // page.setPartState(ref, IWorkbenchPage.STATE_MINIMIZED);
    // System.out.println("setting editor part state to minimized");
    // }
    //

  }

  @Override
  public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {

    // does not work for trying to hide/minimize editor area when perspective first opened
    // when the perspective first opens, set the selected dataset as the root and hide the editor area
    // if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID) && page.isEditorAreaVisible() && page.getEditorReferences().length == 0) {
    // page.setEditorAreaVisible(false);
    // System.out.println("perspectiveChanged setting editor area visible to false");
    // }
  }

  @Override
  public void partClosed(IWorkbenchPart part) {
    // hide editor area if no files are being edited
    IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID) && page.isEditorAreaVisible() && page.getEditorReferences().length == 0) {
      page.setEditorAreaVisible(false);

      // System.out.println("partClosed setting editor area visible to false");
    }
  }

  @Override
  public void partDeactivated(IWorkbenchPart part) {
    // System.out.println("partDeactivated " + part.getTitle());
  }

  @Override
  public void partActivated(IWorkbenchPart part) {
    // System.out.println("partActivated " + part.getTitle());
  }

  @Override
  public void partBroughtToTop(IWorkbenchPart part) {
    // System.out.println("partBroughtToTop " + part.getTitle());
  }

  @Override
  public void partOpened(IWorkbenchPart part) {
    // System.out.println("partOpened " + part.getTitle());

    // IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    // IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    // if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID) && page.isEditorAreaVisible() && page.getEditorReferences().length == 0) {
    // page.setEditorAreaVisible(false);
    //
    // System.out.println("partOpened setting editor area visible to false");
    // }
  }

}
