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
package gov.pnnl.cat.web.scripts;

import gov.pnnl.cat.pipeline.FileProcessingPipelineUtil;
import gov.pnnl.cat.util.DataSniffer;
import gov.pnnl.cat.util.DataSniffer.ContentDataInfo;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.NotificationUtils;
import gov.pnnl.cat.util.ZipUtils;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Called by clients to upload one or more files. destPath points to the folder
 * path in Alfresco where the file will be uploaded to. If zip file is uploaded,
 * it will automatically be unzipped before being added to Alfresco, if
 * autoUznip is true.
 * 
 * @author D3K339
 * 
 * @version $Revision: 1.0 $
 */
public class Upload extends AbstractCatWebScript {

  /* Each file to be uploaded needs to be put in the multipart form
   * with a parameter containing the file and a corresponding parameter
   * containing the alfresco repository path for the new file.  Note that the file name
   * is contained in the repository path, so we don't need a separate param
   * for the file name.  The filePath_ parameter and file parameter_ are correlated
   *  by appending the parameter with an index number (0, 1, 2, 3, etc.).  For example:
   *  filePath_0 = /company_home/User Documents/admin/testFile.txt
   *  file_0 = binary contents
   */
  public static final String PARAM_FILE_PATH_PREFIX = "filePath_";
  public static final String PARAM_FILE_PREFIX = "file_";

  // TODO: - not sure if we want to attach either a metadata file per file uploaded or
  // a  regular expression that maps one metadata file to a set of files
  public static final String PARAM_METADATA_FILE_PREFIX = "metadataFile_";  
  public static final String PARAM_METADATA_REGEX_PREFIX = "metadataRegex_";  

  // If caller wants to redirect after upload - this param is passed in the URL
  public static final String PARAM_REDIRECT_URL = "redirect";

  // If this request should automatically unzip any zip files contained in the set
  // This param is passed in the URL
  public static final String PARAM_UNZIP = "unzip";

  /* If this request is attaching a global metadata file with properties to be set on 
   * every file in the set.  The metadata file param must be passed in the 
   * metadata form.
   */
  public static final String PARAM_METADATA_FILE = "globalMetadataFile";

  // For performance testing or for uploading files where we don't care about
  // metadata extraction, we can disable the processing pipeline.  This param
  // is passed in the URL
  public static final String PARAM_ENABLE_PIPELINE = "enablePipeline";  

  // For performance testing or for uploading files when we don't care about
  // notifications, we can disable notifications to improve performance.
  // This param is passed in the URL
  public static final String PARAM_ENABLE_NOTIFICATION = "enableNotififcations";  

  // Can set the tx batch size per request - this param is passed in the URL
  public static final String PARAM_BATCH_SIZE = "batchSize";  

  // Can set the number of batch processing threads per request - this param is passed in the URL
  public static final String PARAM_NUM_THREADS = "numThreads";  

  private ContentStore fileContentStore;
  private NotificationUtils notificationUtils;
  private TaggingService taggingService;

  // Global settings (can be set in alfresco-global.properties file)
  private boolean autoUnzip = false;
  private boolean enableNotifications = true;
  private boolean enablePipeline = true;
  private int txBatchSize = 20; // alfresco uses 20 default batch size for its bulk uploader, and this looks like it performs best
  private int numThreads = 1;

  // Request-specific settings
  private ThreadLocal<Map<QName, Serializable>> globalMetadataPropertiesPerRequest = new ThreadLocal<Map<QName, Serializable>>();
  private ThreadLocal<List<String>> globalTagsPerRequest = new ThreadLocal<List<String>>();
  private ThreadLocal< Map<String, Map<QName, Serializable>> > fileSpecificMetadataPropertiesPerRequest = new ThreadLocal< Map<String, Map<QName, Serializable>> >();
  private ThreadLocal<Boolean> unzipPerRequest = new ThreadLocal<Boolean>();
  private ThreadLocal<Integer> txBatchSizePerRequest = new ThreadLocal<Integer>();
  private ThreadLocal<Integer> numThreadsPerRequest = new ThreadLocal<Integer>();
  private ThreadLocal<Boolean> enablePipelinePerRequest = new ThreadLocal<Boolean>();
  private ThreadLocal<Boolean> enableNotificationsPerRequest = new ThreadLocal<Boolean>();

