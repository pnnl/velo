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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatClipboard;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.ResourceList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 */
public class CopyHandler extends AbstractHandler {
  protected static Logger logger = CatLogger.getLogger(CopyHandler.class);
  
  /* (non-Javadoc)
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute(ExecutionEvent event) throws ExecutionException {   
    //not the best solution - but ideally we can share logic with
    //the dnd code so that we won't even need this anymore eventually.
      if(deleteSource()) {
        PasteHandler.isPasteWithDelete = true;
      } else {
        PasteHandler.isPasteWithDelete = false;
      }
    
    copyToClipboard(RCPUtil.getCurrentStructuredSelection(HandlerUtil.getCurrentSelection(event), HandlerUtil.getActivePart(event)));
    
    // TODO Auto-generated method stub
    return null;
  }
  
  /**
   * Method deleteSource.
   * @return boolean
   */
  protected boolean deleteSource(){
    return false;
  }
  
  /**
   * Method copyToClipboard.
   * @param selection IStructuredSelection
   */
  @SuppressWarnings("rawtypes")
  private void copyToClipboard(IStructuredSelection selection) {
    logger.debug("selection in copy's run: " + selection);
    if (selection.isEmpty())
      return;

    List<IResource> resourcesToCopy = new ArrayList<IResource>(selection.size());
    Iterator iter = selection.iterator();

    IResource resource;
    
    ResourceList source = new ResourceList();
    while (iter.hasNext()) {
      resource = RCPUtil.getResource(iter.next());
      try {
        source.add(resource);
        resourcesToCopy.add(resource);
      } catch (ResourceException e) {
        logger.error("Could not add resource to the clipboard: " + resource.getPath());
      }
    }

    CatClipboard.getInstance().setContents(resourcesToCopy);
    PasteHandler.clipboardResourceList = source;
  }

}
