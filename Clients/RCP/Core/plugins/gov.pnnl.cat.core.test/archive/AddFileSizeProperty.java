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
package gov.pnnl.cat.core.resources.tests.upgrade;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.tests.CatTest;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class AddFileSizeProperty extends CatTest {
  private final static String PATH = "/physical/reference_library/source";
  protected static Logger logger = CatLogger.getLogger(AddFileSizeProperty.class);
  
  /**
   * Method testAddFileSizeProperty.
   * @throws ResourceException
   */
  public void testAddFileSizeProperty() throws ResourceException {
    CmsPath path = new CmsPath(PATH);
    IResource resource = this.mgr.getResource(path);

    if (!(resource instanceof IFolder)) {
      fail("PATH must be a folder");
    }

    addFileSizeProperty((IFolder) resource);
  }

  /**
   * Method addFileSizeProperty.
   * @param folder IFolder
   * @throws ResourceException
   */
  private void addFileSizeProperty(IFolder folder) throws ResourceException {
    //System.out.print(folder.getPath());
    logger.debug(folder.getPath());
    List<IResource> children = folder.getChildren();
    //System.out.println(" - " + children.size() + " children");
    logger.debug(" - " + children.size() + " children");
    IResource child;
    long size;

    for (int i = 0; i < children.size(); i++) {
      child = (IResource) children.get(i);

      if (child instanceof IFolder && !(child instanceof ILinkedResource)) {
        addFileSizeProperty((IFolder) child);
      } else if (child instanceof IFile && !(child instanceof ILinkedResource)) {
        //System.out.println("\t" + child.getPath());
        logger.debug("\t" + child.getPath());
        if(child.getPropertyAsString(VeloConstants.PROP_SIZE) == null) {
          //System.out.println("size property does not exist - creating one");
          logger.debug("size property does not exist - creating one");
          size = ((IFile) child).getSize();
          child.setProperty(VeloConstants.PROP_SIZE, new Long(size).toString());
        }
      }
    }
  }
}
