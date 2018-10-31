package gov.pnnl.velo.tools.ui.abc.example;

import gov.pnnl.velo.tools.adapters.ToolAdapter;
import gov.pnnl.velo.tools.ui.ToolUIFactory;
import gov.pnnl.velo.tools.ui.adapters.ToolUIFactorySwing;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class TestTool extends ToolAdapter {
  private ToolUIFactorySwing uiFactory = new ToolUIFactorySwing();
    
  public TestTool() {
    uiFactory.setUiClass(ToolUIExample.class.getName());
  }

  @Override
  public ToolUIFactory getUiFactory() {
    return uiFactory;
  }

  @Override
  public String getName() {
    return "Test Tool";
  }

  @Override
  public String getTooltipText() {
    return "Run a test tool.";
  }

  /* (non-Javadoc)
   * @see gov.pnnl.velo.tools.Tool#getToolDescription()
   */
  @Override
  public String getToolDescription() {
    return "this is a test";
  }

  @Override
  public String getMimetype() {
    return "tool/test";
  }

  @Override
  public ImageDescriptor getImageDescriptor() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Image getImage() {
    // TODO Auto-generated method stub
    return null;
  }

}
