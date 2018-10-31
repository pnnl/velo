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
package gov.pnnl.cat.criminalintel;

import java.util.Collection;
import java.util.Map;

import org.alfresco.repo.dictionary.constraint.AbstractConstraint;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class SecurityLevelConstraint extends AbstractConstraint {

  private static final Log logger = LogFactory.getLog(SecurityLevelConstraint.class);

  private static final String ERR_INVALID_VALUE = "d_dictionary.constraint.list_of_values.invalid_value";

  private static final String ERR_NON_STRING = "d_dictionary.constraint.string_length.non_string";

  public static CriminalIntelOptionsBean criminalIntelOptionsBean;

  /**
   * Method evaluateSingleValue.
   * @param value Object
   */
  @Override
  protected void evaluateSingleValue(Object value) {
    // convert the value to a String
    String valueStr = null;
    try {
      valueStr = DefaultTypeConverter.INSTANCE.convert(String.class, value);
    } catch (TypeConversionException e) {
      throw new ConstraintException(ERR_NON_STRING, value);
    }
    // check that the value is in the set of allowed values

    boolean found = false;
    Collection<String> validValues = criminalIntelOptionsBean.getAllowedSecurityLevelValues();
    for (String validValue : validValues) {
      if (validValue.equalsIgnoreCase(valueStr)) {
        found = true;
      }
    }
    if (!found) {
      throw new ConstraintException(ERR_INVALID_VALUE, value);
    }

  }

  /**
   * Method getSecurityLevels.
   * @return Map<String,String>
   */
  public Map<String, String> getSecurityLevels(){
    return criminalIntelOptionsBean.getSecurityLevels();
  }
  
  /**
   * Method getParameters.
   * @return Map<String,Object>
   * @see org.alfresco.service.cmr.dictionary.Constraint#getParameters()
   */
  @Override
  public Map<String, Object> getParameters() {
   return null;
  }

  /**
   * Method initialize.
   * @see org.alfresco.service.cmr.dictionary.Constraint#initialize()
   */
  @Override
  public void initialize() {
  }

}
