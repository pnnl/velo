package gov.pnnl.velo.tools.behavior;

import java.util.List;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.velo.tools.ui.ToolUIListener;

public interface OpenToolInstanceBehavior extends CustomDoubleClickBehavior, ToolBehavior {

  /**
   * Can this tool be opened on the selected resources
   * @param selectedResources
   * @return
   */
  public boolean canOpen(List<IResource> selectedResources);

  /**
   * Open the tool on the given selection.
   * @param selectedResources
   * @param externalListener
   */
  public void open(List<IResource> selectedResources, final ToolUIListener externalListener);
  
  public void updateToolReference(IResource oldPath, IResource newPath);

}
