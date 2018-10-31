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
package gov.pnnl.cat.search.eclipse.search2.internal.ui;

import gov.pnnl.cat.search.eclipse.search.internal.ui.util.ListContentProvider;
import gov.pnnl.cat.search.eclipse.search.ui.ISearchResult;
import gov.pnnl.cat.ui.rcp.util.SWTUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
public class SearchesDialog extends SelectionDialog {
	
	private static final int REMOVE_ID= IDialogConstants.CLIENT_ID+1;
	private static final int WIDTH_IN_CHARACTERS= 55;
	
	private List fInput;
	private TableViewer fViewer;
	private Button fRemoveButton;
	
	/**
	 */
	private static final class SearchesLabelProvider extends LabelProvider {
		
		private ArrayList fImages= new ArrayList();
		
		/**
		 * Method getText.
		 * @param element Object
		 * @return String
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(Object)
		 */
		public String getText(Object element) {
			return ((ISearchResult)element).getLabel();
		}
		
		/**
		 * Method getImage.
		 * @param element Object
		 * @return Image
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(Object)
		 */
		public Image getImage(Object element) {

			ImageDescriptor imageDescriptor= ((ISearchResult)element).getImageDescriptor(); 
			if (imageDescriptor == null)
				return null;
			
			Image image= imageDescriptor.createImage();
			fImages.add(image);

			return image;
		}
		
		/**
		 * Method dispose.
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
			Iterator iter= fImages.iterator();
			while (iter.hasNext())
				((Image)iter.next()).dispose();
			
			fImages= null;
		}
	}

	/**
	 * Constructor for SearchesDialog.
	 * @param parent Shell
	 * @param input List
	 */
	public SearchesDialog(Shell parent, List input) {
		super(parent);
		setTitle(SearchMessages.SearchesDialog_title);  
		setMessage(SearchMessages.SearchesDialog_message); 
		fInput= input;
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
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		
		List initialSelection= getInitialElementSelections();
		if (initialSelection != null)
			fViewer.setSelection(new StructuredSelection(initialSelection));

		validateDialogState();
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
		Composite ancestor= (Composite) super.createDialogArea(container);
		
		createMessageArea(ancestor);
		
		Composite parent= new Composite(ancestor, SWT.NONE);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		parent.setLayout(layout);
		

		fViewer= new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		fViewer.setContentProvider(new ListContentProvider());
		
		final Table table= fViewer.getTable();
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				okPressed();
			}
		});
		fViewer.setLabelProvider(new SearchesLabelProvider());
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= convertHeightInCharsToPixels(15);
		gd.widthHint= convertWidthInCharsToPixels(WIDTH_IN_CHARACTERS);
		table.setLayoutData(gd);
		
		
        fRemoveButton= new Button(parent, SWT.PUSH);
        fRemoveButton.setText(SearchMessages.SearchesDialog_remove_label); 
        fRemoveButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                buttonPressed(REMOVE_ID);
            }
        });
		fRemoveButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		SWTUtil.setButtonDimensionHint(fRemoveButton);
		
		fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				validateDialogState();
			}
		});
		
		applyDialogFont(ancestor);

		// set input & selections last, so all the widgets are created.
		fViewer.setInput(fInput);
		return table;
	}
	
	protected final void validateDialogState() {
		IStructuredSelection sel= (IStructuredSelection) fViewer.getSelection();
		int elementsSelected= sel.toList().size();
		
		fRemoveButton.setEnabled(elementsSelected > 0);
		Button okButton= getOkButton();
		if (okButton != null) {
			okButton.setEnabled(elementsSelected == 1);
		}
	}
	
	
	/**
	 * Method buttonPressed.
	 * @param buttonId int
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == REMOVE_ID) {
			IStructuredSelection selection= (IStructuredSelection) fViewer.getSelection();
			Iterator searchResults= selection.iterator();
			while (searchResults.hasNext()) {
				ISearchResult result= (ISearchResult)searchResults.next();
				InternalSearchUI.getInstance().removeQuery(result.getQuery());
				fInput.remove(result);
				fViewer.remove(result);
			}
			if (fViewer.getSelection().isEmpty() && !fInput.isEmpty()) {
				fViewer.setSelection(new StructuredSelection(fInput.get(0)));
			}
			return;
		}
		super.buttonPressed(buttonId);
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


