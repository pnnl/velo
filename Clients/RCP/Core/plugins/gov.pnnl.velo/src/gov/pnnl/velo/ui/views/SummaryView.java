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
package gov.pnnl.velo.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.eclipse.albireo.core.SizeEvent;
import org.eclipse.albireo.core.SizeListener;
import org.eclipse.albireo.core.SwingControl;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.views.UpdatingResourceView;
import gov.pnnl.velo.ui.util.FormLayoutFactory;

/**
 * Summary View describing resources
 * @author D3K339
 *
 * @version $Revision: 1.0 $
 */
public class SummaryView extends UpdatingResourceView {

  public static final String ID = SummaryView.class.getName();
  public static final String EXTENSION_POINT = "gov.pnnl.velo.summaryViewSectionFactory";
  public static final String ATTRIBUTE = "class";
  private FormToolkit toolkit;
  private ScrolledForm form;

  public static final String DATE_PATTERN = "MM/dd/yyyy";
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);

  private List<Section> sections;
  private List<SummaryViewSectionProvider> sectionExensionProviders;

  /**
   * Default constructor
   */
  public SummaryView() {
    sectionExensionProviders = new ArrayList<SummaryViewSectionProvider>();
    try {
      IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT);

      for (IConfigurationElement configurationElement : elements) {
        Object obj = configurationElement.createExecutableExtension(ATTRIBUTE);
        if(obj instanceof SummaryViewSectionProvider) {
          SummaryViewSectionProvider ext = (SummaryViewSectionProvider)obj;
          sectionExensionProviders.add(ext);
        }
      }
    } catch (Throwable e) {
      logger.error("Unable to load summary view extension points.", e);
    }
    
    // now sort
    Collections.sort(sectionExensionProviders);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl(Composite parent) {
    super.createPartControl(parent);
    this.sections = new ArrayList<Section>();
    this.parent = parent;
    this.toolkit = new FormToolkit(Display.getCurrent());
    this.form = toolkit.createScrolledForm(parent);
    //form.setText("Resource Summary");
    ColumnLayout layout = new ColumnLayout();
    layout.topMargin = 0;
    layout.bottomMargin = 5;
    layout.leftMargin = 10;
    layout.rightMargin = 10;
    layout.horizontalSpacing = 10;
    layout.verticalSpacing = 10;
    layout.maxNumColumns = 4;
    layout.minNumColumns = 1;
    form.getBody().setLayout(layout);

  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    // TODO Auto-generated method stub

  }

  /**
   * Method dispose.
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    toolkit.dispose();
    super.dispose();
  }

  /**
  
   * @return the toolkit */
  public FormToolkit getToolkit() {
    return toolkit;
  }

  /**
   * Method createSWTSection.
   * @param title String
   * @param desc String
   * @param numColumns int
   * @return Composite
   */
  public Composite createSWTSection(String title, String desc, int numColumns) {
    Section section = toolkit.createSection(form.getBody(), Section.TWISTIE | Section.SHORT_TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED);
    section.setText(title);
    section.setDescription(desc);
    Composite client = toolkit.createComposite(section);
    GridLayout layout = new GridLayout();
    layout.marginWidth = layout.marginHeight = 0;
    layout.numColumns = numColumns;
    client.setLayout(layout);
    section.setClient(client);
    section.addExpansionListener(new ExpansionAdapter() {
      public void expansionStateChanged(ExpansionEvent e) {
        form.reflow(false);
      }
    });
    // add the newly created section to our list so that we can dispose of it when the image needs to change
    sections.add(section);

    return client;
  }

  @Override
  protected void drawView(IResource resource) {

    // Wipe out old data
    for (Section section : sections) {
      section.dispose();
    }
    sections.clear();

    if(resource != null) {
      // Set the title
      //form.setText("Summary of " + resource.getName());
      setPartName("Summary: " + resource.getName());

      // now add sections for all contributed extension points
      for(SummaryViewSectionProvider sectionProvider : sectionExensionProviders) {
        sectionProvider.createSummarySection(this, resource);
      }
    } else {
      setPartName("Summary:");
    }
    // refresh the view
    form.reflow(true);
  }

  /**
   * Method createSwingSection.
   * @param title String
   * @param panelToEmbed JPanel
   */
  public void createSwingSection(String title, final JPanel panelToEmbed) {
    // Create the section    
    final Composite swingSection = createSWTSection(title, "", 1);

    // Set the panel's background color to match the typical viewer background color
    final Color bgColor = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND);

    final SwingControl control = new SwingControl(swingSection, SWT.NONE) {

      protected JComponent createSwingComponent() {
        panelToEmbed.setBackground(new java.awt.Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue()));
        return panelToEmbed;
      }
      public Composite getLayoutAncestor() {
        return swingSection;
      }
    };
    GridLayout gridLayout = FormLayoutFactory.createClearGridLayout(true, 1);
    control.setLayout(gridLayout);
    control.setLayoutData (new GridData (SWT.FILL, SWT.FILL, true, true));

    control.addSizeListener(new SizeListener() {

      /**
       * This is called when AWT thread finally sizes the component (after SWT thread lays out the
       * composite the first time)
       * @see org.eclipse.albireo.core.SizeListener#preferredSizeChanged(org.eclipse.albireo.core.SizeEvent)
       */
      @Override
      public void preferredSizeChanged(SizeEvent event) {
        //System.out.println(event.preferred); // AWT size
        control.setSize(event.preferred); // make Eclipse embedded control the same size
        // refresh the view so control is drawn with new size
        form.reflow(true);
        parent.layout(true, true);
      }
    });

  }

  /**
   * Method addPropertySection.
   * @param composite Composite
   * @param resource IResource
   * @param fullyQualifiedPropName String
   * @param propDisplayName String
   * @param font Font
   */
  public void addPropertySection(Composite composite, IResource resource, String fullyQualifiedPropName, String propDisplayName, Font font) {

    String param = resource.getPropertyAsString(fullyQualifiedPropName);
    if(param != null) {
      FormLayoutFactory.createPropertyName(toolkit, composite, propDisplayName, font);
      FormLayoutFactory.createPropertyValue(toolkit, composite, param);
      FormLayoutFactory.createSpacerRow(toolkit, composite);      
    }     
  }
  
  /**
   * Method scaleImage.
   * @param sourceImageData ImageData
   * @param maxWidth int
   * @param maxHeight int
   * @return ImageData
   */
  public ImageData scaleImage(ImageData sourceImageData, int maxWidth, int maxHeight) {
    ImageData imgData = sourceImageData;

    if (imgData.width > maxWidth) {
      double scale = (double) imgData.width / maxWidth;

      int newWidth = maxWidth;
      int newHeight = (int) (imgData.height / scale);

      imgData = imgData.scaledTo(newWidth, newHeight);
    }

    if (imgData.height > maxHeight) {
      double scale = (double) imgData.height / maxHeight;

      int newHeight = maxHeight;
      int newWidth = (int) (imgData.width / scale);

      imgData = imgData.scaledTo(newWidth, newHeight);
    }

    return imgData;
  }
}
