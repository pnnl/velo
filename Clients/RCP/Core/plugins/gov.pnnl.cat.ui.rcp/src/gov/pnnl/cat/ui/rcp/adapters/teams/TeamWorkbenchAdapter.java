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
package gov.pnnl.cat.ui.rcp.adapters.teams;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 */
public class TeamWorkbenchAdapter implements ICatWorkbenchAdapter {

  private Logger logger = CatLogger.getLogger(this.getClass());
  private ISecurityManager mgr = ResourcesPlugin.getSecurityManager();

  /**
   * Method getChildren.
   * @param o Object
   * @return Object[]
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getChildren(Object)
   */
  public Object[] getChildren(Object o) {
    ITeam team = (ITeam) o;
    List<CmsPath> teams = team.getSubgroups();
    List<ITeam> myTeams = new ArrayList<ITeam>();

    for(CmsPath path : teams) {
      try {
        ITeam t =  mgr.getTeam(path);
        myTeams.add(t);
      } catch (Throwable e) {
        logger.warn("Throwing out group: " + path + " because it has no team profile.");
      }
    }
  

  return myTeams.toArray(new ITeam[myTeams.size()]);
  }

  /**
   * Method getColumnImage.
   * @param element Object
   * @param columnIndex int
   * @return Image
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnImage(Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    return SharedImages.getInstance().getImage(SharedImages.CAT_IMG_TEAMS, SharedImages.CAT_IMG_SIZE_16);
  }

  /**
   * Method getColumnText.
   * @param element Object
   * @param columnIndex int
   * @return String
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnText(Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    // TODO: add support for multiple columns
    return getLabel(element);
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getLabel(Object)
   */
  public String getLabel(Object o) {
    ITeam team = (ITeam) o;
    return team.getName();
  }

  /**
   * Method getPath.
   * @param element Object
   * @return CmsPath
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getPath(Object)
   */
  public CmsPath getPath(Object element) {
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
    return (getChildren(element).length > 0);
  }

  /**
   * Method getImageDescriptor.
   * @param object Object
   * @return ImageDescriptor
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
   */
  public ImageDescriptor getImageDescriptor(Object object) {
    return SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_TEAMS, SharedImages.CAT_IMG_SIZE_16);
  }

  /**
   * Method getParent.
   * @param o Object
   * @return Object
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(Object)
   */
  public Object getParent(Object o) {
    ITeam team = (ITeam) o;
    CmsPath parent = team.getParent();
    if(parent != null)
    {
      try {
        return mgr.getTeam(parent);
      } catch (Exception e) {
        logger.error("cannot getParent()", e);
        return null;
      }
    }
    else
    {
      return null;
    }
  }

}
