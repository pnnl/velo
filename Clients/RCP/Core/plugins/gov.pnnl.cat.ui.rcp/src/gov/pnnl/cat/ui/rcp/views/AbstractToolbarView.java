package gov.pnnl.cat.ui.rcp.views;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.part.ViewPart;

/**
 * View that represents a toolbar of commands that can be put anywhere.  Note that
 * I had trouble getting ToolbarManager with command contribution items to display,
 * so we have to go with a ToolBar with action delegates in it for now.
 * @author D3K339
 *
 */
public abstract class AbstractToolbarView extends ViewPart {

  public AbstractToolbarView() {
  }

  /**
   * Create contents of the view part.
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {
    Composite wrapper = new Composite(parent, SWT.NONE);
    final GridLayout gridLayout = new GridLayout(1, true);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    wrapper.setLayout(gridLayout);
    wrapper.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    createToolbar(wrapper);
  }

  protected abstract IContributionItem[] getContributionItems();

  
  private void createToolbar(Composite parent) {
    ToolBarManager toolbarManager = new ToolBarManager(SWT.FLAT | SWT.VERTICAL);
    ToolBar toolbar = toolbarManager.createControl(parent);
    toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
    
    for(IContributionItem item : getContributionItems()) {
      toolbarManager.add(item);
    }
    
    toolbar.pack();
    toolbarManager.update(true);
    
  }
  
  protected IContributionItem createToolItem(ImageDescriptor imageDesc, String tooltip, final String commandId, int buttonStyle) {
    
    CommandContributionItemParameter param = new CommandContributionItemParameter(getSite(), 
        commandId, commandId, 
        null, imageDesc,
        null, null, null, null, tooltip, buttonStyle, null, false);
    
    CommandContributionItem item = new CommandContributionItem(param);
    
    return item;
  }

  @Override
  public void setFocus() {
    // Set the focus
  }

}
