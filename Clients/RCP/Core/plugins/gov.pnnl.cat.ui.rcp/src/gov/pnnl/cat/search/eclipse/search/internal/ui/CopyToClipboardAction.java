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
package gov.pnnl.cat.search.eclipse.search.internal.ui;

import gov.pnnl.cat.search.eclipse.search.CatSearchPlugin;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;

/**
 */
public class CopyToClipboardAction extends Action {

	private StructuredViewer fViewer;
	
	/**
	 * Constructor for CopyToClipboardAction.
	 * @param viewer StructuredViewer
	 */
	public CopyToClipboardAction(StructuredViewer viewer) {
		this();
		Assert.isNotNull(viewer);
		fViewer= viewer;
	}
	
	public CopyToClipboardAction() {
		setText(SearchMessages.CopyToClipboardAction_label); 
		setToolTipText(SearchMessages.CopyToClipboardAction_tooltip); 
	}
	
	/**
	 * @param viewer The viewer to set.
	 */
	public void setViewer(StructuredViewer viewer) {
		fViewer= viewer;
	}

	/*
	 * Implements method from IAction
	 */	
	/**
	 * Method run.
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		Shell shell= CatSearchPlugin.getActiveWorkbenchShell();
		if (shell == null || fViewer == null)
			return;

		ILabelProvider labelProvider= (ILabelProvider)fViewer.getLabelProvider();
		String lineDelim= System.getProperty("line.separator"); //$NON-NLS-1$
		StringBuffer buf= new StringBuffer();
		Iterator iter= getSelection();
		while (iter.hasNext()) {
			if (buf.length() > 0) {
				buf.append(lineDelim);
			}
			buf.append(labelProvider.getText(iter.next()));
		}
		
		if (buf.length() > 0) {
			Clipboard clipboard= new Clipboard(shell.getDisplay());
			try {
				copyToClipbard(clipboard, buf.toString(), shell);
			} finally {
				clipboard.dispose();
			}
		}
	}

	/**
	 * Method getSelection.
	 * @return Iterator
	 */
	private Iterator getSelection() {
		ISelection s= fViewer.getSelection();
		if (s instanceof IStructuredSelection)
			return ((IStructuredSelection)s).iterator();
		return Collections.EMPTY_LIST.iterator();
	}

	/**
	 * Method copyToClipbard.
	 * @param clipboard Clipboard
	 * @param str String
	 * @param shell Shell
	 */
	private void copyToClipbard(Clipboard clipboard, String str, Shell shell) {
		try {
			clipboard.setContents(new String[] { str },	new Transfer[] { TextTransfer.getInstance() });			
		} catch (SWTError ex) {
			if (ex.code != DND.ERROR_CANNOT_SET_CLIPBOARD)
				throw ex;
			String title= SearchMessages.CopyToClipboardAction_error_title;  
			String message= SearchMessages.CopyToClipboardAction_error_message; 
			if (MessageDialog.openQuestion(shell, title, message))
				copyToClipbard(clipboard, str, shell);
		}	
	}
}
