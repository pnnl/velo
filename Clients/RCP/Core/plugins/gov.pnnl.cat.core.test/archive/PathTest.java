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
package gov.pnnl.cat.core.resources.tests;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.CmsPath;

import java.util.Iterator;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 */
public class PathTest extends TestCase {
  protected static Logger logger = CatLogger.getLogger(PathTest.class);

  public void testAppend() {
    CmsPath a = new CmsPath("/foo/bar");
    CmsPath b = new CmsPath("another/path");
    CmsPath c = a.append(b);
    assertEquals("/foo/bar/another/path", c.toString());

    a = new CmsPath("/foo/bar/tricky:directory");
    b = new CmsPath("yet/another/path");
    c = a.append(b);
    assertEquals("/foo/bar/tricky:directory/yet/another/path", c.toString());

    a = new CmsPath("/foo:bar");
    c = a.append("something:else");
    assertEquals("/foo:bar/something:else", c.toString());

    a = new CmsPath("/foo:bar");
    b = new CmsPath("something:else");
    c = a.append(b);
    assertEquals("/foo:bar/something:else", c.toString());

    a = new CmsPath("/ReferenceLibrary/bacworthChapters/terrorism/atomizer1.jpg");
    c = a.append("jcr:content");
    assertEquals("/ReferenceLibrary/bacworthChapters/terrorism/atomizer1.jpg/jcr:content", c.toString());

    a = new CmsPath("/foo/bar");
    c = a.append("something:else");
    assertEquals("/foo/bar/something:else", c.toString());
  }

  public void testGetCommonAncestor(){
    CmsPath a = new CmsPath("/foo/bar");
    CmsPath b = new CmsPath("/foo/bar/another/path");
    assertEquals("/foo/bar", a.getCommonAncestor(b).toString());
    assertEquals("/foo/bar", b.getCommonAncestor(a).toString());

    a = new CmsPath("/zoe/foo/bar");
    b = new CmsPath("/foo/bar/another/path");
    assertEquals("/", a.getCommonAncestor(b).toString());
    assertEquals("/", b.getCommonAncestor(a).toString());
    assertEquals(a.toString(), a.getCommonAncestor(a).toString());
  }
  
  public void testCompareTo() {
    String[] pathStrings = {"/a", "/a/b", "/a/b/c", "/x", "/z"};
    CmsPath[] paths = new CmsPath[pathStrings.length];
    TreeMap map = new TreeMap();
    CmsPath path;
    String a = "aaa";
    String b = "bbb";
    //System.out.println(a.compareTo(b));
    logger.debug(a.compareTo(b));

    for (int i = 0; i < pathStrings.length; i++) {
      paths[i] = new CmsPath(pathStrings[i]);
      map.put(paths[i], paths[i]);
      if (i > 0) {
        assertTrue(paths[i].compareTo(paths[i-1]) > 0);
      }
    }

//    System.out.println("FIRST KEY: " + map.firstKey());
//    System.out.println("SIZE: " + map.size() + "\n");
    logger.debug("FIRST KEY: " + map.firstKey());
    logger.debug("SIZE: " + map.size() + "\n");

    for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
      path = (CmsPath) iter.next();
      //System.out.println(path);
      logger.debug(path);
    }

    path = paths[1];
    //System.out.println("\ngreater than or equal to " + path);
    logger.debug("\ngreater than or equal to " + path);
    for (Iterator iter = map.tailMap(new CmsPath(path.toString() + "\0")).keySet().iterator(); iter.hasNext();) {
      path = (CmsPath) iter.next();
      //System.out.println(path);
      logger.debug(path);
    }

