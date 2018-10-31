package gov.pnnl.velo.tools;

import gov.pnnl.cat.ui.images.ResourceImageFactory;
import gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior;
import gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior;
import gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior;
import gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior;
import gov.pnnl.velo.tools.ui.ToolUIFactory;
import gov.pnnl.velo.ui.commands.CustomExportBehavior;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Interface for a tool that will be hooked into the Velo UI.
 * In order to hook your tool into Velo, you need to extend the 
 * gov.pnnl.velo.tools.toolProvider extension point and provide
 * your implementation of this interface.
 * 
 * If you want to be able to edit and save your tool, then extend the 
 * EditableTool interface.
 * 
 * @author d3k339
 *
 */
public interface Tool {
    
  /**
   * Get the short name for this tool to be shown in menus and lists
   * @return
   */
  public String getName();
  
  /**
   * Tooltip text when opening the tool.
   * @return
   */
  public String getTooltipText();
  
  /**
   * Long description for text balloons
   * @return
   */
  public String getToolDescription();
  
  /**
   * Provide the image descriptor for your icon
   * @return
   */
  public ImageDescriptor getImageDescriptor();
  
  /**
   * The image for your icon
   * @return
   */
  public Image getImage();

  // TODO: tool should include help
  
  /**
   * Get the mimetype for the tool's working dir.
   * @return
   */
  public String getMimetype();
  

  /**
   * Class used to instantiate the user interface.
   * Should be injected via bean configuration.
   * @return
   */
  public ToolUIFactory getUiFactory();
  
		
	// Behaviors to hook into the Velo UI
	public NewToolInstanceBehavior getNewToolInstanceBehavior();
	public OpenToolInstanceBehavior getOpenToolInstanceBehavior();
	public SaveToolInstanceBehavior getSaveToolInstanceBehavior();
	
	/**
	 * Return a list since a tool could provide multiple
	 * @return
	 */
	public List<SummaryViewSectionProvider> getSummaryViewSectionProviders();
	public CustomDeleteBehavior getCustomDeleteBehavior();
	public CustomCompareBehavior getCustomCompareBehavior();
	public CustomExportBehavior getCustomExportBehavior();
	public ResourceImageFactory getResourceImageFactory();

}
