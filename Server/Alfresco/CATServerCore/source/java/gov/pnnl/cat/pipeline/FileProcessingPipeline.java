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
package gov.pnnl.cat.pipeline;

import java.util.Collection;


/**
 * Manages a pipeline for extracting/transforming documents
 * that have been addded/modified by users.
 * @version $Revision: 1.0 $
 */
public interface FileProcessingPipeline {
  
  public static final String PIPELINE_DISABLED = "FileProcessingPipeline.PipelineDisabled"; 

  /**
   * Submit a list of files to run through the pipeline.  Files are
   * partitioned to different thread pools depending upon their size.
   * @param files
   */
  public void submitProcessingJob(Collection<FileProcessingInfo> files);

  
  /**
   * Process the given list of files in the current thread.
   * @param files
   */
  public void processFiles(Collection<FileProcessingInfo> files);
  
  /**
   * The max size of regular files to be processed in the normal file queue.
   * Files bigger than this get added to the big file queue so they don't  
   * hold up smaller files.
  
   * @return long
   */
  public long getMaxRegularFileSize();

  /**
   * The max size of PDF files to be processed in the normal file queue.
   * Files bigger than this get added to the big file queue so they don't  
   * hold up smaller files.
  
   * @return long
   */
  public long getMaxPdfFileSize();

  
  /**
   * The max size of Excel 2007 files to be processed in the normal file queue.
   * Files bigger than this get added to the big file queue so they don't  
   * hold up smaller files.
  
   * @return long
   */
  public long getMaxXlsxFileSize();

  
}
