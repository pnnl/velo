/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import java.io.File;

import gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior;

/**
 * @author d3k339
 *
 */
public class ToolMgrCustomCompareBehavior extends ToolMgrBehavior implements CustomCompareBehavior {

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior#preprocessFiles(java.io.File, java.io.File)
   */
  @Override
  public boolean preprocessFiles(File file1, File file2) throws RuntimeException {
    return toolManager.preprocessFiles(file1, file2);
  }

}
