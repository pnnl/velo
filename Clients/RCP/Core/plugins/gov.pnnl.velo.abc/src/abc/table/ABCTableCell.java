package abc.table;

import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

import abc.table.components.ABCComboBox;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;
import datamodel.DataModelObservable;

public abstract class ABCTableCell extends DefaultCellEditor implements TableCellRenderer, DataModelObserver, Comparable<Object> {

  private static final long serialVersionUID = 1L;

  // Holds the key for this UI element
  protected ABCDataItem primaryItem;

  // Additional data items for check lists and combo boxes
  protected List<ABCDataItem> items;

  // A bit of data regarding our size/placement
  private int startIndex;
  private int endIndex;
  private int rowSpan;

  // Something related to the stop editing on focus lost property is causing
  // cells to call stop editing right after they start editing.  Tossed this in
  // to ignore stop editing events sent within 50ms of a start editing event...
  private boolean respond = true;


  /**
   * Wrapper for check boxes
   * Check box will have just one item
   * [key=(key, alias), value=true/false, linked_object=user defined]
   * @param row
   * @param checkBox
   * @param primaryItem
   */
  public ABCTableCell(JCheckBox checkBox, ABCDataItem primaryItem, int startIndex, int columnSpan, int rowSpan) { 
    super(checkBox); 
    this.primaryItem = primaryItem;

    this.startIndex = startIndex;
    this.endIndex = startIndex + columnSpan - 1;
    this.rowSpan = rowSpan;
  }	

  /**
   * Wrapper for text fields
   * Text field will have just one item
   * [key=(key, alias), value=combo box field]
   * @param row
   * @param textField
   * @param primaryItem
   */
  public ABCTableCell(JTextField textField, ABCDataItem primaryItem, int startIndex, int columnSpan, int rowSpan) { 
    super(textField);  
    this.primaryItem = primaryItem; 

    this.startIndex = startIndex;
    this.endIndex = startIndex + columnSpan - 1;
    this.rowSpan = rowSpan;
  }

  /**
   * Wrapper for check box lists
   * Each check box will have one item: [key=(key, alias), value=true/false, linked_object=user defined]
   * @param row
   * @param textField
   * @param primaryItem
   * @param backingItems
   */
  public ABCTableCell(JTextField textField, List<ABCDataItem> items, ABCDataItem primaryItem, int startIndex, int columnSpan, int rowSpan) { 
    super(textField); 
    this.primaryItem = primaryItem; 
    this.items = items;

    this.startIndex = startIndex;
    this.endIndex = startIndex + columnSpan - 1;
    this.rowSpan = rowSpan;
  }	

