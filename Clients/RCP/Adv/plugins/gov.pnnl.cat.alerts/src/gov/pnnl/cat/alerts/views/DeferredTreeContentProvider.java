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
package gov.pnnl.cat.alerts.views;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;

/**
 */
public class DeferredTreeContentProvider implements ITreeContentProvider {
  private DeferredTreeContentManager contentMgr;

  /**
   * Constructor for DeferredTreeContentProvider.
   * @param viewer AbstractTreeViewer
   */
  public DeferredTreeContentProvider(final AbstractTreeViewer viewer) {
    contentMgr = new DeferredTreeContentManager(viewer);
  }

  /**
   * Method getChildren.
   * @param parent Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  @Override
  public Object[] getChildren(Object parent) {
    return contentMgr.getChildren(parent);
  }

  /**
   * Method getParent.
   * @param element Object
   * @return Object
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
   */
  @Override
  public Object getParent(Object element) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  @Override
  public boolean hasChildren(Object element) {
    return contentMgr.mayHaveChildren(element);
  }

  /**
   * Method getElements.
   * @param inputElement Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object)
   */
  @Override
  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }

  /**
   * Method dispose.
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose() {
    // do nothing
  }

  /**
   * Method inputChanged.
   * @param viewer Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // do nothing
  }

}
