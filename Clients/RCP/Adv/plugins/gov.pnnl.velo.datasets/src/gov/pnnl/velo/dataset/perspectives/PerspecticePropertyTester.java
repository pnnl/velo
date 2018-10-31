package gov.pnnl.velo.dataset.perspectives;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

public class PerspecticePropertyTester extends PropertyTester {

  @Override
  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    IPerspectiveDescriptor perspective = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    String perspectiveId = perspective.getId();
    if (perspectiveId.equalsIgnoreCase(DatasetEditing.ID)) {
      return true;
    }
    return false;
  }
}