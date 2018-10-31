package gov.pnnl.velo.tif.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("fileset")
public class Fileset {
  
  @XStreamAsAttribute
  private String excludes;
  private transient List<String> excludesList;
  
  @XStreamAsAttribute
  private String includes;
  private transient List<String> includesList;
  
  @XStreamAsAttribute
  private String actions;
  private transient List<String> actionsList;
  
  @XStreamAsAttribute
  private String conditions;
  private transient Map<String, String> conditionsMap;
  
  @XStreamAsAttribute
  private String dir;
  private transient String dirPath;
  
//  @XStreamAsAttribute
//  private String file;

  public Fileset() {
  }

  public static Fileset createDir(String dir, String includes, String excludes, String conditions, String actions) {
    Fileset fileset = new Fileset();
    if(dir != null && !dir.isEmpty()) {
      fileset.setDir(dir);
    } else {
      fileset.setDir(".");
    }
    fileset.setIncludes(includes);
    fileset.setExcludes(excludes);
    fileset.setConditions(conditions);
    fileset.setActions(actions);
    return fileset;
  }
  
//  public static Fileset createFile(String file, String conditions, String actions) {
//    Fileset fileset = new Fileset();
//    fileset.setFile(file);
//    fileset.setConditions(conditions);
//    fileset.setActions(actions);
//    return fileset;
//  }
  
  public String getIncludes() {
    return includes;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
    parseIncludes();
  }
  
  public List<String> getIncludesList() {
    if(includesList == null) {
      parseIncludes();
    }
    return includesList;
  }
  
  private void parseIncludes() {
    includesList = new ArrayList<String>();
    if(includes != null && !includes.isEmpty()) {
      String[] parts = includes.split(",");
      for(String part : parts) {
        includesList.add(part);
      }
    }
  }

  public String getActions() {
	if(actions==null)
		actions = "copy";
    return actions;
  }

  public void setActions(String actions) {
    this.actions = actions;
    parseActions();
  }
  
  public List<String> getActionsList() {
    if(actionsList == null) {
      parseActions();
    }
    return actionsList;
  }
  
  private void parseActions() {
    actionsList = new ArrayList<String>();
    if(actions != null && !actions.isEmpty()) {
      String[] parts = actions.split(",");
      for(String part : parts) {
        actionsList.add(part);
      }
    }
  }

  public String getConditions() {
    return conditions;
  }

  public void setConditions(String conditions) {
    this.conditions = conditions;
    parseConditions();
  }
  
  private void parseConditions() {
    conditionsMap = new HashMap<String, String>();
    if(conditions != null && !conditions.isEmpty()) {
      //conditions="exitState=success"
      String[] expressions = conditions.split(",");
      for(String expression : expressions) {
        String[] parts = expression.split("=");
        String key = parts[0];
        String value = parts[1];
        conditionsMap.put(key, value);
      }
    }
    
  }
  
  public Map<String, String> getConditionsMap() {
    if(conditionsMap == null) {
      parseConditions();
    }
    return conditionsMap;
  }

  public String getDir() {
	if(dir==null)
		dir = ".";
    return dir;
  }

  public void setDir(String dir) {
    this.dir = dir;
  }
  
  public String getDirPath(String contextPath){
      //TODO - need to debug this. For some reason I saw dirPath as the last run's contextPath instead of
      //current run's. even though dirPath is a member variable and not a static variable. So for now
      //checking if it starts with contextPath, recomputing it
	  if(dirPath == null || ! dirPath.startsWith(contextPath)) {
		  String dirTrim = getDir().trim(); //this should take care of null/default
		  if (dirTrim.equals("..") || dirTrim.startsWith("../"))
			  dirPath = dir.replaceFirst(
						java.util.regex.Matcher.quoteReplacement(".."),
						new File(contextPath).getParent());
			else if (dirTrim.equals(".") || dirTrim.startsWith("./"))
				dirPath = dir.replaceFirst(
						java.util.regex.Matcher.quoteReplacement("."),
						contextPath);
	  }
	  return dirPath;
  }

//  public String getFile() {
//    return file;
//  }
//
//  public void setFile(String file) {
//    this.file = file;
//  }

  public String getExcludes() {
    return excludes;
  }

  public void setExcludes(String excludes) {
    this.excludes = excludes;
    parseExcludes();
  }
  
  public List<String> getExcludesList() {
    if(excludesList == null) {
      parseExcludes();
    }
    return excludesList;
  }
  
  private void parseExcludes() {
    excludesList = new ArrayList<String>();
    if(excludes != null && !excludes.isEmpty()) {
      String[] parts = excludes.split(",");
      for(String part : parts) {
        excludesList.add(part);
      }
    }
  }


  private String getSafeIncludes() {
    if(includes == null) {
      return "";
    } else {
      return includes;
    }
  }
  
  private String getSafeExcludes() {
    if(excludes == null) {
      return "";
    } else {
      return excludes;
    }    
  }
  
  private String getSafeActions() {
    if(actions == null) {
      return "";
    } else {
      return actions;
    } 
  }
  
