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
package gov.pnnl.cat.alerting.delivery.internal;

import gov.pnnl.cat.alerting.alerts.Alert;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.util.XmlUtility;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;


/** This class implements ContentReader interface.  But since this
 * class is only utilized by the delivery channel content transformations
 * only the methods required by those transformers are implemented.
 * @author D3G574
 *
 * @version $Revision: 1.0 $
 */
public class StringContentReader implements ContentReader {

	private Alert alert;
	private String encoding;
	private String mimetype;
	private long size;
	private String content;
	
	/**
	 * Create a new object based on an existing Alert object
	 * @param alert
	 */
	public StringContentReader(Alert alert) {
		List<Event> events = alert.getEvents();
		
		String eventXML = XmlUtility.serialize(events);
		
		content = eventXML;
		this.alert = alert;
	}
	
	/**
	 * Constructor for StringContentReader.
	 * @param content String
	 */
	public StringContentReader(String content) {
		this.content = content;
		this.alert = null;
	}
	
	
	/**
	 * Method exists.
	 * @return boolean
	 * @see org.alfresco.service.cmr.repository.ContentReader#exists()
	 */
	public boolean exists() {
		return true;
	}

	/**
	 * Method getContent.
	 * @param os OutputStream
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getContent(OutputStream)
	 */
	public void getContent(OutputStream os) throws ContentIOException {
		// no implementation needed
	}

	/**
	 * Method getContent.
	 * @param file File
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getContent(File)
	 */
	public void getContent(File file) throws ContentIOException {
		// no implementation needed
	}

	/**
	 * Method getContentInputStream.
	 * @return InputStream
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getContentInputStream()
	 */
	public InputStream getContentInputStream() throws ContentIOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes());
		return bais;
	}

	/**
	 * Method getContentString.
	 * @return String
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getContentString()
	 */
	public String getContentString() throws ContentIOException {
		return content;
	}

	/**
	 * Method getContentString.
	 * @param length int
	 * @return String
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getContentString(int)
	 */
	public String getContentString(int length) throws ContentIOException {
		// TODO Auto-generated method stub
		return content.substring(0, length);
	}

	/**
	 * Method getFileChannel.
	 * @return FileChannel
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getFileChannel()
	 */
	public FileChannel getFileChannel() throws ContentIOException {
		// no implementation needed
		return null;
	}

	/**
	 * Method getLastModified.
	 * @return long
	 * @see org.alfresco.service.cmr.repository.ContentReader#getLastModified()
	 */
	public long getLastModified() {
		//no implementation needed
		return 0;
	}

	/**
	 * Method getReadableChannel.
	 * @return ReadableByteChannel
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getReadableChannel()
	 */
	public ReadableByteChannel getReadableChannel() throws ContentIOException {
		// no implementation needed
		return null;
	}

	/**
	 * Method getReader.
	 * @return ContentReader
	 * @throws ContentIOException
	 * @see org.alfresco.service.cmr.repository.ContentReader#getReader()
	 */
	public ContentReader getReader() throws ContentIOException {
		return new StringContentReader(alert);
	}

	/**
	 * Method isClosed.
	 * @return boolean
	 * @see org.alfresco.service.cmr.repository.ContentReader#isClosed()
	 */
	public boolean isClosed() {
		// no implementation needed
		return false;
	}

	/**
	 * Method addListener.
	 * @param listener ContentStreamListener
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#addListener(ContentStreamListener)
	 */
	public void addListener(ContentStreamListener listener) {
		// no implementation needed
	}

	/**
	 * Method getContentData.
	 * @return ContentData
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getContentData()
	 */
	public ContentData getContentData() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method getContentUrl.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getContentUrl()
	 */
	public String getContentUrl() {
		// no implementation needed
		return null;
	}

	/**
	 * Method getEncoding.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getEncoding()
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Method getMimetype.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * Method getSize.
	 * @return long
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getSize()
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Method isChannelOpen.
	 * @return boolean
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#isChannelOpen()
	 */
	public boolean isChannelOpen() {
		// no implemetation needed
		return false;
	}

	/**
	 * Method setEncoding.
	 * @param encoding String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#setEncoding(String)
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Method setMimetype.
	 * @param mimetype String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#setMimetype(String)
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * Method setRetryingTransactionHelper.
	 * @param helper RetryingTransactionHelper
	 */
	public void setRetryingTransactionHelper(RetryingTransactionHelper helper) {
		// no implementation needed
	}

  /**
   * Method getLocale.
   * @return Locale
   * @see org.alfresco.service.cmr.repository.ContentAccessor#getLocale()
   */
  public Locale getLocale() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Method setLocale.
   * @param arg0 Locale
   * @see org.alfresco.service.cmr.repository.ContentAccessor#setLocale(Locale)
   */
  public void setLocale(Locale arg0) {
    // TODO Auto-generated method stub
    
  }
}
