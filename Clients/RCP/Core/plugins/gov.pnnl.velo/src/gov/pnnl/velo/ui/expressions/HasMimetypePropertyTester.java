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
/**
 * 
 */
package gov.pnnl.velo.ui.expressions;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.util.VeloConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class HasMimetypePropertyTester extends PropertyTester {
  private static final Logger logger = CatLogger.getLogger(HasMimetypePropertyTester.class);

  /**
   * 
   */
  public HasMimetypePropertyTester() {
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
   */
  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    IStructuredSelection selection;
    boolean retValue = ((Boolean)expectedValue).booleanValue();

    // Get the current selection
    if(!(receiver instanceof IStructuredSelection)) {
      return !retValue;
    }
    
    selection = (IStructuredSelection)receiver;
    
    if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
      return !retValue;
    }
    
    try {        
      IResource file = RCPUtil.getResource(selection.getFirstElement());

      String mimetypeProp = null;
      String mimetype = null;
      if(file != null) {
        // test that the resource has the mimetype defined in args:
        mimetype = (String) args[0];
        logger.debug("Testing "+mimetype+" aspect on file: "+file.getName());
        mimetypeProp = file.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
        logger.debug("mimetype from server: " + mimetypeProp);

        // hack until server gets updated
        String fileName = file.getName();
        if(fileName.endsWith(".bxl")) {
          mimetypeProp = "application/bxl";
        }
      }
      boolean ret = true;
      if(mimetypeProp != null && mimetypeProp.equals(mimetype)) {
        ret = retValue;
      } else {
        ret = !retValue;
      }
      logger.debug("Returning " + ret);
      return ret;
    } catch (Throwable e) {
      logger.error("Could not determine mimetype.", e);
    }

    return false;
  }

}
