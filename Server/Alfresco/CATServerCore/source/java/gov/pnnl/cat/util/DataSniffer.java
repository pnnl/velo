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
/**
 * 
 */
package gov.pnnl.cat.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.encoding.ContentCharsetFinder;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

/**
 * Detect mimetype and encoding for text files.
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class DataSniffer {
  private static final Log logger = LogFactory.getLog(DataSniffer.class);

  // TODO: inject location of file in spring bean definition
  public final static String MIMETYPE_APP_OCTET_STREAM = "application/octet-stream";
  private final static int MIMETYPE_DESC_SIZE = 4;
  private final static int MT_DESC = 0;
  private final static int MT_VAL = 1;
  private final static int MT_PATTERN = 2;
  private final static int MT_RANGE = 3;

  private static PropertiesConfiguration config;  
  private static MimetypeMap mimetypeService;
  private static Resource mimetypeFile;

  private static String defaultEncoding = "UTF-8";

  public void init() {
    // Load the regex patterns use to match text files

    try {
      File file = mimetypeFile.getFile();
      config = new PropertiesConfiguration(file);

    } catch (Throwable e) {
      logger.error("Could not load mimeDetermination.properties", e);
    }

  }

  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeMap
   */
  public void setMimetypeService(MimetypeMap mimetypeService) {
    DataSniffer.mimetypeService = mimetypeService;
  }

  /**
   * Method setMimetypeFile.
   * @param mimetypeFile Resource
   */
  public void setMimetypeFile(Resource mimetypeFile) {
    DataSniffer.mimetypeFile = mimetypeFile;
  }

  /**
   * Method sniffContentData.
   * @param fileName String
   * @param content File
   * @return ContentDataInfo
   */
  public static  ContentDataInfo sniffContentData(String fileName, File content) {
    ContentDataInfo info = null;

    // sniff 8192 bytes
    byte snippet[] = new byte[8192];
    FileInputStream inputStream = null;

    try {
      inputStream = new FileInputStream(content);
      inputStream.read(snippet);
      info = sniffContentData(fileName, snippet);

    } catch (Throwable e) {
      logger.error("Failed to reset sniff input stream.", e);
    } finally {
      if(inputStream != null) {
        IOUtils.closeQuietly(inputStream);
      }
    }
    return info;
  }

  /**
   * Will detect mimetype and encoding from the byte snippet.  Callers should provide
   * byte array of size > 5000 for best results
   * @param fileName
   * @param dataSnippet
  
   * @return ContentDataInfo
   */
  public static ContentDataInfo sniffContentData(String fileName, byte[] dataSnippet) {

    ContentDataInfo info = null;

    try {
      info = sniffContentDataInternal(fileName, dataSnippet);

    } catch (Throwable e) {
      logger.error("Failed to sniff mimetype.", e );
    }

    return info;
  }

  /**
   * See if we can further determine the mimetype if Alfresco thought it was 
   * application/octet-stream
  
   * @param fileName String
   * @param dataSnippet byte[]
   * @return ContentDataInfo
   */
  private static ContentDataInfo sniffContentDataInternal(String fileName, byte[] dataSnippet) {

    String mimetype = mimetypeService.guessMimetype(fileName);
    String encoding = defaultEncoding;

    // first see if we can detect text from binary content
    if(mimetype.equals(MIMETYPE_APP_OCTET_STREAM)) {
      // assume text
      encoding = getEncoding("text/plain", dataSnippet);
      String sniffedMimetype = sniffTextMimetype(fileName, dataSnippet, encoding);
      if(sniffedMimetype != null) {
        mimetype = sniffedMimetype;
      } else {
        encoding = defaultEncoding;
      }

    } else if(mimetypeService.isText(mimetype)) {
      encoding = getEncoding(mimetype, dataSnippet);
    }

    // For now we will only do this for text/plain txt/xml mimetypes
    if (mimetype.equals(MimetypeMap.MIMETYPE_TEXT_PLAIN) 
        || mimetype.equals(MimetypeMap.MIMETYPE_XML)) {
      long start = System.currentTimeMillis();
      // See if we have a more specific type of text, by performing a regex match on the text
      String textMimetype = findPlainTextMimetype(fileName, dataSnippet);
      if(textMimetype != null) {
        mimetype = textMimetype;
      }
      long end = System.currentTimeMillis();
      long time = (end - start) / 1000;
      logger.debug("time to sniff plain text mimetype = " + time + " seconds" );

    }


    return new ContentDataInfo(mimetype, encoding);
  }


  /**
   * Assuming we have plain text, what is the encoding of this text?
   * @param data
  
   * @param mimetype String
   * @return String
   */
  public static String getEncoding(String mimetype, byte[] data) {
    String encodingName = defaultEncoding;
    try {
      ContentCharsetFinder charsetFinder = mimetypeService.getContentCharsetFinder();
      Charset encoding = charsetFinder.getCharset(new ByteArrayInputStream(data), mimetype);
      if (encoding != null) {
        encodingName = encoding.name();
      }

    } catch (Throwable e) {
      encodingName = defaultEncoding;
    }
    return encodingName;
  }


  /**
   * Method sniffTextMimetype.
   * @param fileName String
   * @param data byte[]
   * @param encoding String
   * @return String
   */
  public static String sniffTextMimetype(String fileName, byte[] data, String encoding) {
    String mimetype = null;

    try {
      String s = new String(data, encoding);

      // We need to count the number of non-ascii characters in the string
      // If less than 5% of the characters are non-ascii, we will consider this text
      int numChars = s.length();
      int numNonAsciiChars = 0;

      for(int i = 0; i < s.length(); i++) {
        if(((int)s.charAt(i)) > 127) {
          // this is non-ascii character
          numNonAsciiChars++;
        }
      }
      // if less than 5 percent are non-ascii, we will call this text
      int percentNonAscii = (int)(((double)numNonAsciiChars)/((double)numChars) * 100);
      if(percentNonAscii <= 5) {
        mimetype = "text/plain" ;
      }

    } catch (Throwable e) {
      // ignore errors
      logger.info("Exception sniffing text for file " + fileName, e);
    }
    return mimetype;
  }


  /**
   * We know this is a plain text file, so we want to parse the file using
   * regular expressions to see if we can determine a more specific
   * text mimetype.
  
  
  
   * @param fileName String
   * @param data byte[]
   * @return String
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static String findPlainTextMimetype(String fileName, byte[] data) {
    String mimetype = null; 

    List mtProps = new ArrayList();
    Iterator keys = config.getKeys();

    while (keys.hasNext()) {
      String key = (String)keys.next();
      mtProps.add(config.getList(key));     
    }

    ListIterator mtPropIt = mtProps.listIterator();
    Boolean foundMatch = false;
    String contentStr = new String(data);

    while (mtPropIt.hasNext() && !foundMatch) {

      List mtProp = (List)mtPropIt.next();

      try {

        // A simple check to make sure the right number of values were provided
        if (mtProp.size() != MIMETYPE_DESC_SIZE) {
          throw new Exception("Invalid number of properties describing mime type");
        }

        // Get mime type property values
        String mtDesc = (String)mtProp.get(MT_DESC);
        String mtVal = (String)mtProp.get(MT_VAL);
        String mtPattern = (String)mtProp.get(MT_PATTERN);
        String mtRangeStr = (String)mtProp.get(MT_RANGE);
        Integer mtRange = Integer.parseInt(mtRangeStr);

        // Compile regex and see if there is a match
        Pattern pattern = Pattern.compile(mtPattern);
        Matcher matcher = pattern.matcher(contentStr);
        if (matcher.find()) {
          logger.debug("MimeDetermination : found mime type match for new content -> " + mtDesc + " | " + mtVal);
          foundMatch = true;
          mimetype = mtVal;
        }

      }
      catch (Exception e) {
        logger.error("Error trying to determine specific mimetype for text file : " + fileName, e);
      }         
    }
    return mimetype;
  }

  /**
   * Method getMimetypeService.
   * @return MimetypeService
   */
  public static MimetypeService getMimetypeService() {
    return mimetypeService;
  }

  /**
   */
  public static class ContentDataInfo {
    private String mimetype;
    private String encoding;

    /**
     * Constructor for ContentDataInfo.
     * @param mimetype String
     * @param encoding String
     */
    public ContentDataInfo(String mimetype, String encoding) {
      super();
      this.mimetype = mimetype;
      this.encoding = encoding;
    }

    /**
     * Method getMimetype.
     * @return String
     */
    public String getMimetype() {
      return mimetype;
    }
    /**
     * Method setMimetype.
     * @param mimetype String
     */
    public void setMimetype(String mimetype) {
      this.mimetype = mimetype;
    }
    /**
     * Method getEncoding.
     * @return String
     */
    public String getEncoding() {
      return encoding;
    }
    /**
     * Method setEncoding.
     * @param encoding String
     */
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

  }

}
