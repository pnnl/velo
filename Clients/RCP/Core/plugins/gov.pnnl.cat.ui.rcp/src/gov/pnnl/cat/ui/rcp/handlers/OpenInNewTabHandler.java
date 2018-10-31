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
package gov.pnnl.cat.ui.rcp.handlers;

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IVirtualFolder;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class OpenInNewTabHandler extends AbstractHandler {
  private Logger logger = CatLogger.getLogger(this.getClass());
  

  /* (non-Javadoc)
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      IWorkbenchPart part = HandlerUtil.getActivePart(event);
      if (part instanceof ICatExplorerView) {
        ICatExplorerView catView = (ICatExplorerView) part;
        String perspectiveId = catView.getSite().getPage().getPerspective().getId();
        logger.debug("catPerspective.getViewSite().getWorkbenchWindow().getActivePage().getPerspective(): " + perspectiveId);
        String secondId = "CAT" + catView.getNextUniqueId();

        //now see if we need to change this view's 'type' for when they open in new tab
        //a link to a physical resource from the virtual view (no longer want to be of a 'virtual' 
        //view type, so change to 'physcial')
        IStructuredSelection catFile = RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event));
        IResource resource = (IResource) catFile.getFirstElement();
        try {
          if (resource instanceof ILinkedResource) {
            resource = ((ILinkedResource) resource).getTarget();
          }
        } catch (ResourceException e) {
          // TODO: handle error
          logger.error("Could not look up target", e);
        }

        ICatExplorerView newView = null;
        
          //if virtual
            //open the virtual view for this perspective
          //else
            //open the physical view for this perspective
        
        if (resource instanceof IVirtualFolder) {
          if(perspectiveId.equalsIgnoreCase(CatPerspectiveIDs.ADMIN_DATA_BROWSER)){
            newView = (ICatExplorerView) catView.getPage().showView(CatViewIDs.DATA_INSPECTOR, secondId, IWorkbenchPage.VIEW_ACTIVATE);
          }else {
            newView = (ICatExplorerView) catView.getPage().showView(CatViewIDs.TAXONOMY_MANAGER_TAXONOMIES, secondId, IWorkbenchPage.VIEW_ACTIVATE);
          }
        }else{
          if(perspectiveId.equalsIgnoreCase(CatPerspectiveIDs.ADMIN_DATA_BROWSER)){
           newView = (ICatExplorerView) catView.getPage().showView(CatViewIDs.DATA_INSPECTOR, secondId, IWorkbenchPage.VIEW_ACTIVATE);
          }else {
            newView = (ICatExplorerView) catView.getPage().showView(CatViewIDs.TAXONOMY_MANAGER_DATA_SOURCES, secondId, IWorkbenchPage.VIEW_ACTIVATE);
          }
        }
        
        // find out what was selected to put in new view part
        newView.setRoot(resource);
      }
    } catch (PartInitException e) {
      logger.error(e);
    }
    return null;
  }

}
