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
package gov.pnnl.cat.ui.rcp;

import gov.pnnl.cat.core.resources.IResource;

import java.util.List;

import org.eclipse.jface.viewers.ISelection;

/**
 */
public interface IResourceSelection extends ISelection {
  /**
   * Returns the resource nodes in this selection, or <code>null</code>
   * if the resource node is unavailable.
   * 
  
   * @return List<IResource>
   */
  public List<IResource> getIResources();
  
  
  /**
   * Returns the first selected resource
  
   * @return IResource
   */
  public IResource getIResource();
}
