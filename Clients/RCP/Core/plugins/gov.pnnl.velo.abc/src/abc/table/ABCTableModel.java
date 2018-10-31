package abc.table;

import javax.swing.table.DefaultTableModel;
import abc.validation.ABCDataItem;

public class ABCTableModel extends DefaultTableModel {

  private static final long serialVersionUID = 1L;

  private ABCTable table;

  public ABCTableModel(ABCTable table) {
    this.table = table;			
  }

  @Override
  public int getColumnCount() {
    return 7;
  }

  @Override
  public int getRowCount() {
    int rowCount = 0;
    if(table == null)
      return rowCount;
    // Otherwise count them up
    for(ABCTableRow row: table.getRows()) {
      if(row.isVisible()) // Will be visible if it has no parent or its parent is selected and visible
        rowCount += row.rowSpan;
    }
    return rowCount;
  }

  @Override public Object getValueAt(int row, int column) { return null; }

  @Override 
  public void setValueAt(Object aValue, int row, int column) {	
    /*
    ABCTableRow aBCTableRow = table.getRowModel(row);
    if(aBCTableRow != null && aBCTableRow.hasChildren())  {	
 //     updateChildren(aBCTableRow, row, aValue);
      aBCTableRow.setPreviousSelection((ABCDataItem)aValue); // Update this	
    }			
    */
  }
/*
  public void updateChildren(ABCTableRow tableRow, int row, Object aValue) {	
    int modifiedRows = 0;
    System.out.println("Comparing: " + aValue + " to " + tableRow.getPreviousSelection());
    if(tableRow.hasChildren() && !aValue.equals(tableRow.getPreviousSelection()))  {	
      int previousChildren = tableRow.getChildRows((ABCDataItem)tableRow.getPreviousSelection());
      int newChildren = tableRow.getChildRows((ABCDataItem)aValue);	
      if(newChildren != previousChildren)
        System.out.println("Previous children: " + previousChildren + ", new children: " + newChildren);
      modifiedRows = newChildren;
      if(previousChildren > newChildren) {
        fireTableRowsDeleted(row + 1 + previousChildren - (previousChildren - newChildren), row + previousChildren);
      } else if(newChildren > previousChildren) {
        fireTableRowsInserted(row + 1 + previousChildren, row + newChildren);
        modifiedRows -= newChildren - previousChildren;	// Don't need to modified the inserted rows
      }
      fireTableRowsUpdated(row,  row + modifiedRows);			
    }
  }
  */

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return table.getRowModel(rowIndex).getCell(columnIndex).isEditable();
  }	
}
