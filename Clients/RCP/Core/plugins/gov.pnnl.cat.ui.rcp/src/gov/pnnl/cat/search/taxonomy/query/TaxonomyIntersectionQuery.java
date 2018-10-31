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
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;

import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.search.ICatQueryResult;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;
import gov.pnnl.cat.search.taxonomy.TaxonomySearchPlugin;
import gov.pnnl.cat.search.taxonomy.results.TaxonomyIntersectionSearchResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

/**
 * 
 * @author Eric Marshall
 *
 * @version $Revision: 1.0 $
 */
public class TaxonomyIntersectionQuery implements ITaxonomyIntersectionQuery {

  private AbstractTextSearchResult mResult;

  private Map<CmsPath, Set<IResource>> taxonomies;

	private ITaxonomyIntersectionHelper helper;

	// boolean to make sure the helper only runs once
	private boolean helperHasRun = false;

  private static Logger logger = CatLogger.getLogger(TaxonomyIntersectionQuery.class);
 
  /**
   * giving empty constructor for web services
   * @param helper 
   * 
   */
  public TaxonomyIntersectionQuery(ITaxonomyIntersectionHelper helper) {
  	this.helper = helper;
  }
  
  /**
   * Method getTaxonomies.
   * @return Map<CmsPath,Set<IResource>>
   * @see gov.pnnl.cat.search.taxonomy.query.ITaxonomyIntersectionQuery#getTaxonomies()
   */
  public Map<CmsPath, Set<IResource>> getTaxonomies() {
  	return this.taxonomies;
  }

  /**
   * Method setTaxonomies.
   * @param sets Map<CmsPath,Set<IResource>>
   * @see gov.pnnl.cat.search.taxonomy.query.ITaxonomyIntersectionQuery#setTaxonomies(Map<CmsPath,Set<IResource>>)
   */
  public void setTaxonomies(Map<CmsPath, Set<IResource>> sets) {
  	this.taxonomies = sets;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
   */
  public ISearchResult getSearchResult() {
    if (mResult == null) {
      mResult = new TaxonomyIntersectionSearchResult(this);
      // new SearchResultUpdater(fResult);
    }
    return mResult;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
  	if (monitor != null) {
  		monitor.subTask("Preparing Taxonomy Intersection");
  	}

    try {
      if (this.helper != null && !this.helperHasRun) {
        this.helper.prepareSearch(monitor, this);
        this.helperHasRun = true;
      }

      if (this.taxonomies == null || this.taxonomies.size() <= 0) {
        return new Status(IStatus.WARNING, TaxonomySearchPlugin.PLUGIN_ID, 1, "No taxonomies to search", null);
      }

      if (monitor != null) {
        monitor.beginTask("Intersecting Taxonomies", (this.taxonomies.size() * 3) + 1);
      }

      // TODO: Maybe update monitor as search progresses.  I don't think we can do this because
      // we don't get progress info from the JCR repository search
      // TODO: try to figure out a way to cancel the jcr search if user presses cancel
//    	performClientTaxonomyIntersection(monitor);
      performServerTaxonomyIntersection(monitor);
      String message = "Search completed successfully";
      return new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, message, null);
    } catch (Exception e) {
      CatSearchPlugin.log( new Status(IStatus.ERROR, CatSearchPlugin.getID(), 1, "Error during search.", e));
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#getLabel()
   */
  public String getLabel() {
    return "Taxonomy Intersection Search";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#canRerun()
   */
  public boolean canRerun() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
   */
  public boolean canRunInBackground() {
    return true;
  }

  /*
   * added only for debugging purposes (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return getClass().getName() + " " + this.taxonomies.size()
        + ", result: {" + mResult.toString() + "}.";
  }

  /**
   * Method performServerTaxonomyIntersection.
   * @param monitor IProgressMonitor
   * @throws ResourceException
   */
  private void performServerTaxonomyIntersection(IProgressMonitor monitor) throws ResourceException {
    ISearchManager searchMgr = ResourcesPlugin.getSearchManager();
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    final AbstractTextSearchResult textResult = (AbstractTextSearchResult) getSearchResult();
    textResult.removeAll();
    StringBuffer queryString = new StringBuffer();
    int i = 0;

    for (CmsPath path : taxonomies.keySet()) {
      for (IResource folder : taxonomies.get(path)) {
        String categoryName = folder.getName() + "_" + folder.getPropertyAsString(VeloConstants.PROP_UUID);

        logger.debug("Path: " + path);
        logger.debug("\t" + categoryName);

        if (i > 0) {
          queryString.append(" AND ");
        }
        queryString.append("PATH:\"/tax:classification//tax:");
        queryString.append(mgr.encodeISO9075(categoryName));
        queryString.append("//member\"");
        i++; 
      }
    }

//    String s = "PATH:\"/cm:generalclassifiable/cm:Signers_x0020_of_x0020_the_x0020_Declaration_x0020_of_x0020_Independence//member\" AND " +
//               "PATH:\"/cm:generalclassifiable/cm:Presidents/cm:b//member\"";
//    String s = "PATH:\"/cm:generalclassifiable//cm:Signers_x0020_of_x0020_the_x0020_Declaration_x0020_of_x0020_Independence_dbe5a879-8166-11db-83e4-97361133064e\" AND " +
//               "PATH:\"/cm:generalclassifiable//cm:Presidents_c626a42a-8166-11db-83e4-97361133064e\"";

    logger.debug(queryString.toString());
    ICatQueryResult queryResults = searchMgr.query(queryString.toString());
    List<IResource> searchResults = queryResults.getHandles();

//    logger.debug("SEARCH RESULTS:");

    for (IResource resource : searchResults) {
//      logger.debug(resource.getPath());
//      textResult.addMatch(new Match(resource, 0,0));
      Match m =new Match(resource, 0, 0);
      textResult.addMatch(m);
    }
  }
}
