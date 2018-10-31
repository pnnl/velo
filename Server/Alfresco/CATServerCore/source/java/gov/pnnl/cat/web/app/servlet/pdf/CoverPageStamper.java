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
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/**
 * Utility class used to insert metadata into the BKMS cover page.
 * Currently, this metadata includes:
 * full user name
 * download date
 * source
 * title
 * goto url
 * 
 * @version $Revision: 1.0 $
 */
public class CoverPageStamper {

  private DateFormat mDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ssaaa");
  private String userName;
  private Date date;
	private String source;
  private String title;
  private String goTo;
	
	/**
   *Constructor 
   * @param userName
   * @param date
   * @param source
   * @param goTo
   * @param title
  
  
	 * @throws DocumentException * @throws IOException */
	public CoverPageStamper(String userName, Date date, String source, 
      String goTo, String title) throws DocumentException, IOException {
		
    this.userName = userName;
    this.date = date;
    this.source = source;
    this.goTo = goTo;
    this.title = title;
	}

	/**
	 * Method mark.
	 * @param inputStream InputStream
	 * @param outputStream OutputStream
	 * @throws IOException
	 * @throws DocumentException
	 */
	public void mark(InputStream inputStream, OutputStream outputStream) 
		throws IOException, DocumentException {
    
    int pageNumber = 1;
		PdfReader reader = new PdfReader(inputStream);
		PdfStamper stamp = new PdfStamper(reader, outputStream);
		  
    AcroFields form = stamp.getAcroFields();
    form.setField("Goto", this.goTo);
    form.setField("Title", this.title);
    form.setField("Source", this.source);
    form.setField("User", this.userName);
    form.setField("Date", mDateFormat.format(date));
    stamp.setFormFlattening(true);
 
		stamp.close();
	}
	
	
	/**
	 * Method main.
	 * @param args String[]
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		CoverPageStamper fouoWatermarker = new CoverPageStamper("Test User", new Date(), "made up source", 
        "http://www.google.com", "test page");
		InputStream inputStream = new BufferedInputStream(new FileInputStream("src/config/alfresco/extension/templates/CoverPage.pdf"));
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("CoverPageOut.pdf"));
		fouoWatermarker.mark(inputStream, outputStream);
		
	}

}
