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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter for filtering out resources of all types except those specifically requested.
 * 
 * @author Eric Marshall
 *
 * @version $Revision: 1.0 $
 */
public class ResourceFilter extends ViewerFilter {
  private static Logger logger = CatLogger.getLogger(ResourceFilter.class);
	private int[] types;

	/**
	 * Constructor for ResourceFilter.
	 * @param types int[]
	 */
	public ResourceFilter(int[] types) {
		this.types = types;
	}

	/**
	 * Method select.
	 * @param viewer Viewer
	 * @param parentElement Object
	 * @param element Object
	 * @return boolean
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		IResource resource;
		boolean include = true;

		if (element instanceof IResource) {
			resource = (IResource) element;

			// set to false and place the burden of proof upon us
			include = false;

			for (int i = 0; i < this.types.length && !include; i++) {
				try {
          include = include || resource.isType(this.types[i]);
        } catch (ResourceException e) {
          logger.error(e);
        }
			}
		}

		return include;
	}

}
