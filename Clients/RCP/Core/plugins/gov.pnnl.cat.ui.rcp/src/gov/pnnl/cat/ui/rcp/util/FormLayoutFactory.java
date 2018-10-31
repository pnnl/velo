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
package gov.pnnl.cat.ui.rcp.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 */
public class FormLayoutFactory {

  // Used in place of 0. If 0 is used, widget borders will appear clipped
  // on some platforms (e.g. Windows XP Classic Theme).
  // Form tool kit requires parent composites containing the widget to have
  // at least 1 pixel border margins in order to paint the flat borders.
  // The form toolkit paints flat borders on a given widget when native
  // borders are not painted by SWT. See FormToolkit#paintBordersFor()
  public static final int DEFAULT_CLEAR_MARGIN = 2;

  // Required to allow space for field decorations
  public static final int CONTROL_HORIZONTAL_INDENT = 3;

  // UI Forms Standards

  // FORM BODY
  public static final int FORM_BODY_MARGIN_TOP = 12;

  public static final int FORM_BODY_MARGIN_BOTTOM = 12;

  public static final int FORM_BODY_MARGIN_LEFT = 6;

  public static final int FORM_BODY_MARGIN_RIGHT = 6;

  public static final int FORM_BODY_HORIZONTAL_SPACING = 20;

  // Should be 20; but, we minus 3 because the section automatically pads the
  // bottom margin by that amount
  public static final int FORM_BODY_VERTICAL_SPACING = 17;

  public static final int FORM_BODY_MARGIN_HEIGHT = 0;

  public static final int FORM_BODY_MARGIN_WIDTH = 0;

  // SECTION CLIENT
  public static final int SECTION_CLIENT_MARGIN_TOP = 5;

  public static final int SECTION_CLIENT_MARGIN_BOTTOM = 5;

  // Should be 6; but, we minus 4 because the section automatically pads the
  // left margin by that amount
  public static final int SECTION_CLIENT_MARGIN_LEFT = 2;

  // Should be 6; but, we minus 4 because the section automatically pads the
  // right margin by that amount
  public static final int SECTION_CLIENT_MARGIN_RIGHT = 2;

  public static final int SECTION_CLIENT_HORIZONTAL_SPACING = 5;

  public static final int SECTION_CLIENT_VERTICAL_SPACING = 5;

  public static final int SECTION_CLIENT_MARGIN_HEIGHT = 0;

  public static final int SECTION_CLIENT_MARGIN_WIDTH = 0;

  public static final int SECTION_HEADER_VERTICAL_SPACING = 6;

  // CLEAR
  public static final int CLEAR_MARGIN_TOP = DEFAULT_CLEAR_MARGIN;

  public static final int CLEAR_MARGIN_BOTTOM = DEFAULT_CLEAR_MARGIN;

  public static final int CLEAR_MARGIN_LEFT = DEFAULT_CLEAR_MARGIN;

  public static final int CLEAR_MARGIN_RIGHT = DEFAULT_CLEAR_MARGIN;

  public static final int CLEAR_HORIZONTAL_SPACING = 0;

  public static final int CLEAR_VERTICAL_SPACING = 0;

  public static final int CLEAR_MARGIN_HEIGHT = 0;

  public static final int CLEAR_MARGIN_WIDTH = 0;

  // FORM PANE
  public static final int FORM_PANE_MARGIN_TOP = 0;

  public static final int FORM_PANE_MARGIN_BOTTOM = 0;

  public static final int FORM_PANE_MARGIN_LEFT = 0;

  public static final int FORM_PANE_MARGIN_RIGHT = 0;

  public static final int FORM_PANE_HORIZONTAL_SPACING = FORM_BODY_HORIZONTAL_SPACING;

  public static final int FORM_PANE_VERTICAL_SPACING = FORM_BODY_VERTICAL_SPACING;

  public static final int FORM_PANE_MARGIN_HEIGHT = 0;

  public static final int FORM_PANE_MARGIN_WIDTH = 0;

  // MASTER DETAILS
  public static final int MASTER_DETAILS_MARGIN_TOP = 0;

  public static final int MASTER_DETAILS_MARGIN_BOTTOM = 0;

  // Used only by masters part. Details part margin dynamically calculated
  public static final int MASTER_DETAILS_MARGIN_LEFT = 0;

  // Used only by details part. Masters part margin dynamically calcualated
  public static final int MASTER_DETAILS_MARGIN_RIGHT = 1;

