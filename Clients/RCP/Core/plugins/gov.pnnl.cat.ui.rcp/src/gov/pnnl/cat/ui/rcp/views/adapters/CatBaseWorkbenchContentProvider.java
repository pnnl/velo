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
package gov.pnnl.cat.ui.rcp.views.adapters;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;

/**
 */
public class CatBaseWorkbenchContentProvider extends BaseWorkbenchContentProvider {
  protected Viewer viewer;
  protected boolean showFiles;
  
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(CatBaseWorkbenchContentProvider.class);

  /**
   * Constructor for CatBaseWorkbenchContentProvider.
   * @param showFiles boolean
   */
  public CatBaseWorkbenchContentProvider(boolean showFiles) {
    this.showFiles = showFiles;
  }

  /* (non-Javadoc)
   * Method declared on ITreeContentProvider.
   */
  /**
   * Method hasChildren.
   * @param element Object
   * @return boolean
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
   */
  public boolean hasChildren(Object element) {
    ICatWorkbenchAdapter adapter = RCPUtil.getCatAdapter(element);
    if (adapter != null) {
      return adapter.hasChildren(element);
    }
    return true;
  }

  /**
   * Method inputChanged.
   * @param v Viewer
   * @param oldInput Object
   * @param newInput Object
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
   */
  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    this.viewer = v;
  }

  /* (non-Javadoc)
   * Method declared on ITreeContentProvider.
   */
  /**
   * Method getChildren.
   * @param element Object
   * @return Object[]
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
   */
  public Object[] getChildren(Object element) {
    ICatWorkbenchAdapter adapter = RCPUtil.getCatAdapter(element);
    if (adapter != null) {
      // show busy cursor
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(new Cursor(null, SWT.CURSOR_WAIT));
      
      Object[] children = adapter.getChildren(element);
      if (children == null || children.length == 0) {
        if (element instanceof IResource) {
          children = new Object[] {};
        }
      }
      
      children = RCPUtil.filterHiddenFiles(children);
      
      // restore normal cursor
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(new Cursor(null, SWT.CURSOR_ARROW));
      return children;
    } else {
      return super.getChildren(element);
    }
  }

  @Override
  public Object getParent(Object element) {
    // TODO Auto-generated method stub
    return super.getParent(element);
  }


}