  private String getSafeConditions() {
    if(conditions == null) {
      return "";
    } else {
      return conditions;
    }     
  }
  
  public boolean isCopy() {
    if(getActionsList().contains(JobLaunching.ACTION_COPY)) {
      return true;
    }
    return false;
  }
  
  public boolean isLink() {
    if(getActionsList().contains(JobLaunching.ACTION_LINK)) {
      return true;
    } 
    return false;
  }
  
  public boolean isValidExitState(boolean success) {
    boolean valid = true;
    
    String exitState = getConditionsMap().get(JobLaunching.EXIT_STATE_KEY);
    if(exitState != null && !exitState.isEmpty()) {
      valid = false;
      if(success && !exitState.equalsIgnoreCase(JobLaunching.EXIT_STATE_ERROR) ){
        valid = true;
      } else if (!success && !exitState.equalsIgnoreCase(JobLaunching.EXIT_STATE_SUCCESS)) {
        valid = true;
      }
    }
    return valid;
  }
  
  public boolean isValidExitState(String state) {
    boolean valid = true;
    
    String valueToMatch = getConditionsMap().get(JobLaunching.EXIT_STATE_KEY);
    if(valueToMatch != null && !valueToMatch.isEmpty()) {
      if(valueToMatch.equalsIgnoreCase(JobLaunching.EXIT_STATE_ANY) || valueToMatch.equalsIgnoreCase(state) ){
        valid = true;
      } else {
        valid = false;
      }
    }
    return valid;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return obj != null && obj instanceof Fileset && obj.hashCode() == hashCode();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return dir + "; includes=" + getSafeIncludes() + "; excludes=" + getSafeExcludes() 
        + "; actions=" + getSafeActions() + "; conditions=" + getSafeConditions() ;
  }

  
  /**
   * If dir points to a relative path, it will be relative to 
   * the working dir
   * @param localWorkingDir
   * @return
   */
  public List<File> findMatchingFiles(File workingDir){
	//TODO: if only I can find the right negate patter for regex
		// and get it to work with the WildcardFileFilter I could
		// negate the patern in excludes list and just
		//send it as a combined list of wildcards to WildCardFileFilter
//		File dir = new File(fs.getDir());
//		List<String> wildcardList = fs.getIncludesList();
//		for(String pattern : fs.getExcludesList()) {
//			wildcardList.add("^(?! " + pattern + ").+");
//		}
//		FileFilter fileFilter = new WildcardFileFilter(wildcardList);
//		files = (ArrayList<File>) Arrays.asList(dir.listFiles(fileFilter));
	  
	  //TODO: May be we could use the filter class Zoe created for premier network
	  //that accepts proper regular expression instead of simplistic * or  ?
	  
    // 1) compute which folder we are filtering
    File folderToFilter;
    String dirPath = dir;
    if(dirPath == null || dirPath.isEmpty()) {
      dirPath = "."; // assume working dir if left blank
    }
   
    File folder = new File(dirPath);
    if(folder.isAbsolute()) {
      folderToFilter = folder; // this is an absolute path so we use it directly

    } else {
      folderToFilter = new File(workingDir, dirPath);  // this path is relative to our working dir
    }      

    
    // 2) apply includes and excludes to that folder
    List<File> matchingFiles = new ArrayList<File>();
    File[] children = folderToFilter.listFiles();
    if(children == null) {
      throw new RuntimeException("Unable to list folder: " + folderToFilter.getAbsolutePath() + ".  Maybe it is not a directory or has permission errors.");
    }
    for(File file : children) {
      String name = file.getName();
      boolean include = false;
      boolean exclude = false;
      for (String pattern : getExcludesList()) {
        if (FilenameUtils.wildcardMatch(name, pattern, IOCase.SENSITIVE)) {
          exclude = true;
          break;
        }
      }
      if (!exclude){
        for (String pattern : getIncludesList()) {
          if (FilenameUtils.wildcardMatch(name, pattern, IOCase.SENSITIVE)) {
            include = true;
            break;
          }
        }
      }
      if(include) {
        matchingFiles.add(file);
      }     
    }
    
    return matchingFiles;
  }

//    private static void validateInputs(File folder, String filter) {
//      FileFilter fileFilter = new WildcardFileFilter(filter);
//      File[] listFiles = folder.listFiles(fileFilter);
//      if(listFiles == null || listFiles.length ==0){
//        throw new RuntimeException("Missing mandatory input " + filter);
//      }
//    }

  
  public static void main(String... args){
    Fileset fileset = Fileset.createDir(".", "amanzi.xml,agni.xml,probin", null,"exitState=success", "copy,archive");
    
    for(String include : fileset.getIncludesList())  {
      System.out.println("include: " + include);
    }
    
    for(String exclude : fileset.getExcludesList()) {
      System.out.println("exclude: " + exclude);
    }
    
    for(String key : fileset.getConditionsMap().keySet()) {
      System.out.println("condition: " + key + " = " + fileset.getConditionsMap().get(key));
    }
    
    for(String action : fileset.getActionsList()) {
      System.out.println("action: " + action);
    }
  }
}
