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
package gov.pnnl.cat.web.app.servlet.upload;

import gov.pnnl.cat.pipeline.FileProcessingPipelineUtil;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEvent;
import gov.pnnl.cat.policy.notifiable.message.RepositoryEventList;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.DataSniffer;
import gov.pnnl.cat.util.DataSniffer.ContentDataInfo;
import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.cat.util.NotificationUtils;
import gov.pnnl.cat.util.XmlUtility;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Servlet that allows files to be uploaded much faster than via standard protocls
 * (i.e., CIFS, WebDAV, FTP).
 * 
 * It uploads multiple files in a continuous stream so that:
 * 1) network traffic gets a higher bandwith, so files upload faster
 * 2) content can be written to file at the same time Alfresco is processing transactions
 * 3) transactions can be batched, thus reducing tx commit time
 * 
 * @version $Revision: 1.0 $
 */
public class UploadServlet extends BaseServlet {

  private static final long serialVersionUID = 6900069445027527165L;

  /** an existing Ticket can be passed to most servlet for non-session based authentication */
  private static final String ARG_TICKET = "ticket";

  /** forcing guess access is available on most servlets */
  private static final String ARG_GUEST = "guest";

  // Logging
  private static Log logger = LogFactory.getLog(UploadServlet.class.toString());

  // Init parameter names
  public static final String KEY_STORE = "store";

  public static final String KEY_ROOT_PATH = "rootPath";

  public static final int BATCH_SIZE = 1000;

  public static final Object sync = new Object();

  private ServiceRegistry serviceRegistry;

  private TransactionService transactionService;

  private WebDAVHelper davHelper;

  private NotificationUtils notificationUtils;

  private NodeService nodeService;
  
  private AuthenticationService authenticationService;
    
  public static final String PARAM_ENABLE_PIPELINE = "enablePipeline";  
  public static final String PARAM_ENABLE_NOTIFICATION = "enableNotififcations";  

  
  /**
   * Initialize the servlet
   * 
   * @param config
   *          ServletConfig
  
  
   * @exception ServletException * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig) */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // Get service registry
    WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
    notificationUtils = (NotificationUtils) context.getBean("notificationUtils");
    serviceRegistry = (ServiceRegistry) context.getBean(ServiceRegistry.SERVICE_REGISTRY);

    transactionService = serviceRegistry.getTransactionService();
    authenticationService = (AuthenticationService) context.getBean("authenticationService");
    nodeService = (NodeService) context.getBean("nodeService");
    
