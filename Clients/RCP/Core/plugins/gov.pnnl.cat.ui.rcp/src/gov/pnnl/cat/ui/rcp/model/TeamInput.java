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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.Team;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * A class that can be used as the input to a ContentViewer for displaying users.
 * This class can be adapted into an instance of ICatWorkbenchAdapter.
 *
 * @author Eric Marshall
 * @version $Revision: 1.0 $
 */

public class TeamInput implements IAdaptable, ICatWorkbenchAdapter {

  private ISecurityManager mgr;
  private List<ITeam> teams;
  private HashSet<String> filters = new HashSet<String>();
  private static final Logger logger = CatLogger.getLogger(UserInput.class);
  private boolean showEveryone;

  public TeamInput(boolean showEveryone, List<ITeam> teams) {
    this.showEveryone = showEveryone;
    this.teams = teams;
    mgr = ResourcesPlugin.getSecurityManager();
  }

  public TeamInput(boolean showEveryone) {
    this(showEveryone, null);
  }
  
  public TeamInput() {
    this(false, null);
  }
  
  /**
   * Method getFilteredTeams.
   * @return ITeam[]
   */
  private ITeam[] getFilteredTeams() {
    ITeam[] teams = getTeams();
    List<ITeam> filteredTeams = new ArrayList<ITeam>(teams.length);

    for (ITeam team : teams) {
      if (!filters.contains(team.getName())) {
        filteredTeams.add(team);
      }
    }

    return (ITeam[]) filteredTeams.toArray(new ITeam[filteredTeams.size()]);
  }

  /**
   * Method getTeams.
   * @return ITeam[]
   */
  private ITeam[] getTeams() {
    if (teams != null) {
      return (ITeam[]) teams.toArray(new ITeam[teams.size()]);
    }

    try {
      Collection<ITeam> teams = mgr.getTeams();
      
      // Add an special fake team to represent the EVERYONE group.
      // Let's call this team "All Users" since EVERYONE might be confusing.
      // We need this team for when setting permissions, since you might want to give
      // EVERYONE permissions on your data
      if(showEveryone) {
        ITeam everyone = new Team();
        everyone.setPath(new CmsPath("/" + ITeam.TEAM_NAME_ALL_USERS));
        teams.add(everyone);
      }
      return teams.toArray(new ITeam[teams.size()]);
    } catch (Exception e) {
      logger.error("Unable to retrieve teams", e);
      return new ITeam[0];
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
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getChildren(Object)
   */
  public Object[] getChildren(Object o) {
    return getFilteredTeams();
  }

  /**
   * Method getColumnImage.
   * @param element Object
   * @param columnIndex int
   * @return Image
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnImage(Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    logger.debug("getColumnImage(" + element + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getColumnText.
   * @param element Object
   * @param columnIndex int
   * @return String
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnText(Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    logger.debug("getColumnText(" + element + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getLabel(Object)
   */
  public String getLabel(Object o) {
    logger.debug("getLabel(" + o + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method getPath.
   * @param element Object
   * @return CmsPath
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getPath(Object)
   */
  public CmsPath getPath(Object element) {
    logger.debug("getPath(" + element + ")");
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    logger.debug("hasChildren(" + element + ")");
    return false;
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
    if (adapter == IWorkbenchAdapter.class || adapter == ICatWorkbenchAdapter.class) {
      return this;
    }

    return Platform.getAdapterManager().getAdapter(this, adapter);
  }

}
