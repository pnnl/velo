package gov.pnnl.velo.dataset.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.security.PermissionsForm;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.wizards.ResourceTreeDialog;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;

/**
 * This wizard page is a mashup of the new folder wizard page and the permissions dialog. Code was copied/pasted from those wizards into this one
 */
public class NewDatasetWizardPage extends WizardPage {

  private PermissionsForm permissionsForm;
  private Composite body;
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();
  private IResource containerFolder;
  private Text destinationText;
  private ISelection selection;
  private IWorkbenchWindow window;
  private Text datasetFolderNameText;
  private Text datasetTitleText;

  private final static org.apache.log4j.Logger logger = CatLogger.getLogger(NewDatasetWizardPage.class);

  public NewDatasetWizardPage(ISelection selection, IWorkbenchWindow window, String pageName) {
    super(pageName);
    this.window = window;
    this.selection = selection;
  }

  @Override
  public void createControl(Composite parent) {
    this.body = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.numColumns = 3;
    layout.marginHeight = 10;
    layout.marginWidth = 16;
    layout.horizontalSpacing = 16;
    layout.verticalSpacing = 12;
    body.setLayout(layout);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    body.setLayoutData(gd);

    Label destinationLabel = new Label(body, SWT.BOLD);
    destinationLabel.setText("Destination:");
    destinationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));

    destinationText = new Text(body, SWT.BORDER | SWT.SINGLE);
    destinationText.setEditable(true);
    final GridData gd_1 = new GridData(GridData.FILL_HORIZONTAL);
    destinationText.setLayoutData(gd_1);
    destinationText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        setPageComplete(isPageComplete());
      }
    });

    final Button browseButton = new Button(body, SWT.NONE);
    browseButton.setLayoutData(new GridData(76, SWT.DEFAULT));
    browseButton.setText("B&rowse...");
    browseButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(final SelectionEvent e) {
        ResourceTreeDialog resourceDialog = new ResourceTreeDialog(window.getShell(), ResourcesPlugin.getResourceManager().getRoot(), false, false);
        resourceDialog.create();
        resourceDialog.setTitle("Select Parent Folder");
        resourceDialog.setMessage("Select the destination folder for this new folder.");

        IResource selResource = null;
        // if a folder is already selected, use it to start the browse tree
        if (isParentFolderValid()) {
          try {
            selResource = mgr.getResource(getParentFolder());
          } catch (ResourceException exception) {
            selResource = mgr.getHomeFolder();
          }
        }

        if (selResource != null) {
          resourceDialog.setInitialSelection(selResource);
        }
        if (resourceDialog.open() == Dialog.OK && resourceDialog.getSelectedResource() != null) {
          containerFolder = resourceDialog.getSelectedResource();
          destinationText.setText(resourceDialog.getSelectedResource().getPath().toDisplayString());
          setPageComplete(isPageComplete());
        }
      }
    });

    // Name
    Label nameLabel = new Label(body, SWT.BOLD);
    nameLabel.setText("Dataset Folder Name:");
    nameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
    datasetFolderNameText = new Text(body, SWT.BORDER);
    datasetFolderNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    datasetFolderNameText.setFocus();
    datasetFolderNameText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setPageComplete(isPageComplete());
      }
    });
    // spacer
    new Label(body, SWT.NONE);

    // title
    Label descLabel = new Label(body, SWT.NONE);
    descLabel.setText("Dataset Title:");
    descLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
    datasetTitleText = new Text(body, SWT.BORDER);
    datasetTitleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    datasetTitleText.addModifyListener(new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent e) {
        setPageComplete(isPageComplete());
      }
    });
    // spacer
    new Label(body, SWT.NONE);

    Composite permsComposite = new Composite(body, SWT.NONE);
    GridData permsGd = new GridData(SWT.FILL, SWT.FILL, true, true);
    permsGd.horizontalSpan = 3;
    permsComposite.setLayout(new GridLayout());
    permsComposite.setLayoutData(permsGd);

    permissionsForm = new PermissionsForm(permsComposite, SWT.NONE);
    ((GridLayout) permissionsForm.getLayout()).marginWidth = 16;
    permissionsForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    permissionsForm.setFormBackgroundColor(parent.getBackground());

    setControl(body);

    initialize();
  }

  private void initialize() {
    if (selection != null && selection.isEmpty() == false && selection instanceof IStructuredSelection && containerFolder == null) {
      IStructuredSelection ssel = (IStructuredSelection) selection;
      if (ssel.size() > 1)
        return;

      IFolder container = null;
      Object selectedItem = ssel.getFirstElement();

      // if it is a folder or a file
      if (selectedItem instanceof IResource) {
        IResource resource = RCPUtil.getResource(selectedItem);

        try {
          if (resource instanceof ILinkedResource) {
            resource = ((ILinkedResource) resource).getTarget();
          }
          if (resource instanceof IFile) {
            container = (IFolder) resource.getParent();
          } else if (resource instanceof IFolder) {
            container = (IFolder) resource;
          }
        } catch (ResourceException e) {
          // TODO Auto-generated catch block
          logger.error(e);
        }
      }
      // for Team or User
      else if (selectedItem instanceof IProfilable) {
        IProfilable theItem = (IProfilable) selectedItem;
        try {
          container = (IFolder) mgr.getResource(theItem.getHomeFolder());
        } catch (ResourceException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      } else // can this ever happen?
      {
        logger.error("Unfamiliar selected Item: " + selectedItem.getClass());
      }

      if (container != null) {
        this.destinationText.setText(container.getPath().toDisplayString());
        this.containerFolder = container;
      }

    } else if (containerFolder != null) {
      this.destinationText.setText(containerFolder.getPath().toDisplayString());
    }

  }

  /**
   * Tests if the current workbench selection is a suitable container to use.
   * 
   * @return boolean
   */
  private boolean isParentFolderValid() {
    if (this.destinationText.getText().length() > 0) {
      return true;
    } else
      return false;
  }

  /**
   * Method getDestination.
   * 
   * @return String
   */
  public String getDestination() {
    return destinationText.getText();
  }

  /**
   * Method getDatasetName
   * 
   * @return String
   */
  public String getDatasetName() {
    return datasetFolderNameText.getText();
  }

  /**
   * Method getDatasetTitle
   * 
   * @return String
   */
  public String getDatasetTitle() {
    return datasetTitleText.getText();
  }

  /**
   * Method getParentFolder.
   * 
   * @return CmsPath
   */
  public CmsPath getParentFolder() {
    return new CmsPath(getDestination());
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    loadACL();
  }

  private void loadACL() {
    try {
      permissionsForm.loadPermissions(((NewDatasetWizard) getWizard()).getACL());
    } catch (Throwable e) {
      StatusUtil.handleStatus("Error loading permissions.", e, StatusManager.SHOW);
    }
  }

  public ACL getPermissions() {
    ACL permission = permissionsForm.getPermissions();
    return permission;
  }

  public boolean isPageComplete() {
    boolean pageComplete = true;

    boolean nameSet = !datasetFolderNameText.getText().isEmpty();

    if (!nameSet) {
      setErrorMessage("No dataset folder name defined.");
      pageComplete = false;
    } else {
      setErrorMessage(null);
    }

    return pageComplete;
  }
}
