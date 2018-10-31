package gov.pnnl.velo.tools.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tif.service.TifServiceLocator;
import gov.pnnl.velo.tools.Tool;
import gov.pnnl.velo.util.VeloConstants;
import gov.pnnl.velo.util.VeloTifConstants;

public class ToolUtils {
  public static boolean hasPropertyValue(CmsPath resourcePath, String propName, String... expectedValues) {
    IResource resource = ResourcesPlugin.getResourceManager().getResource(resourcePath);
    return hasPropertyValue(resource, propName, expectedValues);
  }
  
  public static boolean hasPropertyValue(IResource resource, String propName, String... expectedValues) {
    if(resource == null) {
      return false;
    }
    
    String value = resource.getPropertyAsString(propName);
    
    boolean hasValue = false;
    if(value != null) {
      for(String expectedValue : expectedValues) {
        Pattern namePattern = Pattern.compile(expectedValue);
        if(namePattern.matcher(value).find()) {
          hasValue = true;
          break;
        }
      }
    }
    return hasValue;
  }
  
  public static boolean isMimetype(String mimetype, String... expectedMimetypes) {
    boolean hasMimetype = false;
    if(mimetype != null) {
      for(String expectedMimetype : expectedMimetypes) {
        //Don't escape here. Because calling classes could be sending pattern with valid
        //regex metacharacters. If expected mimetype has meta characters to escape,
        //do it from the calling class and send the escaped string to this method
        //expectedMimetype = java.util.regex.Pattern.quote(expectedMimetype);
        Pattern namePattern = Pattern.compile(expectedMimetype);
        if(namePattern.matcher(mimetype).find()) {
          hasMimetype = true;
          break;
        }
      }
    }
    return hasMimetype;

  }
   
  public static List<String> convertProperties(String[] props) {
    return Arrays.asList(props);
  }

  /**
   * For now perform a regex match on name pattern.  If pattern compilation has performance impact,
   * then we can create separate method based on equality match.
   * @param resource
   * @param expectedMimetypes - is a regular expression
   * @return
   */
  public static boolean hasMimetype(IResource resource, String... expectedMimetypes) {
    String mimetype = getMimetype(resource);
    return isMimetype(mimetype, expectedMimetypes);
  }
  
  /**
   * Gets mimetype from local cache
   * @param resource
   * @return
   */
  public static String getMimetype(IResource resource) {
    String mimetype = resource.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
    return mimetype; 
  }

  public static void centerEclipseWindow(Shell shell) {

    Monitor primary = shell.getDisplay().getPrimaryMonitor ();
    Rectangle bounds = primary.getBounds ();
    Rectangle rect = shell.getBounds ();
    int x = bounds.x + (bounds.width - rect.width) / 2;
    int y = bounds.y + (bounds.height - rect.height) / 2;
    shell.setLocation (x, y);
  }

  
  /**
   * Provide a local directory where temporary tool files can be written to before
   * they are saved to the server.
   * @param resource
   * @return
   * @throws IOException
   */
  public static  File getToolWorkingDirectory(Tool tool, IResource resource) throws IOException {
    // Cache working directories so we don't have to keep downloading
    // the same files every time we open the same tool
    String localDirectoryName = resource.getPropertyAsString(VeloConstants.PROP_UUID) + tool.getName();

    // create temp folder under the oascis folder
    File toolsfolder = getToolsFolder();
    File toolDir = new File(toolsfolder, localDirectoryName);

    if(!toolDir.exists()) {
      boolean mkdirSuccess = toolDir.mkdir();
      //Had issues here with mkdir not creating the temp directory for a simulation run
      //Use while loop to force creation of directory
      if(!mkdirSuccess){
        while(!mkdirSuccess){
          mkdirSuccess = toolDir.mkdir();
        }
      }
      System.out.println("mkdir? " + mkdirSuccess);
    }
    return toolDir;
  }
  
  public static File getToolsFolder() {
    File veloFolder =  TifServiceLocator.getVeloWorkspace().getVeloFolder();
    File toolsFolder = new File(veloFolder, "Tools");
    if(!toolsFolder.exists()) {
      toolsFolder.mkdir();
    }
    return toolsFolder;    
  }
  
  /**
   * For the given tool context, return true if this tool has already
   * been run 
   * @param context
   * @return
   */
  public static boolean hasToolRan(CmsPath context) {
    String[] jobStates = new String[]{
        VeloTifConstants.STATUS_KILLED,
        VeloTifConstants.STATUS_SUCCESS,
        VeloTifConstants.STATUS_ERROR,
        VeloTifConstants.STATUS_FAILED,
        VeloTifConstants.STATUS_DISCONNECTED    
    };
    return gov.pnnl.velo.tools.util.ToolUtils.hasPropertyValue(context, VeloTifConstants.JOB_STATUS, jobStates);
  }

  /**
   * Verify if this simulation tools is currently running
   * @return
   */
  public static boolean isToolRunning(CmsPath context) {
    String[] jobStates = new String[]{
        VeloTifConstants.STATUS_SUBMITTING,
        VeloTifConstants.STATUS_WAIT,
        VeloTifConstants.STATUS_START,
        VeloTifConstants.STATUS_POSTPROCESS,
        VeloTifConstants.STATUS_RECONNECT
    };
    return gov.pnnl.velo.tools.util.ToolUtils.hasPropertyValue(context, VeloTifConstants.JOB_STATUS, jobStates);
  }
  
  
  public static IResource getParentWithType(IResource tool, List<String> parentTypes) {
    IResource model = null;
    IResource currentResource = tool;     
    while (model == null) {
      String mimetype = currentResource.getPropertyAsString(VeloConstants.PROP_MIMETYPE);
      if(mimetype == null)
        break;
      // Handle old or new model setup mimetype.
      boolean found = false; 
      for (String type:parentTypes){
        if(mimetype.equals(type)) {
          model = currentResource;
          found = true;
          break;
        }
      }
      if (found){
        break;
      }
      currentResource = currentResource.getParent();
      if(currentResource == null) {
        break;
      }
    }
    return model;
  }
}
