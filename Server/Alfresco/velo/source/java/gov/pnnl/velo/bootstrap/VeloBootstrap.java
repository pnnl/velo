/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.velo.bootstrap;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.JobUtils;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO9075;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.io.Resource;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * Bootstrap Velo node structure.  We do this here so we can check to see
 * if a file exists before trying to create it.  So if the server already
 * has these folders, the bootstrap won't crash.
 * @author d3k339
 *
 * @version $Revision: 1.0 $
 */
public class VeloBootstrap extends AbstractLifecycleBean implements InitializingBean {
  
  private static Log logger = LogFactory.getLog(VeloBootstrap.class);
  public static final String XPATH_HOME_FOLDER_TEMPLATE = "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("User Home Folder");  
  public static final String XPATH_TEAM_FOLDER_TEMPLATE = "/app:company_home/app:dictionary/app:space_templates/cm:" + ISO9075.encode("Team Home Folder");  

  protected NodeUtils nodeUtils;
  protected TransactionService transactionService;
  protected NodeService nodeService;
  protected PermissionService permissionService;
  protected Resource userManualResourcesDir;
  protected ContentService contentService;
  protected MimetypeService mimetypeService;

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.admin.patch.AbstractPatch#applyInternal()
   */
  /**
   * Method onBootstrap.
   * @param event ApplicationEvent
   */
  @Override
  protected void onBootstrap(ApplicationEvent event) {
    try {
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
        public Object execute() throws Exception {
          //set cacheSession to false so ssh sessions created on the server are NOT cached (instead we cache in memory usernames/passwords for machine and create new sessions). 
          JobUtils.CACHE_SSH_SESSION = false;
          
          logger.info("Starting Velo Bootstrap");
          AuthenticationUtil.setRunAsUserSystem();
          NodeRef spaceTemplates = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_SPACE_TEMPLATES);
          
          // bootstrap user home folder space template
          NodeRef userHomeFolder = nodeUtils.getNodeByXPath(XPATH_HOME_FOLDER_TEMPLATE);
          if(userHomeFolder == null) {
            userHomeFolder = nodeUtils.createFolder(spaceTemplates, "User Home Folder");
            nodeService.setProperty(userHomeFolder, VeloServerConstants.PROP_MIMEYPE, VeloConstants.MIMETYPE_USERS);
            permissionService.setPermission(userHomeFolder, PermissionService.OWNER_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
            permissionService.setInheritParentPermissions(userHomeFolder, false);
            nodeService.addAspect(userHomeFolder, CatConstants.ASPECT_USER_HOME_FOLDER, null);
          }
          
          // bootstrap team home folder space template
          NodeRef teamHomeFolder = nodeUtils.getNodeByXPath(XPATH_TEAM_FOLDER_TEMPLATE);
          if(teamHomeFolder == null) {
            teamHomeFolder = nodeUtils.createFolder(spaceTemplates, "Team Home Folder");
            permissionService.setPermission(teamHomeFolder, PermissionService.OWNER_AUTHORITY, PermissionService.ALL_PERMISSIONS, true);
            permissionService.setInheritParentPermissions(teamHomeFolder, false);
            nodeService.addAspect(teamHomeFolder, CatConstants.ASPECT_TEAM_HOME_FOLDER, null);
          }          
          
          // bootstrap Velo folder under Company Home
          NodeRef companyHome = nodeUtils.getNodeByXPath(CatConstants.XPATH_COMPANY_HOME);
          NodeRef veloFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_VELO);
          if(veloFolder == null) {
            veloFolder = nodeUtils.createFolder(companyHome, "Velo");
            permissionService.setPermission(veloFolder, PermissionService.ALL_AUTHORITIES, PermissionService.CONSUMER, true);
            permissionService.setInheritParentPermissions(veloFolder, false);
          }

          NodeRef registryFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_REGISTRY);
          if(registryFolder == null) {
            registryFolder = nodeUtils.createFolder(veloFolder, "Registry");
            permissionService.setInheritParentPermissions(registryFolder, true);
          }

          NodeRef machinesFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_REGISTRY_MACHINES);
          if(machinesFolder == null) {
            machinesFolder = nodeUtils.createFolder(registryFolder, "Machines");
            permissionService.setInheritParentPermissions(machinesFolder, true);
          }
          
