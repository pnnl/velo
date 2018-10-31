/**
 * 
 */
package gov.pnnl.cat.ui.rcp.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 * @author D3K339
 *
 */
public class DefaultTreeRootProvider implements TreeRootProvider {
  private static Logger logger = CatLogger.getLogger(DefaultTreeRootProvider.class);

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.util.TreeRootProvider#getVeloTreeRoot()
   */
  @Override
  public RepositoryContainer getVeloTreeRoot() {
    List<IResource> roots = new ArrayList<IResource>();

    try {

      IResourceManager mgr = ResourcesPlugin.getResourceManager();

      // add User Documents
      IResource userDocs = mgr.getResource(new CmsPath(VeloConstants.PATH_USER_DOCUMENTS));
      roots.add(userDocs);

      // add Team Documents
      IResource teamDocs = mgr.getResource(new CmsPath(VeloConstants.PATH_TEAM_DOCUMENTS));
      roots.add(teamDocs);    

      // Add Velo folder
      IResource velo = mgr.getResource(new CmsPath(VeloConstants.PATH_VELO));
      roots.add(velo); 

    } catch (Throwable e) {
      e.printStackTrace();
      logger.error(e);
    }

    IResource[] rootsArray = new IResource[roots.size()];
    for (int i = 0; i < roots.size(); i++) {
      rootsArray[i] = roots.get(i);
    }
    RepositoryContainer root = new RepositoryContainer(null, rootsArray);
    return root;

  }

}
