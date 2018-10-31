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
package gov.pnnl.cat.server.webservice.group;

import gov.pnnl.cat.util.NodeUtils;

import java.util.List;

/**
 * Use this path class to help with team paths so we can identify subgroups -
 * TODO - get rid of paths in group names - we can navigate children to better identify group hierarchy
 * Path always has a starting separator and no trailing separator
 * @version $Revision: 1.0 $
 */
public class Path { 
 
  // path segments
  private String[] segments;

  /**
   * 
   * @param pathString
   */
  public Path(String pathString) {
    // split the segments
    List<String> segList = NodeUtils.splitAllPaths(pathString);
    segments = segList.toArray(new String[segList.size()]);
  }
  
  /**
   * Constructor for Path.
   * @param segments String[]
   */
  protected Path(String[] segments) {
    this.segments = segments;
  }

  /**
   * Method append.
   * @param tail Path
   * @return Path
   * @see gov.pnnl.cat.util.Path#append(Path)
   */
  public Path append(Path tail) {
    // check if we are appending an empty path
    if (tail == null || tail.segmentCount() == 0) {
      return this;
    }
    
    //concatenate the two segment arrays
    int numSegments = segments.length;
    int tailLen = tail.segmentCount();
    String[] newSegments = new String[numSegments + tailLen];
    System.arraycopy(segments, 0, newSegments, 0, numSegments);
    for (int i = 0; i < tailLen; i++) {
      newSegments[numSegments + i] = tail.segment(i);
    }
    //use my leading separators and the tail's trailing separator
    return new Path(newSegments);
  }
  
  /* (non-Javadoc)
   * @see gov.pnnl.cat.util.Path#append(java.lang.String)
   */
  public Path append(String path) {
    return append(new Path(path));
  }

  /* (non-Javadoc)
   * @see gov.pnnl.cat.util.Path#segments()
   */
  public String[] segments() {
    String[] copiedSegments = new String[segments.length];
    System.arraycopy(segments, 0, copiedSegments, 0, segments.length);
    return copiedSegments;
  }
  
  /**
   * Method segmentCount.
   * @return int
   * @see gov.pnnl.cat.util.Path#segmentCount()
   */
  public int segmentCount() {
    return segments.length;
  }

  /**
   * Method segment.
   * @param i int
   * @return String
   * @see gov.pnnl.cat.util.Path#segment(int)
   */
  public String segment(int i) {
    return segments[i];
  }

  /**
   * Method removeLastSegments.
   * @param count int
   * @return Path
   * @see gov.pnnl.cat.util.Path#removeLastSegments(int)
   */
  public Path removeLastSegments(int count) {
    if (count == 0) {
      return this;
    }
    if (count >= segments.length) {
      //remove all segments
      return new Path(new String[0]);
    }
    int newSize = segments.length - count;
    String[] newSegments = new String[newSize];
    System.arraycopy(this.segments, 0, newSegments, 0, newSize);
    return new Path(newSegments);
  }
  
  /**
   * Method lastSegment.
   * @return String
   * @see gov.pnnl.cat.util.Path#lastSegment()
   */
  public String lastSegment() {
    if(segments.length == 0) {
      return null;
    } else {
      return segments[segments.length-1];
    }
  }
  
  
  
  
  public String getName() {
    return lastSegment();
  }

  
  public Path getParent() {
    return removeLastSegments(1);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  
  public String toString() {
    StringBuilder ret = new StringBuilder();
    ret.append("/");
    for (int i = 0; i < segments.length; i++) {
       ret.append(segments[i]);
       if(i != (segments.length -1)) {
         ret.append("/");
       }
    }
    return ret.toString();
  }
  
  /**
   * For testing
   * @param args
   */
  public static void main(String[] args) {
    Path path = new Path("/a/b/c/d");
    path = path.append("e/f/g/h");
    System.out.println("concatenated path = " + path.toString());
    System.out.println("num segments = " + path.segmentCount());
    

  }
  
}
