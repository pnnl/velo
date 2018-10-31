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
package gov.pnnl.cat.ui.rcp.properties;

import gov.pnnl.cat.core.resources.IResource;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class SamplePropertyPage extends PropertyPage {

  private static final String PATH_TITLE = "Path:";

  private static final String OWNER_TITLE = "&Owner:";

  private static final String OWNER_PROPERTY = "OWNER";

  private static final String DEFAULT_OWNER = "John Doe";

  private static final int TEXT_FIELD_WIDTH = 50;

  private Text ownerText;

  /**
   * Constructor for SamplePropertyPage.
   */
  public SamplePropertyPage() {
    super();
    System.out.println("SamplePropertyPage Constructor");
  }

  /**
   * Method addFirstSection.
   * @param parent Composite
   */
  private void addFirstSection(Composite parent) {
    Composite composite = createDefaultComposite(parent);

    // Label for path field
    Label pathLabel = new Label(composite, SWT.NONE);
    pathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    pathLabel.setText(PATH_TITLE);

    // Path text field
    // Text pathValueText = new Text(composite, SWT.WRAP | SWT.READ_ONLY);
    // IResource resource = RCPUtil.getResource(getElement());
    //    
    // pathValueText.setText(resource.getPath().toString());
    IResource resource = RCPUtil.getResource(getElement());
    Composite textComp = new Composite(composite, SWT.NONE);
    // textComp.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));
    GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
    gd.heightHint = 60;
    textComp.setLayoutData(gd);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    textComp.setLayout(gridLayout);
    Text text = new Text(textComp, SWT.MULTI | SWT.BORDER);
    gd = new GridData(GridData.FILL_BOTH);
    text.setLayoutData(gd);
    text.setText(resource.getName());
  }

  /**
   * Method addSeparator.
   * @param parent Composite
   */
  private void addSeparator(Composite parent) {
    Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    GridData gridData = new GridData();
    gridData.horizontalAlignment = GridData.FILL;
    gridData.grabExcessHorizontalSpace = true;
    separator.setLayoutData(gridData);
  }

  /**
   * Method addSecondSection.
   * @param parent Composite
   */
  private void addSecondSection(Composite parent) {
    Composite composite = createDefaultComposite(parent);

    // Label for owner field
    Label ownerLabel = new Label(composite, SWT.NONE);
    ownerLabel.setText(OWNER_TITLE);

    // Owner text field
    ownerText = new Text(composite, SWT.SINGLE | SWT.BORDER);
    GridData gd = new GridData();
    gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
    ownerText.setLayoutData(gd);

    // Populate owner text field
    IResource resource = RCPUtil.getResource(getElement());

    String owner = resource.getName();
    ownerText.setText((owner != null) ? owner : DEFAULT_OWNER);

  }

  /**
   * @see PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    composite.setLayout(layout);
    GridData data = new GridData(GridData.FILL);
    data.grabExcessHorizontalSpace = true;
    composite.setLayoutData(data);

    addFirstSection(composite);
    addSeparator(composite);
    addSecondSection(composite);
    return composite;
  }

  /**
   * Method createDefaultComposite.
   * @param parent Composite
   * @return Composite
   */
  private Composite createDefaultComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NULL);
    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    composite.setLayout(layout);

    GridData data = new GridData();
    data.verticalAlignment = GridData.FILL;
    data.horizontalAlignment = GridData.FILL;
    composite.setLayoutData(data);

    return composite;
  }

  protected void performDefaults() {
    // Populate the owner text field with the default value
    ownerText.setText(DEFAULT_OWNER);
  }

  /**
   * Method performOk.
   * @return boolean
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    // // store the value in the owner text field
    // try {
    // ((CatItemNode) getElement()).setPersistentProperty(
    // new QualifiedName("", OWNER_PROPERTY),
    // ownerText.getText());
    // } catch (CoreException e) {
    // return false;
    // }
    // return true;
    System.out.println("ok pressed on sample property page!!!");
    return true;
  }

}
