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
package gov.pnnl.cat.ui.images;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.UiPlugin;
import gov.pnnl.velo.util.VeloConstants;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Symbolic constants for commonly used images.
 * @version $Revision: 1.0 $
 */
public class SharedImages {

  public static final String CAT_IMG_BACK = "back.png";
  
  public static final String CAT_IMG_FORWARD = "forward.png";
  
  public static final String CAT_IMG_BOOKS = "books.gif";

  public final static String CAT_IMG_COPY = "copy.gif";

  public final static String CAT_IMG_CUT = "cut.gif";

  public final static String CAT_IMG_DEL = "delete2.gif";

  public final static String CAT_IMG_DOC = "document.gif";

  public final static String CAT_IMG_FOLDER_CLOSED = "folder_closed.gif";

  public final static String CAT_IMG_FOLDER_CUBES = "folder_cubes.gif";

  public final static String CAT_IMG_FOLDER_INTO = "folder_into.gif";

  public final static String CAT_IMG_FOLDER_NEW = "folder_new.gif";

  public final static String CAT_IMG_FOLDER_OPEN = "folder_open.gif";

  public final static String CAT_IMG_FOLDER_WINDOW = "folder_window.gif";

  public final static String CAT_IMG_PASTE = "paste.gif";

  public final static String CAT_IMG_PREFERENCES = "preferences.gif";

  public final static String CAT_IMG_REFESH = "refresh.gif";

  public final static String CAT_IMG_WINDOW = "window.gif";

  public final static String CAT_IMG_UP = "nav_up_blue.gif";

  public final static String CAT_IMG_DOWN = "nav_down_blue.gif";

  public final static String CAT_IMG_COLLAPSE = "collapse.gif";

  public final static String CAT_IMG_SEARCH = "find.gif";

  public final static String CAT_IMG_LINK_DECORATOR = "link_decorator.gif";

  public final static String CAT_IMG_NO_TRANSFORMER_DECORATOR = "question_ov.gif";

  public final static String CAT_IMG_TRANSFORMER_FAILED_DECORATOR = "error_co.gif";

  public final static String CAT_IMG_GHOST_DECORATOR = "ghosty_decorator_white.gif";

  public final static String CAT_IMG_HOME_FOLDER = "house.gif";

  public final static String CAT_IMG_HOME_FOLDER_OTHER = "house_dark.gif";

  public final static String CAT_IMG_FAVORITES = "star_blue.gif";

  public final static String CAT_IMG_LIBRARY = "data.gif";

  public final static String CAT_IMG_VIRTUAL_FOLDER = "blue_folder_closed.gif";

  public final static String CAT_IMG_VIRTUAL_TREE = "branch.gif";

  public final static String CAT_IMG_LINKED_FOLDER = "CAT_Folder_With_Link.gif";

  public final static String CAT_IMG_FOLDER_GRAY = "folder_gray.png";

  public final static String CAT_IMG_TAXONOMY_NEW = "taxonomy_add.png";

  public final static String CAT_IMG_PROJECT_NEW = "project_add.png";

  public final static String CAT_IMG_PROJECT = "briefcase.gif";

  public final static String CAT_IMG_IMPORT_TAXONOMY = "taxonomy_add.png";

  public final static String CAT_IMG_PERSON = "person.gif";

  public static final String CAT_IMG_PERSON_SYS_ADMIN = "knight2.gif";

  public static final String CAT_IMG_PERSON_PORTRAIT_FEMALE = "photo_portrait_female.gif";

  public static final String CAT_IMG_PERSON_PORTRAIT_MALE = "photo_portrait_male.gif";

  public static final String CAT_IMG_PERSON_PORTRAIT_MALE_ERROR = "photo_portrait_male_error.png";

  public static final String CAT_IMG_TEAMS = "team.png";

  public static final String CAT_IMG_TEAMS_ERROR = "team_error.png";

  public static final String CAT_IMG_TEAMS_FOLDER = "team_folder.png";

  public static final String CAT_IMG_USER_NEW = "user_add.png";

