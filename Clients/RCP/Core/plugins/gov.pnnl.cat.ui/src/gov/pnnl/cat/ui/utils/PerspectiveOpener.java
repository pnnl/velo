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
package gov.pnnl.cat.ui.utils;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.preferences.CatPreferenceIDs;
import gov.pnnl.cat.ui.preferences.PreferenceConstants;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 */
public class PerspectiveOpener {
  private IWorkbenchWindow window;
  //  private IAdaptable input;
  private String perspectiveID;
  private Logger logger = CatLogger.getLogger(this.getClass());
  private String customMessagePrompt;

  /**
   * Constructor for PerspectiveOpener.
   * @param perspId String
   * @param input IAdaptable
   * @param window IWorkbenchWindow
   */
  public PerspectiveOpener(String perspId, IAdaptable input, IWorkbenchWindow window) {
    this.perspectiveID = perspId;
    //    this.input = input;
    this.window = window;
  }

  public int openPerspectiveWithPrompt() {
    ScopedPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
    String key = PreferenceConstants.SWITCH_PERSPECTIVES;
    return openPerspectiveWithPrompt(store, key, window.getShell());
  }

  /**
   * Method openPerspectiveWithPrompt.
   * @param store IPreferenceStore
   * @param key String
   * @param shell Shell
   * @return int
   */
  private int openPerspectiveWithPrompt(IPreferenceStore store, String key, Shell shell) {
    String value = store.getString(key);
    int returnCode;

    logger.debug( "Value = " + value);
    if (value != null && value != "" && !value.equals(MessageDialogWithToggle.PROMPT)) {
      if (value.equals(MessageDialogWithToggle.ALWAYS)) {
        returnCode = IDialogConstants.YES_ID;
      } else {
        // assume value.equals(MessageDialogWithToggle.NEVER)
        if (!value.equals(MessageDialogWithToggle.NEVER)) {
          //EZLogger.logWarning("Unexpected property value '" + value + "'", null);
          logger.warn("Unexpected property value '" + value + "'");
        }
        returnCode = IDialogConstants.NO_ID;
      }
    } else {
      returnCode = MessageDialogWithToggle.openYesNoCancelQuestion(shell,
          "Confirm Perspective Switch",
          getMessagePrompt(),
          "&Remember my decision",
          false, store, key).getReturnCode();
    }

    if (returnCode == IDialogConstants.CANCEL_ID) {
      // cancel, no problem.
    } else {
      if (returnCode == IDialogConstants.YES_ID) {
        openPerspective();
      }
    }

    if (store != null && store.needsSaving() && store instanceof IPersistentPreferenceStore) {
      try {
        ((IPersistentPreferenceStore) store).save();
      } catch (IOException e) {
        logger.error("Unable to save preferences", e);
      }
    }

    return returnCode;
  }

  /**
   * Method getMessagePrompt.
   * @return String
   */
  protected String getMessagePrompt() {
    if (this.customMessagePrompt != null) {
      return this.customMessagePrompt;
    }

    return "This action requires the " + getPerspectiveName() + " perspective. " +
    "Do you want to open this perspective now?";
  }

  /**
   * Method setMessagePrompt.
   * @param customMessagePrompt String
   */
  public void setMessagePrompt(String customMessagePrompt) {
    this.customMessagePrompt = customMessagePrompt;
  }

  /**
   * Method getPerspectiveName.
   * @return String
   */
  public String getPerspectiveName() {
    String name = null;

    IPerspectiveDescriptor perspective = this.window.getWorkbench().getPerspectiveRegistry().findPerspectiveWithId(this.perspectiveID);

    if (perspective != null) {
      name = perspective.getLabel();
    }

    if(name == null) {
      name = perspectiveID;
    }
    return name;
  }

  /**
   * Method openPerspective.
   * @param perspId String
   * @param input IAdaptable
   * @param window IWorkbenchWindow
   */
  public static void openPerspective(String perspId, IAdaptable input, IWorkbenchWindow window) {
    new PerspectiveOpener(perspId, input, window).openPerspective();
  }

  /** 
   * Implements Open Perspective. 
   * This method was found online at the following URL:
   * http://www.eclipse.org/articles/using-perspectives/PerspectiveArticle.html
   */
  public void openPerspective() {
    IWorkbench workbench = this.window.getWorkbench();

    // Get "Open Behavior" preference.
    //    AbstractUIPlugin plugin = (AbstractUIPlugin) Platform.getPlugin(PlatformUI.PLUGIN_ID);
    //    IPreferenceStore store = plugin.getPreferenceStore();
    //    String pref = store.getString(IWorkbenchPreferenceConstants.OPEN_NEW_PERSPECTIVE);
    //    System.out.println(pref);
    // Implement open behavior.
    //    try {
    //      if (pref.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_WINDOW))
    //        workbench.openWorkbenchWindow(perspId, input);
    //      else if (pref.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_PAGE))
    //        window.openPage(perspId, input);
    //      else if (pref.equals(IWorkbenchPreferenceConstants.OPEN_PERSPECTIVE_REPLACE)) {
    IPerspectiveRegistry reg = workbench.getPerspectiveRegistry();
    window.getActivePage().setPerspective(reg.findPerspectiveWithId(this.perspectiveID));
    //      }
    //    } catch (WorkbenchException e) {
    //      logger.error(e);
    //    }
  }
}
