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
package gov.pnnl.cat.discussion;

import gov.pnl.ui.utils.StatusUtil;
import gov.pnnl.cat.core.resources.IResourceManager;
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.core.util.ToolErrorHandler;
import gov.pnnl.velo.model.Comment;
import gov.pnnl.cat.discussion.views.DiscussionDialog;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.images.SharedImages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.FindReplaceAction;

/**
 */
public class DiscussionViewComposite extends Composite implements IDiscussionListener, IFindReplaceTarget {
  /**
   * expanding a section takes a decent amount of time. since they must be expanded in the UI thread, we should not expand a large number of sections initially.
   */
  private static final int MAX_SECTIONS_TO_EXPAND = 10;

  private List<IDiscussionListener> listenerList = Collections.synchronizedList(new ArrayList<IDiscussionListener>());

  private Composite blankComp;

  private Composite commentsComp;

  private StackLayout stackLayout;

  private FormToolkit toolkit = new FormToolkit(Display.getCurrent());

  private DiscussionDialog dialog;

  private Logger logger = CatLogger.getLogger(this.getClass());

  // Viewers document contains all the text from each Section in the Comments View
  private TextViewer hiddenFindReplaceViewer;

  private String allCommentsPostContents = "";

  private TextViewer currentCommentsTextViewer;

  private int yCoordinate = 0;

  // This array holds each StyledText so the size can be adjusted depending
  // on the size of the ScrolledComposite
  private ArrayList<StyledText> arrayStyledText = new ArrayList<StyledText>();

  // Contains the hiddenFindReplaceViewer to perform overall search of Comments
  private Composite invisibleComposite;

  private ArrayList<TextViewer> allCommentsSectionTextViewers = new ArrayList<TextViewer>();

  /**
   * Constructor for DiscussionViewComposite.
   * @param parent Composite
   * @param style int
   */
  public DiscussionViewComposite(Composite parent, int style) {
    super(parent, style);
    stackLayout = new StackLayout();
    setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    setLayout(stackLayout);

    // commentsComp = new Composite(this, SWT.NONE | SWT.WRAP);

    commentsComp = toolkit.createComposite(this, SWT.WRAP);
    commentsComp.setLayout(new GridLayout());
    commentsComp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

    setTopControl(blankComp);
    invisibleComposite = new Composite(parent, SWT.NONE);
    invisibleComposite.setVisible(false);

    hiddenFindReplaceViewer = new TextViewer(invisibleComposite, SWT.NONE);
    hiddenFindReplaceViewer.setDocument(new Document());
  }

  /**
   * Method createSections.
   * @param comments Comment[]
   */
  public void createSections(Comment[] comments) {
    setTopControl(commentsComp);

    try {
      int sectionCount = 0;
      Section[] sections = new Section[comments.length];

      for (Comment comment : comments) {
        sections[sectionCount++] = createSection(comment);
      }

      // expanding them all at once looks better as they load
      for (int i = 0; i < sections.length && i < MAX_SECTIONS_TO_EXPAND; i++) {
        sections[i].setExpanded(true);
      } 
    }catch (ResourceException e) {
      StatusUtil.handleStatus("An error occurred loading the comments.", e, StatusManager.SHOW);
    } catch (Throwable e) {
      StatusUtil.handleStatus("An unexpected error occurred rendering the comments.", e, StatusManager.SHOW);
    }


    layoutAll();
  }

