/**
 * 
 */
package gov.pnnl.velo.tools.behavior.Default;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.core.util.ThreadUtils;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior;
import gov.pnnl.velo.ui.validators.NotEmptyStringValidator;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * @author d3k339
 *
 */
public class NewToolInstanceBehaviorDefault implements NewToolInstanceBehavior {
  
  private Tool tool;
  

  public NewToolInstanceBehaviorDefault() {
    super();
  }
  
  @Override
  public void setTool(Tool tool) {
    this.tool = tool;
  }
  
  /**
   * @return the tool
   */
  public Tool getTool() {
    return tool;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior#canCreateNewInstance(java.util.List)
   */
  @Override
  public boolean canCreateNewInstance(List<IResource> selectedResources) {
    
    // default behavior is to allow create if one item is selected and it is a folder and it
    // is not already an instance of the same tool
    boolean canNew = false;
    IFolder parent = getParentFolder(selectedResources);
    if(parent != null) {
      String parentMimetype = ResourcesPlugin.getResourceManager().getProperty(parent.getPath(), VeloConstants.PROP_MIMETYPE);
      if(!tool.getMimetype().equalsIgnoreCase(parentMimetype)) {
        canNew = true;
      }
    }
    return canNew;
  }
  
  /**
   * Based on the selection, determine the parent folder where the new tool would be created.
   * Default is to pick the first folder in the selection, and create the tool under that
   * @param selectedResources
   * @return
   */
  protected IFolder getParentFolder(List<IResource> selectedResources) {
    IFolder parent = null;
    for(IResource resource : selectedResources) {
      if(resource instanceof IFolder) {
        // The tool will go under the first selected resource
        parent = (IFolder)resource;
        break;
      }
    }
    return parent;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior#createNewInstance(java.util.List)
   */
  @Override
  public IFolder createNewInstance(final List<IResource> selectedResources) {
    final boolean swing = ThreadUtils.isAwtThread();
    IFolder newToolWorkingDir = null;
    IFolder parent = getParentFolder(selectedResources);
    if(parent == null) {
      return newToolWorkingDir;
    }
    boolean doit = false;
    String newName = null;
    String title = "Creating " + tool.getName() + " in " + parent.getPath().toDisplayString();
    String msg = tool.getName() + " Name:";
    
    if(swing) { // TODO:
      

    } else { // swt thread
      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      InputDialog dialog = new InputDialog(shell, title, msg, null, new NotEmptyStringValidator());
      if(dialog.open() == Dialog.OK) {
        doit = true;
        newName = dialog.getValue();
      }
    }
    
    if(doit) {
      final String name = newName;
      final IFolder[] createdFolders = new IFolder[1];
      final IFolder finalParent = parent;
      
      if(swing) {
      
      } else {

        // run with progress
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        try {
          service.run(false, false, new IRunnableWithProgress(){
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
              // Create folder with tool mimetype
              createdFolders[0] = createNewToolWorkingDir(finalParent, name, tool.getMimetype()); 
              postCreateNewToolWorkingDir(selectedResources, createdFolders[0]);
            }
          });
          
        } catch (Throwable e) {
          throw new RuntimeException(e);
        }
      }
      newToolWorkingDir = createdFolders[0];
      
    }

    return newToolWorkingDir;
  }
  
  protected void postCreateNewToolWorkingDir(List<IResource> selectedResources, IFolder toolWorkingDir) {
    
  }
  
  protected IFolder createNewToolWorkingDir(IResource parent, String folderName, String mimetype) {
    IFolder toolWorkingDir = null;
    
    try {
      IResourceManager mgr = ResourcesPlugin.getResourceManager();

      List<Resource> resources = new ArrayList<Resource>();
      CmsPath toolPath = parent.getPath().append(folderName);
      Resource toolFolder = new Resource(VeloConstants.TYPE_FOLDER, toolPath);
      toolFolder.addProperty(VeloConstants.PROP_MIMETYPE, mimetype);
      resources.add(toolFolder);
      
       // Create folders for Inputs and Outputs
      Resource inputsFolder = new Resource(VeloConstants.TYPE_FOLDER, toolPath.append(VeloTifConstants.FOLDER_NAME_INPUTS));
      Resource outputsFolder = new Resource(VeloConstants.TYPE_FOLDER, toolPath.append(VeloTifConstants.FOLDER_NAME_OUTPUTS));
      resources.add(inputsFolder);
      resources.add(outputsFolder);      
      mgr.createFolders(resources);
      toolWorkingDir = (IFolder)mgr.getResource(toolPath);
 
      // request to select the newly created object
      IResource tool = mgr.getResource(toolPath);
      RCPUtil.selectResourceInTree(tool);

    } catch (Throwable e) {
      throw new RuntimeException(e);

    }   
    return toolWorkingDir;
  }


}
