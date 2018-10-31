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

import gov.pnnl.cat.search.eclipse.search.internal.ui.SearchMessages;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.dialogs.TypeFilteringDialog;

/**
 */
public class FileTypeEditor extends SelectionAdapter implements DisposeListener, SelectionListener {
	
	private Combo fTextField;
	private Button fBrowseButton;

	final static String TYPE_DELIMITER= SearchMessages.FileTypeEditor_typeDelimiter; 

	/**
	 * Constructor for FileTypeEditor.
	 * @param registry IEditorRegistry
	 * @param textField Combo
	 * @param browseButton Button
	 */
	public FileTypeEditor(IEditorRegistry registry, Combo textField, Button browseButton) {
		fTextField= textField;
		fBrowseButton= browseButton;
		
		fTextField.addDisposeListener(this);
		fBrowseButton.addDisposeListener(this);
		fBrowseButton.addSelectionListener(this);
	}
	
	/**
	 * Method widgetDisposed.
	 * @param event DisposeEvent
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		Widget widget= event.widget;
		if (widget == fTextField) 
			fTextField= null;
		else if (widget	== fBrowseButton)
			fBrowseButton= null;
	}
	
	/**
	 * Method widgetSelected.
	 * @param event SelectionEvent
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == fBrowseButton)
			handleBrowseButton();
	}
		
	/**
	 * Method widgetDoubleSelected.
	 * @param event SelectionEvent
	 */
	public void widgetDoubleSelected(SelectionEvent event) {
	}
	
	/**
	 * Method getFileTypes.
	 * @return String[]
	 */
	public String[] getFileTypes() {
		Set result= new HashSet();
		StringTokenizer tokenizer= new StringTokenizer(fTextField.getText(), TYPE_DELIMITER);

		while (tokenizer.hasMoreTokens()) {
			String currentExtension= tokenizer.nextToken().trim();
			result.add(currentExtension);
		}
		return (String[]) result.toArray(new String[result.size()]);
	}

	/**
	 * Method setFileTypes.
	 * @param types String[]
	 */
	public void setFileTypes(String[] types) {
		fTextField.setText(typesToString(types));
	}

	protected void handleBrowseButton() {
		TypeFilteringDialog dialog= new TypeFilteringDialog(fTextField.getShell(), Arrays.asList(getFileTypes()));
		if (dialog.open() == Window.OK) {
			Object[] result= dialog.getResult();
			HashSet patterns= new HashSet();
			boolean starIncluded= false;
			for (int i= 0; i < result.length; i++) {
				String curr= result[i].toString();
				if (curr.equals("*")) { //$NON-NLS-1$
					starIncluded= true;
				} else {
					patterns.add("*." + curr); //$NON-NLS-1$
				}
			}
			if (patterns.isEmpty() && starIncluded) { // remove star when other file extensions active
				patterns.add("*"); //$NON-NLS-1$
			}
			String[] filePatterns= (String[]) patterns.toArray(new String[patterns.size()]);
			Arrays.sort(filePatterns);
			setFileTypes(filePatterns);
		}
	}

	/**
	 * Method typesToString.
	 * @param types String[]
	 * @return String
	 */
	public static String typesToString(String[] types) {
		StringBuffer result= new StringBuffer();
		for (int i= 0; i < types.length; i++) {
			if (i > 0) {
				result.append(TYPE_DELIMITER);
				result.append(" "); //$NON-NLS-1$
			}
			result.append(types[i]);
		}
		return result.toString();
	}
}
