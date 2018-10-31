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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.statushandlers.StatusManager;

import com.swtdesigner.SWTResourceManager;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IFile;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.discussion.Activator;
import gov.pnnl.cat.discussion.DiscussionConstants;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.velo.model.Comment;


/**
 */
public class DiscussionDialog extends Dialog {
  
  // max size for the column in the database is 255, but the
  // qualifier is 43 characters which limits us to 212 for the name.
  private static final int MAX_SUBJECT_LENGTH = 212;

  private String comment = "";
  private String author = "";
  private String content = "";
  private String date = "";
  private Text contents;
  private Comment post;
  private boolean isNewComment = false;
  //private Text txtSubject;
  private IResource resource;
  private final static int MAX_LENGTH = 1024 * 1024 * 10; //10MB?
  private static Logger logger = CatLogger.getLogger(DiscussionDialog.class);
  
  /**
   * Constructor for DiscussionDialog.
   * @param parentShell Shell
   * @param post Comment
   */
  public DiscussionDialog(Shell parentShell, Comment post) {
    super(parentShell);
    setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
    
    try{
      isNewComment = false;
      this.post = post;
      this.comment = post.getName();
      this.author = post.getAuthor().getUsername();

      // look up the contents of the post and save that
      this.content = post.getContent();

      SimpleDateFormat convertTo = new SimpleDateFormat(DiscussionConstants.COMMON_DATE_FORMAT);
      Date myDate = post.getModifiedOn();
      this.date = convertTo.format(myDate);
    }
    catch(Exception e){
      logger.error(e);
    }
    
    // TODO Auto-generated constructor stub
  }
  
  /**
   * Constructor for DiscussionDialog.
   * @param parentShell Shell
   * @param resource IResource
   */
  public DiscussionDialog(Shell parentShell, IResource resource){
    super(parentShell);
    setShellStyle(SWT.MODELESS | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.CLOSE | SWT.BORDER | SWT.TITLE);
    this.resource = resource;
    isNewComment = true;
  }

  /**
   * Method createDialogArea.
   * @param parent Composite
   * @return Control
   */
  protected Control createDialogArea(Composite parent){
//    Composite container = new Composite(parent, SWT.NONE);
//    container.setLayout(new FillLayout());
//    container.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

    
    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2));
    
    final GridLayout gridLayout_1 = new GridLayout();
    gridLayout_1.numColumns = 2;
    composite.setLayout(gridLayout_1);

//    final Label commentLabel = new Label(composite, SWT.NONE);
//    commentLabel.setFont(SWTResourceManager.getFont("", 8, SWT.BOLD));
//    commentLabel.setText("Subject:");

    int style = SWT.NONE;
    
    if(isNewComment){
     style = SWT.BORDER;
    }
    
//    txtSubject = new Text(composite, style);
//    txtSubject.setEditable(isNewComment);
//    txtSubject.setTextLimit(MAX_SUBJECT_LENGTH);
//    
//    final GridData gd_lblComment = new GridData(SWT.FILL, SWT.CENTER, true, false);
//    txtSubject.setLayoutData(gd_lblComment);
//    txtSubject.setText(comment);
//    
//
//    txtSubject.addModifyListener(new ModifyListener(){
//      public void modifyText(ModifyEvent e) {
//        getButton(IDialogConstants.OK_ID).setEnabled(txtSubject.getText().trim().length() !=0);
//      }
//    });
    
    if(!isNewComment){
      final Label authorLabel = new Label(composite, SWT.NONE);
      authorLabel.setFont(SWTResourceManager.getFont("", 8, SWT.BOLD));
      authorLabel.setText("Author:");

      final Text authorText = new Text(composite, SWT.NONE);
      authorText.setLayoutData(new GridData());
      authorText.setText(author);
      authorText.setEditable(false);

      final Label dateLabel = new Label(composite, SWT.NONE);
      dateLabel.setFont(SWTResourceManager.getFont("", 8, SWT.BOLD));
      dateLabel.setText("Last Modified:");

      final Text dateText = new Text(composite, SWT.NONE);
      dateText.setLayoutData(new GridData());
      dateText.setText(date);
      dateText.setEditable(false);
    }

    contents = new Text(composite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.H_SCROLL);
    final GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    gd_text.widthHint = 0;
    gd_text.heightHint = 225;
    gd_text.minimumHeight = 225;
    contents.setLayoutData(gd_text);
    contents.setText(content.replace("<br/>", "\n"));
    
    contents.addModifyListener(new ModifyListener(){
      public void modifyText(ModifyEvent e) {
        getButton(IDialogConstants.OK_ID).setEnabled(contents.getText().trim().length() !=0);
      }
    });
    
    if(!isNewComment){
      contents.setFocus();
    }
    return composite;
  }
  
  /**
   * Method getTitleText.
   * @return String
   */
  static public String getTitleText()
  {
    return "Comment Details";
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#getInitialSize()
   */
  protected Point getInitialSize() {
    return new Point(400, 400);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  protected void configureShell(Shell newShell)
  {
    super.configureShell(newShell);
    newShell.setText(getTitleText());
  }
  
  /**
   * Method createButtonBar.
   * @param parent Composite
   * @return Control
   */
  protected Control createButtonBar(Composite parent) {
    Control buttonBar = super.createButtonBar(parent);

    if(isNewComment){
      getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
    return buttonBar;
  }
  
  protected void okPressed(){
    try {
      if(!isNewComment){
        // TODO: update comment's contents
//        SetContentJob setContentJob = new SetContentJob(post, contents.getText());
//        setContentJob.setPriority(Job.SHORT);
//        setContentJob.setUser(true);
//        setContentJob.schedule();
        close();
      
      } else{
        //String subject = txtSubject.getText().trim();
        String subject = "Comments";

        //verify user input
        if(contents.getText().trim().length() == 0){
          return;
        }
        
        // create the comment
        ResourcesPlugin.getResourceManager().addComment(resource.getPath(), contents.getText().trim());
        close();
      }
    } catch (Exception e) {
      StatusUtil.handleStatus("An error has occurred saving the comment.", e, StatusManager.SHOW);
      close();
    }
  }
  
  /**
   */
  private class SetContentJob extends Job{
    private IFile post;
    private String contents;
    
    /**
     * Constructor for SetContentJob.
     * @param post IFile
     * @param contents String
     */
    public SetContentJob(IFile post, String contents){
      super("Setting Content");
      this.post = post;
      this.contents = contents;
    }
    
    /**
     * Method run.
     * @param monitor IProgressMonitor
     * @return IStatus
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
      
      try{
        post.setContent(contents);
        
        return Status.OK_STATUS;
      }
      catch(ResourceException e){
        logger.error(e);
        return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Could not set content.", e);
      }
    }
  }
}
