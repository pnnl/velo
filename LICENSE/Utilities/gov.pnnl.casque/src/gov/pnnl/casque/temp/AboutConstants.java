package gov.pnnl.casque.temp;

public final class AboutConstants {
  public static final boolean DEBUG_MODE = true;
  public static final String DEBUG = "[debug] ";

  //  public static final int DEFAULT_BUFFER_SIZE = 8192;
  public static final int DEFAULT_BUFFER_SIZE = new BufferSizeDetector(null).getBufferSize();

  public static final String DEFAULT_ABOUT_DIR_IN = "lib";
  //  public static final String DEFAULT_ABOUT_JAR_SUBDIR_IN = "META-INF";
  public static final String DEFAULT_ABOUT_DIR_OUT = "about_files";

  public static final String DEFAULT_ABOUT_TEMPLATE = "about_html.vm";
  public static final String DEFAULT_ABOUT_OUT = "about.html";

  public static final char EXTENSION_SEPARATOR = '.';
  public static final String EXTENSION_SEPARATOR_STR = (new Character(EXTENSION_SEPARATOR)).toString();
  public static final String LIBRARY_EXTENSION = "jar";

  /**
   * MATCH_INTERESTING
   * Match interesting filename and path from root or META-INF/ directory
   * 
   * (?x)                       # Free-spacing mode to allow comments in regex
   * (?i)                       # Case-insensitive mode
   * ^(?>META-INF               # Match optional, preceding directory at beginning of file path
   * [\\/\\\\])?+               # Allow either type of file separator after preceding directory name
   * (?:LICENSE|NOTICE|README   # Match interesting filename without extension
   * |[^.\\r\\n]+\\.            # or match any filename with extension
   * (?>TXT|HTML))              # match interesting extension if applicable
   * $                          # match end of file path
   */
  public static final String MATCH_INTERESTING_PATH = "(?i)^(?>META-INF[\\/\\\\])?+(?:LICENSE|NOTICE|README|[^\\.\\r\\n\\/\\\\]+\\.(?>TXT|HTML))$";
  //  public static final String[] INTERESTING_ITEMS = {".TXT", "LICENSE", "README", "NOTICE"};
}
