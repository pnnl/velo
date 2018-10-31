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
package gov.pnnl.cat.core.internal.resources;

import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.model.CmsPath;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Remember that every time we add a new method to IResource, we may need to 
 * overload in this class.
 * @version $Revision: 1.0 $
 */
public abstract class EclipseLinkedResource extends EclipseLinkedResourceImpl implements ILinkedResource, IAdaptable {
  /**
   * Constructor for EclipseLinkedResource.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseLinkedResource(CmsPath path, String type, IResourceManager mgr) {
    super(path, type, mgr);
  }
  
  //ZCG: copied from eclipse's PlatformObject class since we can only extend one class
  /**
   * Returns an object which is an instance of the given class
   * associated with this object. Returns <code>null</code> if
   * no such object can be found.
   * <p>
   * This implementation of the method declared by <code>IAdaptable</code>
   * passes the request along to the platform's adapter manager; roughly
   * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
   * Subclasses may override this method (however, if they do so, they
   * should invoke the method on their superclass to ensure that the
   * Platform's adapter manager is consulted).
   * </p>
   *
   * @param adapter the class to adapt to
  
  
   * @return the adapted object or <code>null</code> * @see IAdaptable#getAdapter(Class) */
  public Object getAdapter(Class adapter) {
    return AdapterManager.getDefault().getAdapter(this, adapter);
  }
	
}
