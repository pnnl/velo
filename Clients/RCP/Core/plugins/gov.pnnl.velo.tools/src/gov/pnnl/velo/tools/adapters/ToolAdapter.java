/**
 * 
 */
package gov.pnnl.velo.tools.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wb.swt.SWTResourceManager;
import org.springframework.beans.factory.InitializingBean;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.util.PluginUtils;
import gov.pnnl.cat.ui.images.ResourceImageFactory;
import gov.pnnl.cat.ui.rcp.handlers.CustomCompareBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomDeleteBehavior;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.ToolManager;
import gov.pnnl.velo.tools.behavior.NewToolInstanceBehavior;
import gov.pnnl.velo.tools.behavior.OpenToolInstanceBehavior;
import gov.pnnl.velo.tools.behavior.SaveToolInstanceBehavior;
import gov.pnnl.velo.tools.summary.ToolSection;
import gov.pnnl.velo.tools.ui.ToolUIFactory;
import gov.pnnl.velo.ui.commands.CustomExportBehavior;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;
import gov.pnnl.velo.util.VeloConstants;


/**
 * @author d3k339
 *
 */
public class ToolAdapter implements Tool, InitializingBean, ResourceImageFactory {
  protected String imageUrl = null; 
  protected ImageDescriptor imageDescriptor = null;
  protected Image image = null;
  protected String name = null;
  protected String tooltipText = null;
  protected String toolDescription = null;
  protected String mimetype = null;
  protected ToolUIFactory uiFactory = null;
  protected String newToolInstanceBehaviorClass = null;
  protected String openToolInstanceBehaviorClass = null;
  protected String saveToolInstanceBehaviorClass = null;
  protected String customDeleteBehaviorClass = null;
  protected String customCompareBehaviorClass = null;
  protected String customExportBehaviorClass = null;
  protected List<String> summaryViewSectionProviderClasses = null;
  
  protected ToolManager toolManager;

