package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.tif.service.RegistryConfigFileProvider;
import gov.pnnl.velo.util.VeloServerConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;

public class RegistryConfigFileProviderAlfresco implements RegistryConfigFileProvider {
  
  protected NodeUtils nodeUtils;
  protected NodeService nodeService;
  protected ContentService contentService;
  private static Logger logger = Logger.getLogger(RegistryConfigFileProviderAlfresco.class);

  public RegistryConfigFileProviderAlfresco(NodeUtils nodeUtils, NodeService nodeService, ContentService contentService) {
    super();
    this.nodeUtils = nodeUtils;
    this.nodeService = nodeService;
    this.contentService = contentService;
  }

  @Override
  public File getExecutablesDir() {
    return null;
  }

  @Override
  public List<File> getCodeConfigFiles() {
    return getConfigFiles(VeloServerConstants.XPATH_REGISTRY_CODES);
  }

  @Override
  public List<File> getMachineConfigFiles() {
    return getConfigFiles(VeloServerConstants.XPATH_REGISTRY_MACHINES);
  }

  @Override
  public List<File> getScriptFiles() {
    logger.debug("getting script files from server");
    return getConfigFiles(VeloServerConstants.XPATH_REGISTRY_SCRIPTS);
  }
  
  private List<File> getConfigFiles(String registryFolderXPath) {
    List<File> configFiles = new ArrayList<File>();

    NodeRef registryFolder = nodeUtils.getNodeByXPath(registryFolderXPath);
    if(registryFolder != null) {

      for(ChildAssociationRef child : nodeService.getChildAssocs(registryFolder)) {
        NodeRef nodeRef = child.getChildRef();
        String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        if(name.toLowerCase().endsWith(".xml")) {
          logger.debug("adding file:" + name);
          FileContentReader reader = (FileContentReader)contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
          configFiles.add(reader.getFile());
        }
      }
    }

    return configFiles;
  }
}


