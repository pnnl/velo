/**
 * 
 */
package gov.pnnl.velo.tools.ui.adapters;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IProgressMonitor;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.ui.ToolProgressService;
import gov.pnnl.velo.tools.ui.ToolUI;
import gov.pnnl.velo.tools.ui.ToolUIFactory;
import gov.pnnl.velo.tools.ui.ToolUIListener;

/**
 * Default adapter to instantiate any swing user interface
 * @author D3K339
 *
 */
public class ToolUIFactorySwing implements ToolUIFactory {
  
  protected String uiClass;
  

  public void setUiClass(String uiClass) {
    this.uiClass = uiClass;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.ui.ToolUIFactory#instantiateUI(gov.pnnl.velo.tools.Tool, java.util.List, java.util.List)
   */
  @Override
  public void instantiateUI(final Tool tool, final List<IResource> selectedResources, final List<ToolUIListener> listeners) {
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
       instantiateUIInternal(tool, selectedResources, listeners);
      }
    });
    
  }
  
  protected void instantiateUIInternal(final Tool tool, final List<IResource> selectedResources, final List<ToolUIListener> listeners) {
    IProgressMonitor monitor = null;
    try {
      Class clazz = Class.forName(uiClass);
      ToolUI ui = (ToolUI)clazz.newInstance();
      final ToolUI finalUI = ui;
      monitor = ToolProgressService.getProgressMonitor();
      
      // pass any custom parameters to the UI instance
      initializeParameters(ui);
      
      // Do default tool initialization
      ui.initializeContext(tool, selectedResources);
      
      // now add our window close listener
      WindowAdapter windowListener = new WindowAdapter() {
        @Override
        public void windowClosed(WindowEvent e) {
          for(ToolUIListener toolListener : listeners) {
            toolListener.toolClosed(finalUI);
          }
        }
      };
      // we can assume any Swing UI will be an instance of awt.Window
      ((Window)ui).addWindowListener(windowListener);
      
      // TODO: figure out how/if we need to deal with ui change events outside the jframe

      // now fire our tool created events
      for(ToolUIListener toolListener : listeners) {
        toolListener.toolCreated(finalUI);
      }
      
    } catch (Throwable e) {
      // now fire our tool failed events
      for(ToolUIListener toolListener : listeners) {
        toolListener.toolFailed(e);
      }    
      
    } finally {
      if(monitor != null) {
        monitor.setCanceled(true);
      }
    }
  }

  /**
   * Default is to do nothing - subclasses should override to initialize custom config params set from the xml
   * @param ui
   */
  protected void initializeParameters(ToolUI ui) {
    
  }


}
