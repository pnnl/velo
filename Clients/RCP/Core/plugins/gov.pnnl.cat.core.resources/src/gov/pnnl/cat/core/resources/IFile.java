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
package gov.pnnl.cat.core.resources;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 */
public interface IFile extends IResource {
  
  // attachment mode for HTTP downloads
  public static final String ATTACH = "attach";
  public static final String DIRECT = "direct";


  /**
   * WebDAV URLs will NOT have an authentication ticket.  WebDAV
   * URLs can NOT point to transformed content - they only work
   * on the original document format.
   * @return URL
   *  */
  public URL getWebdavUrl();
  
  /**
   * Http URLs will have an authentication ticket that will last
   * until the CAT session has expired.
  
  
   * @return URL
   *  */
  public URL getHttpUrl();

  /**
   * Http URLs will have an authentication ticket that will last
   * until the CAT session has expired.
   * @param attachmentMode Should the contents be returned directly or as a file attachment
   * @return URL
   *  */
  public URL getHttpUrl(String attachmentMode);
  
  /**
   * Return the original content of this file as an InputStream
   * @return InputStream
   * 
   */
  public InputStream getContent();  
  
  /**
   * Convenience method.  Return the raw text transformation of this file.
  
   * @return Null if the text transform failed * 
   */
  public InputStream getContentAsText();

  /**
   * Convenience method.  Returns null if no text transform exists
   * @return TransformData
   *  */
  public TransformData getTextTransform();
    
  /**
   * Get the mimetype of this file
   * @return String
   */
  public String getMimetype();
  
  /**
   * Set the content of this file
   * @param content
   *  */
  public void setContent(String content);

  /**
   * Get all the transforms available for this node (including the 
   * raw text).
   * @return
   * 
   */
  public Map<String, TransformData> getTransforms();
  
  /**
   * Get the size of the file
   * @return long
   *  */
  public long getSize();

  /**
   * Shortcut method to quickly get the file extension.
   * Adding this to be compatible with Eclipse search code.
  
   * @return String
   */
  public String getFileExtension();
}