  /**
   * Method createSection.
   * @param comment Comment
   * @return Section
   * @throws ResourceException
   * @throws ParseException
   */
  private Section createSection(final Comment comment) throws ResourceException, ParseException {
    final Section comments = toolkit.createSection(commentsComp, Section.TWISTIE | Section.TITLE_BAR | Section.COMPACT);
    final IResourceManager mgr = ResourcesPlugin.getResourceManager();

    comments.addControlListener(new ControlAdapter() {
      public void controlResized(ControlEvent event) {
        resizeLayout();
        layoutAll();
      }
    });
    StringBuilder header = new StringBuilder();

    comments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    Composite messageBody = toolkit.createComposite(comments, SWT.WRAP);
    messageBody.setLayout(new GridLayout());
    comments.setClient(messageBody);

    header.append(comment.getAuthor().getFirstName());
    header.append(" ");
    header.append(comment.getAuthor().getLastName());
    header.append(":  ");
    SimpleDateFormat convertTo = new SimpleDateFormat(DiscussionConstants.COMMON_DATE_FORMAT);
    Date myDate = comment.getCreatedOn();
    header.append(convertTo.format(myDate));
    comments.setText(header.toString());

    toolkit.adapt(comments);
    toolkit.adapt(messageBody);

    // A TextViewer is created for each comment section (Post)
    final TextViewer commentViewer = new TextViewer(messageBody, SWT.MULTI | SWT.WRAP);
    commentViewer.setEditable(false);
    commentViewer.setDocument(new Document(comment.getContent()));
    commentViewer.activatePlugins();

    // Add each commentViewer to an Array. Used for Find/Replace action
    allCommentsSectionTextViewers.add(commentViewer);

    // Get all comments content and append
    allCommentsPostContents += comment.getContent() + "\n";

    // Set the viewers document to contain all text from each comments section
    hiddenFindReplaceViewer.getDocument().set(allCommentsPostContents);

    final StyledText commentsBodyText = commentViewer.getTextWidget();
    commentsBodyText.setWordWrap(true);

    // Add each commentsBodyText to the array. This array is used in the resizeLayout() method
    arrayStyledText.add(commentsBodyText);

    // Setting the GridData of the StyledText makes sure the text wraps inside the ScrolledComposite
    commentsBodyText.setLayoutData(new GridData(Math.max(0, commentsComp.getBounds().width - 70), SWT.DEFAULT));

    // Add keyboard shortcut capability
    commentsBodyText.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        // Implementing the CTRL-A for the keyboard. Equivalent to menu function "Select All"
        if (e.character == 0x01) {
          commentsBodyText.selectAll();
        }
        // Implementing the CTRL-F for the keyboard. Equivalent to menu function "Find"
        if (e.character == 0x06) {
          setSelectedViewer(commentViewer);

          // Code to implement Eclipse Find/Replace
          // The resource bundle is the Discussion.properties file in gov.pnnl.cat.discussion.views
          ResourceBundle resourceBundle = ResourceBundle.getBundle("gov.pnnl.cat.discussion.views.Discussion");
          FindReplaceAction find = new FindReplaceAction(resourceBundle, "find_replace_action", getShell(), DiscussionViewComposite.this);
          find.run();
        }
      }

