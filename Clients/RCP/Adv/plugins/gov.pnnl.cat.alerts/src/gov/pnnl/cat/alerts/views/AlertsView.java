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

import gov.pnnl.cat.alerts.AlertsPlugin;
import gov.pnnl.cat.alerts.model.Alert;
import gov.pnnl.cat.alerts.model.IAlert;
import gov.pnnl.cat.alerts.model.ISubscription.Type;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class AlertsView extends ViewPart implements IResourceEventListener {
  public final static String ID = "gov.pnnl.cat.alerts.views.AlertsView";

  private final static int COL_TITLE = 0;
  private final static int COL_TIME = 1;
  private final static int COL_EVENTS = 2;
  private final static int COL_RECIPIENTS = 3;

  private TreeViewer treeViewer;

  private IAction refreshAction;
  private IAction deleteAction;
  private DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a");

  private final static Logger logger = CatLogger.getLogger(AlertsView.class);

  /**
   * Create contents of the view part
   * @param parent
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    container.setLayout(new GridLayout());

    try {
      createViewer(container);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //
    createActions();
    initializeToolBar();
    initializeMenu();
    createContextMenu();

    ResourcesPlugin.getResourceManager().addResourceEventListener(this);
  }

  /**
   * Method createViewer.
   * @param parent Composite
   * @throws ServerException
   * @throws ResourceException
   */
  private void createViewer(Composite parent) throws ServerException, ResourceException {
    treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
    Tree tree = treeViewer.getTree();
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    tree.setHeaderVisible(true);
    createColumns(tree);

    AlertsInput input = new AlertsInput();

    treeViewer.setContentProvider(new DeferredTreeContentProvider(treeViewer));
    treeViewer.setLabelProvider(new AlertsLabelProvider());
    treeViewer.setComparator(new AlertsViewerComparator());
    treeViewer.setInput(input);

    treeViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        try {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(AlertsPreviewPage.ID, null, IWorkbenchPage.VIEW_VISIBLE);

          IAlert[] alerts = getSelection();

          if (alerts.length > 0) {
            IAlert visibleAlert = alerts[0];

            if (!visibleAlert.isRead()) {
              //removed second param, boolean isRead for now to keep service impl easy, can add back later if its needed
              AlertsPlugin.getDefault().getAlertsService().markAlertsAsRead(new IAlert[] {visibleAlert});

              // we're going to cheat a little bit here
              // and assume that, since there was no exception thrown,
              // the alert was marked as "read."
              // this saves us from hitting the server an extra time
              // to verify what we already know *should* be true.
              ((Alert) visibleAlert).setRead(true);
              treeViewer.update(visibleAlert, null);
            }
          }

        } catch (Throwable e) {
          ToolErrorHandler.handleError("An error occurred trying to view the alert.", e, true);
        }
      }
    });

    getSite().setSelectionProvider(treeViewer);
  }

  /**
   * Method createColumns.
   * @param tree Tree
   */
  private void createColumns(Tree tree) {
    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
    column1.setText("Name");
    column1.setWidth(220);

    TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
    column2.setText("Date");
    column2.setWidth(150);

    TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
    column3.setText("Events");
    column3.setWidth(50);

    TreeColumn column4 = new TreeColumn(tree, SWT.LEFT);
    column4.setText("Recipients");
    column4.setWidth(100);

    // TODO: add support for sorting
  }

  /**
   * Create the actions
   */
  private void createActions() {
    // Create the actions
    refreshAction = new RefreshAction();
    deleteAction = new DeleteAction();

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        deleteAction.setEnabled(
            !treeViewer.getSelection().isEmpty() &&
            ((IStructuredSelection) treeViewer.getSelection()).getFirstElement() instanceof IAlert);
      }
    });
    deleteAction.setEnabled(false);
  }

  /**
   * Initialize the toolbar
   */
  private void initializeToolBar() {
    IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    toolbarManager.add(refreshAction);
    toolbarManager.add(deleteAction);
  }

  /**
   * Initialize the menu
   */
  private void initializeMenu() {
    IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
  }
  
  private void createContextMenu() {
    // Create menu manager
    MenuManager menuMgr = new MenuManager();
    menuMgr.setRemoveAllWhenShown(true);
    menuMgr.addMenuListener(new IMenuListener() {
      @Override
      public void menuAboutToShow(IMenuManager mgr) {
        fillContextMenu(mgr);
      }
    });

    // Create menu
    Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
    treeViewer.getControl().setMenu(menu);

    // Register menu for extension
    getSite().registerContextMenu(menuMgr, treeViewer);
  }

  /**
   * Method fillContextMenu.
   * @param mgr IMenuManager
   */
  private void fillContextMenu(IMenuManager mgr) {
    mgr.add(refreshAction);
    mgr.add(deleteAction);
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    // Set the focus
    treeViewer.getControl().setFocus();
  }

  /**
   * Method getLabel.
   * @param alert IAlert
   * @return String
   */
  private String getLabel(IAlert alert) {
    String label = alert.getTitle();

    if (label == null || label.length() == 0) {
      label = alert.getName();
    }

    return label;
  }

  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    boolean needsRefresh = false;
    final List<String> toUpdate = new ArrayList<String>();

    for (IResourceEvent event : events) {
      if (IAlert.PATH_ALERT_ROOT.isPrefixOf(event.getPath())) {
        if (logger.isDebugEnabled()) {
          logger.debug(String.format("Changes to %s:\n\tadd: %b\n\tdel: %b\n\tprop: %b\n\tcontent: %b\n\taspects: %b",
              event.getPath().toDisplayString(),
              event.hasChange(IResourceEvent.ADDED),
              event.hasChange(IResourceEvent.REMOVED),
              event.hasChange(IResourceEvent.PROPERTY_CHANGED),
              event.hasChange(IResourceEvent.CONTENT_CHANGED),
              event.hasChange(IResourceEvent.ASPECTS_CHANGED)
              ));
        }

        if (event.hasChange(IResourceEvent.ADDED)) {
          needsRefresh = true;
          break;
        }
      }
    }

    if (needsRefresh) {
      getSite().getShell().getDisplay().asyncExec(new Runnable() {
        @Override
        public void run() {
          treeViewer.refresh();
        }
      });
    }
  }

  /**
   */
  private class RefreshAction extends Action {
    public RefreshAction() {
      super("Refresh");
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_REFESH, SharedImages.CAT_IMG_SIZE_16));
      setToolTipText("Refresh View");
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      treeViewer.refresh();
    }
  }

  /**
   * Method getSelection.
   * @return IAlert[]
   */
  private IAlert[] getSelection() {
    Object[] items = ((IStructuredSelection) treeViewer.getSelection()).toArray();
    IAlert[] alerts = new IAlert[items.length];

    for (int i = 0; i < alerts.length; i++) {
      alerts[i] = (IAlert) items[i];
    }

    return alerts;
  }

  /**
   */
  private class DeleteAction extends Action {
    public DeleteAction() {
      super("Delete");
      setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_DEL, SharedImages.CAT_IMG_SIZE_16));
      setToolTipText("Delete");
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
      IAlert[] alerts = getSelection();
      String message;

      if (alerts.length == 1) {
        message = String.format("Are you sure you want to remove the alert \"%s\"?\n\nThis operation cannot be undone.", getLabel(alerts[0]));
      } else {
        Assert.isTrue(alerts.length > 1);
        message = String.format("Are you sure you want to remove these %d alerts?\n\nThis operation cannot be undone.", alerts.length);
      }

      boolean delete = MessageDialog.openConfirm(
          getSite().getShell(),
          "Confirm Delete",
          message
      );
      if (delete) {
        try {
          AlertsPlugin.getDefault().getAlertsService().deleteAlerts(alerts);
          treeViewer.refresh();
        } catch (ServerException e) {
          ToolErrorHandler.handleError("An error occurred removing the alert.", e, true);
        }
      }
    }
  }

  /**
   */
  private class AlertsLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

    /**
     * Method getColumnImage.
     * @param element Object
     * @param columnIndex int
     * @return Image
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
     */
    @Override
    public Image getColumnImage(Object element, int columnIndex) {
      Image img = null;

      if (element instanceof IAlert) {
        if (columnIndex == COL_TITLE) {
          Type type = ((IAlert) element).getSubscriptionType();
          switch (((IAlert) element).getSubscriptionType()) {
            case SEARCH:
              img = AlertsPlugin.getDefault().getImageRegistry().get(AlertsPlugin.IMG_SUBSCRIPTION_SEARCH);
              break;
            case REPOSITORY:
              img = AlertsPlugin.getDefault().getImageRegistry().get(AlertsPlugin.IMG_SUBSCRIPTION);
              break;
          }
        }
      }

      return img;
    }

    /**
     * Method getColumnText.
     * @param element Object
     * @param columnIndex int
     * @return String
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
     */
    @Override
    public String getColumnText(Object element, int columnIndex) {
      String text = "";

      if (element instanceof IAlert) {
        IAlert alert = (IAlert) element;

        switch (columnIndex) {
          case COL_TITLE:
            text = getLabel(alert);
            break;
          case COL_TIME:
            Calendar created = alert.getCreated();
            text = dateFormat.format(created.getTime());
            break;
          case COL_EVENTS:
            int length = alert.getEvents().length;
            text = Integer.toString(length);
            break;
          case COL_RECIPIENTS:
            IUser[] users = alert.getRecipients();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < users.length; i++) {
              sb.append(users[i].getFullName());
              if (i+1 < users.length) {
                sb.append(", ");
              }
            }
            text = sb.toString();
            break;
        }
      }

      return text;
    }

    /**
     * Method getFont.
     * @param element Object
     * @return Font
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(Object)
     */
    @Override
    public Font getFont(Object element) {
      Font font = null;

      if (element instanceof IAlert) {
        IAlert alert = (IAlert) element;
        if (!alert.isRead()) {
          font = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
        }
      }

      return font;
    }
  }

  /**
   */
  private final class AlertsViewerComparator extends ViewerComparator {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
      int category;

      if (element instanceof IAlert) {
        category = 0;
      } else {
        category = 1;
      }

      return category;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#sort(org.eclipse.jface.viewers.Viewer, java.lang.Object[])
     */
    @Override
    public void sort(Viewer viewer, Object[] elements) {
      super.sort(viewer, elements);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
      int dif;

      if (e1 instanceof IAlert && e2 instanceof IAlert) {
        IAlert alert1 = (IAlert) e1;
        IAlert alert2 = (IAlert) e2;
        // subtract by one to sort descending
        dif = 1 - alert1.compareTo(alert2);
      } else {
        dif = super.compare(viewer, e1, e2);
      }

      return dif;
    }
  }

//  /* (non-Javadoc)
//   * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
//   */
//  @Override
//  public Object getAdapter(Class adapter) {
//    if (adapter == IPreviewPage.class) {
//      return new AlertsPreviewPage();
//    }
//    return super.getAdapter(adapter);
//  }

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }

}
