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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * Format {@link Date} into date and time Strings.
 * 
 * TODO Consider adding CAT preference for these formats, and use it instead of hard coded values!
 * @version $Revision: 1.0 $
 */
public class DateFormatUtility {

  public static final String MMY_DD_YY_PATTERN = "MM-dd-yy";

  public static final String DATE_TIME_PATTERN = "MM-dd-yy hh_mm aaa";

  public static final String DEFAULT_DATE_TIME_PATTERN = "MM/dd/yyyy HH:mm:ss z";

  public static final String TIME_PATTERN = "hh:mm aaa";

  public static final SimpleDateFormat ALFRESCO_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public static final SimpleDateFormat JCR_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

  public static final SimpleDateFormat MM_DD_YY_FORMAT = new SimpleDateFormat(MMY_DD_YY_PATTERN);

  public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat(DATE_TIME_PATTERN);

  public static final SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_PATTERN);

  public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(TIME_PATTERN);

  /**
   * Format the given {@link Date} as a date (month, day, year) and time (hours, minutes, seconds) according to the Alfresco date/time format.
   * 
   * @param date
   *          {@link Date} to format
  
   * @return formatted time, or an empty String if the provided date is null */
  public static String formatAlfrescoDateTime(Date date) {
    String format = StringUtils.EMPTY;

    if (date != null) {
      format = ALFRESCO_DATE_TIME_FORMAT.format(date);
      format = getAlfrescoFormatFromJava(format);
    }

    return format;
  }

  /**
   * Format the given {@link Date} as a date (month, day, year) and time (hours, minutes, AM/M) according to the default format.
   * 
   * @param date
   *          {@link Date} to format
  
   * @return formatted date/time, or an empty String if the provided date is null */
  public static String formatDateTime(Date date) {
    String format = StringUtils.EMPTY;

    if (date != null) {
      format = DATE_TIME_FORMAT.format(date);
    }

    return format;
  }

  /**
   * Format the given {@link Date} as a date (month, day, year) and time (hours, minutes, seconds) according to the default date/time format.
   * 
   * @param date
   *          {@link Date} to format
  
   * @return formatted time, or an empty String if the provided date is null */
  public static String formatDefaultDateTime(Date date) {
    String format = StringUtils.EMPTY;

    if (date != null) {
      format = DEFAULT_DATE_TIME_FORMAT.format(date);
    }

    return format;
  }

  /**
   * Format the given {@link Date} as a date (month, day, year) according to the default format.
   * 
   * @param date
   *          {@link Date} to format
  
   * @return formatted date, or an empty String if the provided date is null */
  public static String formatMmDdYy(Date date) {
    String format = StringUtils.EMPTY;

    if (date != null) {
      format = MM_DD_YY_FORMAT.format(date);
    }

    return format;
  }

  /**
   * Format the given {@link Date} as time (hours, minutes, AM/M) according to the default format.
   * 
   * @param date
   *          {@link Date} to format
  
   * @return formatted time, or an empty String if the provided date is null */
  public static String formatTime(Date date) {
    String format = StringUtils.EMPTY;

    if (date != null) {
      format = TIME_FORMAT.format(date);
    }

    return format;
  }

  /**
   * Reformat the Alfresco date/time from the given Java format.
   * 
   * @param javaFormat
   *          String Java formatted date/time
  
   * @return String formatted Alfresco date/time */
  private static String getAlfrescoFormatFromJava(String javaFormat) {
    return javaFormat.replaceFirst("([-+]\\d\\d)(\\d\\d)$", "$1:$2");
  }

  /**
   * Reformat the Java date/time from the given Alfresco format.
   * 
   * @param alfrescoFormat
   *          String Alfresco formatted date/time
  
   * @return String formatted Java date/time */
  private static String getJavaFormatFromAlfresco(String alfrescoFormat) {
    return alfrescoFormat.replaceFirst("([-+]\\d\\d):(\\d\\d)$", "$1$2");
  }

  /**
   * Parse the given String as date/time in the default format.
   * 
   * @param source
   *          String date to parse
  
   * @return {@link Date}, or null if could not parse string */
  public static Date parseDateTime(String source) {
    Date date = null;

    if (StringUtils.isNotBlank(source)) {
      try {
        date = DATE_TIME_FORMAT.parse(source);
      } catch (ParseException e) {
        date = null;
      }
    }

    return date;
  }

  /**
   * Parse the given String as time in the default date/time format.
   * 
   * @param source
   *          String date to parse
  
   * @return {@link Date}, or null if could not parse string */
  public static Date parseDefaultDateTime(String source) {
    Date date = null;

    if (StringUtils.isNotBlank(source)) {
      try {
        date = DEFAULT_DATE_TIME_FORMAT.parse(source);
      } catch (ParseException e) {
        date = null;
      }
    }

    return date;
  }

  /**
   * Parse the given String as time in the JCR date/time format.
   * 
   * @param source
   *          String date to parse
  
   * @return {@link Date}, or null if could not parse string */
  public static Date parseJcrDate(String source) {
    Date date = null;

    try {
      // note: this formatter will not work with this sort of date:
      // 2006-09-13T16:08:25.591-07:00,
      // handle that in the catch below...
      date = JCR_DATE_TIME_FORMAT.parse(source);
    } catch (ParseException Pex) {
    } catch (IllegalArgumentException IAex) {
    }

    if (date == null) {
      // if we get here, lets try another format...
      try {
        // seems like the problem is the last colon in the GMT
        // so remove that silly last colon:
        source = source.substring(0, source.lastIndexOf(":")) + source.substring(source.lastIndexOf(":") + 1, source.length());
        date = ALFRESCO_DATE_TIME_FORMAT.parse(source);
      } catch (ParseException Pex2) {
      }
    }

    return date;
  }

  /**
   * Parse the given String as a Date in the default format.
   * 
   * @param source
   *          String date to parse
  
   * @return {@link Date}, or null if could not parse string */
  public static Date parseMmDdYy(String source) {
    Date date = null;

    if (StringUtils.isNotBlank(source)) {
      try {
        date = MM_DD_YY_FORMAT.parse(source);
      } catch (ParseException e) {
        date = null;
      }
    }

    return date;
  }

  /**
   * Parse the given String as time in the default time format.
   * 
   * @param source
   *          String date to parse
  
   * @return {@link Date}, or null if could not parse string */
  public static Date parseTime(String source) {
    Date date = null;

    if (StringUtils.isNotBlank(source)) {
      try {
        date = TIME_FORMAT.parse(source);
      } catch (ParseException e) {
        date = null;
      }
    }

    return date;
  }

  /**
   * Reformat the given String in the 'from' format into the 'to' format.
   * 
   * @param source
   *          String date to parse
   * @param from
   *          {@link DateFormat} the source is in
   * @param to
   *          {@link DateFormat} to reformat to
  
   * @return reformatted String, or an empty String if the provided source is null */
  public static String reformat(String source, DateFormat from, DateFormat to) {
    String format = StringUtils.EMPTY;

    if (StringUtils.isNotBlank(source)) {
      try {
        String formattedSource = source;

        if (ALFRESCO_DATE_TIME_FORMAT.equals(from)) {
          formattedSource = getJavaFormatFromAlfresco(formattedSource);
        }

        Date fromDate = from.parse(source);
        format = to.format(fromDate);

        if (ALFRESCO_DATE_TIME_FORMAT.equals(to)) {
          format = getAlfrescoFormatFromJava(format);
        }
      } catch (ParseException e) {
        format = StringUtils.EMPTY;
      }
    }

    return format;
  }

  /**
   * Cannot instantiate.
   */
  private DateFormatUtility() {
    super();
  }
}
