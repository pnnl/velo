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
package gov.pnnl.cat.web.app.servlet.pdf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.alfresco.web.app.servlet.DownloadContentServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * DMI change: overloaded Alfresco's DownloadContentServlet so we can change the output stream
 *
 * @version $Revision: 1.0 $
 */
public class PDFConverterDownloadServlet extends DownloadContentServlet
{

  private static final long serialVersionUID = 6041323008262430210L;

  private static Log logger = LogFactory.getLog(PDFConverterDownloadServlet.class);

  private static final String PDF_DOWNLOAD_URL  = "/downloadpdf/attach/{0}/{1}/{2}/{3}";
  private static final String PDF_BROWSER_URL   = "/downloadpdf/direct/{0}/{1}/{2}/{3}";


  /**
   * Helper to generate a URL to a content node for downloading content from the server.
   * The content is supplied as an HTTP1.1 attachment to the response. This generally means
   * a browser should prompt the user to save the content to specified location.
   * 
   * @param ref     NodeRef of the content node to generate URL for (cannot be null)
   * @param name    File name to return in the URL (cannot be null)
   * 
  
   * @return URL to download the content from the specified node */
  public final static String generatePDFDownloadURL(NodeRef ref, String name)
  {
    return generateUrl(PDF_DOWNLOAD_URL, ref, name);
  }

  /**
   * Helper to generate a URL to a content node for downloading content from the server.
   * The content is supplied directly in the reponse. This generally means a browser will
   * attempt to open the content directly if possible, else it will prompt to save the file.
   * 
   * @param ref     NodeRef of the content node to generate URL for (cannot be null)
   * @param name    File name to return in the URL (cannot be null)
   * 
  
   * @return URL to download the content from the specified node */
  public final static String generatePDFBrowserURL(NodeRef ref, String name)
  {
    return generateUrl(PDF_BROWSER_URL, ref, name);
  }

  /**
   * Copied from BaseDownloadContentServlet
   * @param req HttpServletRequest
   * @param res HttpServletResponse
   * @param redirectToLogin boolean
   * @throws ServletException
   * @throws IOException
   */
  protected void processDownloadRequest(HttpServletRequest req, HttpServletResponse res,
      boolean redirectToLogin)
  throws ServletException, IOException
  {  
    Log logger = getLogger();
    FileContentReader reader = null;
    File targetPDFFile = null;
    File watermarkFile = null;
    boolean pdfIsTempFile = true;

    try {

      // Get the original file 
      reader = this.getOriginalFile(req, res, redirectToLogin);

      // reader will be null if access denied
      if(reader != null) {

        // If the file is already PDF, just use that
        if(reader.getMimetype().equals(MimetypeMap.MIMETYPE_PDF)) {
          targetPDFFile = reader.getFile();
          pdfIsTempFile = false;

        } else if(reader.getMimetype().equals(MimetypeMap.MIMETYPE_HTML)){
          // open office doesn't work on html, so we have to do something else
          targetPDFFile = convertHtmlToPdf(reader.getFile());
          logger.debug("PDF file size = " + targetPDFFile.length());
          
        } else {
          
          // make sure the target file is in PDF format
          // use OpenOffice to do the conversion
          targetPDFFile = convertToPdfUsingOpenOffice(reader.getFile());
          
        }
    
        // Get the user name and download date
        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        AuthenticationComponent ac = (AuthenticationComponent)wc.getBean("AuthenticationComponent");
        NodeService nodeService = (NodeService)wc.getBean("NodeService");
        String username = ac.getCurrentUserName();
        PersonService ps = (PersonService)wc.getBean("PersonService");
        NodeRef pRef = ps.getPerson(username);
        String firstName = (String)nodeService.getProperty(pRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String)nodeService.getProperty(pRef, ContentModel.PROP_LASTNAME);
        String fullName = firstName + " " + lastName;
        Date date = new Date();
    

        // stamp with page template
        watermarkFile = stampFOUO(targetPDFFile, fullName, date);

        // add cover page and write to response
        String source = req.getParameter("source");
        String goTo = req.getParameter("goto");
        String title = req.getParameter("title");
        addCoverPageAndSend(watermarkFile, res, fullName, date, source, goTo, title);
      }

    } catch (SocketException e) {

      if (e.getMessage().contains("ClientAbortException")) {
        // the client cut the connection - our mission was accomplished apart from a little error message
        logger.error("Client aborted stream read:\n   content: " + reader);
      } else {
        throw e;
      }

    } catch (Throwable e) {

      throw new AlfrescoRuntimeException("Error during download content servlet processing: " + e.toString(), e);

    } finally {
      if(pdfIsTempFile && targetPDFFile != null) {
        targetPDFFile.delete();
      }
      if(watermarkFile != null) {
        watermarkFile.delete();
      }
    }      


  }

