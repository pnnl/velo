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
package gov.pnnl.cat.ui.rcp.views.repositoryexplorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.TreeExplorer;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public abstract class AbstractExplorerView extends ViewPart implements ICatExplorerView {

  // Viewpart Variables
  protected static int uniqueId = -1;  //should be set during the first view's restoreState 
  private String thisViewId = CatViewIDs.DATA_INSPECTOR;
  public static final String VIEW_CONTEXT_MENU_ID = CatViewIDs.DATA_INSPECTOR + ".contextMenu";
  private Object root = null;

  protected TreeExplorer treeExplorer;

  @Override
  public void setRoot(Object objNewRoot) {
    this.root = objNewRoot;
    if (treeExplorer != null) {
      treeExplorer.setRoot(objNewRoot, true);
    }
  }

  @Override
  public Object getRoot() {
    return this.root;
  }

  @Override
  public void createPartControl(Composite parent) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setFocus() {
    // TODO Auto-generated method stub
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#setViewTitle(java.lang.String)
   */
  public void setViewTitle(String strTile) {
    setPartName(strTile);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#setViewToolTip(java.lang.String)
   */
  public void setViewToolTip(String strToolTip) {
    setTitleToolTip(strToolTip);

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#getViewPart()
   */
  public IViewPart getViewPart() {
    return this;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#getViewId()
   */
  public String getViewId() {
    return thisViewId ;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#setViewId(java.lang.String)
   */
  public void setViewId(String newViewId) {
    this.thisViewId = newViewId;
  }


  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatView#getNextUniqueId()
   */
  public int getNextUniqueId() {
    return uniqueId++;
  }

  /**
   * Method getPage.
   * @return IWorkbenchPage
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getPage()
   */
  public IWorkbenchPage getPage() {
    return this.getViewPart().getViewSite().getPage();
  }


  /**
   * Method getActionBars.
   * @return IActionBars
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#getActionBars()
   */
  public IActionBars getActionBars() {
    return this.getViewSite().getActionBars();
  }

  /**
   * Method expandToPath.
   * @param resource IResource
   */
  public void expandToPath(IResource resource) {
    CmsPath path = resource.getParent().getPath();
    this.treeExplorer.getTreeViewer().expandToPath(path);
  }

  /**
   * Method expandToPath.
   * @param path CmsPath
   */
  public void expandToPath(CmsPath path) {
    this.treeExplorer.getTreeViewer().expandToPath(path);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.repositoryexplorer.ICatExplorerView#isManagedPath(gov.pnnl.velo.model.CmsPath)
   */
  @Override
  public boolean isManagedPath(CmsPath path) {
    // TODO: support filters
    Object root = getRoot();
    List<IResource> roots = new ArrayList<IResource>();

    if (root instanceof IResource) {
      roots.add((IResource)root);
    
    } else if (root instanceof GenericContainer) { // could be any object, but we need to find a repository container
      RepositoryContainer repoContainer = RCPUtil.findRepositoryContainer((GenericContainer)root);
      if(repoContainer != null) {
        IResource[] children = (IResource[])repoContainer.getChildren();
        if(children != null) {
          for(IResource resource : children) {
            roots.add(resource);
          }          
        }
      }
    } 
    
    String pathString = path.toString();
    boolean managedPath = false;
    for(IResource resource : roots) {
      if(pathString.startsWith(resource.getPath().toString())) {
        managedPath = true;
        break;
      }
    }
    return managedPath;
  }
  
  /**
   * Return the eclispe table viewer, if it exists
  
   * @return TableViewer
   */
  public abstract TableViewer getTableViewer();

  /**
   * Return the eclipse tree viewer, if it exists
  
   * @return TreeViewer
   */
  public abstract TreeViewer getTreeViewer();

  /**
   * Return true if the resource table is currently the active widget in the view.
   * We need this for the case where we have views that have more than just a resource
   * table on it.  Table actions should only be enabled if the active widget is the
   * table.
  
   * @return boolean
   */
  public abstract boolean isTableActive();


  /* (non-Javadoc)
   * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
   */
  @Override
  public void init(IViewSite site, IMemento memento) throws PartInitException {
    super.init(site, memento);

    // init the uniqueId, only done on first one of these views to load
    if (memento != null) {
      if (uniqueId == -1) {
        if (memento.getChild("uniqueId") != null) {
          String uniqueIdString = memento.getChild("uniqueId").getID();
          uniqueId = Integer.parseInt(uniqueIdString);
        }
      }
    }
    
    if(treeExplorer != null) {
      treeExplorer.init(site, memento);
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
   */
  @Override
  public void saveState(IMemento memento) {
    if (treeExplorer != null) {
      treeExplorer.saveState(memento);
      // store the static class variable count
      memento.createChild("uniqueId", String.valueOf(uniqueId));
    }
  }

}
