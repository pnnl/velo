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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Wrapper so we can schedule the pipeline to run in a background thread.
 * @version $Revision: 1.0 $
 */
public class FileProcessingJob implements Runnable {
  
  protected static final Log logger = LogFactory.getLog(FileProcessingJob.class);

  private Collection<FileProcessingInfo> nodesToProcess;
  private FileProcessingPipeline pipeline;
  
  /**
   * Constructor
   * @param nodesToProcess
   * @param pipeline
   */
  public FileProcessingJob(Collection<FileProcessingInfo> nodesToProcess, FileProcessingPipeline pipeline) {    
    this.nodesToProcess = nodesToProcess;
    this.pipeline = pipeline;
  }
  
  /**
   * Method run.
   * @see java.lang.Runnable#run()
   */
  public void run() {
    pipeline.processFiles(nodesToProcess);
  }

}
