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
package gov.pnnl.cat.ui.rcp.actions;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;

/**
 * Abstract CAT view action.
 * @see Action
 * @version $Revision: 1.0 $
 */
public abstract class ViewerAction extends Action {
  protected static Logger logger = CatLogger.getLogger(ViewerAction.class);
  
  // Constants
  public static final int ENABLED_ON_MULTIPLE_RESOURCES = 1;
  public static final int ENABLED_ON_SINGLE_FILE        = 2;
  public static final int ENABLED_ON_SINGLE_FOLDER      = 4;
  public static final int ENABLED_ON_EMPTY_SELECTION    = 8;
  public static final int ENABLED_FOR_WRITE             = 16;
  public static final int ENABLED_FOR_DELETE            = 32;
  public static final int ENABLED_ON_TABLE              = 64;

  // Local Variables
  private ContentViewer viewer;

  
  public ViewerAction() {
    super();
  }

  /**
   * Constructor for ViewerAction.
   * @param text String
   * @param image ImageDescriptor
   */
  public ViewerAction(String text, ImageDescriptor image) {
    super(text, image);
  }

  /**
   * Constructor for ViewerAction.
   * @param text String
   * @param style int
   */
  public ViewerAction(String text, int style) {
    super(text, style);
  }

  /**
   * Constructor for ViewerAction.
   * @param text String
   */
  public ViewerAction(String text) {
    super(text);
  }

  
  /**
   * Set the ContentViewer for this action. 
   * @param viewer
   */
  public void setViewer(ContentViewer viewer) {
    this.viewer = viewer;
  }

  /**
   * Get the ContentViewer for this action.
  
   * @return ContentViewer
   */
  public ContentViewer getViewer() {
    return this.viewer;
  }

  /**
   * Get the policy on how this action should be enabled.
  
   * @return policy for enabling this action. */
  public abstract int getPolicy();

  
  /**
   * Query the action's policy against another.
   * @param policy
  
   * @return boolean
   */
  public boolean supportsPolicy(int policy) {
    return (getPolicy() & policy) > 0;
  }

  
  /**
   * Updates if this action is enabled based on passed in selection object.
   * @param selection
   */
  public void updateEnabledStatus(ISelection selection) {
    // Note: selection is null when the table viewer is loaded and, therefore, nothing has been selected yet.
    StructuredSelection structSelection = null;
    
    // Set the status to true at first so that they can be ANDed in the end. 
    boolean canAccess = true;
    boolean canDelete = true;
    boolean isTable = true;
    boolean isEnabled = true;

    if (selection == null) {
      structSelection = new StructuredSelection();
    } else {
      structSelection = (StructuredSelection) selection;
    }

    
    // Begin testing the selectioned object(s) against this action's policy.
    if (structSelection.isEmpty()) {
      isEnabled = supportsPolicy(ENABLED_ON_EMPTY_SELECTION);
    } else {
      
      if (supportsPolicy(ENABLED_FOR_WRITE)) {
        canAccess = true;
      }
      
      if (supportsPolicy(ENABLED_FOR_DELETE)) {
        canDelete = true;
      }      
      
      if (supportsPolicy(ENABLED_ON_TABLE)) {
        if (!(this.viewer instanceof TableViewer))
          isTable = false;
      }

      if (structSelection.size() == 1) {
        IResource resource = RCPUtil.getResource(structSelection.getFirstElement());

        if (resource instanceof IFile) {
          isEnabled = supportsPolicy(ENABLED_ON_SINGLE_FILE);
        } else if (resource instanceof IFolder) {
          isEnabled = supportsPolicy(ENABLED_ON_SINGLE_FOLDER) && canAccess && canDelete && isTable;
        }
        
      } else {
        // sanity check -> selection.size should be > 1
        if (!(structSelection.size() > 1)) {
          //EZLogger.logError(new Exception(), "Unexpected condition!  Selection is not empty, but size is < 1: " + structSelection.isEmpty() + " - " + structSelection.size());
          logger.error("Unexpected condition!  Selection is not empty, but size is < 1: " + structSelection.isEmpty() + " - " + structSelection.size(), new Exception());
        } 
        isEnabled = supportsPolicy(ENABLED_ON_MULTIPLE_RESOURCES) && canAccess && canDelete && isTable;
      }
    }
    
    setEnabled(isEnabled);
  }
}
