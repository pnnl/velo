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
package gov.pnnl.cat.alerting.alerts;

import java.net.URL;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Service for managing alerts that have been persisted to the repository via the RepositoryDeliveryChannel.  Alerts are  created once, and then they remain until they are explicitly deleted or expired.  They can not be modified. TODO: later we can add the ability for the alerts to be expired. For now, users have to delete them manually. TODO: later we can add the ability to send one time alerts, but for  now, all alerts are generated via a subscription. Alert service bean needs to be wrapped in a tx interceptor Alert service bean might need to be wrapped with a security interceptor (for granting access to methods and alert objects).  If NodeService and SearchService security are ok, then we might not need this.
 * @version $Revision: 1.0 $
 */
public interface AlertManagementService {
  
  /**
   * TODO: How long alerts can live in the system.  Not implemented
   * for baseline.
   * Should this be a global parameter, or something each user can set?
   * 
   * @param timeToLive - number of hours, days?
   */
  public void setAlertTimeToLive(int timeToLive);
  
  /**
   * Sets the max number of alerts that can be in one user's inbox at
   * a given time.
   * @param maxAlerts
   */
  public void setMaxAlerts(int maxAlerts);
  
  /**
   * Returns the maximum alerts that a user should contain in his alert box
  
   * @return int
   */
  public int getMaxAlerts();
  
  /**
   * Gets all persisted alerts that the current user has permissions to see.
  
   * @return List<RepositoryAlert>
   */
  public List<RepositoryAlert> getAlerts();
  
  /**
   * Gets all persisted alerts for the given user.
   * 
   * TODO: add support for group alerts
   * @param username String
   * @return List<RepositoryAlert>
   */
  public List<RepositoryAlert> getAlerts(String username);
  
  /**
   * Persist alert to the repository.
   * Putting this here so we can use it for one-time alerts.
  
   * @param alert Alert
   * @return the RepositoryAlert that was just created */
  public RepositoryAlert createAlert(Alert alert);  

  /**
   * Returns true if the given user has >= maxAlerts number of alerts in his 
   * alert box.
   * @param username
  
   * @return boolean
   */
  public boolean areAlertsMaxed(String username);
  
  /**
   * Removes the given alerts from the repository.
   * @param alerts
   */
  public void deleteAlerts(List<RepositoryAlert> alerts);   
  
  /**
   * Get the RSS URL for the given user's alerts
   * @param username
  
   * @return URL
   */
  public URL getRssUrl(String username);
  
  /**
   * Method newTemporaryAlert.
   * @return TemporaryAlert
   */
  public TemporaryAlert newTemporaryAlert();
  /**
   * Method newEvent.
   * @return Event
   */
  public Event newEvent();
  /**
   * Method newAlert.
   * @return Alert
   */
  public Alert newAlert();
  
  /**
   * Method getEvents.
   * @param alert Alert
   * @return List<Event>
   */
  public List<Event> getEvents(Alert alert);
  
  /**
   * Method setAlertRead.
   * @param repositoryAlert RepositoryAlert
   * @param alertRead boolean
   */
  public void setAlertRead(RepositoryAlert repositoryAlert, boolean alertRead);
  /**
   * Method setAlertRead.
   * @param repositoryAlert NodeRef
   * @param alertRead boolean
   */
  public void setAlertRead(NodeRef repositoryAlert, boolean alertRead);
}
