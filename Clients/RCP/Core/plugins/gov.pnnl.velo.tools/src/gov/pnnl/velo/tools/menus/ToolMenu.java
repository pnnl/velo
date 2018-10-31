package gov.pnnl.velo.tools.menus;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.ToolManager;
import gov.pnnl.velo.tools.ToolsPlugin;
import gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior;
import gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;
import gov.pnnl.velo.core.util.ToolErrorHandler;

/**
 * Will automatically populate applicable tools based on ToolManager's set of registered tools.
 * 
 * @author d3k339
 *
 */
public class ToolMenu extends CompoundContributionItem implements IWorkbenchContribution {
  private IServiceLocator serviceLocator;
  private List<IResource> selectedResources;


  // empty menu used if no tools match the selection
  private static IContributionItem[] EMPTY_MENU = new IContributionItem[] { new ContributionItem() {
    public void fill(Menu menu, int index) {
      MenuItem item = new MenuItem(menu, SWT.NONE);
      item.setEnabled(false);
      item.setText("None Apply");
    }

    public boolean isEnabled() {
      return false;
    }
  } };

  /**
   * Constructor
   */
  public ToolMenu() {

  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.menus.IWorkbenchContribution#initialize(org.eclipse.ui.services.IServiceLocator)
   */
  @Override
  public void initialize(IServiceLocator serviceLocator) {
    this.serviceLocator = serviceLocator;    
  }

  private List<IResource> getSelectedResources() {
    IWorkbenchPart part = null;
    ISelection selection = null;
    selectedResources = null;

    ISelectionService selectionService = (ISelectionService) serviceLocator.getService(ISelectionService.class);
    if (selectionService != null) {
      selection = selectionService.getSelection();
    }

    IPartService partService = (IPartService) serviceLocator.getService(IPartService.class);
    if (partService != null) {
      part = partService.getActivePart();
    }

    // If no part or selection, disable all.
    if (part != null && selection != null && !selection.isEmpty() && (selection instanceof IStructuredSelection)) {
      selectedResources = RCPUtil.getResources((IStructuredSelection) selection);
    }

    return selectedResources;
  }

  @Override
  protected IContributionItem[] getContributionItems() {
    getSelectedResources();
    ToolManager mgr = ToolsPlugin.getDefault().getToolManager();
    IContributionItem[] itemsArray = EMPTY_MENU;

    if(selectedResources != null && selectedResources.size() > 0) {

      // Create the menu contributions for tools that apply to this selection
      ArrayList<IContributionItem> items = new ArrayList<IContributionItem>();

      for(Tool tool : mgr.getTools()) {
        OpenToolInstanceBehavior oti = tool.getOpenToolInstanceBehavior();

        if(oti != null && oti.canOpen(selectedResources)) {
          // add this tool to the menu
          OpenToolAction openAction = new OpenToolAction(tool.getName(), tool.getTooltipText(), tool.getImageDescriptor());
          IContributionItem item = new ActionContributionItem(openAction);
          items.add(item);
        }
      }

      addNewToolMenu(items);

      if(items.size() > 0) {
        itemsArray = items.toArray(new IContributionItem[items.size()]);
      }
    }

    return itemsArray;
  }

  protected void addNewToolMenu(List<IContributionItem> items) {
    ToolManager mgr = ToolsPlugin.getDefault().getToolManager();
    MenuManager newMenu = new MenuManager("New", "new");

    for(Tool tool : mgr.getTools()) {
      NewToolInstanceBehavior nti = tool.getNewToolInstanceBehavior();

      if(nti != null && nti.canCreateNewInstance(selectedResources)) {
        // add this tool to the menu
        NewToolAction newItem = new NewToolAction(tool.getName(), tool.getTooltipText(), tool.getImageDescriptor());
        IContributionItem item = new ActionContributionItem(newItem);
        newMenu.add(item);
      }
    }
    
    if(newMenu.getSize() > 0) {
      items.add(newMenu);      
    }

  }

  public class OpenToolAction extends Action {
    private String toolName;
    private String tooltipText;


    public OpenToolAction(String toolName, String tooltipText, ImageDescriptor imageDesc) {
      this.toolName = toolName;
      this.tooltipText = tooltipText;
      this.setImageDescriptor(imageDesc);

    }

    @Override
    public String getText() {
      return toolName;
    }

    @Override
    public String getToolTipText() {
      return tooltipText;
    }

    @Override
    public void run() {

      Tool tool = ToolsPlugin.getDefault().getToolManager().getTool(toolName);
      OpenToolInstanceBehavior oti = tool.getOpenToolInstanceBehavior();

      if(oti != null) {
        oti.open(selectedResources, null);
      }
    }

  }

  public class NewToolAction extends Action {
    private String toolName;
    private String tooltipText;


    public NewToolAction(String toolName, String tooltipText, ImageDescriptor imageDesc) {
      this.toolName = toolName;
      this.tooltipText = "Create" + tooltipText;
      this.setImageDescriptor(imageDesc);

    }

    @Override
    public String getText() {
      return toolName;
    }

    @Override
    public String getToolTipText() {
      return tooltipText;
    }

    @Override
    public void run() {
      try{
        Tool tool = ToolsPlugin.getDefault().getToolManager().getTool(toolName);
        NewToolInstanceBehavior nti = tool.getNewToolInstanceBehavior();
        OpenToolInstanceBehavior oti = tool.getOpenToolInstanceBehavior();
        if(nti != null) {
          IFolder toolFolder = nti.createNewInstance(selectedResources);
          List<IResource> newToolSelection = new ArrayList<IResource>();
          newToolSelection.add(toolFolder);
          if(oti != null) {
            oti.open(newToolSelection, null);
          }
        }
      }catch(Throwable e){
        ToolErrorHandler.handleError("An unexpected error occurred! See the client log for details.", e, true);
      }
    }

  }

}

