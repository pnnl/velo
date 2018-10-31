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
package gov.pnnl.cat.core.internal.resources.cache;

import gov.pnnl.cat.core.internal.resources.ResourceService;
import gov.pnnl.cat.core.internal.resources.datamodel.CachedResource;
import gov.pnnl.cat.core.resources.CmsServiceLocator;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.util.NamespacePrefixResolver;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.model.Resource;
import gov.pnnl.velo.util.VeloConstants;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 */
public class Cache {

  private static Logger logger = CatLogger.getLogger(Cache.class);

  // Resource is a heavyweight object that stores all the properties and aspects for that resource
  // IResource is a lightweight handle
  private Map<CmsPath, CachedResource> resourcesCachedByPath = new HashMap<CmsPath, CachedResource>();
  private Map<CmsPath, List<CachedResource>> childrenCachedByPath = new HashMap<CmsPath, List<CachedResource>>();
  private Map<String, CmsPath> uuidsToPaths = new HashMap<String, CmsPath>();
  private static IResourceManager resourceManager;
  private String monitor = new String("monitor");

  /**
   * Constructor for Cache.
   * @param resourceManager IResourceManager
   */
  public Cache(IResourceManager resourceManager) {
    this.resourceManager = resourceManager;
  }

  /**
   * Method getResource.
   * @param handle IResource
   * @return Resource
   */
  public CachedResource getResource(IResource handle) {
    return getResource(handle.getPath());
  }
  
  /**
   * Method getResource.
   * @param path CmsPath
   * @return Resource
   */
  public CachedResource getResource(CmsPath path) {
    CachedResource resource = null;
    synchronized(monitor) {
      resource = resourcesCachedByPath.get(path);
    }
    return resource;
  }

  /**
   * Method getResource.
   * @param uuid String
   * @return Resource
   */
  public CachedResource getResource(String uuid) {
    CmsPath path  = null;
    CachedResource resource = null;

    synchronized(monitor) {
      path = uuidsToPaths.get(uuid);
      if(path != null) {
        resource = resourcesCachedByPath.get(path);
      }
    }
    return resource;   
  }
  

  /**
   * Method removeResource.
   * @param uuid String
   */
  public void removeResource(String uuid) {
    CmsPath path = null;
    synchronized(monitor) {
      path = uuidsToPaths.get(uuid);
    }
    
    if(path != null) {
      removeResource(path);
    }
  }

  /**
   * Method getHandle.
   * @param uuid String
   * @return IResource
   */
  public IResource getHandle(String uuid) {
    CmsPath path  = null;
    CachedResource resource = null;

    synchronized(monitor) {
      path = uuidsToPaths.get(uuid);
      if(path != null) {
        resource = resourcesCachedByPath.get(path);
      }
    }
    if(resource != null) {
      return resource.getHandle();

    } else {
      return null;    
    }
  }

  /**
   * Method getHandle.
   * @param path CmsPath
   * @return IResource
   */
  public IResource getHandle(CmsPath path) {
    CachedResource resource = null; 
    synchronized(monitor) {
      resource = resourcesCachedByPath.get(path);
    }
    if(resource != null) {
      return resource.getHandle();
    } else {
      return null;
    }
  }
  
  private CachedResource createCachedResource(Resource rawResource) {
    IResource handle = createHandle(rawResource);
    CachedResource resource = new CachedResource(handle, rawResource);
    return resource;
  }

  /**
   * Method addResource.
   * @param resource Resource
   * @return IResource
   */
  public IResource addResource(Resource  rawResource) {
    
    CachedResource resource = createCachedResource(rawResource);
    IResource handle = resource.getHandle();
    
    // If the handle isn't null, then we care about this node, so we should put it in our cache
    if(handle != null) {
      synchronized(monitor) {
        resourcesCachedByPath.put(handle.getPath(), resource);
        uuidsToPaths.put(resource.getUuid(), handle.getPath());
        List<CachedResource> children = childrenCachedByPath.get(handle.getPath().getParent());
        
        // If this is a hidden rendition, we must add ourself to the children of the node, so if we 
        // delete the node, the cache is in sync, since we don't get notifications when hidden renditions are created
        if(resource.getAspects().contains(VeloConstants.ASPECT_HIDDEN_RENDITION)) {
          if(children == null) {
            children = new ArrayList<CachedResource>();
          } else {
            children.remove(resource);
          }
          children.add(resource);
          setChildren(handle.getPath().getParent(), children);
          
        } else if(children != null){
          children.remove(resource);
          children.add(resource);
        }
      }   
    }
    return handle;
  }

  /**
   * Method removeResource.
   * @param path CmsPath
   */
  public void removeResource(CmsPath path) {
    synchronized(monitor) {
      // get remove myself from my parent's children
      List<CachedResource> children = childrenCachedByPath.get(path.getParent());
      
      CachedResource child = resourcesCachedByPath.get(path);
      if(children != null && child != null) {
        children.remove(child);
      }
      
      recursiveRemoveResource(path);  
    }    
  }

  /**
   * Method recursiveRemoveResource.
   * @param path CmsPath
   */
  private void recursiveRemoveResource(CmsPath path) {
    CachedResource resource = resourcesCachedByPath.get(path);
    if(resource != null) {
      uuidsToPaths.remove(resource.getUuid());
    }
    
    List<CachedResource> children = childrenCachedByPath.get(path);
    if(children != null) {
      for(CachedResource child : children) {
        recursiveRemoveResource(child.getHandle().getPath());
      }
    }
    childrenCachedByPath.remove(path);
    resourcesCachedByPath.remove(path);
  }
  
