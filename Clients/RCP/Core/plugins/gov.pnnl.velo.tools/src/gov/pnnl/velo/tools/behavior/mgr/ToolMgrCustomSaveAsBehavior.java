/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomSaveAsBehavior extends ToolMgrBehavior implements CustomSaveAsBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior#saveAs(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean saveAs(IResource resource) throws RuntimeException {
    return toolManager.saveAs(resource);
  }

}