  public static final String CAT_IMG_TEAM_NEW = "team_add.png";

  public static final String CAT_IMG_WRENCH = "wrench.png";

  public static final String CAT_IMG_PROCESS_DIAGRAM = "process_diagram.png";

  public static final String CAT_IMG_DELETE_COMMENT = "message_delete.png";

  public static final String CAT_IMG_EDIT_COMMENT = "message_edit.png";

  public static final String CAT_IMG_NEW_COMMENT = "message_add.png";

  public static final String CAT_IMG_DOC_ERROR = "document_error.png";

  public static final String CAT_IMG_WORKSET = "workset_wiz.png";

  public static final String CAT_IMG_FOLDER = "folder.gif";
  
  public static final String CAT_IMG_SEARCH_SUB = "search_subscription.png";

  public static final int CAT_IMG_SIZE_16 = 16;

  public static final int CAT_IMG_SIZE_8 = 8;

  public static final int CAT_IMG_SIZE_32 = 32;

  public static final int CAT_IMG_SIZE_12 = 12;

  public static final int CAT_IMG_SIZE_64 = 64;

  public static final int CAT_IMG_SIZE_48 = 48;

  private static final String RESOURCE_IMAGE_EXTENSION_POINT = "gov.pnnl.cat.ui.resourceImage";

  private static final String CLASS_ATTRIBUTE = "class";

  private static final Logger LOG = CatLogger.getLogger(SharedImages.class);

  private static final SharedImages INSTANCE = new SharedImages();


  /**
   * Return the singleton instance of {@link SharedImages}
   * 
  
   * @return {@link SharedImages} */
  public static SharedImages getInstance() {
    return INSTANCE;
  }

  /**
   * Enforce singleton pattern by not allowing instantiation outside this class.
   */
  private SharedImages() {
    super();
  }

  /**
   * Return the filename's extension.
   * <p>
   * Will return null if the filename doesn't have an extension
   * </p>
   * 
   * @param filename
   *          String file name to parse
  
   * @return String extension */
  protected String getExtension(String filename) {
    int index = filename.lastIndexOf('.');
    String extension = null;

    if (index != -1) {
      extension = filename.substring(index);
    }

    return extension;
  }

  /**
   * Retrieve the {@link Image} from the registry.
   * <p>
   * Call {@link #getImage(String, IResource, int)} passing null for the {@link IResource}.
   * </p>
   * 
  
   * @param size
   *          int image size
  
   * 
  
   * @param key String
   * @return {@link Image} * @see UiPlugin#getImage(String, int) */
  public Image getImage(String imageName, int size) {
    return getImage(imageName, null, size);
  }

  /**
   * Return the {@link Image} for the given name, {@link IResource}, and image size.
   * 
   * @param symbolicName
   *          String image name
   * @param resource
   *          {@link IResource} to retrieve image for
   * @param size
   *          int image size
  
   * @return {@link Image} */
  private Image getImage(String symbolicName, IResource resource, int size) {
    Image image = null;
    
    if(resource != null && resource instanceof IFile) {
      ImageDescriptor desc = getSystemImageDescriptor(symbolicName); 
      
      if (desc == null) {
        image = UiPlugin.getDefault().getImage(CAT_IMG_DOC, size);
        
      } else {
        image = SWTResourceManager.getImage(desc);
      }      
    
    } else {
      // will get here for the first time requesting say a closed folder icon...
      image = UiPlugin.getDefault().getImage(symbolicName, size);
      
      if (image == null) {
        // just return plain old doc image, we'll get here for the first time the doc image was requested:
        image = UiPlugin.getDefault().getImage(CAT_IMG_DOC, size);
      }      
    }

    return image;
  }

  /**
   * Retrieve the {@link ImageDescriptor} from the registry.
   * <p>
   * Call {@link #getImageDescriptor(String, IResource, int)} passing null for the {@link IResource}.
   * </p>
   * 
   * @param size
   *          int image size
   * 
   * @param key String
   * @return {@link ImageDescriptor} * @see UiPlugin#getImageDescriptor(String, int) */
  public ImageDescriptor getImageDescriptor(String key, int size) {
    return getImageDescriptor(key, null, size);
  }

