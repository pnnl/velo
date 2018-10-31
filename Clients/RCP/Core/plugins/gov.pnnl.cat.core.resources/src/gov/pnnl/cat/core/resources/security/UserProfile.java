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
package gov.pnnl.cat.core.resources.security;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.util.VeloConstants;

/**
 */
public class UserProfile extends Profile{
  
  /**
   * Method getEmail.
   * @return String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getEmail()
   */
  public String getEmail() {
    return (String)properties.get(VeloConstants.PROP_USER_EMAIL);
  }

  /**
   * Method setEmail.
   * @param email String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setEmail(String)
   */
  public void setEmail(String email) {
    properties.put(VeloConstants.PROP_USER_EMAIL,email);
  }
  
  /**
   * Method getHomeFolder.
   * @return CmsPath
   * @see gov.pnnl.cat.core.resources.security.IProfilable#getHomeFolder()
   */
  public CmsPath getHomeFolder() {
    if (this.homeFolder == null) {
      String homeFolderStr = (String)properties.get(VeloConstants.PROP_USER_HOMEFOLDER);
      // could be a uuid if current user doesn't have permissions to see this home folder
      if (homeFolderStr != null && ! homeFolderStr.startsWith("workspace://SpacesStore/")) { 
        this.homeFolder = new CmsPath(homeFolderStr);
      }
    }
    return this.homeFolder;
  }
  /**
   * Method setHomeFolderProperty.
   * @param folderPath String
   * @see gov.pnnl.cat.core.resources.security.IProfilable#setHomeFolderProperty(String)
   */
  public void setHomeFolderProperty(String folderPath){
    setProperty(VeloConstants.PROP_USER_HOMEFOLDER, folderPath);
  }
 
}
