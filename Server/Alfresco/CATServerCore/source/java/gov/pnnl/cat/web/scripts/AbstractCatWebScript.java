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


import gov.pnnl.cat.util.NodeUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;

/**
 * So we can handle incoming content and error messages consistently.
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public abstract class AbstractCatWebScript extends AbstractWebScript {
  protected Log logger = LogFactory.getLog(this.getClass());

  protected NodeService nodeService;
  protected ContentService contentService;
  protected SearchService searchService;
  protected NamespaceService namespaceService;
  protected MimetypeService mimetypeService;
  protected TransactionService transactionService;
  protected CopyService copyService;
  protected AuthorityService authorityService;
  protected PermissionService permissionService;
  protected OwnableService ownableService;
  protected AuthenticationComponent authenticationComponent;
  protected NodeUtils nodeUtils;
  protected DictionaryService dictionaryService;
  protected FileFolderService fileFolderService;
  protected VersionService versionService;
  protected AuthenticationService authenticationService;

  /**
   * Method execute.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @throws IOException
   * @see org.springframework.extensions.webscripts.WebScript#execute(WebScriptRequest, WebScriptResponse)
   */
  @Override
  public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
    try {
      logger.debug("Begin executing method.");
      long start = System.currentTimeMillis();

      // Set ok status by default (will be overriden if error occurs or subclass returns different
      // status
      res.setStatus(HttpServletResponse.SC_OK);

      // First save the request content to a temporary file so we can reuse it in case of a retrying transaction
      BufferedInputStream inputStream = new BufferedInputStream(req.getContent().getInputStream());
      final File requestContent = TempFileProvider.createTempFile("cat_webscript_input_", ".bin");
      FileCopyUtils.copy(inputStream, new FileOutputStream(requestContent));    

      // Wrap in a retrying transaction handler in case of db deadlock
      RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
          {
        public Object execute() throws Exception
        {

          try {
            executeImpl(req, res, requestContent);

          } catch (Exception e) {
            if(e instanceof AccessDeniedException) {
              logger.debug("access denied", e);
              res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
              // throw it back for web script framework to handle
              throw e;
            }
          }
          return null;
        }
          };
          transactionService.getRetryingTransactionHelper().doInTransaction(callback, false);

          // Now clean up our temp file
          requestContent.delete();

          long end = System.currentTimeMillis();
          long time = (end - start)/1000;
          logger.debug("Time to execute method = " + time + " seconds");

          logger.debug("End executing method.");

    } catch (Throwable e) {
      writeError(res, e);    
    } 
  }

  /**
   * Method handleError.
   * @param res WebScriptResponse
   * @param e Throwable
   */
  protected void writeError(WebScriptResponse res, Throwable e) {
    res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    writeError(res, this.getClass().getName() + " failed.", e);

    logger.error(this.getClass().getName() + " failed.", e);
  }
  
  protected void handleError(Throwable e){
    if(e instanceof RuntimeException)
    {
      throw (RuntimeException) e;
    }
    else
    {
      throw new RuntimeException(e);
    }
     
  }

  /**
   * Method writeError.
   * @param res WebScriptResponse
   * @param message String
   * @param e Throwable
   */
  protected void writeError(WebScriptResponse res, String message, Throwable e) {
    PrintStream printStream = null;
    res.setContentType(MimetypeMap.MIMETYPE_TEXT_PLAIN);

    try {
      // write the stack trace to the output stream
      printStream = new PrintStream(res.getOutputStream());
      printStream.println(message);
      e.printStackTrace(printStream);
      printStream.flush();

    } catch (Throwable t) {
      logger.error("Could not print error message to response stream.", t);

    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
  }

  protected void writeMessage(WebScriptResponse res, String message) {
    writeMessage(res, message, MimetypeMap.MIMETYPE_TEXT_PLAIN);    
  }
  
  /**
   * Method writeMessage.
   * @param res WebScriptResponse
   * @param message String
   */
  protected void writeMessage(WebScriptResponse res, String message, String mimetype) {
    PrintStream printStream = null;
    res.setContentType(mimetype);

    try {
      // write the stack trace to the output stream
      printStream = new PrintStream(res.getOutputStream());
      printStream.println(message);
      printStream.flush();

    } catch (Throwable t) {
      logger.error("Could not print message to response stream.", t);

    } finally {
      if(printStream != null) {
        printStream.close();
      }
    }
  }

  /**
   * Add this so we can report better error messages instead
   * of something like "transaction rolled back"
   * @param e

   * @return Throwable
   */
  protected Throwable getRootCause(Throwable e) {
    Throwable rootCause;
    Throwable cause = e;
    do {
      rootCause = cause;
      cause = cause.getCause();
    } while(cause != null);

    return rootCause;
  }

  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Method setNodeService.
   * @param nodeService NodeService
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setSearchService.
   * @param searchService SearchService
   */
  public void setSearchService(SearchService searchService) {
    this.searchService = searchService;
  }

  /**
   * Method setNamespaceService.
   * @param namespaceService NamespaceService
   */
  public void setNamespaceService(NamespaceService namespaceService) {
    this.namespaceService = namespaceService;
  }

  /**
   * Method setTransactionService.
   * @param transactionService TransactionService
   */
  public void setTransactionService(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  /**
   * Method setCopyService.
   * @param copyService CopyService
   */
  public void setCopyService(CopyService copyService) {
    this.copyService = copyService;
  }

  /**
   * Method setAuthorityService.
   * @param authorityService AuthorityService
   */
  public void setAuthorityService(AuthorityService authorityService) {
    this.authorityService = authorityService;
  }

  /**
   * Method setPermissionService.
   * @param permissionService PermissionService
   */
  public void setPermissionService(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  protected abstract Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception;

  /**
   * Method setOwnableService.
   * @param ownableService OwnableService
   */
  public void setOwnableService(OwnableService ownableService) {
    this.ownableService = ownableService;
  }

  /**
   * Method setAuthenticationComponent.
   * @param authenticationComponent AuthenticationComponent
   */
  public void setAuthenticationComponent(AuthenticationComponent authenticationComponent) {
    this.authenticationComponent = authenticationComponent;
  }

  /**
   * Method setNodeUtils.
   * @param nodeUtils NodeUtils
   */
  public void setNodeUtils(NodeUtils nodeUtils) {
    this.nodeUtils = nodeUtils;
  }

  /**
   * Method setDictionaryService.
   * @param dictionaryService DictionaryService
   */
  public void setDictionaryService(DictionaryService dictionaryService) {
    this.dictionaryService = dictionaryService;
  }

  /**
   * Method setFileFolderService.
   * @param fileFolderService FileFolderService
   */
  public void setFileFolderService(FileFolderService fileFolderService) {
    this.fileFolderService = fileFolderService;
  }

  /**
   * Method setVersionService.
   * @param versionService VersionService
   */
  public void setVersionService(VersionService versionService) {
    this.versionService = versionService;
  }

  /**
   * Method setAuthenticationService.
   * @param authenticationService AuthenticationService
   */
  public void setAuthenticationService(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

}
