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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

/**
 */
public class PDFConcatenator {

  
  /**
   * Concatenates PDF documents together.
   * @param inputStreams
   * @param outStream
  
  
   * @throws IOException * @throws DocumentException */
  public void concatenate(InputStream[] inputStreams, OutputStream outStream) throws IOException, DocumentException {
    int pageOffset = 0;
    int numPages = 0;
    List allBookmarks = new ArrayList();
    Document document = null;
    PdfCopy  writer = null;
    PdfReader reader;
    List bookmarks;
    PdfImportedPage page;
    PRAcroForm form;
        
    // Loop through each document in the set
    for(int i = 0; i < inputStreams.length; i++) {
      
      reader = new PdfReader(inputStreams[i]);
      reader.consolidateNamedDestinations();
      numPages = reader.getNumberOfPages();
      bookmarks = SimpleBookmark.getBookmark(reader);
      
      if (bookmarks != null) {
        if (pageOffset != 0) {
          SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
        }
        allBookmarks.addAll(bookmarks);
      }
      
      pageOffset += numPages;
      if (i == 0) {
        document = new Document(reader.getPageSizeWithRotation(1));
        writer = new PdfCopy(document, outStream);
        document.open();
      }

      for (int j = 1; j < numPages+1; j++) {
        page = writer.getImportedPage(reader, j);
        writer.addPage(page);
      }
      
      form = reader.getAcroForm();
      if(form != null) {
        writer.copyAcroForm(reader);
      }
    }
    if (allBookmarks.size() > 0) {
      writer.setOutlines(allBookmarks);
    }
    document.close();
  }

}
