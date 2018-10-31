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
package gov.pnnl.cat.ui.rcp;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.OpenWithAction;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.ResourceList;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.program.Program;

/**
 */
public class OpenWithActionBrowser extends OpenWithAction {

  //the currect selection
  private IStructuredSelection selection;
  
  //the browser to launch
  private Program prog;
  
  private Logger logger = CatLogger.getLogger(this.getClass());

  public OpenWithActionBrowser() {
    super();
    
    //find the default browser
    this.prog = Program.findProgram("html");

    if (this.prog != null) {
      // get the image of the program to use as the image descriptor
      if (prog.getImageData() != null) {
        this.setImageDescriptor(ImageDescriptor.createFromImageData(prog.getImageData()));
      } else {
        this.setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WINDOW, SharedImages.CAT_IMG_SIZE_16));
      }

      this.setText("Browser");
      this.setToolTipText("Open File(s) in " + prog.getName());
    }
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.actions.OpenWithAction#getEnabledStatus(org.eclipse.jface.viewers.StructuredSelection)
   */
  /**
   * Method getEnabledStatus.
   * @param structSelection IStructuredSelection
   * @return boolean
   */
  public boolean getEnabledStatus(IStructuredSelection structSelection) {
    
    // A test for users that do not have a default browser for html.
    if (this.prog == null) {
      setEnabled(false);
      return false;
    }
    
    this.selection = structSelection;

    if (structSelection.isEmpty()) {
      setEnabled(false);
    } else {
      List selectedItems = structSelection.toList();
      ResourceList selectedResources = new ResourceList();

      try {
        for (Object item : selectedItems) {
          selectedResources.add(RCPUtil.getResource(item));
        }
  
        // action is enable for a selection that contain only files
        setEnabled(selectedResources.isHomogeneous(IResource.FILE));
      } catch (ResourceException e) {
        logger.error("Error processing resources", e);
      }
    }

    return this.isEnabled();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.actions.OpenWithAction#runJob(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus runJob(IProgressMonitor monitor) {
    // find out what was selected to pass to the utility method and get just all the files
    StructuredSelection selectedFile = (StructuredSelection) this.selection;
    Vector allFiles = this.getAllFiles(selectedFile);
    monitor.beginTask("Opening files", allFiles.size());
    //for each file, open a browser with the file as its contents
    for (Iterator iter = allFiles.iterator(); iter.hasNext();) {
      try {
        IFile theFile = (IFile) iter.next();
        logger.debug("File: " + theFile.getWebdavUrl().toExternalForm());
        monitor.subTask(theFile.getName());
        if (prog != null) {
          logger.debug("Program Found: " + prog.getName());
          prog.execute(theFile.getWebdavUrl().toExternalForm());
        } else {
          logger.debug("program not found.");
        }
      } catch (Exception ex) {
        logger.error(ex);
        return Status.CANCEL_STATUS;
      }
      monitor.worked(1);
    }

    return Status.OK_STATUS;
  }

}