  public static final int MASTER_DETAILS_HORIZONTAL_SPACING = FORM_BODY_HORIZONTAL_SPACING;

  public static final int MASTER_DETAILS_VERTICAL_SPACING = FORM_BODY_VERTICAL_SPACING;

  public static final int MASTER_DETAILS_MARGIN_HEIGHT = 0;

  public static final int MASTER_DETAILS_MARGIN_WIDTH = 0;

  /**
   * 
   */
  private FormLayoutFactory() {
    // NO-OP
  }

  /**
   * For form bodies.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createFormGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = FORM_BODY_MARGIN_HEIGHT;
    layout.marginWidth = FORM_BODY_MARGIN_WIDTH;

    layout.marginTop = FORM_BODY_MARGIN_TOP;
    layout.marginBottom = FORM_BODY_MARGIN_BOTTOM;
    layout.marginLeft = FORM_BODY_MARGIN_LEFT;
    layout.marginRight = FORM_BODY_MARGIN_RIGHT;

    layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }
  

  /**
   * For multiple columns within another grid cell
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createInnerFormGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = 0;
    layout.marginWidth = 0;

    layout.marginTop = 0;
    layout.marginBottom = 0;
    layout.marginLeft = 0;
    layout.marginRight = 0;

    layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For miscellaneous grouping composites. For sections (as a whole - header plus client).
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createClearGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = CLEAR_MARGIN_HEIGHT;
    layout.marginWidth = CLEAR_MARGIN_WIDTH;

    layout.marginTop = CLEAR_MARGIN_TOP;
    layout.marginBottom = CLEAR_MARGIN_BOTTOM;
    layout.marginLeft = CLEAR_MARGIN_LEFT;
    layout.marginRight = CLEAR_MARGIN_RIGHT;

    layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
    layout.verticalSpacing = CLEAR_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For form bodies.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return TableWrapLayout
   */
  public static TableWrapLayout createFormTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
    TableWrapLayout layout = new TableWrapLayout();

    layout.topMargin = FORM_BODY_MARGIN_TOP;
    layout.bottomMargin = FORM_BODY_MARGIN_BOTTOM;
    layout.leftMargin = FORM_BODY_MARGIN_LEFT;
    layout.rightMargin = FORM_BODY_MARGIN_RIGHT;

    layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For composites used to group sections in left and right panes.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return TableWrapLayout
   */
  public static TableWrapLayout createFormPaneTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
    TableWrapLayout layout = new TableWrapLayout();

    layout.topMargin = FORM_PANE_MARGIN_TOP;
    layout.bottomMargin = FORM_PANE_MARGIN_BOTTOM;
    layout.leftMargin = FORM_PANE_MARGIN_LEFT;
    layout.rightMargin = FORM_PANE_MARGIN_RIGHT;

    layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For composites used to group sections in left and right panes.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createFormPaneGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = FORM_PANE_MARGIN_HEIGHT;
    layout.marginWidth = FORM_PANE_MARGIN_WIDTH;

    layout.marginTop = FORM_PANE_MARGIN_TOP;
    layout.marginBottom = FORM_PANE_MARGIN_BOTTOM;
    layout.marginLeft = FORM_PANE_MARGIN_LEFT;
    layout.marginRight = FORM_PANE_MARGIN_RIGHT;

    layout.horizontalSpacing = FORM_PANE_HORIZONTAL_SPACING;
    layout.verticalSpacing = FORM_PANE_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For miscellaneous grouping composites. For sections (as a whole - header plus client).
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return TableWrapLayout
   */
  public static TableWrapLayout createClearTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
    TableWrapLayout layout = new TableWrapLayout();

    layout.topMargin = CLEAR_MARGIN_TOP;
    layout.bottomMargin = CLEAR_MARGIN_BOTTOM;
    layout.leftMargin = CLEAR_MARGIN_LEFT;
    layout.rightMargin = CLEAR_MARGIN_RIGHT;

    layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
    layout.verticalSpacing = CLEAR_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For master sections belonging to a master details block.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createMasterGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = MASTER_DETAILS_MARGIN_HEIGHT;
    layout.marginWidth = MASTER_DETAILS_MARGIN_WIDTH;

