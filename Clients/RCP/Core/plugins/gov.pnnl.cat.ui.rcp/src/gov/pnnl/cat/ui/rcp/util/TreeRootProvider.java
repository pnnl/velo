package gov.pnnl.cat.ui.rcp.util;

import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.RepositoryContainer;

/**
 * So deployments can override the default tree root
 * that is shown in all RCP dialogs when browsing the repository
 * @author D3K339
 *
 */
public interface TreeRootProvider {
  public  RepositoryContainer getVeloTreeRoot();
}
