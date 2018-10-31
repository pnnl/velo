package gov.pnnl.velo.model;

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

import java.util.Date;

/**
 * Holds file version information from the content repository
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class Version {

  private String wikiPath;
  private String versionLabel;
  private String creator;
  private Date creationDate;
  
  public Version() {
    
  }
  
  /**
   * Constructor for Version.
   * @param wikiPath String
   * @param versionLabel String
   * @param creator String
   * @param creationDate Date
   */
  public Version(String wikiPath, String versionLabel, String creator, Date creationDate) {
    super();
    this.wikiPath = wikiPath;
    this.versionLabel = versionLabel;
    this.creator = creator;
    this.creationDate = creationDate;
  }

  /**
   * Wiki path to the file this version was created from
  
   * @return the wikiPath */
  public String getWikiPath() {
    return wikiPath;
  }

  /**
   * @param wikiPath the wikiPath to set
   */
  public void setWikiPath(String wikiPath) {
    this.wikiPath = wikiPath;
  }

  /**
  
   * @return the versionLabel */
  public String getVersionLabel() {
    return versionLabel;
  }

  /**
   * @param versionLabel the versionLabel to set
   */
  public void setVersionLabel(String versionLabel) {
    this.versionLabel = versionLabel;
  }

  /**
  
   * @return the creator */
  public String getCreator() {
    return creator;
  }

  /**
   * @param creator the creator to set
   */
  public void setCreator(String creator) {
    this.creator = creator;
  }

  /**
  
   * @return the creationDate */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }
  
  
}
