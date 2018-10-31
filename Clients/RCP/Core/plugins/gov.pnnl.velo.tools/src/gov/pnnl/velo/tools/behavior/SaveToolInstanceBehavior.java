package gov.pnnl.velo.tools.behavior;

import java.io.File;
import java.util.Map;

import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.ui.rcp.handlers.CustomPasteBehavior;
import gov.pnnl.cat.ui.rcp.handlers.CustomSaveAsBehavior;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.velo.tools.ui.ToolUI;

/**
 * One interface for implementing save, save-as, copy, and move behaviors.
 * @author d3k339
 *
 */
public interface SaveToolInstanceBehavior extends CustomSaveAsBehavior, CustomPasteBehavior, ToolBehavior {
  // constants for prompting save options
  public static final int SAVE = 0; // ok to overwrite existing
  public static final int SAVE_AS = 1; // need to save as different tool
  public static final int CANCEL = 2;
  public static final int NO = 2;
  
  // variables for save options
  public static final String SAVE_DESTNATION = "save_destination";

  /**
   * Determines if user is allowed to save based on the current state of this tool.
   * For example if the tool is in a read-only state.
   * @return
   */
  public boolean isSaveAllowed(IFolder toolInstanceDir);

  /**
   * @param toolInstanceDir
   * @param toolUI - can be null if being called from outside an invoked tool context (i.e., from the file manager)
   * @param filesToSave
   * @param propertiesToSave
   * @param isMove
   * @throws Exception
   */
  public void saveAs(IFolder toolInstanceDir, ToolUI toolUI, Map<File, CmsPath> filesToSave, Map<String, String> propertiesToSave,
      boolean isMove) throws Exception;

  /**
   * Behavior should first prompt for the corrected save behavior, then perform the correct behavior based on
   * user's preference.
   * @param toolInstanceDir
   * @param toolUI
   * @param filesToSave
   * @param propertiesToSave
   * @return - the user's choice w.r.t. the behavior
   * @throws Exception
   */
  public int save(IFolder toolInstanceDir, ToolUI toolUI, Map<File, CmsPath> filesToSave, Map<String, String> propertiesToSave) throws Exception;

  /**
   * Perform the actual save without any UI prompting. 
   * @param toolInstanceDir
   * @param toolUI
   * @param filesToSave
   * @param propertiesToSave
   * @throws Exception
   */
  public void save(int choice, IFolder toolInstanceDir, ToolUI toolUI, Map<File, CmsPath> filesToSave, Map<String, String> propertiesToSave) throws Exception;
  
  /**
   * Depending on the state of your tool/model, prompt whether the user
   * wants to force a SAVE, SAVE_AS, or CANCEL
   * @param saveInfo - hold information contained in the prompt such as custom options
   * @return
   */
  public int promptForSave(IFolder toolInstanceDir, ToolUI toolUI, Map<String, Object> saveInfo);

  public int promptForSaveOnClose(ToolUI toolUI);

  /**
   * Depending on the state of your tool/model, prompt whether the user
   * wants to SAVE_AS, or CANCEL. This is where you can put any custom
   * logic or prompts to gather additional information regarding the save.
   * @param saveAsInfo - hold information contained in the prompt such as the destination folder
   * @return
   */
  public int promptForSaveAs(final IFolder toolInstanceDir, ToolUI toolUI, final Map<String, Object> saveAsInfo);

  /**
   * If this tool instance has any previous results that need to be
   * cleared out before saving.
   */
  public void clearResults(IFolder toolInstanceFolder);
}
