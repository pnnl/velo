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
package gov.pnnl.cat.web.app.servlet;

import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.IOException;
import java.net.SocketException;
import java.util.Random;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationStatus;
import org.alfresco.web.app.servlet.BaseServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This servlet is based on Alfresco's DownloadContentServlet, except it adds an
 * additional parameter called mimetype.  You can request a different mimetype for
 * a document, and if a transformer exists, the transformed content is returned.
 * 
 * TODO: this servlet has not yet been tested and is not currently running on
 * the CAT server
 * 
 * TODO: if we decide to use it, need to compare with Alfresco 2.0 DownloadContentServlet
 * to make sure path and params are still valid.
 * @version $Revision: 1.0 $
 */
public class CatDownloadContentTransformerServlet extends BaseServlet{
  
  private static final long serialVersionUID = -4558907921887235966L;
  
  private static Log logger = LogFactory.getLog(CatDownloadContentTransformerServlet.class);
  
  private static final String MIMETYPE_OCTET_STREAM = "application/octet-stream";
  
  private static final String MSG_ERROR_CONTENT_MISSING = "error_content_missing";
  
  private static final String ARG_PROPERTY = "property";
  private static final String ARG_ATTACH   = "attach";
  private static final String ARG_FORMAT = "format"; //used to check for mimetype
  
