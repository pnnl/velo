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
package gov.pnnl.cat.alerts.views;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

// copied from:
// http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet040TableViewerSorting.java?view=markup
/**
 */
public abstract class ColumnViewerSorter extends ViewerComparator {
	public static final int ASC = 1;
	
	public static final int NONE = 0;
	
	public static final int DESC = -1;
	
	private int direction = 0;
	
	private TableViewerColumn column;
	
	private ColumnViewer viewer;
	
	/**
	 * Constructor for ColumnViewerSorter.
	 * @param viewer ColumnViewer
	 * @param column TableViewerColumn
	 */
	public ColumnViewerSorter(ColumnViewer viewer, TableViewerColumn column) {
		this.column = column;
		this.viewer = viewer;
		this.column.getColumn().addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				if( ColumnViewerSorter.this.viewer.getComparator() != null ) {
					if( ColumnViewerSorter.this.viewer.getComparator() == ColumnViewerSorter.this ) {
						int tdirection = ColumnViewerSorter.this.direction;
						
						if( tdirection == ASC ) {
							setSorter(ColumnViewerSorter.this, DESC);
						} else if( tdirection == DESC ) {
//							setSorter(ColumnViewerSorter.this, NONE);
							setSorter(ColumnViewerSorter.this, ASC);
						}
					} else {
						setSorter(ColumnViewerSorter.this, ASC);
					}
				} else {
					setSorter(ColumnViewerSorter.this, ASC);
				}
			}
		});
	}
	
	/**
	 * Method setSorter.
	 * @param sorter ColumnViewerSorter
	 * @param direction int
	 */
	public void setSorter(ColumnViewerSorter sorter, int direction) {
		if( direction == NONE ) {
			column.getColumn().getParent().setSortColumn(null);
			column.getColumn().getParent().setSortDirection(SWT.NONE);
			viewer.setComparator(null);
		} else {
			column.getColumn().getParent().setSortColumn(column.getColumn());
			sorter.direction = direction;
			
			if( direction == ASC ) {
				column.getColumn().getParent().setSortDirection(SWT.DOWN);
			} else {
				column.getColumn().getParent().setSortDirection(SWT.UP);
			}
			
			if( viewer.getComparator() == sorter ) {
				viewer.refresh();
			} else {
				viewer.setComparator(sorter);
			}
			
		}
	}

	/**
	 * Method compare.
	 * @param viewer Viewer
	 * @param e1 Object
	 * @param e2 Object
	 * @return int
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return direction * doCompare(viewer, e1, e2);
	}
	
	/**
	 * Method doCompare.
	 * @param viewer Viewer
	 * @param e1 Object
	 * @param e2 Object
	 * @return int
	 */
	protected abstract int doCompare(Viewer viewer, Object e1, Object e2);
}
