package gov.pnnl.cat.ui.rcp.util;

import gov.pnnl.cat.core.resources.IResource;

/**
 * Determines which perspective to use when opening a file in an explorer view.
 * (Like when we open file location from the search results view.)
 * @author D3K339
 *
 */
public interface DocumentLibraryPerspectiveProvider {

  public String getPerspectiveID(IResource resource);
}
