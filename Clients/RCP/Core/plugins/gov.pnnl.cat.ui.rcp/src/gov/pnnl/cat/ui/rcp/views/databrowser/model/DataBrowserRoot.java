package gov.pnnl.cat.ui.rcp.views.databrowser.model;

import java.util.List;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.views.databrowser.service.FavoritesService;
import gov.pnnl.cat.ui.rcp.views.databrowser.service.SmartFolderService;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;
import gov.pnnl.velo.model.CmsPath;

public class DataBrowserRoot extends GenericContainer {
  public static final String FAVORITES = "Favorites";
  public static final String SMART_FOLDERS = "Smart Folders";
  public static final String CATEGORIES = "Categories";


  public DataBrowserRoot(IResource[] repositoryRoots) {
    super("Root");

    children = new Object[4];
    
    // 1) Load Favorites
    GenericContainer favorites = new GenericContainer(FAVORITES, "gov.pnnl.cat.ui", "icons/16x16/favorites.png", this, FavoritesService.getInstance().getFavorites());
    children[0] = (favorites);
    
    // 2) Smart Folders 
    GenericContainer smartFolders = new GenericContainer(SMART_FOLDERS, "gov.pnnl.cat.ui", "icons/16x16/smartFolder.png", this, SmartFolderService.getInstance().getSmartFoldersAsArray());
    children[1] = smartFolders;

    // 3) Repository
    RepositoryContainer repository = new RepositoryContainer(this, repositoryRoots);
    children[2] = repository;
    
    // 4) Categories
    GenericContainer categories = new GenericContainer(CATEGORIES, "gov.pnnl.cat.ui", "icons/16x16/branch.gif", this, getRootCategories());
    children[3] = categories;
  }
  
  /**
   * TODO: we need to put this in IResourceManager or add a CategoryService
   * @return
   */
  private IResource[] getRootCategories() {
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    CmsPath categoriesPath = new CmsPath("/{http://www.alfresco.org/model/content/1.0}categoryRoot/{http://www.alfresco.org/model/content/1.0}generalclassifiable");
    IResource categoryRoot = mgr.getResource(categoriesPath);
    if(categoryRoot != null) {
      List<IResource> children = mgr.getChildren(categoryRoot.getPath());
      return children.toArray(new IResource[children.size()]);
    }
    return new IResource[0];
  }

}
