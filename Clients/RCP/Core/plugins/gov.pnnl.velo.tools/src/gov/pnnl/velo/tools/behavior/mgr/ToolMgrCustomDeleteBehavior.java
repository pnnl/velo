/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomDeleteBehavior extends ToolMgrBehavior implements CustomDeleteBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior#delete(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean delete(IResource source) throws RuntimeException {
    return toolManager.delete(source);
  }

}
