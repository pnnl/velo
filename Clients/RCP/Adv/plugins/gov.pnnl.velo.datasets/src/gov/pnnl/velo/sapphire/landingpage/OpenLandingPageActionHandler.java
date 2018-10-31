package gov.pnnl.velo.sapphire.landingpage;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ISetSelectionTarget;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.rcp.ResourceStructuredSelection;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class OpenLandingPageActionHandler extends SapphireActionHandler {
  @Override
  protected Object run(final Presentation context) {
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
          
          // We are opening in Eclipse's internal web browser view so we can see the address bar, so
          // users can copy the URL
          IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
          ISetSelectionTarget browser = (ISetSelectionTarget)window.getActivePage().showView("org.eclipse.ui.browser.view", null, IWorkbenchPage.VIEW_ACTIVATE);
          
          IFolder dataset = DatasetUtil.getSelectedDatasetInActiveWindow();
          ISelection selection = new ResourceStructuredSelection(dataset);
          
          browser. selectReveal(selection);      
          
        } catch (Exception e) {
          // ignore
        }
      }
    });

    return null;
  }

}
