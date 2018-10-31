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


import gov.pnnl.cat.util.ZipUtils;
import gov.pnnl.velo.util.LocalFileSystemExporter;
import gov.pnnl.velo.util.WikiUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.view.ExporterCrawlerParameters;
import org.alfresco.service.cmr.view.ExporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.FileCopyUtils;


/**
 * Get multiple files in one call and concatenate them all to the same output stream
 * so downloads are accelerated.  For now we assume we always want the current
 * version of each file
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class GetFiles extends AbstractVeloWebScript {
  private ExporterService exporterService;
  
  /**
   * Method setExporterService.
   * @param exporterService ExporterService
   */
  public void setExporterService(ExporterService exporterService) {
    this.exporterService = exporterService;
  }

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
    
	//Check whether single or multiple files to get
	String format = req.getParameter("format");
	String filecontent = req.getParameter("file_content");
	BufferedReader reader = null;
    try {
      FileReader fileReader = new FileReader(requestContent);
      reader = new BufferedReader(fileReader);
      if(format!=null && format.equals("zip"))
     	 getZipFile(reader, res,filecontent); 
      else	  
        getFiles(reader, res);
    } finally {
      if(reader != null) {
        try {reader.close();} catch(Throwable e){}
      }
    }
    return null;
  }
  
  /**
   * Method getZipFile.
   * @param reader BufferedReader
   * @param res WebScriptResponse
   * @param filecontent String
   * @throws Exception
   */
  private void getZipFile(BufferedReader reader, WebScriptResponse res, String filecontent) throws Exception {
	// TODO Auto-generated method stub
	 /*
	  * /**
	   * 1. create tmp folder
	   * 2. loop through request and get all files....copy code
	   * 3. for each path convert that into alfresco node.
	   * 4. if node is type file then copy it to tmp folder
	   * 5. else export it.using exporter service
	   * 6. zip tmp folder using ziputil.createZip  
	   * 7. write zip file to output stream      
	   */ 
	  File tempDir = File.createTempFile("velo_", "_zip");
      tempDir.delete();
      tempDir.mkdir();
      
      try
      {
    	  if(filecontent != null)
          {
        	  // file content comes from parameter , not in body
        	  List lines = IOUtils.readLines(new StringReader(filecontent));
        	  for(int linenumber=0;linenumber < lines.size(); linenumber++)
        		  getFileFolder(res,(String)lines.get(linenumber),tempDir);
          }else
          {		
        	  	// file is passed as POST body
        	     String line;
        	      logger.debug("Trying to read request body");
        	      while ( (line = reader.readLine()) != null) 
        	    	  getFileFolder(res, line, tempDir);
          }
      }catch(Exception e)
      {
    	  logger.debug("Exception in getFileFolder method. Check input path(s)");
      }
      
      // Now zip this dir
      File tempZipDir = File.createTempFile("velo", "_serverziptmp");
      tempZipDir.delete();
      tempZipDir.mkdir();
      ZipUtils.createZip(tempDir.getAbsolutePath(),tempZipDir.getAbsolutePath()+"/velozip.zip");
      ByteArrayInputStream is = new ByteArrayInputStream(IOUtils.toByteArray(new FileInputStream(new File(tempZipDir+"/velozip.zip"))));
      
      try{//write it to output stream
    	  FileCopyUtils.copy(is, res.getOutputStream());
      }
      finally{
      is.close();
      res.getOutputStream().flush();
      res.getOutputStream().close();
      }
}
  /**
   * Method getFileFolder.
   * @param res WebScriptResponse
   * @param line String
   * @param tempDir File
   * @throws FileNotFoundException
   * @throws ContentIOException
   * @throws IOException
   */
  private void getFileFolder(WebScriptResponse res, String line, File tempDir) throws FileNotFoundException, ContentIOException, IOException {


      String wikiPath = line.trim();
      if(!wikiPath.isEmpty()) {
    	  String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
    	  NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
    	  QName contentPropQName = ContentModel.PROP_CONTENT;
    	  ContentReader contentreader = contentService.getReader(nodeRef, contentPropQName);

    	  if(contentreader != null) {
    	      res.setContentType(contentreader.getMimetype());
    	      res.setContentEncoding(contentreader.getEncoding());
    	      String fileName = getFileName(wikiPath);
    	      File opFile = new File(tempDir,fileName);
    	      FileCopyUtils.copy(contentreader.getContentInputStream(), new BufferedOutputStream(new FileOutputStream(opFile)));
    	    } else {
    	      //Export it using exporter service
    	      ExporterCrawlerParameters params = new ExporterCrawlerParameters();
    	      params.setCrawlChildNodes(true);
    	      params.setCrawlSelf(true);
    	      params.setCrawlAssociations(true);
    	      params.setCrawlContent(true);
    	      params.setExportFrom(new Location(nodeRef));
    	     
    	      this.exporterService.exportView(new LocalFileSystemExporter(nodeService, contentService, tempDir, null), params, null);
    	      
    	    }
      }else{
    	  logger.debug("Empty Wiki Path Passed");
      }
  
	
}

/**
 * Method getFileName.
 * @param wikiPath String
 * @return String
 */
public String getFileName(String wikiPath) {
	    // Get the file name
	    if(wikiPath.endsWith("/")) {
	      wikiPath = wikiPath.substring(0, wikiPath.length() - 1);
	    }
	    int lastSlash = wikiPath.lastIndexOf('/');
	    String fileName = wikiPath.substring(lastSlash + 1);

	    return fileName;

	  }
/**
 */
private class FileInfo {
    
    NodeRef nodeRef;
    String wikiPath;
    
    /**
     * Constructor for FileInfo.
     * @param nodeRef NodeRef
     * @param wikiPath String
     */
    public FileInfo(NodeRef nodeRef, String wikiPath) {
      super();
      this.nodeRef = nodeRef;
      this.wikiPath = wikiPath;
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
    while ( (line = reader.readLine()) != null) {
      String wikiPath = line.trim();
      if(!wikiPath.isEmpty()) {
        String alfrescoPath = WikiUtils.getAlfrescoNamePath(wikiPath);
        
        // Find the parent node from the path (will throw exception if node does not exist)
        NodeRef nodeRef = WikiUtils.getNodeByName(alfrescoPath, nodeService);
        files.add(new FileInfo(nodeRef, wikiPath));
      }
    }   
    logger.debug("Done reading request body");
    
    OutputStream out = new BufferedOutputStream(res.getOutputStream());
    for(FileInfo fileInfo : files) {
      byte[] data = fileInfo.wikiPath.getBytes();
      out.write(new String(data.length + ";").getBytes());
      out.write(data);

      FileContentReader contentReader = (FileContentReader)contentService.getReader(fileInfo.nodeRef, ContentModel.PROP_CONTENT);
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
    out.write('!');
    out.flush();
    out.close();
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
  
}
  
