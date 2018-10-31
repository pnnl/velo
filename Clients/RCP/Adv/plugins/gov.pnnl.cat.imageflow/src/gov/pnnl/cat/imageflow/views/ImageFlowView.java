package gov.pnnl.cat.imageflow.views;

import gov.pnnl.cat.imageflow.jface.viewers.ImageFlowViewer;
import gov.pnnl.cat.ui.rcp.views.repositoryexplorer.SelectionProviderIntermediate;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * This is a test class that demonstrates how to use the ImageFlow component inside an Eclipse view.
 */
public class ImageFlowView extends ViewPart {

  /**
   * The ID of the view as specified by the extension.
   */
  public static final String ID = "gov.pnnl.cat.imageflow.views.ImageFlow";

  private ImageFlowViewer imageFlowViewer;

  /**
   * The constructor.
   */
  public ImageFlowView() {

  }

  /**
   * {@inheritDoc}
   * 
   * Initialize our view with embedded AWT frame that holds the ImageFlow component.
   */
  public void createPartControl(Composite parent) {

    // TODO instantiate ImageFlowViewer
    this.imageFlowViewer = new ImageFlowViewer(parent);

    getViewSite().getPage().addSelectionListener(imageFlowViewer);
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose() {
    this.getViewSite().getPage().removeSelectionListener(imageFlowViewer);
    super.dispose();
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus() {
    SelectionProviderIntermediate.getInstance().setSelectionProviderDelegate(imageFlowViewer);
  }
}