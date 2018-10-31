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
package gov.pnnl.cat.ui.rcp.views.users;

import gov.pnnl.cat.core.resources.security.IUser;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 */
public class UserSorter extends ViewerSorter {

  /**
   * Method compare.
   * @param viewer Viewer
   * @param e1 Object
   * @param e2 Object
   * @return int
   */
  public int compare(Viewer viewer, Object e1, Object e2) {
    IUser user1 = (IUser) e1;
    IUser user2 = (IUser) e2;
    String user1Str = getStringValue(user1);
    String user2Str = getStringValue(user2);
    int difference = user1Str.compareTo(user2Str);
    return difference;
  }
  
  /**
   * Method getStringValue.
   * @param user IUser
   * @return String
   */
  private String getStringValue(IUser user)
  {
    String userStr = user.getLastName();
    if(userStr.length() > 0)
    {
      userStr += " ";
    }
    userStr += user.getFirstName();
    if(userStr.length() == 0) {
      userStr = user.getUsername();
    }
    return userStr.toLowerCase();
  }
}
