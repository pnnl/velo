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

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 */
public class FindCommonFilesHelper implements ITaxonomyIntersectionHelper {
  private ISelection selection;

  /**
   * Constructor for FindCommonFilesHelper.
   * @param selection ISelection
   */
  public FindCommonFilesHelper(ISelection selection) {
    this.selection = selection;
  }

  /**
   * Method prepareSearch.
   * @param monitor IProgressMonitor
   * @param query ITaxonomyIntersectionQuery
   * @see gov.pnnl.cat.search.taxonomy.query.ITaxonomyIntersectionHelper#prepareSearch(IProgressMonitor, ITaxonomyIntersectionQuery)
   */
  public void prepareSearch(IProgressMonitor monitor, ITaxonomyIntersectionQuery query) {
    List elements;
    IResource resource;
    Map sets = new HashMap();
    Set curSet;

    if (this.selection instanceof IStructuredSelection) {
      elements = ((IStructuredSelection) this.selection).toList();
    } else {
      elements = new ArrayList(1);
      elements.add(this.selection);
    }

    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      resource = RCPUtil.getResource(iter.next());

      if (resource == null) {
//        EZLogger.logWarning("...", null);
        throw new NullPointerException();
      } else {
//        System.out.println(resource.getPath());
        curSet = new HashSet();
        curSet.add(resource);
        sets.put(resource.getPath(), curSet);
      }
    }

    query.setTaxonomies(sets);
  }

}
