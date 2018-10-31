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

import java.io.Serializable;

/**
 * Acess Control Entity
 * Derived from Alfresco axis web services
 * Used to pass permissions back and forth to CAT web scripts via JSON
 * @version $Revision: 1.0 $
 */

public class ACE  implements Serializable {
  
  public static final String PERMISSION_COORDINATOR = "Coordinator";
  public static final String PERMISSION_COLLABORATOR = "Collaborator";
  public static final String PERMISSION_CONTRIBUTOR = "Contributor";
  public static final String PERMISSION_CONSUMER = "Consumer";

  public static final String ACCESS_STATUS_ALLOWED = "ALLOWED";
  public static final String ACCESS_STATUS_DENIEd = "DENIED";
  
  private static final long serialVersionUID = 1L;
  
  private String authority;
  private String permission;
  private String accessStatus;

  public ACE() {
  }

  /**
   * Constructor for ACE.
   * @param authority String
   * @param permission String
   * @param accessStatus String
   */
  public ACE(String authority, String permission, String accessStatus) {
    this.authority = authority;
    this.permission = permission;
    this.accessStatus = accessStatus;
  }


  /**
   * Gets the authority value for this ACE.
   * 
  
   * @return authority */
  public String getAuthority() {
    return authority;
  }


  /**
   * Sets the authority value for this ACE.
   * 
   * @param authority
   */
  public void setAuthority(String authority) {
    this.authority = authority;
  }


  /**
   * Gets the permission value for this ACE.
   * 
  
   * @return permission */
  public String getPermission() {
    return permission;
  }


  /**
   * Sets the permission value for this ACE.
   * 
   * @param permission
   */
  public void setPermission(String permission) {
    this.permission = permission;
  }


  /**
   * Gets the accessStatus value for this ACE.
   * 
  
   * @return accessStatus */
  public String getAccessStatus() {
    return accessStatus;
  }


  /**
   * Sets the accessStatus value for this ACE.
   * 
   * @param accessStatus
   */
  public void setAccessStatus(String accessStatus) {
    this.accessStatus = accessStatus;
  }

  private java.lang.Object __equalsCalc = null;
  /**
   * Method equals.
   * @param obj java.lang.Object
   * @return boolean
   */
  public synchronized boolean equals(java.lang.Object obj) {
    if (obj == null || !(obj instanceof ACE)) return false;
    ACE other = (ACE) obj;
    if (this == obj) return true;
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true && 
        ((this.authority==null && other.getAuthority()==null) || 
            (this.authority!=null &&
            this.authority.equals(other.getAuthority()))) &&
            ((this.permission==null && other.getPermission()==null) || 
                (this.permission!=null &&
                this.permission.equals(other.getPermission()))) &&
                ((this.accessStatus==null && other.getAccessStatus()==null) || 
                    (this.accessStatus!=null &&
                    this.accessStatus.equals(other.getAccessStatus())));
    __equalsCalc = null;
    return _equals;
  }

  private boolean __hashCodeCalc = false;
  /**
   * Method hashCode.
   * @return int
   */
  public synchronized int hashCode() {
    if (__hashCodeCalc) {
      return 0;
    }
    __hashCodeCalc = true;
    int _hashCode = 1;
    if (getAuthority() != null) {
      _hashCode += getAuthority().hashCode();
    }
    if (getPermission() != null) {
      _hashCode += getPermission().hashCode();
    }
    if (getAccessStatus() != null) {
      _hashCode += getAccessStatus().hashCode();
    }
    __hashCodeCalc = false;
    return _hashCode;
  }
}
