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
package gov.pnnl.cat.search.advanced.test;

import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.tests.CatTest;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.advanced.query.AdvancedSearchQuery;
import gov.pnnl.velo.model.CmsPath;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *  Each test case createFolderAndFiles() for its own
 *  Currently we have these test cases:
 *    1. testing All words - GOOD
 *    2. testing exact phrase - GOOD
 *    3. at least one of the words - GOOD 
 *    4. without the words - GOOD
 *    5. "all of the words" + "exact phrase"
 *    6. "all of the words" + "at least one of the words"
 *    7. "all of the words" + "without the words"
 *    8. All look for options: all, exact, at least one, without
 *    
 *    9. Find matches in - how to test?
 *    
 *    10. How about Look in section?
 *    
 *    11. Metadata option?

 * @author kevinlai
 *
 * @version $Revision: 1.0 $
 */
public class AdvancedSearchTest extends CatTest {
  private static final CmsPath SEARCHJUNIT_BASEPATH = new CmsPath("/SearchTest");
  private CmsPath SEARCHJUNIT_PATH;
  private Logger logger = CatLogger.getLogger(AdvancedSearchTest.class);
  ArrayList<IFile> files;
  IFile file;

  AdvancedSearchQuery searchQuery;

  String allwords = "onee twoo threee";
  String oneofwords = "onee threee twoo";
  String withoutwords = "fourr fivee";

  String fileHit1;
  String fileHit2;
  String fileHit3;
  String fileNonHit1;
  String fileNonHit2;

  public void testAllWords()
  {
    searchQuery = new AdvancedSearchQuery();
    searchQuery.setAllwords(allwords);

    files = new ArrayList<IFile>();

    String searchResultStr = "";
    
    try {
      IFolder folder = addFolder();
      createAllwordsFiles(folder);
      List<IResource> results = searchQuery.executeSearch().getHandles();
      System.out.println("results size:" + results.size());
      assertFalse(results.size() == 0);
      for(IResource hit : results)
      {
        System.out.println(hit.getPath().toDisplayString());
        searchResultStr += hit.getPath().toDisplayString() + "|";
      }
      assertTrue(searchResultStr.indexOf(fileHit1) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit2) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit3) >= 0);
      assertTrue(searchResultStr.indexOf(fileNonHit1) < 0);
      assertTrue(searchResultStr.indexOf(fileNonHit2) < 0);
      
