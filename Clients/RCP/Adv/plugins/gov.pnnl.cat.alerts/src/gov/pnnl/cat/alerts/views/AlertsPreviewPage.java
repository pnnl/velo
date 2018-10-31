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

import gov.pnnl.cat.alerts.model.IAlert;
import gov.pnnl.cat.alerts.model.IEvent;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IFolder;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.actions.OpenFileInSystemEditorAction;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.dnd.DNDSupport;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class AlertsPreviewPage extends ViewPart implements ISelectionListener {
  public static final String ID = AlertsPreviewPage.class.getName();
//  private final static int COL_FILENAME = 0;
//  private final static int COL_CHANGE = 1;
//  private final static int COL_CHANGED_BY = 2;
//  private final static int COL_DATE = 3;

  private Composite control;
  private TableViewer tableViewer;
  private DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");
  private DateFormat detailedDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  private OpenFileInSystemEditorAction openFileAction;

  /**
   * Create the PageBookView Page
   */
  public AlertsPreviewPage() {
    super();
  }
  
  @Override
  public void createPartControl(Composite parent) {
    control = new Composite(parent, SWT.NULL);
    control.setLayout(new GridLayout());

    createViewer(control);
    openFileAction = new OpenFileInSystemEditorAction();
    openFileAction.setViewer(tableViewer);
    getSite().getPage().addSelectionListener(this);

    
  }

  /**
   * Method createViewer.
   * @param parent Composite
   */
  private void createViewer(Composite parent) {
    tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    Table table = tableViewer.getTable();
    table.setLinesVisible(true);
    table.setHeaderVisible(true);
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    createColumns(tableViewer);
    tableViewer.setContentProvider(new EventsContentProvider());
    tableViewer.addDoubleClickListener(new IDoubleClickListener() {
      @Override
      public void doubleClick(DoubleClickEvent e) {
        // assume that something is selected
        Object selection = ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();

        if (selection instanceof IEvent) {
          IEvent event = (IEvent) selection;
          CmsPath path = event.getResourcePath();

          if (!event.isValid()) {
            MessageDialog.openError(tableViewer.getControl().getShell(), "Resource Inaccessible", "This resource appears to have been moved or deleted, and is no longer accessible.");
          } else {
            IResource resource;
            try {
              resource = ResourcesPlugin.getResourceManager().getResource(path);

              if (resource instanceof IFile) {
                openFileAction.openFile((IFile) resource);
                
              } else if (resource instanceof IFolder) {
                RCPUtil.selectResourceInTree(resource);
              }
            } catch (Throwable ex) {
              ToolErrorHandler.handleError("Failed to navigate to resource.", ex, true);
            }
          }
        }
      }
    });

    // add drag support
    int ops = DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_LINK;
    DNDSupport.addDragSupport(tableViewer, ops);
    
    // Add the context menu for this explorer.
    MenuManager popupMenuManager = new MenuManager("#PopupMenu"); //$NON-NLS-1$
    Menu menu = popupMenuManager.createContextMenu(tableViewer.getControl());
    
    tableViewer.getControl().setMenu(menu);
    this.getSite().setSelectionProvider(tableViewer);
    this.getSite().registerContextMenu("gov.pnnl.cat.ui.rcp.preview", popupMenuManager, tableViewer);
  }

  /**
   * Method createColumns.
   * @param viewer TableViewer
   */
  private void createColumns(TableViewer viewer) {
    ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

    CellLabelProvider pathLabelProvider = new CellLabelProvider() {

      @Override
      public String getToolTipText(Object element) {
        IEvent event = (IEvent) element;
        if(event.getResourcePath() != null) {
          return event.getResourcePath().toDisplayString();
        } else {
          if(event.isValid() == false) {
            return "This document is no longer accessible.";
          } else {
            return event.getId();
          }
        }
      }

      /* (non-Javadoc)
       * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipImage(java.lang.Object)
       */
      @Override
      public Image getToolTipImage(Object object) {
        IEvent event = (IEvent) object;
        if(event.isValid()) {
          return super.getToolTipImage(object);
        } else {
          return SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOC_ERROR, SharedImages.CAT_IMG_SIZE_16);
        }
      }

      @Override
      public int getToolTipTimeDisplayed(Object object) {
        return 0;
      }

      @Override
      public void update(ViewerCell cell) {
        IEvent event = (IEvent) cell.getElement();
        cell.setText(event.getResourceName());

        if (!event.isValid()) {
          cell.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DOC_ERROR, SharedImages.CAT_IMG_SIZE_16));
        } else {
          IResource resource;
          try {
            resource = ResourcesPlugin.getResourceManager().getResource(event.getResourcePath());
            cell.setImage(SharedImages.getInstance().getImageForResource(resource, SharedImages.CAT_IMG_SIZE_16));
          } catch (ResourceException e) {
            throw new RuntimeException(e);
          }
        }
      }
    };

    CellLabelProvider changeLabelProvider = new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        IEvent event = (IEvent) cell.getElement();
        String text = "";

        switch (event.getChangeType()) {
          case NEW:
            text = "created";
            break;
          case DELETED:
            text = "deleted";
            break;
          case MODIFIED:
            text = "modified";
            break;
          case EXPIRED:
        	text = "expired";
        	break;
          case EXPIRING:
        	text = "expiring";
        	break;
        }

        cell.setText(text);
      }
    };

    CellLabelProvider changedByLabelProvider = new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        IEvent event = (IEvent) cell.getElement();
        IUser perp = event.getPerpetrator();
        if (perp != null) {
          String fullname = event.getPerpetrator().getFullName();
          if (fullname == null || fullname.equals("")) {
        	cell.setText(perp.getUsername());
          } else {
        	cell.setText(fullname);
          }
        }
      }
    };

    CellLabelProvider dateLabelProvider = new CellLabelProvider() {
      @Override
      public String getToolTipText(Object element) {
        IEvent event = (IEvent) element;
        return detailedDateFormat.format(event.getTime().getTime());
      }

      @Override
      public void update(ViewerCell cell) {
        IEvent event = (IEvent) cell.getElement();
        cell.setText(dateFormat.format(event.getTime().getTime()));
      }
    };
    
    CellLabelProvider locationLabelProvider = new CellLabelProvider() {
      @Override
      public void update(ViewerCell cell) {
        IEvent event = (IEvent) cell.getElement();
        if(event.getResourcePath() != null) {
          cell.setText(event.getResourcePath().toDisplayString());
        } else {
          cell.setText("");
        }
      }
    };

    TableViewerColumn filenameColumn = new TableViewerColumn(viewer, SWT.NONE);
    filenameColumn.setLabelProvider(pathLabelProvider);
    filenameColumn.getColumn().setText("Filename");
    filenameColumn.getColumn().setWidth(175);
    
    ColumnViewerSorter defaultSorter = new ColumnViewerSorter(viewer, filenameColumn) {
      protected int doCompare(Viewer viewer, Object o1, Object o2) {
        IEvent event1 = (IEvent) o1;
        IEvent event2 = (IEvent) o2;
        return event1.getResourceName().compareTo(event2.getResourceName());
      }
    };

    TableViewerColumn changeColumn = new TableViewerColumn(viewer, SWT.NONE);
    changeColumn.setLabelProvider(changeLabelProvider);
    changeColumn.getColumn().setText("Change");
    changeColumn.getColumn().setWidth(80);
    new ColumnViewerSorter(viewer, changeColumn) {
      protected int doCompare(Viewer viewer, Object o1, Object o2) {
        IEvent event1 = (IEvent) o1;
        IEvent event2 = (IEvent) o2;
        return event1.getChangeType().toString().compareTo(event2.getChangeType().toString());
      }
    };

    TableViewerColumn changedByColumn = new TableViewerColumn(viewer, SWT.NONE);
    changedByColumn.setLabelProvider(changedByLabelProvider);
    changedByColumn.getColumn().setText("Changed By");
    changedByColumn.getColumn().setWidth(80);
    new ColumnViewerSorter(viewer, changedByColumn) {
      protected int doCompare(Viewer viewer, Object o1, Object o2) {
        IEvent event1 = (IEvent) o1;
        IEvent event2 = (IEvent) o2;
        return event1.getPerpetrator().getFullName().compareTo(event2.getPerpetrator().getFullName());
      }
    };

    TableViewerColumn dateColumn = new TableViewerColumn(viewer, SWT.NONE);
    dateColumn.setLabelProvider(dateLabelProvider);
    dateColumn.getColumn().setText("Date");
    dateColumn.getColumn().setWidth(120);
    new ColumnViewerSorter(viewer, dateColumn) {
      protected int doCompare(Viewer viewer, Object o1, Object o2) {
        IEvent event1 = (IEvent) o1;
        IEvent event2 = (IEvent) o2;
        return event1.getTime().compareTo(event2.getTime());
      }
    };
    
    TableViewerColumn locationColumn = new TableViewerColumn(viewer, SWT.NONE);
    locationColumn.setLabelProvider(locationLabelProvider);
    locationColumn.getColumn().setText("Repository Location");
    locationColumn.getColumn().setWidth(300);
    new ColumnViewerSorter(viewer, locationColumn) {
      protected int doCompare(Viewer viewer, Object o1, Object o2) {
        IEvent event1 = (IEvent) o1;
        IEvent event2 = (IEvent) o2;
        String path1 = (event1.getResourcePath() == null ? "" : event1.getResourcePath().toDisplayString());
        String path2 = (event2.getResourcePath() == null ? "" : event2.getResourcePath().toDisplayString());
        return path1.compareTo(path2);
      }
    };

    defaultSorter.setSorter(defaultSorter, ColumnViewerSorter.ASC);
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.part.IPage#dispose()
   */
  @Override
  public void dispose() {
    if (control != null && !control.isDisposed()) {
      control.dispose();
    }
    getSite().getPage().removeSelectionListener(this);
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.part.IPage#setFocus()
   */
  @Override
  public void setFocus() {
    // Set the focus
    tableViewer.getControl().setFocus();
  }

  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param selection ISelection
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
   */
  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    if (selection != null && !selection.isEmpty()) {
      Object element = ((IStructuredSelection) selection).getFirstElement();
      
      if (element instanceof IAlert) {
        tableViewer.setInput(element);
        tableViewer.refresh();
      }
    }
  }

  /**
   * A very simple content provider that simply returns the IEvents from an IAlert.
   * @version $Revision: 1.0 $
   */
  private class EventsContentProvider implements IStructuredContentProvider {
    /**
     * Method dispose.
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    /**
     * Method inputChanged.
     * @param viewer Viewer
     * @param oldInput Object
     * @param newInput Object
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer, Object, Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    /**
     * Method getElements.
     * @param inputElement Object
     * @return Object[]
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
      return ((IAlert) inputElement).getEvents();
    }
  }
}
