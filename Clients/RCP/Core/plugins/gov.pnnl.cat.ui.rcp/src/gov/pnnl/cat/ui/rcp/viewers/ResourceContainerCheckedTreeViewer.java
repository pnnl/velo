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

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.decorators.TableDecoratingLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.CatBaseWorkbenchContentProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

/**
 */
public class ResourceContainerCheckedTreeViewer extends ContainerCheckedTreeViewer {

  private Logger logger = CatLogger.getLogger(this.getClass());
  protected IResourceManager mgr;
  private boolean showFiles = true;
  
  /**
   * Constructor for ResourceContainerCheckedTreeViewer.
   * @param parent Composite
   */
  public ResourceContainerCheckedTreeViewer(Composite parent) {
    super(parent);
    init();
  }

  /**
   * Constructor for ResourceContainerCheckedTreeViewer.
   * @param tree Tree
   */
  public ResourceContainerCheckedTreeViewer(Tree tree) {
    super(tree);
    init();
  }

  /**
   * Constructor for ResourceContainerCheckedTreeViewer.
   * @param showFiles boolean
   * @param parent Composite
   * @param style int
   */
  public ResourceContainerCheckedTreeViewer(boolean showFiles, Composite parent, int style) {
    super(parent, style);
    this.showFiles = showFiles;
    init();
  }

  private void init(){
    this.setContentProvider(new CatBaseWorkbenchContentProvider(showFiles));

    ILabelProvider labelProvider = new CatWorkbenchLabelProvider(this);
    ILabelDecorator decorator = PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator();
    setLabelProvider(new TableDecoratingLabelProvider(labelProvider, decorator));

    try {
      this.mgr = ResourcesPlugin.getResourceManager();
    } catch (Exception e) {
      logger.error(e);
    }
  }

  /**
   * Method expandToPath.
   * @param path CmsPath
   */
  public void expandToPath(CmsPath path) {
    List<CmsPath> paths = new ArrayList<CmsPath>();
    paths.add(path);
    this.expandToPath(paths, null, null);
  }
  
  /**
   * Method expandToPath.
   * @param paths List<CmsPath>
   */
  public void expandToPath(List<CmsPath> paths) {
    this.expandToPath(paths, null, null);
  }

  /**
   * Method expandToPath.
   * @param path CmsPath
   * @param tableExplorer TableExplorer
   * @param resource IResource
   */
  public void expandToPath(CmsPath path, TableExplorer tableExplorer, IResource resource) {
    List<CmsPath> paths = new ArrayList<CmsPath>();
    paths.add(path);
    expandToPath(paths, tableExplorer, resource);
  }

  /**
   * Method expandToPath.
   * @param paths List<CmsPath>
   * @param tableExplorer TableExplorer
   * @param resource IResource
   */
  public void expandToPath(List<CmsPath> paths, TableExplorer tableExplorer, IResource resource) {
    if (paths == null) {
      return;
    }
    logger.debug("ResourceTreeView::expandToPath");
    Stack<CmsPath> parents = new Stack<CmsPath>();
    for(CmsPath path : paths) {
      // make an vector of this resources parent(s):
      IResource nodeparent = null;
      CmsPath currentPath = path;
      try {
        nodeparent = mgr.getResource(currentPath);
        while (nodeparent != null) {
          // The tree viewers have special logic that if we include company home in the paths,
          // they won't expand correctly so we have to exclude the company home resource
          CmsPath parentpath = nodeparent.getPath();
          if(parentpath.size() == 1 && parentpath.last().equals(CmsPath.companyHomeSegment)) {
            nodeparent = null;

          } else {
            parents.push(parentpath);
            currentPath = parentpath;
            logger.debug("pushed: " + currentPath);
            nodeparent = nodeparent.getParent();
          }
        }
      } catch (ResourceException e) {
        //EZLogger.logError(e, "Failed to retrieve the resource at path: " + currentPath.toString());
        logger.error("Failed to retrieve the resource at path: " + currentPath.toString(), e);
      }
    }
    CmsPath[] foldersToExpand = (CmsPath[]) parents.toArray(new CmsPath[parents.size()]);

    // reverse the array
    for (int i = 0; i < foldersToExpand.length / 2; i++) {
      int index1 = i;
      int index2 = foldersToExpand.length - i - 1;

      CmsPath temp = foldersToExpand[index1];
      foldersToExpand[index1] = foldersToExpand[index2];
      foldersToExpand[index2] = temp;
    }

    expandMultipleFolders(foldersToExpand, paths, this, tableExplorer, resource);

  }

  /**
   * Method expandMultipleFolders.
   * @param expandingPaths CmsPath[]
   * @param selectedFolders List<CmsPath>
   * @param treeViewer TreeViewer
   * @param tableExplorer TableExplorer
   * @param resource1 IResource
   */
  private void expandMultipleFolders(CmsPath[] expandingPaths, final List<CmsPath> selectedFolders, final TreeViewer treeViewer, TableExplorer tableExplorer, IResource resource1) {

    for (int i = 0; i < expandingPaths.length; i++) {
      CmsPath path = expandingPaths[i];

      // skip the root path "/"
      // it is always expanded
      if (path.size() == 0) {
        continue;
      }

      // if it still exists, expand it
      try {
        final IResource resource = mgr.getResource(path);
        if (resource instanceof IFolder) {
          final IFolder node = (IFolder) resource;

          Object objInput = treeViewer.getInput();
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
          }

          // Check that the node is expandable before even trying.
          if (treeViewer.isExpandable(node) && bExpandAllowed ) { //&& rootPath.isPrefixOf(node.getTreePath())) {
            treeViewer.expandToLevel(node, 1);
          }
        } 

      } catch (ResourceException e) {
        logger.error(e);
      }
    }

    // now set selection after all folders are expanded
    logger.debug("trying to set selection");
    List<IResource> selectedResources = new ArrayList<IResource>();
    for(CmsPath resourcePath : selectedFolders) {
      try {
        IResource resource = mgr.getResource(resourcePath);
        if(resource != null) {
          selectedResources.add(resource);
        } else {
          logger.info("Could not select resource: " + resourcePath.toDisplayString() + " because it does not exist.");
        }
      } catch (Throwable e) {
        logger.info("Could not select resource: " + resourcePath.toDisplayString(), e);
      }
    }
    treeViewer.setSelection(new StructuredSelection(selectedResources));

    try {
      if(resource1 != null && treeViewer instanceof ResourceContainerCheckedTreeViewer){
        tableExplorer.getTableViewer().setSelection(new StructuredSelection(resource1));
      }
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }

  }


}
