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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;

/**
 */
public class HasAspectPropertyTester  extends PropertyTester {
  private static final Logger logger = CatLogger.getLogger(HasAspectPropertyTester.class);
  
  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
   */
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    IResource file = (IResource) receiver;

    //ignore property, instead test that the resource has the aspect defined in args:
    String aspect = (String) args[0];
    logger.debug("Testing "+aspect+" aspect on file: "+file.getName());

    try {
      boolean retValue = ((Boolean)expectedValue).booleanValue();

      // If this file isn't in the cache, test is false
      if(!ResourcesPlugin.getResourceManager().resourceCached(file.getPath())) {
        return !retValue;

      } else if (file.hasAspect(aspect)) {
        return retValue;

      } else {
        return !retValue;
      }
    } catch (Throwable e) {
      logger.error("Could not determine aspect.", e);
    } 

    return false;
  }
}
