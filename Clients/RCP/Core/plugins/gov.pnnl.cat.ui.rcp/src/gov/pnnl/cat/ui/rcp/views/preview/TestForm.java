package gov.pnnl.cat.ui.rcp.views.preview;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;

public class TestForm extends Composite {

  private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

  /**
   * Create the composite.
   * @param parent
   * @param style
   */
  public TestForm(Composite parent, int style) {
    super(parent, style);
    addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        toolkit.dispose();
      }
    });
    toolkit.adapt(this);
    toolkit.paintBordersFor(this);
    
    Label lblMachine = new Label(this, SWT.NONE);
    lblMachine.setBounds(0, 10, 55, 15);
    toolkit.adapt(lblMachine, true, true);
    lblMachine.setText("Machine:");

  }
}
