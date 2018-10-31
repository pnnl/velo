package gov.pnnl.velo.tools.ui.abc;
import java.awt.Component;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import datamodel.Key;
import vabc.ABCStyle;


public class ProcessTreeView extends DefaultSingleCDockable implements Iterable<DefaultSingleCDockable> {

	// Maps the nodes in the tree to the dockable panels
	private Map<Object, DefaultSingleCDockable> linkedPanels;

	private JTree tree;

	public ProcessTreeView(ProcessTree processTree) {
		super("Process", "Process"); // Set the id and the title
		
		linkedPanels = new LinkedHashMap<Object, DefaultSingleCDockable>();		
		
		initializeTree(processTree);

		getContentPane().add(new JScrollPane(tree));
	}

	@Override
	public Iterator<DefaultSingleCDockable> iterator() {
		return linkedPanels.values().iterator();
	}

	public void selectFirst() {
		Object first = linkedPanels.get(linkedPanels.keySet().toArray()[0]);
		select(first);
	}

	private void expandAll() {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	private void select(Object object) {
		int row = 0;
		while (row < tree.getRowCount()) {
			TreePath path = tree.getPathForRow(row);
			for(Object node: linkedPanels.keySet()) {
				if(path.getLastPathComponent().equals(node) && linkedPanels.get(node).equals(object)) {
					System.out.println("Selecting: " + linkedPanels.get(node).getTitleText());
					linkedPanels.get(node).toFront();
					tree.setSelectionPath(path);	
					break;
				}
			}
			row++;
		}
	}
	
	private void initializeTree(ProcessTree map) {		
		tree = new JTree();
		tree.setRootVisible(false);
		tree.setBorder(new EmptyBorder(5, 5, 5, 5)); 
		
		for(Key key: map.keySet()) {
			System.out.println(key);
			ProcessTree child = map.get(key);
			for(Key key2: child.keySet()) {
				System.out.println("\t" + key2);
			}
		}
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultTreeModel model = new DefaultTreeModel(root);
		buildChildren(model, root, map);
		tree.setModel(model);
		
		addTreeSelectionListener();
		addCellRenderer();
		expandAll(); 
	}

	private void buildChildren(DefaultTreeModel model, DefaultMutableTreeNode root, ProcessTree map) {

		for(Key key: map.keySet()) {
			ProcessTree child = map.get(key);
			if(child.getDockable() != null) {
				DefaultMutableTreeNode group = new DefaultMutableTreeNode(child);
				addDockableFocusListener(child.getDockable());
				linkedPanels.put(group, child.getDockable());	
				root.add(group);								
			}
			DefaultMutableTreeNode group = new DefaultMutableTreeNode(key);
			buildChildren(model, group, child);
			if(!group.isLeaf())
				root.add(group);
			
		}
	}

	private void addCellRenderer() {
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,    
					boolean expanded, boolean leaf, int row, boolean hasFocus) {

				ABCStyle style = ABCStyle.style();
				Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				component.setForeground(leaf ? style.getTreeLeafColor() : style.getTreeGroupColor());
				// Background color not working?
				setBackground(sel ? ABCStyle.style().getFieldBorderColor(): style.getBackgroundColor());
				component.setBackground(sel ? ABCStyle.style().getFieldBorderColor() : style.getBackgroundColor());
				component.setFont(style.getDefaultFont());
				((DefaultTreeCellRenderer)component).setBackgroundSelectionColor(ABCStyle.style().getSelectionColor());
				setIcon(null);
				return component;
			}
		});
	}
	
	private void addTreeSelectionListener() {
		tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultSingleCDockable dockable = linkedPanels.get(e.getPath().getLastPathComponent());				
				if(dockable != null) { // Bring this panel to the front
					dockable.toFront();
				}
			}
		});
	}

	private void addDockableFocusListener(DefaultSingleCDockable dockable) {
		dockable.addFocusListener(new CFocusListener() {
			@Override
			public void focusGained(CDockable arg0) {						
				select(((DefaultSingleCDockable)arg0)); // Make sure it's selected in the tree
			}
			@Override
			public void focusLost(CDockable arg0) { }					
		});
	}	
}
