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
package gov.pnnl.cat.ui.rcp.testdata.teams;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.ui.rcp.testdata.users.CatUser;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 */
public class CatTeam {
  private String name;

  private String description;

  private CatUser[] teamMembers;
  
  private CatTeam[] childrenTeams;
  
  private CatTeam parent;

  private ImageDescriptor logo;
  private CmsPath logoImagePath;
  
  private CmsPath homeFolderPath;
  
  /**
   * Constructor for CatTeam.
   * @param name String
   * @param description String
   * @param teamMembers CatUser[]
   * @param childrenTeams CatTeam[]
   * @param logo ImageDescriptor
   * @param logoImagePath CmsPath
   * @param homeFolderPath CmsPath
   */
  public CatTeam(String name, String description, CatUser[] teamMembers, CatTeam[] childrenTeams, ImageDescriptor logo, CmsPath logoImagePath, CmsPath homeFolderPath) {
    super();
    this.name = name;
    this.description = description;
    this.teamMembers = teamMembers;
    this.childrenTeams = childrenTeams;
    this.logo = logo;
    this.logoImagePath = logoImagePath;
    this.homeFolderPath = homeFolderPath;
  }

  /**
   * Method getDescription.
   * @return String
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Method setDescription.
   * @param description String
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Method getName.
   * @return String
   */
  public String getName() {
    return this.name;
  }

  /**
   * Method setName.
   * @param name String
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Method getTeamMembers.
   * @return CatUser[]
   */
  public CatUser[] getTeamMembers() {
    return this.teamMembers;
  }

  /**
   * Method setTeamMembers.
   * @param teamMembers CatUser[]
   */
  public void setTeamMembers(CatUser[] teamMembers) {
    this.teamMembers = teamMembers;
  }

  /**
   * Method getChildrenTeams.
   * @return CatTeam[]
   */
  public CatTeam[] getChildrenTeams() {
    return this.childrenTeams;
  }

  /**
   * Method setChildrenTeams.
   * @param childrenTeams CatTeam[]
   */
  public void setChildrenTeams(CatTeam[] childrenTeams) {
    this.childrenTeams = childrenTeams;
  }

  /**
   * Method getParent.
   * @return CatTeam
   */
  public CatTeam getParent() {
    return this.parent;
  }

  /**
   * Method setParent.
   * @param parent CatTeam
   */
  public void setParent(CatTeam parent) {
    this.parent = parent;
  }

  /**
   * Method getLogo.
   * @return ImageDescriptor
   */
  public ImageDescriptor getLogo() {
    return this.logo;
  }

  /**
   * Method setLogo.
   * @param logo ImageDescriptor
   */
  public void setLogo(ImageDescriptor logo) {
    this.logo = logo;
  }

  /**
   * Method getHomeFolderPath.
   * @return CmsPath
   */
  public CmsPath getHomeFolderPath() {
    return homeFolderPath;
  }

  /**
   * Method setHomeFolderPath.
   * @param catTeamPath CmsPath
   */
  public void setHomeFolderPath(CmsPath catTeamPath) {
    this.homeFolderPath = catTeamPath;
  }

  /**
   * Method getLogoImagePath.
   * @return CmsPath
   */
  public CmsPath getLogoImagePath() {
    return logoImagePath;
  }

  /**
   * Method setLogoImagePath.
   * @param logoImagePath CmsPath
   */
  public void setLogoImagePath(CmsPath logoImagePath) {
    this.logoImagePath = logoImagePath;
  }

}
