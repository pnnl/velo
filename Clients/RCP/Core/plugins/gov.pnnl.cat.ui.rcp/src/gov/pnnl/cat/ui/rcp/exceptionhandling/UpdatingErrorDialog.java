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
package gov.pnnl.cat.ui.rcp.exceptionhandling;

import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.progress.UIJob;

/**
 */
public class UpdatingErrorDialog extends IconAndMessageDialog implements ErrorListener, IJobChangeListener {
  private ExceptionHandlerThread exceptionHandler;

  /**
   * A list of all of the errors that we need to display.
   */
  private List<ErrorEvent> errors = new ArrayList<ErrorEvent>();

  /**
   * I had an idea for using this, but it may not be necessary anymore.
   * Leaving it in until I'm certain.
   */
  private int totalErrorsDisplayed = 0;
  private boolean updateJobRunning = false;

  /**
   * The table viewer used to display errors.
   */
  private TableViewer errorTableViewer;

  private Logger logger = CatLogger.getLogger(UpdatingErrorDialog.class);

  // constants for the columns in the table
  private static final int COL_MSG  = 0;
  private static final int COL_PATH = 1;
  private static final int COL_DATE = 2;  


  /**
   * Constructor for UpdatingErrorDialog.
   * @param parentShell Shell
   * @param exceptionHandler ExceptionHandlerThread
   */
  public UpdatingErrorDialog(Shell parentShell, ExceptionHandlerThread exceptionHandler) {
    super(parentShell);

    // It doesn't matter what we set this to, as long as it is not null.
    // If it were null, the label to display it would not be created.
    // The value we set here will be overwritten before the dialog is actually
    // displayed.
    message = getMessage();

    // save the exception handler so that we can add/remove ourself as a listener.
    this.exceptionHandler = exceptionHandler;

    setShellStyle(SWT.DIALOG_TRIM | SWT.MODELESS | SWT.RESIZE | SWT.MIN
        | getDefaultOrientation()); // Do not want this one to be modal
    setBlockOnOpen(false);
  }

  /**
   * Create contents of the dialog
   * @param parent
   * @return Control
   */
  @Override
  protected Control createDialogArea(Composite parent) {
    createMessageArea(parent);

    // add ourself as a listener for new events.
    exceptionHandler.addErrorListener(this);

    message = getMessage();

    // create the composite that will sit below the image and message.
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
    layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
    layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
    layout.numColumns = 2;
    composite.setLayout(layout);
    GridData childData = new GridData(GridData.FILL_BOTH);
    childData.horizontalSpan = 2;
    composite.setLayoutData(childData);
    composite.setFont(parent.getFont());

    // add the table viewer to the composite
    createJobListArea(composite);

    return composite;
  }

//  /**
//   * Return the initial size of the dialog
//   */
//  @Override
//  protected Point getInitialSize() {
//    return new Point(315, 250);
//  }

  /**
   * Method getImage.
   * @return Image
   */
  @Override
  protected Image getImage() {
    return getErrorImage();
  }

