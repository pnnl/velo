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
package gov.pnnl.cat.ui.rcp.testdata.users;

/**
 */
public class CatUser {
  private String firstName;
  private String lastName;
  private String email;
  private String userId;
  private String phone;
  private boolean isSysAdmin;
  private boolean male;
  
  public CatUser() { }
  /**
   * Constructor for CatUser.
   * @param firstName String
   * @param lastName String
   * @param email String
   * @param userId String
   * @param phone String
   * @param isSysAdmin boolean
   * @param male boolean
   */
  public CatUser(String firstName, String lastName, String email, String userId, String phone, boolean isSysAdmin, boolean male) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.userId = userId;
    this.phone = phone;
    this.isSysAdmin = isSysAdmin;
    this.male = male;
  }
  /**
   * Method toString.
   * @return String
   */
  public String toString() {
    return "Cat User: " + firstName + " [" + (isSysAdmin ? "System Admin" : "User") + "]";
  }
  /**
   * Method isSysAdmin.
   * @return boolean
   */
  public boolean isSysAdmin() {
    return this.isSysAdmin;
  }
  /**
   * Method setSysAdmin.
   * @param isSysAdmin boolean
   */
  public void setSysAdmin(boolean isSysAdmin) {
    this.isSysAdmin = isSysAdmin;
  }
  /**
   * Method getFirstName.
   * @return String
   */
  public String getFirstName() {
    return this.firstName;
  }
  /**
   * Method setFirstName.
   * @param name String
   */
  public void setFirstName(String name) {
    this.firstName = name;
  }
  /**
   * Method getEmail.
   * @return String
   */
  public String getEmail() {
    return this.email;
  }
  /**
   * Method setEmail.
   * @param email String
   */
  public void setEmail(String email) {
    this.email = email;
  }
  /**
   * Method getLastName.
   * @return String
   */
  public String getLastName() {
    return this.lastName;
  }
  /**
   * Method setLastName.
   * @param lastName String
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  /**
   * Method getPhone.
   * @return String
   */
  public String getPhone() {
    return this.phone;
  }
  /**
   * Method setPhone.
   * @param phone String
   */
  public void setPhone(String phone) {
    this.phone = phone;
  }
  /**
   * Method getUserId.
   * @return String
   */
  public String getUserId() {
    return this.userId;
  }
  /**
   * Method setUserId.
   * @param userId String
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
  /**
   * Method isMale.
   * @return boolean
   */
  public boolean isMale() {
    return male;
  }
  /**
   * Method setMale.
   * @param male boolean
   */
  public void setMale(boolean male) {
    this.male = male;
  }
}
