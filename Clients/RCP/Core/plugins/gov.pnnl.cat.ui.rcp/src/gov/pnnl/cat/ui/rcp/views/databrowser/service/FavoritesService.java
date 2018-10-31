package gov.pnnl.cat.ui.rcp.views.databrowser.service;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.FavoritesFolderWrapper;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.IFavoriteFolder;
import gov.pnnl.velo.util.VeloConstants;

/**
 * Manage favorite folders for the current user.
 * TODO: instead of saving locally to workspace, change this to 
 * 
 * @author D3K339
 *
 */
public class FavoritesService {
  
  private static FavoritesService instance = new FavoritesService();
  private List<IFolder> favorites = null;
  
  public static FavoritesService getInstance() {
    return instance;
  }
  
  public void addFavorite(IFolder favoriteFolder) {
    loadFavorites();
    if(!favorites.contains(favoriteFolder)) {
      favorites.add(favoriteFolder);
    }
   
    saveFavorites();
  }
 
  public void removeFavorite(IFolder favoriteFolder) {
    loadFavorites();

    if(favorites.contains(favoriteFolder)) {
      favorites.remove(favoriteFolder);    
    }
    saveFavorites();
  }
  
  public List<IFolder> getFavoriteResources() {
    loadFavorites();
    return favorites;
  }
  
  public IFavoriteFolder[] getFavorites() {
    loadFavorites();
    IFavoriteFolder[] wrappedFavorites = new IFavoriteFolder[favorites.size()];
    int i = 0;
    for(IFolder folder : favorites) {
      wrappedFavorites[i] = new FavoritesFolderWrapper(folder);
      i++;
    }
    return wrappedFavorites;
  }
  
  private void loadFavorites() {

    if(favorites == null) {
      favorites = new ArrayList<IFolder>();
      IResourceManager mgr = ResourcesPlugin.getResourceManager();
      IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("gov.pnnl.cat.ui.rcp");
      
      Preferences favoritesPref = prefs.node("favorites");
      String uuidStr = favoritesPref.get("uuidList", "default");
      if(uuidStr != null) {
        List<String> uuids = new ArrayList<String>();
        for(String uuid : uuidStr.split(",")) {
          uuids.add(uuid);
        }
        List<IResource> resources = mgr.getResourcesByUuid(uuids);
        for(IResource resource : resources) {
          favorites.add((IFolder)resource);
        }
      } 
    }
  }
  
  private void saveFavorites() {
    String uuidStr = "";
    int index = 0;
    for(IFolder folder : favorites) {
      if(index > 0) {
        uuidStr += ",";
      }
      uuidStr += folder.getPropertyAsString(VeloConstants.PROP_UUID);
    }
    IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode("gov.pnnl.cat.ui.rcp");
    Preferences favoritesPref = prefs.node("favorites");
    favoritesPref.put("uuidList", uuidStr);

  }
  
 }