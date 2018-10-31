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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.GenericContainer;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public class GenericContainerWorkbenchAdapter implements ICatWorkbenchAdapter {

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
   */
  public ImageDescriptor getImageDescriptor(Object object) {
    GenericContainer container = (GenericContainer) object;
    return container.getImageDescriptor();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
   */
  public Object getParent(Object object) {
    GenericContainer container = (GenericContainer) object;
    return container.getParent();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element) {
    GenericContainer container = (GenericContainer) element;
    Object[] children = container.getChildren();  
    return children != null && children.length > 0;
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object element) {
    GenericContainer container = (GenericContainer) element;
    return container.getChildren();  
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    GenericContainer container = (GenericContainer) element;
    return container.getImage();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    GenericContainer container = (GenericContainer) element;
    return container.getName();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
   */
  public String getLabel(Object o) {
    GenericContainer superRoot = (GenericContainer) o;
    return superRoot.getName();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getPath(java.lang.Object)
   */
  public CmsPath getPath(Object element) {
    // this shouldn't actually ever be called.
    return null;
  }

}