      deleteFilesAndFolder();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    
  }

  public void testExactPhrase()
  {
    String exactphrase = "\"exactt phrasee\"";
    searchQuery = new AdvancedSearchQuery();
    searchQuery.setExactphrase(exactphrase);

    files = new ArrayList<IFile>();

    String searchResultStr = "";
    
    try {
      addFolder();
      createFiles4Exactphase();
      List<IResource> results = searchQuery.executeSearch().getHandles();
      System.out.println("results size:" + results.size());
      assertFalse(results.size() == 0);
      for(IResource hit : results)
      {
        System.out.println(hit.getPath().toDisplayString());
        searchResultStr += hit.getPath().toDisplayString() + "|";
      }
      assertTrue(searchResultStr.indexOf(fileHit1) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit2) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit3) >= 0);
      assertTrue(searchResultStr.indexOf(fileNonHit1) < 0);
      assertTrue(searchResultStr.indexOf(fileNonHit2) < 0);
      
      //deleteFilesAndFolder();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    
  }

  public void testAtleastOneword()
  {
    String words = "onee twoo threee";
    searchQuery = new AdvancedSearchQuery();
    searchQuery.setOneofwords(words);

    files = new ArrayList<IFile>();

    String searchResultStr = "";
    
    try {
      addFolder();
      createFiles4AtleastOneword();
      List<IResource> results = searchQuery.executeSearch().getHandles();
      System.out.println("results size:" + results.size());
      assertFalse(results.size() == 0);
      for(IResource hit : results)
      {
        System.out.println(hit.getPath().toDisplayString());
        searchResultStr += hit.getPath().toDisplayString() + "|";
      }
      assertTrue(searchResultStr.indexOf(fileHit1) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit2) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit3) >= 0);
      assertTrue(searchResultStr.indexOf(fileNonHit1) < 0);
      assertTrue(searchResultStr.indexOf(fileNonHit2) < 0);
      
      //deleteFilesAndFolder();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    
  }

  /**
   * cannot test withoutwords alone - null result
   * Has to be with something else
   * Take "all of the words" here
   *
   */
  public void testWithoutWords()
  {
    String withoutWords = "onee twoo threee";
    searchQuery = new AdvancedSearchQuery();
    
    searchQuery.setAllwords("exactt");
    searchQuery.setWithoutwords(withoutWords);

    files = new ArrayList<IFile>();

    String searchResultStr = "";
    
    try {
      addFolder();
      createFiles4WithoutWords();
      List<IResource> results = searchQuery.executeSearch().getHandles();
      System.out.println("results size:" + results.size());
      assertFalse(results.size() == 0);
      for(IResource hit : results)
      {
        System.out.println(hit.getPath().toDisplayString());
        searchResultStr += hit.getPath().toDisplayString() + "|";
      }
      System.out.println(searchResultStr);
      System.out.println(fileHit1);
      //exactly opposite of atleastoneword
      assertTrue(searchResultStr.indexOf(fileHit1) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit2) >= 0);
      assertTrue(searchResultStr.indexOf(fileHit3) >= 0);
      assertTrue(searchResultStr.indexOf(fileNonHit1) < 0);
      assertTrue(searchResultStr.indexOf(fileNonHit2) < 0);
      
      //deleteFilesAndFolder();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }
    
  }

  /**
   * Method addFolder.
   * @return IFolder
   * @throws ResourceException
   * @throws ParseException
   */
  private IFolder addFolder() throws ResourceException, ParseException {
    
    long time = System.currentTimeMillis();
    String timeStr = new Long(time).toString();
    SEARCHJUNIT_PATH = SEARCHJUNIT_BASEPATH.append(timeStr);
    CmsPath newFolderPath = SEARCHJUNIT_PATH;

    IFolder newFolder = this.mgr.createFolder(newFolderPath, true);
    assertNotNull(newFolder);
    return newFolder;
  }

  private void deleteFilesAndFolder()
  {
    try {
      for(IFile file : files)
      {
        this.mgr.deleteResource(file.getPath());
      }
      this.mgr.deleteResource(SEARCHJUNIT_PATH);
      this.mgr.deleteResource(SEARCHJUNIT_BASEPATH);
    } catch (ResourceException e) {
      // TODO Auto-generated catch block
      logger.error(e);
    }    
  }

  /**
   * Method createTempFile.
   * @param content String
   * @param prefix String
   * @param suffix String
   * @return File
   */
  private File createTempFile(String content, String prefix, String suffix) {
    try {
      File temp = File.createTempFile(prefix, suffix);
      temp.deleteOnExit();
      FileUtils.writeStringToFile(temp, content);
      return temp;
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException("failed to create temp file", e);
    }
  }    

  /**
   * Vocabularis to be searched:
   * allwords: "onee twoo threee"
   * Add files that should be search results:
   *  hit1.txt
   *  hit2.txt
   * And should not be in the search results:
   *  nonhit1.txt
   *  nonhit2.txt
   * @param parentFolder IFolder
   */
  private void createAllwordsFiles(IFolder parentFolder)
  {
    CmsPath path1 = SEARCHJUNIT_PATH.append("hit1.txt");
    fileHit1 = path1.toDisplayString();
    File temp = createTempFile("There are threee, maybe twoo, or exactly onee egg in the bush", "hit1", ".txt");   
    file = this.mgr.createFile(path1, temp);
    files.add(file);

    CmsPath path2 = SEARCHJUNIT_PATH.append("hit2.txt");
    fileHit2 = path2.toDisplayString();
    temp = createTempFile("I am counting, onee, twoo, or no, there are threee geese eggs!", "hit2", ".txt");   
    file = this.mgr.createFile(path2, temp);
    files.add(file);
    
    CmsPath path3 = SEARCHJUNIT_PATH.append("nonhit1.txt");
    fileNonHit1 = path3.toDisplayString();
    temp = createTempFile("I am counting, one, two, or no, there are threee geese eggs!", "nonhit1", ".txt");   
    file = this.mgr.createFile(path3, temp);
    files.add(file);


    CmsPath path4 = SEARCHJUNIT_PATH.append("nonhit2.txt");
    fileNonHit2 = path4.toDisplayString();
    temp = createTempFile("I am counting, onee, twoo, or no, there are three geese eggs!", "nonhit2", ".txt");   
    file = this.mgr.createFile(path4, temp);
    files.add(file);
  
  }

  /**
   * Search word: "exactt phrasee"
   * Add files that should be search results:
   *  hit1.txt
   *  hit2.txt
   *  hit3.txt
   * And should not be in the search results:
   *  nonhit1.txt
   *  nonhit2.txt
   */
  private void createFiles4Exactphase()
  {
    throw new RuntimeException("Commented this method out");
//    CmsPath path1 = SEARCHJUNIT_PATH.append("hit1.txt");
//    fileHit1 = path1.toDisplayString();
//    InputStream content1 = new ByteArrayInputStream(
//        "this file contains some exactt phrasee in the paragraph".getBytes());
//
//    CmsPath path2 = SEARCHJUNIT_PATH.append("hit2.txt");
//    fileHit2 = path2.toDisplayString();
//    InputStream content2 = new ByteArrayInputStream(
//        "this file also contains some exactt or Phrasee in the paragraph".getBytes()); //or does not matter
//
//    CmsPath path3 = SEARCHJUNIT_PATH.append("hit3.txt");
//    fileHit3 = path3.toDisplayString();
//    InputStream content3 = new ByteArrayInputStream(
//        "this file contains capital Exactt, Phrasee in the paragraph".getBytes()); //comma does not matter
//
//    CmsPath path4 = SEARCHJUNIT_PATH.append("nonhit1.txt");
//    fileNonHit1 = path4.toDisplayString();
//    InputStream content4 = new ByteArrayInputStream(
//        "this file contains exactt ocean phrasee appears in another place".getBytes());
//
//    CmsPath path5 = SEARCHJUNIT_PATH.append("nonhit2.txt");
//    fileNonHit2 = path5.toDisplayString();
//    InputStream content5 = new ByteArrayInputStream(
//        "this file contains exactt mountain phrasee separated by a comma".getBytes());
//
//    try {
//      File = this.mgr.addFile(path1, content1);
//      Files.add(File);
//      File = this.mgr.addFile(path2, content2);
//      Files.add(File);
//      File = this.mgr.addFile(path3, content3);
//      Files.add(File);
//      File = this.mgr.addFile(path4, content4);
//      Files.add(File);
//      File = this.mgr.addFile(path5, content5);
//      Files.add(File);
//    } catch (ResourceException e) {
//      // TODO Auto-generated catch block
//      logger.error(e);
//    }
  }


  /**
   * Search word: "onee twoo threee"
   * Add files that should be search results:
   *  hit1.txt
   *  hit2.txt
   *  hit3.txt
   * And should not be in the search results:
   *  nonhit1.txt
   *  nonhit2.txt
   */
  private void createFiles4AtleastOneword()
  {
    throw new RuntimeException("Commented this method out.");
//    CmsPath path1 = SEARCHJUNIT_PATH.append("hit1.txt");
//    fileHit1 = path1.toDisplayString();
//    InputStream content1 = new ByteArrayInputStream(
//        "This document contains onee match".getBytes());
//
//    CmsPath path2 = SEARCHJUNIT_PATH.append("hit2.txt");
//    fileHit2 = path2.toDisplayString();
//    InputStream content2 = new ByteArrayInputStream(
//        "There are twoo matches in the document".getBytes());
//
//    CmsPath path3 = SEARCHJUNIT_PATH.append("hit3.txt");
//    fileHit3 = path3.toDisplayString();
//    InputStream content3 = new ByteArrayInputStream(
//        "Amazingly threee things are found here".getBytes());
//
//    CmsPath path4 = SEARCHJUNIT_PATH.append("nonhit1.txt");
//    fileNonHit1 = path4.toDisplayString();
//    InputStream content4 = new ByteArrayInputStream(
//        "this file contains one, two, and three hits, but unfortunately none has the exact spelling".getBytes());
//
//    CmsPath path5 = SEARCHJUNIT_PATH.append("nonhit2.txt");
//    fileNonHit2 = path5.toDisplayString();
//    InputStream content5 = new ByteArrayInputStream(
//        "this file contains oneee, twooo, and threeee hits, but unfortunately none has the exact spelling".getBytes());
//
//    try {
//      File = this.mgr.addFile(path1, content1);
//      Files.add(File);
//      File = this.mgr.addFile(path2, content2);
//      Files.add(File);
//      File = this.mgr.addFile(path3, content3);
//      Files.add(File);
//      File = this.mgr.addFile(path4, content4);
//      Files.add(File);
//      File = this.mgr.addFile(path5, content5);
//      Files.add(File);
//    } catch (ResourceException e) {
//      // TODO Auto-generated catch block
//      logger.error(e);
//    }
  }

  /**
   * Search word: "onee twoo threee"
   * Add files that should be search results:
   *  hit1.txt
   *  hit2.txt
   *  hit3.txt
   * And should not be in the search results:
   *  nonhit1.txt
   *  nonhit2.txt
   */
  private void createFiles4WithoutWords()
  {
    throw new RuntimeException("Commented this method out");

//    CmsPath path1 = SEARCHJUNIT_PATH.append("hit1.txt");
//    fileHit1 = path1.toDisplayString();
//    InputStream content1 = new ByteArrayInputStream(
//        "This exactt document contains oneee match".getBytes());
//
//    CmsPath path2 = SEARCHJUNIT_PATH.append("hit2.txt");
//    fileHit2 = path2.toDisplayString();
//    InputStream content2 = new ByteArrayInputStream(
//        "This exactt document contains twooo matches".getBytes());
//
//    CmsPath path3 = SEARCHJUNIT_PATH.append("hit3.txt");
//    fileHit3 = path3.toDisplayString();
//    InputStream content3 = new ByteArrayInputStream(
//        "This exactt document contains threeee match".getBytes());
//
//    CmsPath path4 = SEARCHJUNIT_PATH.append("nonhit1.txt");
//    fileNonHit1 = path4.toDisplayString();
//    InputStream content4 = new ByteArrayInputStream(
//        "This exactt document contains threee match".getBytes());
//
//    CmsPath path5 = SEARCHJUNIT_PATH.append("nonhit2.txt");
//    fileNonHit2 = path5.toDisplayString();
//    InputStream content5 = new ByteArrayInputStream(
//        "This exactt document contains twoo match".getBytes());
//
//    try {
//      File = this.mgr.addFile(path1, content1);
//      Files.add(File);
//      File = this.mgr.addFile(path2, content2);
//      Files.add(File);
//      File = this.mgr.addFile(path3, content3);
//      Files.add(File);
//      File = this.mgr.addFile(path4, content4);
//      Files.add(File);
//      File = this.mgr.addFile(path5, content5);
//      Files.add(File);
//    } catch (ResourceException e) {
//      // TODO Auto-generated catch block
//      logger.error(e);
//    }
  }

}
