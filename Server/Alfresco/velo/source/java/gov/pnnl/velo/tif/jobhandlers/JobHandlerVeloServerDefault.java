package gov.pnnl.velo.tif.jobhandlers;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tif.model.Fileset;
import gov.pnnl.velo.tif.model.JobConfig;
import gov.pnnl.velo.tif.model.Machine;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.util.JobUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kepler.job.JobException;

public class JobHandlerVeloServerDefault extends JobHandlerVeloDefault {

  protected NodeService nodeService;
  protected ContentService contentService;
 // protected ExporterService exporterService;

  protected static final Log logger = LogFactory
      .getLog(JobHandlerVeloServerDefault.class.getName());

  @Override
  public void init() {
    // TODO Auto-generated method stub

  }

  @Override
  public File prepareLocalWorkingDirectory() throws Exception {
    File localWorkingDir;

    // TODO - should some part of this code be moved to a
    // VeloWorkSpaceServer class ?

    if (JobUtils.isLocalhost(machine)) {
      logger.debug("Job is running on localhost. Make localWorkingDir same as RemoteDir");
      localWorkingDir = new File(jobConfig.getRemoteDir());
      boolean returnval =localWorkingDir.mkdirs();
      logger.debug("mkdir result =" + returnval);
    } else {
      // 1. create temporary dir for collecting files this job.. is a long
      // life (24 hours) good enough? do we even need that?
      logger.debug("remoteuser - " + jobConfig.getUserName());
      logger.debug("tool code id - " + jobConfig.getCode().getIdAndVersion());
      String filenamekey = jobConfig.getUserName() + "_"
          + jobConfig.getCode().getIdAndVersion()  + "_" + System.currentTimeMillis();
      
      localWorkingDir = TempFileProvider.getLongLifeTempDir(filenamekey);
    }
    // Default handler doesn't do anything else.
    // Overriding classes can use this space to do any local preprocessing
    // or add any constant files etc.
    logger.debug("Setting local working dir to: "
        + localWorkingDir.getAbsolutePath());
    setlocalWorkingDir(localWorkingDir);
    jobConfig.setLocalWorkingDir(localWorkingDir);
    return localWorkingDir;
  }

 
  @Override
  public void stageVeloServerInputs(List<Fileset> veloServerInputs, List<Fileset> jobLocalInputs) throws JobException {
    try {
      Machine machine = jobConfig.getMachine();
      boolean remoteJob = !JobUtils.isLocalhost(machine); // check if job
      // machine ==
      // velo server
      StringBuilder renameScript = new StringBuilder("#!/bin/sh\n");
      renameScript.append("cd ");
      renameScript.append(jobConfig.getRemoteDir());
      renameScript.append("\n");
      List<File> filesToRemoteCopy = new ArrayList<File>();
      boolean foundMatches = false;
      // <fileset dir="." includes="*" excludes=".*.xml,.akuna_inputs"/>
      if(veloServerInputs !=null) {
      for (Fileset fs : veloServerInputs) {

        // 1. First find the directory
        String dir = fs.getDirPath(jobConfig.getContextPath());

        // 2. Now get all the children of that directory
        NodeRef inputFolder = NodeUtils.getNodeByName(dir, nodeService);
        List<ChildAssociationRef> children = nodeService
            .getChildAssocs(inputFolder);
        for (ChildAssociationRef child : children) {

          NodeRef childNode = child.getChildRef();
          if (nodeService.getType(childNode).equals(ContentModel.TYPE_LINK)) {
            childNode = (NodeRef) nodeService.getProperty(childNode, ContentModel.PROP_LINK_DESTINATION);
          }

          String childName = (String) nodeService.getProperty(childNode,
              ContentModel.PROP_NAME);
          // 3. Check if it is a file to be excluded
          // Check exclude pattern first as that might most often
          // contains specific names,
          // or at least lesser pattern to match
          boolean include = true;
          for (int i = 0; i < fs.getExcludesList().size() && include; i++) {
            if (childName.matches(fs.getExcludesList().get(i))) {
              include = false;
            }
          }
          if (!include) {
            continue; // skip to the next child
          }
          // 4. If not check if it conforms to the include pattern
          for (String includePattern : fs.getIncludesList()) {
            if(includePattern.equals("*")){
              includePattern = ".*"; //else you would get an exception when calling matches()
            }
            if (childName.matches(includePattern)
                && nodeService.getType(childNode).equals(
                    ContentModel.TYPE_CONTENT)) {
              
              File namedFile = new File(getLocalWorkingDir(), childName);
              FileContentReader reader = (FileContentReader) contentService
                  .getReader(childNode, ContentModel.PROP_CONTENT);
              
              File file = reader.getFile();
              foundMatches = true;
              if(remoteJob){
                filesToRemoteCopy.add(file);
              }else{
                //if job is running on the Velo server then just copy the file to localWorkingDir
                //which is equal to remoteDir
                FileUtils.copyFile(file, new File(jobConfig.getRemoteDir()));
              }
              //add line to rename script
              renameScript.append("mv ");
              renameScript.append(file.getName());
              renameScript.append(" \"");
              renameScript.append(childName);
              renameScript.append("\"\n");
             }
//            else if (childName.matches(includePattern)
//                && nodeService.getType(childNode).equals(
//                    ContentModel.TYPE_FOLDER)) {
//               
////               if(remoteJob){
////                 //filesToRemoteCopy.add(  );
////               }else{
////                //local copy to jobConfig.remoteDir()
////               }
//            }
          }// end of loop through includes list for this fs
        }// end of looping through this filset dir's children
      }// completed processing all fileset elements
     }
      //Process other custom input files that might have got added directly to
      //jobConfig object. i.e file not configured through code registry xml
     
      if(stageDynamicInputFiles(renameScript, filesToRemoteCopy,remoteJob))
        foundMatches = true;
     
     
      if(foundMatches){
        File scriptFile = createRenameScript(renameScript);
        if(remoteJob){
          filesToRemoteCopy.add(scriptFile);
          execObject.copyTo(filesToRemoteCopy, jobConfig.getRemoteDir(), true);
          JobUtils.executeScript(execObject, jobConfig.getRemoteDir() + "/" + "rename.sh");
        }else{
          //file already copied to remoteDir
          JobUtils.executeScript(execObject, scriptFile.getAbsolutePath() + "/" + scriptFile.getName());
        }
      }
      
    } catch (Exception e) {
      throw new JobException("Exception staging velo server inputs: "
          + e.getMessage(), e);
    }
  }
  