  /**
   * Method errorReceived.
   * @param event ErrorEvent
   * @see gov.pnnl.cat.ui.rcp.exceptionhandling.ErrorListener#errorReceived(ErrorEvent)
   */
  public void errorReceived(final ErrorEvent event) {
    logger.debug("Error received");
    refresh(event);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.dialogs.Dialog#initializeBounds()
   */
  protected void initializeBounds() {
    super.initializeBounds();
    // using some internal classes to animate the dialog coming up.
    // this is not necessary and can be removed if we ever run into problems.
    Rectangle shellPosition = getShell().getBounds();
    ProgressManagerUtil.animateUp(shellPosition);
  }

  /**
   * The proper way to open this dialog.
   * 
   * @param error the initial <code>ErrorEvent</code> to display
  
   * @return int
   */
  public int open(ErrorEvent error) {
    synchronized (errors) {
      this.errors.add(error);
    }
    return super.open();
  }

  /**
   * Method close.
   * @return boolean
   */
  public boolean close() {
    boolean closed = super.close();
    if (closed) {
      // remove ourself as a listener.
      this.exceptionHandler.removeErrorListener(this);
      synchronized (errors) {
        totalErrorsDisplayed = 0;
        this.errors.clear();
      }
      // not sure if we still need to do this...
//      errorTableViewer = null;
    }
    return closed;
  }

  /**
   * Refreshes the dialog to display the specified error.
   * @param event
   */
  private void refresh(ErrorEvent event) {
    synchronized (errors) {
      this.errors.add(event);
    }
    scheduleRefreshJob();
  }

  /**
   * Schedules a job to run in the UI thread that will refresh the dialog with
   * the latest errors.
   */
  private void scheduleRefreshJob() {
    if (totalErrorsDisplayed < errors.size() && !updateJobRunning) {
      updateJobRunning = true;
      UpdateDialogJob updateDialogJob = new UpdateDialogJob("Updating Dialog");
      updateDialogJob.setSystem(true);
      updateDialogJob.addJobChangeListener(this);
      updateDialogJob.setPriority(Job.INTERACTIVE);
      updateDialogJob.schedule();
    }
  }

  /**
   * Method configureShell.
   * @param newShell Shell
   */
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText("Error Loading Resources");
  }

  /**
   * A UIJob that updates the dialog with the latest message and the latest
   * events in the table.
   *
   * @author Eric Marshall
   * @version $Revision: 1.0 $
   */
  private class UpdateDialogJob extends UIJob {
    /**
     * Constructor for UpdateDialogJob.
     * @param name String
     */
    public UpdateDialogJob(String name) {
      super(name);
    }

    /**
     * Method runInUIThread.
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
      // get the lock on the errors so we don't miss any
      synchronized (errors) {
        // keep track of how many errors we are currently reporting
        totalErrorsDisplayed = errors.size();

        // don't update if we are shutting down
        if (messageLabel != null && !messageLabel.isDisposed()) {
          messageLabel.setText(getMessage());
          messageLabel.redraw();
          messageLabel.getParent().layout();
          refreshErrorTable();
        }

        return Status.OK_STATUS;
      }
    }
  }

  /**
   * Returns the string that we should show in the dialog.
  
   * @return String
   */
  protected String getMessage() {
    StringBuilder msg = new StringBuilder();
    synchronized (errors) {
      if (errors.size() > 1) {
        msg.append("Multiple errors have occurred. Select an error below for details.\n\n");
        msg.append("Total errors: ");
        msg.append(errors.size());   
      } else {
        msg.append("An error has occurred.  Select it below for details.\n\n");
      }
    }

    return msg.toString();
  }

  /**
   * Return a viewer sorter for looking at the jobs.
   * 
  
   * @return ViewerSorter */
  private ViewerSorter getViewerSorter() {
    return new ViewerSorter() {
      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
       *      java.lang.Object, java.lang.Object)
       */
      public int compare(Viewer testViewer, Object e1, Object e2) {
        return ((ErrorEvent) e1).compareTo((ErrorEvent) e2);
      }
    };
  }

