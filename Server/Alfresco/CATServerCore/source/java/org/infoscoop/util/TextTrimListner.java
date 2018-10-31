package org.infoscoop.util;

/**
 * Listner that trims trailing white space after \n 
 * @author Chandrika Sivaramakrishnan
 *
 */
 public class TextTrimListner implements Xml2JsonListener {
    public String text(String text) throws Exception {
      return text.trim().replaceAll("(\r\n|\n)[ \t]+", "$1");
    }
  }