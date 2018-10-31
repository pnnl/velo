package gov.pnnl.velo.dataset.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.dataset.DatasetsPlugin;
import gov.pnnl.velo.dataset.util.DatasetConstants;
import gov.pnnl.velo.dataset.util.DatasetUtil;
import gov.pnnl.velo.model.ACL;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

public class NewDatasetWizard extends Wizard implements INewWizard {
  public static final String ID = NewDatasetWizard.class.getCanonicalName();

  private final static org.apache.log4j.Logger logger = CatLogger.getLogger(NewDatasetWizard.class);

  private ACL acl;
  private IWorkbenchWindow workbenchWindow;
  private NewDatasetWizardPage newDatasetPage;
  private IStructuredSelection selection;
  private boolean success;
  private IFolder datasetFolder;

  public NewDatasetWizard() {
    this("New Dataset");
  }

  public NewDatasetWizard(String title) {
    super();
    setWindowTitle(title);
    setNeedsProgressMonitor(true);
    setTitleBarColor(new RGB(2, 43, 43));
  }

  @Override
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.workbenchWindow = workbench.getActiveWorkbenchWindow();
    this.selection = selection;
  }

  public void addPages() {
    this.getShell().setSize(500, 770); // have to set the size or the permisions composite doesn't show the 'help' text when selecting a permission level
    // Point parentLocation = getShell().getParent().getLocation();
    // this.getShell().setLocation(parentLocation.x + 200, parentLocation.y + 200);

    setDefaultPageImageDescriptor(DatasetsPlugin.getDefault().getImageDescriptor("index.png", 48));

    this.newDatasetPage = new NewDatasetWizardPage(selection, workbenchWindow, "Create a new dataset.");
    addPage(newDatasetPage);
  }

  /**
   * This method is called when 'Finish' button is pressed in the wizard. We will create an operation and run it using wizard as execution context.
   * 
   * @return boolean
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    this.success = false;
    final CmsPath parentFolder = newDatasetPage.getParentFolder();
    final String fileName = newDatasetPage.getDatasetName();
    final String title = newDatasetPage.getDatasetTitle();
    this.acl = newDatasetPage.getPermissions();

    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          success = doFinish(parentFolder, fileName, title, monitor);
        } finally {
          monitor.done();
        }

      }
    };
    try {
      getContainer().run(true, false, op);

      // Don't do this as users found irritating
//      if (success) {
//        DatasetUtil.openDatasetInNewWindow(datasetFolder);
//      }
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      Throwable realException = e.getCause();
      logger.error("Error", realException);
      ToolErrorHandler.handleError("An unexpected error occurred.", e, true);
      return false;
    }

    return success;
  }

  /**
   * The worker method. It will find the container, create the file if missing or just replace its contents, and open the editor on the newly created file.
   * 
   * @param parentFolder
   *          CmsPath
   * @param newFolderName
   *          String
   * @param monitor
   *          IProgressMonitor
   * @return boolean
   */
  private boolean doFinish(CmsPath parentFolder, String newFolderName, String title, IProgressMonitor monitor) {
    monitor.beginTask("Creating " + newFolderName, 2);
    CmsPath newFolderPath = parentFolder.append(newFolderName);
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    try {
      if (mgr.resourceExists(newFolderPath)) {
        String errMsg = "A folder with the same name already exists.  Rename the new folder and try again.";
        ToolErrorHandler.handleError(errMsg, null, true);
        return false;
      }

      this.datasetFolder = mgr.createFolder(newFolderPath);
      mgr.addAspect(newFolderPath, DatasetConstants.ASPECT_DATASET);
      mgr.addAspect(newFolderPath, VeloConstants.ASPECT_WEB_VIEW);
      mgr.setProperty(newFolderPath, VeloConstants.PROP_WEB_VIEW_URL, DatasetUtil.getDatasetLandingPageURL(datasetFolder.getPropertyAsString(VeloConstants.PROP_UUID)));

      if (title != null && title.length() > 0) {
        mgr.setProperty(newFolderPath, VeloConstants.PROP_TITLE, title);
      }

      // placeholder code to create dataset tree for now while server is still geting implemented
      mgr.createFolder(newFolderPath.append("Data"));
      monitor.worked(1);
      mgr.createFolder(newFolderPath.append("Metadata"));
      monitor.worked(1);
      // create a temp file to init dataset.xml's content using title entered from wizard page:
      File tmp = File.createTempFile("dataset", ".xml");
      FileUtils.writeStringToFile(tmp, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<datasetMetadata>\n<citationInformation>\n<title>" + StringEscapeUtils.escapeXml(title) + "</title>\n</citationInformation>\n</datasetMetadata>");
      mgr.createFile(newFolderPath.append("Metadata").append("dataset.xml"), tmp);
      monitor.worked(1);

      // set permissions on the dataset
      monitor.subTask("Setting Permissions");
      setPermissions(newFolderPath);
      monitor.worked(1);

    } catch (Throwable e) {
      String errMsg = "An error occurred creating the dataset.";
      ToolErrorHandler.handleError(errMsg, e, true);
      return false;
    }
    return true;
  }

  public void setACL(ACL acl) {
    this.acl = acl;
  }

  public ACL getACL() {
    if (acl == null) {
      this.acl = new ACL();
      acl.setInheritPermissions(false);
      acl.setOwner(ResourcesPlugin.getSecurityManager().getActiveUser().getUsername());
      acl.setNodePath("/dataset");// fake path since this node hasn't been added yet
    }
    return acl;
  }

  private void setPermissions(CmsPath datasetPath) {
    ISecurityManager smgr = ResourcesPlugin.getSecurityManager();
    acl.setNodePath(datasetPath.toAssociationNamePath());
    smgr.setPermissions(new ACL[] { acl });
  }

}