  /**
   * Can be set in alfresco-global.properties as velo.upload.tx.batch.size
   * @param batchSize
   */
  public void setTxBatchSize(int txBatchSize) {
    this.txBatchSize = txBatchSize;
  }

  /**
   * Can be set in alfresco-global.properties as velo.upload.autoUnzip
   * @param autoUnzip
   */
  public void setAutoUnzip(boolean autoUnzip) {
    this.autoUnzip = autoUnzip;
  }

  /**
   * Can be set in alfresco-global.properties as velo.upload.num.threads
   * @param numThreads
   */
  public void setNumThreads(int numThreads) {
    this.numThreads = numThreads;
  }

  public void setFileContentStore(ContentStore fileContentStore) {
    this.fileContentStore = fileContentStore;
  }

  public void setNotificationUtils(NotificationUtils notificationUtils) {
    this.notificationUtils = notificationUtils;
  }

  /**
   * Method execute.
   * 
   * @param req
   *            WebScriptRequest
   * @param res
   *            WebScriptResponse
   * @throws IOException
   * @see org.springframework.extensions.webscripts.WebScript#execute(WebScriptRequest,
   *      WebScriptResponse)
   */
  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res)
      throws IOException {
    // Copy the request to a new temp folder so we can rename it without colliding with other requests
    //create long life temporary dir for unzipping huge files... is a long life (24 hours) good enough?
    File alfTempDir = TempFileProvider.getLongLifeTempDir("velo-upload");
    File tempDir = File.createTempFile("velo-upload-", "", alfTempDir);
    tempDir.delete();
    tempDir.mkdir();

    try {
      long start = System.currentTimeMillis();
      logger.debug("Begin executing method.");

      // Set ok status by default (will be overriden if error occurs)
      res.setStatus(HttpServletResponse.SC_OK);

      // Create the status object which will hold result stats, including errors
      final UploadStatus status = new UploadStatus();

      // Parse the global parameters passed via URL
      parseRequestParameters(req);
      final Boolean batchEnableNotifications = enableNotificationsPerRequest.get();
      final Boolean batchEnablePipeline = enablePipelinePerRequest.get();
      final Integer batchSize = txBatchSizePerRequest.get();
      System.out.println("notifications enabled = " + batchEnableNotifications);
      System.out.println("pipeline enabled = " + batchEnablePipeline);

      // get the files to upload from the request
      final List<UploadFileInfo> filesToUpload = parseFilesToUpload(req, tempDir);

      // Compute the batches (so they run faster)
      final List<List<UploadFileInfo>> batches = computeBatches(filesToUpload, batchSize); 

      // Run each batch in separate tx
      for(final List<UploadFileInfo> batch : batches) {
        RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
          public UploadStatus execute() throws Exception {
            notificationUtils.setNotificationsEnabledForThisTransaction(batchEnableNotifications);

            // disable pipeline
            if(!batchEnablePipeline) {
              FileProcessingPipelineUtil.disablePiplineForCurrentTransaction();
            }

            try {
              executeImpl(req, res, batch, status);

            } catch (Exception e) {
              if (e instanceof AccessDeniedException) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              } else {
                // throw it back for web script framework to handle
                throw e;
              }
            }
            return null;
          }
        };
        long txstart = System.currentTimeMillis();
        transactionService.getRetryingTransactionHelper().doInTransaction(callback,false);
        long end = System.currentTimeMillis();
        long txtime = (end - txstart);
        logger.debug("upload alf tx time: " + txtime + " ms");
        System.out.println("upload alf tx time: " + txtime + " ms");
      }

      long end = System.currentTimeMillis();
      System.out.println("upload total time = " + (end - start) + " ms");
      logger.debug("End executing method.");

      if (status != null) {
        // Write the status log to the response stream
        res.setStatus(207);
        writeStatus(res, status);
      }
    } catch (Throwable e) {
      handleError(e);
      logger.error(this.getClass().getName() + " failed.", e);

    } finally {
      // Now clean up our temp folder
      //in case of failure, don't fail the whole upload
      FileUtils.deleteQuietly(alfTempDir);
    }

    String redirectUrl = req.getParameter(PARAM_REDIRECT_URL);
    if (redirectUrl != null) {
      ((WebScriptServletResponse) res).getHttpServletResponse()
      .sendRedirect(redirectUrl);
    }
  }

  /**
   * Set all global request parameters passed in via the URL.  Any file-specific params
   * passed in the multi-part form will be processed in parseFilesToUpload() method.
   */
  private void parseRequestParameters(WebScriptRequest req) {

    // get the unzip parameter from the request
    // If not set, will use global value
    String unzipParam = req.getParameter(PARAM_UNZIP);
    if((unzipParam != null) && (unzipParam.equalsIgnoreCase("true") || unzipParam.equalsIgnoreCase("false"))) {
      unzipPerRequest.set(Boolean.valueOf(unzipParam));
    } else {
      unzipPerRequest.set(this.autoUnzip);
    }

    // get the enable pipeline parameter from the request
    // If not set, will use global value
    String enablePipelineParam = req.getParameter(PARAM_ENABLE_PIPELINE);
    if((enablePipelineParam != null) && (enablePipelineParam.equalsIgnoreCase("true") || enablePipelineParam.equalsIgnoreCase("false"))) {
      enablePipelinePerRequest.set(Boolean.valueOf(enablePipelineParam));
    } else {
      enablePipelinePerRequest.set(this.enablePipeline);
    }

    // get the enable notifications parameter from the request
    // If not set, will use global value
    String enableNotificationsParam = req.getParameter(PARAM_ENABLE_NOTIFICATION);
    if((enableNotificationsParam != null) && (enableNotificationsParam.equalsIgnoreCase("true") || enableNotificationsParam.equalsIgnoreCase("false"))) {
      enableNotificationsPerRequest.set(Boolean.valueOf(enableNotificationsParam));
    } else {
      enableNotificationsPerRequest.set(this.enableNotifications);
    }

    // Get the transaction batch size from the request
    // If not set, will use global value
    String batchSizeParam = req.getParameter(PARAM_BATCH_SIZE);
    if((batchSizeParam != null)) {
      txBatchSizePerRequest.set(Integer.valueOf(batchSizeParam));
    } else {
      txBatchSizePerRequest.set(this.txBatchSize);
    }

    // Get the number of threads from the request
    // If not set, will use global value
    String numThreadsParam = req.getParameter(PARAM_NUM_THREADS);
    if((numThreadsParam != null)) {
      numThreadsPerRequest.set(Integer.valueOf(numThreadsParam));
    } else {
      numThreadsPerRequest.set(this.numThreads);
    }

  }

  /**
   * Method getFolder.
   * 
   * @param folderPath
   *            String
   * @return NodeRef
   * @throws FileNotFoundException
   */
  public NodeRef getFolder(final String folderPath)
      throws FileNotFoundException {

    logger.debug("path = " + folderPath);

    // Find the dest node from the path (will throw exception if node does
    // not exist)
    NodeRef parent = NodeUtils.getNodeByName(folderPath, nodeService);

    if (!nodeService.getType(parent).equals(ContentModel.TYPE_FOLDER)) {
      throw new RuntimeException("Destination path is not a collection!");
    }

    return parent;

  }

  private List<List<UploadFileInfo>> computeBatches(List<UploadFileInfo> allFiles, int batchSize) {
    int numBatches = allFiles.size()/batchSize;
    if(numBatches == 0) {
      // we have very small number of files, so upload in one batch
      numBatches = 1;
      batchSize = allFiles.size();
    }
    List<List<UploadFileInfo>> batches = new ArrayList<List<UploadFileInfo>>();  
    List<UploadFileInfo> currentBatch = new ArrayList<UploadFileInfo>();
    batches.add(currentBatch);
    int fileNum = 0;
    int batchNum = 0;
    for(UploadFileInfo file : allFiles) {
      if(fileNum >= batchSize && batchNum < numBatches-1) {
        // time to move to next batch
        fileNum = 0;
        batchNum++;
        currentBatch = new ArrayList<UploadFileInfo>();
        batches.add(currentBatch);
      }
      currentBatch.add(file);
      fileNum++;
    }

    System.out.println("batch size = " + batchSize);
    System.out.println("num batches = " + numBatches);
    System.out.println("num files = " + allFiles.size());

    return batches;
  }

  /**
   * @param req
   * @param destDir
   * @return
   * @throws Exception
   */
  private List<UploadFileInfo> parseFilesToUpload(WebScriptRequest req, File tempDir) throws Exception {

    List<UploadFileInfo> filesToUpload = new ArrayList<>();
    InputStream globalMetadataFile = null;

    WebScriptServletRequest request = (WebScriptServletRequest) req;
    FormField[] fields = request.getFormData().getFields();

    // the key to this map is the file regex
    Map<String, InputStream> fileSpecificMetadata = new HashMap<String, InputStream>();

    for (FormField field : fields) {

      // This is a metadata file with props to be assigned to all the files
      if (field.getName().startsWith(PARAM_METADATA_FILE) && field.getIsFile()) {
        globalMetadataFile = field.getInputStream();

      } else if (field.getName().startsWith(PARAM_METADATA_FILE_PREFIX) && field.getIsFile()) { 
        // This is a file-specific metadata file
        String regex = field.getName().substring(PARAM_METADATA_FILE_PREFIX.length());
        InputStream in = field.getInputStream();
        fileSpecificMetadata.put(regex, in);

      } else if (field.getIsFile()) {
        // This is a file to upload
        String path = field.getName();
        UploadFileInfo fileInfo = getUploadFileInfo(field.getFileItem(), path, tempDir);
        filesToUpload.add(fileInfo);     
      }
    }

    // TODO: iterate through the metadata files and the regexes and find the correlated matches based on id#
    // TODO: if metadata files not empty, then we need to parse metadata files like we do for global metadata, and
    // we have to save the metadata files and regexes as the   fileSpecificMetadataPropertiesPerRequest thread local variable
    if(fileSpecificMetadata.size() > 0) {
      parseFileSpecificMetadata(fileSpecificMetadata); // add this method
    }

    if(globalMetadataFile != null) {
      parseGlobalMetadata(globalMetadataFile);
    }

    return filesToUpload;
  }

  private void parseGlobalMetadata(InputStream globalMetadataFile) {
    try {
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.enableComplexMapKeySerialization().create();          
      java.lang.reflect.Type mapType =  new com.google.gson.reflect.TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();

      Reader reader = new InputStreamReader(globalMetadataFile);
      
      final HashMap<String, ArrayList<String>> properties = gson.fromJson(reader, mapType);
      final Map<QName, Serializable> alfProps = new HashMap<QName, Serializable>();
      final List<String> alfTags = new ArrayList<String>();
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
        public UploadStatus execute() throws Exception {

          for(String key : properties.keySet()) {
            List<String> value = properties.get(key);
            //see if the key is TAGS (if so this is the list of tags to apply)
            if (key.equals("TAGS")) {
              alfTags.addAll(value);
            }else{
              QName propQname = QName.createQName(key);
              Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, propQname, value);
              alfProps.put(propQname, convertedValue);
            }
          }
          if(!alfTags.isEmpty()){
            globalTagsPerRequest.set(alfTags);
          }
          globalMetadataPropertiesPerRequest.set(alfProps);
          return null;
        }
      };
      long txstart = System.currentTimeMillis();
      transactionService.getRetryingTransactionHelper().doInTransaction(callback,true);
    } catch (Exception e) {
      // TODO: should we fail the whole upload if the metadata failed
      handleError(e);
    }


  }



  private void parseFileSpecificMetadata(final Map<String, InputStream> fileSpecificMetadata) {
    try {
      final Map<String, Map<QName, Serializable>> alfFileProps = new HashMap<String, Map<QName, Serializable>>();
      
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {
        @SuppressWarnings("unchecked")
        public UploadStatus execute() throws Exception {
          GsonBuilder builder = new GsonBuilder();
          Gson gson = builder.enableComplexMapKeySerialization().create();          
          java.lang.reflect.Type mapType =  new com.google.gson.reflect.TypeToken<HashMap<String, ArrayList<String>>>(){}.getType();

          for(String key : fileSpecificMetadata.keySet()) {
            InputStream valueStream = fileSpecificMetadata.get(key);
            Reader reader = new InputStreamReader(valueStream);
            String regex = key;
            
            HashMap<String, ArrayList<String>> properties = gson.fromJson(reader, mapType);
            Map<QName, Serializable> alfPropsMap = new HashMap<QName, Serializable>();
            
            // List<String>  value = properties.get(key);
            for(String key1 : properties.keySet()) {
              List<String> value = properties.get(key1);
              QName propQname = QName.createQName(key1);
              Serializable convertedValue = WebScriptUtils.getPropertyValueFromStringList(dictionaryService, propQname, value);
              alfPropsMap.put(propQname, convertedValue);
            }
            alfFileProps.put(regex, alfPropsMap);


          }
          fileSpecificMetadataPropertiesPerRequest.set(alfFileProps);
          return null;
        }
      };
      long txstart = System.currentTimeMillis();
      transactionService.getRetryingTransactionHelper().doInTransaction(callback,true);
    } catch (Exception e) {
      handleError(e);
    }
  }

  private UploadFileInfo getUploadFileInfo(FileItem formFile, String repositoryFilePath, File tempDir) throws Exception {
    CmsPath path = new CmsPath(repositoryFilePath);
    String fileName = path.getName();

    Boolean unzip = unzipPerRequest.get();
    if(unzip == null) {
      unzip = autoUnzip;
    }

    // if this is a zip file that we are going to unzip, then we are going to do this in a temp folder, so
    // don't bother making an entry in the alfresco content store
    if(ZipUtils.isCompressed(fileName) && unzip) {
      // we have to write the input stream into the temp dir
      // TODO - this is inefficient, but the spring webscript framework does not expose the File in FormData,only
      // InputStream, and java ZipFile only takes a File in the constructor :(
      File zipFile = new File(tempDir, fileName);
      formFile.write(zipFile); // move the file form field to the temp dir
      return new UploadFileInfo(path,zipFile, true);

    } else {
      // make an entry in the alfresco file store
      // we create the file directly into the file store so we don't have to copy from
      // another temp file, which prevents writing the file twice
      return createEntryInContentStore(formFile , path);
    }

  }

  private UploadFileInfo createEntryInContentStore(FileItem formFile, CmsPath repositoryFilePath) throws Exception {

    // ContentData properties
    String contentUrl = null;
    long fileSize = 0;
    String mimetype = null;
    String encoding = null;

    // Create new entry in content store
    ContentWriter writer = fileContentStore.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
    contentUrl = writer.getContentUrl();
    File contentStoreFile = ((FileContentWriter) writer).getFile(); // new File(writer.getContentUrl());
    
    // We have to delete the content store file, because the content store creates a new, empty file instead
    // of just giving us a file name
    contentStoreFile.delete();

    // move form file to content store
    formFile.write(contentStoreFile);
    fileSize = contentStoreFile.length();

    // detect mimetype
    try {
      ContentDataInfo info = DataSniffer.sniffContentData(repositoryFilePath.getName(), contentStoreFile);
      mimetype = info.getMimetype();
      encoding = info.getEncoding();
    } catch (Throwable e) {
      logger.error(e);
    }


    ContentData contentData = new ContentData(contentUrl, mimetype, fileSize, encoding);
    UploadFileInfo fileInfo = new UploadFileInfo(contentData, repositoryFilePath, contentStoreFile);
    return fileInfo;
  }

  /**
   * Method executeImpl.
   * 
   * @param req
   *            WebScriptRequest
   * @param res
   *            WebScriptResponse
   * @param filesToUpload
   *            List<File>
   * @return Object
   * @throws Exception
   */
  protected void executeImpl(WebScriptRequest req, WebScriptResponse res,
      List<UploadFileInfo> filesToUpload, UploadStatus status) throws Exception {

    for(UploadFileInfo fileInfo : filesToUpload) {
      status = upload(fileInfo, status);
    }

  }

  /**
   * @param filePath - filePath includes name of file as last segment
   * @param file
   * @param status
   * @return
   * @throws Exception
   */
  public UploadStatus upload(UploadFileInfo fileInfo, UploadStatus status) throws Exception {
    long start = System.currentTimeMillis();

    CmsPath path = fileInfo.repositoryPath;
    NodeRef parent = getFolder(path.getParent().toAssociationNamePath());

    if (!wasUnzipped(fileInfo, parent, status)) {
      createFileFromContentStore(fileInfo, parent, status);
    }

    long end = System.currentTimeMillis();
    long time = (end - start) / 1000;
    logger.debug("Time to upload: " + status.numUploadedFiles + " files = "
        + time + " seconds");
    logger.debug("Returning from upload request.");
    return status;
  }

  /**
   * Method createFolder.
   * 
   * @param srcFolder
   *            File
   * @param destFolder
   *            NodeRef
   * @param status
   *            UploadStatus
   * @return NodeRef
   */
  private NodeRef createFolder(final File srcFolder,
      final NodeRef destFolder, final String destName,
      final UploadStatus status) {
    NodeRef child = null;

    try {

      String folderName = destName;
      if (destName == null || destName.trim().isEmpty())
        folderName = srcFolder.getName();
      child = nodeService.getChildByName(destFolder,
          ContentModel.ASSOC_CONTAINS, folderName);

      if (child == null) {
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(
            1);
        properties.put(ContentModel.PROP_NAME, folderName);
        QName folderQName = QName.createQName(
            NamespaceService.CONTENT_MODEL_1_0_URI, folderName);
        child = nodeService.createNode(destFolder,
            ContentModel.ASSOC_CONTAINS, folderQName,
            ContentModel.TYPE_FOLDER, properties).getChildRef();
      }
      status.numUploadedCollections++;
      return child;

    } catch (Throwable e) {
      status.failedFiles.put(srcFolder.getAbsolutePath(), e);
      logger.error(
          "Failed to upload folder: " + srcFolder.getAbsolutePath(),
          e);
    }
    return child;

  }

  /**
   * Creates a file that is currently residing in a temp folder, as it was from an unzipped file.
   * Given that, this file must be moved into the content store first before it can be created.
   * @param srcFile
   * @param fileName
   * @param destFolder
   * @param status
   * @return
   */
  private NodeRef createFileFromTempFolder(File srcFile, String fileName, NodeRef destFolder, UploadStatus status) {

    NodeRef child = null;

    try {
      child = nodeService.getChildByName(destFolder,
          ContentModel.ASSOC_CONTAINS, fileName);

      if (child == null) {
        Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
        Map<QName, Serializable> globalProps = globalMetadataPropertiesPerRequest.get();
        List<String> globalTags = globalTagsPerRequest.get();
        if(globalProps != null) {
          contentProps.putAll(globalProps);
        }

        // TODO:  if there are fileSpecificMetadataPropertiesPerRequest values set, then we have to iterate over the fileSpecificMetadataPropertiesPerRequest map,
        // and we have to see if the alfresco path of the current file
        // matches the regex for the current property map, and if so, then add those properties to contentProps
        String destinationFolderFullPath = nodeService.getPath(destFolder).toString();
        CmsPath filePath = new CmsPath(destinationFolderFullPath).append(fileName);
        String displayPath = filePath.toDisplayString();



        if (fileSpecificMetadataPropertiesPerRequest != null)
        {
          Map<String, Map<QName, Serializable>> fileSpecificMetadataProperties = fileSpecificMetadataPropertiesPerRequest.get();
          for (String key : fileSpecificMetadataProperties.keySet()){
            Map<QName, Serializable> valueStream = fileSpecificMetadataProperties.get(key);

            //for (QName mapKey : valueStream.keySet() ){
             // Serializable values = valueStream.get(mapKey);

              if(isFileRegexPathMatch(displayPath, key))
              {
                contentProps.putAll(valueStream);
              }
              else if(wildCardMatch(displayPath, key))
              {
                contentProps.putAll(valueStream);
              }
          //  }    
          }
        }

        NodeRef nodeRef = NodeUtils.createFile(destFolder, fileName, srcFile, nodeService, contentService, mimetypeService, contentProps);
        
        if(globalTags != null && !globalTags.isEmpty()){
          //hack for now to ensure company home has the tag scope aspect applied before we tag anything during uploads
          if(!taggingService.isTagScope(NodeUtils.getCompanyHome(nodeService))){
            taggingService.addTagScope(NodeUtils.getCompanyHome(nodeService));
          }
          taggingService.addTags(nodeRef, globalTags);
        }
        
      } else {
        // Update content for the file
        NodeUtils.updateFileContents(child,
            new FileInputStream(srcFile), nodeService,
            contentService);
      }

      status.numUploadedFiles++;

    } catch (Throwable e) {
      status.failedFiles.put(srcFile.getAbsolutePath(), e);
      logger.error("Failed to upload file: " + srcFile.getAbsolutePath(),
          e);
    }

    return child;
  }

  private boolean isFileRegexPathMatch(String alfrescoPath, String regex) {
    // TODO: copy the regex matching from ImportPremierDataSet class, look around line 480
    // if any part of the path matches the regex, it's ok
    // TODO: what do we do if the regex is an absolute path - maybe we reject?
    return alfrescoPath.contains(regex);
  }

  public static boolean wildCardMatch(String text, String pattern) {
    // Create the cards by splitting using a RegEx. If more speed
    // is desired, a simpler character based splitting can be done.
    String[] cards = pattern.split("\\*");

    //if only one 'card' is returned then that means 
    //there are no wildcards, so only return true if text and pattern match exactly
    if(cards.length == 1){
      if(text.equals(pattern)){
        return true;
      }else{
        return false;
      }
    }

    // Iterate over the cards.
    for (String card : cards) {
      int idx = text.indexOf(card);
      System.out.println("card: " + card + " idx: " + idx);

      // Card not detected in the text.
      if (idx == -1) {
        return false;
      }

      // Move ahead, towards the right of the text.
      text = text.substring(idx + card.length());
      System.out.println("text: " + text);
    }

    return true;
  }
  //TODO: make similar changes in this as well as above. this is in content store and above one is for the unzipped files from temp folder
  private NodeRef createFileFromContentStore(UploadFileInfo fileInfo, NodeRef destFolder, UploadStatus status) {
    String fileName = fileInfo.repositoryPath.getName();
    Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
    contentProps.put(ContentModel.PROP_NAME, fileName);
    contentProps.put(ContentModel.PROP_CONTENT, fileInfo.contentData);

    Map<QName, Serializable> globalProps = globalMetadataPropertiesPerRequest.get();
    List<String> globalTags = globalTagsPerRequest.get();
    if(globalProps != null) {
      contentProps.putAll(globalProps);
    }
    String destinationFolderFullPath = nodeService.getPath(destFolder).toString();
    CmsPath filePath = new CmsPath(destinationFolderFullPath).append(fileName);
    String displayPath = filePath.toDisplayString();

    Map<String, Map<QName, Serializable>> fileSpecificMetadataProperties = fileSpecificMetadataPropertiesPerRequest.get();
    if(fileSpecificMetadataProperties != null) {

      for (String key : fileSpecificMetadataProperties.keySet()){
        Map<QName, Serializable> valueStream = fileSpecificMetadataProperties.get(key);

       // for (QName mapKey : valueStream.keySet() ){
          //Serializable values = valueStream.get(mapKey);
          //String regex= values.toString();
          if(isFileRegexPathMatch(displayPath, key))
          {
            contentProps.putAll(valueStream);
          }

          else if(wildCardMatch(displayPath, key))
          {
            contentProps.putAll(valueStream);
          }
        //}    
      }
    }

    // create content node
    NodeRef child = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS, fileName);
    NodeRef newNode;
    if(child == null) {
      newNode = nodeService.createNode(destFolder, ContentModel.ASSOC_CONTAINS, 
          QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, fileName), ContentModel.TYPE_CONTENT, contentProps).getChildRef();
    } else {
      nodeService.setProperty(child, ContentModel.PROP_CONTENT, fileInfo.contentData);
      newNode = child;
    }
    if(globalTags != null && !globalTags.isEmpty()){
      //hack for now to ensure company home has the tag scope aspect applied before we tag anything during uploads
      if(!taggingService.isTagScope(NodeUtils.getCompanyHome(nodeService))){
        taggingService.addTagScope(NodeUtils.getCompanyHome(nodeService));
      }
      taggingService.addTags(newNode, globalTags);
      
    }
    return newNode;
  }

  /**
   * Unzips file to new temp dir
   * @param zipFile
   * @param destFolder
   * @param status
   */
  private void unCompressFile(File zipFile, NodeRef destFolder,
      final UploadStatus status) {
    File tempDir = null;
    try {
      // create temp folder
      File alfTempDir = TempFileProvider.getTempDir();
      tempDir = File.createTempFile("velo-upload-", "", alfTempDir);
      tempDir.delete();
      tempDir.mkdir();

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
        recursiveUpload(file, destFolder, status);
      }

      status.numUnzippedFiles++;

    } catch (Throwable e) {
      status.failedFiles.put(zipFile.getAbsolutePath(), e);
      logger.error("Failed to unzip file: " + zipFile.getAbsolutePath(),
          e);
    }

    // remove temp folder
    if (tempDir != null) {
      try {
        FileUtils.deleteDirectory(tempDir);
      } catch (Throwable e) {
        logger.error(
            "Unable to delete temp folder: "
                + tempDir.getAbsolutePath(), e);
      }
    }
  }

  /**
   * Method recursiveUpload.
   * 
   * @param srcFile
   *            File
   * @param destFolder
   *            NodeRef
   * @param status
   *            UploadStatus
   * @throws IOException
   */
  public void recursiveUpload(File tmpFile, NodeRef destFolder,
      UploadStatus status) throws IOException {
    if (tmpFile.isDirectory()) {
      NodeRef newFolder = createFolder(tmpFile, destFolder, null, status);

      File[] children = tmpFile.listFiles();
      for (File child : children) {
        recursiveUpload(child, newFolder, status);
      }

    } else {
      if (!wasUnzipped(tmpFile, destFolder, status)) {
        createFileFromTempFolder(tmpFile, tmpFile.getName(), destFolder, status);
      }
    }
  }

  /**
   * Method wasUnzipped.
   * 
   * @param srcFile
   *            File
   * @param destFolder
   *            NodeRef
   * @param status
   *            UploadStatus
   * @return boolean
   */
  public boolean wasUnzipped(UploadFileInfo fileInfo, NodeRef destFolder, UploadStatus status) {
    boolean wasUnzipped = false;
    if(fileInfo.isZip) {
      wasUnzipped = true;
      unCompressFile(fileInfo.file, destFolder, status);      
    }
    return wasUnzipped;
  }

  public boolean wasUnzipped(File srcFile, NodeRef destFolder, UploadStatus status) {
    String fileName = srcFile.getName();  
    Boolean unzip = unzipPerRequest.get();
    if(unzip == null) {
      unzip = autoUnzip;
    }

    boolean wasUnzipped = false;
    if (ZipUtils.isCompressed(fileName) && unzip) {
      wasUnzipped = true;
      unCompressFile(srcFile, destFolder, status);
    }
    return wasUnzipped;
  }

  /**
   * Method writeStatus.
   * 
   * @param res
   *            WebScriptResponse
   * @param status
   *            UploadStatus
   */
  private void writeStatus(WebScriptResponse res, UploadStatus status) {
    // write the UploadStatus to the output stream
    PrintStream printStream = null;
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

    try {
      printStream = new PrintStream(res.getOutputStream());
      printStream.println("Number of files uploaded: "
          + status.numUploadedFiles);
      printStream.println("Number of collections uploaded: "
          + status.numUploadedCollections);
      printStream.println("Number of unzipped files: "
          + status.numUnzippedFiles);

      int numFailedFiles = status.failedFiles.keySet().size();
      printStream.println("Number of failed files: " + numFailedFiles);

      if (numFailedFiles > 0) {
        printStream.println("\nFailed file details:\n");
        for (String fileName : status.failedFiles.keySet()) {
          printStream.println("\nFile: " + fileName);
          status.failedFiles.get(fileName).printStackTrace(
              printStream);
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


  /* (non-Javadoc)
   * @see gov.pnnl.cat.web.scripts.AbstractCatWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse, java.io.File)
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res,
      File requestContent) throws Exception {
    // TODO refactor so we don't need this separaete method
    return null;
  }

  public void setTaggingService(TaggingService taggingService) {
    this.taggingService = taggingService;
  }

  /**
   * Class to hold information for a file to be uploaded into alfresco content store.
   */
  class UploadFileInfo {
    private ContentData contentData;
    private CmsPath repositoryPath;
    private File file;
    private boolean isZip = false;

    /**
     * Constructor for UploadInfo.
     * @param contentData ContentData
     * @param repositoryPath String
     */
    public UploadFileInfo(ContentData contentData, CmsPath repositoryPath, File contentStoreFile) {
      this.contentData = contentData;
      this.repositoryPath = repositoryPath;
      this.file = contentStoreFile;
    }

    public UploadFileInfo(CmsPath repositoryPath, File zipFile, boolean isZip) {
      this.repositoryPath = repositoryPath;
      this.isZip = isZip;
      this.file = zipFile;
    }

  }
}
