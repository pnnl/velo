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
package gov.pnnl.cat.ui.rcp.views.teams;

import gov.pnnl.cat.core.resources.security.IUser;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 */
public class TeamMemberTableContentProvider implements ITreeContentProvider {
  private final static String[] FILTERED_USERNAMES = {"admin", "guest"};

  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object)
   */
  public Object[] getElements(Object inputElement) {
    if (inputElement == null) {
      return new IUser[0];
    }
    IUser[] users = (IUser[]) inputElement;

    return users;
  }
  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
  }

  /**
   * Method inputChanged.
   * @param viewer Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(
    Viewer viewer,
    Object oldInput,
    Object newInput) {
//    logger.debug("Input changed: old=" + oldInput + ", new=" + newInput);
  }
  /**
   * Method getChildren.
   * @param parentElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(Object parentElement) {
    // TODO Auto-generated method stub
    return null;
  }
  /**
   * Method getParent.
   * @param element Object
   * @return Object
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
   */
  public Object getParent(Object element) {
    // TODO Auto-generated method stub
    return null;
  }
  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    // TODO Auto-generated method stub
    return false;
  }

}
