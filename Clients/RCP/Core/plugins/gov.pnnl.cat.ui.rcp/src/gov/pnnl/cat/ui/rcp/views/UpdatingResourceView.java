package gov.pnnl.cat.ui.rcp.views;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

/**
 * View part that automatically hooks into resource selection, change, and deletion events
 * so that the view part can keep in sync.
 * @author D3K339
 *
 */
public abstract class UpdatingResourceView extends ViewPart implements ISelectionListener, IResourceEventListener {

  protected Logger logger = CatLogger.getLogger(getClass());

  protected Composite parent;
  private ISelection lastSelection;

  /**
   * Default constructor
   */
  public UpdatingResourceView() {
    
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    this.parent = parent;
    
    // add myself as a selection listener
    getSite().getPage().addSelectionListener(this);
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);    
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    getSite().getPage().removeSelectionListener(this);
    ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
    super.dispose();
  }

  public void refreshView() {
    IStructuredSelection structuredSelection = (IStructuredSelection)lastSelection;
    IResource resource = RCPUtil.getResource(structuredSelection.getFirstElement());
    drawView(resource);
    
    // make Eclipse refresh the view
    parent.layout(true, true);
  }
  
  /**
   * Redraw the view when a selection changes.
   * @param selection ISelection
   */
  protected abstract void drawView(IResource resource);

  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param selection ISelection
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
   */
  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    // If the selection hasn't changed, don't refresh the view
    if(selection.equals(lastSelection)) {
      return;
    }
    
    // If the selection is empty or not a resource, just leave what was here last
    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return;
      
    } else {
      IStructuredSelection structuredSelection = (IStructuredSelection)selection;
      IResource resource = RCPUtil.getResource(structuredSelection.getFirstElement());

      //there can be selection events that are NOT resources:
      if(resource == null) {
        return;        
      } else {
        lastSelection = selection;
        
        // make sure the resource is in the cache - this could happen for search results, because they aren't in the cache yet
        ResourcesPlugin.getResourceManager().getResource(resource.getPath(), true);        
        drawView(resource);        
      }

    }

  }

  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    IResourceEvent changeEvent = null;
    IStructuredSelection structuredSelection = (IStructuredSelection)lastSelection;
    //this will occur when the rcp is just launched and no selection has yet been made but a notification comes in
    if(structuredSelection == null || structuredSelection.isEmpty()){
      return;
    }
    
    IResource resource = RCPUtil.getResource(structuredSelection.getFirstElement());
    if (resource == null) {
      return;
    }

    for (IResourceEvent event : events) {
      if(event.getPath().equals(resource.getPath())) {
        changeEvent = event;
        break;
      }
    }
    
    if (changeEvent != null) {
      final IResourceEvent finalEvent = changeEvent;
      // refresh the view
      Display.getDefault().asyncExec(new Runnable() {
        @Override
        public void run() {
          IResource resource;
          if(finalEvent.hasChange(IResourceEvent.REMOVED)) {
            resource = null;
            lastSelection = null;
          } else {
            IStructuredSelection structuredSelection = (IStructuredSelection)lastSelection;
            resource = RCPUtil.getResource(structuredSelection.getFirstElement());
          }
          drawView(resource);
        }
      });
    }

  }

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }
}
