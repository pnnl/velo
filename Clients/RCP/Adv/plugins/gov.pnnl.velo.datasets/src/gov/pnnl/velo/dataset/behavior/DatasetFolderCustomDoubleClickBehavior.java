package gov.pnnl.velo.dataset.behavior;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.ui.rcp.handlers.CustomDoubleClickBehavior;
import gov.pnnl.velo.dataset.util.DatasetUtil;

public class DatasetFolderCustomDoubleClickBehavior implements CustomDoubleClickBehavior {

  private IResourceManager mgr;

  public DatasetFolderCustomDoubleClickBehavior() {
    this.mgr = ResourcesPlugin.getDefault().getResourceManager();
  }

  @Override
  public boolean doubleClick(IResource source) throws RuntimeException {
    // TODO test if it has the dataset aspect, and if so open to edit it
    if (source instanceof IFolder && mgr.resourceExists(source.getPath().append("Metadata"))) {

      DatasetUtil.openDatasetInNewWindow((IFolder) source);
      return true;
    }

    return false;
  }

}
