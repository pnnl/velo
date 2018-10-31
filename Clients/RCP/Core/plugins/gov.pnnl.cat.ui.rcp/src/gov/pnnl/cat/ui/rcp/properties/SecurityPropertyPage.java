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
package gov.pnnl.cat.ui.rcp.properties;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.ui.rcp.security.PermissionsForm;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.model.ACL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Allows user to change permissions on the current document.
 */
public class SecurityPropertyPage extends PropertyPage {
  private PermissionsForm permissionsForm;
  private ISecurityManager smgr = ResourcesPlugin.getSecurityManager();
  
  /**
   * Constructor for SamplePropertyPage.
   */
  public SecurityPropertyPage() {
    super();
    noDefaultAndApplyButton();
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents(Composite parent) {
    this.setTitle("Permissions");
    GridLayout layout = new GridLayout();
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    layout.marginRight = 10;
    layout.numColumns = 1;
    parent.setLayout(layout);
    permissionsForm = new PermissionsForm(parent, SWT.NONE);
    permissionsForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    permissionsForm.setFormBackgroundColor(parent.getBackground());
    
    loadACL();
    return permissionsForm;
  }
  
  private void loadACL() {
    try {
      IResource selected = getResource();
      ACL acl = smgr.getPermissions(selected.getPath());
      permissionsForm.loadPermissions(acl);
    
    } catch (Throwable e) {
      StatusUtil.handleStatus(
          "Error loading permissions.",
          e, StatusManager.SHOW);
    }
  }
  
  /**
   * Method getResource.
   * @return IResource
   */
  protected IResource getResource() {
    return RCPUtil.getResource(getElement());
  }
  

  /* (non-Javadoc)
   * @see org.eclipse.jface.preference.PreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    if(permissionsForm.isEditable()) {
      ACL permission = permissionsForm.getPermissions();
      try {
        smgr.setPermissions(new ACL[]{permission});

      } catch (Throwable e) {
        StatusUtil.handleStatus(
            "Error saving permissions.",
            e, StatusManager.SHOW);
      }
    }
    return true;
  }


}