  @Override
  public String getSubmitCommandOptions(JobConfig launchConfig){
	  return "";
  }
  
  
/**
 * stages inputs files directly added to jobConfig object. This happens after staging files
 * configured in code registry xml. 
 * @param renameScript
 * @param filesToRemoteCopy
 * @param remoteJob true if velo server is different from compute server
 * @return true if one or more files were staged successfully. false otherwise
 * @throws Exception
 */
 private boolean stageDynamicInputFiles(StringBuilder renameScript,
      List<File> filesToRemoteCopy, boolean remoteJob) throws Exception {
    
   List<String> jobInputFiles = jobConfig.getDynamicInputFiles();
   if(jobInputFiles == null || jobInputFiles.size()==0){
     return false;
   }
   boolean found = false;
   for(String path : jobInputFiles) {
     if(path != null && !path.trim().isEmpty()) {
       CmsPath filePath = new CmsPath(path);
       NodeRef nodeRef = NodeUtils.getNodeByName(filePath.toAssociationNamePath(), nodeService);
        if (processInputFile(nodeRef, renameScript, filesToRemoteCopy, remoteJob)){
         found = true;
       }
     }
   }
   return found;
 }
 
 private File createRenameScript(StringBuilder renameScript) throws Exception {
   File scriptFile = null;
   
   if(!renameScript.toString().isEmpty()) {
     scriptFile = new File(jobConfig.getLocalWorkingDir(), "rename.sh");
     
     FileOutputStream outputStream= null;
     try {
       outputStream = new FileOutputStream(scriptFile);
       org.apache.commons.io.IOUtils.write(renameScript.toString(), outputStream);

     } finally {
       if(outputStream != null) {
         org.apache.commons.io.IOUtils.closeQuietly(outputStream);
       }      
     }      
   }
   return scriptFile;
 }
 
