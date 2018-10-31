/*
 * gov.pnnl.velo
 * 
 * Author: Cody Curry
 */
package net.sf.jautodoc.preferences;

import net.sf.jautodoc.utils.BufferSizeDetector;

public final class AboutConstants {
  public static final boolean DEBUG_MODE = false;
  public static final String DEBUG = "[debug] ";
  
  public static final String DATE = "2013";

  //  public static final int DEFAULT_BUFFER_SIZE = 8192;
  public static final int DEFAULT_BUFFER_SIZE = new BufferSizeDetector(null).getBufferSize();

  public static final String DEFAULT_ABOUT_DIR_IN = "lib";
  //  public static final String DEFAULT_ABOUT_JAR_SUBDIR_IN = "META-INF";
  public static final String DEFAULT_ABOUT_DIR_OUT = "about_files";

  public static final String DEFAULT_ABOUT_OUT = "about.html";
  
  public static final String DEFAULT_ABOUT_TEMPLATE = "about_html_stub.txt";

  public static final char EXTENSION_SEPARATOR = '.';
  public static final String EXTENSION_SEPARATOR_STR = (new Character(EXTENSION_SEPARATOR)).toString();
  public static final String LIBRARY_EXTENSION = "jar";

  /**
   * MATCH_INTERESTING
   * Match interesting filename and path from root or META-INF/ directory
   * 
   * (?x)                       # Free-spacing mode to allow comments in regex
   * (?i)                       # Case-insensitive mode
   * ^                          # Match beginning of file path
   * (?>META-INF                # Match preceding (optional) directory
   * [\\/\\\\])?+               # Allow either type of file separator after preceding directory name
   * (?:LICENSE|NOTICE|README   # Match interesting filename without extension
   * |[^.\\r\\n]+\\.            #   or match any filename with extension
   * (?>TXT|HTML|RTF))          # Match interesting extension if applicable
   * $                          # Match end of file path
   */
  public static final String MATCH_INTERESTING_PATH = "(?i)^(?>(?>META-INF|LICENSE)[\\/\\\\])?+(?:LICENSE|NOTICE|README|[^\\.\\r\\n\\/\\\\]+\\.(?>TXT|HTML|RTF))$";
  //  public static final String[] INTERESTING_ITEMS = {".TXT", "LICENSE", "README", "NOTICE"};
}
