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
package gov.pnnl.cat.ui.rcp.actions.filters;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.ui.IActionFilter;

/**
 */
public class ResourceActionFilter implements IActionFilter {

  // TODO: add more contants here to allow greater flexibility for actions
  // TODO: use CatConstants, DmiConstants, or something along those lines
  public static final String TAXONOMY_ROOT = "taxonomyRoot";
  public static final String LINK = "link";
  public static final String RESOURCE = "resource";
  public static final String FILE = "file";

  public static final String NAME_TYPE = "type";
  public static final String NAME_ASPECT = "aspect";

  private static ResourceActionFilter singleton;
  private Logger logger = CatLogger.getLogger(getClass());

  private IResourceManager mgr;

  private ResourceActionFilter() {
    this.mgr = ResourcesPlugin.getResourceManager();
  }

  /**
   * Method getInstance.
   * @return ResourceActionFilter
   */
  public synchronized static ResourceActionFilter getInstance() {
    if (singleton == null) {
      singleton = new ResourceActionFilter();
    }
    return singleton;
  }

  /**
   * Method testAttribute.
   * @param target Object
   * @param name String
   * @param value String
   * @return boolean
   * @see org.eclipse.ui.IActionFilter#testAttribute(Object, String, String)
   */
  public boolean testAttribute(Object target, String name, String value) {
    boolean matches;
    IResource resource = RCPUtil.getResource(target);
    long start = System.currentTimeMillis();

    try {
      // make sure we have a valid resource
      if (resource == null || !this.mgr.resourceCached(resource.getPath())) {
        matches = false;

      } else {

        if (name.equals(NAME_TYPE)) {
          if (value.equals(TAXONOMY_ROOT)) {
            matches = resource.isType(IResource.TAXONOMY_ROOT);
          } else if (value.equals(LINK)) {
            matches = resource.isType(IResource.LINK);
          } else if (value.equals(RESOURCE)) {
            matches = true;
          } else if (value.equals(FILE)){
            matches = resource.isType(IResource.FILE);  
          } else {
            logger.warn("Unsupported type value: " + value);
            matches = false;
          }
        } else if (name.equals(NAME_ASPECT)) {
          matches = mgr.getAspects(resource.getPath(), false).contains(value);

        } else {
          // maybe it's a property
          String propValue = mgr.getProperty(resource.getPath(), name, false);

          if (propValue == null) {
            matches = value.equalsIgnoreCase("null");
          } else {
            matches = value.equals(propValue);
          }

        }
      }

    } catch (Throwable e) {
      logger.error("Could not test attribute for " + resource.getPath() + ": name=" + name + " value=" + value, e);
      matches = false;
    }

    long totalTime = System.currentTimeMillis() - start;
    logger.debug("testAttribute(" + resource + ", " + name + ", " + value + ") took " + totalTime + " ms to return " + matches);

    return matches;
  }

}