  /**
   * Method doGet.
   * @param req HttpServletRequest
   * @param res HttpServletResponse
   * @throws ServletException
   * @throws IOException
   */
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
  throws ServletException, IOException
  {
    String uri = req.getRequestURI();
    
    if (logger.isDebugEnabled())
       logger.debug("Processing URL: " + uri + (req.getQueryString() != null ? ("?" + req.getQueryString()) : ""));
    
    AuthenticationStatus status = servletAuthenticate(req, res);
    if (status == AuthenticationStatus.Failure)
    {
       return;
    }
    
    uri = uri.substring(req.getContextPath().length());
    
    String mimetypeFormat = null;
    String format = req.getParameter(ARG_FORMAT); //look for "format" parameter

    if(format == null || format.length() == 0)
    {
      mimetypeFormat = "";
      throw new IllegalArgumentException("Download URL did not contain format parameter: " + uri);
    }
    else
    {
      mimetypeFormat = format; //original mimetype
      
      // mimetype without leading and trailing whitespaces and "/" removed
      String fixedMimetype = mimetypeFormat.replace("/", "");
      fixedMimetype.trim();
      
      StringTokenizer t = new StringTokenizer(uri, "/");
      if (t.countTokens() < 5)
      {
         throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
      }
      
      t.nextToken();    // skip servlet name
      
      String attachToken = t.nextToken();
      boolean attachment = attachToken.equals(ARG_ATTACH);
      
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      String id = t.nextToken();
      //String filename = t.nextToken(); // might have to use this in the future
      
      QName propertyQName = null;
      propertyQName = ContentModel.PROP_CONTENT;
      
      QName persistantPropertyQName = null;
      persistantPropertyQName = QName.createQName(propertyQName.toString()+ "-" + fixedMimetype);
      // build noderef from the appropriate URL elements
      NodeRef nodeRef = new NodeRef(storeRef, id);
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Found NodeRef: " + nodeRef.toString());
         //logger.debug("Will use filename: " + filename);
         logger.debug("For property: " + propertyQName);
         logger.debug("With attachment mode: " + attachment);
      }
      
      ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
      ContentService contentService = serviceRegistry.getContentService();
      PermissionService permissionService = serviceRegistry.getPermissionService();
      NodeService nodeService = serviceRegistry.getNodeService();
      
      try
      {
         // check that the user has at least READ_CONTENT access - else redirect to the login page
         if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
         {
            if (logger.isDebugEnabled())
               logger.debug("User does not have permissions to read content for NodeRef: " + nodeRef.toString());
            redirectToLoginPage(req, res, getServletContext());
            return;
         }
         
         if (attachment == true)
         {
            // set header based on filename - will force a Save As from the browse if it doesn't recognise it
            // this is better than the default response of the browser trying to display the contents
            res.setHeader("Content-Disposition", "attachment");
         }
         try
         { // If the node has already has mimetype
           if(nodeService.getProperty(nodeRef, persistantPropertyQName) != null)
           {
             // get the content reader
             ContentReader persistantReader = contentService.getReader(nodeRef, persistantPropertyQName);
             // ensure that it is safe
             persistantReader = FileContentReader.getSafeContentReader(
                 persistantReader,
                 Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                 nodeRef, persistantReader);
             
             String mimetype = persistantReader.getMimetype();
             res.setContentType(mimetype);

             try
             {
               // no need to transform since mimetype already exists.  Just output stream to browser
               persistantReader.getContent(res.getOutputStream());
             }
             catch (SocketException e)
             {
                if (e.getMessage().contains("ClientAbortException"))
                {
                   // the client cut the connection - our mission was accomplished apart from a little error message
                   logger.error("Client aborted stream read:\n   node: " + nodeRef + "\n   content: " + persistantReader);
                }
                else
                {
                   throw e;
                }
             }
          }
           else
           {
             // get the content reader
             ContentReader reader = contentService.getReader(nodeRef, propertyQName);
             
             // ensure that it is safe to use
             reader = FileContentReader.getSafeContentReader(
                        reader,
                        Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                        nodeRef, reader);
             
             String oldMimetype = reader.getMimetype();
             
             // try to get transformer
             ContentTransformer transformer = contentService.getTransformer(oldMimetype, mimetypeFormat);
             
             if(transformer == null)
             { // no transformer was found for the specified mimetypes
               logger.debug("The Transformer is null");
             }
             else
             {
               // create user transaction
               UserTransaction tx = null;
               ServiceRegistry service = BaseServlet.getServiceRegistry(getServletContext());
               tx = service.getTransactionService().getUserTransaction();
               tx.begin();
               
               long systemClock = System.currentTimeMillis();
               Random rnd = new Random();
               // create unique temporary file name
               String tempFileName = String.valueOf(rnd.nextLong() + systemClock);
               WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
               NodeUtils nodeUtils = (NodeUtils) wc.getBean("nodeUtils");
               
               // get parent folder to know where to create the temporary node
               NodeRef parentFolder = nodeUtils.getNodeByName(CatConstants.PATH_TEMP_DOCUMENTS);
               
               // create temporary node
               ChildAssociationRef tempNode = nodeService.createNode(
                   parentFolder, 
                   ContentModel.ASSOC_CONTAINS, 
                   QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, tempFileName),
                   ContentModel.TYPE_CONTENT);
               
               // get the propery of the temporary node
               QName tempQName = nodeService.getType(tempNode.getChildRef());
               ContentWriter tempWriter = contentService.getWriter(tempNode.getChildRef(), tempQName, true);
               
               tempWriter.setMimetype(mimetypeFormat);
               tempWriter.setEncoding(reader.getEncoding());
               transformer.transform(reader, tempWriter);
               res.setContentType(mimetypeFormat);
               tempQName = nodeService.getType(tempNode.getChildRef());
               
               // get the reader for the newly created temporary node
               ContentReader tempReader = contentService.getReader(tempNode.getChildRef(), tempQName);

               // ensure that it is safe to use
               tempReader = FileContentReader.getSafeContentReader(
                   tempReader,
                          Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
                          tempNode.getChildRef(), tempReader);
               try
               {
                 // output newly transformed file to the browser
                 tempReader.getContent(res.getOutputStream());
                 
               }
               catch (SocketException e)
               {
                  if (e.getMessage().contains("ClientAbortException"))
                  {
                     // the client cut the connection - our mission was accomplished apart from a little error message
                     logger.error("Client aborted stream read:\n   node: " + nodeRef + "\n   content: " + reader);
                  }
                  else
                  {
                     throw e;
                  }
               }
               // delete temporary node
               nodeService.deleteNode(tempNode.getChildRef());
             }
           }
         }
         catch(InvalidNodeRefException ex)
         {
           // the node did not have a persistant mimetype 
         }
      }
      catch (Throwable err)
      {
         throw new AlfrescoRuntimeException("Error during download content servlet processing: " + err.getMessage(), err);
      }
     
    }
    
  }
  
}
