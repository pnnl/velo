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
package gov.pnnl.velo.model;

import java.util.Date;

/**
 * POJO Comment that can be serialized to JSON to pass data back and forth between
 * client and server.
 * @author d3k339
 * @version $Revision: 1.0 $
 */
public class Comment {
  
  private boolean canEdit;
  private String uuid;
  private String name;
  private String content;
  private Author author;
  private Date createdOn;
  private Date modifiedOn;
    
  /**
   * Method isCanEdit.
   * @return boolean
   */
  public boolean isCanEdit() {
    return canEdit;
  }

  /**
   * Method setCanEdit.
   * @param canEdit boolean
   */
  public void setCanEdit(boolean canEdit) {
    this.canEdit = canEdit;
  }

  /**
   * Method getUuid.
   * @return String
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Method setUuid.
   * @param uuid String
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Method getName.
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * Method setName.
   * @param name String
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Method getContent.
   * @return String
   */
  public String getContent() {
    return content;
  }

  /**
   * Method setContent.
   * @param content String
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Method getAuthor.
   * @return Author
   */
  public Author getAuthor() {
    return author;
  }

  /**
   * Method setAuthor.
   * @param author Author
   */
  public void setAuthor(Author author) {
    this.author = author;
  }

  /**
   * Method getCreatedOn.
   * @return Date
   */
  public Date getCreatedOn() {
    return createdOn;
  }

  /**
   * Method setCreatedOn.
   * @param createdOn Date
   */
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  /**
   * Method getModifiedOn.
   * @return Date
   */
  public Date getModifiedOn() {
    return modifiedOn;
  }

  /**
   * Method setModifiedOn.
   * @param modifiedOn Date
   */
  public void setModifiedOn(Date modifiedOn) {
    this.modifiedOn = modifiedOn;
  }

  /**
   */
  public static class Author {
    private String username;
    private String firstName;
    private String lastName;
    
    public Author() {
      
    }
    
    /**
     * Constructor for Author.
     * @param username String
     * @param firstName String
     * @param lastName String
     */
    public Author(String username, String firstName, String lastName) {
      super();
      this.username = username;
      this.firstName = firstName;
      this.lastName = lastName;
    }
    
    /**
     * Method getUsername.
     * @return String
     */
    public String getUsername() {
      return username;
    }
    /**
     * Method setUsername.
     * @param username String
     */
    public void setUsername(String username) {
      this.username = username;
    }
    /**
     * Method getFirstName.
     * @return String
     */
    public String getFirstName() {
      return firstName;
    }
    /**
     * Method setFirstName.
     * @param firstName String
     */
    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }
    /**
     * Method getLastName.
     * @return String
     */
    public String getLastName() {
      return lastName;
    }
    /**
     * Method setLastName.
     * @param lastName String
     */
    public void setLastName(String lastName) {
      this.lastName = lastName;
    }
    
  }

}

