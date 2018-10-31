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
package gov.pnnl.cat.ui.rcp;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.EventListenerList;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.swt.widgets.Display;


/**
 */
public class SystemManager implements IResourceEventListener, IJobChangeListener {

  private EventListenerList evSystemListenerList = new EventListenerList();
  private static SystemManager manager = new SystemManager();
  private static boolean bCreated = false;
  private static int currentJobsRunning = 0;
  private static Vector updatesQue = new Vector();
  private Logger logger = CatLogger.getLogger(this.getClass());
  
  /**
   * Method getInstance.
   * @return SystemManager
   */
  public static SystemManager getInstance() {
    if (bCreated == false) {
      ResourcesPlugin.getResourceManager().addResourceEventListener(manager);
      bCreated = true;
    }
    return manager;
  }
  
  /**
   *  Add Listener<br>
   *  Add a listener to the the event list.
   *
   *  @param listener  the listener to add to the list
   */
  public void addDropListener(ISystemUpdateListener listener) {
    synchronized (evSystemListenerList) {
      evSystemListenerList.add(ISystemUpdateListener.class, listener);
    }
  }

  /**
   *  Remove Listener<br>
   *  Remove a listener from the the event list.
   *
   *  @param listener  the listener to remove from the list
   */
  public void removeDropListener(ISystemUpdateListener listener) {
    synchronized (evSystemListenerList) {
      evSystemListenerList.remove(ISystemUpdateListener.class, listener);
    }
  }    

  /**
   * Method fireFolderUpdated.
   * @param path CmsPath
   */
  protected void fireFolderUpdated(CmsPath path) {
    // TODO: Remove code that is no longer need since UI code has changed
//    if(currentJobsRunning == 0){
      notifyListeners(path);
//    }else{
//      if(!updatesQue.contains(path)){
//        updatesQue.addElement(path);
//      }
//    }
  }
  
  private void emptyQue(){
    for (Iterator iter = updatesQue.iterator(); iter.hasNext();) {
      CmsPath path = (CmsPath) iter.next();
      iter.remove();
      try {
        if (ResourcesPlugin.getResourceManager().resourceExists(path)) {
          notifyListeners(path);
        }
      } catch (ResourceException e1) {
        // TODO Auto-generated catch block
        logger.error(e1);
      }
      
      
    }
//      try {
//        if (ResourcesPlugin.getResourceTreeManager().resourceExists(((IFolder)updatesQue.elementAt(i)).getPath())) {
//        }
//      } catch (ResourceException e1) {
//        // TODO Auto-generated catch block
//        logger.error(e1);
//      }      
  }
  
  /**
   * Method notifyListeners.
   * @param path CmsPath
   */
  private void notifyListeners(final CmsPath path){
    final Object[] listeners = evSystemListenerList.getListenerList();
    // Each listener occupies two elements - the first is the listener class
    // and the second is the listener instance
    
    for (int i = 0; i < listeners.length; i += 2) {
      if (listeners[i] == ISystemUpdateListener.class) {
        final int index = i;
        Display.getDefault().asyncExec(new Runnable() {          
          @Override
          public void run() {
            ( (ISystemUpdateListener) listeners[index + 1]).refreshResource(path, false);
          }
        });
        
      }
    }  
  }

  /**
   * Method folderUpdated.
   * @param folder CmsPath
   */
  public void folderUpdated(CmsPath folder) {
    fireFolderUpdated(folder);
    
  } 
  
  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  public void onEvent(IBatchNotification events) {
    //non redundant events only include content_changed type of events
    //(IE: you'd get a changed event for the parent folder of the removed resouce)
    Iterator iter = events.getNonRedundantEvents();
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    // Keep a list of all parent folders so we can refresh them once if children have changed
    List<IResource> parentFoldersToUpdate = new ArrayList<IResource>();
    
    while (iter.hasNext()) {
      IResourceEvent event = (IResourceEvent) iter.next();
      if (event.hasChange(IResourceEvent.CONTENT_CHANGED) || event.hasChange(IResourceEvent.PROPERTY_CHANGED)) {
        
        CmsPath path = event.getPath();
//          System.out.println("event about : " + path);
//          IResource resource = mgr.getResource(path);
//          if (resource instanceof IFolder) {
//            updateFolder = (IFolder) resource;
//          }
        try {
          IResource resource = mgr.getResource(path);
          
          // We seem to be getting JMS messages for nodes other than those that go into the
          // cache (like system nodes), so if it comes back null from the cache, just
          // ignore this event
          if(resource == null) {
            continue;
          }
          
          if (resource instanceof IFolder && !parentFoldersToUpdate.contains(resource)) {
            parentFoldersToUpdate.add(resource);
           
          } else {
            IResource parent = resource.getParent();
            if(parent instanceof IFolder && !parentFoldersToUpdate.contains(parent)) {
              parentFoldersToUpdate.add(parent);
            }
          }
          
          
        } catch (ResourceException e) {
          // this happens pretty regularly when another user is deleting
          // folders that you can't see.
          logger.debug("Unable to get resource at " + path, e);
        }

      } 
    }
    
    // now send out one notification for the folders that 
    for (IResource folder : parentFoldersToUpdate) {
      fireFolderUpdated(folder.getPath()); 
    }
  }

  /**
   * Method aboutToRun.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(IJobChangeEvent)
   */
  public void aboutToRun(IJobChangeEvent event) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method awake.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(IJobChangeEvent)
   */
  public void awake(IJobChangeEvent event) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method done.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(IJobChangeEvent)
   */
  public void done(IJobChangeEvent event) {
    currentJobsRunning = currentJobsRunning - 1;
    logger.debug("Current Jobs Running: "+currentJobsRunning);
    if(currentJobsRunning == 0){
      emptyQue();
    }
    
  }

  /**
   * Method running.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(IJobChangeEvent)
   */
  public void running(IJobChangeEvent event) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method scheduled.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(IJobChangeEvent)
   */
  public void scheduled(IJobChangeEvent event) {
    currentJobsRunning++;
    logger.debug("Job Started: "+currentJobsRunning);
  }

  /**
   * Method sleeping.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(IJobChangeEvent)
   */
  public void sleeping(IJobChangeEvent event) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }   
}
