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
import gov.pnnl.cat.alerts.model.ISubscription;
import gov.pnnl.cat.alerts.model.ISubscription.Frequency;
import gov.pnnl.cat.alerts.model.RepositorySubscription;
import gov.pnnl.cat.alerts.model.SearchSubscription;
import gov.pnnl.cat.alerts.views.SubscriptionInput.UserDir;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.ServerException;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;
import gov.pnnl.cat.ui.rcp.views.adapters.ICatWorkbenchAdapter;
import gov.pnnl.velo.core.util.ToolErrorHandler;

import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class SubscriptionsView extends ViewPart implements IResourceEventListener {
  public final static String ID = "gov.pnnl.cat.alerts.views.SubscriptionManagement";

  private final static int COL_TITLE = 0;
  private final static int COL_TYPE = 1;
  private final static int COL_FREQUENCY = 2;
  private final static int COL_CHANNELS = 3;

  private TreeViewer treeViewer;

  private IAction refreshAction;
  private IAction deleteAction;
  private IAction newSubscriptionAction;
  private IContributionItem mWizardMenu;

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
    treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
    Tree tree = treeViewer.getTree();
    tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    tree.setHeaderVisible(true);
    createColumns(tree);

    SubscriptionInput input = new SubscriptionInput();
    input.setGroupByUsers(
        ResourcesPlugin.getSecurityManager().getActiveUser().isAdmin());

    treeViewer.setContentProvider(new DeferredTreeContentProvider(treeViewer));
    treeViewer.setLabelProvider(new SubscriptionLabelProvider());
    treeViewer.setInput(input);

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        deleteAction.setEnabled(!treeViewer.getSelection().isEmpty());
      }
    });
  }

  /**
   * Method onEvent.
   * @param events IBatchNotification
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#onEvent(IBatchNotification)
   */
  @Override
  public void onEvent(IBatchNotification events) {
    boolean needsRefresh = false;

    for (IResourceEvent event : events) {
      if (ISubscription.PATH_SUB_ROOT.isPrefixOf(event.getPath())) {
        needsRefresh = true;
        break;
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

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    // TODO Auto-generated method stub
    super.dispose();
    ResourcesPlugin.getResourceManager().removeResourceEventListener(this);
  }

  /**
   * Method createColumns.
   * @param tree Tree
   */
  private void createColumns(Tree tree) {
    TreeColumn column1 = new TreeColumn(tree, SWT.LEFT);
    column1.setText("Name");
    column1.setWidth(175);

    TreeColumn column2 = new TreeColumn(tree, SWT.LEFT);
    column2.setText("Type");
    column2.setWidth(55);

    TreeColumn column3 = new TreeColumn(tree, SWT.LEFT);
    column3.setText("Frequency");
    column3.setWidth(70);

    TreeColumn column4 = new TreeColumn(tree, SWT.LEFT);
    column4.setText("Delivery Channels");
    column4.setWidth(120);

    //    fColumn1.addSelectionListener(new SelectionAdapter() {
    //      public void widgetSelected(SelectionEvent e) {
    //        MESSAGE_ORDER *= -1;
    //        ViewerComparator comparator = getViewerComparator(MESSAGE);
    //        fFilteredTree.getViewer().setComparator(comparator);
    //        boolean isComparatorSet = ((EventDetailsDialogAction) fPropertiesAction).resetSelection(MESSAGE, MESSAGE_ORDER);
    //        setComparator(MESSAGE);
    //        if (!isComparatorSet)
    //          ((EventDetailsDialogAction) fPropertiesAction).setComparator(fComparator);
    //        fMemento.putInteger(P_ORDER_VALUE, MESSAGE_ORDER);
    //        fMemento.putInteger(P_ORDER_TYPE, MESSAGE);
    //        setColumnSorting(fColumn1, MESSAGE_ORDER);
    //      }
    //    });

  }

  /**
   * Create the actions
   */
  private void createActions() {
    // Create the actions
    refreshAction = new RefreshAction();
    deleteAction = new DeleteAction();
    //    newSubscriptionAction = new NewSubscriptionAction();

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        deleteAction.setEnabled(
            !treeViewer.getSelection().isEmpty() &&
            ((IStructuredSelection) treeViewer.getSelection()).getFirstElement() instanceof ISubscription);
      }
    });
    deleteAction.setEnabled(false);

    mWizardMenu = ContributionItemFactory.NEW_WIZARD_SHORTLIST.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
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
    mgr.add(new GroupMarker("newSubscriptions"));
    mgr.add(new Separator());
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
   * @param subscription ISubscription
   * @return String
   */
  private String getLabel(ISubscription subscription) {
    String label = subscription.getTitle();

    if (label == null || label.length() == 0) {
      label = subscription.getName();
    }

    return label;
  }

  /**
   */
  private class NewSubscriptionAction extends Action {
    public NewSubscriptionAction() {
      super("New Subscription");
      setImageDescriptor(AlertsPlugin.getDefault().getImageRegistry().getDescriptor(AlertsPlugin.IMG_SUBSCRIPTION_SEARCH));
      setToolTipText("Create a New Subscription");
    }

    /**
     * Method run.
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {

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
      ISubscription subscription = (ISubscription) ((IStructuredSelection) treeViewer.getSelection()).getFirstElement();

      boolean delete = MessageDialog.openConfirm(
          getSite().getShell(),
          "Confirm Delete",
          "Are you sure you want to remove the subscription \"" + getLabel(subscription) + "\"?\nThis operation cannot be undone.");
      if (delete) {
        try {
          AlertsPlugin.getDefault().getAlertsService().deleteSubscription(subscription);
          treeViewer.refresh();
        } catch (ServerException e) {
          ToolErrorHandler.handleError("An error occurred while removing the subscription.", e, true);
        }
      }
    }
  }

  /**
   */
  private class SubscriptionLabelProvider extends LabelProvider implements ITableLabelProvider {

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

      if (element instanceof ISubscription) {
        switch (columnIndex) {
          case COL_TITLE:
            if (element instanceof SearchSubscription) {
              img = AlertsPlugin.getDefault().getImageRegistry().get(AlertsPlugin.IMG_SUBSCRIPTION_SEARCH);
            }else if(element instanceof RepositorySubscription){
            	img = AlertsPlugin.getDefault().getImageRegistry().get(AlertsPlugin.IMG_SUBSCRIPTION);
            }
            break;
          case COL_TYPE:
            break;
          case COL_FREQUENCY:
            break;
          default:
            break;
        }
      } else if (element instanceof UserDir) {
        if (columnIndex == COL_TITLE) {
          img = SharedImages.getInstance().getImage(SharedImages.CAT_IMG_PERSON, SharedImages.CAT_IMG_SIZE_16);
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

      if (element instanceof ISubscription) {
        ISubscription subscription = (ISubscription) element;

        switch (columnIndex) {
          case COL_TITLE:
            text = getLabel(subscription);
            break;
          case COL_TYPE:
            if (element instanceof SearchSubscription) {
              text = "Search";
            }else if (element instanceof RepositorySubscription){
              text = "Repository";
            }
            break;
          case COL_FREQUENCY:
            Frequency frequency = subscription.getFrequency();
            switch (frequency) {
              case DAILY:
                text = "Daily";
                break;
              case WEEKLY:
                text = "Weekly";
                break;
              case HOURLY:
            	text = "Hourly";
            	break;
            }
            break;
          case COL_CHANNELS:
            String[] friendlyNames = new String[subscription.getChannels().length];

            for (int i = 0; i < subscription.getChannels().length; i++) {
              switch (subscription.getChannels()[i]) {
                case EMAIL:
                  friendlyNames[i] = "Email";
                  break;
                case REPOSITORY:
                  friendlyNames[i] = "CAT";
                  break;
              }
            }

            StringBuilder sb = new StringBuilder();
            String separator = ", ";
            Arrays.sort(friendlyNames);

            if (friendlyNames.length > 0) {
              sb.append(friendlyNames[0]);
              for (int i = 1; i < friendlyNames.length; i++) {
                sb.append(separator).append(friendlyNames[i]);
              }
            }

            text = sb.toString();
        }
      } else if (element instanceof UserDir) {
        if (columnIndex == COL_TITLE) {
          UserDir userDir = (UserDir) element;
          ICatWorkbenchAdapter catAdapter = RCPUtil.getCatAdapter(userDir.getUser());
          text = catAdapter.getLabel(userDir.getUser());
        }
      }

      return text;
    }
  }

  /**
   * Method cacheCleared.
   * @see gov.pnnl.cat.core.resources.events.IResourceEventListener#cacheCleared()
   */
  @Override
  public void cacheCleared() {
    // TODO Auto-generated method stub
    
  }
}
