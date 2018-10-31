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
package gov.pnnl.cat.core.resources;

import gov.pnnl.cat.core.internal.resources.events.NotificationManagerJMS;
import gov.pnnl.cat.core.resources.search.ISearchManager;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.tif.service.VeloWorkspace;

import org.apache.log4j.Logger;

/**
 */
public class CmsServiceLocator {
  
  private static VeloWorkspace veloWorkspace;
  private static IResourceManager resourceManager;
  private static ISecurityManager securityManager;
  private static ISearchManager searchManager;
  private static IMimetypeManager mimetypeManager;
  private static NotificationManagerJMS notificationManager;
  private static IRemoteFileManager remoteFileManager;
  
  protected static Logger logger = CatLogger.getLogger(CmsServiceLocator.class);

  public CmsServiceLocator(){
  }
  
  public static IRemoteFileManager getRemoteFileManager() {
    return remoteFileManager;
  }

  public void setRemoteFileManager(IRemoteFileManager remoteFileManager) {
    CmsServiceLocator.remoteFileManager = remoteFileManager;
  }

  /**
   * @return
   */
  public static VeloWorkspace getVeloWorkspace() {
    return veloWorkspace;
  }
  
  /**
   * @param veloWorkspace the veloWorkspace to set
   */
  public void setVeloWorkspace(VeloWorkspace veloWorkspace) {
    CmsServiceLocator.veloWorkspace = veloWorkspace;
  }
  
  /**
   * Method getResourceManager.
   * @return IResourceManager
   */
  public static IResourceManager getResourceManager() {
    return resourceManager;
  }
  
  /**
   * @param resourceMgr the resourceMgr to set
   */
  public void setResourceManager(IResourceManager resourceMgr) {
    CmsServiceLocator.resourceManager = resourceMgr;
  }

  public static NotificationManagerJMS getNotificationManager() {
    return notificationManager;   
  }
  
   /**
   * @param notificationManager the notificationManager to set
   */
  public void setNotificationManager(NotificationManagerJMS notificationManager) {
    CmsServiceLocator.notificationManager = notificationManager;
  }

  /**
    * Method getSearchManager.
    * @return ISearchManager
    */
   public static ISearchManager getSearchManager() {
    return searchManager;
  }
  
  /**
   * @param searchManager the searchManager to set
   */
  public void setSearchManager(ISearchManager searchManager) {
    CmsServiceLocator.searchManager = searchManager;
  }

  /**
   * Method getSecurityManager.
   * @return ISecurityManager
   */
  public static ISecurityManager getSecurityManager() {  
    return securityManager;
  }
  
  /**
   * @param securityManager the securityManager to set
   */
  public void setSecurityManager(ISecurityManager securityManager) {
    CmsServiceLocator.securityManager = securityManager;
  }

  /**
   * Method getMimetypeManager.
   * @return IMimetypeManager
   */
  public static synchronized IMimetypeManager getMimetypeManager() {
    return mimetypeManager;
  }

  /**
   * @param mimetypeManager the mimetypeManager to set
   */
  public void setMimetypeManager(IMimetypeManager mimetypeManager) {
    CmsServiceLocator.mimetypeManager = mimetypeManager;
  }
  
}