  /**
   * Sets the content provider for the viewer.
   */
  protected void initContentProvider() {
    IContentProvider provider = new IStructuredContentProvider() {
      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.IContentProvider#dispose()
       */
      public void dispose() {
        // Nothing of interest here
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
       */
      public Object[] getElements(Object inputElement) {
        synchronized (errors) {
          return errors.toArray();
        }
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
       *      java.lang.Object, java.lang.Object)
       */
      public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput != null) {
          refreshErrorTable();
        }
      }
    };
    errorTableViewer.setContentProvider(provider);
    errorTableViewer.setInput(errors);
  }

  /**
   * Refresh the contents of the viewer.
   */
  private void refreshErrorTable() {
    if (errorTableViewer != null && !errorTableViewer.getControl().isDisposed()) {
      errorTableViewer.refresh();
//      Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
//      getShell().setSize(newSize);
    }
  }

  private void initLabelProvider() {
    ITableLabelProvider provider = new ITableLabelProvider() {

      public void addListener(ILabelProviderListener listener) {
        // Do nothing
      }

      public void dispose() {
        // Do nothing
      }

      public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == COL_MSG) {
          return SharedImages.getInstance().getImage(SharedImages.CAT_IMG_BOOKS, SharedImages.CAT_IMG_SIZE_16);
        }
        return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
       *      int)
       */
      public String getColumnText(Object element, int columnIndex) {
        String text = null;

        if (element instanceof ErrorEvent) {
          ErrorEvent event = (ErrorEvent) element;

          switch (columnIndex) {
            case COL_MSG:
              StringBuilder strBuilder = new StringBuilder();
              Throwable err = event.getErrorStatus().getException();
              strBuilder.append(err.getClass().getName());

              if (err.getMessage() != null && err.getMessage().length() > 0) {
                strBuilder.append(": ");
                strBuilder.append(err.getMessage());
              }

              text = strBuilder.toString();
              break;
            case COL_PATH:
              text = event.getPath().toString();
              break;
            case COL_DATE:
              SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
              text = dateFormat.format(event.getDate());
              break;
          }
        }

        if (text == null) {
          text = "";
        }

        return text;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
       *      java.lang.String)
       */
      public boolean isLabelProperty(Object element, String property) {
        return false;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
       */
      public void removeListener(ILabelProviderListener listener) {
        // Do nothing
      }
    };
    errorTableViewer.setLabelProvider(provider);
  }

  /**
   * Method createTableColumns.
   * @param table Table
   */
  private void createTableColumns(Table table) {
    TableColumn col1 = new TableColumn(table, COL_MSG);
    col1.setText("Message");
    col1.setWidth(250);

    TableColumn col2 = new TableColumn(table, COL_PATH);
    col2.setText("Path");
    col2.setWidth(100);

    TableColumn col3 = new TableColumn(table, COL_DATE);
    col3.setText("Date");
    col3.setWidth(140);

    table.setHeaderVisible(true);
  }

  /**
   * Create an area that allow the user to select one of multiple jobs that
   * have reported errors
   * 
   * @param parent -
   *            the parent of the area
   */
  private void createJobListArea(Composite parent) {
    // Display a list of paths that have reported errors
    int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION;
    errorTableViewer = new TableViewer(parent, style);
    errorTableViewer.setSorter(getViewerSorter());

    createTableColumns(errorTableViewer.getTable());
    Control control = errorTableViewer.getControl();
    GridData data = new GridData(GridData.FILL_BOTH
        | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    data.heightHint = convertHeightInCharsToPixels(10);
    control.setLayoutData(data);
    initContentProvider();
    initLabelProvider();
    applyDialogFont(parent);



    errorTableViewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
      }
   });
  }

  /**
   * Method done.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(IJobChangeEvent)
   */
  public void done(IJobChangeEvent event) {
    updateJobRunning = false;
    scheduleRefreshJob();
  }

  /**
   * Method aboutToRun.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(IJobChangeEvent)
   */
  public void aboutToRun(IJobChangeEvent event) {}
  /**
   * Method awake.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(IJobChangeEvent)
   */
  public void awake(IJobChangeEvent event) {}
  /**
   * Method running.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(IJobChangeEvent)
   */
  public void running(IJobChangeEvent event) {}
  /**
   * Method scheduled.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(IJobChangeEvent)
   */
  public void scheduled(IJobChangeEvent event) {}
  /**
   * Method sleeping.
   * @param event IJobChangeEvent
   * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(IJobChangeEvent)
   */
  public void sleeping(IJobChangeEvent event) {}
}