  /**
   * Method addCoverPageAndSend.
   * @param watermarkFile File
   * @param res HttpServletResponse
   * @param userName String
   * @param date Date
   * @param source String
   * @param goTo String
   * @param title String
   * @throws Exception
   */
  private void addCoverPageAndSend(File watermarkFile, HttpServletResponse res,
      String userName, Date date, String source, String goTo, String title) throws Exception {

    BufferedInputStream watermarkedPDFInstream = null;
    BufferedInputStream coverPagedPDFInstream = null;
    BufferedOutputStream coverPagedPDFOutstream = null;
    File coverpageFile = null;
    try
    {
      coverpageFile = TempFileProvider.createTempFile("coverpdf", ".pdf");

      // prepare the response
      res.setContentType(MimetypeMap.MIMETYPE_PDF);
      //res.setCharacterEncoding(reader.getEncoding()); DO WE NEED TO SET THIS?

      // Add cover page
      coverPagedPDFOutstream = new BufferedOutputStream(new FileOutputStream(coverpageFile));
      watermarkedPDFInstream = new BufferedInputStream(new FileInputStream(watermarkFile));
      PDFConcatenator concatenator = new PDFConcatenator();
      concatenator.concatenate(new InputStream[]{PDFConverterDownloadServlet.class.getResourceAsStream("/alfresco/extension/templates/CoverPage.pdf"), watermarkedPDFInstream}, coverPagedPDFOutstream);
      coverPagedPDFOutstream.close();
      watermarkedPDFInstream.close();

      // Add username and download date
      coverPagedPDFInstream = new BufferedInputStream(new FileInputStream(coverpageFile));
      CoverPageStamper usernameAndDateMarker = new CoverPageStamper(userName, date, source, goTo, title);
      usernameAndDateMarker.mark(coverPagedPDFInstream, res.getOutputStream());
      coverPagedPDFInstream.close();

    } finally {

      if(coverPagedPDFOutstream != null) {
        coverPagedPDFOutstream.close();
      }
      if(watermarkedPDFInstream != null) {
        watermarkedPDFInstream.close();
      }
      if(coverPagedPDFInstream != null) {
        coverPagedPDFInstream.close();
      }
      if(coverpageFile != null) {
        coverpageFile.delete();
      }
    }      
  }
  
  /**
   * Method convertHtmlToPdf.
   * @param sourceFile File
   * @return File
   * @throws Exception
   */
  private File convertHtmlToPdf(File sourceFile) throws Exception {
    logger.debug("converting html to pdf");
    
    // for now we don't have an alternative, so use open office converter
    // TODO: implement alternate method HERE
    return convertToPdfUsingOpenOffice(sourceFile); 
  }

  /**
   * Method convertToPdfUsingOpenOffice.
   * @param sourceFile File
   * @return File
   * @throws Exception
   */
  private File convertToPdfUsingOpenOffice(File sourceFile) throws Exception {

    logger.debug("trying to convert to pdf");
    OpenOfficeDocumentConverter converter = null;
    File targetPDFFile = null;
    logger.debug("original file size = " + sourceFile.length());
    boolean abort = false;

    try {
      targetPDFFile = TempFileProvider.createTempFile("pdftarget", ".pdf");
      converter = new OpenOfficeDocumentConverter();

      converter.connect("localhost", 8100);
      converter.convert(sourceFile.getAbsolutePath(), 
          targetPDFFile.getAbsolutePath(), 
          OpenOfficeConstants.PDF_EXPORT_FILTER);

      return targetPDFFile;

    } catch (Exception e) { 
      abort = true;
      throw e;
      
    } finally {

      if(converter != null) {
        converter.disconnect();
      }
      if(abort == true && targetPDFFile != null) {
        targetPDFFile.delete();
      }  
    }   
  }

  /**
   * Method stampFOUO.
   * @param targetPDFFile File
   * @param userName String
   * @param date Date
   * @return File
   * @throws Exception
   */
  private File stampFOUO(File targetPDFFile, String userName, Date date) throws Exception {

    File watermarkFile = null;
    BufferedInputStream pdfFileInstream = null;
    BufferedOutputStream watermarkedPDFOutstream = null;
    boolean abort = false;
    
    try {
      watermarkFile = TempFileProvider.createTempFile("waterpdf", ".pdf");

      // Stamp with FOUO
      pdfFileInstream = new BufferedInputStream(new FileInputStream(targetPDFFile));
      watermarkedPDFOutstream = new BufferedOutputStream(new FileOutputStream(watermarkFile));
      PDFWatermarker watermarker = new PDFWatermarker(userName, date);
      watermarker.watermark(pdfFileInstream, watermarkedPDFOutstream);
      pdfFileInstream.close();
      watermarkedPDFOutstream.close();

      return watermarkFile;

    } catch (Exception e) {
      abort = true;
      throw e;
      
    } finally {

      if(watermarkedPDFOutstream != null) {
        watermarkedPDFOutstream.close();
      }
      if(pdfFileInstream != null) {
        pdfFileInstream.close();
      }
      if(abort == true && watermarkFile != null) {
        watermarkFile.delete();
      }
    }   
  }

