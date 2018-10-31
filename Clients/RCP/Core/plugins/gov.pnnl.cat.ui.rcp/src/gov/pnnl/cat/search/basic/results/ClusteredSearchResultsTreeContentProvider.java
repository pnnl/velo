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
package gov.pnnl.cat.search.basic.results;

import gov.pnnl.cat.core.resources.search.ICluster;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 */
public class ClusteredSearchResultsTreeContentProvider implements ITreeContentProvider {

  /**
   * Method getChildren.
   * @param parentElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(Object parentElement) {
    ICluster cluster = (ICluster) parentElement;
    if (cluster.getSubclusters() != null && cluster.getSubclusters().size() > 0) {
      return (ICluster[]) cluster.getSubclusters().toArray(new ICluster[cluster.getSubclusters().size()]);
    }
    return new ICluster[0];
  }

  /**
   * Method getParent.
   * @param element Object
   * @return Object
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
   */
  public Object getParent(Object element) {
    return ((ICluster) element).getParent();
  }

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    ICluster cluster = (ICluster) element;
    return cluster.getSubclusters() != null && cluster.getSubclusters().size() > 0;
  }

  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object)
   */
  public Object[] getElements(Object inputElement) {
    ClusteredSearchResult root = (ClusteredSearchResult) inputElement;

    return new ICluster[] { root.getClusteredResultRoot() };
  }

  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub

  }

  /**
   * Method inputChanged.
   * @param viewer Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // TODO Auto-generated method stub

  }
  //
  // public Object[] getChildren(Object parentElement) {
  // return
  // ((ClusteredSearchResultInput)parentElement).getChildren(parentElement);
  // }
  //
  // public Object getParent(Object element) {
  // return ((ClusteredSearchResultInput)element).getParent(element);
  // }
  //
  // public boolean hasChildren(Object element) {
  // return ((ClusteredSearchResultInput)element).getChildren(element).length >
  // 0;
  // }
  //
  // public Object[] getElements(Object inputElement) {
  // return
  // ((ClusteredSearchResultInput)inputElement).getChildren(inputElement);
  // }

}
