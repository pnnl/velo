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
package gov.pnnl.cat.search.taxonomy.pages;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.adapters.CatBaseWorkbenchContentProvider;

import org.apache.log4j.Logger;

/**
 */
public class TaxonomyContentProvider extends CatBaseWorkbenchContentProvider {
  private static Logger logger = CatLogger.getLogger(TaxonomyContentProvider.class);

	public TaxonomyContentProvider() {
		super(false);
	}

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
  	IResource resource;

  	if (element instanceof IResource) {
  		resource = (IResource) element;
  		
  		try {
        if (resource.isType(IResource.FOLDER) && !resource.isType(IResource.LINK)) {
        	return super.hasChildren(element);
        }
      } catch (ResourceException e) {
        logger.error("Could not get type for " + resource.getPath(), e);
      }
  	}

    return false;
  }

}
