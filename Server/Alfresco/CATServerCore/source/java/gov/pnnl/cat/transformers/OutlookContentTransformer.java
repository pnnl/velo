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
package gov.pnnl.cat.transformers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformer2;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;
import org.apache.poi.poifs.filesystem.DocumentInputStream;

/**
 * This transformer will only work on Outlook ole2 format .msg file documents.
 *
 * @version $Revision: 1.0 $
 */
public class OutlookContentTransformer extends AbstractContentTransformer2 {

  // MimetypeMap doesn't define this mimetype, so I have my own constant here
  public static String MIMETYPE_MESSAGE = "message/rfc822";

  private static final String STREAM_PREFIX = "__substg1.0_";
  private static final int STREAM_PREFIX_LENGTH = STREAM_PREFIX.length();

  private static final QName PROP_MESSAGEBODY = 
    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "messagebody");

  private static final QName PROP_TO = 
    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "to");

  private static final QName PROP_FROM = 
    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "from");
  
  private static Log logger = LogFactory.getLog(OutlookContentTransformer.class); 

  /**
   * Windows carriage return line feed pair.
   */
  private static final String LINE_BREAK = "\r\n";

  // the to: email addresses
  private ThreadLocal<List<String>> receipientEmails = new ThreadLocal<List<String>>();


  /**
   * Only works from Outlook to text/plain
   * @param sourceMimetype String
   * @param targetMimetype String
   * @return double
   */
  public double getReliability(String sourceMimetype, String targetMimetype) {

    if (!MIMETYPE_MESSAGE.equals(sourceMimetype) || 
        !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype)) {
      // only support Outlook -> Text
      return 0.0;
    } else {
      return 1.0;
    }
  }


  /**
   * Method transformInternal.
   * @param reader ContentReader
   * @param writer ContentWriter
   * @param options TransformationOptions
   * @throws Exception
   */
  @Override
  protected void transformInternal(ContentReader reader, ContentWriter writer, 
      TransformationOptions options) throws Exception {

    System.out.println("calling transformInternal");
    if(logger.isDebugEnabled()) {
      logger.debug("calling transformInternal");
    }

    InputStream is = reader.getContentInputStream();
    OutputStream os = writer.getContentOutputStream();
    String encoding = writer.getEncoding();

    // the pieces of the message:
    final Map<QName, String> destination = new HashMap<QName, String>();


    POIFSReaderListener readerListener = new POIFSReaderListener()
    {
      int numEvents = 0;
      public void processPOIFSReaderEvent(final POIFSReaderEvent event)
      {
        numEvents++;
        logger.debug("processing event # " + numEvents);
        logger.debug("event name = " + event.getName());
        try
        {
          if (event.getName().startsWith(STREAM_PREFIX))
          {
            StreamHandler handler = new StreamHandler(event.getName(), event.getStream());
            handler.process(destination);
          }
        }
        catch (Exception ex)
        {
          throw new ContentIOException("Property set stream: " + event.getPath() + event.getName(), ex);
        }
      }
    };

    try
    {
      this.receipientEmails.set(new ArrayList<String>());

      POIFSReader poiFSReader = new POIFSReader();
      poiFSReader.registerListener(readerListener);

      try
      {
        poiFSReader.read(is);
      }
      catch (IOException err)
      {
        // probably not an Outlook format MSG - ignore for now
        logger.warn("Unable to extract meta-data from message: " + err.getMessage());
      }

      // get to addresses
//      String to = "";
//      for(String address : receipientEmails.get()) {
//        to += address + "; ";
//      }

      // write all the pieces to the output stream
      String to = destination.get(PROP_TO).trim();
      String from = destination.get(PROP_FROM).trim();
      String sent = destination.get(ContentModel.PROP_SENTDATE);
      String subject = destination.get(ContentModel.PROP_SUBJECT);
      String body = destination.get(PROP_MESSAGEBODY);
      if (body == null) {
        body = "";
      }

      writeString(os, encoding, "From: " + from);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, "To: " + to);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, "Sent: " + sent);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, "Subject: " + subject);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, LINE_BREAK);
      writeString(os, encoding, body);
    }
    finally
    {
      if (is != null)
      {
        try { is.close(); } catch (Throwable e) {}
      }
      if (os != null)
      {
        try { os.close(); } catch (Throwable e) {}
      }
    }
  }

  /**
   * Method convertExchangeAddress.
   * @param email String
   * @return String
   */
  private static String convertExchangeAddress(String email)
  {
    System.out.println("calling convertExchangeAddress");
    if (email.lastIndexOf("/CN=") == -1)
    {
      return email;
    }
    else
    {
      // found a full Exchange format To header
      return email.substring(email.lastIndexOf("/CN=") + 4);
    }
  }

  private static final String ENCODING_TEXT = "001E";
  private static final String ENCODING_BINARY = "0102";
  private static final String ENCODING_UNICODE = "001F";

  private static final String SUBSTG_MESSAGEBODY = "1000";
  private static final String SUBSTG_RECIPIENTEMAIL = "39FE";      // 7bit email address
  private static final String SUBSTG_RECIPIENTSEARCH = "300B";     // address 'search' variant
  private static final String SUBSTG_RECEIVEDEMAIL = "0076";
  private static final String SUBSTG_SENDEREMAIL = "0C1F";
  private static final String SUBSTG_DATE = "0047";
  private static final String SUBSTG_SUBJECT = "0037";
  
  // Cat added these two because they look better formatted - need to test
  private static final String SUBSTG_SENDER = "0042";
  private static final String SUBSTG_RECIPIENT = "0E04";

  /**
   * Class to handle stream types. Can process and extract specific streams.
   * @version $Revision: 1.0 $
   */
  private class StreamHandler
  {
    /**
     * Constructor for StreamHandler.
     * @param name String
     * @param stream DocumentInputStream
     */
    StreamHandler(String name, DocumentInputStream stream)
    {
      this.type = name.substring(STREAM_PREFIX_LENGTH, STREAM_PREFIX_LENGTH + 4);
      this.encoding = name.substring(STREAM_PREFIX_LENGTH + 4, STREAM_PREFIX_LENGTH + 8);
      this.stream = stream;
    }

    /**
     * Method process.
     * @param destination Map<QName,String>
     * @throws IOException
     */
    void process(final Map<QName, String> destination)
    throws IOException
    {
      if (type.equals(SUBSTG_SENDEREMAIL))
      {
        // this field isn't very readable
        destination.put(ContentModel.PROP_ORIGINATOR, convertExchangeAddress(extractText()));
      }
      else if (type.equals(SUBSTG_SENDER)) 
      {
        destination.put(PROP_FROM, extractText());
      }
      else if (type.equals(SUBSTG_RECIPIENTEMAIL))
      {
        receipientEmails.get().add(convertExchangeAddress(extractText()));
      }
      else if (type.equals(SUBSTG_RECIPIENTSEARCH))
      {
        String email = extractText(ENCODING_TEXT);
        int smptIndex = email.indexOf("SMTP:");
        if (smptIndex != -1)
        {
          /* also may be used for SUBSTG_RECIPIENTTRANSPORT = "5FF7"; 
                   with search for SMPT followed by a null char */

          // this is a secondary mechanism for encoding a receipient email address
          // the 7 bit email address may not have been set by Outlook - so this is needed instead
          // handle null character at end of string
          int endIndex = email.length();
          if (email.codePointAt(email.length() - 1) == 0)
          {
            endIndex--;
          }
          email = email.substring(smptIndex + 5, endIndex);
          receipientEmails.get().add(email);
        }
      }
      else if (type.equals(SUBSTG_RECIPIENT)) {
        destination.put(PROP_TO, extractText()); 
      }
      else if (type.equals(SUBSTG_RECEIVEDEMAIL))
      {
        destination.put(ContentModel.PROP_ADDRESSEE, convertExchangeAddress(extractText()));
      }
      else if (type.equals(SUBSTG_SUBJECT))
      {
        destination.put(ContentModel.PROP_SUBJECT, extractText());
      }
      else if (type.equals(SUBSTG_MESSAGEBODY))
      {
        destination.put(PROP_MESSAGEBODY, extractText());
      }
      else if (type.equals(SUBSTG_DATE))
      {
        // TODO: it looks like the date isn't converted correctly (maybe format is off)
        
        // the date is not "really" plain text - but it's appropriate to parse as such
        String date = extractText(ENCODING_TEXT);
        logger.debug("date:" + date);
        int valueIndex = date.indexOf("l=");
        if (valueIndex != -1)
        {
          int dateIndex = date.indexOf('-', valueIndex);
          if (dateIndex != -1)
          {
            dateIndex++;
            String strYear = date.substring(dateIndex, dateIndex + 2);
            int year = Integer.parseInt(strYear) + (2000 - 1900);
            String strMonth = date.substring(dateIndex + 2, dateIndex + 4);
            int month = Integer.parseInt(strMonth) - 1;
            String strDay = date.substring(dateIndex + 4, dateIndex + 6);
            int day = Integer.parseInt(strDay);
            String strHour = date.substring(dateIndex + 6, dateIndex + 8);
            int hour = Integer.parseInt(strHour);
            String strMinute = date.substring(dateIndex + 8, dateIndex + 10);
            int minute = Integer.parseInt(strMinute);
            String strSecond = date.substring(dateIndex + 10, dateIndex + 12);
            int second = Integer.parseInt(strSecond);
            Date date2 = new Date(year, month, day, hour, minute, second);
            int offset = date2.getTimezoneOffset();
            Date date3 = new Date(date2.getTime() - offset*60*1000);
            destination.put(ContentModel.PROP_SENTDATE, date3.toString());
          }
        } 
      } 
      else 
      {
        logger.debug("Non-saved event: " + type);
        logger.debug("value: " + extractText());
      }
    }

    /**
     * Extract the text from the stream based on the encoding
     * 
    
     * 
    
     * @return String * @throws IOException */
    private String extractText()
    throws IOException
    {
      return extractText(this.encoding);
    }

    /**
     * Extract the text from the stream based on the encoding
     * 
    
     * 
    
     * @param encoding String
     * @return String * @throws IOException */
    private String extractText(String encoding)
    throws IOException
    {
      byte[] data = new byte[stream.available()];
      stream.read(data);

      if (encoding.equals(ENCODING_TEXT) || encoding.equals(ENCODING_BINARY))
      {
        return new String(data);
      }
      else if (encoding.equals(ENCODING_UNICODE))
      {
        // convert double-byte encoding to single byte for String conversion
        byte[] b = new byte[data.length >> 1];
        for (int i=0; i<b.length; i++)
        {
          b[i] = data[i << 1];
        }
        return new String(b);
      }
      else
      {
        return new String(data);
      }
    }

    private String type;
    private String encoding;
    private DocumentInputStream stream;
  }

  /**
   * Writes the given data to the stream using the encoding specified.  If the encoding
   * is not given, the default <tt>String</tt> to <tt>byte[]</tt> conversion will be
   * used.
   * <p>
   * 
   * @param os the stream to write to
   * @param encoding the encoding to use, or null if the default encoding is acceptable
   * @param value the string to write
  
   * @throws Exception */
  protected void writeString(OutputStream os, String encoding, String value) throws Exception
  {
    if (value == null)
    {
      // nothing to do
      return;
    }
    if (value.length() == 0)
    {
      // nothing to do
      return;
    }

    byte[] bytes = null;
    if (encoding == null)
    {
      // use default encoding
      bytes = value.getBytes();
    }
    else
    {
      bytes = value.getBytes(encoding);
    }
    // write to the stream
    os.write(bytes);
    // done
  }


  /**
   * // only support Outlook -> Text
   * @param sourceMimetype String
   * @param targetMimetype String
   * @param options TransformationOptions
   * @return boolean
   * @see org.alfresco.repo.content.transform.ContentTransformer#isTransformable(String, String, TransformationOptions)
   */
  @Override
  public boolean isTransformable(String sourceMimetype, String targetMimetype, TransformationOptions options)
  {
    if (!MIMETYPE_MESSAGE.equals(sourceMimetype) || 
        !MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimetype)) {
          // only support Outlook -> Text
          return false;
      }
      else
      {
          return true;
      }
  }


}
