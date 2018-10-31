package gov.pnnl.velo.model;

import java.io.File;

import gov.pnnl.velo.util.VeloConstants;


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


/**
 *  This class just wraps regular Resource class and gives us
 *  easy accessor methods for the remote link properties.
 */
public class RemoteLink extends Resource {
  
  public RemoteLink(String type, String parentPath, String remoteMachine, String remoteFilePath, String linkDescription, String linkTitle) {
    super(new CmsPath(parentPath).append(new File(remoteFilePath).getName()));
    setType(type); // could be a folder or a file
    setRemoteMachine(remoteMachine);
    setRemoteFilePath(remoteFilePath);
    setLinkDescription(linkDescription);
    setLinkTitle(linkTitle);
  }
 
  public String getLinkTitle() {
    return getPropertyAsString(VeloConstants.PROP_TITLE);
  }
  
  public String getLinkDescription() {
    return getPropertyAsString(VeloConstants.PROP_DESCRIPTION);
  }
  
  public String getRemoteMachine() {
    return getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_MACHINE);
  }
  
  public String getRemoteFilePath() {
    return getPropertyAsString(VeloConstants.PROP_REMOTE_LINK_PATH);
  }

  public void setLinkTitle(String title) {
    setProperty(VeloConstants.PROP_TITLE, title);
  }
  
  public void setLinkDescription(String desc) {
    setProperty(VeloConstants.PROP_DESCRIPTION, desc);
  }
  
  public void setRemoteMachine(String host) {
    setProperty(VeloConstants.PROP_REMOTE_LINK_MACHINE, host);
  }
  
  public void setRemoteFilePath(String path) {
    setProperty(VeloConstants.PROP_REMOTE_LINK_PATH, path);
  }
}
