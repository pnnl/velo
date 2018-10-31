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
package gov.pnnl.velo.webscripts;

import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.ZipUtils;
import gov.pnnl.velo.policy.FileNamePolicy;
import gov.pnnl.velo.util.VeloServerConstants;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.FileUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * Called by wiki to upload one or more files. Parameter sourcePath points to a folder path on the local filesystem where the files to be uploaded are located. destPath points to the folder path in Alfresco where the files will be uploaded to. Folders found under the sourcePath folder will be uploaded recursively. If zip files are found under sourcePath, they will automatically be unzipped before being uploaded to Alfresco.
 * 
 * After all files have been uploaded, sourcePath will be removed from the filesystem.
 * 
 * @author D3K339
 * 
 * @version $Revision: 1.0 $
 */
public class Upload extends AbstractVeloWebScript {

  public static final String PARAM_SOURCE_PATH = "sourcePath";
  public static final String PARAM_DEST_PATH = "destPath";
  public static final String PARAM_UNZIP = "unzip";
  public static final String PARAM_METADATA = "metadataFile";
  private ThreadLocal<Boolean> unzip_per_request = new ThreadLocal<Boolean>();
  // private String metadataFilePath;

  private boolean autoUnzip = false;
  private FileNamePolicy fileNamePolicy;
  
  private TaggingService taggingService;

  // private Map<QName, Serializable> adhocProps = new HashMap<QName, Serializable>();

  /**
   * Method setAutoUnzip.
   * 
   * @param autoUnzip
   *          boolean
   */
  public void setAutoUnzip(boolean autoUnzip) {
    this.autoUnzip = autoUnzip;
  }

  /**
   * Method setFileNamePolicy.
   * 
   * @param fileNamePolicy
   *          FileNamePolicy
   */
  public void setFileNamePolicy(FileNamePolicy fileNamePolicy) {
    this.fileNamePolicy = fileNamePolicy;
  }

