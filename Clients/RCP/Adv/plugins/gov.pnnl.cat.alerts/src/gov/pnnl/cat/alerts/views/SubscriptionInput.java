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
package gov.pnnl.cat.alerts.views;

import gov.pnnl.cat.alerts.AlertService;
import gov.pnnl.cat.alerts.AlertsPlugin;
import gov.pnnl.cat.alerts.model.ISubscription;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.security.IUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.progress.IElementCollector;

/**
 */
public class SubscriptionInput extends DeferredWorkbenchAdapter {
  private boolean groupByUsers = false;

  /**
   * Method setGroupByUsers.
   * @param groupByUsers boolean
   */
  public void setGroupByUsers(boolean groupByUsers) {
    this.groupByUsers = groupByUsers;
  }

  /**
   * Method fetchDeferredChildren.
   * @param object Object
   * @param collector IElementCollector
   * @param monitor IProgressMonitor
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(Object, IElementCollector, IProgressMonitor)
   */
  @Override
  public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
    AlertService alertsService = AlertsPlugin.getDefault().getAlertsService();

    try {
      ISubscription[] subscriptions = alertsService.getSubscriptions();

      if (!groupByUsers) {
        collector.add(subscriptions, monitor);
      } else {
        Map<IUser, UserDir> users = new HashMap<IUser, UserDir>();
  
        for (ISubscription subscription : subscriptions) {
          IUser user = subscription.getUser();
          UserDir userDir;
  
          if (users.containsKey(user)) {
            userDir = users.get(user);
          } else {
            userDir = new UserDir(user);
            users.put(user, userDir);
          }
  
          userDir.addChild(subscription);
        }
  
        collector.add(users.values().toArray(), monitor);
      }

    } catch (ServerException e) {
      throw new RuntimeException(e);
    }

    collector.done();
  }

  /**
   * Method isContainer.
   * @return boolean
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
   */
  @Override
  public boolean isContainer() {
    return true;
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(Object)
   */
  @Override
  public String getLabel(Object o) {
    return "Subscriptions";
  }

  /**
   */
  class UserDir extends DeferredWorkbenchAdapter {
    private List<ISubscription> subscriptions = new ArrayList<ISubscription>();
    private IUser user;

    /**
     * Constructor for UserDir.
     * @param user IUser
     */
    public UserDir(IUser user) {
      this.user = user;
    }

    /**
     * Method getUser.
     * @return IUser
     */
    public IUser getUser() {
      return user;
    }

    /**
     * Method addChild.
     * @param subscription ISubscription
     */
    public void addChild(ISubscription subscription) {
      subscriptions.add(subscription);
    }

    /**
     * Method fetchDeferredChildren.
     * @param object Object
     * @param collector IElementCollector
     * @param monitor IProgressMonitor
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(Object, IElementCollector, IProgressMonitor)
     */
    @Override
    public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
      collector.add(subscriptions.toArray(), monitor);
      collector.done();
    }

    /**
     * Method isContainer.
     * @return boolean
     * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
     */
    @Override
    public boolean isContainer() {
      return true;
    }

    /**
     * Method getChildren.
     * @param o Object
     * @return Object[]
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
     */
    @Override
    public Object[] getChildren(Object o) {
      return subscriptions.toArray();
    }
    
  }
}
