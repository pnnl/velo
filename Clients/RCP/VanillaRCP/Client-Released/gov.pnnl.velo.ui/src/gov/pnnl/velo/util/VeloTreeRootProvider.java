/**
 * 
 */
package gov.pnnl.velo.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.TreeRootProvider;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.velo.model.CmsPath;

/**
 * Used by all views, dialogs, and wizards that allow repository browsing, so 
 * they show a consistent, unified organization.
 */
public class VeloTreeRootProvider implements TreeRootProvider {
  private static Logger logger = CatLogger.getLogger(VeloTreeRootProvider.class);

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.util.TreeRootProvider#getVeloTreeRoot()
   */
  @Override
  public RepositoryContainer getVeloTreeRoot() {

    List<IResource> roots = new ArrayList<IResource>();

    try {
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      ISecurityManager smgr = ResourcesPlugin.getSecurityManager();

      // add any home folders the user has permissions to see
      CmsPath userDocsPath = new CmsPath("/User Documents");
      IFolder userDocuments = (IFolder)mgr.getResource(userDocsPath);
      roots.add(userDocuments);

      // add any team folders this user is a member of
      CmsPath teamDocsPath = new CmsPath("/Team Documents");
      IFolder teamDocuments = (IFolder)mgr.getResource(teamDocsPath);
      roots.add(teamDocuments);
      
    } catch (Throwable e) {   
      e.printStackTrace();
      logger.error(e);
    }

    IResource[] rootsArray = new IResource[roots.size()];
    for(int i = 0; i < roots.size(); i++) {
      rootsArray[i] = roots.get(i);
    }
    RepositoryContainer root = new RepositoryContainer(null, rootsArray);
    return root;
  
  }

}
