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
package gov.pnnl.cat.ui.rcp.actions.jobs;

import gov.pnnl.cat.core.resources.ICatCML;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.util.BlankRemover;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 */
public class ImportTaxonomyJob extends Job {

  String mFile;
  CmsPath mParentFolder;
  
  private static Logger logger = CatLogger.getLogger(ImportTaxonomyJob.class);
  
  /**
   * Constructor for ImportTaxonomyJob.
   * @param file String
   * @param parentFolder CmsPath
   */
  public ImportTaxonomyJob(String file, CmsPath parentFolder)
  {
    super("Importing Taxonomy");
    mFile = file;
    mParentFolder = parentFolder;
  }
  
  /**
   * Method run.
   * @param monitor IProgressMonitor
   * @return IStatus
   */
  @Override
  protected IStatus run(IProgressMonitor monitor) {
    
    if(monitor.isCanceled()) 
    {
      return Status.CANCEL_STATUS;
    }
    try {
      importTaxonomy(mFile, mParentFolder, monitor);
    } catch (Exception e) {
      String errMsg = "Taxonomy import failed.";
      ToolErrorHandler.handleError(errMsg, e, true);
      return Status.CANCEL_STATUS; //is there an error status?
    }
    // TODO Auto-generated method stub
    return Status.OK_STATUS;
  }

  /** This method is copied from ImportTaxonomyWizard with some 
   * modification: adding a ProgressMonitor as a parameter
   *  when monitor is canceled, stop the job and delete those already imported
   * 
   * This is a temporary method that imports a crude tab delimited format.  Basically
   * tabs indicate subdirectories.  No comments are allowed.  It's not robust and will
   * likely crash if the format is invalid.  
   * 
   * This version of the method uses multiple CML statements to do the import.
   * 
   * TODO: This should be replaced with a proper import file format.
   * @param filename
  
  
  
   * @param parentPath CmsPath
   * @param monitor IProgressMonitor
   * @throws Exception
   * @throws ResourceException  * @throws IOException  */

  public static void importTaxonomy(String filename, CmsPath parentPath, IProgressMonitor monitor) throws Exception {
    BufferedReader reader = null;
    CmsPath taxonomyPath = null;
    IResourceManager mgr = ResourcesPlugin.getResourceManager();
    
    String blankLineRegex = "[\\s]+";//used to check for totally blank lines   
    long start = System.currentTimeMillis();
    boolean cleanupTaxonomy = true;
    //boolean cleanupTaxonomy = false;
    
    try {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
      
      // First line is the taxonomy name
      String displayName = reader.readLine().trim();
            
      // create the taxonomy    
      taxonomyPath = parentPath.append(displayName);
      logger.debug("creating taxonomy " + taxonomyPath.toDisplayString());
      
      // If the resource already exists, we want to throw an exception, but 
      // not delete the existing taxonomy
      if((mgr.resourceExists(taxonomyPath))) {
        cleanupTaxonomy = false;
        throw new ResourceException("Taxonomy: \"" + taxonomyPath.toDisplayString() + "\" already exists!");
      }
      mgr.createTaxonomy(taxonomyPath);

      // load the taxonomy tree     
      Vector<String> segments = new Vector<String>();
      segments.add(taxonomyPath.toDisplayString());
      String[] tokens; 
      String folder;
      
      ICatCML cml = mgr.getCML();
      int nodeCount = 0;
      int lineNumber = 1;
      String line = reader.readLine();
      while(line != null) {
        if(monitor.isCanceled())
        {
          throw new Exception("Import Taxonomy Canceled");
        }
        lineNumber++;
        
        // remove trailing whitespace
        line = BlankRemover.rtrim(line);
        
        // ignore blank lines
        if(line.matches(blankLineRegex) || line.length() == 0 ) {
          line = reader.readLine();
          continue;
        }
        
        // Create the folder path from the line
        tokens = line.split("\\t");
        int level = tokens.length - 1;
        folder = line.trim();
        
        // Assemble the base path from the segments.  This is very important so the
        // folder gets created under the right parent.  If we don't do this, the folder
        // hierarchy will be wrong and flat (all at level 0).
        // Segment(0) is always the base path to the taxonomy
        
        // change by Dave Gillen 2011-Dec-07
        // something changed in the way segments are built into paths
        // so I changed this code so the folder path initializes itself
        // with the path to the base folder of this new taxonomy
        // and only segments that are part of this taxonomy are concatenated
        // with the folder name as the last segment
        // so segments[0] is never used, and is replaced instead with the constructor arg for the folderPath
        CmsPath folderPath = new CmsPath(taxonomyPath.toFullyQualifiedString());
        for (int i = 1; i <= level; i++) {
          if (i >= segments.size()) {
            throw new RuntimeException("Invalid file format on line number: " + lineNumber);
          }
          // This represents other parent folders in the path hierarchy
          folderPath = folderPath.append(segments.elementAt(i));
        }  
        // this is the name of the current folder that needs to be added
        folderPath = folderPath.append(folder);
        
        logger.debug("Trying to create folder: " + folderPath.toDisplayString());
        cml.addFolder(folderPath);
        int nextSegment = level + 1;
        if(nextSegment >= segments.size()) {
          segments.addElement(folder);
        } else {
          segments.setElementAt(folder, nextSegment);
        }
        line = reader.readLine();
        
        nodeCount++;
        if(nodeCount % 50 == 0) {
          logger.debug("executing batch upload");
          mgr.executeCml(cml);
          cml = mgr.getCML();
        }
      }
      
      // run anything left
      logger.debug("executing final upload");
      mgr.executeCml(cml);

    } catch (Exception e) {
      //System.out.println("import taxonomy exception");
      if (cleanupTaxonomy && taxonomyPath != null) {
        try {
          //System.out.println("delete taxonomy");
          mgr.deleteResource(taxonomyPath);
        } catch (ResourceException re) {
          // do nothing. at least we tried.
          logger.warn("Could not delete taxonomy after import failed.", e);
        }       
      }
      throw e;
    } finally {
      if (reader != null) {
          reader.close();
      }     
    }
    long end = System.currentTimeMillis();
    logger.debug("time to import taxonomy = " + (end - start));
  }

}
