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
package gov.pnnl.cat.ui.rcp.expressions;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.CatClipboard;
import gov.pnnl.cat.ui.rcp.handlers.PasteHandler;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.AbstractExplorerView;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 */
public class IsPasteAllowedPropertyTester  extends PropertyTester {
  protected static Logger logger = CatLogger.getLogger(IsPasteAllowedPropertyTester.class);
      
  /**
   * Method test.
   * @param receiver Object
   * @param property String
   * @param args Object[]
   * @param expectedValue Object
   * @return boolean
   * @see org.eclipse.core.expressions.IPropertyTester#test(Object, String, Object[], Object)
   */
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

    IWorkbenchPart part = (IWorkbenchPart) receiver;
    IWorkbenchSite site = part.getSite();
    String pasteType = (String) args[0];    
    boolean pasteAllowed = false;
    
    if (site.getSelectionProvider() != null) {
      ISelection selection = site.getSelectionProvider().getSelection();
      
      // Need to check to see if this is being called on a table.
      if (selection instanceof IStructuredSelection && (part instanceof AbstractExplorerView)) {
        IStructuredSelection struct = (IStructuredSelection) selection;
        pasteAllowed = getEnabledStatus(selection, part, pasteType);
      }

    }
    return pasteAllowed;
  }

  /**
   * Method getEnabledStatus.
   * @param selection ISelection
   * @param part IWorkbenchPart
   * @param pasteType String
   * @return boolean
   */
  public boolean getEnabledStatus(ISelection selection, IWorkbenchPart part, String pasteType) {
    
    // start off by default setting enabled for paste and paste shortcut to true:
    boolean setPasteEnabled = true;
    boolean setPasteShortcutEnabled = true;

    IFolder destination = PasteHandler.getDestinationFolder(selection, part);
   
    // Nothing is on the clipboard.
    CatClipboard clipboard = CatClipboard.getInstance();
    // this.isEnabledPasteShortcut = !clipboard.isEmpty(); // &&
    // !isPasteWithDelete;
    if (clipboard.isEmpty()) {// can't paste if nothing was copied
      setPasteEnabled = false;
      setPasteShortcutEnabled = false;
    } else if (destination == null) {
      setPasteEnabled = false;
      setPasteShortcutEnabled = false;
    } else {
      // if the selected had a folder or link, or if 'cut' was selected, don't enable pasteShortcut
      if (PasteHandler.clipboardResourceList.contains(IResource.FOLDER) 
          || PasteHandler.clipboardResourceList.contains(IResource.LINK) || PasteHandler.isPasteWithDelete) {
        setPasteShortcutEnabled = false;
      }
      
      //not allowing projects to be moved or copied to be inside another project:
      try{
        if(PasteHandler.clipboardResourceList.contains(IResource.PROJECT) && destination.isTypeInPath(IResource.PROJECT)){
          setPasteEnabled = false;
        }
      }catch(ResourceException re){
        logger.error(re);
      }
      try {
        // not allowing user to MOVE anything from a non-taxonomy to a taxonomy & vice versa
        // if source contains a taxonomy File (or folder or root) && destination is not a taxonomy folder or root
        // disallow move, allow copy only...and vice versa
        if (PasteHandler.isPasteWithDelete) {
          if((PasteHandler.clipboardResourceList.contains(IResource.TAXONOMY_FILE) || PasteHandler.clipboardResourceList.contains(IResource.TAXONOMY_FOLDER)) 
              && !destination.isType(IResource.TAXONOMY_FOLDER) && !destination.isType(IResource.TAXONOMY_ROOT)){
            setPasteEnabled = false;
          }else if(!PasteHandler.clipboardResourceList.isHomogeneous(new int[]{IResource.TAXONOMY_FILE, IResource.TAXONOMY_FOLDER}) && 
              (destination.isType(IResource.TAXONOMY_FOLDER) || destination.isType(IResource.TAXONOMY_ROOT))){
            setPasteEnabled = false;
          }
        } else
          if(PasteHandler.clipboardResourceList.contains(IResource.TAXONOMY_ROOT) 
              && destination.isTypeInPath(IResource.TAXONOMY_ROOT)){
            //cannot paste a copy of a taxonomy inside another taxonomy (or itself even):
            setPasteEnabled = false;
          }
      } catch (ResourceException e) {
        //don't do anything if we catch an exception, if paste should have been disable but was not, we'll still give user an error dialog
      }
    }

    PasteHandler.isEnabledPasteShortcut = setPasteShortcutEnabled;
    if(pasteType.equals("pasteShortcut")) {
      return setPasteShortcutEnabled;
    } else {
      return setPasteEnabled;
    }
    
  }

}