  /**
   * Gets the original file from parsing the HTTP request.  Copied from BaseDownloadContentServlet.
   * TODO: 2.1 Upgrade Check - compare code with 2.1 version
   * @param req
   * @param res
   * @param redirectToLogin
  
   * @return FileContentReader
   * @throws Exception
   */
  private FileContentReader getOriginalFile(HttpServletRequest req, HttpServletResponse res, boolean redirectToLogin) throws Exception {
    Log logger = getLogger();
    String uri = req.getRequestURI();
    FileContentReader reader = null;

    if (logger.isDebugEnabled())
    {
      String queryString = req.getQueryString();
      logger.debug("Processing URL: " + uri + 
          ((queryString != null && queryString.length() > 0) ? ("?" + queryString) : ""));
    }

    // TODO: add compression here?
    //       see http://servlets.com/jservlet2/examples/ch06/ViewResourceCompress.java for example
    //       only really needed if we don't use the built in compression of the servlet container
    uri = uri.substring(req.getContextPath().length());
    StringTokenizer t = new StringTokenizer(uri, "/");
    int tokenCount = t.countTokens();

    t.nextToken();    // skip servlet name

    // attachment mode (either 'attach' or 'direct')
    String attachToken = t.nextToken();
    boolean attachment = URL_ATTACH.equals(attachToken) || URL_ATTACH_LONG.equals(attachToken);
    
    // get or calculate the noderef and filename to download as
    NodeRef nodeRef;
    String filename;

    // do we have a path parameter instead of a NodeRef?
    String path = req.getParameter(ARG_PATH);
    if (path != null && path.length() != 0)
    {
      // process the name based path to resolve the NodeRef and the Filename element
      PathRefInfo pathInfo = resolveNamePath(getServletContext(), path); 

      nodeRef = pathInfo.NodeRef;
      filename = pathInfo.Filename;
    }
    else
    {
      // a NodeRef must have been specified if no path has been found
      if (tokenCount < 6)
      {
        throw new IllegalArgumentException("Download URL did not contain all required args: " + uri); 
      }

      // assume 'workspace' or other NodeRef based protocol for remaining URL elements
      StoreRef storeRef = new StoreRef(t.nextToken(), t.nextToken());
      String id = t.nextToken();
      // build noderef from the appropriate URL elements
      nodeRef = new NodeRef(storeRef, id);

      // filename is last remaining token
      filename = t.nextToken();
    }

    // get qualified of the property to get content from - default to ContentModel.PROP_CONTENT
    QName propertyQName = ContentModel.PROP_CONTENT;
    String property = req.getParameter(ARG_PROPERTY);
    if (property != null && property.length() != 0)
    {
      propertyQName = QName.createQName(property);
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("Found NodeRef: " + nodeRef.toString());
      logger.debug("Will use filename: " + filename);
      logger.debug("For property: " + propertyQName);
      logger.debug("With attachment mode: " + attachment);
    }

    // get the services we need to retrieve the content
    ServiceRegistry serviceRegistry = getServiceRegistry(getServletContext());
    NodeService nodeService = serviceRegistry.getNodeService();
    ContentService contentService = serviceRegistry.getContentService();
    PermissionService permissionService = serviceRegistry.getPermissionService();

    // check that the user has at least READ_CONTENT access - else redirect to the login page
    if (permissionService.hasPermission(nodeRef, PermissionService.READ_CONTENT) == AccessStatus.DENIED)
    {
      if (logger.isDebugEnabled())
        logger.debug("User does not have permissions to read content for NodeRef: " + nodeRef.toString());

      if (redirectToLogin)
      {
        if (logger.isDebugEnabled())
          logger.debug("Redirecting to login page...");

        redirectToLoginPage(req, res, getServletContext());
      }
      else
      {
        if (logger.isDebugEnabled())
          logger.debug("Returning 403 Forbidden error...");

        res.sendError(HttpServletResponse.SC_FORBIDDEN);
      }  
      return reader;
    }

    // check If-Modified-Since header and set Last-Modified header as appropriate
    Date modified = (Date)nodeService.getProperty(nodeRef, ContentModel.PROP_MODIFIED);
    long modifiedSince = req.getDateHeader("If-Modified-Since");
    if (modifiedSince > 0L)
    {
      // round the date to the ignore millisecond value which is not supplied by header
      long modDate = (modified.getTime() / 1000L) * 1000L;
      if (modDate <= modifiedSince)
      {
        res.setStatus(304);
        return reader;
      }
    }
    res.setDateHeader("Last-Modified", modified.getTime());

    if (attachment == true)
    {
      // set header based on filename - will force a Save As from the browse if it doesn't recognise it
      // this is better than the default response of the browser trying to display the contents
      res.setHeader("Content-Disposition", "attachment");
    }

    // get the content reader
    reader = (FileContentReader)contentService.getReader(nodeRef, propertyQName);

    // ensure that it is safe to use
//  reader = (FileContentReader)FileContentReader.getSafeContentReader(
//  reader,
//  Application.getMessage(req.getSession(), MSG_ERROR_CONTENT_MISSING),
//  nodeRef, reader);

    return reader;

  }
}
