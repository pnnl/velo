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
package com.centerkey.utils;

import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * <b>Bare Bones Browser Launch for Java</b><br>
 * Utility class to open a web page from a Swing application
 * in the user's default browser.<br>
 * Supports: Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7<br>
 * Example Usage:<code><br> &nbsp; &nbsp;
 *    String url = "http://www.google.com/";<br> &nbsp; &nbsp;
 *    BareBonesBrowserLaunch.openURL(url);<br></code>
 * Latest Version: <a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a><br>
 * Author: Dem Pilafian<br>
 * Public Domain Software -- Free to Use as You Like
 * @version 3.1, June 6, 2010
 */
public class BareBonesBrowserLaunch {
  static final Logger logger = Logger.getLogger(BareBonesBrowserLaunch.class);

  static final String[] browsers = { "google-chrome", "firefox", "opera",
    "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla" };
  static final String errMsg = "Error attempting to launch web browser";

  /**
   * Opens the specified web page in the user's default browser
   * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
   */
  public static void openURL(String url) {
    try {  //attempt to use Desktop library from JDK 1.6+
      Class<?> d = Class.forName("java.awt.Desktop");
      d.getDeclaredMethod("browse", new Class[] {java.net.URI.class}).invoke(
          d.getDeclaredMethod("getDesktop").invoke(null),
          new Object[] {java.net.URI.create(url)});
      //above code mimicks:  java.awt.Desktop.getDesktop().browse()
      logger.debug("Opened browser using java.awt.Desktop.getDesktop().browse()");

    } catch (Exception ignore) {  //library not available or failed
      logger.info("Java 1.6 not available, trying to find system browser.");
      String osName = System.getProperty("os.name");
      logger.debug("os.name = " + osName);
      try {
        if (osName.startsWith("Mac OS")) {
          Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
              "openURL", new Class[] {String.class}).invoke(null,
                  new Object[] {url});

        } else if (osName.startsWith("Windows")) {
          Runtime.getRuntime().exec(
              "rundll32 url.dll,FileProtocolHandler " + url);
        } else { //assume Unix or Linux
          String browser = null;
          for (String b : browsers) {
            if (browser == null && Runtime.getRuntime().exec(new String[]
                                                                        {"which", b}).getInputStream().read() != -1) {
              Runtime.getRuntime().exec(new String[] {browser = b, url});
            }
          }
          if (browser == null) {
            throw new Exception(Arrays.toString(browsers));
          }
        }
      }
      catch (Throwable e) {
        logger.error("Failed to launch browser.", e);
      }
    }
  }

}
