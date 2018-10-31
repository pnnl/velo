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
package gov.pnnl.cat.search.basic.results;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultListener;
import gov.pnnl.cat.search.eclipse.search.ui.text.AbstractTextSearchResult;
import gov.pnnl.cat.search.eclipse.search.ui.text.Match;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 */
public abstract class AbstractResourceSearchResult extends AbstractTextSearchResult implements IResourceEventListener {

  private Map<CmsPath, Match> allResults = Collections.synchronizedMap(new HashMap<CmsPath, Match>());
  private Object listenerLock = new String("listener lock");

  /**
   * Method addListener.
   * @param l ISearchResultListener
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#addListener(ISearchResultListener)
   */
  public void addListener(ISearchResultListener l) {
    synchronized (listenerLock) {
      super.addListener(l);
      if (getListeners().size() == 1) {
        ResourcesPlugin.getResourceManager().addResourceEventListener(this);
      }
    }
  }
  /**
   * Method removeListener.
   * @param l ISearchResultListener
   * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResult#removeListener(ISearchResultListener)
   */
  public void removeListener(ISearchResultListener l) {
    synchronized (listenerLock) {
      super.removeListener(l);
      if (getListeners().size() == 0) {
        ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
      }
    }
  }

  /**
   * Method removeMatches.
   * @param matches Match[]
   */
  public void removeMatches(Match[] matches) {
    doRemoveMatch(matches);
    super.removeMatches(matches);
  }
  /**
   * Method removeMatch.
   * @param match Match
   */
  public void removeMatch(Match match) {
    doRemoveMatch(match);
    super.removeMatch(match);
  }
  public void removeAll() {
    allResults.clear();
    super.removeAll();
  }
  /**
   * Method addMatch.
   * @param match Match
   */
  public void addMatch(Match match) {
    doAddMatch(match);
    super.addMatch(match);
  }
  /**
   * Method addMatches.
   * @param matches Match[]
   */
  public void addMatches(Match[] matches) {
    doAddMatch(matches);
    super.addMatches(matches);
  }

  /**
   * Method doAddMatch.
   * @param matches Match[]
   */
  private void doAddMatch(Match... matches) {
    for (Match match : matches) {
      Object element = match.getElement();
      IResource resouce = RCPUtil.getResource(element);
      if (resouce != null) {
        allResults.put(resouce.getPath(), match);
      }
    }
  }
  /**
   * Method doRemoveMatch.
   * @param matches Match[]
   */
  private void doRemoveMatch(Match... matches) {
    for (Match match : matches) {
      Object element = match.getElement();
      IResource resouce = RCPUtil.getResource(element);
      if (resouce != null) {
        allResults.remove(resouce);
      }
    }
  }

  /**
   * Method onEvent.
   * @param notification IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  public void onEvent(IBatchNotification notification) {
    Iterator<IResourceEvent> allEvents = notification.getAllEvents();
    while (allEvents.hasNext()) {
      IResourceEvent resourceEvent = allEvents.next();
      if (resourceEvent.hasChange(IResourceEvent.REMOVED)) {
        Match match = allResults.get(resourceEvent.getPath());
        if (match != null) {
//          MatchEvent matchEvent = getSearchResultEvent(match, MatchEvent.REMOVED);
//          fireChange(matchEvent);
          removeMatch(match);
        }
      }
    }
  }
}