    layout.marginTop = MASTER_DETAILS_MARGIN_TOP;
    layout.marginBottom = MASTER_DETAILS_MARGIN_BOTTOM;
    layout.marginLeft = MASTER_DETAILS_MARGIN_LEFT;
    // Cannot set layout on a sash form.
    // In order to replicate the horizontal spacing between sections,
    // divide the amount by 2 and set the master section right margin to
    // half the amount and set the left details section margin to half
    // the amount. The default sash width is currently set at 3.
    // Minus 1 pixel from each half. Use the 1 left over pixel to separate
    // the details section from the vertical scollbar.
    int marginRight = MASTER_DETAILS_HORIZONTAL_SPACING;
    if (marginRight > 0) {
      marginRight = marginRight / 2;
      if (marginRight > 0) {
        marginRight--;
      }
    }
    layout.marginRight = marginRight;

    layout.horizontalSpacing = MASTER_DETAILS_HORIZONTAL_SPACING;
    layout.verticalSpacing = MASTER_DETAILS_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For details sections belonging to a master details block.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createDetailsGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = MASTER_DETAILS_MARGIN_HEIGHT;
    layout.marginWidth = MASTER_DETAILS_MARGIN_WIDTH;

    layout.marginTop = MASTER_DETAILS_MARGIN_TOP;
    layout.marginBottom = MASTER_DETAILS_MARGIN_BOTTOM;
    // Cannot set layout on a sash form.
    // In order to replicate the horizontal spacing between sections,
    // divide the amount by 2 and set the master section right margin to
    // half the amount and set the left details section margin to half
    // the amount. The default sash width is currently set at 3.
    // Minus 1 pixel from each half. Use the 1 left over pixel to separate
    // the details section from the vertical scollbar.
    int marginLeft = MASTER_DETAILS_HORIZONTAL_SPACING;
    if (marginLeft > 0) {
      marginLeft = marginLeft / 2;
      if (marginLeft > 0) {
        marginLeft--;
      }
    }
    layout.marginLeft = marginLeft;
    layout.marginRight = MASTER_DETAILS_MARGIN_RIGHT;

    layout.horizontalSpacing = MASTER_DETAILS_HORIZONTAL_SPACING;
    layout.verticalSpacing = MASTER_DETAILS_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For composites set as section clients. For composites containg form text.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return GridLayout
   */
  public static GridLayout createSectionClientGridLayout(boolean makeColumnsEqualWidth, int numColumns) {
    GridLayout layout = new GridLayout();

    layout.marginHeight = SECTION_CLIENT_MARGIN_HEIGHT;
    layout.marginWidth = SECTION_CLIENT_MARGIN_WIDTH;

    layout.marginTop = SECTION_CLIENT_MARGIN_TOP;
    layout.marginBottom = SECTION_CLIENT_MARGIN_BOTTOM;
    layout.marginLeft = SECTION_CLIENT_MARGIN_LEFT;
    layout.marginRight = SECTION_CLIENT_MARGIN_RIGHT;

    layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
    layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * For composites set as section clients. For composites containg form text.
   * 
   * @param makeColumnsEqualWidth
   * @param numColumns
  
   * @return TableWrapLayout
   */
  public static TableWrapLayout createSectionClientTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns) {
    TableWrapLayout layout = new TableWrapLayout();

    layout.topMargin = SECTION_CLIENT_MARGIN_TOP;
    layout.bottomMargin = SECTION_CLIENT_MARGIN_BOTTOM;
    layout.leftMargin = SECTION_CLIENT_MARGIN_LEFT;
    layout.rightMargin = SECTION_CLIENT_MARGIN_RIGHT;

    layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
    layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;

    layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
    layout.numColumns = numColumns;

    return layout;
  }

  /**
   * Debug method.
   * 
   * MAGENTA = 11 CYAN = 13 GREEN = 5
   * 
   * @param container
   * @param color
   */
  public static void visualizeLayoutArea(Composite container, int color) {
    container.setBackground(Display.getCurrent().getSystemColor(color));
  }

  /**
   * Method createSection.
   * @param form ScrolledForm
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param sectionName String
   * @param numColumns int
   * @param horizontalSpan int
   * @param expanded boolean
   * @return Composite
   */
  public static Composite createSection(final ScrolledForm form, FormToolkit toolkit, Composite composite, String sectionName, int numColumns, int horizontalSpan, boolean expanded) {
    return createSection(form, toolkit, composite, sectionName, numColumns, horizontalSpan, Section.TWISTIE | Section.TITLE_BAR, expanded);
  }

