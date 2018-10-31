/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.ui.rcp.views.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.actions.ActionUtil;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.DataBrowserRoot;
import gov.pnnl.cat.ui.rcp.views.databrowser.service.FavoritesService;
import gov.pnnl.cat.ui.rcp.views.databrowser.service.FavoritesService;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.CatViewerContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 * TODO: this class is needs to extend ViewerDropAdapter so the logic is correct.
 *
 */
public class ExplorerDropSupport extends DropTargetAdapter {

  private final FileTransfer fileTransfer = FileTransfer.getInstance();
  private final CatResourceTransfer resourceTransfer = CatResourceTransfer.getInstance();

  private CatViewerContainer explorerContainer;

  private Logger logger = CatLogger.getLogger(this.getClass());
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();
  
  /**
   * The last valid operation.  We need to remember the last good operation
   * in the case where the current operation temporarily is not valid (drag over
   * someplace you can't drop).
   */
  private int lastValidOperation;
  
  /**
   * Constructor for ExplorerDropSupport.
   * @param window IWorkbenchWindow
   */
  public ExplorerDropSupport(CatViewerContainer explorerContainer) {
    this.explorerContainer = explorerContainer;
  }
  
  /**
   * Method getDropLocation.
   * @param event DropTargetEvent
   * @return IResource
   */
  public Object getDropLocation(DropTargetEvent event) {
    if (explorerContainer == null) {
      return null;
    }

    Object dropTarget = null;

    if (explorerContainer instanceof TreeExplorer) {
      if (event.item != null) {
        Object o = ((Item) event.item).getData();
        
        if (o instanceof IResource) { // we are dropping into another resource
          dropTarget = o;
        
        } else if (o instanceof GenericContainer) { // we are dropping into a generic container
          
          if( ((GenericContainer)o).getName().equals(DataBrowserRoot.FAVORITES)) { // dropping into favorites
            dropTarget = o;         
          }
        }
      }
      
    } else if (explorerContainer instanceof TableExplorer) {
      if (event.item != null) {
        Object o = ((Item) event.item).getData();
        if (o instanceof IResource) {
          dropTarget = (IResource) o;
        }
      } else {
        // In some cases the table viewer's input will be set to null if
        // the current selection is not applicable to it.
        // We need to handle this case appropriately.
        Object input = explorerContainer.getViewer().getInput();

        if (input != null && input instanceof IResource) {
          // this is the case when they drag over white space over the table
          dropTarget = (IResource) input;
        }
      }
    }
    return dropTarget;
  }
  
