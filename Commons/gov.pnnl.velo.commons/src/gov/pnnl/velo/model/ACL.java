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
 * Access Control List
 * Derived from Alfresco axis web services
 * Used to pass permissions back and forth to CAT web scripts via JSON
 * @version $Revision: 1.0 $
 */

public class ACL  implements Serializable {

  private static final long serialVersionUID = 1L;
  private String nodePath;
  private boolean inheritPermissions;
  private String owner; // the owner of this node - owner gets special permissions
  private ACE[] aces;

  public ACL() {
  }

  /**
   * Constructor for ACL.
   * @param nodePath String
   * @param inheritPermissions boolean
   * @param aces ACE[]
   */
  public ACL(String nodePath, boolean inheritPermissions, ACE[] aces) {
    this.nodePath = nodePath;
    this.inheritPermissions = inheritPermissions;
    this.aces = aces;
  }
  
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * Method getNodePath.
   * @return String
   */
  public String getNodePath() {
    return nodePath;
  }

  /**
   * Method setNodePath.
   * @param nodePath String
   */
  public void setNodePath(String nodePath) {
    this.nodePath = nodePath;
  }

  /**
   * Gets the inheritPermissions value for this ACL.
   * 
  
   * @return inheritPermissions */
  public boolean isInheritPermissions() {
    return inheritPermissions;
  }

  /**
   * Sets the inheritPermissions value for this ACL.
   * 
   * @param inheritPermissions
   */
  public void setInheritPermissions(boolean inheritPermissions) {
    this.inheritPermissions = inheritPermissions;
  }

  /**
   * Gets the aces value for this ACL.
   * 
  
   * @return aces */
  public ACE[] getAces() {
    return aces;
  }


  /**
   * Sets the aces value for this ACL.
   * 
   * @param aces
   */
  public void setAces(ACE[] aces) {
    this.aces = aces;
  }


  private java.lang.Object __equalsCalc = null;
  /**
   * Method equals.
   * @param obj java.lang.Object
   * @return boolean
   */
  public synchronized boolean equals(java.lang.Object obj) {
    if (obj == null || !(obj instanceof ACL)) return false;
    ACL other = (ACL) obj;
    if (this == obj) return true;
    if (__equalsCalc != null) {
      return (__equalsCalc == obj);
    }
    __equalsCalc = obj;
    boolean _equals;
    _equals = true && 
        ((this.nodePath==null && other.nodePath==null) || 
            (this.nodePath!=null &&
            this.nodePath.equals(other.nodePath))) &&
            this.inheritPermissions == other.isInheritPermissions() &&
            ((this.aces==null && other.getAces()==null) || 
                (this.aces!=null &&
                java.util.Arrays.equals(this.aces, other.getAces())));
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
    if (getNodePath() != null) {
      _hashCode += getNodePath().hashCode();
    }
    _hashCode += (isInheritPermissions() ? Boolean.TRUE : Boolean.FALSE).hashCode();
    if (getAces() != null) {
      for (int i=0;
          i<java.lang.reflect.Array.getLength(getAces());
          i++) {
        java.lang.Object obj = java.lang.reflect.Array.get(getAces(), i);
        if (obj != null &&
            !obj.getClass().isArray()) {
          _hashCode += obj.hashCode();
        }
      }
    }
    __hashCodeCalc = false;
    return _hashCode;
  }

}
