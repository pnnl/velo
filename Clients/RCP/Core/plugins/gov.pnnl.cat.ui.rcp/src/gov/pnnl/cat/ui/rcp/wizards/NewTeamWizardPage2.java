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
package gov.pnnl.cat.ui.rcp.wizards;

import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.rcp.dialogs.properties.team.TeamMembersPropertiesComposite;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 */
public class NewTeamWizardPage2 extends WizardPage {

  private TeamMembersPropertiesComposite teamMemberPropsComposite;

  /**
   * Create the wizard
   */
  public NewTeamWizardPage2() {
    super("wizardPage");
    setTitle("Add Team Members");
    setDescription("Add team members to the new team.");
  }

  /**
   * Create contents of the wizard
   * @param parent
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
   */
  public void createControl(Composite parent) {
    this.teamMemberPropsComposite = new TeamMembersPropertiesComposite(parent, SWT.NULL, null);
    setControl(this.teamMemberPropsComposite);
  }

  /**
   * Method getMembers.
   * @return IUser[]
   */
  public IUser [] getMembers() {
    return teamMemberPropsComposite.getMembers();
  }
}
