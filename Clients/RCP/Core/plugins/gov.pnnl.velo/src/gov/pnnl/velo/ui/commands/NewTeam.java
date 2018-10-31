/**
 * 
 */
package gov.pnnl.velo.ui.commands;

import gov.pnnl.cat.ui.rcp.wizards.NewTeamWizard;

import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author D3K339
 *
 */
public class NewTeam extends InvokeWizardCommand {

  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.commands.InvokeWizardCommand#getWizardId(org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  protected String getWizardId(IStructuredSelection selection) {
    return NewTeamWizard.ID;
  }

}
