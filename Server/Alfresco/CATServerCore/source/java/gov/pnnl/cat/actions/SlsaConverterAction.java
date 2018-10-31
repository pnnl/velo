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
/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package gov.pnnl.cat.actions;

import gov.pnl.cat.slsa.MaterialSpectrum;
import gov.pnnl.cat.util.XmlUtility;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Takes an xml SLSA file and converts it to a .txt data file with an attached commment containing the metadata.
 * @version $Revision: 1.0 $
 */
public class SlsaConverterAction extends ActionExecuterAbstractBase {

  private static Log logger = LogFactory.getLog(SlsaConverterAction.class);

  public static final String invalidCharactersRegex = "[\"\\*\\\\>\\<\\?\\/\\:\\|\\%\\&\\+\\;\\xA3\\xAC]+";

  /**
   * The node service
   */
  private NodeService nodeService;

  /**
   * Our content service
   */
  private ContentService contentService;

  /**
   * Set the node service
   * 
   * @param nodeService
   *          the node service
   */
  public void setNodeService(NodeService nodeService) {
    this.nodeService = nodeService;
  }

  /**
   * @param contentService
   *          The contentService to set.
   */
  public void setContentService(ContentService contentService) {
    this.contentService = contentService;
  }

  /**
  
   * @param action Action
   * @param nodeRef NodeRef
   * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef) */
  public void executeImpl(Action action, NodeRef nodeRef) {

    if (this.nodeService.exists(nodeRef)) {

      // Get the xml file content
      ContentReader cr = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

      // Parse with XML
      MaterialSpectrum spec = XmlUtility.deserializeInputStreamAndClose(cr.getContentInputStream());

      // Put the metadata in a comment
      createComment(nodeRef, spec);

      NodeRef parentFolder = nodeService.getPrimaryParent(nodeRef).getParentRef();
      String fileName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
      String newName = spec.getMaterialName() + "-" + baseName + ".txt";

      // get rid of invalid characters contained in the material name property
      newName = newName.replaceAll(SlsaConverterAction.invalidCharactersRegex, "_");

      QName newChildAssocName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName);

      // Move to a .txt file
      nodeService.moveNode(nodeRef, parentFolder, ContentModel.ASSOC_CONTAINS, newChildAssocName);

      // Rename the name property
      nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName);

      // Rewrite the content to include only data
      ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
      writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

      StringBuilder data = new StringBuilder();
      addSection("DATABASE", spec.getDatabaseName(), data);
      addSection("FILENAME", spec.getDatabaseFilename(), data);
      addSection("SAMPLE NUMBER", spec.getSampleNumber(), data);
      addSection("MATERIAL NAME", spec.getMaterialName(), data);
      addSection("MATERIAL TYPE", spec.getMaterialType(), data);
      addSection("MATERIAL CLASS", spec.getMaterialClass(), data);
      addSection("MATERIAL SUBCLASS", spec.getMaterialSubclass(), data);
      addSection("X UNITS", spec.getXUnits(), data);
      addSection("Y UNITS", spec.getYUnits(), data);
      addSection("WAVELENGTH RANGE", spec.getWavelengthRange(), data);
      addSection("MINIMUM WAVELENGTH", String.valueOf(spec.getMinWavelength()), data);
      addSection("MAXIMUM WAVELENGTH", String.valueOf(spec.getMaxWavelength()), data);
      addSection("MINIMUM REFLECTANCE", String.valueOf(spec.getMinReflectance()), data);
      addSection("MAXIMUM REFLECTANCE", String.valueOf(spec.getMaxReflectance()), data);
      addSection("NUMBER OF DATA POINTS", String.valueOf(spec.getNumberOfDataPoints()), data);
      data.append("X VALUE\t\tY VALUE\n");
      for (double[] point : spec.getSpectralData()) {
        data.append(point[0]);
        data.append("\t\t");
        data.append(point[1]);
        data.append('\n');
      }

