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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.html.simpleparser.HTMLWorker;
import com.itextpdf.text.pdf.PdfWriter;


/**
 */
public class GetImagesAsPdf extends AbstractVeloWebScript {
  protected Log logger = LogFactory.getLog(this.getClass());

  public static final String PARAM_SNAPSHOT_UUIDS = "snapshotUuids";
  public static final String PARAM_PDF_FILENAME = "pdfFileName";
  public static final String PARAM_DESTINATION_DIR_UUID = "destinationUuid";
  
  /**
   * Method executeImpl.
   * @param req WebScriptRequest
   * @param res WebScriptResponse
   * @param requestContent File
   * @throws Exception
   */
  @Override
  protected Object executeImpl(WebScriptRequest req, WebScriptResponse res, File requestContent) throws Exception {

    String imageUuids = req.getParameter(PARAM_SNAPSHOT_UUIDS);
    String pdfFileName = req.getParameter(PARAM_PDF_FILENAME);
    String destinationUuid = req.getParameter(PARAM_DESTINATION_DIR_UUID);
    getImagesAsPdf(imageUuids, pdfFileName, destinationUuid);
    return null;
  }

  /**
   * Method getImagesAsPdf.
   * @param imageUuids String
   * @param pdfFileName String
   * @param destinationUuid String
   */
  public void getImagesAsPdf(String imageUuids, String pdfFileName, String destinationUuid) {
    String[] uuids = imageUuids.split(",");

    try {
      StringBuffer content = new StringBuffer();
      content.append("<html><body>");// <head><style> .spacer { border-bottom: 1px solid black; margin: 10px 0px; padding: 10px 0px; } </style></head>
      for (String uuid : uuids) {
        NodeRef imgNodeRef = NodeUtils.getNodeByUuid(uuid);
        if (nodeService.exists(imgNodeRef)) {//in case some bad uuid gets passed in, don't fail the whole pdf?
          // TODO: get the mimetype property - don't try to re-guess the mimetype, which is slow
          String mimetype = mimetypeService.guessMimetype((String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_NAME));
          if (isImage(mimetype)) {
            // all I want is the real path on disc to the file so that I can reference it in my html, hopefully this isn't a naughty thing to do...
            FileContentReader contentReader = (FileContentReader) this.contentService.getReader(imgNodeRef, ContentModel.PROP_CONTENT);
            try {
              String imageFilePath = contentReader.getFile().getCanonicalPath();
              // <img border="0" src="C:/projects/CSI/pdfs/sliceY.png" width="450" />
              content.append("<img border=\"0\" src=\"" + imageFilePath + "\" width=\"450\"  />");
              // <p>Norwegian Mountain Trip</p>
              // this will be invalide html if the description has any chars like / > etc.
              String description = (String) nodeService.getProperty(imgNodeRef, ContentModel.PROP_DESCRIPTION);
              if (description != null && !description.isEmpty()) {
                content.append("<p>" + description + "</p>");
              }
              // content.append("<hr/>"); //cannot get a horizontal line either way :( removing for now
              // content.append("<div class=\"spacer\"></div>");
              // I know this tag doesn't make sense out of a <p> but it seems to prevent the text from overlapping the images in the pdf
              content.append("<br/>");
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
      content.append("</body></html>");
      // next convert the html to pdf
      File pdfFile = convertHtmlToPdf(content.toString());

      NodeRef destinationNode = NodeUtils.getNodeByUuid(destinationUuid);
      NodeUtils.createFile(destinationNode, pdfFileName, pdfFile, nodeService, contentService, mimetypeService);

    } catch (ContentIOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }    
  }

  /**
   * Method convertHtmlToPdf.
   * @param html String
   * @return File
   * @throws Exception
   */
  private File convertHtmlToPdf(String html) throws Exception {
    File tempPdfFile = File.createTempFile("export", ".pdf");
    HashMap<String, Object> providers = new HashMap<String, Object>();
    providers.put(HTMLWorker.FONT_PROVIDER, new MyFontFactory());
    Document document = new Document();
    PdfWriter.getInstance(document, new FileOutputStream(tempPdfFile));
    document.open();
    List<Element> objects = HTMLWorker.parseToList(new StringReader(html), null, providers);
    for (Element element : objects) {
      document.add(element);
    }
    document.close();
    return tempPdfFile;
  }

  /**
   * Method isImage.
   * @param mimetype String
   * @return boolean
   */
  private boolean isImage(String mimetype) {
    if (mimetype.startsWith("image")) {
      return true;
    }
    return false;
  }

  /**
   * Inner class implementing the FontProvider class. This is needed if you want to select the correct fonts.
   * @version $Revision: 1.0 $
   */
  public static class MyFontFactory implements FontProvider {
    /**
     * Method getFont.
     * @param fontname String
     * @param encoding String
     * @param embedded boolean
     * @param size float
     * @param style int
     * @param color BaseColor
     * @return Font
     * @see com.itextpdf.text.FontProvider#getFont(String, String, boolean, float, int, BaseColor)
     */
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, BaseColor color) {
      return new Font(FontFamily.TIMES_ROMAN, size, style, color);
    }

    /**
     * Method isRegistered.
     * @param fontname String
     * @return boolean
     * @see com.itextpdf.text.FontProvider#isRegistered(String)
     */
    public boolean isRegistered(String fontname) {
      return false;
    }
  }

}
