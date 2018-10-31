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
import gov.pnnl.cat.core.resources.ILinkedResource;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.velo.model.CmsPath;

/**
 * Remember that every time we add a new method to IResource, we may need to 
 * overload in this class.
 * @version $Revision: 1.0 $
 */
public abstract class EclipseLinkedResourceImpl extends EclipseResourceImpl implements ILinkedResource {

  /**
   * Constructor for EclipseLinkedResourceImpl.
   * @param path CmsPath
   * @param type QualifiedName
   * @param mgr IResourceManager
   */
  public EclipseLinkedResourceImpl(CmsPath path, String type, IResourceManager mgr) {
    super(path, type, mgr);
  }
  

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.ILinkedResource#getTarget()
   */
  @Override
  public IResource getTarget() {
    return getResourceManager().getTarget(getPath());
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.internal.resources.EclipseResourceImpl#isLink()
   */
  @Override
  public boolean isLink() {
    return true;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.core.resources.ILinkedResource#updateTarget(gov.pnnl.cat.core.resources.IFile)
   */
  @Override
	public void updateTarget(IFile newTarget) throws ResourceException {
		getResourceManager().updateLinkTarget(getPath(), newTarget);
	}
	
}
