package gov.pnnl.velo.tools.ui.abc.panels;

import javax.swing.JLabel;

import datamodel.Key;
import gov.pnnl.velo.tools.ui.abc.ToolUIABCDefault;

public class TempToolPanel extends AbstractToolPanel {

	private static final long serialVersionUID = 1L;

	public TempToolPanel(String alias) {
		super(new Key(alias));
	}

	@Override
	public void initializePanel(ToolUIABCDefault tool) {
		add(new JLabel("Place holder"));
	}

	@Override
	public boolean isComplete() {
		return true;
	}

	@Override
	public boolean hasErrors() {
		return false;
	}
	
	

}
