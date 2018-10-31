package gov.pnnl.velo.tools.ui;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public class ToolUIUtils {

  public static void centerEclipseWindow(Shell shell) {

    Monitor primary = shell.getDisplay().getPrimaryMonitor ();
    Rectangle bounds = primary.getBounds ();
    Rectangle rect = shell.getBounds ();
    int x = bounds.x + (bounds.width - rect.width) / 2;
    int y = bounds.y + (bounds.height - rect.height) / 2;
    shell.setLocation (x, y);
  }
}
