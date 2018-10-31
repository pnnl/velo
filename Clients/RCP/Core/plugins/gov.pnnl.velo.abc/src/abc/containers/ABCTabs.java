package abc.containers;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;

import vabc.ABCStyle;
import vabc.IABCUserObject;


public class ABCTabs extends ABCComponent {

	private static final long serialVersionUID = 1L;
	
	JTabbedPane jTabbedPane;
	
	public ABCTabs(ABC abcParent, String key, String label, IABCUserObject abcUserObject) {
		super(abcParent, key, label, null, abcUserObject);
		setLayout(new BorderLayout());
		setBackground(ABCStyle.style().getBackgroundColor());
		setBorder(null);
		jTabbedPane = new JTabbedPane();
		jTabbedPane.setBorder(null);
		jTabbedPane.setBackground(ABCStyle.style().getBackgroundColor());
		add(jTabbedPane);		
	}
	
	@Override
	public void addComponentToUI(ABCComponent component) {
		if(component.isVisible()) {
			component.setBorder(null); 	// Remove border when in tabs
			jTabbedPane.addTab(component.getLabel(), component);
		}
	}

  @Override
  public boolean isDynamic() {
    return false;
  }

}
