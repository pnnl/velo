package gov.pnnl.velo.sapphire.dataset;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;

import com.centerkey.utils.BareBonesBrowserLaunch;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class OpenGeoWebsiteActionHandler extends SapphireActionHandler {
  @Override
  protected Object run(final Presentation context) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
          BareBonesBrowserLaunch.openURL("http://boundingbox.klokantech.com");  
          
        } catch (Exception e) {
          // ignore
        }
      }
    });

    return null;
  }

}
