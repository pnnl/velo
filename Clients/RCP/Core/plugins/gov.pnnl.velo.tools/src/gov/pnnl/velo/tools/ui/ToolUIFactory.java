package gov.pnnl.velo.tools.ui;

import java.util.List;

import org.springframework.util.ErrorHandler;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.tools.Tool;

/**
 * Bridge between tool definition and UI instantiation.
 * @author D3K339
 *
 */
public interface ToolUIFactory {
    
  /**
   * This is the method that actually creates the UI piece - don't create
   * any UI objects until this method is called to avoid threading deadlock
   * issues between swing and swt.
   * Since this method will likely run asynchronously to avoid threading issues,
   * we pass listeners so any errors can be resolved properly by the caller.
   * @param tool
   * @param selectedResources
   * @param listeners
   */
  public void instantiateUI(Tool tool, List<IResource> selectedResources, List<ToolUIListener>listeners);
  
  
}
