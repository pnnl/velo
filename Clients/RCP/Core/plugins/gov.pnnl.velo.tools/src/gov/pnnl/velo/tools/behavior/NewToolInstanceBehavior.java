package gov.pnnl.velo.tools.behavior;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;

import java.util.List;

public interface NewToolInstanceBehavior extends ToolBehavior {

  /**
   * Return true if you can create a new tool instance from the given
   * selection.
   * @param selectedResources
   * @return
   */
  public boolean canCreateNewInstance(List<IResource> selectedResources);
  
  /**
   * Create a new tool instance with the given name based on the given selection.  
   * Return the tool's working directory
   * @param selectedResources
   * @return
   */
  public IFolder createNewInstance(List<IResource> selectedResources);

}
