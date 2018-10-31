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
package gov.pnnl.cat.policy.notifiable.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A collection of RepositoryEvent objects.  Includes methods
 * to automatically cast to and from a RepositoryEvent object
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class RepositoryEventList implements Iterable<RepositoryEvent>{
  private ArrayList<RepositoryEvent> repoEvents = new ArrayList<RepositoryEvent>();
  private HashSet<String> nodeUuids = new HashSet<String>();
  /**
   * For serializable purposes
   */
  private static final long serialVersionUID = 2956686550031107468L;

  /**
   * Method get.
   * @param i int
   * @return RepositoryEvent
   */
  public RepositoryEvent get(int i) {
		return (RepositoryEvent)repoEvents.get(i);
	}
	
	/**
	 * Method iterator.
	 * @return RepositoryEventIterator
	 * @see java.lang.Iterable#iterator()
	 */
	public RepositoryEventIterator iterator() {
		return new RepositoryEventIterator(repoEvents.iterator());
	}

  /**
   * Method add.
   * @param e RepositoryEvent
   * @return boolean
   */
  public boolean add(RepositoryEvent e) {
    if(!nodeUuids.contains(e.getNodeId())){
      nodeUuids.add(e.getNodeId());
      //addModifiedPropertyChangedEvent(e);
    }
    return repoEvents.add(e);
  }

  /**
   * Method addAll.
   * @param es Collection<RepositoryEvent>
   * @return boolean
   */
  public boolean addAll(Collection<RepositoryEvent> es) {
    for (RepositoryEvent repositoryEvent : es) {
      if(!nodeUuids.contains(repositoryEvent.getNodeId())){
        nodeUuids.add(repositoryEvent.getNodeId());
        //addModifiedPropertyChangedEvent(repositoryEvent);
      }
    }
    return repoEvents.addAll(es);
  }
  
  /**
   * Method addAll.
   * @param repoEventList RepositoryEventList
   * @return boolean
   */
  public boolean addAll(RepositoryEventList repoEventList) {
    for (RepositoryEvent repositoryEvent : repoEventList.getRepoistoryEventList()) {
      if(!nodeUuids.contains(repositoryEvent.getNodeId())){
        nodeUuids.add(repositoryEvent.getNodeId());
        //addModifiedPropertyChangedEvent(repositoryEvent);
      }
    }
    return repoEvents.addAll(repoEventList.getRepoistoryEventList());
  }
	
  /**
   * Method size.
   * @return int
   */
  public int size(){
    return repoEvents.size();
  }
  
  /**
   * Method getRepoistoryEventList.
   * @return List<RepositoryEvent>
   */
  public List<RepositoryEvent> getRepoistoryEventList(){
    return repoEvents;
  }
  
//  private void addModifiedPropertyChangedEvent(RepositoryEvent event) {
//    if(event.getEventType().equals(RepositoryEvent.TYPE_PROPERTY_CHANGED) && event.getPropertyName().equalsIgnoreCase("{http://www.alfresco.org/model/content/1.0}modified")){
//      RepositoryEvent propChangedEvent = new RepositoryEvent();
//      propChangedEvent.setNodeId(event.getNodeId());
//      propChangedEvent.setNodePath(event.getNodePath());
//      propChangedEvent.setEventPerpetrator(event.getEventPerpetrator());
//      propChangedEvent.setPropertyName("{http://www.alfresco.org/model/content/1.0}modified");
//      Date now = new Date();
//      propChangedEvent.setPropertyValue(now.toString());
//      propChangedEvent.setEventTimestamp(System.currentTimeMillis());
//      repoEvents.add(propChangedEvent);
//    }
//  }
  
}
