package abc.table.components;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;

public class ABCCheckBoxList extends ABCTableCell {

	private static final long serialVersionUID = 1L;

	private JList jList;
	private JScrollPane scrollPane;
	
	public ABCCheckBoxList(List<ABCDataItem> backingItems, ABCDataItem primaryItem, int startIndex, int columnSpan, int rowSpan) {
		super(new JTextField(), backingItems, primaryItem, startIndex, columnSpan, rowSpan);
		jList = new JList(backingItems.toArray());
		jList.setCellRenderer(new CheckListRenderer(jList));
		jList.setBackground(ABCStyle.style().getFieldBackgroundColor());
		jList.setBorder(null);
		
		scrollPane = new JScrollPane(jList);
		// Redirect focus to the text editor
		scrollPane.addFocusListener(new FocusListener() {
			@Override public void focusGained(FocusEvent e) {	jList.requestFocus(); }
			@Override public void focusLost(FocusEvent e) {	}			
		});
		scrollPane.setBorder(null);
		editorComponent = scrollPane;	
		jList.addMouseListener(new MouseAdapter() {    
      @Override
      public void mousePressed(MouseEvent event) {
        if(jList.getModel().getSize() == 0)
          return; // No point?  TODO: Should we hide empty lists?
        int index = jList.locationToIndex(event.getPoint());
        ABCDataItem item = (ABCDataItem)jList.getModel().getElementAt(index);
        ABCCheckBoxList.this.primaryItem.setValueSelected(true, jList);
        Boolean checked = !(new Boolean(item.getValue()));
        item.getHijacked().setValue(checked.toString(), jList);
        jList.repaint(jList.getCellBounds(index, index)); 
      }       
    });
		super.initialize();
	}

  @Override
  public void updateData(String change) {
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    super.paintComponent(observable, change); 
  }
	
	private class CheckListRenderer extends JCheckBox implements ListCellRenderer {  

		private static final long serialVersionUID = 2994426848047213323L;

		private JList list;
		public CheckListRenderer(JList jList) {
			this.list = jList;					
			list.setFixedCellHeight(16);
			setBackground(ABCStyle.style().getFieldBackgroundColor());
		}

		public Component getListCellRendererComponent(JList list,  Object value,  int index,  boolean isSelected,  boolean cellHasFocus)  
		{  		  
		  ABCDataItem item = (ABCDataItem)value;
		  this.setToolTipText(item.getTooltip());
			setSelected(new Boolean(item.getValue()));
			setText(item.getHijacked().getAlias());  
			setFont(item.getFieldFont());
			// Font color can come from the primary item
			setForeground(primaryItem.getFieldForegroundColor());
      setBackground(item.getFieldBackgroundColor());
			setEnabled(list.isEnabled());
			return this;  
		}	
	}
	
}
