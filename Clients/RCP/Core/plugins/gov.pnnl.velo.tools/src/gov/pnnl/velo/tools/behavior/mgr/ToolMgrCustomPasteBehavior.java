/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomPasteBehavior extends ToolMgrBehavior implements CustomPasteBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior#paste(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource, boolean)
   */
  @Override
  public boolean paste(IResource source, IResource destinationParent, boolean isMove) throws RuntimeException {
    return toolManager.paste(source, destinationParent, isMove);
  }

}
