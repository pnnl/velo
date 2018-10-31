/**
 * 
 */
package gov.pnnl.velo.tools.behavior.mgr;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.velo.ui.views.SummaryView;
import gov.pnnl.velo.ui.views.SummaryViewSectionProvider;

/**
 * @author d3k339
 *
 */
public class ToolMgrSummaryViewSectionProvider extends ToolMgrBehavior implements SummaryViewSectionProvider {

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(SummaryViewSectionProvider o) {
    return toolManager.compareTo(o);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.ui.views.SummaryViewSectionProvider#createSummarySection(gov.pnnl.velo.ui.views.SummaryView, gov.pnnl.cat.core.resources.IResource)
   */
  @Override
  public boolean createSummarySection(SummaryView view, IResource selectedResource) {
    return toolManager.createSummarySection(view, selectedResource);
  }

}
