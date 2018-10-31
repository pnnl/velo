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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 */
public class TeamContentProvider implements ITreeContentProvider {
  private final static String[] FILTERED_TEAMNAMES = {"admin", "guest"};
  private ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
  private Logger logger = CatLogger.getLogger(TeamContentProvider.class);
  
  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object)
   */
  public Object[] getElements(Object inputElement) {
    //System.out.println("TeamContentProvider:getElements()");
    if (inputElement == null) {
      return new ITeam[0];
    }
    ITeam[] teams = (ITeam[]) inputElement;
    ArrayList<ITeam> filteredTeams = new ArrayList<ITeam>(teams.length);

    for (ITeam team : teams) {
      boolean filteredOut = false;

      //don't show the children node
      if(team.getParent() != null)
      {
        filteredOut = true;
      }
      
      for (String filteredTeamname : FILTERED_TEAMNAMES) {
        if (team.getName().equals(filteredTeamname)) {
          filteredOut = true;
        }
      }

      if (!filteredOut) {
        filteredTeams.add(team);
      }
    }

    return (ITeam[]) filteredTeams.toArray(new ITeam[filteredTeams.size()]);
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
  }

  /**
   * Method getChildren.
   * @param parentElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(Object parentElement) {
    ITeam team = (ITeam) parentElement;
    List<CmsPath> teams = team.getSubgroups();
    ITeam [] myTeams = new ITeam[teams.size()];
    //TODO: exception handling
    try {
      int i = 0;
      for(CmsPath path : teams)
      {
        ITeam t =  mgr.getTeam(path);
        myTeams[i++] = t; 
      }
    }
    catch (Exception e)
    {
      logger.error(e);
    }
    
    return myTeams;
  }
  
  /**
   * Method getParent.
   * @param element Object
   * @return Object
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
   */
  public Object getParent(Object element) {
    ITeam team = (ITeam) element;
    CmsPath parent = team.getParent();
    //TODO: exception handling
    try
    {
      return mgr.getTeam(parent);
    }
    catch (Exception e)
    {
      logger.error(e);
      return null;
    }
  }
  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    ITeam team = (ITeam) element;
    return (team.getSubgroups().size() > 0);
  }
}
  