 private boolean processInputFile(NodeRef fileRef, StringBuilder renameScript, List<File> filesToRemoteCopy, boolean remoteJob) throws Exception {
   // If the child is a file (i.e., content), then get the FileContentReader and stage that File
   if(nodeService.getType(fileRef).equals(ContentModel.TYPE_CONTENT)) {
     FileContentReader reader = (FileContentReader)contentService.getReader(fileRef, ContentModel.PROP_CONTENT);
     File alfrescoFile = reader.getFile();
     if(remoteJob)
       filesToRemoteCopy.add(alfrescoFile);
     else{
       FileUtils.copyFile(alfrescoFile, new File(jobConfig.getRemoteDir()));
     }
     String fileName = (String)nodeService.getProperty(fileRef, ContentModel.PROP_NAME);
     renameScript.append("mv " + alfrescoFile.getName() + " \"" + fileName + "\";\n");
     return true;
   }
   return false;
 }
 

//  public void stageVeloServerInputs(List<Fileset> veloServerInputs,
//      List<Fileset> jobLocalInputs) throws JobException {
//
//    // TODO In future may be we can use the accessibleFromVeloServer
//    // param of machine config and if it is set to true try to transfer
//    // files directly from velo server to remote job server
//
//    // For now downloading files to local file system and adding
//    // to localInputs list.
//    // JobManager always calls stageVeloServerInputs before stageLocalInputs
//    try {
//      Machine machine = TifServiceLocator.getMachineRegistry().get(
//          jobConfig.getMachineId());
//      boolean remoteJob = !JobUtils.isLocalhost(machine); // check if job
//      // machine ==
//      // velo server
//      StringBuilder linksScript = new StringBuilder();
//      String firstLine = "#!/bin/sh\n";
//
//      // <fileset dir="." includes="*" excludes=".*.xml,.akuna_inputs"/>
//      for (Fileset fs : veloServerInputs) {
//
//        // 1. First find the directory
//        String dir = fs.getDirPath(jobConfig.getContextPath());
//
//        // 2. Now get all the children of that directory
//        NodeRef inputFolder = NodeUtils.getNodeByName(dir, nodeService);
//        List<ChildAssociationRef> children = nodeService
//            .getChildAssocs(inputFolder);
//        for (ChildAssociationRef child : children) {
//
//          NodeRef childNode = child.getChildRef();
//          String childName = (String) nodeService.getProperty(childNode,
//              ContentModel.PROP_NAME);
//          // 3. Check if it is a file to be excluded
//          // Check exclude pattern first as that might most often
//          // contains specific names,
//          // or at least lesser pattern to match
//          boolean include = true;
//          for (int i = 0; i < fs.getExcludesList().size() && include; i++) {
//            if (childName.matches(fs.getExcludesList().get(i))) {
//              include = false;
//            }
//          }
//          if (!include) {
//            continue; // skip to the next child
//          }
//          // 4. If not check if it conforms to the include pattern
//          for (String includePattern : fs.getIncludesList()) {
//            if (childName.matches(includePattern)
//                && nodeService.getType(childNode).equals(
//                    ContentModel.TYPE_CONTENT)) {
//              File namedFile = new File(getLocalWorkingDir(), childName);
//              FileContentReader reader = (FileContentReader) contentService
//                  .getReader(childNode, ContentModel.PROP_CONTENT);
//
//              if (SystemUtils.IS_OS_WINDOWS) {
//                FileUtils.copyFile(reader.getFile(), namedFile);
//
//              } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_SOLARIS
//                  || SystemUtils.IS_OS_UNIX) {
//
//                // Use symbolic links
//                String sourceFile = reader.getFile().getAbsolutePath();
//                String linkFile = namedFile.getAbsolutePath();
//
//                // Make sure to dereference links in the source
//                // ln -s $(readlink -f
//                // alf_data/contentstore/2013/4/4/16/41/4fdd836b-82da-4ea5-8a85-d8e7daf9049d.bin)
//                linksScript.append("ln -s $(readlink -f " + sourceFile + ") \""
//                    + linkFile + "\";\n");
//                logger.debug("Command used to lync local file - "
//                    + "ln -s $(readlink -f " + sourceFile + ") \"" + linkFile
//                    + "\";\n");
//              }
//
//            } else if (childName.matches(includePattern)
//                && nodeService.getType(childNode).equals(
//                    ContentModel.TYPE_FOLDER)) {
//              long exportStart = System.currentTimeMillis();
//              export(childNode, getLocalWorkingDir(), linksScript);
//              long exportEnd = System.currentTimeMillis();
//              logger.info("Time to export folder " + childName + " = "
//                  + (exportEnd - exportStart) / 1000 + " seconds");
//
//            } else {
//              include = false;
//            }
//            if (include & remoteJob) {
//              // Add to localInputs list so that it eventually get
//              // staged to remote machine
//              // when stageLocalInputs() is called
//              // if it is a valid file but if it is not a
//              // remoteJob then
//              // file has already been copied into right job
//              // directory (see prepareLocalWorkingDir)
//              // so nothing more to be done
//              Fileset f = new Fileset();
//              f.setDir(getLocalWorkingDir().getAbsolutePath());
//              f.setIncludes(childName);
//              jobLocalInputs.add(f);
//            }
//          }// end of loop through includes list for this fs
//        }// end of looping through this filset dir's children
//      }// completed processing all fileset elements
//
//      // Execute the script if necessary to actually create all the
//      // symbolic links
//      if (!linksScript.toString().isEmpty()) {
//        linksScript.insert(0, firstLine);
//        File tempScriptFile = TempFileProvider.createTempFile("linkScript",
//            ".sh");
//        File runDir = TempFileProvider.getTempDir();
//        tempScriptFile.deleteOnExit();
//        FileOutputStream outputStream = null;
//        try {
//          outputStream = new FileOutputStream(tempScriptFile);
//          org.apache.commons.io.IOUtils.write(linksScript.toString(),
//              outputStream);
//
//        } finally {
//          if (outputStream != null) {
//            org.apache.commons.io.IOUtils.closeQuietly(outputStream);
//          }
//        }
//
//        String chmodCmd[] = { "chmod", "777", tempScriptFile.getAbsolutePath() };
//        // TODO:Should probably move this out of WikiUtils and rename it
//        // to execLocalCommand. It has nothing specific to wiki
//        WikiUtils.execCommand(chmodCmd, runDir);
//        String scriptCmd[] = { tempScriptFile.getAbsolutePath() };
//        long startScript = System.currentTimeMillis();
//        WikiUtils.execCommand(scriptCmd, runDir);
//        long endScript = System.currentTimeMillis();
//        logger.info("Time to execute link script: " + (endScript - startScript)
//            / 1000 + " seconds");
//
//      }
//
//    } catch (Exception e) {
//      throw new JobException("Exception staging velo server inputs: "
//          + e.getMessage());
//    }
//
//  }
//
//  /**
//   * Method export.
//   * 
//   * @param exportNode
//   *          NodeRef
//   * @param destination
//   *          File
//   * @param linkScript
//   *          StringBuilder
//   */
//  private void export(NodeRef exportNode, File destination,
//      StringBuilder linkScript) {
//
//    ExporterCrawlerParameters params = new ExporterCrawlerParameters();
//    params.setCrawlChildNodes(true);
//    params.setCrawlSelf(true);
//    params.setCrawlAssociations(true);
//    params.setCrawlContent(true);
//
//    params.setExportFrom(new Location(exportNode));
//
//    // perform the actual export
//    this.exporterService.exportView(new LocalFileSystemExporter(nodeService,
//        contentService, destination, linkScript), params, null);
//  }
  
 
  
 

  // //setters and getters
  public NodeService getNodeService() {

    return nodeService;
  }

  public void setNodeService(NodeService nodeService) {

    this.nodeService = nodeService;
  }

  public ContentService getContentService() {

    return contentService;
  }

  public void setContentService(ContentService contentService) {

    this.contentService = contentService;
  }

//  public ExporterService getExporterService() {
//
//    return exporterService;
//  }
//
//  public void setExporterService(ExporterService exporterService) {
//
//    this.exporterService = exporterService;
//  }

}
