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
package gov.pnnl.cat.search.advanced.pages;

import gov.pnnl.cat.core.resources.IMimetypeManager;
import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.search.advanced.AdvancedSearchExtensions;
import gov.pnnl.cat.search.advanced.AdvancedSearchOptions;
import gov.pnnl.cat.ui.rcp.viewers.ResourceContainerCheckedTreeViewer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FileFolderSorter;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.treeexplorer.FolderFilter;
import gov.pnnl.velo.model.CmsPath;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.javawiki.calendar.CalendarDialog;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

/**
 */
public class AdvancedSearchComposite extends Composite {

  protected static Logger logger = CatLogger.getLogger(AdvancedSearchComposite.class);
  
  private FormToolkit toolkit = new FormToolkit(Display.getCurrent());

  Button btnCreatedDate;

  private Text txtCreatedDateTo;

  private Text txtCreatedDateFrom;

  Button btnModifiedDate;

  private Text txtModifiedDateTo;

  private Text txtModifiedDateFrom;

  private Text txtAuthor;

  private Text txtTitle;

  private Text txtDescription;

  private Combo txtBasicSearch;
  
  private ResourceContainerCheckedTreeViewer checkboxTreeViewer;

  private Combo comboFileFormat;

  private Tree tree;

  private Composite sectionClient;

  private Composite sectionLookinClient;

  private Composite sectionMoreOptsClient;

  private ArrayList<String> mimetypeNames;

  private SimpleDateFormat sdf;

  IMimetypeManager mimetypeMgr;