  public void setRawChildren(CmsPath path, List<Resource> rawChildren) {
    List<CachedResource> childrenToAdd = new ArrayList<CachedResource>();
    
    for(Resource rawResource : rawChildren) {
      CachedResource resource = createCachedResource(rawResource);
      IResource handle = resource.getHandle();
      if(handle != null) {
        childrenToAdd.add(resource);
      }
    }
    setChildren(path, childrenToAdd);
  }
  
  /**
   * Method setChildren.
   * @param path CmsPath
   * @param children List<Resource>
   */
  public void setChildren(CmsPath path, List<CachedResource> children) {

    synchronized(monitor) {
      childrenCachedByPath.put(path, children);       
      for(CachedResource child : children) {
        resourcesCachedByPath.put(child.getHandle().getPath(), child);
        uuidsToPaths.put(child.getUuid(), child.getHandle().getPath());

      }
      
      // we also need to update the child count for the parent in the cache
      Resource parent = resourcesCachedByPath.get(path);
      if(parent == null) {
        logger.error("Trying to add children to a null resource in the cache: " + path);
      } else {
        parent.setNumChildren(children.size());
      }
     }
  }

  /**
   * Method getChildren.
   * @param parentPath CmsPath
   * @return List<Resource>
   */
  public List<CachedResource> getChildren(CmsPath parentPath) {
    List<CachedResource> children = null;
    synchronized(monitor) {
      children = childrenCachedByPath.get(parentPath);
    }
    return children;
  }

  /**
   * Method getChildrenHandles.
   * @param parentPath CmsPath
   * @return List<IResource>
   */
  public List<IResource> getChildrenHandles(CmsPath parentPath) {
    List<CachedResource> children = null;
    synchronized(monitor) {
      children = childrenCachedByPath.get(parentPath);
    }

    if(children != null) {
      List<IResource> handles = new ArrayList<IResource>();
      for(CachedResource resource : children) {
        handles.add(resource.getHandle());
      }
      return handles;  
    } else {
      return null;
    }    
  }

  
  /**
   * Wipe the whole cache
   */
   public void clear() {
     synchronized(monitor) {
       resourcesCachedByPath.clear();
       childrenCachedByPath.clear();   
       uuidsToPaths.clear();
     }
   }

   /**
    * Method createHandle.
    * @param resource Resource
    * @return IResource
    * @throws ResourceException
    */
   private IResource createHandle(Resource  resource) throws ResourceException {
     IResource handle = null;
     String primaryNodeType = resource.getType();
     CmsPath path = new CmsPath(resource.getPath());

     if (path == null || primaryNodeType == null) {
       throw new NullPointerException();
     }

     if(ResourceService.typeClassMap.containsKey(primaryNodeType) && !resource.getAspects().contains(VeloConstants.ASPECT_IGNORE)){
       try {
         Constructor<IResource> constructor = 
             ResourceService.typeClassMap.get(primaryNodeType).getDeclaredConstructor(CmsPath.class, String.class, IResourceManager.class);
         handle = constructor.newInstance(path, primaryNodeType, resourceManager);

       } catch (Exception e){
         throw new ResourceException(e);
       }
     } else {
       logger.debug("IGNORING: " + path + " of type '" + primaryNodeType + "'");

     }

     return handle;
   }

   /**
   * @param uuid - can not be null
   * @param fileName
   * @param version
   * @param property - can not be null
   * @return
   */
  public File getCachedContent(String uuid, String fileName, String version, String property) {
     File contentFile = null;
     assert(uuid != null);
     assert(property != null);
     
     try {
       File resourceFolder = null;
       File cacheFolder = getFileCacheFolder();
       File uuidFolder = new File(cacheFolder, uuid);
       File propFolder = new File(uuidFolder, getPropertyLocalName(property));
       
       if(version != null) {
         resourceFolder = new File(propFolder, version);
       } else {
         resourceFolder = propFolder;
       }
       
       if(!resourceFolder.exists()) {
         FileUtils.forceMkdir(resourceFolder);
       }
       
       if(fileName == null) {
         fileName = "un-named";
       }
       contentFile = new File(resourceFolder, fileName);
       
     
     } catch (Throwable e) {
       logger.error("failed to retrieve file from cache: " + uuid, e);
     }
     
     return contentFile;
   }
   
   private String getPropertyLocalName(String fullyQualifiedProperty) {
     QName qname = NamespacePrefixResolver.parseQNameStringSafe(fullyQualifiedProperty);
     return qname.getLocalPart();
   }

   /**
    * Method getFileCacheFolder.
    * @return File
    * @throws Exception
    */
   private File getFileCacheFolder() throws Exception {

     // create temp folder under the workspace folder
     //get object which represents the workspace
     File workspaceFolder = CmsServiceLocator.getVeloWorkspace().getVeloFolder();
     File cacheDir = new File(workspaceFolder, "CatFileCache");


     if(!cacheDir.exists()) {
       boolean mkdirSuccess = cacheDir.mkdir();
       //Had issues here with mkdir not creating the temp directory for a simulation run
       //Use while loop to force creation of directory
       if(!mkdirSuccess){
         while(!mkdirSuccess){
           mkdirSuccess = cacheDir.mkdir();
         }
       }
     }
     return cacheDir;
   }


}