  @Override
  public void afterPropertiesSet() throws Exception {
    // register myself
    toolManager.registerTool(this);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getTooltipText() {
    return tooltipText;
  }

  @Override
  public String getToolDescription() {
    return toolDescription;
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    if(imageDescriptor == null) {
      File file = getImageFile();
      if(file != null) {
        imageDescriptor = SWTResourceManager.getImageDescriptor(file.getAbsolutePath());
      }
    }
    return imageDescriptor;
  }

  @Override
  public Image getImage() {
    if(image == null) {
      File file = getImageFile();
      if(file != null) {
        image = SWTResourceManager.getImage(file.getAbsolutePath());
      }
    }
    return image;
  }

  @Override
  public String getMimetype() {
    return mimetype;
  }

  public NewToolInstanceBehavior getNewToolInstanceBehavior() {
    NewToolInstanceBehavior behavior = (NewToolInstanceBehavior)instantiateClass(newToolInstanceBehaviorClass);
    if(behavior != null) {
      behavior.setTool(this);
    }
    return behavior;
  }
  
  @Override
  public OpenToolInstanceBehavior getOpenToolInstanceBehavior() {
    OpenToolInstanceBehavior behavior = (OpenToolInstanceBehavior)instantiateClass(openToolInstanceBehaviorClass);
    if(behavior != null) {
      behavior.setTool(this);
    }
    return behavior;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.Tool#getSaveToolInstanceBehavior()
   */
  @Override
  public SaveToolInstanceBehavior getSaveToolInstanceBehavior() {
    SaveToolInstanceBehavior behavior = (SaveToolInstanceBehavior)instantiateClass(saveToolInstanceBehaviorClass);
    if(behavior != null) {
      behavior.setTool(this);
    }
    return behavior;
  }

  @Override
  public List<SummaryViewSectionProvider> getSummaryViewSectionProviders() {
    List<SummaryViewSectionProvider> providers = new ArrayList<SummaryViewSectionProvider>();
    if(summaryViewSectionProviderClasses != null) {
      for(String providerClass : summaryViewSectionProviderClasses) {
        SummaryViewSectionProvider section = (SummaryViewSectionProvider)instantiateClass(providerClass);
        if(section != null) {
          if(section instanceof ToolSection) {
            ((ToolSection)section).setTool(this);
          }
          providers.add(section);
        }
      }
    }
    return providers;
  }

  @Override
  public CustomDeleteBehavior getCustomDeleteBehavior() {
    return (CustomDeleteBehavior)instantiateClass(customDeleteBehaviorClass);
  }

  @Override
  public CustomCompareBehavior getCustomCompareBehavior() {
    return (CustomCompareBehavior)instantiateClass(customCompareBehaviorClass);
  }

  @Override
  public CustomExportBehavior getCustomExportBehavior() {
    return (CustomExportBehavior)instantiateClass(customExportBehaviorClass);
  }

  @Override
  public ResourceImageFactory getResourceImageFactory() {
    return this;
  }
  
  public void setToolManager(ToolManager toolManager) {
    this.toolManager = toolManager;
  }

  /**
   * Plugin URL of the form:
   * platform:/plugin/org.eclipse.datatools.connectivity.sqm.core.ui/icons/server_explorer.gif
   * @param imageUrl
   */
  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setTooltipText(String tooltipText) {
    this.tooltipText = tooltipText;
  }

  public void setToolDescription(String toolDescription) {
    this.toolDescription = toolDescription;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.Tool#getUiFactory()
   */
  @Override
  public ToolUIFactory getUiFactory() {
    return uiFactory;
  }

  public void setUiFactory(ToolUIFactory uiFactory) {
    this.uiFactory = uiFactory;
  }

  public void setNewToolInstanceBehaviorClass(String newToolInstanceBehaviorClass) {
    this.newToolInstanceBehaviorClass = newToolInstanceBehaviorClass;
  }

  public void setOpenToolInstanceBehaviorClass(String openToolInstanceBehaviorClass) {
    this.openToolInstanceBehaviorClass = openToolInstanceBehaviorClass;
  }

  public void setSaveToolInstanceBehaviorClass(String saveToolInstanceBehaviorClass) {
    this.saveToolInstanceBehaviorClass = saveToolInstanceBehaviorClass;
  }

  public void setCustomDeleteBehaviorClass(String customDeleteBehaviorClass) {
    this.customDeleteBehaviorClass = customDeleteBehaviorClass;
  }

  public void setCustomCompareBehaviorClass(String customCompareBehaviorClass) {
    this.customCompareBehaviorClass = customCompareBehaviorClass;
  }

  public void setCustomExportBehaviorClass(String customExportBehaviorClass) {
    this.customExportBehaviorClass = customExportBehaviorClass;
  }

  public void setSummaryViewSectionProviderClasses(List<String> summaryViewSectionProviderClasses) {
    this.summaryViewSectionProviderClasses = summaryViewSectionProviderClasses;
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImage(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public Image getImage(IResource resource, int size) {
    if(isSupported(resource)) {
      return getImage();
    }
    return null;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.ui.images.ResourceImageFactory#getImageDescriptor(gov.pnnl.cat.core.resources.IResource, int)
   */
  @Override
  public ImageDescriptor getImageDescriptor(IResource resource, int size) {
    if(isSupported(resource)) {
      return getImageDescriptor();
    }
    return null;
  }
  
  private boolean isSupported(IResource resource) {
    boolean supported = false;
    try {
      String mimetype = resource.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
      if(mimetype != null) {
        supported = getMimetype().equals(mimetype);
      }
    } catch(Throwable e) {
      // ignore - this could happen when items are deleted and we don't have
      // a big notificatioin throttle on the server
    }
    return supported;
  }


  @SuppressWarnings("rawtypes")
  protected Class getClass(String className) {
    try {
      if(className != null) {
        return Class.forName(className);
      }
      return null;
      
    } catch(RuntimeException e) {
      throw e;
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }
  
  protected Object instantiateClass(String className) {
    try {
      if(className != null) {
        return Class.forName(className).newInstance();
      }
      return null;
      
    } catch(RuntimeException e) {
      throw e;
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }
  
  protected File getImageFile() {
    File file = null;
    if(imageUrl != null) {
      file = PluginUtils.getPluginFile(imageUrl);
    }
    return file;    
  }

}
