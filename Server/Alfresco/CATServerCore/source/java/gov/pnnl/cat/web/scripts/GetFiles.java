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


import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ISO9075;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

/**
 * Get multiple files in one call and concatenate them all to the same output stream
 * so downloads are accelerated.  For now we assume we always want the current
 * version of each file
 * TODO: maybe we should use atom/rss format with binary base64 encoded so it is more standard
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetFiles extends AbstractCatWebScript {

  private AuditComponent auditComponent;
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @return Object
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {
    // Read the request body to get the links to create
    // Body is a file of link params, one remote link per line
    
    BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
    
      getFiles(reader, res);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }
  
  /**
   */
  private class FileInfo {
    
    NodeRef nodeRef;
    String path;
    
    /**
     * Constructor for FileInfo.
     * @param nodeRef NodeRef
     * @param path String
     */
    public FileInfo(NodeRef nodeRef, String path) {
      super();
      this.nodeRef = nodeRef;
      this.path = path;
    }
  }
  
  /**
   * Method getFiles.
   * @param reader BufferedReader
   * @param res WebScriptResponse
   * @throws Exception
   */
  public void getFiles(BufferedReader reader, WebScriptResponse res) throws Exception{
    String line;
    logger.debug("Trying to read request body");
    List<FileInfo> files = new ArrayList<FileInfo>();
    Throwable exception = null;
    while ( (line = reader.readLine()) != null) {
      String path = line.trim();
      if(!path.isEmpty()) {
        NodeRef nodeRef = NodeUtils.getNodeByName(path, nodeService);
        if(nodeRef != null) {
          files.add(new FileInfo(nodeRef, path));
        } else {
          logger.warn("Could not download file: " + path + " because it does not exist.");
        }
        //add an audit trail that this file was downloaded via RCP export by the logged in user 
        //(audit info only stored if audit.enabled and audit.velo-access.enabled are both set to true in globial props file)
        //we're storing this as an 'EXPORT' to distinguish from alfresco's 'READ' because RCP's preview pane can download
        //contents which will be auditing as a READ, but so will the export
        Map<String, Serializable> auditMap = new HashMap<String, Serializable>();
        auditMap.put("node", nodeRef);
        auditMap.put("action","EXPORT");
        auditMap.put("path", ISO9075.decode(nodeService.getPath(nodeRef).toPrefixString(namespaceService)));
        auditComponent.recordAuditValues("/"+CatConstants.ROOT_VELO_AUDITING_PATH, auditMap);
      }
    }   
    logger.debug("Done reading request body");
    
    OutputStream out = new BufferedOutputStream(res.getOutputStream());
    for(FileInfo fileInfo : files) {
      
      try {
        byte[] data = fileInfo.path.getBytes();
        out.write(new String(data.length + ";").getBytes());
        out.write(data);

        FileContentReader contentReader = (FileContentReader)contentService.getReader(fileInfo.nodeRef, ContentModel.PROP_CONTENT);
        if(contentReader == null) {
          // no content - write an empty string
          out.write(new String("0;").getBytes());
          
        } else {
          File file = contentReader.getFile();
          out.write(new String(file.length() + ";").getBytes());
          int len = -1;
          data = new byte[8096];
          BufferedInputStream fin = new BufferedInputStream(new FileInputStream(file));
          try {
            while ((len = fin.read(data)) != -1) {
              out.write(data, 0, len);
            }
          } finally {
            fin.close();
          }
        }
      } catch (Throwable e) {
        logger.error("Failed to send file: " + fileInfo.path, e);
        exception = e;
      }
      
    }
    out.write('!');
    out.flush();
    out.close();
    
    if(exception != null) {
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);  
    } 
  }
 
  
  protected void sendFile() {
//    try {
//      System.out.println("Piping...");
//      long start = System.currentTimeMillis();
//      UploadFileInfo fileInfo = getNextFile();
//      long time = System.currentTimeMillis();
//      while (fileInfo != null && monitor.isCanceled() == false) {
//        monitor.subTask("Uploading File: " + fileInfo.sourceFile.getName());
//        byte[] data = fileInfo.destinationPath.toDisplayString().getBytes();
//        out.write(new String(data.length + ";").getBytes());
//        out.write(data);
//
//        out.write(new String(fileInfo.sourceFile.length() + ";").getBytes());
//        int len = -1;
//        data = new byte[8096];
//        BufferedInputStream fin = new BufferedInputStream(new FileInputStream(fileInfo.sourceFile));
//        while ((len = fin.read(data)) != -1) {
//          out.write(data, 0, len);
//        }
//        fin.close();
//        monitor.worked(1);
//        fileInfo = getNextFile();
//      }
//      out.write('!');
//      out.flush();
//      out.close();
//      System.out.println("Done");
//    } catch (Throwable th) {
//      ex = th;
//    }

  }


  public void setAuditComponent(AuditComponent auditComponent) {
    this.auditComponent = auditComponent;
  }
  
}
  