      writer.putContent(data.toString());

    }
  }

  /**
   * Method addSection.
   * @param title String
   * @param value String
   * @param data StringBuilder
   */
  private void addSection(String title, String value, StringBuilder data) {
    data.append(title);
    data.append('\n');
    data.append("----------");
    data.append('\n');
    data.append(value);
    data.append("\n\n");
  }

  /**
   * Method createComment.
   * @param nodeRef NodeRef
   * @param spec MaterialSpectrum
   */
  private void createComment(NodeRef nodeRef, MaterialSpectrum spec) {

    // add the discussable aspect
    if (!nodeService.hasAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE)) {
      this.nodeService.addAspect(nodeRef, ForumModel.ASPECT_DISCUSSABLE, null);
    }

    // create a child forum space using the child association just introduced by
    // adding the discussable aspect
    String forumName = "discussion";

    NodeRef forumNodeRef = nodeService.getChildByName(nodeRef, ForumModel.ASSOC_DISCUSSION, forumName);
    ChildAssociationRef childRef;

    if (forumNodeRef == null) {
      Map<QName, Serializable> forumProps = new HashMap<QName, Serializable>(1);
      forumProps.put(ContentModel.PROP_NAME, forumName);
      childRef = this.nodeService.createNode(nodeRef, ForumModel.ASSOC_DISCUSSION, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, forumName), ForumModel.TYPE_FORUM, forumProps);

      forumNodeRef = childRef.getChildRef();
    } else {
      logger.info("discussion node already found for " + nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
    }

    // Add the topic
    String topicName = "SpectrumMetadata";
    Map<QName, Serializable> topicProps = new HashMap<QName, Serializable>(1);
    topicProps.put(ContentModel.PROP_NAME, topicName);
    childRef = this.nodeService.createNode(forumNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, topicName), ForumModel.TYPE_TOPIC, topicProps);

    NodeRef topicNodeRef = childRef.getChildRef();

    // Add the post
    StringBuilder sb = new StringBuilder();
    sb.append("posted-");
    sb.append(new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()));
    sb.append(".html");
    String postName = sb.toString();

    Map<QName, Serializable> postProps = new HashMap<QName, Serializable>(1);
    postProps.put(ContentModel.PROP_NAME, postName);
    childRef = this.nodeService.createNode(topicNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, postName), ForumModel.TYPE_POST, postProps);

    NodeRef postNodeRef = childRef.getChildRef();

    // Write content to the post
    ContentWriter writer = contentService.getWriter(postNodeRef, ContentModel.PROP_CONTENT, true);
    writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
    writer.setEncoding("UTF-8");

    StringBuilder metadata = new StringBuilder();
    addSection("DATABASE", spec.getDatabaseName(), metadata);
    addSection("FILENAME", spec.getDatabaseFilename(), metadata);
    addSection("SAMPLE NUMBER", spec.getSampleNumber(), metadata);
    addSection("MATERIAL NAME", spec.getMaterialName(), metadata);
    addSection("MATERIAL TYPE", spec.getMaterialType(), metadata);
    addSection("MATERIAL CLASS", spec.getMaterialClass(), metadata);
    addSection("MATERIAL SUBCLASS", spec.getMaterialSubclass(), metadata);
    addSection("X UNITS", spec.getXUnits(), metadata);
    addSection("Y UNITS", spec.getYUnits(), metadata);
    addSection("WAVELENGTH RANGE", spec.getWavelengthRange(), metadata);
    addSection("MINIMUM WAVELENGTH", String.valueOf(spec.getMinWavelength()), metadata);
    addSection("MAXIMUM WAVELENGTH", String.valueOf(spec.getMaxWavelength()), metadata);
    addSection("MINIMUM REFLECTANCE", String.valueOf(spec.getMinReflectance()), metadata);
    addSection("MAXIMUM REFLECTANCE", String.valueOf(spec.getMaxReflectance()), metadata);
    addSection("NUMBER OF DATA POINTS", String.valueOf(spec.getNumberOfDataPoints()), metadata);
    addSection("MEASUREMENT TYPE", spec.getMeasurementType(), metadata);
    addSection("MATERIAL ORIGIN", spec.getMaterialOrigin(), metadata);
    addSection("MATERIAL DESCRIPTION", spec.getMaterialDescription(), metadata);
    addSection("PARTICLE SIZE", spec.getParticleSize(), metadata);
    addSection("DATABASE ADDITIONAL INFO FILE", spec.getDatabaseAdditionalInfoFile(), metadata);
    addSection("ADDITIONAL DESCRIPTION", spec.getAdditionalDescription(), metadata);
    addSection("CHEMICAL ANALYSIS OF MATERIAL", spec.getChemicalAnalysisOfMaterial(), metadata);
    addSection("MATERIAL SOURCE", spec.getMaterialSource(), metadata);
    addSection("MATERIAL LOCALITY", spec.getMaterialLocality(), metadata);
    addSection("MINERAL DESCRIPTION", spec.getMineralDescription(), metadata);
    addSection("XRD ANALYSIS", spec.getXrdAnalysis(), metadata);
    addSection("CONDITIONS OF SYNTHESIS", spec.getConditionsOfSynthesis(), metadata);
    addSection("SPECTRAL DESCRIPTION", spec.getSpectralDescription(), metadata);
    addSection("PETROGRAPHIC DESCRIPTION", spec.getPetrographicDescription(), metadata);
    addSection("MICROPROBE ANALYSIS", spec.getMicroprobeAnalysis(), metadata);
    addSection("TOTAL HEMISPHERICAL EMISSION", String.valueOf(spec.getTotalHemiEmission()), metadata);
    addSection("MODEL", spec.getModel(), metadata);

    writer.putContent(metadata.toString());

  }

  /**
   * No parameters
   * @param arg0 List<ParameterDefinition>
   */
  @Override
  protected void addParameterDefinitions(List<ParameterDefinition> arg0) {
    // None!
  }

}
