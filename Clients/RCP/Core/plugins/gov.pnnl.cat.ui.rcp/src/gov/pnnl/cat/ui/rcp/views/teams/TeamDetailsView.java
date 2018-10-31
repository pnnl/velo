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
package gov.pnnl.cat.ui.rcp.views.teams;

import gov.pnnl.velo.model.CmsPath;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.core.resources.security.IProfilable;
import gov.pnnl.cat.core.resources.security.ISecurityManager;
import gov.pnnl.cat.core.resources.security.ITeam;
import gov.pnnl.cat.core.resources.security.IUser;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;
import gov.pnnl.cat.ui.rcp.CatViewIDs;
import gov.pnnl.cat.ui.rcp.model.UserInput;
import gov.pnnl.cat.ui.rcp.views.adapters.CatWorkbenchLabelProvider;
import gov.pnnl.cat.ui.rcp.views.adapters.TableCatWorkbenchProvider;
import gov.pnnl.cat.ui.rcp.views.profile.ProfileDetailsView;
import gov.pnnl.cat.ui.rcp.views.users.UserSorter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

/**
 */
public class TeamDetailsView extends ProfileDetailsView {

  private Text homeFolder;
  private Text teamDescription;
  private Text teamName;
  private Label pictureLabel;
  private Composite topComposite;
  private Composite bottomComposite;
  private Table table;
  private TableViewer membersTable;
  public final static int MAX_DESC_LENGTH = 512;

  private static Logger logger = CatLogger.getLogger(TeamDetailsView.class);
  
  //inner class to manage get UserInput for thr team
  /**
   */
  public class GetUserInputJob extends Job
  {
    private ITeam team;
    private UserInput userInput;
    /**
     * Constructor for GetUserInputJob.
     * @param name String
     * @param team ITeam
     */
    public GetUserInputJob(String name, ITeam team) {
      super(name);
      this.team = team;
    }

    /**
     * Method run.
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
      if (monitor == null) {
        monitor = new NullProgressMonitor();
      }

      if (monitor.isCanceled()) {
        return Status.CANCEL_STATUS;
      }

      try {
        monitor.beginTask("Getting users", IProgressMonitor.UNKNOWN);
        //do work here
        
        ISecurityManager mgr = ResourcesPlugin.getSecurityManager();
        List<String> members = mgr.getTeam(team.getPath()).getMembers();//get the team anew so as we can get fresh members

        monitor.subTask("Creating Image");
        long begin = System.currentTimeMillis();

        userInput = new UserInput(mgr.getUsers(members));
        userInput.setFilterSpecialUsers(true);

        if (monitor.isCanceled()) {
          return Status.CANCEL_STATUS;
        }

        long end = System.currentTimeMillis();
        logger.debug("get team UserInput in " + (end - begin) + " ms");
        
        monitor.subTask("Done!");
      } catch (Exception e) {
        //EZLogger.logWarning("Unable to create UserInput for  " + team.getName(), e);
        logger.warn("Unable to create UserInput for " + team.getName(),e);
      }

      return Status.OK_STATUS;
    }

    /**
     * Method getUserInput.
     * @return UserInput
     */
    public UserInput getUserInput() {
      return userInput;
    }
  }

  public TeamDetailsView() {
  }

  /**
   * Method createPartControl.
   * @param parent Composite
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(Composite)
   */
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    parent.setLayout(new GridLayout());

