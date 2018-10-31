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
package gov.pnnl.velo.util;

import java.io.File;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.download.AbstractExporter;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;

/**
 * used to export files and folder to local file system.  Will overwite any files 
 * that already exist, TODO: add overwrite boolean flag and createLink boolean flag to
 * create sym links to content instead of copies for linux OS's.
 * @author zoe
 *
 * @version $Revision: 1.0 $
 */
public class LocalFileSystemExporter extends AbstractExporter {

  private NodeService nodeService;
  private ContentService contentService;
  private File destination;
  private StringBuilder commandScript;
  
  /**
   * Constructor for LocalFileSystemExporter.
   * @param nodeService NodeService
   * @param contentService ContentService
   * @param destination File
   * @param commandScript StringBuilder
   */
  public LocalFileSystemExporter(NodeService nodeService, ContentService contentService, File destination, StringBuilder commandScript) {
    this.nodeService = nodeService;
    this.destination = destination;
    this.contentService = contentService;
    this.commandScript = commandScript;
  }

  /**
   * Method startNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.service.cmr.view.Exporter#startNode(NodeRef)
   */
  @Override
  public void startNode(NodeRef nodeRef) {
    //have to create folders here instead of in startAssoc because folders with no children that has content won't be called in start/endAssoc
    String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      File subFolder = new File(destination, name);
      subFolder.mkdirs();
      destination = subFolder;
    }
  }

  /**
   * Method endNode.
   * @param nodeRef NodeRef
   * @see org.alfresco.service.cmr.view.Exporter#endNode(NodeRef)
   */
  @Override
  public void endNode(NodeRef nodeRef) {
    String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    if (nodeService.getType(nodeRef).equals(ContentModel.TYPE_FOLDER)) {
      String currentPath = destination.getAbsolutePath();
      destination = new File(currentPath.substring(0, currentPath.lastIndexOf(name)));
    }
  }

  /**
   * Method content.
   * @param nodeRef NodeRef
   * @param property QName
   * @param content InputStream
   * @param contentData ContentData
   * @param index int
   * @see org.alfresco.service.cmr.view.Exporter#content(NodeRef, QName, InputStream, ContentData, int)
   */
  @Override
  public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
    try {
      String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
      File outputFile = new File(destination, name);
      FileContentReader reader = (FileContentReader)contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

      if (SystemUtils.IS_OS_WINDOWS || commandScript == null) {
        FileUtils.copyFile(reader.getFile(), outputFile);

      } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_SOLARIS || SystemUtils.IS_OS_UNIX) {

        // Use symbolic links
        String sourceFile = reader.getFile().getAbsolutePath();
        String linkFile = outputFile.getAbsolutePath();

        // Make sure to dereference links in the source
        //ln -s $(readlink -f alf_data/contentstore/2013/4/4/16/41/4fdd836b-82da-4ea5-8a85-d8e7daf9049d.bin)
        commandScript.append("ln -s $(readlink -f " + sourceFile + ") \"" + linkFile + "\";\n");
      }

    } catch (Throwable e) {
      throw new RuntimeException("Failed to process content event - nodeRef " + nodeRef + "; property " + property);
    }
  }

}
