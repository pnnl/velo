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
package gov.pnnl.cat.ui.rcp.adapters;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourceNotFoundException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.DateFormatUtility;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.tableexplorer.TableExplorer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.IFileTreeWrapper;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.IFolderTreeWrapper;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class ResourceWorkbenchAdapter implements ICatWorkbenchAdapter {

  private final static Object[] EMPTY_ARRAY = {};
  private final static String UNAVAILABLE_PROPERTY_DISPLAY = "";
  private Logger logger = CatLogger.getLogger(this.getClass());
  private IResourceManager mgr = ResourcesPlugin.getResourceManager();

  /**
   * Method getChildren.
   * @param element Object
   * @return Object[]
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getChildren(Object)
   */
  public Object[] getChildren(Object element) {
    Object[] kids = null; 
    IResource resource = (IResource) element;

    if (resource == null) {
      logger.debug("NULL!!!!!!");
      return EMPTY_ARRAY;
    }

    if (resource instanceof IFolder) {
      try {
        List<IResource> children = mgr.getChildren(resource.getPath());
        kids = children.toArray();

      } catch (ResourceException e) {
        logger.error("Unable to get children for " + resource.getPath(), e);
        kids = null;
      }

    } else {
      kids = EMPTY_ARRAY;
    }

    return kids;
  }

  /**
   * Method getElements.
   * @param element Object
   * @return Object[]
   */
  public Object[] getElements(Object element) {
    return getChildren(element);
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
   */
  public Object getParent(Object element) {
    IResource resource = (IResource) element;
    Object parent = null;

    try {
      if(resource instanceof IFileTreeWrapper) {
        // resource is at root level, then we need to return the RepositoryContainer as the parent
        parent = ((IFileTreeWrapper)resource).getRepositoryContainer();

      } else if (resource instanceof IFolderTreeWrapper) {
        parent = ((IFolderTreeWrapper)resource).getRepositoryContainer();
        
      } else {
        parent = resource.getParent();
      }
      
    } catch (ResourceException e) {
      parent = null;//can catch an exception here when a folder is opened in a new tab and its parent or a grandparent was deleted
    }

    return parent;
  }

  /*
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
   */
  public ImageDescriptor getImageDescriptor(Object object) {
    IResource resource = (IResource) object;

    return SharedImages.getInstance().getImageDescriptorForResource(resource, SharedImages.CAT_IMG_SIZE_16);
  }

  /*
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
   */
  public String getLabel(Object object) {
    IResource resource = (IResource) object;
    return resource.getName();
  }

  
  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    IResource resource = (IResource) element;
    try {

      if (resource instanceof IFile) {
        return false;

      } else {
        // TODO: in order to get the child count with or without folders,
        // we have to add this information to the cache when resources are loaded.
        // We need a new fake property returned from search and get calls        
        return mgr.getChildCount(resource.getPath()) > 0;
      }
    } catch (Exception e) {
      logger.error("Error processing hasChildren for " + resource.getPath(), e);
      return false;
    }
  }


  /**
   * Method getColumnImage.
   * @param element Object
   * @param columnIndex int
   * @return Image
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnImage(Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    IResource resource = RCPUtil.getResource(element);
    if (columnIndex == 0) {
      Image image = SharedImages.getInstance().getImageForResource(resource, SharedImages.CAT_IMG_SIZE_16);
      return image;
    }
    return null;
  }

  /**
   * Method getColumnText.
   * @param element Object
   * @param columnIndex int
   * @return String
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getColumnText(Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    IResource resource = RCPUtil.getResource(element);
    CmsPath path = resource.getPath();
    IResourceManager mgr = ResourcesPlugin.getResourceManager();

    switch (columnIndex) {
      case TableExplorer.NAME_COLUMN:

        return resource.getName();

      case TableExplorer.SIZE_COLUMN:
        if (resource instanceof IFile) {
          try {
            IFile theFile = (IFile) resource;

            if (theFile instanceof ILinkedResource) {
              // load the destination property from the cache
              IResource target = mgr.getPropertyAsResource(theFile.getPath(), VeloConstants.PROP_LINK_DESTINATION);
              if(target != null) {
                path = target.getPath();           
              }
            }

            // get the size property for the relevant path (which has been set
            // to the target if the file was a link)
            String value = mgr.getProperty(path, VeloConstants.PROP_SIZE);

            if (value == null) {
              // size property has not been set
              //return UNAVAILABLE_PROPERTY_DISPLAY;
              return "0 KB";
            }

            long size = Long.parseLong((String) value);
            // String sizeDisplay = FileUtils.byteCountToDisplaySize(size);
            long sizeKb = Math.round(size / 1024.0);
            if (sizeKb == 0 && size > 0) {
              sizeKb = 1;
            }
            return sizeKb + " KB";

          } catch (ResourceException e) {
            return getPropertyErrorString("Size");
          }
        }

        return "";

      case TableExplorer.MODIFIED_COLUMN:
        try {
          String value = resource.getPropertyAsString(VeloConstants.PROP_MODIFIED);

          if (value == null) {
            // size property has not been set
            return UNAVAILABLE_PROPERTY_DISPLAY;
          }

          String strDate = value;
          Date date = DateFormatUtility.parseJcrDate(strDate);

          if (date != null) {
            return DateFormatUtility.formatDefaultDateTime(date);
          }

          return strDate;



        } catch (ResourceException e) {
          return getPropertyErrorString("Modified");
        }

      case TableExplorer.PATH_COLUMN:
        return resource.getPath().toDisplayString();

      case TableExplorer.TYPE_COLUMN: 
      { 
        String type = getProperty(resource, VeloConstants.PROP_MIMETYPE, "Mimetype");

        if (resource instanceof ILinkedResource) {
          if(type == null || type.isEmpty()) {
            type = "Link";
          }
        }
        return type;
      }
      case TableExplorer.CREATED_COLUMN:
        try {
          String value = resource.getPropertyAsString(VeloConstants.PROP_CREATED);

          if (value == null) {
            // size property has not been set
            return UNAVAILABLE_PROPERTY_DISPLAY;
          }

          String strDate = value;
          Date date = DateFormatUtility.parseJcrDate(strDate);

          if (date != null) {
            return DateFormatUtility.formatDefaultDateTime(date);
          }

          return strDate;

        } catch (ResourceException e) {
          return getPropertyErrorString("Created");
        }

      case TableExplorer.CREATOR_COLUMN:
        return getProperty(resource, VeloConstants.PROP_CREATOR, "Creator");

        //      case TableExplorer.DATA_SOURCE_COLUMN:
        //        return "data_source";
      default:
        return "";
    }

  }

  /**
   * Method getProperty.
   * @param resource IResource
   * @param property QualifiedName
   * @param propertyName String
   * @return String
   */
  private String getProperty(IResource resource, String property, String propertyName) {
    try {
      String value = resource.getPropertyAsString(property);

      // the property does not exist
      if (value == null) {
        return UNAVAILABLE_PROPERTY_DISPLAY;
      }

      return value;

    } catch (ResourceNotFoundException e) {
      // If resource has been deleted from the cache, just ignore this request
      return null;

    } catch (ResourceException e) {
      logger.warn("Error retrieving property: " + propertyName, e);
      return getPropertyErrorString(propertyName);
    }
  }

  /**
   * Method getPropertyErrorString.
   * @param propertyName String
   * @return String
   */
  private String getPropertyErrorString(String propertyName) {
    return "Unavailable";
    //    return propertyName + " is Unavailable";
  }

  /**
   * Method getPath.
   * @param element Object
   * @return CmsPath
   * @see gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter#getPath(Object)
   */
  public CmsPath getPath(Object element) {
    IResource resource = (IResource) element;

    return resource.getPath();
  }
}
