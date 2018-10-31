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

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Dialog that shows a list of items with icon and label.
 * @version $Revision: 1.0 $
 */
public class ListDialog extends SelectionDialog {
	
	private static final int WIDTH_IN_CHARACTERS= 55;
	
	private IStructuredContentProvider fContentProvider;
	private ILabelProvider fLabelProvider;
	private Object fInput;
	private TableViewer fViewer;
	private boolean fCreateCancelButton= true;
	
	/**
	 * Constructor for ListDialog.
	 * @param parent Shell
	 * @param input Object
	 * @param title String
	 * @param message String
	 * @param sp IStructuredContentProvider
	 * @param lp ILabelProvider
	 */
	public ListDialog(Shell parent, Object input, String title, String message, IStructuredContentProvider sp, ILabelProvider lp) {
		super(parent);
		setTitle(title);
		setMessage(message);
		fInput= input;
		fContentProvider= sp;
		fLabelProvider= lp;
	}
	
	/**
	 * Method setCreateCancelButton.
	 * @param value boolean
	 */
	public void setCreateCancelButton(boolean value) {
		fCreateCancelButton= value;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	/**
	 * Method createMessageArea.
	 * @param composite Composite
	 * @return Label
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite,SWT.WRAP);
		label.setText(getMessage()); 
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		label.setLayoutData(gd);
		applyDialogFont(label);
		return label;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	/**
	 * Method createDialogArea.
	 * @param container Composite
	 * @return Control
	 */
	protected Control createDialogArea(Composite container) {
		Composite parent= (Composite) super.createDialogArea(container);
		createMessageArea(parent);
		fViewer= new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		fViewer.setContentProvider(fContentProvider);

		final Table table= fViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (fCreateCancelButton)
					okPressed();
			}
		});
		fViewer.setLabelProvider(fLabelProvider);
		fViewer.setInput(fInput);
		List initialSelection= getInitialElementSelections();
		if (initialSelection != null)
			fViewer.setSelection(new StructuredSelection(initialSelection));
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(15);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		table.setLayoutData(gd);
		applyDialogFont(table);
		return table;
	}
	
	/*
	 * Overrides method from Dialog
	 */
	/**
	 * Method createButtonsForButtonBar.
	 * @param parent Composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		if (! fCreateCancelButton)
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		else
			super.createButtonsForButtonBar(parent);	
	}	
	
	/*
	 * Overrides method from Dialog
	 */
	protected void okPressed() {
		// Build a list of selected children.
		ISelection selection= fViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			setResult(((IStructuredSelection)fViewer.getSelection()).toList());
		super.okPressed();
	}
}