  private void evaluateDropTarget(DropTargetEvent event) {
    // TODO: hook in custom behaviors to check whether the current selection can be dropped
    // into the target folder - for now we are allowing all IResources to be dropped into IFolders
    boolean dropAllowed = false;
    IResource destination = null;
    
    if(isSupportedType(event)) {

      Object dropLocation = getDropLocation(event);

      if(dropLocation instanceof IResource) { // drop to resource
        destination = (IResource) dropLocation;
        
        if(destination.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
          
          // only cat transfers can be dropped here, not files from the filesystem
          if(isResourceTransfer(event.currentDataType)) {
            dropAllowed = true;
          }
        
        } else if(destination.isType(IResource.FOLDER)) {
          dropAllowed = true;
        }

      } else if (dropLocation instanceof GenericContainer) { // drop to favorites
        
        // can only drop folders, so we must check that selected source is a folder
        String[] sourcePaths = (String[]) resourceTransfer.nativeToJava(event.currentDataType);
        boolean allFolders = true;
        for(int i = 0; i < sourcePaths.length; i++) {   
          IResource dropSource = mgr.getResource(new CmsPath(sourcePaths[i]));
          if(dropSource.isType(IResource.FOLDER) == false) {
            allFolders = false;
            break;
          }
        }
        dropAllowed = allFolders;
      }
    }
    
    // always remember what was previously requested so we can complete the drop
    // in cases where we drag over a temporary invalid location
    if (event.detail != DND.DROP_NONE) {
      lastValidOperation = event.detail;
    }

    if(!dropAllowed) {
      event.detail = DND.DROP_NONE;     
      event.feedback = DND.FEEDBACK_NONE;
    } else {
      // TODO: we need to see why the event detail for search results is not allowing drop
      if(destination != null && destination.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
        event.detail = DND.DROP_COPY;
      } else {
        event.detail = lastValidOperation;  
      }
      event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOver(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragOver(DropTargetEvent event) {
    evaluateDropTarget(event);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetAdapter#dragEnter(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragEnter(DropTargetEvent event) {
    evaluateDropTarget(event);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.dnd.DropTargetAdapter#dragOperationChanged(org.eclipse.swt.dnd.DropTargetEvent)
   */
  public void dragOperationChanged(DropTargetEvent event) {
    evaluateDropTarget(event);
  }
  
//  private ApplicationWindow getWindow() {
//    return ((ApplicationWindow)PlatformUI.getWorkbench().getActiveWorkbenchWindow());
//  }

  /**
   * Method dragLeave.
   * @param event DropTargetEvent
   * @see org.eclipse.swt.dnd.DropTargetListener#dragLeave(DropTargetEvent)
   */
  public void dragLeave(DropTargetEvent event) {
//    logger.debug("dragLeave");
    //clear out any messages we might have set
    //getWindow().setStatus(null);
  }

  /**
   * Method dropAccept.
   * @param event DropTargetEvent
   * @see org.eclipse.swt.dnd.DropTargetListener#dropAccept(DropTargetEvent)
   */
  public void dropAccept(DropTargetEvent event) {
//    System.out.println("dropAccept: "+event.data);   
    for (int i = 0; i < event.dataTypes.length; i++) {
      if (CatResourceTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
//        event.detail = DND.DROP_NONE;
        break;
      }
      
      // Only allow copy with FileTransfer - we will never do move or link from the 
      // local file system
      if (FileTransfer.getInstance().isSupportedType(event.dataTypes[i])) {
        if (event.detail != DND.DROP_COPY) {
          // Force transfer from file system to be copy
          logger.debug("event.detail = DND.DROP_COPY");
          event.detail = DND.DROP_COPY;
          break;
        }
      }
    }  
  }

  /**
   * Method isSupportedType.
   * @param event DropTargetEvent
   * @return boolean
   */
  private boolean isSupportedType(DropTargetEvent event) {
    boolean supported = resourceTransfer.isSupportedType(event.currentDataType) ||
    fileTransfer.isSupportedType(event.currentDataType);
    
    return supported;
  }

  /**
   * Method drop.
   * @param event DropTargetEvent
   * @see org.eclipse.swt.dnd.DropTargetListener#drop(DropTargetEvent)
   */
  public void drop(final DropTargetEvent event) {

    // only continue if the drop is one of our supported types
    // (could this even happen?)
    if (!isSupportedType(event)) {
      return;
    }

    Object dropLocation = getDropLocation(event);
    
    if(dropLocation instanceof GenericContainer && ((GenericContainer)dropLocation).getName().equals(DataBrowserRoot.FAVORITES)) {
      dropToFavorites((GenericContainer)dropLocation, event);
    
    } else {
      IResource destination = (IResource) dropLocation;
      if(destination.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
        dropToCategory(destination, event);
      } else {
        dropToResource(destination, event);
      }
    }

  }
  
  private void dropToCategory(IResource category, DropTargetEvent event) {
    String[] sourcePaths = (String[]) event.data;
    
    try {
      List<CmsPath> resourcePaths = new ArrayList<CmsPath>();
      for(String pathStr : sourcePaths) {
        resourcePaths.add(new CmsPath(pathStr));
      }
      mgr.addCategories(resourcePaths, category);
      
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Failed to add to category.", e, true);
    }
    
  }

  private void dropToResource(IResource dropLocation, DropTargetEvent event) {
    IFolder destinationFolder = null;
  
    if (dropLocation != null) {
      
      if (dropLocation instanceof IFolder) {
        destinationFolder = ((IFolder) dropLocation);
      
      } else if (dropLocation instanceof IFile) {
        try {
          destinationFolder = (IFolder) dropLocation.getParent();
        } catch (ResourceException e) {
          // TODO Auto-generated catch block
          logger.error(e);
        }
      }

      if(destinationFolder != null) {
        
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
        // Perform drop for a CatResourceTransfer
        if ( isResourceTransfer(event.currentDataType) ) {
          String[] sourcePaths = (String[]) event.data;
          ActionUtil.createDndJob(shell, sourcePaths, destinationFolder, event.detail);
          
        } else if ( isFileTransfer(event.currentDataType) ) {
          // Perform a drop for a fileTransfer
          String[] sourcePaths = (String[]) event.data;
          ActionUtil.createDndJob(shell, sourcePaths, destinationFolder, ActionUtil.UPLOAD);
          
        }
      }
    }
  }
  
  private boolean isResourceTransfer(TransferData transferData) {
    return resourceTransfer.isSupportedType(transferData);
  }
  
  private boolean isFileTransfer(TransferData transferData) {
    return fileTransfer.isSupportedType(transferData);
  }
  
  /**
   * This will only happen if in a tree explorer
   * @param favoritesContainer
   * @param event
   */
  private void dropToFavorites(GenericContainer favoritesContainer, DropTargetEvent event) {
    String[] sourcePaths = (String[]) event.data;
    FavoritesService favoritesService = FavoritesService.getInstance();
    for(String sourcePath : sourcePaths) {
      IFolder favoriteFolder = (IFolder)mgr.getResource(new CmsPath(sourcePath));
      favoritesService.addFavorite(favoriteFolder);
    }
    
    // refresh the tree
    TreeExplorer treeExplorer = (TreeExplorer)explorerContainer;
    treeExplorer.reload();
  }
  
}
