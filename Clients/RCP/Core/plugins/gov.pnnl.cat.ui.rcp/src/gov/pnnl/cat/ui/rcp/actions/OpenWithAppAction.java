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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.program.Program;

/**
 */
public class OpenWithAppAction extends ViewerAction {
  private String fileExtension;
  private String label;
  private Logger logger = CatLogger.getLogger(this.getClass());

  /**
   * Constructor for OpenWithAppAction.
   * @param fileExtension String
   * @param label String
   */
  public OpenWithAppAction(String fileExtension, String label) {
    super(label);

    this.fileExtension = fileExtension;
    this.label = label;

    // get the image of the program
    Program prog = Program.findProgram(this.fileExtension);

    if (prog.getImageData() != null) {
      setImageDescriptor(ImageDescriptor.createFromImageData(prog.getImageData()));
    } else {
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_WINDOW, SharedImages.CAT_IMG_SIZE_16));
    }

    setToolTipText("Open File in " + this.label);
  }

  /**
   * Method run.
   * @see org.eclipse.jface.action.IAction#run()
   */
  public void run() {
    try {
      // find out what was selected to put in new view part
      StructuredSelection selectedFile = (StructuredSelection) getViewer().getSelection();
      IFile theFile = (IFile) RCPUtil.getResource(selectedFile.getFirstElement());

      logger.debug("File: " + theFile.getWebdavUrl().toExternalForm());
      // String strExt = ".doc";
      launchProgram(theFile, this.fileExtension);
    } catch (Exception ex) {
      logger.error(ex);
    }
  }

  /**
   * Method launchProgram.
   * @param theFile IFile
   * @param strExt String
   * @throws ResourceException
   */
  private void launchProgram(IFile theFile, String strExt) throws ResourceException {
    Program prog = Program.findProgram(strExt);
    if (prog != null) {
      logger.debug("Program Found: " + prog.getName());
      prog.execute(theFile.getWebdavUrl().toExternalForm());
    } else {
      logger.debug("program not found.");
    }
  }

  /**
   * Method getPolicy.
   * @return int
   */
  public int getPolicy() {
    return ENABLED_ON_SINGLE_FILE;
  }
}
