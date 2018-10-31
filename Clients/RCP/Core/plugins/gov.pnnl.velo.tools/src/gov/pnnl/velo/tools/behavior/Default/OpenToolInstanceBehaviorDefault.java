/**
 * 
 */
package gov.pnnl.velo.tools.behavior.Default;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior;
import gov.pnnl.velo.tools.ui.ToolUI;
import gov.pnnl.velo.tools.ui.ToolUIFactory;
import gov.pnnl.velo.tools.ui.ToolUIListener;
import gov.pnnl.velo.util.VeloConstants;

/**
 * @author d3k339
 *
 */
public class OpenToolInstanceBehaviorDefault implements OpenToolInstanceBehavior {
  
  // Default behavior is to only allow the tool to be opened once for a given tool instance folder
  private static Map<IResource, ToolUI> openTools = new HashMap<IResource, ToolUI>();

  private Tool tool;
  
  /**
   * @param tool
   */
  public OpenToolInstanceBehaviorDefault() {
    super();
  }
  
  @Override
  public void setTool(Tool tool) {
    this.tool = tool;
  }
  
  public Tool getTool() {
    return tool;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior#doubleClick(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean doubleClick(IResource resource) {
    boolean opened = false;
    
    List<IResource> selectedResources = new ArrayList<IResource>();
    selectedResources.add(resource);
    if(canOpen(selectedResources)) {
      open(selectedResources, null);
      opened = true;
    }

    return opened;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior#canOpen(java.util.List)
   */
  @Override
  public boolean canOpen(List<IResource> selectedResources) {
    // default behavior is to allow open if one item is selected and it is a folder and it
    // has the tool's mimetype
    boolean canOpen = false;
    IFolder workingDir = getToolWorkingDir(selectedResources);
    if(workingDir != null) {
      String mimetype = ResourcesPlugin.getResourceManager().getProperty(workingDir.getPath(), VeloConstants.PROP_MIMETYPE);
      if(tool.getMimetype().equalsIgnoreCase(mimetype)) {
        canOpen = true;
      }
    }
    return canOpen;
  }
  
  protected IFolder getToolWorkingDir(List<IResource> selectedResources) {
    IFolder toolWorkingDir = null;
    if(selectedResources.size() == 1 && selectedResources.get(0) instanceof IFolder) {
      toolWorkingDir = (IFolder)selectedResources.get(0);
    }
    return toolWorkingDir;
  }
  

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior#open(java.util.List, gov.pnnl.velo.tools.ui.ToolUIListener)
   */
  @Override
  public void open(List<IResource> selectedResources, final ToolUIListener externalListener) {

    ToolUI toolUI = null;
    final IFolder toolWorkingDir = getToolWorkingDir(selectedResources);
    if(toolWorkingDir == null) {
      return;
    }

    try {
      // First check to see if it is in the map
      toolUI = openTools.get(toolWorkingDir);
      
      if(toolUI == null) {
        // listeners
        List<ToolUIListener> listeners = new ArrayList<ToolUIListener>();
        ToolUIListener listener = new ToolUIListener() {
          @Override
          public void toolClosed(ToolUI toolUI) {
            
            //openTools.remove(toolWorkingDir);
            //reverting what I did in an attempt to fix save As
            openTools.remove(toolUI.getToolInstanceDir());         
          }

          @Override
          public void toolCreated(ToolUI toolUI) {
            //openTools.put(toolWorkingDir, toolUI);
            //reverting what I did in an attempt to fix save As
            openTools.put(toolUI.getToolInstanceDir(),toolUI);  
          }

          @Override
          public void toolFailed(Throwable exception) {
            openTools.remove(toolWorkingDir); // remove from the cache
            throw new RuntimeException(exception);
          }
          
          
        };
        listeners.add(listener);
        if(externalListener != null) {
          listeners.add(externalListener);
        }
        
        ToolUIFactory uiFactory  = (ToolUIFactory) tool.getUiFactory();
        uiFactory.instantiateUI(tool, selectedResources, listeners);
        
      } else {
        toolUI.bringToFront();
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
  
  public void updateToolReference(IResource oldRef, IResource newRef){
    ToolUI toolUI = openTools.remove(oldRef);
    if(toolUI!=null){
      openTools.put(newRef, toolUI);
    }
  }

}
