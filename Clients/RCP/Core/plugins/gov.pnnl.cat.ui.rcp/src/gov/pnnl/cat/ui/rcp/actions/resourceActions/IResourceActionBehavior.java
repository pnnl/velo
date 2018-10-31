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
package gov.pnnl.cat.ui.rcp.actions.resourceActions;

import gov.pnnl.cat.core.resources.IFolder;

import org.eclipse.swt.widgets.Shell;

/**
 */
public interface IResourceActionBehavior {
  public final static String ACTION_TYPE_COPY = "copy";
  public final static String ACTION_TYPE_LINK = "link";
  public final static String ACTION_TYPE_MOVE = "move";
  public final static String ACTION_TYPE_UPLOAD = "upload";

  /**
   * Performs the specified operation.
   * The receiver can choose not to perform the operation and
   * defer responsibility to another <tt>IResourceActionBehavior</tt>
   * further down the queue. In this case, <tt>false</tt>
   * must be returned to indicate that the operation was not performed.
   * <p/>
   * If no contributors choose to overload the behavior for an operation,
   * the default operation will take place.
   * 
   * @param shell the <tt>Shell</tt> to use if user interaction is necessary.
   * @param sourcePaths the source resources in the operation
   * @param destination the destination resource (if any, can be <tt>null</tt>)
   * @param operation the operation to perform.
  
   * @return <tt>true</tt> if the operation has been performed, regardless of potential error conditions. <tt>false</tt> if the operation was <b>not</b> performed. */
  public boolean run(Shell shell, String[] sourcePaths, IFolder destination, String operation);
}
