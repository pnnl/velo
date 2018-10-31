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
import gov.pnnl.cat.core.resources.ResourceException;
import gov.pnnl.cat.logging.CatLogger;
import gov.pnnl.cat.ui.rcp.util.RCPUtil;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 */
public class AdvancedPropertiesPage extends PropertyPage implements IWorkbenchPropertyPage {

  private Text typeText;
  private List list;
  private Text pathText;
  private IResource resource;
  private Logger logger = CatLogger.getLogger(getClass());

  public AdvancedPropertiesPage() {
    super();
  }

  /**
   * Method createContents.
   * @param parent Composite
   * @return Control
   */
  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);
    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    container.setLayout(gridLayout);

    resource = RCPUtil.getResource(getElement());

    final Label qualifiedPathLabel = new Label(container, SWT.NONE);
    qualifiedPathLabel.setText("Qualified Path:");

    pathText = new Text(container, SWT.BORDER);
    pathText.setEditable(false);
    pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    pathText.setText(resource.getPath().toPrefixString());

    final Label typeLabel = new Label(container, SWT.NONE);
    typeLabel.setText("Type:");

    typeText = new Text(container, SWT.BORDER);
    typeText.setEditable(false);
    typeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    typeText.setText(resource.getNodeType());

    final Label aspectsLabel = new Label(container, SWT.NONE);
    aspectsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    aspectsLabel.setText("Aspects:");

    final ListViewer listViewer = new ListViewer(container, SWT.BORDER);
    list = listViewer.getList();
    list.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    try {
      java.util.List<String> aspects = resource.getAspects();

      for (String aspect : aspects) {
        list.add(aspect);
      }
    } catch (ResourceException e) {
      logger.error(e);
    }

    //
    return container;
  }

}
