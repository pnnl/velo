package gov.pnnl.velo.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.webservice.util.ISO9075;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.Assert;

import com.thoughtworks.xstream.XStream;

import gov.pnnl.cat.core.util.NamespacePrefixResolver;
import gov.pnnl.velo.util.VeloConstants;

/**
 * Class for managing paths to resources on the content management system. Each segment of the path is fully qualified and has an associated namespace. Paths may be displayed with or without the namespace.
 * 
 * Methods and constructors are available to construct a <code>CmsPath</code> instance from a path string or by building the path incrementally, including the ability to append and prepend path elements.
 * <p>
 * Path elements supported:
 * <ul>
 * <li><b>/{namespace}name</b> fully qualified element</li>
 * <li><b>/name</b> element using default namespace</li>
 * </ul>
 * 
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class CmsPath implements Iterable<CmsPath.Segment>, Serializable, Comparable<CmsPath> {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_NAMESPACE = VeloConstants.NAMESPACE_CONTENT;
  private static final String APPLICATION_NAMESPACE = VeloConstants.NAMESPACE_APP;
  private static final String COMPANY_HOME_ASSOC_NAME = "company_home";
  private static final String COMPANY_HOME_PROP_NAME = "company home";

  private static final String REGEX_VALID_FILE_CHARS = "[^\\x00-\\x08\\x0A-\\x1f?*:\\\"<>|]";
  private static final String REGEX_SEPARATOR = "[/\\\\]";
  private static final String REGEX_PREFIX = "\\w{2,4}";
  private static final String REGEX_NAMESPACE = "\\{(\\w+?://\\S+?)\\}";
  private static final String REGEX_PREFIX_LOOKAHEAD = "(?=(?:" + REGEX_SEPARATOR + REGEX_PREFIX + "\\:)|(?:$))";
  private static final String REGEX_NAMESPACE_LOOKAHEAD = "(?=(?:" + REGEX_SEPARATOR + REGEX_NAMESPACE + ")|(?:$))";

  public static final String REGEX_FULLY_QUALIFIED_PATH = "(" + REGEX_SEPARATOR + "?\\{(\\w+?://\\S+?)\\})(" + REGEX_VALID_FILE_CHARS + "+?)" + REGEX_NAMESPACE_LOOKAHEAD;
  public static final String REGEX_FULLY_QUALIFIED_PATH_FULL_MATCH = "(" + REGEX_FULLY_QUALIFIED_PATH + ")+" + REGEX_SEPARATOR + "?$";
  public static final String REGEX_PREFIX_PATH = "" + REGEX_SEPARATOR + "?(\\w{2,4})\\:(" + REGEX_VALID_FILE_CHARS + "+?)" + REGEX_PREFIX_LOOKAHEAD;
  public static final String REGEX_PREFIX_PATH_FULL_MATCH = "(" + REGEX_PREFIX_PATH + ")+" + REGEX_SEPARATOR + "?$";

  public static final Pattern PATTERN_FULLY_QUALIFIED_PATH = Pattern.compile(REGEX_FULLY_QUALIFIED_PATH);
  public static final Pattern PATTERN_FULLY_QUALIFIED_PATH_FULL_MATCH = Pattern.compile(REGEX_FULLY_QUALIFIED_PATH_FULL_MATCH);
  public static final Pattern PATTERN_PREFIX_PATH = Pattern.compile(REGEX_PREFIX_PATH);
  public static final Pattern PATTERN_PREFIX_PATH_FULL_MATCH = Pattern.compile(REGEX_PREFIX_PATH_FULL_MATCH);

  public static Segment companyHomeSegment;

  static {
    // TODO: use constants instead of hard coding here
    companyHomeSegment = new Segment(APPLICATION_NAMESPACE, COMPANY_HOME_ASSOC_NAME);
  }

  private LinkedList<Segment> segments;

  public CmsPath() {
    // use linked list so as random access is not required, but both prepending and appending is
    segments = new LinkedList<Segment>();
  }

  /**
   * Constructor for CmsPath.
   * 
   * @param path
   *          CmsPath
   * @param validate
   *          boolean
   */
  public CmsPath(CmsPath path) {
    this();
    for (Segment segment : path.segments) {
      segments.addLast(segment);
    }
  }

  /**
   * We automatically parse out namespaces if provided inside {}. If namespaces are not present, the default namespace is applied.
   * 
   * Since this is a CMS path, we assume the separator character is always /
   * 
   * @param path
   */
  public CmsPath(String path) {
    this.init(path);
  }

  private void init(String path) {
    // use linked list so as random access is not required, but both prepending and appending is
    segments = new LinkedList<Segment>();
    
    // See if we have namespaces in our path:
    if (PATTERN_FULLY_QUALIFIED_PATH_FULL_MATCH.matcher(path).find()) {
      parseFullyQualifiedPath(path);

    } else if (PATTERN_PREFIX_PATH_FULL_MATCH.matcher(path).find()) {
      parsePrefixPath(path);

    } else {
      parseSimplePath(path);
    }
  }

  /**
   * @param path
   */
  private void parseSimplePath(String path) {
    // split on slashes
    path = path.replaceAll("^(/|\\\\)+", "").replaceAll("(/|\\\\)+$", "");
    String[] segmentStrings = path.split("(/|\\\\)+");
    int startIndex = 0;

    // Always use our company home segment to ensure name is normalized
    if (isCompanyHome(segmentStrings[0])) {
      startIndex = 1;
    }
    segments.addLast(companyHomeSegment);

    for (int i = startIndex; i < segmentStrings.length; i++) {
      String segmentString = segmentStrings[i];
      if (!segmentString.isEmpty()) {
        Segment segment = new Segment(segmentString);
        segments.addLast(segment);
      }
    }
  }

  public static boolean isCompanyHome(String name) {
    boolean ret = false;
    // could be either company_home OR Company Home
    if (name.toLowerCase().equals(COMPANY_HOME_PROP_NAME) || name.toLowerCase().equals(COMPANY_HOME_ASSOC_NAME)) {
      ret = true;
    }
    return ret;
  }

  /**
   * Method parseFullyQualifiedPath.
   * 
   * @param qualifiedPath
   *          String
   */
  private void parseFullyQualifiedPath(String qualifiedPath) {
    Matcher matcher = PATTERN_FULLY_QUALIFIED_PATH.matcher(qualifiedPath);

    while (matcher.find()) {
      // this group still has the curly brackets, so remove them
      String namespace = matcher.group(1);
      namespace = namespace.substring(2, namespace.length() - 1);

      String name = ISO9075.decode(matcher.group(3));
      Segment segment = new Segment(namespace, name);
      segments.addLast(segment);
    }
  }

  /**
   * Method parsePrefixPath.
   * 
   * @param qualifiedPath
   *          String
   */
  public void parsePrefixPath(String qualifiedPath) {
    Matcher matcher = PATTERN_PREFIX_PATH.matcher(qualifiedPath);

    while (matcher.find()) {
      String prefix = matcher.group(1);
      String name = ISO9075.decode(matcher.group(2));
      String namespace = NamespacePrefixResolver.getNamespaceURI(prefix);
      Segment segment = new Segment(namespace, name);
      segments.addLast(segment);
    }
  }

  /**
   * 
   * @return Returns a typed iterator over the path elements * @see java.lang.Iterable#iterator()
   */
  public Iterator<CmsPath.Segment> iterator() {
    return segments.iterator();
  }

  /**
   * Method getSegments.
   * 
   * @return LinkedList<Segment>
   */
  public LinkedList<Segment> getSegments() {
    return segments;
  }

  /**
   * Method getName.
   * 
   * @return String
   */
  public String getName() {
    return segments.getLast().getName();
  }

  public String getFileExtension() {
    String fileName = getName();
    return FilenameUtils.getExtension(fileName);
  }

  /**
   * Add a path element to the beginning of the path. This operation is useful in cases where a path is built by traversing up a hierarchy.
   * 
   * @param pathElement
   * 
   * @return Returns this instance of the path
   */
  public CmsPath prepend(CmsPath.Segment pathElement) {
    CmsPath newPath = new CmsPath(this);
    newPath.segments.addFirst(pathElement);
    return newPath;
  }

  /**
   * Merge the given path into the beginning of this path.
   * 
   * @param path
   * 
   * @return Returns this instance of the path
   */
  public CmsPath prepend(CmsPath path) {
    CmsPath newPath = new CmsPath(this);
    newPath.segments.addAll(0, path.segments);
    return newPath;
  }

  /**
   * Appends a segment to the end of the path
   * 
   * @param pathElement
   * 
   * @return Returns this instance of the path
   */
  public CmsPath append(CmsPath.Segment pathElement) {

    CmsPath newPath = new CmsPath(this);
    newPath.segments.addLast(pathElement);
    return newPath;
  }

  /**
   * Append a whole path to this path.
   * 
   * @param path
   * 
   * @return Returns this instance of the path
   */
  public CmsPath append(CmsPath path) {
    CmsPath newPath = new CmsPath(this);
    newPath.segments.addAll(path.segments);
    return newPath;
  }

  /**
   * Method append.
   * 
   * @param segmentName
   *          String
   * @return CmsPath
   */
  public CmsPath append(String segmentName) {
    Segment segment = new Segment(segmentName);
    return append(segment);
  }

  /**
   * 
   * @return Returns the first segment in the path or null if the path is empty
   */
  public Segment first() {
    return segments.getFirst();
  }

  /**
   * 
   * @return Returns the last segment in the path or null if the path is empty
   */
  public Segment last() {
    return segments.getLast();
  }

  /**
   * 
   * @return Returns the number of segments in this path
   */
  public int size() {
    return segments.size();
  }

  /**
   * Get the segment at position i
   * 
   * 
   * @param i
   *          int
   * @return Segment
   */
  public Segment get(int i) {
    return segments.get(i);
  }

  /**
   * Method toString.
   * 
   * @return String
   */
  @Override
  public String toString() {
    return toFullyQualifiedString();
  }

  /**
   * Method compareTo.
   * 
   * @param otherPath
   *          CmsPath
   * @return int
   */
  public int compareTo(CmsPath otherPath) {
    return toString().compareTo(otherPath.toString());
  }

  /**
   * 
   * @return Returns a human readable form of this path without any namespaces
   */
  public String toDisplayString() {
    return toAssociationNamePath(false);
  }

  /**
   * Method toDisplayString.
   * 
   * @param includeCompanyHome
   *          boolean
   * @return String
   */
  private String toAssociationNamePath(boolean includeCompanyHome) {
    StringBuilder sb = new StringBuilder(128);
    int i = 0;
    for (Segment segment : segments) {
      boolean includeSegment = true;
      if (i == 0 && includeCompanyHome == false && segment.equals(companyHomeSegment)) {
        includeSegment = false;
      }
      if (includeSegment) {
        sb.append("/");
        sb.append(segment.getName());
        i++;
      }
    }
    return sb.toString();
  }

  public String toAssociationNamePath() {
    return toAssociationNamePath(true);
  }

  /**
   * 
   * @return Returns a string path which includes namespace prefixes for each segment
   */
  public String toPrefixString() {
    return toPrefixString(false);
  }

  /**
   * Method toPrefixString.
   * 
   * @param encoded
   *          boolean
   * @return String
   */
  public String toPrefixString(boolean encoded) {
    StringBuilder sb = new StringBuilder(128);
    for (Segment segment : segments) {
      sb.append("/");
      sb.append(segment.getPrefixedString(encoded));
    }
    return sb.toString();
  }

  /**
   * 
   * @return Returns a string path which includes full namespace for each segment
   */
  public String toFullyQualifiedString() {
    StringBuilder sb = new StringBuilder(128);
    for (Segment segment : segments) {
      sb.append("/");
      sb.append(segment.getFullyQualifiedString());
    }
    return sb.toString();
  }

  /**
   * Return a new Path representing this path to the specified length
   * 
   * @param length
   * 
   * @return the sub-path
   */
  public CmsPath subPath(int length) {
    return subPath(0, length);
  }

  /**
   * Return a new Path representing this path to the specified length
   * 
   * @param start
   *          the starting index (0 based)
   * @param length
   *          the length of the sub-path
   * 
   * @return the sub-path
   */
  public CmsPath subPath(int start, int length) {

    if (start < 0 || start >= segments.size()) {
      throw new IndexOutOfBoundsException("Start index " + start + " must be between 0 and " + (segments.size() - 1));
    }
    if (length < 0 || length > (segments.size())) {
      throw new IndexOutOfBoundsException("Sub path length must be between 0 and " + (segments.size()));
    }

    CmsPath subPath = new CmsPath();
    int index = start;
    for (int i = 0; i < length; i++) {
      subPath.segments.addLast(this.get(index));
      index++;
    }
    return subPath;
  }

  /**
   * Method getParent.
   * 
   * @return CmsPath
   */
  public CmsPath getParent() {
    return removeLastSegments(1);
  }

  /**
   * Method isPrefixOf.
   * 
   * @param otherPath
   *          CmsPath
   * @return boolean
   */
  public boolean isPrefixOf(CmsPath otherPath) {

    // Root path is a prefix of all paths
    if (size() == 0) {
      return true;
    }

    int len = segments.size();
    if (len > otherPath.size()) {
      return false;
    }

    for (int i = 0; i < len; i++) {
      if (!get(i).equals(otherPath.get(i)))
        return false;
    }
    return true;

  }

  /**
   * Method getCommonAncestor.
   * 
   * @param anotherPath
   *          CmsPath
   * @return CmsPath
   */
  public CmsPath getCommonAncestor(CmsPath anotherPath) {
    Assert.isNotNull(anotherPath);
    int max = Math.min(this.size(), anotherPath.size());
    int count = 0;
    // loop thru each segment of this path and continue until
    // we find a segment that doesn't match
    while (count < max && this.get(count).equals(anotherPath.get(count))) {
      count++;
    }
    return this.subPath(0, count - 1);

  }

  /**
   * Method removeLastSegments.
   * 
   * @param num
   *          int
   * @return CmsPath
   */
  public CmsPath removeLastSegments(int num) {
    int length = size() - num;
    if (length > 0) {
      return subPath(length);

    } else if (length == 0) {
      return new CmsPath(); // root path

    } else {
      return null; // no path
    }
  }

  /**
   * Override equals to check equality of Path instances
   * 
   * @param o
   *          Object
   * @return boolean
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CmsPath)) {
      return false;
    }
    CmsPath other = (CmsPath) o;
    return this.segments.equals(other.segments);
  }

  /**
   * Override hashCode to check hash equality of Path instances
   * 
   * @return int
   */
  @Override
  public int hashCode() {
    return segments.hashCode();
  }

  /**
   * Represents a path element.
   * <p>
   * In <b>/x/y/z</b>, elements are <b>x</b>, <b>y</b> and <b>z</b>.
   * 
   * @version $Revision: 1.0 $
   */
  public static class Segment implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name = "";
    private String namespace = DEFAULT_NAMESPACE;

    /**
     * Constructor for Segment.
     * 
     * @param namespace
     *          String
     * @param name
     *          String
     */
    public Segment(String namespace, String name) {
      super();
      this.namespace = namespace;
      this.name = name;
    }

    /**
     * Constructor for Segment.
     * 
     * @param name
     *          String
     */
    public Segment(String name) {
      super();
      if (PATTERN_FULLY_QUALIFIED_PATH_FULL_MATCH.matcher(name).find()) {
        parseFullyQualifiedName(name);

      } else if (PATTERN_PREFIX_PATH_FULL_MATCH.matcher(name).find()) {
        parsePrefixName(name);

      } else {
        this.name = name;
      }
    }

    /**
     * Method parseFullyQualifiedName.
     * 
     * @param qualifiedName
     *          String
     */
    private void parseFullyQualifiedName(String qualifiedName) {
      Matcher matcher = PATTERN_FULLY_QUALIFIED_PATH.matcher(qualifiedName);

      while (matcher.find()) {
        // this group still has the curly brackets, so remove them
        String namespace = matcher.group(1);
        namespace = namespace.substring(1, namespace.length() - 1);
        String name = ISO9075.decode(matcher.group(3));
        this.name = name;
        this.namespace = namespace;
      }
    }

    /**
     * Method parsePrefixName.
     * 
     * @param qualifiedName
     *          String
     */
    public void parsePrefixName(String qualifiedName) {
      Matcher matcher = PATTERN_PREFIX_PATH.matcher(qualifiedName);

      while (matcher.find()) {
        String prefix = matcher.group(1);
        String name = ISO9075.decode(matcher.group(2));
        String namespace = NamespacePrefixResolver.getNamespaceURI(prefix);
        this.name = name;
        this.namespace = namespace;
      }
    }

    /**
     * 
     * @return Returns the path element portion including leading '/' and never null
     */
    public String getName() {
      return name;
    }

    /**
     * Method setName.
     * 
     * @param name
     *          String
     */
    public void setName(String name) {
      this.name = name;
    }

    /**
     * Method getNamespace.
     * 
     * @return String
     */
    public String getNamespace() {
      return namespace;
    }

    /**
     * 
     * @param encoded
     *          boolean
     * @return the path element portion (with namespaces converted to prefixes)
     */
    public String getPrefixedString(boolean encoded) {
      String prefix = NamespacePrefixResolver.getPrefix(namespace);
      String nm = name;
      if (encoded) {
        nm = ISO9075.encode(name);
      }
      return prefix + ":" + nm;
    }

    /**
     * Method getFullyQualifiedString.
     * 
     * @return String
     */
    public String getFullyQualifiedString() {
      return "{" + namespace + "}" + name;
    }

    /**
     * Method getFileExtension.
     * 
     * @return String
     */
    public String getFileExtension() {
      int index = name.lastIndexOf("."); //$NON-NLS-1$
      if (index == -1) {
        return null;
      }
      return name.substring(index + 1);
    }

    /**
     * 
     * @return String
     * @see #getName()
     */
    @Override
    public String toString() {
      return getName();
    }

    /**
     * Method equals.
     * 
     * @param o
     *          Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof CmsPath.Segment)) {
        return false;
      }
      CmsPath.Segment other = (CmsPath.Segment) o;
      return this.name.equals(other.name) && ((this.namespace == null && other.namespace == null) || (this.namespace.equals(other.namespace)));
    }

    /**
     * Method hashCode.
     * 
     * @return int
     */
    @Override
    public int hashCode() {
      return getFullyQualifiedString().hashCode();
    }

  }

  //we do NOT want xstream to call defaultWriteObject or we'll get a very large string for long paths, each segment qualified, etc.  Instead just write out the string representation of a path
  private void writeObject(ObjectOutputStream out) throws IOException {
    //out.defaultWriteObject();  
    String pathString = this.toFullyQualifiedString();
    out.writeObject(pathString);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    //in.defaultReadObject();
    String pathString = (String) in.readObject();

    this.init(pathString);
  }
  
  public static void main(String[] args) {
    CmsPath test = new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home");
    
    XStream xstream = new XStream();

   
    xstream.autodetectAnnotations(true);
    xstream.toXML(test, System.out);
    
    String serialized = "<gov.pnnl.velo.model.CmsPath serialization=\"custom\">  <gov.pnnl.velo.model.CmsPath>    <string>/{http://www.alfresco.org/model/application/1.0}company_home</string>  </gov.pnnl.velo.model.CmsPath></gov.pnnl.velo.model.CmsPath>";
    CmsPath test2 = (CmsPath) xstream.fromXML(serialized);

    //this is how the the old xml looked before overriding read/write object methods
//     String serialized = "<gov.pnnl.velo.model.CmsPath>  <segments>    <gov.pnnl.velo.model.CmsPath_-Segment>      <name>company_home</name>      <namespace>http://www.alfresco.org/model/application/1.0</namespace>    </gov.pnnl.velo.model.CmsPath_-Segment>  </segments></gov.pnnl.velo.model.CmsPath>";
//     CmsPath test2 = (CmsPath) xstream.fromXML(serialized);
    
    
    if(test.equals(test2)){
      System.out.println("\n\npassed");
    }else{
      System.out.println("\n\nfailed");
    }
  }

}