          NodeRef codesFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_REGISTRY_CODES);
          if(codesFolder == null) {
            codesFolder = nodeUtils.createFolder(registryFolder, "Codes");
            permissionService.setInheritParentPermissions(codesFolder, true);
          }

          NodeRef scriptsFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_REGISTRY_SCRIPTS);
          if(scriptsFolder == null) {
            scriptsFolder = nodeUtils.createFolder(registryFolder, "JobScripts");
            permissionService.setInheritParentPermissions(scriptsFolder, true);
          }

          // Ref Data
          NodeRef refDataFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_REFDATA);
          if(refDataFolder == null){
            // Create the node properties
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
            properties.put(ContentModel.PROP_NAME, "refdata");
            properties.put(VeloServerConstants.PROP_MIMEYPE, "cmsfile/refdata");
            refDataFolder = NodeUtils.createFolder(veloFolder, "refdata", properties,nodeService);
            permissionService.setPermission(refDataFolder, PermissionService.ALL_AUTHORITIES, "Collaborator", true);
            permissionService.setInheritParentPermissions(refDataFolder, false);
          }

          NodeRef userManualFolder = nodeUtils.getNodeByXPath(VeloServerConstants.XPATH_USERMANUAL);
          if(userManualFolder == null){
            userManualFolder = nodeUtils.createFolder(refDataFolder, "UserManualResources");
            permissionService.setPermission(userManualFolder, PermissionService.ALL_AUTHORITIES, "Collaborator", true);
            permissionService.setInheritParentPermissions(userManualFolder, true);
          }
          if(WikiUtils.getWikiHome()!=null){
            //Now upload the images in usermanualresources folder
            if(userManualResourcesDir.getFile().exists() && userManualResourcesDir.getFile().isDirectory()) {
              populateFiles(userManualResourcesDir.getFile(),userManualFolder,"UserManualResources" );
            }
          }

          return null;
        }

        private void populateFiles(File localDir, NodeRef userManualFolder, String folderName) {
          for (File file : localDir.listFiles()) {

            logger.debug("adding registry file: " + file.getName());
            NodeRef nodeRef = nodeService.getChildByName(userManualFolder,
                ContentModel.ASSOC_CONTAINS, file.getName());

            if (nodeRef == null) {
              NodeUtils.createFile(userManualFolder, file.getName(), file, nodeService,
                  contentService, mimetypeService);

            } else {
              try {
                NodeUtils.updateFileContents(nodeRef, new FileInputStream(file), nodeService, contentService);
              } catch (FileNotFoundException e) {
                log.error("Unable to update newer version of the file :" + file.getName() + "into " + folderName + e);
              }
            }

          }
          
        }

      };

      transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, false);
      logger.info("Completed Velo Bootstrap");

    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Velo Bootstrap failed.", e);
    }
  }
  
  /**
   * Make sure folder doesn't already exist before trying to create it
   * @param name
   * @param mimetype
   * @param parent
  
   * @param inheritPermissions boolean
   * @return NodeRef
   */
  protected NodeRef createFolder(String name, String mimetype, NodeRef parent, boolean inheritPermissions) {
    
    NodeRef newFolder = nodeService.getChildByName(parent, ContentModel.ASSOC_CONTAINS, name);
    if(newFolder == null) {  
      
      // Create the node properties
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
      properties.put(ContentModel.PROP_NAME, name);
      properties.put(VeloServerConstants.PROP_MIMEYPE, mimetype);

      // Create the folder
      QName folderQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name);
      newFolder = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, folderQName, ContentModel.TYPE_FOLDER, properties).getChildRef();
            
      permissionService.setInheritParentPermissions(newFolder, inheritPermissions);
    }
    
    return newFolder;
  }

  
  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method onShutdown.
   * @param arg0 ApplicationEvent
   */
  @Override
  protected void onShutdown(ApplicationEvent arg0) {
    // TODO Auto-generated method stub

  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {    
  }
  
  /**
   * Method setPermissionService.
   * @param permissionService PermissionService
   */
  public void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  public void setUserManualResourcesDir(Resource userManualResourcesDir) {
    this.userManualResourcesDir = userManualResourcesDir;
  }

  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }
}
