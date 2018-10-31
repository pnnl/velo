package gov.pnnl.velo.dataset.handler;

import org.eclipse.jface.viewers.IStructuredSelection;

import gov.pnnl.velo.dataset.wizards.NewDatasetWizard;
import gov.pnnl.velo.ui.commands.InvokeWizardCommand;

public class InvokeNewDatasetWizard extends InvokeWizardCommand {
  public static final String ID = InvokeNewDatasetWizard.class.getCanonicalName();

  @Override
  protected String getWizardId(IStructuredSelection selection) {
    return NewDatasetWizard.ID;
  }
}