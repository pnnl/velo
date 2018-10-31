/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomDoubleClickBehavior extends ToolMgrBehavior implements CustomDoubleClickBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior#doubleClick(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean doubleClick(IResource source) throws RuntimeException {
    return toolManager.doubleClick(source);
  }

}
