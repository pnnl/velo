package abc.containers;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.w3c.dom.Node;

import vabc.ABCConstants;
import vabc.ABCStyle;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.IABCUserObject;
import datamodel.DataItem;
import datamodel.Key;
import abc.table.ABCTable;

public class ABCSection extends ABCComponent {

	private static final long serialVersionUID = 1L;

	private ParallelGroup parallelGroup;
	private SequentialGroup sequentialGroup;
	
	public ABCSection(ABC abcParent, String key, String label, IABCUserObject abcUserObject) {
		super(abcParent, key, label, null, abcUserObject);		
		initializeLayout(this);
	}

	public ABCSection(ABC abcParent, String key, String label, DataItem identifier, Node node, IABCUserObject abcUserObject) {
		super(abcParent, key, label, identifier, abcUserObject);
		
		this.setBackground(ABCStyle.style().getBackgroundColor());
		
		boolean isCollapsible = false, collapsed=false;
		Node collapsibleAttr = node.getAttributes().getNamedItem(ABCConstants.Key.COLLAPSIBLE);
		if (collapsibleAttr != null && collapsibleAttr.getNodeValue().equals("true")) isCollapsible=true;
		Node shouldCollapseAttr = node.getAttributes().getNamedItem(ABCConstants.Key.COLLAPSED);
		if (shouldCollapseAttr != null && shouldCollapseAttr.getNodeValue().equals("true")) collapsed=true;
		if (isCollapsible) {
			CollapsibleGradientPanel p = new CollapsibleGradientPanel(label, null, null);	
			initializeLayout(p.getContent());
			this.setLayout(new BorderLayout());
			this.add(p);
			setBorder(null); // override superclass
			p.setBorder(null);
			p.getContent().setBorder(null);
			p.collapse(collapsed);
		} else {
			initializeLayout(this);
		}

		// Add this level of primitives				
		initialize(node, abcParent.dataProvider, abcParent.actionProvider);
	}
	
	private void initializeLayout(JPanel contents) {
		GroupLayout groupLayout = new GroupLayout(contents);
		contents.setLayout(groupLayout);
		parallelGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
		sequentialGroup = groupLayout.createSequentialGroup();
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGroup(parallelGroup)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(sequentialGroup));			
	}

	private void initialize(Node node, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

	//	Node keyNode = node.getAttributes().getNamedItem(ABCConstants.Key.KEY);
	//	if(keyNode != null && !dataProvider.shouldShow(node)) return;
	//	if (!dataProvider.shouldShow(node)) return;
		
		// Add this level of primitives
    ABCTable section = new ABCTable(ABCSection.this, node, dataProvider, actionProvider);
		addPrimitive(section);
		
		parallelGroup.addComponent(section, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);		
		// Resize everything except tables vertically
		sequentialGroup.addComponent(section, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);

	}

	@Override
	public void addComponentToUI(ABCComponent component) {
		// Max value allow for vertical resizing, preferred size keeps it the same		
		boolean resizeVertical = component instanceof ABCExpandedList || component instanceof ABCTabs;
		parallelGroup.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		sequentialGroup.addComponent(component, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, 
				resizeVertical ? Short.MAX_VALUE : GroupLayout.PREFERRED_SIZE);
	} 
	
	/**
	 * Override the superclass enable in order to disable the titled border too.
	 * Not sure if we want this but I thought it looked silly the other way.:
	 */
	@Override
	public boolean enable(Key key, boolean shouldEnable) {
	  if (this.identifier!= null && key != null && this.identifier.getKey().equals(key.key)) {
	    setEnabled(shouldEnable);
	    key = null; // Disable everything in this section
	  }
	  return super.enable(key,shouldEnable);
	}
	

  @Override
  public boolean isDynamic() {
    return false;
  }

	
}
