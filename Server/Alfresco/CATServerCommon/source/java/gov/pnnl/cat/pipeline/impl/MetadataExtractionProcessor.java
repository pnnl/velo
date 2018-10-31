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
package gov.pnnl.cat.pipeline.impl;

import gov.pnnl.cat.intercept.extracter.BadCharacterRemover;
import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.transformers.ExecTransform;
import gov.pnnl.cat.transformers.StreamGobbler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class MetadataExtractionProcessor extends AbstractFileProcessor {
  private boolean carryAspectProperties = true;

  private MetadataExtracterRegistry metadataExtracterRegistry;

  // Timeout threshold for runaway processors (in ms)
  private long processorTimeout = 1000 * 60 * 3; // 3 minutes (in ms)

  private int processorPollingIncrement = 100; // ms
  
  private BadCharacterRemover badCharacterRemover;

  
  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "Metadata Extraction";
  }

  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {
    NodeRef nodeRef = fileInfo.getNodeToExtract();

    if (nodeService.exists(nodeRef)) {
      //old way
//      // Do not extract metadata for .xlsx files since it takes a really long time!
//       String mimetype = fileInfo.getMimetype();
//       long fileSize = fileInfo.getFileSize();
//      
//       if (mimetype.equals(CatConstants.MIMETYPE_XLSX) && (fileSize > pipeline.getMaxXlsxFileSize())){
//       logger.debug("Metadata extraction not performed on: " + fileInfo.getFileName()+ " because it is larger than " + String.valueOf(pipeline.getMaxXlsxFileSize()).charAt(0) + "MB");
//      
//       } else{
////       Extract metadata
//       Action action = actionService.createAction("extract-metadata");
//       actionService.executeAction(action, nodeRef, false, false);
//       }
      //new way
      executeImpl(nodeRef);
    }
  }

  // CAT CHANGE - COPIED FROM ALFRESCO'S ContentMetadataExtracter class so we could exec in a seperate process
  /**
   * @see org.alfresco.repo.action.executer.ActionExecuter#execute(org.alfresco.service.cmr.repository.NodeRef, NodeRef)
   */
  public void executeImpl(NodeRef actionedUponNodeRef) {
    if (!nodeService.exists(actionedUponNodeRef)) {
      // Node is gone
      return;
    }
    ContentReader reader = contentService.getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT);
    // The reader may be null, e.g. for folders and the like
    if (reader == null || reader.getMimetype() == null) {
      // No content to extract data from
      return;
    }
    String mimetype = reader.getMimetype();
    MetadataExtracter extracter = metadataExtracterRegistry.getExtracter(mimetype);
    if (extracter == null) {
      // There is no extracter to use
      return;
    }

    // Get all the node's properties
    Map<QName, Serializable> nodeProperties = nodeService.getProperties(actionedUponNodeRef);

    // TODO: The override policy should be a parameter here. Instead, we'll use the default policy
    // set on the extracter.
    // Give the node's properties to the extracter to be modified
    Map<QName, Serializable> modifiedProperties = null;
    try {

      modifiedProperties = remoteMetadataExtract((FileContentReader) reader, extracter, nodeProperties);

      // Convert the properties according to the dictionary types
      Map<QName, Serializable>  systemProperties = ((AbstractMappingMetadataExtracter)extracter).convertSystemPropertyValues(modifiedProperties);
      // Now use the proper overwrite policy
      modifiedProperties = ((AbstractMappingMetadataExtracter)extracter).getOverwritePolicy().applyProperties(systemProperties, nodeProperties);
      
      // modifiedProperties = extracter.extract(
      // reader,
      // /*OverwritePolicy.PRAGMATIC,*/
      // nodeProperties);
    } catch (Throwable e) {
      // Extracters should attempt to handle all error conditions and extract
      // as much as they can. If, however, one should fail, we don't want the
      // action itself to fail. We absorb and report the exception here to
      // solve ETHREEOH-1936 and ALFCOM-2889.
      logger.debug("metadata failed: " + e.getMessage());
      if (logger.isDebugEnabled()) {
        logger.debug("Raw metadata extraction failed: \n" + "   Extracter: " + this + "\n" + "   Node:      " + actionedUponNodeRef + "\n" + "   Content:   " + reader, e);
      } else {
        logger.warn("Raw metadata extraction failed (turn on DEBUG for full error): \n" + "   Extracter: " + this + "\n" + "   Node:      " + actionedUponNodeRef + "\n" + "   Content:   " + reader + "\n" + "   Failure:   " + e.getMessage());
      }
      modifiedProperties = new HashMap<QName, Serializable>(0);
    }

    // If none of the properties where changed, then there is nothing more to do
    if (modifiedProperties.size() == 0) {
      return;
    }

    // Check that all properties have the appropriate aspect applied
    Set<QName> requiredAspectQNames = new HashSet<QName>(3);
    Set<QName> aspectPropertyQNames = new HashSet<QName>(17);

    /**
     * The modified properties contain null values as well. As we are only interested in the keys, this will force aspect aspect properties to be removed even if there are no settable properties pertaining to the aspect.
     */
    for (QName propertyQName : modifiedProperties.keySet()) {
      PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
      if (propertyDef == null) {
        // The property is not defined in the model
        continue;
      }
      ClassDefinition propertyContainerDef = propertyDef.getContainerClass();
      if (propertyContainerDef.isAspect()) {
        QName aspectQName = propertyContainerDef.getName();
        requiredAspectQNames.add(aspectQName);
        // Get all properties associated with the aspect
        Set<QName> aspectProperties = propertyContainerDef.getProperties().keySet();
        aspectPropertyQNames.addAll(aspectProperties);
      }
    }

    if (!carryAspectProperties) {
      // Remove any node properties that are defined on the aspects but were not extracted
      for (QName aspectPropertyQName : aspectPropertyQNames) {
        if (!modifiedProperties.containsKey(aspectPropertyQName)) {
          // Simple case: This property was not extracted
          nodeProperties.remove(aspectPropertyQName);
        } else if (modifiedProperties.get(aspectPropertyQName) == null) {
          // Trickier (ALF-1823): The property was extracted as 'null'
          nodeProperties.remove(aspectPropertyQName);
        }
      }
    }

    // Add all the properties to the node BEFORE we add the aspects
    nodeService.setProperties(actionedUponNodeRef, nodeProperties);

    // Add each of the aspects, as required
    for (QName requiredAspectQName : requiredAspectQNames) {
      if (nodeService.hasAspect(actionedUponNodeRef, requiredAspectQName)) {
        // The node has the aspect already
        continue;
      } else {
        nodeService.addAspect(actionedUponNodeRef, requiredAspectQName, null);
      }
    }
  }

  private Map<QName, Serializable> remoteMetadataExtract(FileContentReader reader, MetadataExtracter extracter, Map<QName, Serializable> nodeProperties) throws Exception {
    // temp file to save properties to, will also be used to read properties from
    File propFile = TempFileProvider.createTempFile("metadataExtracterProps_", ".bin");
    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(propFile)));
    out.writeObject(nodeProperties);
    out.flush();
    out.close();

    File extracterFile = TempFileProvider.createTempFile("metadataExtracter_", ".bin");
    out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(extracterFile)));
    out.writeObject(extracter);
    out.flush();
    out.close();

    // 1. call our own main method, pass in as params: path to temp file of saved props, FileContentReader's file's path
    String cmd[] = new String[9];
    cmd[0] = ExecTransform.getJavaExex();
    cmd[1] = "-Xmx1024m";
    cmd[2] = "-cp";
    cmd[3] = ExecTransform.getClassPath();
    cmd[4] = MetadataExtractionProcessor.class.getCanonicalName();
    cmd[5] = extracterFile.getCanonicalPath();
    cmd[6] = reader.getFile().getCanonicalPath();
    cmd[7] = propFile.getCanonicalPath();
    cmd[8] = reader.getMimetype();

    if (logger.isDebugEnabled()) {
      StringBuilder builder = new StringBuilder();
      builder.append('\'');
      for (String arg : cmd) {
        builder.append(arg);
        builder.append(' ');
      }
      builder.deleteCharAt(builder.length() - 1);
      builder.append('\'');
      logger.debug("CMD: " + builder.toString());
    }

    Runtime rt = Runtime.getRuntime();
    Process proc = rt.exec(cmd);

    StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
    StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());

    errorGobbler.start();
    outputGobbler.start();

    boolean finished = false;
    long elapsedTime = 0;
    long startTime = 0;
    long endTime = 0;
    long intervalTime = 0;
    int exitVal = 0;
    while (!finished) {
//      System.out.println("not finished, sleeping");
      // sleep
      try {
        startTime = System.currentTimeMillis();
        Thread.sleep(processorPollingIncrement);
      } catch (InterruptedException e) {
      }
      endTime = System.currentTimeMillis();
      // sleep may actually run longer than what it says, so we need to
      // count clock time instead when updating the elapsed time
      intervalTime = (endTime - startTime);
      elapsedTime += intervalTime;
//      System.out.println("finished sleeping, checking if its time to cancel");
      if (!finished && elapsedTime >= processorTimeout) {
        logger.info("Trying to cancel execution thread for file");
//        System.out.println("Trying to cancel execution thread for file");
        // Destroy the process, we're done waiting for it
        proc.destroy();
        // mark finished state, since the thread may not throw an exception when it cancels!
        finished = true;
        exitVal = 1;
//        System.out.println("done stopping thread");
      } else {
        try {
          exitVal = proc.exitValue();
          finished = true;
//          System.out.println("done waiting");
        } catch (IllegalThreadStateException e) {
//          System.out.println("caught IllegalThreadStateException, wait some more");
        }
      }
    }

    if (exitVal != 0) {
      errorGobbler.join();
      String msg = errorGobbler.getMessage();
      logger.debug("metadata proc failed: "+msg);
      throw new ContentIOException(msg);
    }else{
      outputGobbler.join();
      String msg = outputGobbler.getMessage();
      logger.debug("metadata proc succeeded: "+msg);
    }
    ObjectInputStream is = null;
    try {
      XStream xstream = new XStream(new DomDriver());
      is = new ObjectInputStream(new FileInputStream(propFile));
//      Map<QName, Serializable> props = (Map<QName, Serializable>) is.readObject(); //these are the modified properties
      Map<QName, Serializable> props = (Map<QName, Serializable>) xstream.fromXML(is);
//      is = new ObjectInputStream(new FileInputStream(extracterFile));//re-use the extracterFile for the full properties map
//      Map<QName, Serializable> newNodeProperties = (Map<QName, Serializable>)xstream.fromXML(is);
//      Map<QName, Serializable> newNodeProperties = (Map<QName, Serializable>) is.readObject();//these are the full list of properties (what's actually set back on the node)
      
      //loop thru the full list of props and add them back to the object passed in to us - its this object that 
      //is used to set props back to alfresco:
//      for (QName propName : newNodeProperties.keySet()) {
//        nodeProperties.put(propName, newNodeProperties.get(propName));
//      }
      
      return badCharacterRemover.modifyMetadata(props);
    } finally {
      if (is != null) {
        is.close();
      }
    }
  }

  public void setMetadataExtracterRegistry(MetadataExtracterRegistry metadataExtracterRegistry) {
    this.metadataExtracterRegistry = metadataExtracterRegistry;
  }

  public static void main(String[] args) {

    File source = null;
    File extracterFile = null;
    File properties = null;

    if (args.length > 0) {
      extracterFile = new File(args[0]);
      source = new File(args[1]);
      properties = new File(args[2]);
      String mimetype = args[3];
      
      ObjectInputStream propsInputStream = null;
      ObjectInputStream extracterInputStream = null;
      ObjectOutputStream out = null;
      try {
//        Class extracterClass = Class.forName(args[0]);
//        MetadataExtracter extracter = (MetadataExtracter) extracterClass.newInstance();

        extracterInputStream = new ObjectInputStream(new FileInputStream(extracterFile));
        MetadataExtracter extracter =  (MetadataExtracter) extracterInputStream.readObject();
        IOUtils.closeQuietly(extracterInputStream);
        

        propsInputStream = new ObjectInputStream(new FileInputStream(properties));
        Map<QName, Serializable> props = (Map<QName, Serializable>) propsInputStream.readObject();
        IOUtils.closeQuietly(propsInputStream);
        
        FileContentReader reader = new FileContentReader(source);
        reader.setMimetype(mimetype);

        Map<QName, Serializable> modifiedProps = extracter.extract(reader, props);

        XStream xstream = new XStream(new DomDriver());
        out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(properties)));
//        out.writeObject(modifiedProps);
//        out.flush();
        xstream.toXML(modifiedProps, out);

        //need to write the full list of props back to a file as well, these are the ones actually used
        //to save to alfresco
        out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(extracterFile)));
//        out.writeObject(props);
//        out.flush();
//        try use xstream, for some reason the sentdate prop is coming back as a string instead of a Date
        xstream.toXML(props, out);
      } catch (Throwable t) {
        System.err.println("Failed to metadata extract file.");
        t.printStackTrace();
        System.exit(1);
      } finally {
        IOUtils.closeQuietly(propsInputStream);
        IOUtils.closeQuietly(out);
      }
    } else {
      System.err.println("Bad arguments for metadata extractor.");
      System.exit(2);
    }

    System.exit(0);
  }

  public void setProcessorTimeout(long processorTimeout) {
    this.processorTimeout = processorTimeout;
  }

  public void setProcessorPollingIncrement(int processorPollingIncrement) {
    this.processorPollingIncrement = processorPollingIncrement;
  }

  public void setBadCharacterRemover(BadCharacterRemover badCharacterRemover) {
    this.badCharacterRemover = badCharacterRemover;
  }

}
