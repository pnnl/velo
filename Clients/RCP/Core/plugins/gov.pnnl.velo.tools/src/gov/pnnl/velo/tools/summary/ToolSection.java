package gov.pnnl.velo.tools.summary;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.tools.behavior.ToolBehavior;
import gov.pnnl.velo.ui.views.SummaryView;
//import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;
import gov.pnnl.velo.util.VeloConstants;

public abstract class ToolSection implements SummaryViewSectionProvider, ToolBehavior {
  
  protected Tool tool;
  
  public abstract void createSection(SummaryView view, IResource resource);

  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.behavior.ToolBehavior#setTool(gov.pnnl.velo.tools.Tool)
   */
  @Override
  public void setTool(Tool tool) {
    this.tool = tool;    
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.views.SummaryViewSectionProvider#createSummarySection(gov.pnnl.velo.ui.views.SummaryView, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean createSummarySection(SummaryView view, IResource selectedResource) {
    // Only draw this section if selected resource has correct mimetype
    String mimetype = selectedResource.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
    boolean created = false;
    
    if(mimetype != null && mimetype.equals(tool.getMimetype())) {
      createSection(view, selectedResource);
      created = true;
    }
    return created;
  }
  

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(SummaryViewSectionProvider o) {
    return 0;
  }

}