    //sash to seperate top from bottom
    SashForm sash = new SashForm(parent, SWT.VERTICAL);
    sash.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));

    //top scroll so that if image for team is wide (like CAT's) the user can scroll to see
    ScrolledComposite topScroll = new ScrolledComposite(sash, SWT.V_SCROLL | SWT.H_SCROLL);
    topScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    topScroll.setLayout(new FillLayout());

    //topComposite has team info (name, desc, etc.) and logo (2 seperate composites)
    topComposite = new Composite(topScroll, SWT.NONE);
    topComposite.setLocation(0, 0);
    GridLayout topCompgridLayout = new GridLayout();
    topCompgridLayout.numColumns = 3;
    topComposite.setLayout(topCompgridLayout);
    topComposite.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));

    //Add top comp as the scroll comp's content:
    topScroll.setContent(topComposite);

    //labels and text for team info:
    //team name stuff;
    Label label = new Label(topComposite, SWT.NONE);
    GridData nameGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
    label.setLayoutData(nameGridData);
    label.setText("Team Name:");
    teamName = new Text(topComposite, SWT.WRAP);
    teamName.setEditable(false);
    teamName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    pictureLabel = new Label(topComposite, SWT.NULL);
    pictureLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 1, 3));

    //description stuff:
    Label descriptionLabel = new Label(topComposite, SWT.NONE);
    GridData descriptionGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
    descriptionGridData.minimumWidth = -1;
    descriptionLabel.setLayoutData(descriptionGridData);
    descriptionLabel.setText("Description:");
    teamDescription = new Text(topComposite, SWT.WRAP);
    teamDescription.setEditable(false);
    GridData descriptionTextGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
    descriptionTextGridData.widthHint = 175;
    teamDescription.setLayoutData(descriptionTextGridData);

    //home folder stuff
    Label homeFolderLabel = new Label(topComposite, SWT.NONE);
    GridData homeFolderGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
    homeFolderLabel.setLayoutData(homeFolderGridData);
    homeFolderLabel.setText("Home Folder:");
    homeFolder = new Text(topComposite, SWT.WRAP);
    homeFolder.setEditable(false);
    homeFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

    //bottom composite holds the table of team members
    bottomComposite = new Composite(sash, SWT.NONE);
    bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    GridLayout bottomCompGridLayout = new GridLayout();
    bottomComposite.setLayout(bottomCompGridLayout);
    bottomComposite.setBackground(parent.getBackground());

    //member label:
    Label membersLabel = new Label(bottomComposite, SWT.NONE);
    membersLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    membersLabel.setText("Members:");

    //composite to hold table
    Composite composite = new Composite(bottomComposite, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    composite.setLayout(new GridLayout());

    //table of team members:
    membersTable = new TableViewer(composite);
    table = membersTable.getTable();
    table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    membersTable.setContentProvider(new TableCatWorkbenchProvider());
    membersTable.setLabelProvider(new CatWorkbenchLabelProvider(membersTable));
    membersTable.setSorter(new UserSorter());

    sash.setWeights(new int[] {60, 40});
    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
    gd.verticalSpan = 1;
    sash.setLayoutData(gd);

    getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(CatViewIDs.TEAM, this);
    layout();
  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose() {
    getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(CatViewIDs.TEAM, this);
    super.dispose();
  }


  /**
   * Method setFocus.
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus() {
//    this.composite.setFocus();
  }

  /**
   * Method setImage.
   * @param img Image
   */
  protected void setImage(Image img) {
    pictureLabel.setImage(img);
  }

  protected void layout() {
    topComposite.setSize(topComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    topComposite.layout();
  }

  /**
   * Method getDefaultImageDescriptor.
   * @return ImageDescriptor
   */
  protected ImageDescriptor getDefaultImageDescriptor() {
    return SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_TEAMS, SharedImages.CAT_IMG_SIZE_48);
  }

  /**
   * Method getErrorImageDescriptor.
   * @return ImageDescriptor
   */
  protected ImageDescriptor getErrorImageDescriptor() {
    return SharedImages.getInstance().getImageDescriptor(SharedImages.CAT_IMG_TEAMS_ERROR, SharedImages.CAT_IMG_SIZE_48);
  }

  /**
   * Method selectionChanged.
   * @param profilable IProfilable
   */
  public void selectionChanged(IProfilable profilable) {

    if (profilable == null) {
      // clear the view
      this.teamName.setText("");
      this.teamDescription.setText("");
      this.homeFolder.setText("");
      membersTable.setInput(new UserInput(new ArrayList<IUser>(0)));

    } else {
      if (profilable instanceof ITeam) {
        final ITeam team = (ITeam) profilable;

        this.teamName.setText(getNonNullString(team.getName()));
        String desc = team.getDescription();
        if(desc != null && desc.length() > MAX_DESC_LENGTH) {
          desc = desc.substring(0, MAX_DESC_LENGTH) + "...";
        }

        this.teamDescription.setText(getNonNullString(desc));
        CmsPath homeFolder = team.getHomeFolder();
        if (homeFolder == null) {
          this.homeFolder.setText("");
        } else {
          // this will throw a null pointer exception if the home folder is null...
          // will that ever happen?
          this.homeFolder.setText(getNonNullString(team.getHomeFolder().toDisplayString()));
        }

        //Have a GetUserInputJob to handle the potential Alfresco server call in 
        //a different thread so UI would be more responsive
        final GetUserInputJob getUserInputJob = new GetUserInputJob("Getting UserInput", team);
        getUserInputJob.setPriority(Job.DECORATE);
        getUserInputJob.setSystem(true);

        // add a listener so that when the job completes (i.e. UserInput is 
        // obtained for the team), we update memberstable
        getUserInputJob.addJobChangeListener(new JobChangeAdapter() {
          public void done(IJobChangeEvent event) {
            final UserInput userInput = getUserInputJob.getUserInput();
            if (userInput != null) {
              Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                  try {
                    membersTable.setInput(userInput);
                  } catch (Exception e) {
                    //EZLogger.logWarning("Unable to create UserInput for " + team.getName(), e);
                    logger.warn("Unable to create UserInput for " + team.getName(), e);
                  }
                }
              });
            }
          }
        });
        getUserInputJob.schedule();

      }
    }
  }

}
