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

/**
 */
public interface FileProcessor {

  /**
   * Process the file (extract, transform, etc.)
   * @param fileInfo
   * @throws Exception
   */
  public void processFile(FileProcessingInfo fileInfo) throws Exception;
  
  /**
   * Called if file processing has ben cancelled due to timeout.
   * @param info
   * @param cause TODO
   */
  public void logProcessorError(FileProcessingInfo info, Throwable cause);
  
  /**
   * Method getName.
   * @return String
   */
  public String getName();
  
  /**
   * My priority so the pipeline can deterine processing order with
   * respect to the other processors.
   * TODO: later we may have to add scheduling rules so we can describe
   * more complex ordering logic.
  
   * @return Integer
   */
  public Integer getPriority();
  
  /**
   * So we can configure when this processor should run in relation
   * to the others.
   * @param priority
   */
  public void setPriority(Integer priority);
  
  /**
   * So we can disable a processor via a config file
   * @param enabled
   */
  public void setEnabled(boolean enabled);
  
  /**
  
   * @return true if this processor should run */
  public boolean isEnabled();
  
  /**
   * If true, this processor must run, even if other
   * processors time out because they took too long.
  
   * @param mandatory boolean
   */
  public void setMandatory(boolean mandatory);
  
  /**
  
   * @return - return true if this processor must run and
   * cannot be skipped even if it takes a long time */
  public boolean isMandatory();
}
