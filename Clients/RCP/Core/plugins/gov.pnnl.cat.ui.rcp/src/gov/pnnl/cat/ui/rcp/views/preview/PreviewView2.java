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
package gov.pnnl.cat.ui.rcp.views.preview;

import gov.pnnl.cat.core.internal.resources.events.BatchNotification;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.Random;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class PreviewView2 extends ViewPart implements ISelectionListener, IResourceEventListener {
  /**
   * The ID for the Preview View.
   */
  public static final String ID = "gov.pnnl.cat.ui.rcp.preview";
  private ISelection lastSelection;
  private Composite composite;
  private Composite parent;
  private IPreviewPage currentPage;
  Random rand = new Random(12324324);

  
  @Override
  public void createPartControl(Composite parent) {
    // TODO Auto-generated method stub
    composite = new Composite(parent, SWT.NONE);
    this.parent = parent;

    getSite().getPage().addSelectionListener(this);
    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    if(currentPage != null) {
      currentPage.setFocus();
    }
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    if(currentPage != null) {
      currentPage.dispose();
    }
  }

  /**
   * Method drawView.
   * @param selection ISelection
   */
  protected void drawView(IResource resource) {

    // Wipe out old data
    composite.dispose();
    if(currentPage != null) {
      currentPage.dispose(); // clean up any listeners, etc.
    }
    composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.numColumns = 1;
    layout.makeColumnsEqualWidth = true;
    composite.setLayout(layout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    if(resource != null) {

      // See if this resource has a special adapter to create it's own preview
      IPreviewPage page = (IPreviewPage) RCPUtil.getAdapter(resource, IPreviewPage.class, false);

      // If not, use the default one
      if (page == null) {
        page = new FilePreviewPage();
      }

      page.createControl(composite, resource, this);
      currentPage = page;
      setPartName("Preview: " + resource.getName());
    
    } else {
      currentPage = null;
      setPartName("Preview:");
    }
    
    // refresh the view
    parent.layout(true, true);
  }

  /*
   * (non-Javadoc) Method declared on ISelectionListener. Notify the current page that the selection has changed.
   */
  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param sel ISelection
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
      if (resource == null) {
        resource = (IResource) RCPUtil.getAdapter(structuredSelection.getFirstElement(), IResource.class, false);
      }

      //there can be selection events that are NOT resources:
      if(resource == null) {
        return;
        
      } else {
        // make sure the resource is in the cache - this could happen for search results, because they aren't in the cache yet
        ResourcesPlugin.getResourceManager().getResource(resource.getPath(), true);

        lastSelection = selection;
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
      if(event.getPath().equals(resource.getPath()) && (event.hasChange(IResourceEvent.CONTENT_CHANGED) || event.hasChange(IResourceEvent.REMOVED)) ) {
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
