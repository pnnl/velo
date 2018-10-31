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
package gov.pnnl.velo.core.util;

import java.util.List;

/**
 */
public class StringUtil {
  /**
   * Consolidate a list into a delimited string.
   * @param coll
   * @param delimiter
  
   * @return String
   */
  public static String join(List<String> coll, String delimiter)
  {
    if (coll.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();

    for (String x : coll) {
      sb.append(x + delimiter);
    }
    sb.delete(sb.length()-delimiter.length(), sb.length());

    return sb.toString();
  }

  /**
   * Method join.
   * @param coll String[]
   * @param delimiter String
   * @return String
   */
  public static String join(String[] coll, String delimiter)
  {
    if (coll.length == 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder();

    for (String x : coll) {
      sb.append(x + delimiter);
    }
    sb.delete(sb.length()-delimiter.length(), sb.length());

    return sb.toString();
  }

  /**
   * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
   *
   * <pre>
   * StringUtils.isNotBlank(null)      = false
   * StringUtils.isNotBlank("")        = false
   * StringUtils.isNotBlank(" ")       = false
   * StringUtils.isNotBlank("bob")     = true
   * StringUtils.isNotBlank("  bob  ") = true
   * </pre>
   *
   * @param str  the String to check, may be null
  
   * @return <code>true</code> if the String is
   *  not empty and not null and not whitespace */
  public static boolean isNotBlank(String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return false;
    }
    for (int i = 0; i < strLen; i++) {
      if ((Character.isWhitespace(str.charAt(i)) == false)) {
        return true;
      }
    }
    return false;
  }
}
