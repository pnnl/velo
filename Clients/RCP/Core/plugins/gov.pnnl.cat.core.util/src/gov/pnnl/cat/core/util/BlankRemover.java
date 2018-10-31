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
package gov.pnnl.cat.core.util;

import gov.pnnl.cat.logging.CatLogger;

import org.apache.log4j.Logger;

/**
 */
public class BlankRemover
{
  protected static Logger logger = CatLogger.getLogger(BlankRemover.class);
    /* remove leading whitespace */
    /**
     * Method ltrim.
     * @param source String
     * @return String
     */
    public static String ltrim(String source) {
        return source.replaceAll("^\\s+", "");
    }

    /* remove trailing whitespace */
    /**
     * Method rtrim.
     * @param source String
     * @return String
     */
    public static String rtrim(String source) {
        return source.replaceAll("\\s+$", "");
    }

    /* replace multiple whitespaces between words with single blank */
    /**
     * Method itrim.
     * @param source String
     * @return String
     */
    public static String itrim(String source) {
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }

    /* remove all superfluous whitespaces in source string */
    /**
     * Method trim.
     * @param source String
     * @return String
     */
    public static String trim(String source) {
        return itrim(ltrim(rtrim(source)));
    }

    /**
     * Method lrtrim.
     * @param source String
     * @return String
     */
    public static String lrtrim(String source){
        return ltrim(rtrim(source));
    }

    /**
     * Method main.
     * @param args String[]
     */
    public static void main(String[] args){
        String oldStr =
         ">     <1-2-1-2-1-2-1-2-1-2-1-----2-1-2-1-2-1-2-1-2-1-2-1-2>   <";
        String newStr = oldStr.replaceAll("-", " ");
//        System.out.println(newStr);
//        System.out.println(ltrim(newStr));
//        System.out.println(rtrim(newStr));
//        System.out.println(itrim(newStr));
//        System.out.println(lrtrim(newStr));
          logger.debug(newStr);
          logger.debug(ltrim(newStr));
          logger.debug(rtrim(newStr));
          logger.debug(itrim(newStr));
          logger.debug(lrtrim(newStr));
    }


}
