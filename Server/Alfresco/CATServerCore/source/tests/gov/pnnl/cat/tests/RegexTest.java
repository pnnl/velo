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
package gov.pnnl.cat.tests;

public class RegexTest {
  private static String[] intelTypes = null;
  private static String[] securityLevels = null;
 
  /**
   * @param args
   */
  public static void main(String[] args) {

    String uuidRegex = ".*_[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}";
    String categoryName = "World Nuclear Sites_a3fd407a-33e6-11dc-a5f5-c133b6a3f528";
    //String categoryName = "World Nuclear Sites";
    if(categoryName.matches(uuidRegex)) {
      System.out.println("category is a taxonomy");
      
    } else {
      System.out.println("category is not a taxonomy");
    }
    String optionsString = "intelTypes: option A that has a duration of 30 Days,option B that has a duration of 90 Days,option C that has a duration of 1 Year,option D that has a duration of 5 Years,\nsecurityLevels: Sensitive (description of 'Sesitive'),Confidential (description of 'Confidential'),Restricted (description of 'Restricted'),Unclassified (description of 'Unclassified'),";
    intelTypes = optionsString.substring("intelTypes:".length(), optionsString.indexOf("\n")).split(",");
    securityLevels = optionsString.substring(optionsString.indexOf("securityLevels:") + "securityLevels:".length() ).split(",");
     
    for (String type : intelTypes) {
      System.out.println("type: " + type);
    }for (String type : securityLevels) {
      System.out.println("level: " + type);
    }
    
  }

}
