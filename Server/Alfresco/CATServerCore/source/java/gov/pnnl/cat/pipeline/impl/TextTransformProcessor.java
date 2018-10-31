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

import gov.pnnl.cat.pipeline.AbstractFileProcessor;
import gov.pnnl.cat.pipeline.FileProcessingInfo;
import gov.pnnl.cat.pipeline.ProcessorTimeoutException;
import gov.pnnl.cat.transformers.ExecTransform;
import gov.pnnl.cat.transformers.StreamGobbler;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.TransformUtils;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;

/**
 */
public class TextTransformProcessor extends AbstractFileProcessor {

  protected static final String NO_TRANSFORM_TEXT = "The plain text extraction has not yet been executed for this node.  If the text does not appear within a few minutes, contact your CAT administrator.";

  // Timeout threshold for runaway processors (in ms)
  private long processorTimeout = 1000 * 60 * 3; // 3 minutes (in ms)

  private int processorPollingIncrement = 100; // ms
  private TransformerDebug transformerDebug;
  
  /*
   * (non-Javadoc)
   * 
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#processFile(gov.pnnl.cat.policy.pipeline.FileProcessingInfo)
   */
  @Override
  public void processFile(FileProcessingInfo fileInfo) throws Exception {

    NodeRef nodeRef = fileInfo.getNodeToExtract();

    // First make sure that we still need to do a transform
    if (nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM) == null) {
      return;
    }

    // TODO: right now there is a bug with Open Office in that it takes FOREVER to extract xlsx documents
    // so for now, we don't want to bottle neck the thread pool, so don't text extract them and just say
    // there was an error
    String mimetype = fileInfo.getMimetype();
    long fileSize = fileInfo.getFileSize();

