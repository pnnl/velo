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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * A filter for filtering out resources except those specifically requested by filename extension.
 * 
 * @author Zoe Guillen
 *
 * @version $Revision: 1.0 $
 */
public class FileExtensionFilter extends ViewerFilter {
  private static Logger logger = CatLogger.getLogger(FileExtensionFilter.class);
	private List<String> extensions;

	/**
	 * Constructor for FileExtensionFilter.
	 * @param types int[]
	 */
	public FileExtensionFilter(List<String> extensions) {
		this.extensions = extensions;
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

			//include folders to preserve tree, only filter out files if they don't have the wanted extension
			if(resource.isType(IResource.FOLDER)){
			  return true;
			}
			  
			// set to false and place the burden of proof upon us
			include = false;
			

			for (int i = 0; i < this.extensions.size() && !include; i++) {
				try {
          include = include || resource.getName().endsWith(this.extensions.get(i));
        } catch (ResourceException e) {
          logger.error(e);
        }
			}
		}

		return include;
	}

}