      public void keyReleased(KeyEvent e) {
      }
    });

    // Add right-click capability on commentViewers
    final Menu menu = new Menu(commentsBodyText);

    final MenuItem copyItem = new MenuItem(menu, SWT.PUSH);
    copyItem.setText("Copy");
    copyItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        commentsBodyText.copy();
      }
    });

    MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
    selectAllItem.setText("Select All");
    selectAllItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        commentsBodyText.selectAll();
      }
    });

    commentsBodyText.setMenu(menu);

    new MenuItem(menu, SWT.SEPARATOR);

    final MenuItem findItem = new MenuItem(menu, SWT.NONE);
    findItem.setText("Find...");
    findItem.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        setSelectedViewer(commentViewer);

        // Code to implement Eclipse Find/Replace
        // The resource bundle is the Discussion.properties file in gov.pnnl.cat.discussion.views
        ResourceBundle resourceBundle = ResourceBundle.getBundle("gov.pnnl.cat.discussion.views.Discussion");
        FindReplaceAction find = new FindReplaceAction(resourceBundle, "find_replace_action", getShell(), DiscussionViewComposite.this);
        find.run();
      }
    });

    commentsBodyText.addMouseListener(new MouseListener() {
      public void mouseDoubleClick(MouseEvent e) {
      }

      public void mouseDown(MouseEvent e) {
      }

      public void mouseUp(MouseEvent e) {
        // Only enable the COPY menu item if text is selected
        if (e.button == 3) {
          if (commentsBodyText.getSelectionText().trim().length() > 0) {
            copyItem.setEnabled(true);
          } else {
            copyItem.setEnabled(false);
          }
        }
      }

    });

    // This composite holds the Delete and Edit buttons inside each Comments header
    final Composite buttonsComposite = toolkit.createComposite(comments, SWT.NONE);
    final GridLayout gridLayouts = new GridLayout();

    gridLayouts.numColumns = 2;
    gridLayouts.verticalSpacing = 0;
    gridLayouts.marginHeight = 0;
    gridLayouts.marginWidth = 0;

    buttonsComposite.setLayout(gridLayouts);
    comments.setTextClient(buttonsComposite);

    toolkit.paintBordersFor(buttonsComposite);
    buttonsComposite.setBackground(comments.getTitleBarBackground());

    // Add Delete button to header
    final ImageHyperlink deleteComment = new ImageHyperlink(buttonsComposite, SWT.NONE);

    deleteComment.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_DELETE_COMMENT, SharedImages.CAT_IMG_SIZE_16));
    deleteComment.setBackground(comments.getTitleBarGradientBackground());
    deleteComment.setToolTipText("Delete Comment");

    deleteComment.addHyperlinkListener(new HyperlinkAdapter() {
      public void linkActivated(HyperlinkEvent e) {
        boolean delete = MessageDialog.openConfirm(getDisplay().getActiveShell(), "Confirm Delete", "Are you sure you want to delete this comment?");

        if (delete) {
          try {
            // delete comment from the server
            System.out.println(comment.getUuid());
            mgr.deleteResource(comment.getUuid());
            
            //post.getTopic().delete();
          } catch (ResourceException ex) {
            ToolErrorHandler.handleError("An error occurred deleting the comment.", ex, true);
          }
        }
      }
    });

    String modifier = comment.getAuthor().getUsername();
    String currentUser = ResourcesPlugin.getDefault().getSecurityManager().getUsername();
    // Only allow the delete comment to be visible if the current user is the user
    // who created the comment
    deleteComment.setVisible(modifier.equalsIgnoreCase(currentUser));

    // Add edit comment button the header
    final ImageHyperlink editComment = new ImageHyperlink(buttonsComposite, SWT.NONE);
    editComment.setImage(SharedImages.getInstance().getImage(SharedImages.CAT_IMG_EDIT_COMMENT, SharedImages.CAT_IMG_SIZE_16));
    editComment.setBackground(comments.getTitleBarGradientBackground());
    editComment.setToolTipText("Edit Comment");

    editComment.addHyperlinkListener(new HyperlinkAdapter() {
      public void linkActivated(HyperlinkEvent e) {
        // Open dialog with information.
        dialog = new DiscussionDialog(getDisplay().getActiveShell(), comment);
        dialog.open();
      }
    });

    stackLayout.topControl = commentsComp;
    return comments;
  }

  /**
   * Method setTopControl.
   * @param control Composite
   */
  private void setTopControl(Composite control) {
    stackLayout.topControl = control;
    layout();
  }

  public void doLayout() {
    layout();
  }

  /**
   * Method changeTopControl.
   * @param control int
   */
  public void changeTopControl(int control) {

    if (control == 0) {
      setTopControl(blankComp);
    }
    if (control == 1) {
      setTopControl(commentsComp);
    }
  }

  public void layoutAll() {
    commentsComp.layout();
    layout();
  }

  /**
   * Method resizeLayout.
   * @see gov.pnnl.cat.discussion.IDiscussionListener#resizeLayout()
   */
  public void resizeLayout() {
    // IDiscussionListener interface to call the DiscussionView to resize scrolled composite

    // For each StyledText component...resize the component to fit within the ScrolledComposite
    for (StyledText textBody : arrayStyledText) {
      textBody.setLayoutData(new GridData(Math.max(0, commentsComp.getBounds().width - 70), SWT.DEFAULT));
    }

    layoutAll();
    notifyListeners();
  }

  /**
   * Method addListener.
   * @param listener IDiscussionListener
   */
  public void addListener(IDiscussionListener listener) {
    listenerList.add(listener);
  }

  /**
   * Method removeListener.
   * @param listener IDiscussionListener
   */
  public void removeListener(IDiscussionListener listener) {
    listenerList.remove(listener);
  }

  private void notifyListeners() {
    List<IDiscussionListener> copyListenerList = new ArrayList<IDiscussionListener>(listenerList);

    for (IDiscussionListener currentListener : copyListenerList) {
      currentListener.resizeLayout();
    }
  }

  public void ExpandSections() {

  }

  // Sets the current TextViewer that the event occurred from
  /**
   * Method setSelectedViewer.
   * @param viewer TextViewer
   */
  public void setSelectedViewer(TextViewer viewer) {
    currentCommentsTextViewer = viewer;
  }

  /**
   * Method getCurrentViewer.
   * @return TextViewer
   */
  public TextViewer getCurrentViewer() {
    return currentCommentsTextViewer;
  }

  // This method sets the current location of the search string word within the view
  /**
   * Method setSearchStringPointLocation.
   * @param yCoord int
   */
  public void setSearchStringPointLocation(int yCoord) {
    yCoordinate = yCoord;
  }

  // Returns the current position of the string being searched
  /**
   * Method getSearchStringPointLocation.
   * @return int
   */
  public int getSearchStringPointLocation() {
    return yCoordinate;
  }

  /**
   * IFindReplaceTarget implementation
   * @return boolean
   * @see org.eclipse.jface.text.IFindReplaceTarget#canPerformFind()
   */
  public boolean canPerformFind() {
    return true;
  }

  /**
   * Method findAndSelect.
   * @param widgetOffset int
   * @param findString String
   * @param searchForward boolean
   * @param caseSensitive boolean
   * @param wholeWord boolean
   * @return int
   * @see org.eclipse.jface.text.IFindReplaceTarget#findAndSelect(int, String, boolean, boolean, boolean)
   */
  public int findAndSelect(int widgetOffset, String findString, boolean searchForward, boolean caseSensitive, boolean wholeWord) {
    // Find the location of the string being searched for in the hiddenFindReplaceViewer
    // since it contains all the commentViewers text
    int startingPosition = hiddenFindReplaceViewer.getFindReplaceTarget().findAndSelect(widgetOffset, findString, searchForward, caseSensitive, wholeWord);

    int start = 0;
    int end;

    // Reset the selection of text of the currentCommentsTextViewer to nothing. This eliminates
    // the highlighting of multiple strings in different viewers.
    currentCommentsTextViewer.getTextWidget().setSelectionRange(0, 0);

    // Perform search of desired string in each commentsViewer
    // If the string is found in the hiddenFindReplaceViewer, traverse each commentsView
    // to find starting point of string.
    for (TextViewer viewer : allCommentsSectionTextViewers) {
      currentCommentsTextViewer = viewer;
      end = start + currentCommentsTextViewer.getDocument().getLength();
      if (start <= startingPosition && startingPosition < end) {
        // If the starting position of the string is found inside the commentViewer,
        // calculate the offset of where the string would be relative to the commentViewer
        int offset = startingPosition - start;

        // While searching, if the section contains the string being searched and the section
        // is not expanded, expand the section.
        Section section = (Section) currentCommentsTextViewer.getControl().getParent().getParent();
        if (section.isExpanded() == false) {
          section.setExpanded(true);
        }

        //Set the caret position of the TextViewer to the starting location of the search string
        currentCommentsTextViewer.getTextWidget().setCaretOffset(offset);

        //Find the position of the TextViewer relative to the ScrolledComposite then add the y value of
        //the caret to center the search string inside of the ScrolledComposite.
        int yCoord = currentCommentsTextViewer.getControl().getParent().getParent().getLocation().y + currentCommentsTextViewer.getTextWidget().getCaret().getLocation().y;
        //Set the location of the search string.  This value is translated as pixels
        setSearchStringPointLocation(yCoord);
        resizeLayout();
        // return the string to the viewer
        return currentCommentsTextViewer.getFindReplaceTarget().findAndSelect(offset, findString, searchForward, caseSensitive, wholeWord);
      }

      // If the string wasn't found in within the range of text in the currentCommentsViewer
      // go to the next commentViewer
      start = end + 1;
    }
    // If string is not found at all return -1 (String not found).
    return -1;
  }

  /**
   * Method getSelection.
   * @return Point
   * @see org.eclipse.jface.text.IFindReplaceTarget#getSelection()
   */
  public Point getSelection() {
    // If a string occurs more than once in the hiddenFindReplace viewer document, find the next occurrence
    return hiddenFindReplaceViewer.getFindReplaceTarget().getSelection();
  }

  /**
   * Method getSelectionText.
   * @return String
   * @see org.eclipse.jface.text.IFindReplaceTarget#getSelectionText()
   */
  public String getSelectionText() {
    return currentCommentsTextViewer.getFindReplaceTarget().getSelectionText();
  }

  /**
   * Method isEditable.
   * @return boolean
   * @see org.eclipse.jface.text.IFindReplaceTarget#isEditable()
   */
  public boolean isEditable() {
    // Can't edit the comments directly in the viewer. User must use edit button.
    return false;
  }

  /**
   * Method replaceSelection.
   * @param text String
   * @see org.eclipse.jface.text.IFindReplaceTarget#replaceSelection(String)
   */
  public void replaceSelection(String text) {
    // Do nothing. Can't edit directly.
  }

}
