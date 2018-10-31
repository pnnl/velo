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
package gov.pnnl.cat.ui.rcp.model;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A class that can be used as the input to a ContentViewer for displaying users.
 * This class can be adapted into an instance of ICatWorkbenchAdapter.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */
public class UserInput implements IWorkbenchAdapter, IAdaptable {

  private ISecurityManager mgr;
  private List<IUser> users;
  private HashSet<String> filters = new HashSet<String>();
  private static final Logger logger = CatLogger.getLogger(UserInput.class);
  private static final String ADMIN = "admin";
  private static final String GUEST = "guest";

  public UserInput() {
    mgr = ResourcesPlugin.getSecurityManager();
  }

  /**
   * Constructor for UserInput.
   * @param users List<IUser>
   */
  public UserInput(List<IUser> users) {
    this();
    this.users = users;
  }

  /**
   * Method getFilteredUsers.
   * @return IUser[]
   */
  private IUser[] getFilteredUsers() {
    IUser[] users = getUsers();
    List<IUser> filteredUsers = new ArrayList<IUser>(users.length);

    for (IUser user : users) {
      if (!filters.contains(user.getUsername())) {
        filteredUsers.add(user);
      }
    }

    return (IUser[]) filteredUsers.toArray(new IUser[filteredUsers.size()]);
  }

  /**
   * Method getUsers.
   * @return IUser[]
   */
  private IUser[] getUsers() {
    if (users != null) {
      return (IUser[]) users.toArray(new IUser[users.size()]);
    }

    try {
      return mgr.getUsers();
    } catch (Exception e) {
      logger.error("Unable to retrieve users", e);
      return new IUser[0];
    }
  }

  /**
   * Method setFilterSpecialUsers.
   * @param filter boolean
   */
  public void setFilterSpecialUsers(boolean filter) {
    if (filter) {
      filters.add(ADMIN);
    } else {
      filters.remove(ADMIN);
    }
  }

  /**
   * Method addFilter.
   * @param filteredUsername String
   */
  public void addFilter(String filteredUsername) {
    filters.add(filteredUsername);
  }

  /**
   * Method removeFilter.
   * @param filteredUsername String
   * @return boolean
   */
  public boolean removeFilter(String filteredUsername) {
    return filters.remove(filteredUsername);
  }

  /**
   * Method getChildren.
   * @param o Object
   * @return Object[]
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
   */
  public Object[] getChildren(Object o) {
    return getFilteredUsers();
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(Object)
   */
  public String getLabel(Object o) {
    logger.debug("getLabel(" + o + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getImageDescriptor.
   * @param object Object
   * @return ImageDescriptor
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
   */
  public ImageDescriptor getImageDescriptor(Object object) {
    logger.debug("getImageDescriptor(" + object + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getParent.
   * @param o Object
   * @return Object
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(Object)
   */
  public Object getParent(Object o) {
    logger.debug("getParent(" + o + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getAdapter.
   * @param adapter Class
   * @return Object
   */
  public Object getAdapter(Class adapter) {
    if (adapter == IWorkbenchAdapter.class) {
      return this;
    }

    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

}
