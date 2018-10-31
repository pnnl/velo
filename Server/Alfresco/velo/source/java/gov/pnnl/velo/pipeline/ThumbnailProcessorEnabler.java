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
package gov.pnnl.velo.pipeline;

import gov.pnnl.cat.pipeline.impl.ThumbnailProcessor;

import org.springframework.beans.factory.InitializingBean;

/**
 */
public class ThumbnailProcessorEnabler implements InitializingBean {
  private boolean enabled;
  private ThumbnailProcessor thumbnailProcessor;
  
  /**
   * Method afterPropertiesSet.
   * @throws Exception
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    thumbnailProcessor.setEnabled(enabled);    
  }

  /**
   * Method setEnabled.
   * @param enabled boolean
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Method setThumbnailProcessor.
   * @param thumbnailProcessor ThumbnailProcessor
   */
  public void setThumbnailProcessor(ThumbnailProcessor thumbnailProcessor) {
    this.thumbnailProcessor = thumbnailProcessor;
  }
  
  
  
}
