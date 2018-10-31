package abc.table.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;

public class ABCCheckBox extends ABCTableCell {

	private static final long serialVersionUID = 1L;

	public ABCCheckBox(String alias, ABCDataItem data, int startIndex, int columnSpan, int rowSpan) {
		super(new CheckBox(data, alias), data, startIndex, columnSpan, rowSpan);	
		// Causes check box to flash selected/unselected when tabbing
		((CheckBox)editorComponent).removeActionListener(delegate);
		super.initialize();
	}
	
  @Override
  public void updateData(String change) {
    ((CheckBox)getComponent()).update(change);
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    super.paintComponent(observable, change);
    // TODO, what kinds of updates can we do??
  }
  
	public static class CheckBox extends JCheckBox implements ABCCellEditor {	

		private static final long serialVersionUID = 1L;

		private final ABCDataItem data;
		
		public CheckBox(ABCDataItem data, String alias) {
			super(alias);
			this.data = data;
			this.setBorderPainted(true);
			this.setToolTipText(data.getTooltip());
			this.setBackground(ABCStyle.style().getBackgroundColor());
			super.setSelected(Boolean.valueOf(data.getValue()));
			this.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
			//	this.setHorizontalAlignment(JCheckBox.CENTER);
			InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doClick");
			
			getActionMap().put("doClick", new AbstractAction() {
				private static final long serialVersionUID = 1L;
				@Override public void actionPerformed(ActionEvent e) { 
					setSelected(!Boolean.valueOf(CheckBox.this.data.getValue()));
				}		
			});
			
			this.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if(CheckBox.this.isEnabled()) {
						// Bind new selection to data item
						setForeground(CheckBox.this.isEnabled() ? ABCStyle.style().getForegroundColor() : ABCStyle.style().getTextDisabledColor());
						CheckBox.this.data.getHijacked().setValue(String.valueOf(isSelected()), CheckBox.this);
					} else {
						// Restore previous selection
						setSelected(Boolean.valueOf(CheckBox.this.data.getValue()));
					}
				}				
			});
		}	
		
		public void update(String change) {
		  if(change.equals(DataItem.VALUE))
		    super.setSelected(Boolean.valueOf(data.getValue()));
		}

		public void stopEditing() { 
			// Nothing to do
		}	
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return getComponent();
	}

}
