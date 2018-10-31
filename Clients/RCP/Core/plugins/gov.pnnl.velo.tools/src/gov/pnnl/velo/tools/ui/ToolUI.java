package gov.pnnl.velo.tools.ui;

import java.io.File;
import java.util.List;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.tools.Tool;

/**
 * Generic interface for interacting with a Tool UI
 * @author D3K339
 *
 */
public interface ToolUI {
   
  /**
   * When the tool UI starts up, it can load state based on the 
   * currently selected resources in the repository plus the tool
   * defintion.
   * @param tool - tool definition
   * @param selectedResources
   */
  public void initializeContext(Tool tool, List<IResource> selectedResources);

  /**
   * Get the tool's server-side instance dir.  Could return null
   * if tool doesn't have one.
   * @return
   */
  public IFolder getToolInstanceDir();
  
  public File getToolLocalWorkingDir();
  
  public Tool getToolDefintion();
  
  // bring to front
  public void bringToFront();

  public boolean isDirty();
  public void setDirty(boolean dirty);
  
  public void changeContext(IFolder newToolInstanceDir);
  
}