    // Get the WebDAV helper
    davHelper = (WebDAVHelper) context.getBean("webDAVHelper");
  }

  /**
   * Class to hold all the status data for the bulk upload:
   * 1) which files had errors
   * 2) what is the parent folder to send a notification on when the job completes
   * @version $Revision: 1.0 $
   */
  protected class UploadStatus {
    private String errorMessageDetails;
    private List<String> failedFiles = new ArrayList<String>();

    private List<UploadedNodeInfo> addedNodes = new ArrayList<UploadedNodeInfo>();

    private boolean unauthorized = false;

    /**
     * Method queueNodeForNotification.
     * @param nodeInfo UploadedNodeInfo
     */
    public void queueNodeForNotification(UploadedNodeInfo nodeInfo) {
      addedNodes.add(nodeInfo);
    }

    /**
     * Method getErrorMessage.
     * @return String
     */
    public String getErrorMessage() {
      String errorMessage = null;

      if (failedFiles.size() > 0) {
        String genericMessage = "See server log for more details.";
        StringBuilder sb = new StringBuilder("Failed to upload the following files.  ");
        if(errorMessageDetails == null){
          sb.append(genericMessage);
        }else{
          sb.append(errorMessageDetails);
        }
        sb.append("\n\n");
        for (String file : failedFiles) {
          sb.append(file);
          sb.append("\n");
        }
        errorMessage = sb.toString();
      }
      return errorMessage;
    }

    public void sendJmsNotification() {

      final String username = AuthenticationUtil.getRunAsUser();

      // NotificationUtils methods must run in a tx
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {

        @Override
        public Object execute() throws Throwable {
          AuthenticationUtil.setRunAsUser(username);
          RepositoryEventList eventList = new RepositoryEventList();

          for (UploadedNodeInfo info : addedNodes) {
            NodeRef node = info.nodeRef;
            if(info.newNode) {
              RepositoryEvent event = new RepositoryEvent(RepositoryEvent.TYPE_NODE_ADDED);
              event.setNodeId(node.getId());
              event.setNodePath(notificationUtils.getNodePath(node));
              event.setEventPerpetrator(username);
              event.setEventTimestamp(System.currentTimeMillis());
              eventList.add(event);
            } else {
              RepositoryEvent contentChangedEvent = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
              contentChangedEvent.setNodeId(node.getId());
              contentChangedEvent.setNodePath(notificationUtils.getNodePath(node));
              contentChangedEvent.setEventPerpetrator(username);
              contentChangedEvent.setPropertyName(ContentModel.PROP_CONTENT.toString());
              contentChangedEvent.setPropertyValue(nodeService.getProperty(node, ContentModel.PROP_CONTENT).toString());
              contentChangedEvent.setEventTimestamp(System.currentTimeMillis());

              RepositoryEvent propChangedEvent = new RepositoryEvent(RepositoryEvent.TYPE_PROPERTY_CHANGED);
              propChangedEvent.setNodeId(node.getId());
              propChangedEvent.setNodePath(notificationUtils.getNodePath(node));
              propChangedEvent.setEventPerpetrator(username);
              propChangedEvent.setPropertyName(ContentModel.PROP_MODIFIED.toString());
              propChangedEvent.setPropertyValue(nodeService.getProperty(node, ContentModel.PROP_MODIFIED).toString());
              propChangedEvent.setEventTimestamp(System.currentTimeMillis());

              eventList.add(contentChangedEvent);
              eventList.add(propChangedEvent);
            }
          }
          notificationUtils.sendRepositoryEventList(eventList);

          return null;
        }
      };
      transactionService.getRetryingTransactionHelper().doInTransaction(callback, true);

    }

  }

  /**
   * Add all the uploaded files to Alfresco.
   * 
   * @param uploadList
   * @param httpRequestProcessor
   * @param username
  
   * @param parentFolders Map<String,NodeRef>
   * @param enablePipeline boolean
   * @param enableNotifications boolean
   * @return The node that has the shortest path (i.e., the highest in the tree) */
  protected UploadStatus processUploadedFiles(final List<UploadInfo> uploadList, Map<String, NodeRef> parentFolders,
      Thread httpRequestProcessor, final String username,
      boolean enablePipeline, boolean enableNotifications) {
    
    final ArrayList<UploadInfo> batch = new ArrayList<UploadInfo>();
    long startWhole = System.currentTimeMillis();
    UploadStatus status = new UploadStatus();

    // While there are still files to add to Alfresco (content being uploaded in separate thread)
    while (httpRequestProcessor.isAlive() || uploadList.size() > 0) {
      if (uploadList.size() > 0) {
        UploadInfo uploadInfo = uploadList.remove(0);
        batch.add(uploadInfo);

        if (batch.size() >= BATCH_SIZE) {
          processBatchUpload(batch, parentFolders, username, status, enablePipeline, enableNotifications);
        }
      } else {
        synchronized (uploadList) {
          try {
            uploadList.wait(100);
          } catch (InterruptedException e) {
          }
        }
      }
    }

    if (batch.size() > 0) {
      processBatchUpload(batch, parentFolders, username, status, enablePipeline, enableNotifications);
    }

    long endWhole = System.currentTimeMillis();
    logger.error("Total time to add all files to Alfresco took: " + (endWhole - startWhole)/1000 + " seconds ");// + " for " + filename

    return status;
  }

  /**
   * 
   * @param batch
   * @param username
  
   * @param parentFolders Map<String,NodeRef>
   * @param status UploadStatus
   * @param enablePipeline boolean
   * @param enableNotifications boolean
   */
  private void processBatchUpload(final List<UploadInfo> batch, final Map<String, NodeRef> parentFolders, final String username, final UploadStatus status, 
      final boolean enablePipeline, final boolean enableNotifications) {

    try {
      // Insert the whole batch to Alfresco in a single tx
      RetryingTransactionCallback<List<UploadedNodeInfo>> callback = new RetryingTransactionCallback<List<UploadedNodeInfo>>() {

        @Override
        public List<UploadedNodeInfo> execute() throws Throwable {
          List<UploadedNodeInfo> addedNodes = new ArrayList<UploadedNodeInfo>();
          
          AuthenticationUtil.setRunAsUser(username);
          notificationUtils.setNotificationsEnabledForThisTransaction(false);

          // disable pipeline
          if(!enablePipeline) {
            FileProcessingPipelineUtil.disablePiplineForCurrentTransaction();
          }
          
          for (UploadInfo uInfo : batch) {
            try {
              processFile(uInfo, addedNodes, parentFolders);

            } catch (Throwable e) {

              Throwable retryThrowable = RetryingTransactionHelper.extractRetryCause(e);
              if (retryThrowable != null) {
                // The transaction will be retrying on this, so there's no need for the
                // error logging that would normally happen
                throw e;
              } else {
                if (e instanceof AccessDeniedException) {
                  status.unauthorized = true;
                }
                status.failedFiles.add(uInfo.repositoryPath);
                status.errorMessageDetails = e.getMessage();
                logger.error("Failed to upload file: " + uInfo.repositoryPath, e);
              }
            }
          }
          return addedNodes;
        }
      };
      List<UploadedNodeInfo> addedNodes = transactionService.getRetryingTransactionHelper().doInTransaction(callback);

      if(enableNotifications) {
        for (UploadedNodeInfo nodeInfo : addedNodes) {
          status.queueNodeForNotification(nodeInfo);
        }
      }

      batch.clear();

    } catch (Throwable e) {
      status.failedFiles.add("An entire batch of size " + BATCH_SIZE + " failed to upload because of a transaction commit error.");
      logger.error("Error uploading file batch of size:" + BATCH_SIZE, e);
    }

  }

  /**
   * Method processFile.
   * @param uploadInfo UploadInfo
   * @param addedNodes List<UploadedNodeInfo>
   * @param parentFolders Map<String,NodeRef>
   */
  private void processFile(UploadInfo uploadInfo, List<UploadedNodeInfo> addedNodes, Map<String, NodeRef> parentFolders) {
    String[] paths = this.davHelper.splitPath(uploadInfo.repositoryPath);

    String parentFolderPath = CatConstants.PATH_COMPANY_HOME + paths[0];
    String fileName = paths[1];
    NodeRef destFolder = parentFolders.get(parentFolderPath);
    
    if(destFolder == null) {
      destFolder = NodeUtils.getNodeByName(parentFolderPath, nodeService);
      parentFolders.put(parentFolderPath, destFolder);
    }

    NodeRef node = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS, fileName);
    
    boolean newNode = true;

    // case where the node doesn't already exist:
    if (node == null) {
      
      Map<QName, Serializable> contentProps = new HashMap<QName, Serializable>();
      contentProps.put(ContentModel.PROP_NAME, fileName);
      contentProps.put(ContentModel.PROP_CONTENT, uploadInfo.contentData);
      
      // create content node
      ChildAssociationRef association = nodeService.createNode(destFolder, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, paths[1]), ContentModel.TYPE_CONTENT, contentProps);
      node = association.getChildRef();
   
    } else {
      newNode = false;
      // if the node is a link, replace it's source's content instead of THIS node
      if (nodeService.getType(node).equals(ContentModel.TYPE_LINK)) {
        node = (NodeRef) nodeService.getProperty(node, ContentModel.PROP_LINK_DESTINATION);
      } else if (nodeService.getType(node).equals(ContentModel.TYPE_FOLDER)) {
        // make sure that we are not trying to use a folder
        // TODO use dictionary service to check for subtypes of folders as well as cm:folder
        throw new RuntimeException("UploadServlet can only process files, not folders.");
      }
      nodeService.setProperty(node, ContentModel.PROP_CONTENT, uploadInfo.contentData);
    }

    UploadedNodeInfo nodeInfo = new UploadedNodeInfo();
    nodeInfo.newNode = newNode;
    nodeInfo.nodeRef = node;
    addedNodes.add(nodeInfo);
  }

  /**
   * Method servletAuthenticate.
   * @param req HttpServletRequest
   * @param res HttpServletResponse
   * @param redirectToLoginPage boolean
   * @return AuthenticationStatus
   * @throws IOException
   */
  @Override
  public AuthenticationStatus servletAuthenticate(HttpServletRequest req, HttpServletResponse res,
      boolean redirectToLoginPage) throws IOException {
    
    AuthenticationStatus status;
    // Get the authorization header    
    String authHdr = req.getHeader("Authorization");

    // see if a ticket or a force Guest parameter has been supplied
    String ticket = req.getParameter(ARG_TICKET);

    if (ticket != null && ticket.length() != 0)
    {
      status = AuthenticationHelper.authenticate(getServletContext(), req, res, ticket);
    }
    else if ( authHdr != null && authHdr.length() > 5 && authHdr.substring(0,5).equalsIgnoreCase("BASIC"))
    {
      // Basic authentication details present
      String basicAuth = new String(Base64.decodeBase64(authHdr.substring(5).getBytes()));

      // Split the username and password

      String username = null;
      String password = null;

      int pos = basicAuth.indexOf(":");
      if ( pos != -1)
      {
        username = basicAuth.substring(0, pos);
        password = basicAuth.substring(pos + 1);
      }
      else
      {
        username = basicAuth;
        password = "";
      }

      // Authenticate the user
      try {
        authenticationService.authenticate(username, password.toCharArray());
        status= AuthenticationStatus.Success;
        
      } catch (AuthenticationException e) {
        status = AuthenticationStatus.Failure;
      }
    }

    else
    {
      boolean forceGuest = false;
      String guest = req.getParameter(ARG_GUEST);
      if (guest != null)
      {
        forceGuest = Boolean.parseBoolean(guest);
      }
      status = AuthenticationHelper.authenticate(getServletContext(), req, res, forceGuest);
    }
    if (status == AuthenticationStatus.Failure && redirectToLoginPage)
    {
      // authentication failed - now need to display the login page to the user, if asked to
      redirectToLoginPage(req, res, getServletContext());
    }

    return status;
  }

  /**
   * 
   * @param req HttpServletRequest
   * @param res HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

    // check if authenticated
    // NOTE: this ONLY works right now if you pass the ticket in the URL. Username/password authentication does not work unless we
    // add a servlet filter. The WevDAV AuthenticationFilter DOES NOT work with UploadServlet because authentication failures force
    // a retry from the client, which is not allowed when streaming the request body.
    AuthenticationStatus authStatus = servletAuthenticate(req, res, false);
    if (authStatus == AuthenticationStatus.Failure || authStatus == AuthenticationStatus.Guest) {
      res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    // Get the current user from the authentication context
    String currentUser = AuthenticationUtil.getRunAsUser();

    // Launch a separate thread to read all incoming files coming from the HttpRequest and write
    // them to the Alfresco content store (outside a tx)
    List<UploadInfo> uploadList = Collections.synchronizedList(new ArrayList<UploadInfo>());
    HttpRequestProcessor requestProcessor = new HttpRequestProcessor(uploadList, req);
    requestProcessor.setName("Upload Thread");
    requestProcessor.start();
    
    // Get parameters, used to disable things for performance reasons
    String enablePipelineParam = req.getParameter(PARAM_ENABLE_PIPELINE);
    String enableNotificationsParam = req.getParameter(PARAM_ENABLE_NOTIFICATION);
    boolean enablePipeline = true;
    boolean enableNotifications = true;
    if(enablePipelineParam != null) {
      enablePipeline = Boolean.valueOf(enablePipelineParam);
    }
    if(enableNotificationsParam != null) {
      enableNotifications = Boolean.valueOf(enableNotificationsParam);
    }

    // In the current thread, save all uploaded files to Alfresco
    // Cache the parent folders so we don't have to look them up every time for each node
    Map<String, NodeRef> parentFolders = new HashMap<String, NodeRef>();
    UploadStatus status = processUploadedFiles(uploadList, parentFolders, requestProcessor, currentUser, enablePipeline, enableNotifications);

    // Handle error messages
    int responseCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    if (status.unauthorized) {
      responseCode = HttpServletResponse.SC_UNAUTHORIZED;
    }

    String errorMessage = status.getErrorMessage();
    if (errorMessage != null) {
      res.sendError(responseCode, errorMessage);
    }

    // Notify parent folder that the job has completed
    if(enableNotifications) {
      status.sendJmsNotification();
    }
    
    // added by Dave Gillen. Return a list of uploaded uuids to the caller so we can junit test the upload process
    // only execute this code if the client asks for it
    if (req.getQueryString() != null && req.getQueryString().contains("output=xstream")) {
      // return a list of uuids that were just added so our client knows what files were uploaded
      List<String> uuids = new ArrayList<String>();
      for (UploadedNodeInfo nodeInfo : status.addedNodes) {
        uuids.add(nodeInfo.nodeRef.getId());
      }

      XmlUtility.serializeToWriter(uuids, res.getWriter());
    }

  }

  /**
   * Method createFolder.
   * @param folder File
   */
  private void createFolder(File folder) {
    if (folder.exists() == false) {
      synchronized (sync) {
        folder.mkdirs();
      }
    }
  }

  /**
   * Method findBreak.
   * @param buf byte[]
   * @param start int
   * @param end int
   * @return int
   */
  public int findBreak(byte[] buf, int start, int end) {
    for (int i = start; i < end; i++) {
      if (buf[i] == (int) ';') {
        return i;
      }
    }
    return -1;
  }

  /**
   * Method getLength.
   * @param buf byte[]
   * @param start int
   * @return int
   */
  public int getLength(byte[] buf, int start) {
    String sLen = "";
    for (int i = start; i < buf.length; i++) {
      if (buf[i] != (int) ';') {
        if (buf[i] == (int) '!') {
          return -1;
        }
        sLen += buf[i];
      }
    }
    return Integer.parseInt(sLen);

  }

  /**
   */
  class UploadInfo {
    private ContentData contentData;
    private String repositoryPath;

    /**
     * Constructor for UploadInfo.
     * @param contentData ContentData
     * @param repositoryPath String
     */
    public UploadInfo(ContentData contentData, String repositoryPath) {
      this.contentData = contentData;
      this.repositoryPath = repositoryPath;
    }

  }

  /**
   */
  class HttpRequestProcessor extends Thread {

    private List<UploadInfo> uploadList;

    private HttpServletRequest request;

    /**
     * Constructor for HttpRequestProcessor.
     * @param uploadList List<UploadInfo>
     * @param request HttpServletRequest
     */
    public HttpRequestProcessor(List<UploadInfo> uploadList, HttpServletRequest request) {
      this.uploadList = uploadList;
      this.request = request;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      WritableByteChannel fch = null;

      try {
        String uri = request.getRequestURI();
        WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        ContentStore fcs = (ContentStore) ctx.getBean("fileContentStore");
        MimetypeService mimetypeService = (MimetypeService) ctx.getBean("mimetypeService");

        if (logger.isDebugEnabled()) {
          logger.debug("doPost Processing URL: " + uri + (request.getQueryString() != null ? ("?" + request.getQueryString()) : ""));
        }

        // Create a buffered input stream to start reading from
        BufferedInputStream gzIn = new BufferedInputStream(request.getInputStream(), 1048576);
        long total = 0;
        int readLen = -1;

        // Buffer for storing read content
        byte buffer[] = new byte[8192];

        long bytesLeft = 0;
        long dataSize = -1;
        StringBuilder content = new StringBuilder();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(32768);
        String leftover = null;
        File curFile = null;

        // ContentData properties
        String repositoryPath = null;
        String contentUrl = null;
        long fileSize = 0;
        String fileName = null;
        String mimetype = null;
        String encoding = null;

        ContentWriter writer = null;

        // While we can read in data, read it into the buffer
        while ((readLen = gzIn.read(buffer)) != -1) {
          total += readLen;
          int pos = 0;

          // While the current position in the read that we are at
          // is less than what was read the last time
          while (pos < readLen) {
            if (bytesLeft == 0) { // Read all we can
              // We read all that we are supposed to, so we need to find another
              // break in the stream.
              int end = findBreak(buffer, pos, readLen);
              if (end == -1) {
                // If the break was not found see if there is a ! to mark the
                // end of the upload.
                if (buffer[pos] == (int) '!') {
                  return;
                } else {
                  // No break was found so store the leftover bytes for
                  // use on the next read.
                  leftover = new String(buffer, pos, readLen - pos);
                  pos += readLen - pos;
                }
              } else { // Found a break
                if (leftover != null) {
                  // Leftover from last read, so use that with the new read to
                  // find the data length size.
                  dataSize = Long.parseLong(leftover + new String(buffer, pos, end - pos));

                  leftover = null;
                } else {
                  // No leftover, so just parse what is in the buffer to get the
                  // data size.
                  dataSize = Long.parseLong(new String(buffer, pos, end - pos));
                }
                if (dataSize == -1) {
                  // No data size so return.
                  return;
                }
                if (dataSize == 0) {
                  // file has a 0 byte size. move onto the next file
                  fch.close();
                  fch = null;
                  writer = null;

                  // populate the contentdata and add to list:
                  ContentData contentData = new ContentData(contentUrl, mimetype, fileSize, encoding);
                  UploadInfo fileInfo = new UploadInfo(contentData, repositoryPath);
                  uploadList.add(fileInfo);
                  synchronized (uploadList) {
                    uploadList.notifyAll();
                  }
                  
                }
                // Set the bytes left to read to the data size we just found.
                bytesLeft = dataSize;
                // Set the position in the buffer to the end of what we just read
                // plus 1
                pos = end + 1;

                // If fch is set then we must be reading the content of the file next so
                // we can set the size.
                if (fch != null) {
                  fileSize = dataSize;
                }
              }
            } else { // Still have bytes to read.
              int size = -1;
              // if bytes left is less than what we have left in the current
              // buffer that we read, use the bytes left for the read size,
              // else use what is left in the buffer for the size
              if (bytesLeft < (readLen - pos)) {
                //if we get here, then we know size must be an int because its got to be less than buffer's size
                size = (int)bytesLeft;
              } else {
                // size equals what we read in the buffer minus our current
                // position in the buffer.
                size = readLen - pos;
              }
              // if the curFile is null, that means we are reading the name of
              // the file and will store that in the content stringbuffer.
              if (writer == null) {
                content.append(new String(buffer, pos, size));
              } else { // reading file content
                // if the the remaining space in the byteBuffer is less than
                // the size we are working on, we need to write out what we
                // have and clear it before putting the next amount in.
                if (byteBuffer.remaining() < size) {
                  byteBuffer.flip();
                  fch.write(byteBuffer);
                  byteBuffer.clear();
                }
                
                // see if we need to detect mimetype
                // we only do this on the very first buffer from the file
                if(mimetype == null) {
                  try {
                    byte[] transfer = Arrays.copyOfRange(buffer, pos, pos+size);
                    ContentDataInfo info = DataSniffer.sniffContentData(fileName, transfer);
                    mimetype = info.getMimetype();
                    encoding = info.getEncoding();
                  } catch (Throwable e) {
                    e.printStackTrace();
                  }
                }
                
                byteBuffer.put(buffer, pos, size);
              }

              // modify bytes left by reducing it by the amount we
              // just read
              bytesLeft -= size;

              // if we finished reading the file, we need to do some
              // cleanup
              if (bytesLeft == 0) {
                // if curFile is null, that means we just finished
                // reading the file name information
                if (writer == null) {
                  // create a new output file based upon the name
                  // we just read.

                  writer = fcs.getWriter(ContentStore.NEW_CONTENT_CONTEXT);
                  curFile = ((FileContentWriter) writer).getFile(); // new File(writer.getContentUrl());

                  contentUrl = writer.getContentUrl();
                  repositoryPath = content.toString();
                  String[] segments = repositoryPath.split("/");
                  fileName = segments[segments.length - 1];

                  mimetype = null;
                  encoding = null;

                  // clean the buffer that stored the file name
                  content.delete(0, content.length());

                  // if the file already exist, delete it, else
                  // create it.
                  if (curFile.exists() == false) {
                    createFolder(curFile.getParentFile());
                    curFile.createNewFile();
                  }

                  // Create a RandomAccessFile to for our file content
                  // Using RAF since it can provide better performance since
                  // you can set the size.
                  fch = writer.getWritableChannel();//fout.getChannel();
                  
                } else if (fch != null) { // Just finished reading the content for a file
                  // Empty the last byte into the file and clear the buffer
                  byteBuffer.flip();
                  fch.write(byteBuffer);               
                  byteBuffer.clear();

                  //this triggers content listeners that the writing is done, 
                  //which is needed for the replicating content store
                  // Close the channel and file
                  fch.close();

                  // Set values back to null
                  fch = null;
                  writer = null;

                  ContentData contentData = new ContentData(contentUrl, mimetype, fileSize, encoding);

                  UploadInfo fileInfo = new UploadInfo(contentData, repositoryPath);
                  uploadList.add(fileInfo);
                  synchronized (uploadList) {
                    uploadList.notifyAll();
                  }
                  
                }
              }
              // Update our position in the buffer with what
              // we just processed
              pos += size;
            }

          }

        }
      } catch (Throwable err) {
        logger.error("Error during upload servlet processing: ", err);
        throw new AlfrescoRuntimeException("Error during upload servlet processing: " + err.getMessage(), err);
      } finally {
        if (fch != null) {
          try {
            fch.close();
          } catch (IOException e) {
          }
        }
      }
    }

  }
  /**
   */
  public class UploadedNodeInfo  {
    NodeRef nodeRef;
    boolean newNode = true; // whether node was new or just updated
  }
}
