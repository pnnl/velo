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
package gov.pnnl.velo.wiki.content.impl;


import gov.pnnl.cat.util.NodeUtils;
import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.InitializingBean;

/**
 */
public class SEGYWikiContentExtractor extends ExeBasedContentExtractor implements InitializingBean {


  private MimetypeService mimetypeService;

  /* (non-Javadoc)
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractor#getSupportedMimetypes()
   */
  @Override
  public List<String> getSupportedMimetypes() {
    List<String> mimetypes = new ArrayList<String>();
    mimetypes.add("seismic/x-segy");
    return mimetypes;
  }

  /**
   * Method extractWikiContent.
   * @param alfrescoNode NodeRef
   * @return File
   * @throws Exception
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractor#extractWikiContent(NodeRef)
   */
  @Override
  public File extractWikiContent(NodeRef alfrescoNode) throws Exception{
    String summary = "";

    File inputFile = getAlfrescoFile(alfrescoNode);
    NodeRef primaryParent = nodeService.getPrimaryParent(alfrescoNode).getParentRef();
    String context = WikiUtils.getWikiPath(primaryParent, nodeService);
    context = context.endsWith("/")?context:context+"/";
    
    String imagename = (String)nodeService.getProperty(alfrescoNode, ContentModel.PROP_NAME);
    imagename = imagename.substring(0, imagename.lastIndexOf(".sgy")) + ".png";
    File outputFile = new File(System.getProperty("java.io.tmpdir"),imagename);
    
    String workingDirPath = WikiUtils.getWikiExtensions() + "/scripts/getmime/Extractors";
    File workingDir = new File(workingDirPath);
    
    String exePath = workingDirPath + "/SEGYExtractor.py";
    
    String[] cmdArray = {"python", exePath, 
        "--inputfile="+inputFile.getAbsolutePath(), 
        "--outputfile="+outputFile.getAbsolutePath()};
    WikiUtils.execCommand(cmdArray, workingDir); 
    
    NodeUtils.createFile(primaryParent, imagename,
        outputFile, nodeService, contentService, mimetypeService);
    
    
    // Add default metadata template around summary
    StringBuilder metadata = new StringBuilder("");
    metadata.append("__NOTOC__\n\n"); 
    metadata.append("= Metadata  =\n\n");
    metadata.append("== Metadata for {{PAGENAME}}  ==\n\n");
    metadata.append("[[Image:");
    metadata.append(context);
    metadata.append(outputFile.getName());
    metadata.append( "]]\n\n");
    metadata.append("= Provenance  =\n\n");
    metadata.append("There are no saved relationships to this file\n\n");
    metadata.append("=Annotation=\n\n");
    metadata.append("== User Annotation ==\n\n");
    metadata.append("<headertabs />\n\n");
    
    return WikiUtils.createTempFile(metadata.toString());

  }

  /**
   * Method getMimetypeService.
   * @return MimetypeService
   */
  public MimetypeService getMimetypeService() {
    return mimetypeService;
  }

  /**
   * Method setMimetypeService.
   * @param mimetypeService MimetypeService
   */
  public void setMimetypeService(MimetypeService mimetypeService) {
    this.mimetypeService = mimetypeService;
  }

  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub
    
  }


}
