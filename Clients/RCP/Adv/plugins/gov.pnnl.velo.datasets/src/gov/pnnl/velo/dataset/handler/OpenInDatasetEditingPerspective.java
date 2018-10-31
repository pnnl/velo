package gov.pnnl.velo.dataset.handler;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class OpenInDatasetEditingPerspective extends AbstractHandler {
  private Logger logger = CatLogger.getLogger(this.getClass());

  public OpenInDatasetEditingPerspective() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      IStructuredSelection catFile = RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));
      IFolder dataset = (IFolder) catFile.getFirstElement();
      //IWorkbenchWindow openedWindow = DatasetUtil.getDatasetWindow(dataset);
      // TODO this does not work (it does not find the already opened dataset) if the dataset editing perspective
      // is opened in the main window for a dataset, then the user goes back to my workspace and right clicks on the same dataset and selects "Edit Dataset' action

//      if (openedWindow != null) {
//        openedWindow.getShell().forceActive();
//      } else {
        DatasetUtil.openDatasetInNewWindow(dataset);
//      }
    } catch (Throwable e) {
      StatusUtil.handleStatus("Failed to open editor.", e, StatusManager.SHOW);
    }

    return null;
  }
}
