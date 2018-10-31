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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Locale;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentStreamListener;
import org.alfresco.service.cmr.repository.ContentWriter;

/**
 */
public class StringContentWriter implements ContentWriter {

	private String content;
	private String encoding;
	private String mimetype;
	
	/**
	 * Method getContent.
	 * @return String
	 */
	public String getContent() {
		return content;
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#getContentOutputStream()
	 */
	@Override
	public OutputStream getContentOutputStream() throws ContentIOException {
		// no implementation needed
		return null;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#getFileChannel(boolean)
	 */
	@Override
	public FileChannel getFileChannel(boolean truncate)
			throws ContentIOException {
		// no implementation needed
		return null;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#getReader()
	 */
	@Override
	public ContentReader getReader() throws ContentIOException {
		// no implementation needed
		return null;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#getWritableChannel()
	 */
	@Override
	public WritableByteChannel getWritableChannel() throws ContentIOException {
		// no implementation needed
		return null;
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#isClosed()
	 */
	@Override
	public boolean isClosed() {
		// no implementation needed
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#putContent(org.alfresco.service.cmr.repository.ContentReader)
	 */
	@Override
	public void putContent(ContentReader reader) throws ContentIOException {
		content = reader.getContentString();
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#putContent(java.io.InputStream)
	 */
	@Override
	public void putContent(InputStream is) throws ContentIOException {
		try {
			InputStreamReader isr = new InputStreamReader(is, encoding);
			fillFromReader(isr);
		} catch (Exception e) {
			throw new ContentIOException(e.getMessage(), e);
		}
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#putContent(java.io.File)
	 */
	@Override
	public void putContent(File file) throws ContentIOException {
		try {
			FileReader reader = new FileReader(file);
			fillFromReader(reader);
		} catch (Exception e) {
			throw new ContentIOException(e.getMessage(), e);
		}		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.service.cmr.repository.ContentWriter#putContent(java.lang.String)
	 */
	@Override
	public void putContent(String content) throws ContentIOException {
		this.content = content;

	}

	/**
	 * Method addListener.
	 * @param listener ContentStreamListener
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#addListener(ContentStreamListener)
	 */
	@Override
	public void addListener(ContentStreamListener listener) {
		// no implementation needed

	}

	/**
	 * Method getContentData.
	 * @return ContentData
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getContentData()
	 */
	@Override
	public ContentData getContentData() {
		// no implementation needed
		return null;
	}

	/**
	 * Method getContentUrl.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getContentUrl()
	 */
	@Override
	public String getContentUrl() {
		// no implementation needed
		return null;
	}
	
	/**
	 * Method getEncoding.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getEncoding()
	 */
	@Override
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Method getMimetype.
	 * @return String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()
	 */
	@Override
	public String getMimetype() {
		return mimetype;
	}
	
	/**
	 * Method getSize.
	 * @return long
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getSize()
	 */
	@Override
	public long getSize() {
		return (content == null ? 0 : content.length());
	}
	
	/**
	 * Method isChannelOpen.
	 * @return boolean
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#isChannelOpen()
	 */
	@Override
	public boolean isChannelOpen() {
		// no implementation needed
		return false;
	}

	/**
	 * Method setEncoding.
	 * @param encoding String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#setEncoding(String)
	 */
	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Method setMimetype.
	 * @param mimetype String
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#setMimetype(String)
	 */
	@Override
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
	 * Method fillFromReader.
	 * @param r Reader
	 * @throws IOException
	 */
	private void fillFromReader(Reader r) throws IOException {
		try {
			String line = "";
			StringBuffer contentBuffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(r);
			while ((line = reader.readLine()) != null) {
				contentBuffer.append(line).append('\n');
			}
			reader.close();
			this.content = contentBuffer.toString();
		} catch (IOException e) {
			this.content = null;
			throw e;
		}
	}

	/**
	 * Method getLocale.
	 * @return Locale
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#getLocale()
	 */
	@Override
  public Locale getLocale() {
    // TODO Auto-generated method stub
    return null;
  }

	/**
	 * Method setLocale.
	 * @param arg0 Locale
	 * @see org.alfresco.service.cmr.repository.ContentAccessor#setLocale(Locale)
	 */
	@Override
  public void setLocale(Locale arg0) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method guessMimetype.
   * @param filename String
   * @see org.alfresco.service.cmr.repository.ContentWriter#guessMimetype(String)
   */
  @Override
  public void guessMimetype(String filename) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Method guessEncoding.
   * @see org.alfresco.service.cmr.repository.ContentWriter#guessEncoding()
   */
  @Override
  public void guessEncoding() {
    // TODO Auto-generated method stub
    
  }

}
