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
package gov.pnnl.cat.policy.images;

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Behavior used to keep profile images formatted correctly so they can
 * be rendered consistently in CAT/Eclipse.
 * @version $Revision: 1.0 $
 */
public class ProfileImageBehavior extends ExtensiblePolicyAdapter {

  /** The logger */
  private static Log logger = LogFactory.getLog(ProfileImageBehavior.class); 

  /** Used to do the image conversion **/
  // the transformer itself
  protected ContentTransformer imageMagickContentTransformer;

  // the worker that is wrapped by the transformer and actually does the work
  protected ContentTransformerWorker worker;

  /** The content service */
  protected ContentService contentService;

  /** the resize command default */
  protected String commandOptions = "-resize 300x300";

  /** the mimetype default */
  protected String mimetype = "image/png";

  /** the image property */
  protected String imagePropQName;

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#init()
   */
  public void init() {
    // done in ExtensiblePolicy
  }

  /**
   * Method setimagePropQName.
   * @param propQName String
   */
  public void setimagePropQName(String propQName) {
    this.imagePropQName = propQName;
  }

  /**
   * Method setCommandOptions.
   * @param command String
   */
  public void setCommandOptions(String command) {
    this.commandOptions = command;
  }

  /**
   * Set the image magick content transformer
   * 
   * @param imageMagickContentTransformer   the conten transformer
   */
  public void setImageMagickContentTransformer(ContentTransformer imageMagickContentTransformer) {
    this.imageMagickContentTransformer = imageMagickContentTransformer;
  }

  /**
   * Method setWorker.
   * @param worker ContentTransformerWorker
   */
  public void setWorker(ContentTransformerWorker worker) {
    this.worker = worker;
  }

  /**
   * Method setContentService.
   * @param contentService ContentService
   */
  public void setContentService (ContentService contentService) {
    this.contentService = contentService;
  }

  /**
   * Method setMimetype.
   * @param mimetype String
   */
  public void setMimetype (String mimetype) {
    this.mimetype = mimetype;
  }

  /**
   * Resize the picture on the profile if it is provided so
   * it will fit into the picture slot in CAT.  Also, convert
   * any .jpeg format to .png, as .jpeg renders poorly in
   * Eclipse
   * @param childAssocRef ChildAssociationRef
   * @see org.alfresco.repo.node.NodeServicePolicies$OnCreateNodePolicy#onCreateNode(ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    NodeRef profile = childAssocRef.getChildRef();
    logger.debug("calling onCreateNode for type: " + nodeService.getType(profile));    

    transformImage(profile);
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.ExtensiblePolicyAdapter#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
   */
  @Override
  public void onUpdateProperties(
      NodeRef nodeRef,
      Map<QName, Serializable> before,
      Map<QName, Serializable> after) {
    logger.debug("calling onUpdateProperties for type: " + nodeService.getType(nodeRef));

    QName propQName = QName.createQName(this.imagePropQName);

    ContentData imgBefore = (ContentData) before.get(propQName);
    ContentData imgAfter = (ContentData) after.get(propQName);

    if (imgAfter != null && !imgAfter.equals(imgBefore)) {
      transformImage(nodeRef);
    }
  }

  /**
   * Method transformImage.
   * @param profile NodeRef
   */
  protected void transformImage(NodeRef profile) {
    QName propQName = QName.createQName(this.imagePropQName);

    // Use use reader and writer to the same property :)
    ContentReader contentReader = this.contentService.getReader(profile, propQName);
    ContentWriter contentWriter = this.contentService.getWriter(profile, propQName, true);

    // don't do anything if an image has not been set
    if(contentReader == null ) {
      logger.debug("contentReader is null");
      ContentData imgProp = (ContentData)nodeService.getProperty(profile, propQName);
      logger.debug("content property = " + imgProp);
      return;
    }

    // check if the transformer is going to work, i.e. is available
    if (!this.worker.isAvailable()) {
      logger.error("No MagickContentTransformer available on server!");

    } else {

      // set the destination mime type to png
      contentWriter.setMimetype(this.mimetype);
      contentWriter.setEncoding(contentReader.getEncoding());     // original encoding

      logger.debug("trying to perform image transform");
      this.imageMagickContentTransformer.transform(contentReader, contentWriter);
    }    
  }

}
