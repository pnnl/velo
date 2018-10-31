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
package gov.pnnl.cat.ui.rcp.dialogs;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog box for deleting links to resources.
 * @version $Revision: 1.0 $
 */
public class DeleteResourceWithLinksErrorDialog extends Dialog {
  
  private IResource resource;
  private IResourceManager mgr;
  private List linksToResource;
  
  // GUI Components
  private Button deleteAllLinksButton;
  private Button cancelDeleteOpertionButton;

  private Logger logger = CatLogger.getLogger(this.getClass());
  
  /**
   * @param parentShell 
   * @param res IResource that may have links.
   */
  public DeleteResourceWithLinksErrorDialog(Shell parentShell, IResource res) {
    super(parentShell);
    this.resource = res;
  }

  
  /**
   * Create contents of the dialog
   * 
   * @param parent
   * @return Control
   */
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setToolTipText("Resource Links");

    final Label errorLabel = new Label(container, SWT.NONE);
    errorLabel.setLayoutData(new GridData(SWT.DEFAULT, 34));
    errorLabel.setText(createText());

    final ListViewer linkViewer = new ListViewer(container, SWT.BORDER);
    this.linksToResource = linkViewer.getList();
    linksToResource.setEnabled(false);

    populateLinksList(this.resource);
    
    final GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
    gridData.heightHint = 84;
    gridData.widthHint = 182;
    this.linksToResource.setLayoutData(gridData);
        
    return container;
  }

  /**
   * @see org.eclipse.jface.window.Window#close()
   */
  public boolean close() {
    boolean returnValue = super.close();
    if (returnValue) {
      // Clean up here.
      
    }
    return returnValue;
  }
    

  /**
   * Method createText.
   * @return String
   */
  private String createText() {
    String text = "The resource ";
    if (resource instanceof IFolder) {
      text += "or one of it's contents ";
    }
    text += "has the following link(s).  \nThese links must be removed before this operation can continue.";
    
    return text;
  }
  
  
  
  /**
   * Method populateLinksList.
   * @param currentResource IResource
   */
  protected void populateLinksList(IResource currentResource) {
    if (currentResource != null) {

      // Walk the list of links for this resource.
      this.mgr = ResourcesPlugin.getResourceManager();

      Vector<IResource> resourceLinks = null;
      try {
        resourceLinks = new Vector<IResource>(this.mgr.getResourcesLinkedToPath(currentResource.getPath()));
      } catch (ResourceException e) {
        ToolErrorHandler.handleError("The server has failed to get the links for the resource: " + currentResource.getPath(), e, true); 
        this.close();
      }

      for (Iterator<IResource> iter = resourceLinks.iterator(); iter.hasNext();) {
        IResource res = (IResource) iter.next();

        // Note: Always add to the zeroth place so that the link is removed before the parent.
        this.linksToResource.add(res.getPath().toFullyQualifiedString(), 0);

        // Now see if the link has a link pointing to it. This was possible in older versions of CAT :-( But should no longer be possible.
        this.populateLinksList(res);
      }

      // Is this a folder? Then we must include the children's links also.
      logger.debug("Is it a folder? = " + (currentResource instanceof IFolder));
      if (currentResource instanceof IFolder) {
        logger.debug("Folder found: " + resource);
        IFolder folder = (IFolder) currentResource;
        java.util.List<IResource> children = null;
        try {
          children = folder.getChildren();
        } catch (ResourceException e) {
          ToolErrorHandler.handleError("The server has failed to retrieve the children of the folder: " + folder.getPath(), e, true); 
          this.close();
        }
        for (Iterator<IResource> iter = children.iterator(); iter.hasNext();) {
          IResource res = (IResource) iter.next();
          this.populateLinksList(res);
        }
      }

    } else {
      logger.debug("The resource is null");
    }
  }
  
  
  /**
   * Create contents of the button bar.
   * @param parent Composite this button bar is to be added to.
   */
  protected void createButtonsForButtonBar(Composite parent) {
    // ---
    // DELETE LINKS BUTTON
    // ---
    // increment the number of columns in the button bar
    ((GridLayout) parent.getLayout()).numColumns++;
    deleteAllLinksButton = new Button(parent, SWT.PUSH);
    deleteAllLinksButton.setText("Delete All Links");
    deleteAllLinksButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        try {
          // Walk all the links and delete.
          for (int i = 0; i < linksToResource.getItemCount(); i++) {
            CmsPath pathToLink = new CmsPath(linksToResource.getItem(i));
            mgr.deleteResource(pathToLink);
          }

          mgr.deleteResource(resource.getPath());
        } catch (ResourceException e) {
          ToolErrorHandler.handleError("The server has failed to delete the link: " + resource.getPath(), e, true); 
          
        } finally {
          // Close the dialog box.
          close();
        }
      }
    });
    this.setButtonLayoutData(deleteAllLinksButton);
    getShell().setDefaultButton(deleteAllLinksButton);

    // ---
    // CANCEL DELETION BUTTON
    // ---
    // increment the number of columns in the button bar
    ((GridLayout) parent.getLayout()).numColumns++;
    cancelDeleteOpertionButton = new Button(parent, SWT.PUSH);
    cancelDeleteOpertionButton.setText("Cancel Delete Operation");
    cancelDeleteOpertionButton.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        // Cancel button was selected so... close the dialog box.  Done.
        close();
      }
    });
    this.setButtonLayoutData(cancelDeleteOpertionButton);
    getShell().setDefaultButton(cancelDeleteOpertionButton);

  } // end createButtonsForButtonBar()
  
}
