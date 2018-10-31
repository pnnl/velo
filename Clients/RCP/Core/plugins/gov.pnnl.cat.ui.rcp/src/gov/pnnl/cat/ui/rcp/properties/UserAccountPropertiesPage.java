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

import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.rcp.dialogs.properties.user.UserPropertiesComposite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class UserAccountPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

  private UserPropertiesComposite userPropertiesComposite;
  public UserAccountPropertiesPage() {
    super();
    noDefaultAndApplyButton();
  }

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  @Override
  protected Control createContents(Composite parent) {
    IUser user = (IUser)getElement();
    userPropertiesComposite = new UserPropertiesComposite(parent, SWT.NULL, user);
    return userPropertiesComposite;
  }

}