  /**
   * Wrapper for combo boxes (user defined lists as well as units)
   * Primary item will be the currently selected item [key=(key, alias), value=selected]
   * So we can get at the items in the list, they will also have backing items:
   *           [key=(key, alias), value=true/false, linked_object=user defined]
   * @param row
   * @param comboBox
   * @param primaryItem
   * @param backingItems
   */
  public ABCTableCell(JComboBox comboBox, ABCDataItem primaryItem, List<ABCDataItem> items, int startIndex, int columnSpan, int rowSpan) { 
    super(comboBox); 
    this.primaryItem = primaryItem; 
    this.items = items;

    this.startIndex = startIndex;
    this.endIndex = startIndex + columnSpan - 1;
    this.rowSpan = rowSpan;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getRowSpan() {
    return rowSpan;
  }

  public int getEndIndex() {
    return endIndex;
  }  

  public boolean is(Key key) {
    if(key == null)
      return true; // a null key matches everything
    if(primaryItem != null && primaryItem.matches(key))
      return true;
    if(items != null) {
      for(DataItem item: items) {
        if(item.matches(key))
          return true;
      }
    }
    return false;
  }

  protected void initialize() {

    setClickCountToStart(1);

    if(items != null) {
      for(DataItem data: items) {
        data.addObserver((DataModelObserver)this);
      }
    }

    if(primaryItem != null) {
      primaryItem.addObserver(this);
    }

    if(isEditable()) {

      JComponent component = (JComponent) getComponent();

      // Set a nice name for UI tester
      if(primaryItem != null) {
        component.setName(primaryItem.getKey().key + "_" + component.getClass().getSimpleName().toLowerCase());
      } else if(items != null && !items.isEmpty()) {
        component.setName(items.get(0).getKey().key + "_" + component.getClass().getSimpleName().toLowerCase());        
      }

      paintComponent(null, null);

    }
  }

  public abstract void updateData(String change);

  /**
   * Things that apply to all table cells
   * @param observable TODO
   */
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    JComponent editor = getEditor();
    JComponent component = (JComponent)getComponent();

    if(primaryItem != null) {
      editor.setFont(primaryItem.getFieldFont());
      editor.setForeground(primaryItem.getFieldForegroundColor());

      if(isEditable()) {
        // TODO: Decide if we want the spacing?   component.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.VALUE)));
        if(component.equals(editor))
          editor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.VALUE)),
              BorderFactory.createEmptyBorder(0, 3, 0, 0)));  
        else // Scroll panes shouldn't have a border with some space, we also want to apply them to the scroll pane not the editor inside it
          component.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.VALUE)));
      }
    }
   
  }

  @Override
  public Object getCellEditorValue() { 
    return this instanceof ABCComboBox ? super.getCellEditorValue() : getComponent();	
  }	

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    return getComponent();
  }

  public JComponent getEditor() {
    // Just in case we wrapped this component in a scroll pane
    JComponent editor = (JComponent)getComponent();
    if(editor instanceof JScrollPane)
      editor = (JComponent)((JScrollPane)editor).getViewport().getView();
    return editor;
  }
  
  public boolean isEditable() {
    if(primaryItem != null) {
      return primaryItem.isEditable();
    }
    if(items != null) {
      for(ABCDataItem item: items) {
        if(!item.isEditable())
          return false;
      }
    }
    return false;
  }

  public boolean isSelected() {
    // If this is a combo box and we're a unit we don't want
    // to set the field as selected... hmmm how do we handle this?
    if(primaryItem != null) {
      return primaryItem.isValueSelected();
    }
    if(items != null) {
      for(ABCDataItem item: items) {
        if(!item.isValueSelected())
          return false;
      }
    }
    return false;
  }

  public void setSelected(boolean isSelected, Object source) {
    if(primaryItem != null) {
      primaryItem.setValueSelected(isSelected, source);
    }
    if(items != null) {
      for(ABCDataItem item: items) {
        item.setValueSelected(isSelected, source);
      }
    }
  }

  protected interface ABCCellEditor {
    public void stopEditing();
  }

  public void startCellEditing() {
    // Once we start editing, wait a while before we allow editing to stop
    synchronized(ABCTableCell.this) {
      ABCTableCell.this.respond = false;
    }
    setSelected(true, this);
    new Thread(new Runnable() {
      @Override
      public void run() {        
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        synchronized(ABCTableCell.this) {
          ABCTableCell.this.respond = true;
        }
      }    
    }).start();

  }

  @Override 
  public boolean stopCellEditing() {    
    synchronized(ABCTableCell.this) {
      if(!ABCTableCell.this.respond)
        return true;
    }
    // Data items will be in charge of this now
    setSelected(false, this);    
    boolean editingStopped = super.stopCellEditing();
    if(getComponent() instanceof ABCCellEditor)
      ((ABCCellEditor)getComponent()).stopEditing();  
    return editingStopped;
  }

  @Override
  public int compareTo(Object comparable) {
    if(comparable instanceof ABCTableCell) {
      if(((ABCTableCell)comparable).equals(this))
        return 0;
      return 1;
    } else if(comparable instanceof Integer) {
      Integer index = (Integer)comparable;
      if(index < getStartIndex())
        return -1;
      if(index > getEndIndex())
        return 1;
      return 0;
    }
    return -1;  
  }

  @Override
  public boolean shouldObserve(DataModelObservable observable) {
    // Make sure we own the item
    if(!(observable instanceof ABCDataItem))
      return false;
    if(primaryItem != null && primaryItem.equals(observable))
      return true;
    if(items != null) {
      for(ABCDataItem item: items) {
        if(item.equals(observable))
          return true;
      }
    }
    return false; // Not our item
  }

  @Override
  public void startObserving(DataModelObservable observable) {
    // Should we apply UI constraints?

  }

  @Override
  public void stopObserving(DataModelObservable observable) {
    // Do nothing?
  }

  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) {

    // If the change did not come from the UI
    if(!dataModelChange.getSource().equals(getEditor())) {     
      updateData(dataModelChange.getChange());
    }
    paintComponent(observable, dataModelChange); // Update the UI when a change is made
 
  }

}
