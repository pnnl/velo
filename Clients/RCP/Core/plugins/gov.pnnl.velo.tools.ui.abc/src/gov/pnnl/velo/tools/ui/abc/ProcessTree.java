package gov.pnnl.velo.tools.ui.abc;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import datamodel.Key;
import gov.pnnl.velo.tools.ui.abc.panels.ABCToolPanel;
import gov.pnnl.velo.tools.ui.abc.panels.AbstractToolPanel;

/**
 * Recursive map, used by tools to help
 * specify the process trees components.
 * 
 * @author port091
 *
 */
public class ProcessTree implements Iterable<AbstractToolPanel> {

	public enum What { NODE, TOOL_PANEL, NEW_ABC_PANEL }	
	public enum Where { ROOT, UNDER, BEFORE, AFTER }
	
	private HashMap<Key, ProcessTree> map;
	private List<Key> keys; // keeps track of order, a little 
	// easier to do insert before/after then a LinkedHashMap
	
	private Dockable dockable; // If we link to a panel

	public ProcessTree(AbstractToolPanel toolPanel) {
		this();
		this.dockable = new Dockable(toolPanel);
	}
	
	public ProcessTree() {
		map = new HashMap<Key, ProcessTree>();
		keys = new ArrayList<Key>();
	}
	
	public boolean add(What what, Object element, Where where, String node) {
		
		int index = -1;
		Key newKey = null;
		
		ProcessTree childTree = null;
		ProcessTree parentTree = null;
		
		if(What.NODE == what) {
			
			if(element instanceof Key)
				newKey = (Key)element;
			
			else if(element instanceof String)
				newKey = new Key((String)element);
			
			else 
				return false;
			
			childTree = new ProcessTree();
			
		} else if(What.TOOL_PANEL == what) {
			
			if(element instanceof AbstractToolPanel) {
				newKey = ((AbstractToolPanel)element).getKey();
			} else {
				return false;
			}
			
			childTree = new ProcessTree((AbstractToolPanel)element);
			
		} else if(What.NEW_ABC_PANEL == what) {
			
			if(element instanceof Key)
				newKey = (Key)element;
			
			else if(element instanceof String)
				newKey = new Key((String)element, (String)element);
			
			else
				return false;
			
			childTree = new ProcessTree(new ABCToolPanel(newKey));
		} 
		
		if(Where.UNDER == where) {
			parentTree = getParent(node).get(node);
			index = parentTree.indexOf(node);
		} else if(Where.BEFORE == where) {
			parentTree = getParent(node);
			index = parentTree.indexOf(node);
		} else if(Where.AFTER == where) {
			parentTree = getParent(node);
			index = parentTree.indexOf(node) + 1;
		} else {
			parentTree = this; // Goes here
		}

		parentTree.map.put(newKey, childTree);
		if(index < 0 || index > keys.size()) {
			parentTree.keys.add(newKey);
		} else {
			parentTree.keys.add(index, newKey);
		}
		
		return true;		
	}

	public DefaultSingleCDockable getDockable() {
		return dockable;
	}

	public List<Key> keySet() {
		return keys;
	}
		
	public ProcessTree get(Key key) {
		return map.get(key);
	}
	
	public ProcessTree get(Object element) {
		for(Key key: keys) {
			if(key.equals(element))
				return map.get(key);
		}
		return null;
	}
	
	public int indexOf(String node) {
		for(int i = 0; i < keys.size(); i++) {
			if(keys.get(i).equals(node))
				return i;
		}
		return -1;
	}

	@Override
	public Iterator<AbstractToolPanel> iterator() {
		return getToolPanels().iterator();
	}
	
	@Override
	public String toString() {
		return dockable != null ? dockable.toolPanel.getKey().toString() : "Unnamed";
	}
	
	private ProcessTree getParent(String identifier) {

		for(Key key: keys) {
			if(key.equals(identifier))
				return this; // Found it

			// Recurse
			ProcessTree temp = map.get(key).getParent(identifier);
			if(temp != null)
				return temp;
		}

		return null; // Doesn't exist

	}

	private List<AbstractToolPanel> getToolPanels() {

		List<AbstractToolPanel> toolPanels = new ArrayList<AbstractToolPanel>();

		if(dockable != null) {
			toolPanels.add(dockable.toolPanel);
		}

		for(Key key: keys) {
			toolPanels.addAll(map.get(key).getToolPanels());
		}

		return toolPanels;

	}

	private class Dockable extends DefaultSingleCDockable {		
		private AbstractToolPanel toolPanel = null;
		private Dockable(AbstractToolPanel toolPanel) {
			super(toolPanel.getKey().key, toolPanel.getKey().toString(), toolPanel);
			this.toolPanel = toolPanel;
		}
	}
}
