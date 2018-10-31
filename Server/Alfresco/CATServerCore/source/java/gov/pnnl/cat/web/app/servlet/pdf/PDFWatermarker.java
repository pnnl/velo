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

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 */
public class PDFWatermarker {

	private BaseFont mBaseFont;
  private int mFontSize = 8;
  private Color mStampColor = Color.WHITE;
	private float mOpacityLevel = 1.0f;

  private float userNameX = 506;
  private float userNameY = 23.5f;
  private String userName;
  private Date date;
  private float dateX = 138;
  private float dateY = 23.5f;
  private DateFormat mDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ssaaa");
  
	
	/**
	 * Constructor for PDFWatermarker.
	 * @param userName String
	 * @param date Date
	 * @throws DocumentException
	 * @throws IOException
	 */
	public PDFWatermarker(String userName, Date date) throws DocumentException, IOException {
		this.mBaseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
				BaseFont.EMBEDDED);
    this.userName = userName;
    this.date = date;
	}

	/**
	 * Method watermark.
	 * @param inputStream InputStream
	 * @param outputStream OutputStream
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void watermark(InputStream inputStream, OutputStream outputStream) 
		throws IOException, DocumentException {
		PdfReader reader = new PdfReader(inputStream);
		PdfStamper stamp = new PdfStamper(reader, outputStream);

		final int numPages = reader.getNumberOfPages();
		PdfContentByte overContent;
		PdfContentByte underContent;
    
		for(int i = 1; i < numPages+1; i++) {

      // Get the header/footer template      
		  // TODO: get the page rotation, and if it is 90 degrees, then
      // use a different landscape template to stamp the header/footer
      InputStream templateInput = PDFWatermarker.class.getResourceAsStream("/alfresco/extension/templates/HeaderFooterTemplate.pdf");
      PdfReader templateReader = new PdfReader(templateInput);
      ByteArrayOutputStream templateStream = new ByteArrayOutputStream();
      PdfStamper templateStamp = new PdfStamper(templateReader, templateStream);
 
      // Fill out the user/date fields      
      AcroFields form = templateStamp.getAcroFields();
      form.setField("User", this.userName);
      form.setField("Date", mDateFormat.format(date));
      
      // Save the completed form to the byte array
      templateStamp.setFormFlattening(true);
      templateStamp.close();
      PdfReader completedForm = new PdfReader(templateStream.toByteArray());      
      
      // Underlay the template under the original content
      PdfImportedPage templatePage = stamp.getImportedPage(completedForm, 1);      
      underContent = stamp.getUnderContent(i);
      PdfGState transparentState = new PdfGState();
      transparentState.setFillOpacity(mOpacityLevel);
      underContent.setGState(transparentState);
      underContent.addTemplate(templatePage, 0, 0);
      
 
      
      // Overlay the username/date stamp
//      overContent = stamp.getOverContent(i);
//			overContent.beginText();
//
//			Rectangle pageSize = reader.getPageSizeWithRotation(i);
//			overContent.setFontAndSize(mBaseFont, mFontSize);
//			overContent.setColorFill(mStampColor);
//      overContent.showTextAligned(Element.ALIGN_LEFT, userName,
//          userNameX, pageSize.height()-userNameY, 0);
//      overContent.showTextAligned(Element.ALIGN_LEFT, mDateFormat.format(date),
//          dateX, pageSize.height()-dateY, 0);
//
//			overContent.endText();
		}
		stamp.close();
	}

	/**
	 * Method main.
	 * @param args String[]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		PDFWatermarker fouoWatermarker = new PDFWatermarker("test user", new Date());
		InputStream inputStream = new BufferedInputStream(new FileInputStream("exampleOutput/ExamplePowerpoint.pdf"));
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("watermark_monkey2.pdf"));
		fouoWatermarker.watermark(inputStream, outputStream);
		
	}

}
