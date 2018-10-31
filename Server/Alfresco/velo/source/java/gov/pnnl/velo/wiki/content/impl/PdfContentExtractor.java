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


import gov.pnnl.velo.util.WikiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.factory.InitializingBean;

/**
 */
public class PdfContentExtractor extends ExeBasedContentExtractor implements InitializingBean {

  private List<String> mimetypes = new ArrayList<String>();

  /* (non-Javadoc)
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    mimetypes.add("application/pdf");
  }


  /* (non-Javadoc)
   * @see gov.pnnl.velo.wiki.content.WikiContentExtractor#getSupportedMimetypes()
   */
  @Override
  public List<String> getSupportedMimetypes() {
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
  public File extractWikiContent(NodeRef alfrescoNode) throws Exception {

    File inputFile = getAlfrescoFile(alfrescoNode);
    File outputFile = TempFileProvider.createTempFile("velo-pdf-", ".metadata");
    
    String workingDirPath = WikiUtils.getWikiExtensions() + "/scripts/getmime/Extractors";
    File workingDir = new File(workingDirPath);
    
    String exePath = workingDirPath + "/PDFExtractor.py";
    
    String[] cmdArray = {"python", exePath, 
    		"--inputfile="+inputFile.getAbsolutePath(), 
    		"--outputfile="+outputFile.getAbsolutePath()};
    WikiUtils.execCommand(cmdArray, workingDir); 
    
    return outputFile;
  }

  
}
