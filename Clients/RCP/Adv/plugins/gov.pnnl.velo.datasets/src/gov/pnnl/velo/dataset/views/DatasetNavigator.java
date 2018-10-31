package gov.pnnl.velo.dataset.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.WorkbenchException;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.TreeExplorerView;
import gov.pnnl.cat.ui.rcp.wizards.ResourceTreeDialog;
import gov.pnnl.velo.dataset.DatasetsPlugin;
import gov.pnnl.velo.dataset.perspectives.DatasetEditing;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class DatasetNavigator extends TreeExplorerView implements IPerspectiveListener3 {
  public static final String ID = DatasetNavigator.class.getName();

  public DatasetNavigator() {
    super();
  }

  /**
   * Method getRoot.
   * 
   * @return Object
   */
  @Override
  public Object getDefaultRoot() {
    if (treeExplorer != null) {
      return treeExplorer.getRoot();
    }

    return null; // set later
  }

  /**
   * Method isRootIncluded.
   * 
   * @return boolean
   */
  @Override
  public boolean isRootIncluded() {
    return true;
  }

  // override the message composite as instead of a 'create one' link we want users to 'browse' to their dataset:

  @Override
  protected void createMessageView(final Composite parent) {
    this.messageComposite = new Composite(parent, 0);
    this.messageComposite.setLayout(new FillLayout());

    this.page = new Composite(messageComposite, SWT.FILL);
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginLeft = 2;
    gridLayout.marginHeight = 4;
    page.setLayout(gridLayout);

    Link link = new Link(page, SWT.LEFT | SWT.TOP | SWT.WRAP);
    link.setText("Please select a Dataset. \n<A>Browse...</A>");
    link.setSize(140, 40);
    link.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        IResourceManager mgr = ResourcesPlugin.getResourceManager();
        ResourceTreeDialog dialog = new ResourceTreeDialog(page.getShell(), mgr.getRoot(), false, false);
        dialog.create();
        dialog.setTitle("Select Dataset");
        dialog.setMessage("Select the dataset you would like to edit.");
        // dialog.setInitialSelection ... should we fisrt search for the dataset aspect and just present them as a flat list instead?

        if (dialog.open() == Dialog.OK && dialog.getSelectedResource() != null) {
          IResource dataset = dialog.getSelectedResource();
          IWorkbenchWindow openedWindow = DatasetUtil.getDatasetWindow((IFolder) dataset);
          // TODO this does not work (it does not find the already opened dataset) if the dataset editing perspective
          // is opened in the main window for a dataset, then the user goes back to my workspace and right clicks on the same dataset and selects "Edit Dataset' action
          if (openedWindow != null) {
            boolean showDataset = MessageDialog.openQuestion(parent.getShell(), "Dataset already opened.", "The selected dataset is already opened for editing.  Would you like to switch the view to the opened dataset?");
            if (showDataset) {
              // this is not working if user opens dataset in seperate window first, then goes back to main window and opens dataset editing perspective, and browses
              // to the already opened dataset. It detects its already open but when 'yes' is selected to switch to the already opened dataset
              // the other window is not brought on top.
              openedWindow.getShell().forceActive();
              // also make sure the Dataset Editing perspective is opened
              try {
                openedWindow.getWorkbench().showPerspective(DatasetEditing.ID, openedWindow);
              } catch (WorkbenchException e) {
                e.printStackTrace();
              }
            }
          } else {
            DatasetUtil.setSelectedDataset((IFolder) dataset);// this also opens the metadata editors and minimizes them
            stackLayout.topControl = treeComposite;
            mainComp.layout();
          }
        }
      }
    });
  }

  @Override
  public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID)) {
      DatasetsPlugin.getDefault().getOpenedDatasetEditorPages().add(page);
    }
  }

  @Override
  public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    if (perspective.getId().equalsIgnoreCase(DatasetEditing.ID)) {
      // TODO - this is never called so need to listen to another event in order to remove the page from our list
      DatasetsPlugin.getDefault().getOpenedDatasetEditorPages().remove(page);
    }
  }

  @Override
  public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
  }

  @Override
  public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
  }

  @Override
  public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
  }

}