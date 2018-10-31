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
package testcase.jface;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

/**
 */
public class DeferredContentProviderTest {
	private static final int NUM_ROWS = 20;

	/**
	 * Method main.
	 * @param args String[]
	 */
	public static void main(String[] args) {
		// Create concurrent model instance
		final SetModel model = new SetModel();
		
		// Create UI
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		Table table = new Table(shell, SWT.BORDER | SWT.VIRTUAL | SWT.FULL_SELECTION);
		//new TableColumn(table, 100, SWT.LEAD);
		
		// Create viewer using DeferredContentProvider
		TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new DeferredContentProvider(
				String.CASE_INSENSITIVE_ORDER));
		viewer.setInput(model);
		
		// Fill concurrnt model with content
		Thread modelThread = new Thread(new Runnable() {
			public void run() {
				List<String> items = new ArrayList<String>(NUM_ROWS);
				for (int i = 0; i < NUM_ROWS; i++) {
					items.add("Item " + (i + 1));
				}
				model.addAll(items);
			}
		});
		
		// Show UI (scrolling with scrollbar will show)
		modelThread.start();
		shell.setLayout(new FillLayout());
		shell.setSize(100, 20 * table.getItemHeight()); // first 20 items
		shell.open();
		
		while (!shell.isDisposed ()) {
			if (!display.readAndDispatch ()) display.sleep ();
		}
		display.dispose ();
	}
}
