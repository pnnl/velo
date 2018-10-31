/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import org.eclipse.jface.viewers.IStructuredSelection;

import gov.pnnl.velo.ui.commands.CustomExportBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomExportBehavior extends ToolMgrBehavior implements CustomExportBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.commands.CustomExportBehavior#export(org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public boolean export(IStructuredSelection selection) throws RuntimeException {
    return toolManager.export(selection);
  }

}
