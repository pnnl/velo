package gov.pnnl.cat.outlook.dnd;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.outlook.jobs.UploadOutlookEmailJob;
import gov.pnnl.cat.ui.rcp.views.dnd.ExplorerDropSupport;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.ui.statushandlers.StatusManager;

public class OutlookMsgDropTargetListener extends ExplorerDropSupport {
  
  private static Logger logger = CatLogger.getLogger(OutlookMsgDropTargetListener.class);
  private OutlookMsgTransfer outlookTransfer = OutlookMsgTransfer.getInstance();

  public OutlookMsgDropTargetListener(CatViewerContainer explorerContainer) {
    super(explorerContainer);
    // TODO Auto-generated constructor stub
  }

  public void dragEnter(DropTargetEvent event) {
    logger.debug("dragEnter");

    boolean supportsOutlookDnd = false;

    for (int i = 0; i < event.dataTypes.length; i++) {
      if (outlookTransfer.isSupportedType(event.dataTypes[i])){
        event.currentDataType = event.dataTypes[i];
        logger.warn("event.detail = DND.DROP_COPY");
        event.detail = DND.DROP_COPY;
        supportsOutlookDnd = true;
        logger.debug("inspire transfer is supported");
        break;
      }
    }
    
    // don't interfere with other DnD operations (e.g. file or resource
    // transfers)
    if (supportsOutlookDnd) {
      if (event.detail == DND.DROP_DEFAULT) {
        if ((event.operations & DND.DROP_COPY) != 0) {
          logger.warn("event.detail = DND.DROP_COPY");
          event.detail = DND.DROP_COPY;
        } else if ((event.operations & DND.DROP_MOVE) != 0) {
          logger.warn("event.detail = DND.DROP_MOVE");
          event.detail = DND.DROP_MOVE;
        } else {
          logger.warn("event.detail = DND.DROP_NONE");
          event.detail = DND.DROP_NONE;
        }
      }
    }
  }

  public void dragLeave(DropTargetEvent event) {
    logger.debug("dragLeave");
  }

  public void dragOperationChanged(DropTargetEvent event) {

  }

  public void dragOver(DropTargetEvent event) {
    event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
  }

  public void drop(DropTargetEvent event) {
    logger.debug("drop");

    if (!outlookTransfer.isSupportedType(event.currentDataType)) {
      return;
    }

    Object destination = getDropLocation(event);

    if (destination == null) {
      logger.warn("drop location is null!");
    } else if (!(destination instanceof IResource)) {
      logger.warn("Drop location is not a resource");
      
    } else {
      IResource resource = (IResource)destination;
      if (resource instanceof IFile) {
        try {
          resource = resource.getParent();
        } catch (Exception e) {
          logger.error("Could not retrieve parent folder. File: " + resource.getPath());
          ToolErrorHandler.handleError("An unexpected error occurred.", e, true);
        }
      }

      logger.debug("Dropping onto " + resource.getPath());
      
   // Perform a drop of outlook email items
      if (outlookTransfer.isSupportedType(event.currentDataType) && destination != null) {
        try {
          UploadOutlookEmailJob job = (UploadOutlookEmailJob)event.data;
          job.setDestinationFolder((IFolder)destination);
          job.setUser(true);
          job.schedule();
        } catch (Exception e) {
          StatusUtil.handleStatus("Error uploading email.", e, StatusManager.SHOW);
          return;
        }
      }
    }
  }

  public void dropAccept(DropTargetEvent event) {

  }

}