  private GridData textFieldGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);

  private boolean searchAllLocations = true;
  
  private AdvancedSearchExtensions advExt = null;
  
  /**
   * Create the composite
   * 
   * @param parent
   * @param style
   * @param advExt AdvancedSearchExtensions
   */
  public AdvancedSearchComposite(Composite parent, int style, AdvancedSearchExtensions advExt) {
    super(parent, style);
    this.advExt = advExt;
    
    setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    final GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    setLayout(gridLayout);
    toolkit.paintBordersFor(parent);
    createLookForSection();
    createLookinSection();
    createMoreOptionsSection();

  }

  public void createLookForSection() {
    // "Look For" section
    final Section sectionLookfor = toolkit.createSection(this, Section.DESCRIPTION | Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
    sectionLookfor.setText("Look For:");
    // Make sure the grid expands to fill section
    final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    sectionLookfor.setLayoutData(gridData);
    sectionClient = toolkit.createComposite(sectionLookfor, SWT.NONE);
    toolkit.paintBordersFor(sectionClient);
    GridLayout lookForGridLayout = new GridLayout(2, false);
    lookForGridLayout.marginWidth = 0;
    lookForGridLayout.marginHeight = 0;
    lookForGridLayout.marginRight = 0;
    sectionClient.setLayout(lookForGridLayout);

    // Create simple search text box
    createBasicSearchPart();
    
    // create blank line in grid
    toolkit.createLabel(sectionClient, "");
    final Label label = toolkit.createLabel(sectionClient, "");
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    // Add sectionClient composite to Section sectionLookfor
    sectionLookfor.setClient(sectionClient);

  }

  public void createBasicSearchPart() {
    // Create simple search textbox
    toolkit.createLabel(sectionClient, "");
    txtBasicSearch = new Combo(sectionClient, SWT.SINGLE | SWT.BORDER);
    txtBasicSearch.setLayoutData(textFieldGridData);
  }
  
  public void createFindMatchesInPart() {
    // create "find matches in:" label and set correct font for display
    Label lblFindMatchesIn = toolkit.createLabel(sectionClient, "Find matches in:");
    FontData fontData = new FontData();
    fontData.setStyle(SWT.BOLD);
    fontData.setHeight(8);
    Font font = new Font(Display.getCurrent(), fontData);
    lblFindMatchesIn.setFont(font);
    // an empty label to fill the space
    final Label label = toolkit.createLabel(sectionClient, "");
    label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
  }

  public void createLookinSection() {
    /*
     * final Section sectionLookfor = toolkit.createSection(this, Section.DESCRIPTION|Section.TWISTIE|Section.TITLE_BAR|Section.EXPANDED); sectionLookfor.setText("Look For:"); //Make sure the grid expands to fill section sectionLookfor.setLayoutData(new GridData(GridData.FILL_BOTH)); sectionClient = toolkit.createComposite(sectionLookfor);
     */
    // "Look in" section
    Section sectionLookin = toolkit.createSection(this, Section.DESCRIPTION | Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
    sectionLookin.setText("Look in:");
    // Make sure the grid expands to fill section
    sectionLookin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    sectionLookinClient = toolkit.createComposite(sectionLookin);
    toolkit.paintBordersFor(sectionLookinClient);
    GridLayout lookInGridLayout = new GridLayout();
    lookInGridLayout.numColumns = 2;
    sectionLookinClient.setLayout(lookInGridLayout);

    // Create "All locations" radio button
    createAllLocationsPart();

    // Create "Specifiy" radio button
    createSpecifiyLocationsPart();

    sectionLookin.setClient(sectionLookinClient);

    // create tree and checkboxTreeViewer
    createTreeAndCheckboxTreeViewerPart();

  }

  public void createAllLocationsPart() {
    GridData gd = new GridData();
    gd.horizontalSpan = 2;
    // Create "All locations" radio button
    final Button btnAllLocations = toolkit.createButton(sectionLookinClient, "All locations", SWT.RADIO);
    btnAllLocations.setLayoutData(gd);

    btnAllLocations.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        if (btnAllLocations.getSelection()) {
          checkboxTreeViewer.getControl().setEnabled(false);
          searchAllLocations = true;
        }
      }

      public void widgetDefaultSelected(SelectionEvent event) {
      }
    });

    // Make "All locations" radio button default selection
    btnAllLocations.setSelection(true);
  }

  public void createSpecifiyLocationsPart() {
    GridData gd1 = new GridData();
    gd1.horizontalSpan = 2;
    // Create "Specifiy" radio button
    final Button btnSpecify = toolkit.createButton(sectionLookinClient, "Specify:", SWT.RADIO);
    btnSpecify.setLayoutData(gd1);

    btnSpecify.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        if (btnSpecify.getSelection()) {
          checkboxTreeViewer.getControl().setEnabled(true);
          searchAllLocations = false;
          // no longer doing this here, instead set the input right away so that we can keep the user's selection
          // in case they switch back and forth between all locaitons and specify
          // checkboxTreeViewer.setInput(ResourcesPlugin.getResourceManager());
        }
      }

      public void widgetDefaultSelected(SelectionEvent event) {
      }
    });
  }

  public void createTreeAndCheckboxTreeViewerPart() {
    // create tree and checkboxTreeViewer
    checkboxTreeViewer = new ResourceContainerCheckedTreeViewer(false, sectionLookinClient, SWT.CHECK | SWT.BORDER);
    tree = checkboxTreeViewer.getTree();
    tree.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
    
    checkboxTreeViewer.setSorter(new FileFolderSorter());
    checkboxTreeViewer.addFilter(new FolderFilter());
    checkboxTreeViewer.setInput(ResourcesPlugin.getResourceManager().getRoot());
    // by default it's disabled until "specify" radio button is selected:
    checkboxTreeViewer.getControl().setEnabled(false);
    
    //Create a white background image to be displayed behind the ContainerTree
    PaletteData palette = new PaletteData(new RGB[]{new RGB(255,255,255)});
    checkboxTreeViewer.getControl().setBackgroundImage(new Image(getDisplay(), new ImageData(1,1,8,palette)));
    
    toolkit.adapt(tree, true, true);
    final GridData gridData_1 = new GridData(GridData.FILL_BOTH);

    // gridData_1.heightHint = 100;
    tree.setLayoutData(gridData_1);
  }

  public void createMoreOptionsSection() {
    // "More Options" section
    Section sectionMoreOpts = toolkit.createSection(this, Section.DESCRIPTION | Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
    sectionMoreOpts.setText("More Options:");
    // Make sure the grid expands to fill section
    sectionMoreOpts.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    sectionMoreOptsClient = toolkit.createComposite(sectionMoreOpts);
    toolkit.paintBordersFor(sectionMoreOptsClient);
    GridLayout moreOptionsGridLayout = new GridLayout();
    moreOptionsGridLayout.numColumns = 2;
    sectionMoreOptsClient.setLayout(moreOptionsGridLayout);

    // create mimetypes
    createMimetypesPart();

    // Create "File format" label and dropdown list
    createFileFormatPart();

    // create "title" section and textbox
    createTitlePart();

    // create "author" section and textbox
    createAuthorPart();

    // create "description" section and textbox
    createDescriptionPart();

    sectionMoreOpts.setClient(sectionMoreOptsClient);

    // create "Modified Date:" checkbox
    createModifiedDatePart();
    // a kludge if not using GridData: empty label to make up for the row
    // toolkit.createLabel(sectionMoreOptsClient, "");

    // TODO: DATE_PATTERN should be global so class AdvanceSearchQuery and others can use it
    final String DATE_PATTERN = "MM/dd/yyyy"; // "yyyy-MM-dd";
    sdf = new SimpleDateFormat(DATE_PATTERN);

    // create "From" section for modified date
    createModifiedDateFromPart();

    // create "To" section for modified date
    createModifiedDateToPart();

    // create "Created Date:" checkbox
    createCreatedDatePart();

    // create "From" section for created date
    createCreatedDateFromPart();

    // create "To" section for created date
    createCreatedDateToPart();

    
    for (AdvancedSearchOptions advOption :  advExt.getAdvOptionsExtensions()) {
      advOption.createPart(sectionMoreOptsClient, toolkit);
    }
  }

  public void createFileFormatPart() {
    // Create "File format" label and dropdown list
    toolkit.createLabel(sectionMoreOptsClient, "File format:");
    comboFileFormat = new Combo(sectionMoreOptsClient, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
    comboFileFormat.setLayoutData(textFieldGridData);

    // get mimetypes
    comboFileFormat.add("");
    comboFileFormat.add("All Formats");
    Collections.sort(mimetypeNames);
    for (int i = 0; i < mimetypeNames.size(); i++) {
      comboFileFormat.add(mimetypeNames.get(i));
    }
  }

  public void createMimetypesPart() {
    // Carina will provide a web service for the MimeType and maps
    mimetypeMgr = ResourcesPlugin.getMimetypeManager();

    List<String> mimetypes = mimetypeMgr.getMimetypes();
    Map<String, String> displayNames = mimetypeMgr.getDisplaysByMimetype();
    String displayName;
    mimetypeNames = new ArrayList<String>(10);

    for (String mimetype : mimetypes) {
      displayName = displayNames.get(mimetype);
      if (displayName != null) {
        mimetypeNames.add(displayName);
      }
      // else { //we agree not to add no-displayname ones
      // mimetypeNames.add(mimetype);
      // }
    }
  }

  public void createTitlePart() {
    // create "title" section and textbox
    toolkit.createLabel(sectionMoreOptsClient, "Title:");
    txtTitle = toolkit.createText(sectionMoreOptsClient, "", SWT.BORDER);
    txtTitle.setLayoutData(textFieldGridData);
  }

  public void createAuthorPart() {
    // create "author" section and textbox
    toolkit.createLabel(sectionMoreOptsClient, "Author:");
    txtAuthor = toolkit.createText(sectionMoreOptsClient, "", SWT.BORDER);
    txtAuthor.setLayoutData(textFieldGridData);
  }

  public void createDescriptionPart() {
    // create "description" section and textbox
    toolkit.createLabel(sectionMoreOptsClient, "Description:");
    txtDescription = toolkit.createText(sectionMoreOptsClient, "", SWT.BORDER);
    txtDescription.setLayoutData(textFieldGridData);
  }

  public void createModifiedDatePart() {
    // create "Modified Date:" checkbox

    btnModifiedDate = toolkit.createButton(sectionMoreOptsClient, "Modified Date:", SWT.CHECK);
    GridData gd3 = new GridData();
    gd3.horizontalSpan = 2;
    btnModifiedDate.setLayoutData(gd3);

    btnModifiedDate.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        if (btnModifiedDate.getSelection()) {
          txtModifiedDateFrom.setEnabled(true);
          txtModifiedDateTo.setEnabled(true);
        } else {
          txtModifiedDateFrom.setEnabled(false);
          txtModifiedDateTo.setEnabled(false);
        }
      }

      public void widgetDefaultSelected(SelectionEvent event) {

      }
    });
  }

  public void createModifiedDateFromPart() {
    // create "From" section for modified date
    toolkit.createLabel(sectionMoreOptsClient, "      From:");
    txtModifiedDateFrom = toolkit.createText(sectionMoreOptsClient, sdf.format(new Date()), SWT.LEFT | SWT.BORDER);
    txtModifiedDateFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    txtModifiedDateFrom.setEditable(false);
//    txtModifiedDateFrom.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
    txtModifiedDateFrom.setEnabled(false);

    final SWTCalendarListener dateChangedListenerMF = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        txtModifiedDateFrom.setText(sdf.format(event.getCalendar().getTime()));
      }
    };
    txtModifiedDateFrom.addListener(SWT.MouseUp, new Listener() {
      public void handleEvent(Event event) {
        try {
          CalendarDialog dialog = new CalendarDialog(getShell());
          dialog.setDate(sdf.parse(txtModifiedDateFrom.getText()));
          dialog.addDateChangedListener(dateChangedListenerMF);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(getShell(), "Format-Error", "Couldn't parse date.");
        }

      }
    });
  }

  public void createModifiedDateToPart() {
    // create "To" section for modified date

    toolkit.createLabel(sectionMoreOptsClient, "      To:");

    txtModifiedDateTo = toolkit.createText(sectionMoreOptsClient, sdf.format(new Date()), SWT.LEFT | SWT.BORDER);
    txtModifiedDateTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    txtModifiedDateTo.setEditable(false);
//    txtModifiedDateTo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
    txtModifiedDateTo.setEnabled(false);
    final SWTCalendarListener dateChangedListener = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        txtModifiedDateTo.setText(sdf.format(event.getCalendar().getTime()));
      }
    };

    txtModifiedDateTo.addListener(SWT.MouseUp, new Listener() {
      public void handleEvent(Event event) {
        try {
          CalendarDialog dialog = new CalendarDialog(getShell());
          dialog.setDate(sdf.parse(txtModifiedDateTo.getText()));
          dialog.addDateChangedListener(dateChangedListener);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(getShell(), "Format-Error", "Couldn't parse date.");
        }

      }
    });
  }

  public void createCreatedDatePart() {
    // create "Created Date:" checkbox
    btnCreatedDate = toolkit.createButton(sectionMoreOptsClient, "Created Date:", SWT.CHECK);
    GridData gd4 = new GridData();
    gd4.horizontalSpan = 2;
    btnCreatedDate.setLayoutData(gd4);

    btnCreatedDate.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent event) {
        if (btnCreatedDate.getSelection()) {
          txtCreatedDateFrom.setEnabled(true);
          txtCreatedDateTo.setEnabled(true);
        } else {
          txtCreatedDateFrom.setEnabled(false);
          txtCreatedDateTo.setEnabled(false);
        }
      }

      public void widgetDefaultSelected(SelectionEvent event) {

      }
    });
  }

  public void createCreatedDateFromPart() {
    // create "From" section for created date

    toolkit.createLabel(sectionMoreOptsClient, "      From:");
    txtCreatedDateFrom = toolkit.createText(sectionMoreOptsClient, sdf.format(new Date()), SWT.LEFT);
    txtCreatedDateFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    txtCreatedDateFrom.setEditable(false);
//    txtCreatedDateFrom.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
    txtCreatedDateFrom.setEnabled(false);
    final SWTCalendarListener dateChangedListenerCF = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        txtCreatedDateFrom.setText(sdf.format(event.getCalendar().getTime()));
      }
    };
    txtCreatedDateFrom.addListener(SWT.MouseUp, new Listener() {
      public void handleEvent(Event event) {
        try {
          CalendarDialog dialog = new CalendarDialog(getShell());
          dialog.setDate(sdf.parse(txtCreatedDateFrom.getText()));
          dialog.addDateChangedListener(dateChangedListenerCF);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(getShell(), "Format-Error", "Couldn't parse date.");
        }
      }
    });
  }

  public void createCreatedDateToPart() {
    // create "To" section for created date

    toolkit.createLabel(sectionMoreOptsClient, "      To:");
    txtCreatedDateTo = toolkit.createText(sectionMoreOptsClient, sdf.format(new Date()), SWT.LEFT);
    txtCreatedDateTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    txtCreatedDateTo.setEditable(false);
//    txtCreatedDateTo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
    txtCreatedDateTo.setEnabled(false);
    final SWTCalendarListener dateChangedListenerCT = new SWTCalendarListener() {
      public void dateChanged(SWTCalendarEvent event) {
        txtCreatedDateTo.setText(sdf.format(event.getCalendar().getTime()));
      }
    };
    txtCreatedDateTo.addListener(SWT.MouseUp, new Listener() {
      public void handleEvent(Event event) {
        try {
          CalendarDialog dialog = new CalendarDialog(getShell());
          dialog.setDate(sdf.parse(txtCreatedDateTo.getText()));
          dialog.addDateChangedListener(dateChangedListenerCT);
          dialog.open();
        } catch (ParseException e1) {
          MessageDialog.openError(getShell(), "Format-Error", "Couldn't parse date.");
        }
      }
    });
  }

  public void dispose() {
    super.dispose();
  }

  protected void checkSubclass() {
    // Disable the check that prevents subclassing of SWT components
  }

  /**
   * Method getText.
   * @return String
   */
  public String getText() {
    return txtBasicSearch.getText();
  }
  
  /**
   * Method getAuthor.
   * @return String
   */
  public String getAuthor() {
    // return this.firstNameCombo.getText();
    return txtAuthor.getText();
  }

  /**
   * Method getDescription.
   * @return String
   */
  public String getDescription() {
    // return this.firstNameCombo.getText();
    return txtDescription.getText();
  }

  /**
   * Method getTitle.
   * @return String
   */
  public String getTitle() {
    return txtTitle.getText();
  }

  /**
   * Method modifiedDateSpecified.
   * @return boolean
   */
  public boolean modifiedDateSpecified() {
    //System.out.println("Modified?" + btnModifiedDate.getSelection());
    logger.debug("Modified?" + btnModifiedDate.getSelection());
    return btnModifiedDate.getSelection();
  }

  /**
   * Method createdDateSpecified.
   * @return boolean
   */
  public boolean createdDateSpecified() {
    //System.out.println("Created selected?" + btnCreatedDate.getSelection());
    logger.debug("Created selected?" + btnCreatedDate.getSelection());
    return btnCreatedDate.getSelection();
  }

  /**
   * Method getModifiedDateFrom.
   * @return String
   */
  public String getModifiedDateFrom() {
    return txtModifiedDateFrom.getText();
  }

  /**
   * Method getModifiedDateTo.
   * @return String
   */
  public String getModifiedDateTo() {
    return txtModifiedDateTo.getText();
  }

  /**
   * Method getCreatedDateFrom.
   * @return String
   */
  public String getCreatedDateFrom() {
    return txtCreatedDateFrom.getText();
  }

  /**
   * Method getCreatedDateTo.
   * @return String
   */
  public String getCreatedDateTo() {
    return txtCreatedDateTo.getText();
  }

  /**
   * Method getMimeType.
   * @return String
   */
  public String getMimeType() {
    String mimetypeDisplay = comboFileFormat.getText();
    Map<String, String> mimeTypes = mimetypeMgr.getMimetypesByDisplay();
    String mimetype = mimeTypes.get(mimetypeDisplay);
    if (mimetype == null || mimetype.length() == 0) {
      mimetype = mimetypeDisplay;
    }
    return mimetype;

  }

  /**
   * This method returns the most concise list of Check box tree viewer checked items For example, 1) User Documents - checked but grayed out Alex - checked Curt - checked Zoe - not checked It returns an array of two: /User Documents/Alex, /User Documents/Curt
   * 
   * 2) User Documents - checked Alex - checked Curt - checked Zoe - checked It means that all subfolders under /User Documents are checked It returns any array of one: /User Documents
   * 
  
   * @return CmsPath[]
   */
  public CmsPath[] getCheckedPaths() {
    Object[] allCheckedPaths = checkboxTreeViewer.getCheckedElements();
    ArrayList<CmsPath> paths = new ArrayList<CmsPath>();
    for (Object obj : allCheckedPaths) {
      if (obj instanceof IResource) {
        IResource resource = (IResource) obj;
        if (!checkboxTreeViewer.getGrayed(obj)) {
          CmsPath path = resource.getPath();
          if (paths.size() == 0) // no path there yet
          {
            paths.add(path);
          } else {
            for (CmsPath path2 : paths) // if parent path is already there, do not add
            {
              if (!path2.isPrefixOf(path)) {
                paths.add(path);
                break;
              }
            }
          }
        }
      }
    }

    CmsPath[] reducedCheckedPaths = paths.toArray(new CmsPath[paths.size()]);
    return reducedCheckedPaths;
  }
  
  /**
   * Method isSearchAllLocations.
   * @return boolean
   */
  public boolean isSearchAllLocations(){
    return searchAllLocations;
  }
  
  /**
   * Method getTextCombo.
   * @return Combo
   */
  public Combo getTextCombo() {
    return this.txtBasicSearch;
  }
}
