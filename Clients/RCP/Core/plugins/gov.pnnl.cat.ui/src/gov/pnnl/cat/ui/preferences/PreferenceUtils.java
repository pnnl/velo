package gov.pnnl.cat.ui.preferences;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.events.ISystemResourceChangeEvent;
import org.eclipse.rse.core.events.ISystemResourceChangeEvents;
import org.eclipse.rse.core.events.ISystemResourceChangeListener;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class PreferenceUtils {
  private static Logger logger = CatLogger.getLogger(PreferenceUtils.class);
  
  static {
    // register for filesystem changes so we can wipe cached paths
    RSECorePlugin.getTheSystemRegistry().addSystemResourceChangeListener(new ISystemResourceChangeListener() {
      
      @Override
      public void systemResourceChanged(ISystemResourceChangeEvent event) {
        if(event.getType() == ISystemResourceChangeEvents.EVENT_DELETE && event.getSource() instanceof IHost) {
          setLastBrowsedFile(((IHost)event.getSource()).getName(), "");
        }
        
      }
    });
  }

  public static void setLastBrowsedFile(String host, String path) {
    
    String preferenceName = PreferenceConstants.LAST_BROWSED_PREFIX + host;
    if(host.equals("Local")) {
      preferenceName = PreferenceConstants.LAST_BROWSED_LOCAL_FOLDER;
    }
    
    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    preferences.setValue(preferenceName, path); 
    try {
      preferences.save();
    } catch (Throwable e) {
      logger.error(e);
    }

  }
  
  /**
   * Method setLastBrowsedLocalFile.
   * @param localFile File
   */
  public static void setLastBrowsedLocalFile(File localFile) {
    String lastBrowsedFolderPath;
    if(localFile.isDirectory()) {
      lastBrowsedFolderPath = localFile.getAbsolutePath();

    } else {
      lastBrowsedFolderPath = localFile.getParentFile().getAbsolutePath();
    }

    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    preferences.setValue(PreferenceConstants.LAST_BROWSED_LOCAL_FOLDER, lastBrowsedFolderPath); 
    try {
      preferences.save();
    } catch (Throwable e) {
      logger.error(e);
    }
  }

  public static String getLastBrowsedPath(String host) {
    String preferenceName = PreferenceConstants.LAST_BROWSED_PREFIX + host;
    if(host.equals("Local")) {
      preferenceName = PreferenceConstants.LAST_BROWSED_LOCAL_FOLDER;
    }
    
    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    String lastBrowsedPath = preferences.getString(preferenceName);
    return lastBrowsedPath;
  }
  
  /**
   * Method getLastBrowsedLocalDirectoryPath.
   * @return String
   */
  public static String getLastBrowsedLocalDirectoryPath() {
    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    String lastBrowsedFolderPath = preferences.getString(PreferenceConstants.LAST_BROWSED_LOCAL_FOLDER);
    return lastBrowsedFolderPath;
  }

  /**
   * Method getLastBrowsedCatDirectoryPath.
   * @return String
   */
  public static String getLastBrowsedCatDirectoryPath() {
    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    String lastBrowsedCatPath = preferences.getString(PreferenceConstants.LAST_BROWSED_CAT_FOLDER);
    return lastBrowsedCatPath;    
  }

  /**
   * Method setLastBrowsedCatFile.
   * @param catFile IResource
   */
  public static void setLastBrowsedCatFile(IResource catFile) {
    String lastBrowsedCatPath;
    if(catFile instanceof IFolder) {
      lastBrowsedCatPath = catFile.getPath().toFullyQualifiedString();

    } else {
      lastBrowsedCatPath = catFile.getParent().getPath().toFullyQualifiedString();
    }

    ScopedPreferenceStore preferences =  new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    preferences.setValue(PreferenceConstants.LAST_BROWSED_CAT_FOLDER, lastBrowsedCatPath);    
    try {
      preferences.save();
    } catch (Throwable e) {
      logger.error(e);
    }
  }

}