    if (mimetype.equals(CatConstants.MIMETYPE_XLSX) && (fileSize > pipeline.getMaxXlsxFileSize())) {
      // mark as text transform failed
      markTextTransformFailed(nodeRef, "Text transforms are not performed on MS Excel 2007 files larger than " + String.valueOf(pipeline.getMaxXlsxFileSize()).charAt(0) + " MB.  Try saving your file as Excel 97/2003 format and re-upload.");

    } else if (fileInfo.isTextExtractionRequired()) {
      // Action action = actionService.createAction("text-extraction-action");
      // actionService.executeAction(action, nodeRef, false, false);
      QName nodeType = nodeService.getType(nodeRef);
      if (nodeType.equals(ContentModel.TYPE_CONTENT)) {
        newTextTransformContentNode(fileInfo);
      } else {
        logger.debug("This action is not appropriate for nodes of type: " + nodeType);
      }

    }
  }

  /**
   * Method getName.
   * @return String
   * @see gov.pnnl.cat.pipeline.FileProcessor#getName()
   */
  @Override
  public String getName() {
    return "Text Extraction";
  }

  /**
   * Method markTextTransformFailed.
   * @param nodeRef NodeRef
   * @param errorMsg String
   */
  private void markTextTransformFailed(NodeRef nodeRef, String errorMsg) {

    nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, "Transform failed.  " + errorMsg);
    nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM, null);

  }

  /**
   * Method setProcessorTimeout.
   * @param processorTimeout long
   */
  public void setProcessorTimeout(long processorTimeout) {
    this.processorTimeout = processorTimeout;
  }

  /**
   * Method setProcessorPollingIncrement.
   * @param processorPollingIncrement int
   */
  public void setProcessorPollingIncrement(int processorPollingIncrement) {
    this.processorPollingIncrement = processorPollingIncrement;
  }

  /**
   * Method setTransformerDebug.
   * @param transformerDebug TransformerDebug
   */
  public void setTransformerDebug(TransformerDebug transformerDebug) {
    this.transformerDebug = transformerDebug;
  }

  /**
   * Method logProcessorError.
   * @param info FileProcessingInfo
   * @param error Throwable
   * @see gov.pnnl.cat.pipeline.FileProcessor#logProcessorError(FileProcessingInfo, Throwable)
   */
  @Override
  public void logProcessorError(FileProcessingInfo info, Throwable error) {
    super.logProcessorError(info, error);
    NodeRef nodeRef = info.getNodeToExtract();

    // mark the node as having a transform error so it isn't retried
    if(!nodeService.hasAspect(nodeRef, CatConstants.ASPECT_TEXT_TRANSFORM)) {
      nodeService.addAspect(nodeRef, CatConstants.ASPECT_TEXT_TRANSFORM, null);
    }

    nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, "Transform failed.  Text transform timed out.  See server log");
    // remove this flag so we know the transform completed, whether an error occurred or not
    nodeService.removeProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM);
  }

  /**
   * Method newTextTransformContentNode.
   * @param info FileProcessingInfo
   */
  private void newTextTransformContentNode(FileProcessingInfo info) {
    NodeRef nodeRef = info.getNodeToExtract();

    // get a ContentReader, then find transformer based on the content mime type -> plain text
    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    if (reader != null && reader.exists()) {
      try {   
        // get the transformer
        TransformationOptions options = new TransformationOptions();
        options.setSourceNodeRef(nodeRef);
        transformerDebug.pushAvailable(reader.getContentUrl(), reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN, options);
        long sourceSize = reader.getSize();
        List<ContentTransformer> transformers = contentService.getActiveTransformers(reader.getMimetype(), sourceSize, MimetypeMap.MIMETYPE_TEXT_PLAIN, options);
        transformerDebug.availableTransformers(transformers, sourceSize, "NodeContentGet");

        if (transformers.isEmpty())
        {
          // log it
          if (logger.isDebugEnabled()) {
            logger.debug("Not indexed: No transformation: \n" + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN);
          }
          nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, "Transform failed.  No text transformer found for:  " + reader.getMimetype());

        } else {

          ContentTransformer transformer = transformers.get(0);

          // Perform transformation catering for mimetype AND encoding
          ContentWriter writer = TransformUtils.getTransformWriter(nodeRef, nodeService, contentService);
          writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
          writer.setEncoding("UTF-8");                            // Expect transformers to produce UTF-8

          try
          {
            // run all types transforms in seperate processes
            //writer.putContent("");

            long start = System.currentTimeMillis();
            transformer.transform(reader, writer);
            long transformDuration = System.currentTimeMillis() - start;
            logger.debug("Transform duration = " + String.valueOf(transformDuration));

            // Update the size part of the content property
            ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT));
            ContentReader transformReader = contentService.getReader(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);

            // How could this not be the case?
            if (transformReader != null && transformReader instanceof FileContentReader) {

              FileContentReader fileContentReader = (FileContentReader) transformReader;
              long filesize = fileContentReader.getFile().length();
              ContentData updatedContentData = new ContentData(contentData.getContentUrl(), contentData.getMimetype(), filesize, contentData.getEncoding());
              nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT, updatedContentData);
            }
            if (nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR) != null) {
              // update this node to indicate no error occured
              // this only happens on a content update when the previous transform failed but now
              // this one worked
              nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, null);
            }

          } catch (Throwable e) {

            // log the error and store it in the node's error property
            // I could add this test and modify message to say that the file was too large. e.getCause() instanceof java.lang.OutOfMemoryError
            if (e.getCause() instanceof java.lang.OutOfMemoryError) {
              logger.error("Transformer failed, OutOfMemoryError: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
              nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  The file is too large to transform.");
            } else {
              logger.error("Transformer failed: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
              nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  See server log");
            }

          }

        }
      } finally
      {
        transformerDebug.popAvailable();
      }
    }


    // remove this flag so we know the transform completed, whether an error occurred or not
    nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM, null);

  }


  /**
   * Text-transform an individual content node
   * 
  
   * @param fileInfo FileProcessingInfo
   */
  private void textTransformContentNode(FileProcessingInfo fileInfo) {
    NodeRef nodeRef = fileInfo.getNodeToExtract();
    boolean timedOut = false;

    // get a ContentReader, then find transformer based on the content mime type -> plain text
    ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
    if (reader != null && reader.exists()) {
      ContentTransformer transformer = contentService.getTransformer(reader.getMimetype(), MimetypeMap.MIMETYPE_TEXT_PLAIN);
      // is this transformer good enough?
      if (transformer == null) {
        // log it
        if (logger.isDebugEnabled()) {
          logger.debug("Not indexed: No transformation: \n" + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN);
        }
        nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, "Transform failed.  No text transformer found for:  " + reader.getMimetype());
      }

      else {
        // get a ContentWriter for the extracted text to be written to
        ContentWriter writer = TransformUtils.getTransformWriter(nodeRef, nodeService, contentService);

        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        try {
          // run all types transforms in seperate processes
          writer.putContent("");

          transformer.transform(reader, writer);

          // Update the size part of the content property
          ContentData contentData = DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT));
          ContentReader transformReader = contentService.getReader(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT);

          // How could this not be the case?
          if (transformReader != null && transformReader instanceof FileContentReader) {

            FileContentReader fileContentReader = (FileContentReader) transformReader;
            long filesize = fileContentReader.getFile().length();
            ContentData updatedContentData = new ContentData(contentData.getContentUrl(), contentData.getMimetype(), filesize, contentData.getEncoding());
            nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORMED_CONTENT, updatedContentData);
          }
          if (nodeService.getProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR) != null) {
            // update this node to indicate no error occured
            // this only happens on a content update when the previous transform failed but now
            // this one worked
            nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, null);
          }

        } catch (Exception e) {
          // log the error and store it in the node's error property
          // I could add this test and modify message to say that the file was too large. e.getCause() instanceof java.lang.OutOfMemoryError
          if (e.getCause() instanceof java.lang.OutOfMemoryError) {
            logger.error("Transformer failed, OutOfMemoryError: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
            nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  The file is too large to transform.");
          } else {
            logger.error("Transformer failed: \n   node: " + nodeRef + "   source: " + reader + "\n" + "   target: " + MimetypeMap.MIMETYPE_TEXT_PLAIN, e);
            nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_TRANSFORM_ERROR, e.getClass().getName() + ": Transform failed.  See server log");
          }
        }

      }
    }
    // If the full text indexer is running at the same time that this job completes, but this file
    // was tried to be indexed first in the batch when this job hadn't complted yet,
    // then the dirty flag won't get set again for a retry - we need a delayed trigger

    // problems when fti and transform threads run at same time

    // remove this flag so we know the transform completed, whether an error occurred or not
    nodeService.setProperty(nodeRef, CatConstants.PROP_TEXT_NEEDS_TRANSFORM, null);

    // String fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    // System.out.println("Finished text transform for file: " + fileName);

    if(timedOut){
      throw new ProcessorTimeoutException();
    }
  }

  /**
   * Method remoteTransform.
   * @param sourceClass ContentTransformer
   * @param reader FileContentReader
   * @param writer FileContentWriter
   * @return boolean
   * @throws Exception
   */
  private boolean remoteTransform(ContentTransformer sourceClass, FileContentReader reader, FileContentWriter writer) throws Exception {
    String cmd[] = new String[14];
    cmd[0] = ExecTransform.getJavaExex();
    cmd[1] = "-Xmx1024m";
    cmd[2] = "-cp";
    cmd[3] = ExecTransform.getSmallClassPath();
    cmd[4] = TextTransformExec.class.getCanonicalName();
    cmd[5] = sourceClass.getClass().getCanonicalName();
    cmd[6] = reader.getFile().getCanonicalPath();
    cmd[7] = writer.getFile().getCanonicalPath();
    cmd[8] = reader.getEncoding();
    cmd[9] = writer.getEncoding();
    cmd[10] = reader.getMimetype();
    cmd[11] = writer.getMimetype();
    cmd[12] = ExecTransform.getLibFolder();
    cmd[13] = ExecTransform.getClassesFolder();

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
    boolean destroyedProc = false;
    while (!finished) {
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
      if (!finished && elapsedTime >= processorTimeout) {
        logger.info("Trying to cancel execution thread for file");
        // Destroy the process, we're done waiting for it
        proc.destroy();
        destroyedProc = true;
        // mark finished state, since the thread may not throw an exception when it cancels!
        finished = true;
        exitVal = 1;
      } else {
        try {
          exitVal = proc.exitValue();
          finished = true;
        } catch (IllegalThreadStateException e) {
          //          System.out.println("caught IllegalThreadStateException, wait some more");
        }
      }
    }

    if (!destroyedProc && exitVal != 0) {
      errorGobbler.join();
      String msg = errorGobbler.getMessage();
      throw new ContentIOException(msg);
    }
    return destroyedProc;
  }

}
