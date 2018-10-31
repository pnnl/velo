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
package gov.pnnl.cat.ui.rcp.viewers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.adapters.CatBaseWorkbenchContentProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public class ResourceTreeViewer extends TreeViewer {

  private Logger logger = CatLogger.getLogger(this.getClass());
  protected IResourceManager mgr;
  private boolean showFiles = true;
  
  /**
   * Constructor for ResourceTreeViewer.
   * @param parent Composite
   */
  public ResourceTreeViewer(Composite parent) {
    super(parent);
    init();
  }

  /**
   * Constructor for ResourceTreeViewer.
   * @param tree Tree
   */
  public ResourceTreeViewer(Tree tree) {
    super(tree);
    init();
  }

  /**
   * Constructor for ResourceTreeViewer.
   * @param showFiles boolean
   * @param parent Composite
   * @param style int
   */
  public ResourceTreeViewer(boolean showFiles, Composite parent, int style) {
    super(parent, style);
    this.showFiles = showFiles;
    init();
  }

  private void init(){
    this.setContentProvider(new CatBaseWorkbenchContentProvider(showFiles));

    IStyledLabelProvider labelProvider = new CatWorkbenchLabelProvider(this);
    ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
    setLabelProvider(new DecoratingStyledCellLabelProvider(labelProvider, decorator, null));

    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }
  }
  
  @Override
  public void refresh(Object element, boolean updateLabels) {
    if(element instanceof IResource) {
      element = findWrappedResource((IResource)element);
    }
    super.refresh(element, updateLabels);
  }

  @Override
  public void update(Object element, String[] properties) {
    if(element instanceof IResource) {
      element = findWrappedResource((IResource)element);
    }
    super.update(element, properties);
  }

  /**
   * Method expandToPath.
   * @param paths List<CmsPath>
   */
  public void expandToPath(List<CmsPath> paths) {    
    for(CmsPath path : paths) {
      // expand each node down to the parent
      expandToPath(path);      
    }
  }

  /**
   * Method expandToPath.  Note that this method expands up to the parent folder so that this
   * resource can be shown in the tree, but IT DOES NOT SELECT THE RESOURCE.  To select the resource
   * you need to call selectResource().
   * @param path CmsPath
   */
  public void expandToPath(CmsPath path) {    
    try {
      List<CmsPath> parents = new ArrayList<CmsPath>();

      // We have to expand all the nodes from the parent down to the resource
      CmsPath parentPath = path.getParent();

      // The tree viewers have special logic that if we include company home in the paths,
      // they won't expand correctly so we have to exclude the company home resource
      while (!parentPath.last().equals(CmsPath.companyHomeSegment)) {
        parents.add(0, parentPath);;
        parentPath = parentPath.getParent();
      }

      expandInternal(parents);
    } catch (Throwable e) {
      logger.error("Failed to expand path: " + path, e);
    }
  }
  
  public void selectResource(IResource resource) {
    List<IResource> selectedResources = new ArrayList<IResource>();
    
    // we need to make sure this isn't a top-level wrapped node
    selectedResources.add(resource);
    selectResources(selectedResources);
  }
  
  public void selectResources(List<IResource> selectedResources) {
    List<IResource> wrappedList = new ArrayList<IResource>();
    for(IResource resource : selectedResources) {
        wrappedList.add(findWrappedResource(resource));
    }
    setSelection(new StructuredSelection(wrappedList)); 
  }
  
  private IResource findWrappedResource(IResource resource) {
    IResource node = resource;
    Object objInput = getInput();
    RepositoryContainer repo = null;
    IResource[] repoRoots = null;
    
    if(objInput instanceof RepositoryContainer) {
      repo = (RepositoryContainer)objInput;
      repoRoots = (IResource[])repo.getChildren();
      
    } else if (objInput instanceof GenericContainer){
      
      repo = RCPUtil.findRepositoryContainer((GenericContainer)objInput);
      repoRoots = (IResource[])repo.getChildren();
      expandToLevel(repo, 1);
    }
    
    // See if we are expanding a root resource from the RepositoryContainer
    // If so, we need to get the resource from the viewer input, not from ResourceManager,
    // because the viewer input has a specially wrapped version of IResource that points to
    // RepositoryContainer as its parent - this is needed so that we can properly expand
    // resources that are nested inside 2 levels of virtual containers in the viewer
    if(repoRoots != null) {
      IResource rootResource = findRootResource(resource.getPath(), repoRoots);
      if(rootResource != null) {
        node = (IFolder)rootResource;
      }
    }
    return node;
  }

  private void expandInternal(List<CmsPath> parentPaths) {
    Object objInput = getInput();
    for (CmsPath path : parentPaths) {

      // skip the root path "/"
      // it is always expanded
      if (path.size() == 0) {
        continue;
      }

      // if it still exists, expand it
      try {
        final IResource resource = mgr.getResource(path);
        if (resource instanceof IFolder) {
          IResource node = findWrappedResource(resource);

          boolean bExpandAllowed = false;

          if (objInput instanceof IResource) {
            IFolder folderParent = (IFolder) objInput;
            if (folderParent.getPath().isPrefixOf(resource.getPath())) {
              bExpandAllowed = true;
            }
            
          } else {
            // Allow expansion for all other type of input objects
            bExpandAllowed = true;
          }

          if (bExpandAllowed == false) {
            logger.debug("EXPAND NOT allowed: "+resource.getPath());
          
          } else if (isExpandable(node)) {
            // Check that the node is expandable before even trying.
            expandToLevel(node, 1);
          }
        } 

      } catch (ResourceException e) {
        logger.error(e);
      }
    }
  }
  
  private IResource findRootResource(CmsPath path, IResource[] repoRoots) {
    try {
      for(IResource resource : repoRoots) {
        if(path.equals(resource.getPath())) {
          return resource;
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

}