  /**
   * Return the {@link ImageDescriptor} for the given image name, {@link IResource}, and size.
   * 
   * @param symbolicName
   *          String image name
   * @param resource
   *          {@link IResource} to retrieve image for
   * @param size
   *          int image size
   * 
   * @return {@link ImageDescriptor} * @see SharedImages#getImageDescriptor(String, int) * @see #getFileSystemImage(String, int) * @see UiPlugin#getImageDescriptor(String, int) */
  private ImageDescriptor getImageDescriptor(String symbolicName, IResource resource, int size) {
    ImageDescriptor image = null;
    
    if(resource != null && resource instanceof IFile) {
      ImageDescriptor desc = getSystemImageDescriptor(symbolicName); 
      
      if (desc == null) {
        image = UiPlugin.getDefault().getImageDescriptor(CAT_IMG_DOC, size);
        
      } else {
        image = desc;
      }      
    
    } else {
      // will get here for the first time requesting say a closed folder icon...
      image = UiPlugin.getDefault().getImageDescriptor(symbolicName, size);
      
      if (image == null) {
        // just return plain old doc image, we'll get here for the first time the doc image was requested:
        image = UiPlugin.getDefault().getImageDescriptor(CAT_IMG_DOC, size);
      }      
    }

    return image;
  }

  /**
   * Return the {@link ImageDescriptor} for the given {@link IResource} and size.
   * 
   * @param resource
   *          {@link IResource} to retrieve image for
   * @param size
   *          int image size
  
   * 
  
  
   * @return {@link ImageDescriptor} * @see #getImageKeyForResource(IResource) * @see #getImageDescriptor(String, IResource, int) */
  public ImageDescriptor getImageDescriptorForResource(IResource resource, int size) {
    ImageDescriptor imageDescriptor = null;

    for (ResourceImageFactory resourceImageFactory : getResourceImageExtensions()) {
      imageDescriptor = resourceImageFactory.getImageDescriptor(resource, size);
      if (imageDescriptor != null) {
        break;
      }
    }

    if (imageDescriptor == null) {
      String key = getImageKeyForResource(resource);
      imageDescriptor = getImageDescriptor(key, resource, size);
    }

    return imageDescriptor;
  }

  /**
   * Return the {@link Image} for the given {@link IResource} and size.
   * 
   * @param resource
   *          {@link IResource} to retrieve image for
   * @param size
   *          int image size
  
   * 
  
  
   * @return {@link Image} * @see #getImageKeyForResource(IResource) * @see #getImage(String, IResource, int) */
  public Image getImageForResource(IResource resource, int size) {
    Image image = null;

    for (ResourceImageFactory resourceImageFactory : getResourceImageExtensions()) {
      image = resourceImageFactory.getImage(resource, size);

      if (image != null) {
        break;
      }
    }

    if (image == null) {
      String key = getImageKeyForResource(resource);
      image = getImage(key, resource, size);
    }

    return image;
  }

