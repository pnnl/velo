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
 * 
 */
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;

/**
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class VersionProcessor extends AbstractFileProcessor {
  private boolean autoVersion = false;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "Versioning";
  }

  /**
   * Method setAutoVersion.
   * @param autoVersion boolean
   */
  public void setAutoVersion(boolean autoVersion) {
    this.autoVersion = autoVersion;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {
    NodeRef nodeRef = fileInfo.getNodeToExtract();

    if(autoVersion == false ) {
      return;
    }

    // add the versionable aspect if it's not there
    if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)) {

      // Explicitly set the autoversion and initial version properties to 
      // false, so even if the default aspect definition gets changed in
      // contentModel.xml, it won't affect our policy
      Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
      properties.put(ContentModel.PROP_INITIAL_VERSION, false);
      properties.put(ContentModel.PROP_AUTO_VERSION, false);
      properties.put(ContentModel.PROP_AUTO_VERSION_PROPS, false);
      nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, properties);       
    }
    
    // increment the version
    // assume every change is major for now
    Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(1, 1.0f);
    versionProperties.put(Version.PROP_DESCRIPTION, "Version updated via automated policy.");
    versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
    versionService.createVersion(nodeRef, versionProperties);

  }

}
