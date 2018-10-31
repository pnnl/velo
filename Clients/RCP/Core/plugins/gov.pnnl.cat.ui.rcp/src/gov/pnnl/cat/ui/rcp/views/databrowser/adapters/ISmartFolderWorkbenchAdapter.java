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
package gov.pnnl.cat.ui.rcp.views.databrowser.adapters;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.cat.ui.rcp.views.databrowser.model.ISmartFolder;
import gov.pnnl.velo.model.CmsPath;

/**
 */
public class ISmartFolderWorkbenchAdapter implements ICatWorkbenchAdapter {

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
   */
  public ImageDescriptor getImageDescriptor(Object object) {
    ISmartFolder smartFolder = (ISmartFolder) object;
    return smartFolder.getImageDescriptor();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
   */
  public Object getParent(Object object) {
    return null;  // we don't care about the parent
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element) {
    return false; // no children
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object element) {
    return null; // no children
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage(Object element, int columnIndex) {
    ISmartFolder smartFolder = (ISmartFolder) element;
    return smartFolder.getImage();

  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getColumnText(java.lang.Object, int)
   */
  public String getColumnText(Object element, int columnIndex) {
    ISmartFolder smartFolder = (ISmartFolder) element;
    return smartFolder.getName();

  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
   */
  public String getLabel(Object element) {
    ISmartFolder smartFolder = (ISmartFolder) element;
    return smartFolder.getName();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.views.ICatWorkbenchAdapter#getPath(java.lang.Object)
   */
  public CmsPath getPath(Object element) {
    // this shouldn't actually ever be called.
    return null;
  }

}
