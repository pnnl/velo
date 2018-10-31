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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry;
import gov.pnnl.cat.search.eclipse.search.ui.NewSearchUI;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;

/**
 * Represents an entry in the search result view
 * @version $Revision: 1.0 $
 */
public class SearchResultViewEntry extends PlatformObject implements ISearchResultViewEntry {

	private Object fGroupByKey= null;
	private IResource fResource= null;
	//private IMarker fMarker= null;
	private ArrayList fMarkers= null;
	private ArrayList fAttributes;
//	private int fSelectedMarkerIndex;
	//private long fModificationStamp= IResource.NULL_STAMP;
	private String fMarkerType;
	
	/**
	 * Constructor for SearchResultViewEntry.
	 * @param groupByKey Object
	 * @param resource IResource
	 */
	public SearchResultViewEntry(Object groupByKey, IResource resource) {
		fGroupByKey= groupByKey;
		fResource= resource;
		//if (fResource != null)
      //TODO: fix this when we have the equivalent method
			//fModificationStamp= fResource.getModificationStamp();
	}
	
	//---- Accessors ------------------------------------------------
	/**
	 * Method getGroupByKey.
	 * @return Object
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry#getGroupByKey()
	 */
	public Object getGroupByKey() {
		return fGroupByKey;
	}

	/**
	 * Method setGroupByKey.
	 * @param groupByKey Object
	 */
	void setGroupByKey(Object groupByKey) {
		fGroupByKey= groupByKey;
	}
	
	/**
	 * Method getResource.
	 * @return IResource
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry#getResource()
	 */
	public IResource getResource() {
		return fResource;
	}
	
	/**
	 * Method getMatchCount.
	 * @return int
	 * @see gov.pnnl.cat.search.eclipse.search.ui.ISearchResultViewEntry#getMatchCount()
	 */
	public int getMatchCount() {
    return 1;
    /*
		if (fMarkers != null)
			return fMarkers.size();
		if (fMarkers == null && fMarker != null)
			return 1;
		return 0;
    */
	}

	/**
	 * Method isPotentialMatch.
	 * @return boolean
	 */
	boolean isPotentialMatch() {
    /*
		if (fMarker != null)
			return fMarker.getAttribute(SearchUI.POTENTIAL_MATCH, false);
      */
		return false;
	}
	
	/**
	 * Method getAttributesPerMarker.
	 * @return List
	 */
	List getAttributesPerMarker() {
		if (fAttributes == null)
			return new ArrayList(0);
		return fAttributes;
	}
	
	/**
	 * Method getModificationStamp.
	 * @return long
	 */
	public long getModificationStamp() {
		//return fModificationStamp;
    return 0;
	}
	
	void clearMarkerList() {
		//fMarker= null;
		if (fMarkers != null)
			fMarkers.clear();
	}
		/*
	void add(IMarker marker) {
		if (marker != null && fMarkerType == null) {
			try {
				fMarkerType= marker.getType();
			} catch (CoreException ex) {
				// will default to org.eclipse.search.searchmarker
			}
		}

		if (fMarker == null) {
			fMarker= marker;
			if (fMarkers != null)
				fMarkers.add(marker);
			return;
		}
		if (fMarkers == null) {
			fMarkers= new ArrayList(10);
			addByStartpos(fMarkers, fMarker);
		}
		addByStartpos(fMarkers, marker);
	}
	*/
	/**
		 * Method setSelectedMarkerIndex.
		 * @param index int
		 */
		void setSelectedMarkerIndex(int index) {
//		fSelectedMarkerIndex= index;
	}
	
  /*
	public IMarker getSelectedMarker() {
		fSelectedMarkerIndex= Math.min(fSelectedMarkerIndex, getMatchCount() - 1);
		if (fMarkers == null && fMarker == null)
			return null;
		if (fMarkers != null && fSelectedMarkerIndex >= 0)
			return (IMarker)fMarkers.get(fSelectedMarkerIndex);
		return fMarker;
	}
	*/
	/**
   * Method getMarkers.
   * @return List
   */
  public List getMarkers() {
    /*
		if (fMarkers == null && fMarker == null)
			return new ArrayList(0);
		else if (fMarkers == null && fMarker != null) {
			List markers= new ArrayList(1);
			markers.add(fMarker);
			return markers;
		}
		return fMarkers;
    */
    return new ArrayList(0);
	}

	/**
	 * Method getMarkerType.
	 * @return String
	 */
	String getMarkerType() {
		if (fMarkerType == null)
		  return NewSearchUI.SEARCH_MARKER;
		else
			return fMarkerType;
	}
	/*
	boolean contains(IMarker marker) {
		if (fMarkers == null && fMarker == null)
			return false;
		if (fMarkers == null)
			return fMarker.equals(marker);
		else
			return fMarkers.contains(marker);
	}
	*/
  /*
	void remove(IMarker marker) {
		if (marker == null)
			return;
			
		if (fMarkers == null) {
			if (fMarker != null && fMarker.equals(marker))
				fMarker= null;
		}
		else {
			fMarkers.remove(marker);
			if (fMarkers.size() == 1) {
				fMarker= (IMarker)fMarkers.get(0);
				fMarkers= null;
			}
		}
	}
	*/
	void backupMarkers() {
    /*
		if (fResource != null)
			fModificationStamp= fResource.getModificationStamp();
		List markers= getMarkers();
		fAttributes= new ArrayList(markers.size());
		Iterator iter= markers.iterator();
		while (iter.hasNext()) {
			IMarker marker= (IMarker)iter.next();
			Map attributes= null;
			try {
				attributes= marker.getAttributes();
			} catch (CoreException ex) {
				// don't backup corrupt marker
				continue;
			}
			fAttributes.add(attributes);
		}
    */
	}
	/*
	private void addByStartpos(ArrayList markers, IMarker marker) {
		int startPos= marker.getAttribute(IMarker.CHAR_START, -1);
		int i= 0;
		int markerCount= markers.size();
		while (i < markerCount && startPos >= ((IMarker)markers.get(i)).getAttribute(IMarker.CHAR_START, -1))
			i++;
		markers.add(i, marker);
		if (i == 0)
			fMarker= marker;
	}
	*/
  
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	/**
	 * Method getAdapter.
	 * @param adapter Class
	 * @return Object
	 */
	public Object getAdapter(Class adapter) {
		return super.getAdapter(adapter);
	}
}
