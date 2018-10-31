/**
 * 
 */
package gov.pnnl.cat.ui.rcp.views.databrowser;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.wb.swt.SWTResourceManager;

import gov.pnnl.cat.ui.rcp.views.AbstractToolbarView;

/**
 * @author D3K339
 *
 */
public class ActionsView extends AbstractToolbarView {
  public static final String ID = ActionsView.class.getName(); //$NON-NLS-1$


  @Override
  protected IContributionItem[] getContributionItems() {
    return new IContributionItem[] {
        createToolItem(SWTResourceManager.getPluginImageDescriptor("gov.pnnl.cat.ui", "icons/16x16/bug_green.png"), "Toggle debug logging", "gov.pnnl.cat.logging.commands.ToggleLogging", SWT.CHECK)
    };
  }
  
  

}
