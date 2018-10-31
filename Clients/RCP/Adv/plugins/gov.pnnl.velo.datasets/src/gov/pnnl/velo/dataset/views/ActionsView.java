package gov.pnnl.velo.dataset.views;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.ui.rcp.views.AbstractToolbarView;

public class ActionsView extends AbstractToolbarView {

  public static final String ID = ActionsView.class.getName(); //$NON-NLS-1$

  @Override
  protected IContributionItem[] getContributionItems() {
    return new IContributionItem[] {
        createToolItem(SWTResourceManager.getPluginImageDescriptor("gov.pnnl.velo.datasets", "icons/16x16/save_edit.png"), "Save", "org.eclipse.ui.file.save", SWT.PUSH),
        createToolItem(SWTResourceManager.getPluginImageDescriptor("gov.pnnl.velo.datasets", "icons/16x16/share_button.png"), "Publish Dataset", "gov.pnnl.velo.dataset.handler.PublishDataset", SWT.PUSH)
        
    };
  }

}
