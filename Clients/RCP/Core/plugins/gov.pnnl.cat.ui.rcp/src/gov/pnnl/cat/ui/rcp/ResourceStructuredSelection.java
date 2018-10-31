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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * A concrete implementation of the <code>IResourceSelection</code> interface,
 * suitable for instantiating.
 * @version $Revision: 1.0 $
 */
public class ResourceStructuredSelection extends StructuredSelection implements IResourceSelection {
  private List<IResource> resourceNodes = new ArrayList<IResource>();

  /**
   * Creates an empty selection.
   *
   */
  public ResourceStructuredSelection() {
  }

  /**
   * Creates a resource selection containing the specified resource.
   * 
  
   * @param resourceNode IResource
   */
  public ResourceStructuredSelection(IResource resourceNode) {
    this.resourceNodes.add(resourceNode);
  }

  /**
   * Creates a CatItemNode selection from the specified selection.
   * 
   * @param selection
   */
  public ResourceStructuredSelection(IStructuredSelection selection) {
    if (selection == null || selection.isEmpty()) {
      return;
    }

    IStructuredSelection structuredSelection = (IStructuredSelection) selection;
    @SuppressWarnings("rawtypes")
    Iterator it = structuredSelection.iterator();
   
    while(it.hasNext()) {
      IResource catItemNode = (IResource) it.next();
      resourceNodes.add(catItemNode);
    }
  }

  /**
   * Returns the resource node in this selection, or <code>null</code>
   * if the resource node is unavailable.
   * 
  
   * @return IResource
   * @see gov.pnnl.cat.ui.rcp.IResourceSelection#getIResource()
   */
  public IResource getIResource() {
    if(resourceNodes.size() > 0) {
      return resourceNodes.get(0);
    }
    return  null;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.IResourceSelection#getIResources()
   */
  @Override
  public List<IResource> getIResources() {
    return resourceNodes;
  }

  /**
   * Method isEmpty.
   * @return boolean
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  public boolean isEmpty() {
    return resourceNodes == null || resourceNodes.size() == 0;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.StructuredSelection#getFirstElement()
   */
  @Override
  public Object getFirstElement() {
    return getIResource();
  }

  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.StructuredSelection#iterator()
   */
  @SuppressWarnings("rawtypes")
  @Override
  public Iterator iterator() {
    return resourceNodes.iterator();
  }

  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    if (isEmpty()) {
      return super.toString();
    }
    String str = "";
    int count = 0;
    for(IResource resource : resourceNodes) {
      if(count > 0) {
        str += "; ";
      }
      str += resource.getPath().toDisplayString();
      count++;
    }
    return str;
  }
}