  /**
   * Method execute.
   * 
   * @param req
   *          WebScriptRequest
   * @param res
   *          WebScriptResponse
   * @throws IOException
   * @see org.springframework.extensions.webscripts.WebScript#execute(WebScriptRequest, WebScriptResponse)
   */
  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {

    
    long startUpload = System.currentTimeMillis();
    // Copy the request to a new temp folder so we can rename it without
    // colliding with other requests
    // create long life temporary dir for unzipping huge files... is a long life
    // (24 hours) good enough?
    File alfTempDir = TempFileProvider.getLongLifeTempDir("velo-upload");
    File tempDir = File.createTempFile("velo-upload-", "", alfTempDir);
    tempDir.delete();
    tempDir.mkdir();

    final File requestContent = new File(tempDir, "content.bin");

    
    try {
      long start = System.currentTimeMillis();

      logger.debug("Begin executing method.");

      // Set ok status by default (will be overriden if error occurs or subclass
      // returns different
      // status
      res.setStatus(HttpServletResponse.SC_OK);

      // First save the request content to a temporary file so we can reuse it
      // in case of a retrying transaction
      BufferedInputStream inputStream = new BufferedInputStream(req.getContent().getInputStream());
      FileCopyUtils.copy(inputStream, new FileOutputStream(requestContent));

      // Wrap in a retrying transaction handler in case of db deadlock
      RetryingTransactionCallback<UploadStatus> callback = new RetryingTransactionCallback<UploadStatus>() {
        public UploadStatus execute() throws Exception {
          UploadStatus status = null;
          try {
            status = (UploadStatus) executeImpl(req, res, requestContent);

          } catch (Exception e) {
            if (e instanceof AccessDeniedException) {
              res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
              // throw it back for web script framework to handle
              throw e;
            }
          }
          return status;
        }
      };

      long txstart = System.currentTimeMillis();
      UploadStatus status = transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);
      long end = System.currentTimeMillis();
      long txtime = (end - txstart);
      logger.debug("upload alf tx time: " + txtime + "\n\n");

      logger.debug("End executing method.");

      if (status != null) {
        // Write the status log to the response stream
        res.setStatus(207);
        writeStatus(res, status);
      }

      long endUpload = System.currentTimeMillis();
      long uploadTime = (endUpload - startUpload);
      logger.debug("upload alf tx time: " + uploadTime + "\n\n");
      System.out.println("upload alf tx time: " + uploadTime + "\n\n");

    } catch (Throwable e) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writeError(res, this.getClass().getName() + " failed.", e);
      logger.error(this.getClass().getName() + " failed.", e);

    } finally {
      // Now clean up our temp folder
      // in case of failure, don't fail the whole upload
      FileUtils.deleteQuietly(alfTempDir);
    }
  }

  /**
   * Method getFolder.
   * 
   * @param wikiFolderPath
   *          String
   * @return NodeRef
   * @throws FileNotFoundException
   */
  public NodeRef getFolder(final String wikiFolderPath) throws FileNotFoundException {

    // Convert the folder path to alfresco format
    String path = WikiUtils.getAlfrescoNamePath(wikiFolderPath);
    logger.debug("path = " + path);

    // Find the dest node from the path (will throw exception if node does not
    // exist)
    NodeRef parent = WikiUtils.getNodeByName(path, nodeService);

    if (!nodeService.getType(parent).equals(ContentModel.TYPE_FOLDER)) {
      throw new RuntimeException("Destination path is not a collection!");
    }

    return parent;

  }

  /**
   * Method executeImpl.
   * 
   * @param req
   *          WebScriptRequest
   * @param res
   *          WebScriptResponse
   * @param requestContent
   *          File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Get the request parameters:
    String sourcePath = req.getParameter(PARAM_SOURCE_PATH);
    String destPath = req.getParameter(PARAM_DEST_PATH);
    String unzip_param = req.getParameter(PARAM_UNZIP);
    String metadataFileParam = req.getParameter(PARAM_METADATA);

    return upload(sourcePath, destPath, unzip_param, metadataFileParam, requestContent);
  }

  /**
   * Method upload.
   * 
   * @param sourcePath
   *          String
   * @param destPath
   *          String
   * @param requestContent
   *          File
   * @return UploadStatus
   * @throws Exception
   */
  public UploadStatus upload(String sourcePath, String destPath, String unzip_param, String metadataFileParam, File requestContent) throws Exception {
    long start = System.currentTimeMillis();

    List<String> tags = new ArrayList<String>();
    Map<QName, Serializable> adhocProps = new HashMap<QName, Serializable>();

    
    if (metadataFileParam != null) {
      // Move metadata file to a longer lived(24hrs) temp directory so that it
      // can
      // be picked up phase 2. This will give some time for use case where
      // system reboots and
      // picks up phase2 processing from where it left
      // TODO - doesn't handle cases where system goes down for a long period of
      // time. Where
      // should file be saved? Although such a use case shouldn't in theory
      // happen in prod environments
      File file = new File(metadataFileParam);
      if (file.isFile()) {
        if (file.getName().endsWith(".properties")) {
          /*
           * File alfTempDir2 = TempFileProvider.getLongLifeTempDir("metadata_"+System .currentTimeMillis()) ; File newMetadataFile= new File(alfTempDir2,file.getName()); FileUtils.moveFile(file, newMetadataFile); metadataFilePath = newMetadataFile.getAbsolutePath();
           */
          // Instead of storing in file set it as properties in Alfresco
          Properties properties = new Properties();
          properties.load(new FileInputStream(file));
          String adhocNamespace = (String) properties.get("namespace");
          if (adhocNamespace == null || adhocNamespace.trim().isEmpty()) {
            adhocNamespace = VeloServerConstants.NAMESPACE_VELO_ADHOC;
          }
          Enumeration<?> e = properties.propertyNames();
          while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            // add test to see if prop name is 'tag' and not a q-name here
            if (key.equals("TAGS")) {
              String tagsString = properties.getProperty(key);
              if(tagsString != null && !tagsString.isEmpty()){
                tags = Arrays.asList(tagsString.split(","));
              }
            } else {
              adhocProps.put(QName.createQName(adhocNamespace, key), properties.getProperty(key));
            }
          }
        } else if (file.getName().endsWith(".json")) {
          // Currently used only by RCP client and
          // RCP Client call Cat Server Core Upload webscript
          /*
           * GsonBuilder builder = new GsonBuilder(); Gson gson = builder.enableComplexMapKeySerialization().create(); java.lang.reflect.Type mapType = new com.google.gson.reflect.TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();
           * 
           * //Reader reader = new InputStreamReader(globalMetadataFile); Reader reader = new FileReader(file);
           * 
           * final HashMap<String, ArrayList<String>> properties = gson.fromJson(reader, mapType); final Map<QName, Serializable> alfProps = new HashMap<QName, Serializable>(); for(String key : properties.keySet()) { List<String> value = properties.get(key); QName propQname = QName.createQName(key); Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, propQname, value); adhocProps.put(propQname, convertedValue); }
           */
        }
      }
    }

    if ((unzip_param != null) && (unzip_param.equalsIgnoreCase("true") || unzip_param.equalsIgnoreCase("false")))
      unzip_per_request.set(Boolean.valueOf(unzip_param));
    else
      unzip_per_request.set(this.autoUnzip);
    
    
    
    
    
    // Create the status object
    UploadStatus status = new UploadStatus();

    NodeRef parent = getFolder(destPath);

    File sourceFile = new File(sourcePath);
    if (sourceFile.isDirectory()) { // it is a local directory of files
      // Iterate through all files/folder in sourcePath
      File[] filesToUpload = sourceFile.listFiles();
      for (File file : filesToUpload) {
        recursiveUpload(file, parent, status, adhocProps, tags);
      }

      // Delete the source folder if
      // all of the files were uploaded successfully
      // if(status.failedFiles.keySet().size() == 0) { //removing this logic and
      // deleting temp dir now even if there are failed files
      // because otherwise each subsequent upload is added to this temp dir
      // resulting in it getting re-uploaded with each new file
      try {
        FileUtils.deleteDirectory(sourceFile);
      } catch (Throwable e) {
        logger.error("Failed to delete temp folder: " + sourceFile.getAbsolutePath(), e);
      }
      // }

    } else if (sourceFile.exists()) { // it is a local file
      recursiveUpload(sourceFile, parent, status, adhocProps, tags);

      try {
        sourceFile.delete();
      } catch (Throwable e) {
        logger.error("Failed to delete file: " + sourceFile.getAbsolutePath(), e);
      }

    } else { // it is a remote file sent via the request
      // First rename the file
      // (sourcePath is the file name in this case)
      String fileName = sourcePath;
      File renamedFile = new File(requestContent.getParentFile(), fileName);
      requestContent.renameTo(renamedFile);
      recursiveUpload(renamedFile, parent, status, adhocProps, tags);
    }

    long end = System.currentTimeMillis();
    long time = (end - start) / 1000;
    logger.debug("Phase 1 time to upload: " + status.numUploadedFiles + " files = " + time + " seconds");
    logger.debug("Returning from upload request.");
    return status;
  }

  /**
   * Method createFolder.
   * 
   * @param srcFolder
   *          File
   * @param destFolder
   *          NodeRef
   * @param status
   *          UploadStatus
   * @return NodeRef
   */
  protected NodeRef createFolder(final File srcFolder, final NodeRef destFolder, final UploadStatus status) {
    NodeRef child = null;

    try {

      String folderName = srcFolder.getName();

      // Check to see if the file name is going to be fixed by the
      // FileNamePolicy
      if (fileNamePolicy != null && fileNamePolicy.getBadFileNamePattern() != null && fileNamePolicy.getBadFileNamePattern().matcher(folderName).find()) {
        status.numRenamedFiles++;

        // Get the new name
        folderName = FileNamePolicy.getFixedName(folderName);
      }

      child = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS, folderName);

      if (child == null) {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, folderName);
        QName folderQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, folderName);
        child = nodeService.createNode(destFolder, ContentModel.ASSOC_CONTAINS, folderQName, ContentModel.TYPE_FOLDER, properties).getChildRef();
      }
      status.numUploadedCollections++;
      return child;

    } catch (Throwable e) {
      status.failedFiles.put(srcFolder.getAbsolutePath(), e);
      logger.error("Failed to upload folder: " + srcFolder.getAbsolutePath(), e);
    }
    return child;

  }

  /**
   * Method createFile.
   * 
   * @param srcFile
   *          File
   * @param destFolder
   *          NodeRef
   * @param status
   *          UploadStatus
   * @param adhocProps 
   * @return NodeRef
   */
  protected NodeRef createFile(final File srcFile, final NodeRef destFolder, final UploadStatus status, Map<QName, Serializable> adhocProps, List<String> tags) {

    NodeRef child = null;

    try {
      String fileName = srcFile.getName();

      // Check to see if the file name is going to be fixed by the
      // FileNamePolicy
      if (WikiUtils.getWikiHome() != null && fileNamePolicy.getBadFileNamePattern() != null && fileNamePolicy.getBadFileNamePattern().matcher(fileName).find()) {
        status.numRenamedFiles++;

        // Get the new name
        fileName = FileNamePolicy.getFixedName(fileName);
      }

      child = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS, fileName);

      Map<QName, Serializable> props = new HashMap<QName, Serializable>();
      if (!adhocProps.isEmpty()) {
        props.putAll(adhocProps);
        // props.put(VeloServerConstants.PROP_METADATA_FILE,metadataFilePath);
      }
      if (child == null) {
        child = NodeUtils.createFile(destFolder, fileName, srcFile, nodeService, contentService, mimetypeService, props);
      } else {
        // Update content for the file
        NodeUtils.updateFileContents(child, new FileInputStream(srcFile), nodeService, contentService);
        nodeService.addProperties(child, props);
      }
      if (taggingService!=null && tags!=null && !tags.isEmpty()) {
        taggingService.addTags(child, tags);
      }

      status.numUploadedFiles++;

    } catch (Throwable e) {
      status.failedFiles.put(srcFile.getAbsolutePath(), e);
      logger.error("Failed to upload file: " + srcFile.getAbsolutePath(), e);
    }
    return child;
  }

  /**
   * Method unCompressFile.
   * 
   * @param zipFile
   *          File
   * @param destFolder
   *          NodeRef
   * @param status
   *          UploadStatus
   * @param adhocProps 
   */
  private void unCompressFile(File zipFile, NodeRef destFolder, final UploadStatus status, Map<QName, Serializable> adhocProps, List<String> tags) {
    File tempDir = null;
    try {
      // create temp folder
      File alfTempDir = TempFileProvider.getTempDir();
      tempDir = File.createTempFile("velo-upload-", "", alfTempDir);
      tempDir.delete();
      tempDir.mkdir();

      // TODO: handle other types of zip (tar, bzip)
      // Unzip to temp folder
      if (ZipUtils.isZip(zipFile)) {
        ZipUtils.unzipFile(zipFile, tempDir);

      } else if (ZipUtils.isTarGz(zipFile)) {
        ZipUtils.tarGzUnzipFile(zipFile, tempDir);

      } else if (ZipUtils.isTar(zipFile)) {
        ZipUtils.untarFile(zipFile, tempDir);

      } else {
        ZipUtils.gUnzipFile(zipFile, tempDir);
      }

      // recursively upload everything in temp folder
      File[] files = tempDir.listFiles();
      for (File file : files) {
        recursiveUpload(file, destFolder, status, adhocProps, tags);
      }

      status.numUnzippedFiles++;

    } catch (Throwable e) {
      status.failedFiles.put(zipFile.getAbsolutePath(), e);
      logger.error("Failed to unzip file: " + zipFile.getAbsolutePath(), e);
    }

    // remove temp folder
    if (tempDir != null) {
      try {
        FileUtils.deleteDirectory(tempDir);
      } catch (Throwable e) {
        logger.error("Unable to delete temp folder: " + tempDir.getAbsolutePath(), e);
      }
    }
  }

  /**
   * Method recursiveUpload.
   * 
   * @param srcFile
   *          File
   * @param destFolder
   *          NodeRef
   * @param status
   *          UploadStatus
   * @param adhocProps 
   * @throws IOException
   */
  public void recursiveUpload(File srcFile, NodeRef destFolder, UploadStatus status, Map<QName, Serializable> adhocProps, List<String> tags) throws IOException {
    if (srcFile.isDirectory()) {
      NodeRef newFolder = createFolder(srcFile, destFolder, status);

      File[] children = srcFile.listFiles();
      for (File child : children) {
        recursiveUpload(child, newFolder, status, adhocProps, tags);
      }

    } else {
      if (!wasUnzipped(srcFile, destFolder, status, adhocProps, tags) && !srcFile.getName().equals(VeloServerConstants.METADATA_FILENAME)) {
        createFile(srcFile, destFolder, status, adhocProps, tags);
      }
    }
  }

  /**
   * Method wasUnzipped.
   * 
   * @param srcFile
   *          File
   * @param destFolder
   *          NodeRef
   * @param status
   *          UploadStatus
   * @param adhocProps 
   * @return boolean
   */
  public boolean wasUnzipped(File srcFile, NodeRef destFolder, UploadStatus status, Map<QName, Serializable> adhocProps, List<String> tags) {
    boolean wasUnzipped = false;
    Boolean unzip = unzip_per_request.get();
    if (unzip == null) {
      unzip = autoUnzip;
    }
    if (unzip) {
      if (ZipUtils.isCompressed(srcFile)) {
        wasUnzipped = true;
        unCompressFile(srcFile, destFolder, status, adhocProps, tags);

      }
    }
    return wasUnzipped;
  }

  /**
   * Method writeStatus.
   * 
   * @param res
   *          WebScriptResponse
   * @param status
   *          UploadStatus
   */
  private void writeStatus(WebScriptResponse res, UploadStatus status) {
    // write the UploadStatus to the output stream
    PrintStream printStream = null;
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

    try {
      printStream = new PrintStream(res.getOutputStream());
      printStream.println("Number of files uploaded: " + status.numUploadedFiles);
      printStream.println("Number of collections uploaded: " + status.numUploadedCollections);
      printStream.println("Number of renamed files: " + status.numRenamedFiles);
      printStream.println("Number of unzipped files: " + status.numUnzippedFiles);

      int numFailedFiles = status.failedFiles.keySet().size();
      printStream.println("Number of failed files: " + numFailedFiles);

      if (numFailedFiles > 0) {
        printStream.println("\nFailed file details:\n");
        for (String fileName : status.failedFiles.keySet()) {
          printStream.println("\nFile: " + fileName);
          status.failedFiles.get(fileName).printStackTrace(printStream);
        }
      }
      printStream.flush();

    } catch (Throwable t) {
      logger.error("Could not print status to response stream.", t);

    } finally {
      if (printStream != null) {
        printStream.close();
      }
    }
  }

  public TaggingService getTaggingService() {
    return taggingService;
  }

  public void setTaggingService(TaggingService taggingService) {
    this.taggingService = taggingService;
  }
}
