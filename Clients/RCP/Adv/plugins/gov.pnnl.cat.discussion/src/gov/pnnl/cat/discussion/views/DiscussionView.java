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
package gov.pnnl.cat.discussion.views;

import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.events.IBatchNotification;
import gov.pnnl.cat.core.resources.events.IResourceEvent;
import gov.pnnl.cat.core.resources.events.IResourceEventListener;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.cat.discussion.DiscussionConstants;
import gov.pnnl.cat.discussion.DiscussionViewComposite;
import gov.pnnl.cat.discussion.IDiscussionListener;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class DiscussionView extends ViewPart implements ISelectionListener, IDiscussionListener, IPerspectiveListener {

  private final static int MAX_LENGTH = 1024 * 100;

  public static final int COLUMN_SUBJECT = 0;

  public static final int COLUMN_REPLIES = 1;

  private ScrolledComposite commentsScrolledComposite;

  private DiscussionViewComposite commentsComposite;

  private static String Comment_new = "&Create New Comment";

  private static String Comment_new_tooltip = "Create New Comment";

  private IResource currentSelection;

  protected DiscussionDialog newMessageDialog;

  private Action createNewComment;

  private static final Logger logger = CatLogger.getLogger(DiscussionView.class);

  public DiscussionView() {
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    // Create ScrolledComposite that will hold all of the comments
    commentsScrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.WRAP);
    commentsScrolledComposite.setLayout(new GridLayout(1, true));
    commentsScrolledComposite.setAlwaysShowScrollBars(false);

    createActions();
    initializeToolBar();
    initializedResourceListener();
    ISelection selection = this.getSite().getPage().getSelection();

    if (selection != null && !selection.isEmpty()) {
      selectionChanged(this, this.getSite().getPage().getSelection());
    }

    initializeListeners();
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    // important: We need to unregister our listener when the view is disposed
    getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
    getSite().getWorkbenchWindow().removePerspectiveListener(this);
    super.dispose();
  }

  private void initializeListeners() {
    getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
    getSite().getWorkbenchWindow().addPerspectiveListener(this);
  }

  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
  }

  /**
   * Method selectionChanged.
   * @param part IWorkbenchPart
   * @param selection ISelection
   * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
   */
  public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    logger.debug("Selection changed: " + selection);
    IResource previousSelection = currentSelection;

    if (selection == null || selection.isEmpty()) {
      logger.debug("Ignoring empty selection");
      // clear view
      // logger.debug("Empty selection, clearing");
      // clear();
      // currentSelection = null;
      // createNewComment.setEnabled(false);
      return;
    }

    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      Object selectedItem = structuredSelection.getFirstElement();
      IResource resource = RCPUtil.getResource(selectedItem);

      if (resource == null) {
        // clear view
        logger.debug("Non-resource selection, clearing");
        clear();
        currentSelection = null;
        createNewComment.setEnabled(false);
        return;
      }

      logger.debug("Comparing to previous selection (" + previousSelection + ")");
      if (!resource.equals(previousSelection)) {
        showComments(resource);
        createNewComment.setEnabled(true);
      }
    }
  }

  /**
   * Method showComments.
   * @param resource IResource
   */
  private void showComments(IResource resource) {
    currentSelection = resource;
    logger.debug("Valid selection: " + resource);

    try {
      if (resource.hasAspect(DiscussionConstants.ASPECT_DISCUSSABLE)) {
        logger.debug("scheduling job");
        GetTopicsJob getTopicsJob = new GetTopicsJob(resource);
        getTopicsJob.setPriority(Job.SHORT);
        getTopicsJob.schedule();
      } else {
        Runnable task = new Runnable() {
          public void run() {
            commentsScrolledComposite.setMinSize(SWT.DEFAULT, SWT.DEFAULT);
            clear();
          }
        };

        // perform the task in the UI thread
        if (Display.getCurrent() == null) {
          Display.getDefault().syncExec(task);
        } else {
          task.run();
        }
      }
    } catch (ResourceException e) {
      ToolErrorHandler.handleError("An error occurred loading the discussion data for: " + resource.getPath().toDisplayString(), e, true);
    }
  }

  private void initializeToolBar() {
    IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
    toolBarManager.add(createNewComment);
  }

  private void createActions() {
    createNewComment = new Action(Comment_new) { //$NON-NLS-1$
      public void run() {
        newMessageDialog = new DiscussionDialog(Display.getCurrent().getActiveShell(), currentSelection);
        newMessageDialog.open();
      }
    };
    createNewComment.setToolTipText(Comment_new_tooltip); //$NON-NLS-1$
    createNewComment.setImageDescriptor(SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_NEW_COMMENT, SharedImages.CAT_IMG_SIZE_16));

  }

  /**
   */
  private class GetTopicsJob extends Job {

    private IResource resource;

    /**
     * Constructor for GetTopicsJob.
     * @param resource IResource
     */
    public GetTopicsJob(IResource resource) {
      super("Loading Topics");
      this.resource = resource;
    }

    /**
     * Method run.
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {

      try {
        IResourceManager manager = ResourcesPlugin.getDefault().getResourceManager();
        final Comment[] comments = manager.getComments(resource.getPath());

        if (currentSelection != null && resource.equals(currentSelection)) {
          Display.getDefault().syncExec(new Runnable() {
            public void run() {
              logger.debug("updating view");
              if (!commentsScrolledComposite.isDisposed()) {
                clear();
                if (comments.length >= 0) {
                  commentsComposite = new DiscussionViewComposite(commentsScrolledComposite, SWT.NONE);
                  commentsComposite.addListener(DiscussionView.this);

                  commentsScrolledComposite.setContent(commentsComposite);
                  commentsScrolledComposite.setExpandVertical(true);
                  commentsScrolledComposite.setExpandHorizontal(true);

                  commentsComposite.createSections(comments);
                  commentsComposite.changeTopControl(1);
                  commentsComposite.layout();
                }
              }
            }
          });
        }

        return Status.OK_STATUS;
      } catch (Exception e) {
        ToolErrorHandler.handleError("An error occured during the loading of topics.", e, true);
        // return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could not load topics.", e);
        return Status.CANCEL_STATUS;
      }
    }
  }

  /**
   * Method resizeLayout.
   * @see gov.pnnl.cat.discussion.IDiscussionListener#resizeLayout()
   */
  public void resizeLayout() {
    commentsComposite.layout();
    commentsScrolledComposite.setMinSize(commentsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    if (commentsComposite.getCurrentViewer() != null) {
      commentsScrolledComposite.setOrigin(0, commentsComposite.getSearchStringPointLocation());
    }
  }

  private void clear() {
    if (commentsComposite != null && !commentsComposite.isDisposed()) {
      commentsComposite.removeListener(this);
      commentsComposite.dispose();
      commentsScrolledComposite.setContent(null);
    }
  }

  private void initializedResourceListener() {
    ResourcesPlugin.getDefault().getResourceManager().addResourceEventListener(new IResourceEventListener() {
      public void onEvent(IBatchNotification events) {
        handleOnEvent(events);
      }

      @Override
      public void cacheCleared() {
        // TODO Auto-generated method stub
        
      }
    });
  }

  /**
   * Method handleOnEvent.
   * @param notification IBatchNotification
   */
  private void handleOnEvent(IBatchNotification notification) {
    // if nothing is selected, ignore all events
    if (currentSelection == null) {
      return;
    }

    boolean updateView = false;
    Iterator<IResourceEvent> events = notification.getNonRedundantEvents();

    while (events.hasNext() && !updateView) {
      CmsPath eventPath = events.next().getPath();
      updateView = currentSelection != null && currentSelection.getPath().isPrefixOf(eventPath);
    }

    if (updateView) {
      logger.debug("Updating view as a result of a notification");
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          clear();
        }
      });
      showComments(currentSelection);
    }
  }

  /**
   * Method perspectiveActivated.
   * @param page IWorkbenchPage
   * @param perspective IPerspectiveDescriptor
   * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)
   */
  public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    selectionChanged(page.getActivePart(), page.getSelection(perspective.getId()));
  }

  /**
   * Method perspectiveChanged.
   * @param page IWorkbenchPage
   * @param perspective IPerspectiveDescriptor
   * @param changeId String
   * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(IWorkbenchPage, IPerspectiveDescriptor, String)
   */
  public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
  }
}
