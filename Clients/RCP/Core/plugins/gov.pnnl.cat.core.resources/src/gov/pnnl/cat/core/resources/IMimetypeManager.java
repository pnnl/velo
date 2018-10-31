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

import java.util.List;
import java.util.Map;

/**
 * Based on Alfresco's MimetypeService interface
 * @version $Revision: 1.0 $
 */
public interface IMimetypeManager {
  
  public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
  
  /**
   * Get the extension for the specified mimetype  
   * 
   * @param mimetype a valid mimetype
  
  
   * @return Returns the default extension for the mimetype * @throws CATRuntimeException if the mimetype doesn't exist */
  public String getExtension(String mimetype);

  /**
   * Get the human readable mimetype descriptions indexed by file extension
   * 
  
   * @return the map of displays indexed by extension */
  public Map<String, String> getDisplaysByExtension();

  /**
   * Look up a mimetype by display name
   * 
  
   * @return the map of mimetypes indexed by display name */
  public Map<String, String> getMimetypesByDisplay();
  
  /**
   * Get the human readable mimetype descriptions indexed by mimetype
   *
  
   * @return the map of displays indexed by mimetype */
  public Map<String, String> getDisplaysByMimetype();

  /**
   * Get the file extensions indexed by mimetype
   * 
  
   * @return the map of extension indexed by mimetype */
  public Map<String, String> getExtensionsByMimetype();

  /**
   * Get the mimetypes indexed by extension
   * 
  
   * @return the map of mimetypes indexed by extension */
  public Map<String, String> getMimetypesByExtension();

  /**
   * Get all mimetypes
   * 
  
   * @return all mimetypes */
  public List<String> getMimetypes();

  /**
   * Provides a non-null best guess of the appropriate mimetype given a
   * filename.
   * 
   * @param filename the name of the file with an optional file extension
  
   * @return Returns the best guess mimetype or the mimetype for
   *      straight binary files if no extension could be found. */
  public String guessMimetype(String filename);
}
