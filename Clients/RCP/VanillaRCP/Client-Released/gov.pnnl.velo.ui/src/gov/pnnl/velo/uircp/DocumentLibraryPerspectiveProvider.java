package gov.pnnl.velo.uircp;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.perspectives.DataBrowser;

public class DocumentLibraryPerspectiveProvider implements gov.pnnl.cat.ui.rcp.util.DocumentLibraryPerspectiveProvider {

  @Override
  public String getPerspectiveID(IResource resource) {
    return DataBrowser.ID;
  }

}