    path = paths[1];
    //System.out.println("\nless than " + path);
    logger.debug("\nless than" + path);
    for (Iterator iter = map.headMap(path).keySet().iterator(); iter.hasNext();) {
      path = (CmsPath) iter.next();
      //System.out.println(path);
      logger.debug(path);
    }
  }

  public void testRegularExpressions() {
    String fullyQualifiedPath1 = "/{http://www.alfresco.org/model/application/1.0}company_home/" +
      "{http://www.alfresco.org/model/content/1.0}foo/" +
      "{http://www.alfresco.org/model/content/1.0}bar";
    String fullyQualifiedPath2 = fullyQualifiedPath1 + "/";
    String prefixPath1 = "/app:company_home/cm:foo/cm:bar";
    String prefixPath2 = prefixPath1 + "/";

    assertTrue(CmsPath.PATTERN_FULLY_QUALIFIED_PATH.matcher(fullyQualifiedPath1).find());
    assertTrue(CmsPath.PATTERN_FULLY_QUALIFIED_PATH.matcher(fullyQualifiedPath2).find());
    assertTrue(CmsPath.PATTERN_PREFIX_PATH.matcher(prefixPath1).find());
    assertTrue(CmsPath.PATTERN_PREFIX_PATH.matcher(prefixPath2).find());

    assertTrue(CmsPath.PATTERN_FULLY_QUALIFIED_PATH_FULL_MATCH.matcher(fullyQualifiedPath1).matches());
    assertTrue(CmsPath.PATTERN_FULLY_QUALIFIED_PATH_FULL_MATCH.matcher(fullyQualifiedPath2).matches());
    assertTrue(CmsPath.PATTERN_PREFIX_PATH_FULL_MATCH.matcher(prefixPath1).matches());
    assertTrue(CmsPath.PATTERN_PREFIX_PATH_FULL_MATCH.matcher(prefixPath2).matches());
  }

  public void testQualifiedPaths() {
    try {
      new CmsPath("/foo/bar/a/b/c");
      fail("should have thrown exception");
    } catch (IllegalArgumentException expected) {
      // expected behavior
    }

    CmsPath path = new CmsPath("/app:company_home/cm:foo/cm:bar");
    assertEquals("/app:company_home/cm:foo/cm:bar", path.toDisplayString());
    assertEquals("/app:company_home/cm:foo/cm:bar", path.toPrefixString());

    // different separator (/ vs. \)
    path = new CmsPath("\\app:company_home\\cm:foo\\cm:bar");
    assertEquals("/foo/bar", path.toDisplayString());
    assertEquals("/app:company_home/cm:foo/cm:bar", path.toPrefixString());

    CmsPath qPath = new CmsPath("/app:company_home/cm:foo/cm:bar");
    assertEquals("/foo/bar", qPath.toDisplayString());
    assertEquals("/app:company_home/cm:foo/cm:bar", qPath.toPrefixString());
    assertEquals("/{http://www.alfresco.org/model/application/1.0}company_home/" +
    		"{http://www.alfresco.org/model/content/1.0}foo/" +
    		"{http://www.alfresco.org/model/content/1.0}bar",
    		qPath.toString());

    CmsPath fqPath = new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home/" +
        "{http://www.alfresco.org/model/content/1.0}foo/" +
        "{http://www.alfresco.org/model/content/1.0}bar");
    assertEquals("/foo/bar", fqPath.toDisplayString());
    assertEquals("/app:company_home/cm:foo/cm:bar", fqPath.toPrefixString());
    assertEquals("/{http://www.alfresco.org/model/application/1.0}company_home/" +
        "{http://www.alfresco.org/model/content/1.0}foo/" +
        "{http://www.alfresco.org/model/content/1.0}bar",
        fqPath.toString());

    fqPath = new CmsPath("/{http://www.alfresco.org/model/application/1.0}company_home/" +
    		"{http://www.alfresco.org/model/content/1.0}User Documents/" +
    		"{http://www.alfresco.org/model/content/1.0}eric");

    qPath = new CmsPath("/app:company_home/cm:User_x0020_Documents/cm:eric/cm:Personal_x0020_Library/cm:pathFormats.txt");

    // make sure that these paths don't throw exceptions
    new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt");
    new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt,.+=_-)(&^%$#@!`~");

    CmsPath teamPath = new CmsPath("/sys:system/sys:teams/cm:The A-Team/cm:The B-Team");
    assertEquals("/system/teams/The A-Team/The B-Team", teamPath.toDisplayString());
    assertEquals("/{http://www.alfresco.org/model/system/1.0}system/{http://www.alfresco.org/model/system/1.0}teams/{http://www.alfresco.org/model/content/1.0}The A-Team/{http://www.alfresco.org/model/content/1.0}The B-Team", teamPath.toFullyQualifiedString());

    // these should fail
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt*");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt|");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt:");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt?");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt<");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt>");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
    try {
      new CmsPath("/{http://www.alfresco.org/model/content/1.0}pathFormats.txt\"");
      fail("Should have failed");
    } catch (IllegalArgumentException expected) {}
  }

  public void testAppendQualifiedPaths() {
    CmsPath qPath = new CmsPath("/app:company_home/cm:foo/cm:bar");
    String fqString = "/{http://www.alfresco.org/model/application/1.0}company_home/" +
      "{http://www.alfresco.org/model/content/1.0}foo/" +
      "{http://www.alfresco.org/model/content/1.0}bar";

    assertEquals(fqString + "/{http://www.alfresco.org/model/content/1.0}a/" +
    		"{http://www.alfresco.org/model/content/1.0}b/" +
    		"{http://www.alfresco.org/model/content/1.0}c", qPath.append("a/b/c").toString());

    assertEquals(fqString + "/{http://www.alfresco.org/model/content/1.0}a/" +
        "{http://www.alfresco.org/model/content/1.0}b", qPath.append("/a/b").toString());
    assertEquals(fqString + "/{http://www.alfresco.org/model/content/1.0}a/" +
        "{http://www.alfresco.org/model/content/1.0}b", qPath.append("/cm:a/cm:b").toString());
    assertEquals(fqString + "/{http://www.alfresco.org/model/content/1.0}a/" +
        "{http://www.alfresco.org/model/content/1.0}b", qPath.append("/{http://www.alfresco.org/model/content/1.0}a/{http://www.alfresco.org/model/content/1.0}b").toString());
//    qPath.append();
  }

  public void testToPrefixString() {
    CmsPath path = new CmsPath();
    assertEquals("/", path.toPrefixString(true));
  }

  public void setUp() {
  }
}
