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
package gov.pnnl.cat.search.eclipse.search.internal.ui.util;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * A field editor for a combo box that allows the drop-down selection of one of a list of items.
 * 
 * XXX: Note this is a copy from org.eclipse.debug.internal.ui.preferences
 * 		This class can be removed once bug 24928 is fixed.
 * 
 * @since 2.1
 * @version $Revision: 1.0 $
 */
public class ComboFieldEditor extends FieldEditor {

	/**
	 * The <code>Combo</code> widget.
	 */
	private Combo fCombo;
	
	/**
	 * The value (not the name) of the currently selected item in the Combo widget.
	 */
	private String fValue;
	
	/**
	 * The names (labels) and underlying values to populate the combo widget.  These should be
	 * arranged as: { {name1, value1}, {name2, value2}, ...}
	 */
	private String[][] fEntryNamesAndValues;

	/**
	 * Constructor for ComboFieldEditor.
	 * @param name String
	 * @param labelText String
	 * @param entryNamesAndValues String[][]
	 * @param parent Composite
	 */
	public ComboFieldEditor(String name, String labelText, String[][] entryNamesAndValues, Composite parent) {
		init(name, labelText);
		Assert.isTrue(checkArray(entryNamesAndValues));
		fEntryNamesAndValues= entryNamesAndValues;
		createControl(parent);		
	}

	/*
	 * Checks whether given <code>String[][]</code> is of "type" 
	 * <code>String[][2]</code>.
	 *
	 * @return <code>true</code> if it is ok, and <code>false</code> otherwise
	 */
	/**
	 * Method checkArray.
	 * @param table String[][]
	 * @return boolean
	 */
	private boolean checkArray(String[][] table) {
		if (table == null) {
			return false;
		}
		for (int i= 0; i < table.length; i++) {
			String[] array= table[i];
			if (array == null || array.length != 2) {
				return false;
			}
		}
		return true;
	}

	/*
	 * @see FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		Control control= getLabelControl();
		if (control != null) {
			((GridData)control.getLayoutData()).horizontalSpan= numColumns;
		}
		((GridData)fCombo.getLayoutData()).horizontalSpan= numColumns;
	}

	/*
	 * @see FieldEditor#doFillIntoGrid(Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control= getLabelControl(parent);
		GridData gd= new GridData();
		gd.horizontalSpan= numColumns;
		control.setLayoutData(gd);
		control= getComboBoxControl(parent);
		gd= new GridData();
		gd.horizontalSpan= numColumns;
		control.setLayoutData(gd);
	}

	/*
	 * @see FieldEditor#doLoad()
	 */
	protected void doLoad() {
		updateComboForValue(getPreferenceStore().getString(getPreferenceName()));
	}

	/*
	 * @see FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		updateComboForValue(getPreferenceStore().getDefaultString(getPreferenceName()));
	}

	/*
	 * @see FieldEditor#doStore()
	 */
	protected void doStore() {
		if (fValue == null) {
			getPreferenceStore().setToDefault(getPreferenceName());
			return;
		}
	
		getPreferenceStore().setValue(getPreferenceName(), fValue);
	}

	/*
	 * @see FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		return 2;
	}

	/*
	 * Lazily create and return the Combo control.
	 */
	/**
	 * Method getComboBoxControl.
	 * @param parent Composite
	 * @return Combo
	 */
	public Combo getComboBoxControl(Composite parent) {
		if (fCombo == null) {
			fCombo= new Combo(parent, SWT.READ_ONLY);
			for (int i= 0; i < fEntryNamesAndValues.length; i++) {
				fCombo.add(fEntryNamesAndValues[i][0], i);
			}
			fCombo.setFont(parent.getFont());
			fCombo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent evt) {
					String oldValue= fValue;
					String name= fCombo.getText();
					fValue= getValueForName(name);
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, fValue);					
				}
			});
		}
		return fCombo;
	}
	
	/*
	 * Given the name (label) of an entry, return the corresponding value.
	 */
	/**
	 * Method getValueForName.
	 * @param name String
	 * @return String
	 */
	protected String getValueForName(String name) {
		for (int i= 0; i < fEntryNamesAndValues.length; i++) {
			String[] entry= fEntryNamesAndValues[i];
			if (name.equals(entry[0])) {
				return entry[1];
			}
		}
		return fEntryNamesAndValues[0][0];
	}
	
	/*
	 * Set the name in the combo widget to match the specified value.
	 */
	/**
	 * Method updateComboForValue.
	 * @param value String
	 */
	protected void updateComboForValue(String value) {
		fValue= value;
		for (int i= 0; i < fEntryNamesAndValues.length; i++) {
			if (value.equals(fEntryNamesAndValues[i][1])) {
				fCombo.setText(fEntryNamesAndValues[i][0]);
				return;
			}
		}
		if (fEntryNamesAndValues.length > 0) {
			fValue= fEntryNamesAndValues[0][1];
			fCombo.setText(fEntryNamesAndValues[0][0]);
		}
	}
}
