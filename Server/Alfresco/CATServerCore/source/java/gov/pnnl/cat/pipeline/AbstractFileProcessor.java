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

import gov.pnnl.cat.policy.ExtensiblePolicyAdapter;
import gov.pnnl.cat.util.CatConstants;
import gov.pnnl.cat.util.NodeUtils;

import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public abstract class AbstractFileProcessor extends ExtensiblePolicyAdapter implements FileProcessor {

  //Logger
  protected Log logger = LogFactory.getLog(this.getClass());

  // Priority (injected via config file)
  protected Integer priority;
  
  // Enabled status, true by default
  protected boolean enabled = true;
  
  // Mandatory, true by default
  protected boolean mandatory = true;

  // Injected beans
  protected FileProcessingPipeline pipeline;

  @Override
  public void init() {
    
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#getPriority()
   */
  @Override
  public Integer getPriority() {
    return priority;
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.policy.pipeline.FileProcessor#setPriority(java.lang.Integer)
   */
  @Override
  public void setPriority(Integer priority) {
    this.priority = priority;

  }

  /**
   * @param pipeline the pipeline to set
   */
  public void setPipeline(FileProcessingPipeline pipeline) {
    this.pipeline = pipeline;
  }

  /**
   * Method equals.
   * @param obj Object
   * @return boolean
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof FileProcessor && ((FileProcessor)obj).getName().equals(getName());
  }

  /**
   * Method hashCode.
   * @return int
   */
  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * Method toString.
   * @return String
   */
  @Override
  public String toString() {
    return getName();
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.pipeline.FileProcessor#cancel(gov.pnnl.cat.pipeline.FileProcessingInfo)
   */
  /**
   * Method logProcessorError.
   * @param info FileProcessingInfo
   * @param error Throwable
   * @see gov.pnnl.cat.pipeline.FileProcessor#logProcessorError(FileProcessingInfo, Throwable)
   */
  @Override
  public void logProcessorError(FileProcessingInfo info, Throwable error) {
    Throwable cause = NodeUtils.getRootCause(error);
    NodeRef nodeRef = info.getNodeToExtract();
    ArrayList<String> errors = (ArrayList<String>)nodeService.getProperty(nodeRef, CatConstants.PROP_PIPELINE_ERROR);
    if(errors == null){
      errors = new ArrayList<String>();
    }
    
    String msg = getName() + " failed. " + cause;
    errors.add(msg);
    nodeService.setProperty(nodeRef, CatConstants.PROP_PIPELINE_ERROR, errors);
    logger.debug("Logged Processor Error for " +info.getFileName() + ". error: " + msg);
  }

  /**
   * Method isEnabled.
   * @return boolean
   * @see gov.pnnl.cat.pipeline.FileProcessor#isEnabled()
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Method setEnabled.
   * @param enabled boolean
   * @see gov.pnnl.cat.pipeline.FileProcessor#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Method isMandatory.
   * @return boolean
   * @see gov.pnnl.cat.pipeline.FileProcessor#isMandatory()
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * Method setMandatory.
   * @param mandatory boolean
   * @see gov.pnnl.cat.pipeline.FileProcessor#setMandatory(boolean)
   */
  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }
  
}
