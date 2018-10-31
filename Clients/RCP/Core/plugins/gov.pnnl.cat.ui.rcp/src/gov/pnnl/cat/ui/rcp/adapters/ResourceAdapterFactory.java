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

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.actions.filters.ResourceActionFilter;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.velo.util.VeloConstants;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Adapts IResource objects into standard types used in the workbench.
 */
public class ResourceAdapterFactory implements IAdapterFactory {

  public static final Class[] types = { IWorkbenchAdapter.class,
      ICatWorkbenchAdapter.class,
      IResource.class,
      IFolder.class,
      IActionFilter.class,
      URL.class,
      ITransferObjectAdapter.class};

  /**
   * Method getAdapter.
   * @param adaptableObject Object
   * @param adapterType Class
   * @return Object
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(Object, Class)
   */
  public Object getAdapter(Object adaptableObject, Class adapterType) {
    if (adaptableObject instanceof IResource) {
      if (adapterType == IWorkbenchAdapter.class || adapterType == ICatWorkbenchAdapter.class) {
        return new ResourceWorkbenchAdapter();
      }

      if (adapterType == IResource.class) {
        return ((IResource) adaptableObject);
      }

      if (adapterType == IFolder.class) {
        IResource resource = (IResource) adaptableObject;
        if (resource instanceof IFolder) {
          return resource;
        }
      }
      
      if (adapterType == IActionFilter.class) {
        return ResourceActionFilter.getInstance();
      }
      
      if(adapterType == URL.class) {
        String url = ((IResource)adaptableObject).getPropertyAsString(VeloConstants.PROP_WEB_VIEW_URL);
        if(url != null) {
          try {
            return new URL(url);
          } catch (MalformedURLException e) {
            throw new RuntimeException(e);
          }
        }
      }
      
      if(adapterType == ITransferObjectAdapter.class) {
        return new ResourceTransferObjectAdapter(adaptableObject);
      }
    }

    return null;
  }

  /**
   * Method getAdapterList.
   * @return Class[]
   * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
   */
  public Class[] getAdapterList() {
    return types;
  }

}
