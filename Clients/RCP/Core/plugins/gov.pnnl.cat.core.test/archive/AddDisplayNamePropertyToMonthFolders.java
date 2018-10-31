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

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.tests.CatTest;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;

import java.util.List;

import org.apache.log4j.Logger;

/**
 */
public class AddDisplayNamePropertyToMonthFolders extends CatTest {
  private final static String PATH = "/virtual/timeline/1990s";
  protected static Logger logger = CatLogger.getLogger(AddDisplayNamePropertyToMonthFolders.class);
  
  /**
   * Method testAddDisplayNamePropertyToMonthFolders.
   * @throws ResourceException
   */
  public void testAddDisplayNamePropertyToMonthFolders() throws ResourceException {
    CmsPath path = new CmsPath(PATH);
    IResource resource = this.mgr.getResource(path);

    if (!(resource instanceof IFolder)) {
      fail("PATH must be a folder");
    }

    addDisplayNameProperty((IFolder) resource);
  }

  /**
   * Method addDisplayNameProperty.
   * @param folder IFolder
   * @throws ResourceException
   */
  private void addDisplayNameProperty(IFolder folder) throws ResourceException {
    //System.out.print(folder.getPath());
    logger.debug(folder.getPath());
    List<IResource> children = folder.getChildren();
    //System.out.println(" - " + children.size() + " children");
    logger.debug(" - " + children.size() + " children");
    IResource child;

    for (int i = 0; i < children.size(); i++) {
      child = (IResource) children.get(i);

      if (!processYearChild(child) || !(child instanceof IFolder)) {
        //System.out.println("\tskipping child " + i + "/" + children.size() + " " + child);
        logger.debug("\tskipping child " + i + "/" + children.size() + " " + child);
      } else {
        //System.out.println("\t" + child.getPath());
        logger.debug("\t" + child.getPath());
        addMonthDisplayNameToChildrenOf((IFolder) child);
      }
    }
  }

  /**
   * Method addMonthDisplayNameToChildrenOf.
   * @param folder IFolder
   * @throws ResourceException
   */
  private void addMonthDisplayNameToChildrenOf(IFolder folder) throws ResourceException {
    List<IResource> children = folder.getChildren();
    IResource child;
    int month;
    String displayName = null;

    for (int i = 0; i < children.size(); i++) {
      child = (IResource) children.get(i);

      month = Integer.parseInt(child.getName());

      switch (month) {
        case 1:
          displayName = "January";
          break;
        case 2:
          displayName = "February";
          break;
        case 3:
          displayName = "March";
          break;
        case 4:
          displayName = "April";
          break;
        case 5:
          displayName = "May";
          break;
        case 6:
          displayName = "June";
          break;
        case 7:
          displayName = "July";
          break;
        case 8:
          displayName = "August";
          break;
        case 9:
          displayName = "September";
          break;
        case 10:
          displayName = "October";
          break;
        case 11:
          displayName = "November";
          break;
        case 12:
          displayName = "December";
          break;
      }
      if (displayName != null) {
        // this program won't do anything without the following line, but
        // we've removed the display name property.
//        child.setProperty(VeloConstants.PROP_DISPLAY_NAME, displayName);
      }
    }
  }

  /**
   * Method processYearChild.
   * @param child IResource
   * @return boolean
   */
  protected boolean processYearChild(IResource child) {
    return processChild(child, 1000, 9999);
  }

  /**
   * Method processMonthChild.
   * @param child IResource
   * @return boolean
   */
  protected boolean processMonthChild(IResource child) {
    return processChild(child, 1, 12);
  }

  /**
   * Method processChild.
   * @param child IResource
   * @param min int
   * @param max int
   * @return boolean
   */
  private boolean processChild(IResource child, int min, int max) {
    String name = child.getName();
    int value;

    value = Integer.parseInt(name);

    return value >= min && value <= max;
  }

}
