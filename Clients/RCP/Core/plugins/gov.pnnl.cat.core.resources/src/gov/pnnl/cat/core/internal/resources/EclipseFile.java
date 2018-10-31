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

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.velo.model.CmsPath;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdaptable;

/***
 * This is a lightweight handle to a resource in the ResourceManager, who 
 * maintains the cache and does all the real work.
 * @version $Revision: 1.0 $
 */
public class EclipseFile extends EclipseFileImpl implements IFile, IAdaptable {
  /**
   * Constructor for EclipseFile.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseFile(CmsPath path, String type, IResourceManager mgr) {
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