  // Layout helper methods
  /**
   * Method createSection.
   * @param form ScrolledForm
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param sectionName String
   * @param numColumns int
   * @param horizontalSpan int
   * @param style int
   * @param expanded boolean
   * @return Composite
   */
  public static Composite createSection(final ScrolledForm form, FormToolkit toolkit, Composite composite, String sectionName, int numColumns, int horizontalSpan, int style, boolean expanded) {
    Section section = createSectionSection(form, toolkit, composite, sectionName, numColumns, horizontalSpan, style, expanded);
    return createSectionBody(section, toolkit, numColumns);
  }

  // Layout helper methods
  /**
   * Method createSectionSection.
   * @param form ScrolledForm
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param sectionName String
   * @param numColumns int
   * @param horizontalSpan int
   * @param style int
   * @param expanded boolean
   * @return Section
   */
  public static Section createSectionSection(final ScrolledForm form, FormToolkit toolkit, Composite composite, String sectionName, 
      int numColumns, int horizontalSpan, int style, boolean expanded) {
    Section section;
    if (expanded) {
      section = toolkit.createSection(composite, style | Section.EXPANDED);
    } else {
      section = toolkit.createSection(composite, style | Section.TITLE_BAR);
    }

    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        form.reflow(true);
      }
    });

    section.setText(sectionName);

    GridLayout layout = FormLayoutFactory.createClearGridLayout(true, numColumns);
    section.setLayout(layout);
    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
    layoutData.horizontalSpan = horizontalSpan;
    section.setLayoutData(layoutData);

    return section;
  }


  /**
   * Method createSectionBody.
   * @param section Section
   * @param toolkit FormToolkit
   * @param numColumns int
   * @return Composite
   */
  public static Composite createSectionBody(Section section, FormToolkit toolkit, int numColumns){
    Composite sectionBody = toolkit.createComposite(section, SWT.NONE);
    section.setClient(sectionBody);
    GridLayout bodyLayout = FormLayoutFactory.createClearGridLayout(false, numColumns);
    sectionBody.setLayout(bodyLayout);
    sectionBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    return sectionBody;
  }

  /**
   * Method createZeroMarginSection.
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param sectionName String
   * @param numColumns int
   * @param horizontalSpan int
   * @param expanded boolean
   * @return Composite
   */
  public static Composite createZeroMarginSection(FormToolkit toolkit, Composite composite, String sectionName, int numColumns, int horizontalSpan, boolean expanded) {
    Section section;
    if (expanded) {
      section = toolkit.createSection(composite, Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
    } else {
      section = toolkit.createSection(composite, Section.TWISTIE | Section.TITLE_BAR);
    }

    section.setText(sectionName);
    GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
    layoutData.horizontalSpan = horizontalSpan;
    section.setLayoutData(layoutData);

    Composite sectionBody = toolkit.createComposite(section, SWT.NONE);
    section.setClient(sectionBody);
    GridLayout sectionLayout = new GridLayout();
    sectionLayout.numColumns = numColumns;
    sectionLayout.marginTop = 0;
    sectionLayout.marginRight = 0;
    sectionLayout.marginLeft = 0;
    sectionLayout.horizontalSpacing = 10;
    sectionLayout.marginBottom = 0;
    sectionBody.setLayout(sectionLayout);
    sectionBody.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    return sectionBody;
  }

  /**
   * Method createSpanComposite.
   * @param parent Composite
   * @param numColumns int
   * @param span int
   * @return Composite
   */
  public static Composite createSpanComposite(Composite parent, int numColumns, int span) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout sectionLayout = new GridLayout();
    sectionLayout.numColumns = numColumns;
    sectionLayout.marginTop = 0;
    sectionLayout.marginRight = 0;
    sectionLayout.marginLeft = 0;
    sectionLayout.marginBottom = 0;
    sectionLayout.horizontalSpacing = 5;
    sectionLayout.verticalSpacing = 0;
    composite.setLayout(sectionLayout);
    GridData layoutData = new GridData(SWT.LEFT, SWT.BEGINNING, false, true);
    layoutData.horizontalSpan = span;
    composite.setLayoutData(layoutData);
    return composite;
  }

  /**
   * Method createIndentComposite.
   * @param parent Composite
   * @param numColumns int
   * @param span int
   * @return Composite
   */
  public static Composite createIndentComposite(Composite parent, int numColumns, int span) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout sectionLayout = new GridLayout();
    sectionLayout.numColumns = numColumns;
    sectionLayout.marginTop = 0;
    sectionLayout.marginBottom = 0;
    sectionLayout.marginRight = 0;
    sectionLayout.marginLeft = 20;
    sectionLayout.horizontalSpacing = 5;
    sectionLayout.verticalSpacing = 5;
    composite.setLayout(sectionLayout);
    GridData layoutData = new GridData(SWT.LEFT, SWT.BEGINNING, false, true);
    layoutData.horizontalSpan = span;
    composite.setLayoutData(layoutData);
    composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    return composite;
  }

  /**
   * Get the host for shorter web link labels
   * 
   * @param httpUrl
  
   * @return String
   */
  public static String getHost(String httpUrl) {
    String host = "";

    try {
      URL url = new URL(httpUrl);
      host = url.getHost();

    } catch (MalformedURLException e) {
      e.printStackTrace();
      host = e.toString();
    }
    return host;

  }

  /**
   * Method create2ColRowComposite.
   * @param toolkit FormToolkit
   * @param parent Composite
   * @return Composite
   */
  public static Composite create2ColRowComposite(FormToolkit toolkit, Composite parent) {
    // make a composite to put the link and image in:
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout sectionLayout = new GridLayout();
    sectionLayout.numColumns = 2;
    sectionLayout.marginTop = 2;
    sectionLayout.marginRight = 0;
    sectionLayout.marginLeft = 0;
    sectionLayout.marginBottom = 0;
    sectionLayout.horizontalSpacing = 5;
    sectionLayout.marginHeight = 0;
    sectionLayout.marginWidth = 0;
    sectionLayout.verticalSpacing = 0;
    composite.setLayout(sectionLayout);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    composite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

    return composite;
  }

  /**
   * Method createPropertyName.
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param propName String
   * @param boldFont Font
   */
  public static void createPropertyName(FormToolkit toolkit, Composite composite, String propName, Font boldFont) {
    Label label = toolkit.createLabel(composite, propName + ": ");
    label.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
    label.setFont(boldFont);
    label.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
    // label.setForeground(toolkit.getColors().getColor(IFormColors.H_HOVER_LIGHT));

    // Another way to do a label, but not bold
    // StyledText text = new StyledText(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.READ_ONLY | SWT.WRAP);
    // text.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
    // text.setText(propName + ": ");
    // toolkit.adapt(text, true, true);

    // This is supposed to work, but it does not render bold font :(
    // FormText formText = toolkit.createFormText(composite, false);
    // formText.setWhitespaceNormalized(true);
    // formText.setText("<b>" + propName + ":</b>", true, false);
    // formText.setLayoutData(new GridData(SWT.LEFT, SWT.BEGINNING, false, false));
  }

  /**
   * Method createPropertyValue.
   * @param toolkit FormToolkit
   * @param composite Composite
   * @param propValue String
   * @return Text
   */
  public static Text createPropertyValue(FormToolkit toolkit, final Composite composite, String propValue) {
    final Text label = new Text(composite, SWT.NONE | SWT.WRAP);
    if(propValue != null) {
      label.setText(propValue);
    }
    label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
    label.setEditable(false);
    return label;
    // This code doesn't work very well
    // This is making the value seem excessively wide and messing up formatting
    // final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    // // don't wrap lines less than 20 characters long
    // if(propValue.length() > 20) {
    // gridData.widthHint = 120;
    // }
    // label.setLayoutData(gridData);
    //
    // composite.addControlListener(new ControlAdapter() {
    //
    // public void controlResized(ControlEvent e) {
    // Rectangle area = composite.getClientArea();
    // gridData.widthHint = area.width;
    // label.setLayoutData(gridData);
    // }
    // });

  }

  /**
   * Method createSpacerRow.
   * @param toolkit FormToolkit
   * @param composite Composite
   */
  public static void createSpacerRow(FormToolkit toolkit, Composite composite) {
    GridData layoutData = new GridData(SWT.LEFT, SWT.BEGINNING, false, false);
    layoutData.heightHint = 3;
    Label label = toolkit.createLabel(composite, "");
    label.setLayoutData(layoutData);
    Label label2 = toolkit.createLabel(composite, "");
    label2.setLayoutData(layoutData);
  }
}