  /**
   * Generate a key for the given {@link IResource} used in the cache Map of images.
   * 
   * @param resource
   *          {@link IResource} to generate a key for
  
   * @return String key */
  private String getImageKeyForResource(IResource resource) {
    try {
      if (resource instanceof ILinkedResource) {
        ILinkedResource link = (ILinkedResource) resource;
        IResource target = link.getTarget();
       
        // target could be null if it has been deleted
        // TODO: need a decorator for orphaned link
        if(target != null) {
          return getImageKeyForResource(link.getTarget());
        } else {
          return CAT_IMG_DOC;
        }
      } else if (resource instanceof IFolder) {
        IFolder folder = (IFolder) resource;
        IResourceManager mgr = ResourcesPlugin.getResourceManager();

        // reference library (TODO: get rid of this)
        if (folder.getPath().toDisplayString().equals("/" + IResourceManager.REFERENCE_LIBRARY)) {
          return CAT_IMG_LIBRARY;
        }

        // user home folder
        try {
          if (resource.isType(IResource.USER_HOME_FOLDER)) {
            // if its the logged in user's home folder return the special icon
            if (mgr.getHomeFolder().getPath().equals(resource.getPath())) {
              return CAT_IMG_HOME_FOLDER;
            } else {// otherwise return the generic home folder icon
              return CAT_IMG_HOME_FOLDER_OTHER;
            }
          }

        } catch (Throwable e) {
          LOG.warn("unable to lookup personal library", e);
        }

//        // favorites (note that this needs to be listed *above* taxonomy root)
//        if (resource.isType(IResource.FAVORITES_ROOT)) {
//          return CAT_IMG_FAVORITES;
//        }
//
//        // personal libraries
//        if (resource.hasAspect(VeloConstants.ASPECT_PERSONAL_LIBRARY_ROOT)) {
//          return CAT_IMG_LIBRARY;
//        }

        // team home folder
        if (resource.isType(IResource.TEAM_HOME_FOLDER)) {
          return CAT_IMG_TEAMS_FOLDER;
        }
        
        // category (TODO: change this to int comparison)
        if(resource.getNodeType().equals(VeloConstants.TYPE_CATEGORY)) {
          return CAT_IMG_VIRTUAL_FOLDER;
        }

        // taxonomy root
        if (resource.isType(IResource.TAXONOMY_ROOT)) {
          return CAT_IMG_VIRTUAL_TREE;
        }

        // taxonomy folder
        if (resource.isType(IResource.TAXONOMY_FOLDER)) {
          return CAT_IMG_VIRTUAL_FOLDER;
        }

        // project
        if (resource.isType(IResource.PROJECT)) {
          return CAT_IMG_PROJECT;
        }

        // config root
        if (resource.isType(IResource.CONFIG_ROOT)) {
          return CAT_IMG_WRENCH;
        }

        // default folder
        return CAT_IMG_FOLDER_CLOSED;

      } else if (resource instanceof IFile) {
        String ext = getExtension(resource.getName());

        if (ext == null || ext.length() == 0) {
          return CAT_IMG_DOC;
        } else {
          return ext;
        }
      }

      LOG.warn("in SharedImages, getImageKeyForResource: there is a resource we haven't accounted for");
    } catch (Exception e) {
      LOG.error("Error determining image for " + resource.getPath(), e);
    }

    return null;
  }

  /**
   * Retrieve the List of extensions for the {@link #RESOURCE_IMAGE_EXTENSION_POINT}.
   * 
  
   * @return List of {@link ResourceImageFactory} */
  private List<ResourceImageFactory> getResourceImageExtensions() {
    IConfigurationElement[] configurationElements = Platform.getExtensionRegistry().getConfigurationElementsFor(RESOURCE_IMAGE_EXTENSION_POINT);
    List<ResourceImageFactory> extensions = new ArrayList<ResourceImageFactory>(configurationElements.length);

    try {
      for (IConfigurationElement configurationElement : configurationElements) {
        Object extension = configurationElement.createExecutableExtension(CLASS_ATTRIBUTE);

        if (extension instanceof ResourceImageFactory) {
          extensions.add((ResourceImageFactory) extension);
        }
      }
    } catch (CoreException e) {
      LOG.error("Unable to instantiate extension", e);
    }

    return extensions;
  }

  /**
   * Return the {@link ImageDescriptor} from the registry's external editor.
   * 
   * @param symbolicName
   *          String image name
  
   * @return {@link ImageDescriptor} */
  protected ImageDescriptor getSystemImageDescriptor(String symbolicName) {
    ImageDescriptor desc = null;
    IEditorRegistry fRegistry = PlatformUI.getWorkbench().getEditorRegistry();
    try {
      IEditorDescriptor editorDesc = fRegistry.getDefaultEditor(symbolicName);

      if (editorDesc != null) {
        desc = editorDesc.getImageDescriptor();
      } else {
        desc = PlatformUI.getWorkbench().getEditorRegistry().getSystemExternalEditorImageDescriptor(symbolicName);
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }

    return desc;
  }
}
