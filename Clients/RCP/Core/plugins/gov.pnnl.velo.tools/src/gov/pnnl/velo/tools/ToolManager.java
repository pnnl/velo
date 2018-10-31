package gov.pnnl.velo.tools;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.ResourceImageFactory;
import gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior;
import gov.pnnl.velo.ui.commands.CustomExportBehavior;
import gov.pnnl.velo.ui.views.SummaryView;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

/**
 * The ToolManager manages all the tools registered in the Velo UI.
 * It makes sure they are contributed to the tool toolbar, menus, & popup menus,
 * their summary pages are contributed to the summary view, their custom
 * copy/paste/delete/double click/export behaviors are contributed to the explorers.
 * 
 * @author d3k339
 *
 */
public class ToolManager implements SummaryViewSectionProvider, CustomDoubleClickBehavior, CustomPasteBehavior, 
CustomDeleteBehavior, CustomCompareBehavior, CustomExportBehavior, CustomSaveAsBehavior, ResourceImageFactory {

  // Parameters for Tool extension point
  public static final String TOOL_UI_EXTENSION_POINT = "gov.pnnl.velo.tools.tool";
  public static final String CLASS_ATTRIBUTE = "class";

  private Logger logger = CatLogger.getLogger(ToolManager.class);
  private Map<String, Tool> tools = new HashMap<String, Tool>();
  
  public ToolManager() {
  }
  
  public void registerTool(Tool tool) {
    tools.put(tool.getName(), tool);
  }

  public Collection<Tool> getTools() {
    return tools.values();
  }
  
  public Tool getTool(String toolName) {
    return tools.get(toolName);
  }
      
  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(SummaryViewSectionProvider o) {
    // For now we don't care what order the sections are in
    return 0;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.views.SummaryViewSectionProvider#createSummarySection(gov.pnnl.velo.ui.views.SummaryView, gov.pnnl.cat.core.resources.IResource)
   */
  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.views.SummaryViewSectionProvider#createSummarySection(gov.pnnl.velo.ui.views.SummaryView, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean createSummarySection(SummaryView view, IResource selectedResource) {
    List<SummaryViewSectionProvider> p;
    for(Tool tool : getTools()) {
      p = tool.getSummaryViewSectionProviders();
      if(p != null) {
        // one tool could contribute multiple sections to the summary view
        for(SummaryViewSectionProvider provider : p) {
          provider.createSummarySection(view, selectedResource);   
        }
      }
    }
    return true;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.commands.CustomExportBehavior#export(org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public boolean export(IStructuredSelection selection) throws RuntimeException {
    boolean exported = false;
    CustomExportBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getCustomExportBehavior();
      if(b != null) {
        exported = exported || b.export(selection);
      }
    }
    return exported;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior#preprocessFiles(java.io.File, java.io.File)
   */
  @Override
  public boolean preprocessFiles(File file1, File file2) throws RuntimeException {
    boolean preprocessed = false;
    CustomCompareBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getCustomCompareBehavior();
      if(b != null) {
        preprocessed = preprocessed || b.preprocessFiles(file1, file2);
      }
    }
    return preprocessed;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior#delete(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean delete(IResource source) throws RuntimeException {
    boolean deleted = false;
    CustomDeleteBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getCustomDeleteBehavior();
      if(b != null) {
        deleted = deleted || b.delete(source);
      }
    }
    return deleted;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior#paste(gov.pnnl.cat.core.resources.IResource, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean paste(IResource source, IResource destinationParent, boolean isMove) throws RuntimeException {
    boolean pasted = false;
    CustomPasteBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getSaveToolInstanceBehavior();
      if(b != null) {
        pasted = pasted || b.paste(source, destinationParent, isMove);
      }
    }
    return pasted;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior#doubleClick(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean doubleClick(IResource source) throws RuntimeException {
    boolean doubleClicked = false;
    CustomDoubleClickBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getOpenToolInstanceBehavior();
      if(b != null) {
        doubleClicked = doubleClicked || b.doubleClick(source);
      }
    }
    return doubleClicked;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior#saveAs(gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean saveAs(IResource resource) throws RuntimeException {
    boolean saved = false;
    CustomSaveAsBehavior b;
    for(Tool tool : getTools()) {
      b = tool.getSaveToolInstanceBehavior();
      if(b != null) {
        saved = saved || b.saveAs(resource);
      }
    }
    return saved;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImage(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public Image getImage(IResource resource, int size) {
    Image image = null;
    ResourceImageFactory f;
    for(Tool tool : getTools()) {
      f = tool.getResourceImageFactory();
      if(f != null) {
        image = f.getImage(resource, size);
        if(image != null) {
          break;
        }
      }
    }
    return image;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImageDescriptor(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    ImageDescriptor image = null;
    ResourceImageFactory f;
    for(Tool tool : getTools()) {
      f = tool.getResourceImageFactory();
      if(f != null) {
        image = f.getImageDescriptor(resource, size);
        if(image != null) {
          break;
        }
      }
    }
    return image;
  }
}
