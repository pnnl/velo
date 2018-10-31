package gov.pnnl.cat.ui.rcp.views;
/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.AuthenticationEvent;
import org.eclipse.swt.browser.AuthenticationListener;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.part.ViewPart;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

/**
 * Copied from Eclipse ResourceWebBrowser so we can make listening for selection
 * events automatic and we can auto-authenticate.
 */
public class ResourceWebBrowser extends ViewPart implements
    IBrowserViewerContainer, ISetSelectionTarget, IAcceptUrl {
  public static final String ID = ResourceWebBrowser.class.getName(); //$NON-NLS-1$

  protected BrowserViewer viewer;
  private String lastUrl;

  protected ISelectionListener listener;

  public void createPartControl(Composite parent) {
    // TODO: not sure if we want to use the nav bar or not, so I'm using no styles for now
    //int style = WebBrowserUtil.decodeStyle(getViewSite().getSecondaryId());
    viewer = new BrowserViewer(parent, SWT.NONE);
    viewer.setContainer(this);
    

    // Velo change:  make sure that we are listening for selection events!
    addSelectionListener();
    // end Velo change
    /*
     * PropertyChangeListener propertyChangeListener = new
     * PropertyChangeListener() { public void
     * propertyChange(PropertyChangeEvent event) { if
     * (BrowserViewer.PROPERTY_TITLE.equals(event.getPropertyName())) {
     * setPartName((String) event.getNewValue()); } } };
     * viewer.addPropertyChangeListener(propertyChangeListener);
     */
    initDragAndDrop();
    
    // Velo change:  listen for authentication requests
    addAuthenticationListener();
    
    // Velo change - trigger an authentication event
    String url = ResourcesPlugin.getResourceManager().getRepositoryUrlBase() + "/service/cat/startSession";
    setURL(url);
  }
  
  private void addAuthenticationListener() {
    
    // browser is not triggering an auth event when called via jquery ajax, so we have to 
    // authenticate first
    viewer.getBrowser().addAuthenticationListener(new AuthenticationListener() {
      
      @Override
      public void authenticate(AuthenticationEvent event) {

        try {
          // when this is the first request for the browser, the browser last navigation url is null, so it's sending null
          // as the event location - i think this is an eclipse bug; we are working around it
          if(event.location == null) {
            event.user = ResourcesPlugin.getSecurityManager().getUsername();
            event.password = ResourcesPlugin.getSecurityManager().getPassword();          

          } else {
            URL url = new URL(event.location);
            URL repoUrl = new URL(ResourcesPlugin.getResourceManager().getRepositoryUrlBase());
            if (url.getHost().equals(repoUrl.getHost())) {       
              // log in as our velo user
              event.user = ResourcesPlugin.getSecurityManager().getUsername();
              event.password = ResourcesPlugin.getSecurityManager().getPassword();          
            } 
          }
          
        } catch (MalformedURLException e) {
          /* should not happen, let default prompter run */
        }       
      
      }
    });
  }

  public void dispose() {

    // This gets called before dispose, so we need to hook in so we can wipe the 
    // content url in case there is an embedded iframe that will crash on mac
//    if (viewer!=null) {
//      // We need to clear the browser document to avoid a crash when the
//      // browser is disposed on mac
//      viewer.getBrowser().setText("<html></html>");
//    }  
    if (viewer!=null)
      viewer.setContainer(null);
    if (listener != null)
      removeSelectionListener();
  }

  @Override
  public void setURL(String url) {
    if (viewer != null) {
      viewer.setURL(url);    
    }
  }

  public void setFocus() {
    viewer.setFocus();
  }

  public boolean close() {
    try {
      getSite().getPage().hideView(this);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public IActionBars getActionBars() {
    return getViewSite().getActionBars();
  }

  public void openInExternalBrowser(String url) {
    try {
      URL theURL = new URL(url);
      IWorkbenchBrowserSupport support = PlatformUI.getWorkbench()
          .getBrowserSupport();
      support.getExternalBrowser().openURL(theURL);
    } catch (MalformedURLException e) {
      // TODO handle this
    } catch (PartInitException e) {
      // TODO handle this
    }
  }

  public void addSelectionListener() {
    if (listener != null)
      return;

    listener = new ISelectionListener() {
      public void selectionChanged(IWorkbenchPart part,
          ISelection selection) {
        onSelectionChange(selection);
      }
    };
    getSite().getWorkbenchWindow().getSelectionService()
        .addPostSelectionListener(listener);
  }

  private void onSelectionChange(ISelection selection) {
    // If the selection is empty or not a resource, just leave what was here last
    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return;
      
    } 
    IStructuredSelection structuredSelection = (IStructuredSelection)selection;
    IResource resource = RCPUtil.getResource(structuredSelection.getFirstElement());

    //there can be selection events that are NOT resources:
    if(resource == null) {
      return;        
    } else {
      Object obj = structuredSelection.getFirstElement();
      URL url = RCPUtil.getAdapter(obj, URL.class, true);
      if (url!=null) {
        String urlStr = url.toExternalForm();
        if(!urlStr.equals(lastUrl)) {
          setURL(urlStr);
          lastUrl = urlStr;
          
          // update my title to reflect the resource being displayed
          setPartName("Web Page: " + resource.getName());
        }
      }
    }

  }

  public void removeSelectionListener() {
    if (listener == null)
      return;
    getSite().getWorkbenchWindow().getSelectionService()
        .removePostSelectionListener(listener);
    listener = null;
  }

  /**
   * Return true if the filename has a "web" extension.
   *
   * @param name
   * @return
   */
  protected boolean isWebFile(String name) {
    return name.endsWith("html") || name.endsWith("htm") || name.endsWith("gif") || //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        name.endsWith("jpg"); //$NON-NLS-1$
  }

  /**
   * Adds drag and drop support to the view.
   */
  protected void initDragAndDrop() {
    Transfer[] transfers = new Transfer[] {
    // LocalSelectionTransfer.getInstance(),
    // ResourceTransfer.getInstance(),
    FileTransfer.getInstance() };

    DropTarget dropTarget = new DropTarget(viewer, DND.DROP_COPY
        | DND.DROP_DEFAULT);
    dropTarget.setTransfer(transfers);
    dropTarget.addDropListener(new WebBrowserViewDropAdapter(viewer));
  }

  public void selectReveal(ISelection selection) {
    onSelectionChange(selection);
  }

  public void setBrowserViewName(String name) {
    setPartName(name);
  }

  public void setBrowserViewTooltip(String tip) {
    setTitleToolTip(tip);
  }
  
  public class WebBrowserViewDropAdapter extends DropTargetAdapter {
    /**
     * The view to which this drop support has been added.
     */
    private BrowserViewer view;

    /**
     * The current operation.
     */
    private int currentOperation = DND.DROP_NONE;

    /**
     * The last valid operation.
     */
    private int lastValidOperation = DND.DROP_NONE;

    protected WebBrowserViewDropAdapter(BrowserViewer view) {
     this.view = view;
   }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * The mouse has moved over the drop target.  If the
     * target item has changed, notify the action and check
     * that it is still enabled.
     */
    private void doDropValidation(DropTargetEvent event) {
        //update last valid operation
        if (event.detail != DND.DROP_NONE)
            lastValidOperation = event.detail;

        //valid drop and set event detail accordingly
        if (validateDrop(event.detail, event.currentDataType))
            currentOperation = lastValidOperation;
        else
            currentOperation = DND.DROP_NONE;

        event.detail = currentOperation;
    }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * The drop operation has changed, see if the action
     * should still be enabled.
     */
    public void dragOperationChanged(DropTargetEvent event) {
        doDropValidation(event);
    }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * The mouse has moved over the drop target.  If the
     * target item has changed, notify the action and check
     * that it is still enabled.
     */
    public void dragOver(DropTargetEvent event) {
        //set the location feedback
      event.feedback = DND.FEEDBACK_SELECT;

        //see if anything has really changed before doing validation.
        doDropValidation(event);
    }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * The user has dropped something on the desktop viewer.
     */
    public void drop(DropTargetEvent event) {
        //perform the drop behaviour
        if (!performDrop(event.data))
            event.detail = DND.DROP_NONE;

        currentOperation = event.detail;
    }

    /* (non-Javadoc)
     * Method declared on DropTargetAdapter.
     * Last chance for the action to disable itself
     */
    public void dropAccept(DropTargetEvent event) {
        if (!validateDrop(event.detail, event.currentDataType))
            event.detail = DND.DROP_NONE;
    }

   public void dragEnter(DropTargetEvent event) {
     if (event.detail == DND.DROP_DEFAULT)
       event.detail = DND.DROP_COPY;

        doDropValidation(event);
   }

   /**
     * Performs any work associated with the drop.
     * <p>
     * Subclasses must implement this method to provide drop behavior.
     * </p>
     *
     * @param data the drop data
     * @return <code>true</code> if the drop was successful, and
     *   <code>false</code> otherwise
     */
   protected boolean performDrop(Object data) {
     if (data instanceof String[]) {
       String[] s = (String[]) data;
       if (s.length == 0)
         return true;
       File f = new File(s[0]);
       try {
         view.setURL(f.toURI().toURL().toExternalForm());
       } catch (Exception e) {
         // TODO
       }
     }

     return true;
   }

   /**
     * Validates dropping on the given object. This method is called whenever some
     * aspect of the drop operation changes.
     * <p>
     * Subclasses must implement this method to define which drops make sense.
     * </p>
     *
     * @param target the object that the mouse is currently hovering over, or
     *   <code>null</code> if the mouse is hovering over empty space
     * @param operation the current drag operation (copy, move, etc.)
     * @param transferType the current transfer type
     * @return <code>true</code> if the drop is valid, and <code>false</code>
     *   otherwise
     */
   protected boolean validateDrop(int operation, TransferData transferType) {
     if (FileTransfer.getInstance().isSupportedType(transferType))
       return true;
     /*if (ResourceTransfer.getInstance().isSupportedType(transferType))
       return true;
     if (LocalSelectionTransfer.getInstance().isSupportedType(transferType))
       return true;*/

     return false;
   }
 }
}
