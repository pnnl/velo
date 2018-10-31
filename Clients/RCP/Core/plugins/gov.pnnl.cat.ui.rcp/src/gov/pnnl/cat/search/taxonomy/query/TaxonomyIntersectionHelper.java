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
package gov.pnnl.cat.search.taxonomy.query;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Display;

/**
 */
public class TaxonomyIntersectionHelper implements ITaxonomyIntersectionHelper {

	private CheckboxTreeViewer viewer;
  private static Logger logger = CatLogger.getLogger(TaxonomyIntersectionHelper.class);

	/**
	 * Constructor for TaxonomyIntersectionHelper.
	 * @param viewer CheckboxTreeViewer
	 */
	public TaxonomyIntersectionHelper(CheckboxTreeViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * Method prepareSearch.
	 * @param monitor IProgressMonitor
	 * @param query ITaxonomyIntersectionQuery
	 * @throws ResourceException
	 * @see gov.pnnl.cat.search.taxonomy.query.ITaxonomyIntersectionHelper#prepareSearch(IProgressMonitor, ITaxonomyIntersectionQuery)
	 */
	public void prepareSearch(IProgressMonitor monitor, ITaxonomyIntersectionQuery query) throws ResourceException {
    final Object[] checkedElementsFinal = new Object[1];
    final Object[] grayedElementsFinal  = new Object[1];

		Display.getDefault().syncExec(new Runnable() {
      public void run() {
        checkedElementsFinal[0] = viewer.getCheckedElements();
        grayedElementsFinal[0]  = viewer.getGrayedElements();
      }
    });

    Object[] checkedElements = (Object[]) checkedElementsFinal[0];
    Object[] grayedElements  = (Object[]) grayedElementsFinal[0];
		Map<CmsPath, Set<IResource>> sets = new HashMap<CmsPath, Set<IResource>>();

		IResource resource;
		CmsPath lastGrayed = null;
		CmsPath lastSolid = null;
		Set<IResource> curSet;

		if (checkedElements.length > 0) {
			// do the search

			for (int i = 0; i < checkedElements.length; i++) {
				resource = (IResource) checkedElements[i];

				// if the current item is grayed, then we treat it and its children
				// differently
				if (arrayContains(grayedElements, resource)) {
					lastGrayed = resource.getPath();
				} else {
					// current selection is solid (not grey)

					// if this is a child of the last solid check, then we can ignore
					// it.
					if (lastSolid != null && lastSolid.isPrefixOf(resource.getPath())) {
						// we can ignore children of the last solid
					} else {
						// this is a solid selection and it is not a child of the last
						// solid that we saw

						if (resource.isType(IResource.FOLDER)) {
							lastSolid = resource.getPath();
							// logger.debug("Important: " + lastSolid);
							curSet = new HashSet<IResource>();
							curSet.add(resource);
							sets.put(lastSolid, curSet);
						} else {
							// must be a link (leaf)
							// that is not a child of our last solid
							// logger.debug("must be a link: " + resource.getPath());

							if (lastGrayed != null && lastGrayed.isPrefixOf(resource.getPath())) {
								// logger.debug(" grayed parent: " + lastGrayed);
								if (sets.containsKey(lastGrayed)) {
									curSet = sets.get(lastGrayed);
								} else {
									curSet = new HashSet<IResource>();
									sets.put(lastGrayed, curSet);
								}

								curSet.add(resource);
							} else {
								logger.warn("Don't know how to deal with resource: " + resource.getPath());
							}
						}
					}
				}
			}

//			// PRINT STUFF OUT FOR DEBUGGING PURPOSES
//			int count = 0;
//			for (Iterator iter = sets.keySet().iterator(); iter.hasNext();) {
//				CmsPath path = (CmsPath) iter.next();
//				logger.debug("SET " + count + ":\n\t" + path);
//
//				for (Iterator iterator = ((Set) sets.get(path)).iterator(); iterator.hasNext();) {
//					IResource curResource = (IResource) iterator.next();
//					logger.debug("\t\t" + curResource.getPath());
//				}
//
//				count++;
//			}
		}

		query.setTaxonomies(sets);
	}

	// it makes me want to cry to have to write this method
	// I couldn't find anything in the Java API that does this for me.
	/**
	 * returns true if the specified array contains the itemSought.
	 * This method checks identity, not equality.
	 * @param array Object[]
	 * @param itemSought Object
	 * @return boolean
	 */
	private boolean arrayContains(Object[] array, Object itemSought) {
		boolean found = false;

		for (int i = 0; i < array.length && !found; i++) {
			if (array[i] == itemSought) {
				found = true;
			}
		}

		return found;
	}
}
