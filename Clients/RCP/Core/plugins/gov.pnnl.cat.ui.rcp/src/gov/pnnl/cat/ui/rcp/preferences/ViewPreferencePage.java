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
package gov.pnnl.cat.ui.rcp.preferences;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.preferences.CatPreferenceIDs;
import gov.pnnl.cat.ui.preferences.PreferenceConstants;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * @version $Revision: 1.0 $
 */

public class ViewPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

  private ScopedPreferenceStore preferences;
  private BooleanFieldEditor showHiddenFilesField;
  private Logger logger = CatLogger.getLogger(this.getClass());
  
	public ViewPreferencePage() {
		super(GRID);
    preferences = new ScopedPreferenceStore(new InstanceScope(), CatPreferenceIDs.CAT_PREFERENCE_ID);
		//setDescription("View Preferences");
	}

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  protected Control createContents(Composite parent) {
    Control control = super.createContents(parent);
    return control;
  }
  
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
    showHiddenFilesField = new BooleanFieldEditor(PreferenceConstants.SHOW_HIDDEN_FILES,
        "&Show hidden files.",
        getFieldEditorParent());
    addField(	showHiddenFilesField	);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
   
	}
	
  
  /**
   * Method performOk.
   * @return boolean
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk(){
    try {
      storeValues();

    } catch (Exception e) {
      logger.error("Exception caught while trying to performOk." , e);
    }
    return super.performOk();
  }
  
  /**
   * Method doGetPreferenceStore.
   * @return IPreferenceStore
   */
  protected IPreferenceStore doGetPreferenceStore() {
    return preferences;
 }

  
  private void storeValues() {
    IPreferenceStore store = getPreferenceStore();
    store.setValue(PreferenceConstants.SHOW_HIDDEN_FILES, showHiddenFilesField.getBooleanValue());
  }
  
}
