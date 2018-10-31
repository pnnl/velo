package gov.pnnl.cat.ui.rcp.util;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.CatPerspectiveIDs;

public class DefaultDocumentLibraryPerspectiveProvider implements DocumentLibraryPerspectiveProvider {

  @Override
  public String getPerspectiveID(IResource resource) {
    return CatPerspectiveIDs.ADMIN_DATA_BROWSER;
  }

}
