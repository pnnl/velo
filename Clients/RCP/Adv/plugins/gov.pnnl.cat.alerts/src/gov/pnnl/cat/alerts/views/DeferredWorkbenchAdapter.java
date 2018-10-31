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

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

/**
 */
public abstract class DeferredWorkbenchAdapter implements IDeferredWorkbenchAdapter {

  /**
   * Method getRule.
   * @param object Object
   * @return ISchedulingRule
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(Object)
   */
  @Override
  public ISchedulingRule getRule(Object object) {
    return null;
  }

  /**
   * Method isContainer.
   * @return boolean
   * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
   */
  @Override
  public boolean isContainer() {
    return false;
  }

  /**
   * Method getChildren.
   * @param o Object
   * @return Object[]
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(Object)
   */
  @Override
  public Object[] getChildren(Object o) {
    return null;
  }

  /**
   * Method getImageDescriptor.
   * @param object Object
   * @return ImageDescriptor
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(Object)
   */
  @Override
  public ImageDescriptor getImageDescriptor(Object object) {
    return null;
  }

  /**
   * Method getLabel.
   * @param o Object
   * @return String
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(Object)
   */
  @Override
  public String getLabel(Object o) {
    return null;
  }

  /**
   * Method getParent.
   * @param o Object
   * @return Object
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(Object)
   */
  @Override
  public Object getParent(Object o) {
    return null;
  }

}
