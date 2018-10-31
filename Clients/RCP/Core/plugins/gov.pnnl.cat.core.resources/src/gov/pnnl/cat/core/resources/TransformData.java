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
package gov.pnnl.cat.core.resources;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.util.VeloConstants;

import org.apache.log4j.Logger;

/**
 */
public class TransformData {
  
  /**
   * The label for the raw text transform
   */
  // Transform names
  public static String TEXT = "Raw Text";
  
  // Transform extensions
  public static String TEXT_EXTENSION = ".txt";
  
  @SuppressWarnings("unused")
  private static Logger logger = CatLogger.getLogger(TransformData.class);
  private String errorMsg;
  private String transformerName;
  private String property;
  
  /**
   * Parse the transformation data from the special 
   * cat:transformations quick lookup property
   * @param name String
   * @param parent IFile
   * @throws ResourceException
   */
  public TransformData(String name, String property, String errorMessage) throws ResourceException {

    this.transformerName = name;
    this.errorMsg = errorMessage;
    if(property != null) {
      this.property = property;
    } else {
      this.property = VeloConstants.PROP_TEXT_TRANSFORMED_CONTENT; // default is text transform
    }
 
  }

  /**
   * Return the display label for the transformer
  
   * @return String
   */
  public String getTransformerName() {
    return this.transformerName;
  }
  
  /**
   * Return an error message if an error occurred during the transformation.
   * @return null if transform completed successfully. */
  public String getErrorMessage() {
    return this.errorMsg;
  }
  
  /**
   * Return the property name containing transformed content  
   * @return String
   */
  public String getContentProperty() {
    return this.property;	
  }
  
}
