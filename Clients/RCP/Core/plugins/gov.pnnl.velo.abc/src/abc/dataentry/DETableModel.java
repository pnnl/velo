package abc.dataentry;

import java.util.Collection;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

public class DETableModel extends AbstractTableModel {
    public static final int INDEX_INDEX = 0;

    protected String[] columnNames;
    protected Vector dataVector;

    public DETableModel(String[] columnNames) {
        this.columnNames = columnNames;
        dataVector = new Vector();
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public boolean isCellEditable(int row, int column) {
        if (column == columnNames.length) return false;
        if (column == 0) return false;
        return true;
    }

    public Class getColumnClass(int column) {
        if (column == 0)
          return int.class;
        else if (column < getColumnCount()-1)
          return String.class; // TODO
        else
          return Object.class;
    }

    public Object getValueAt(int row, int column) {
        DERow record = (DERow)dataVector.get(row);
        if (column == 0)
           return row;
        else if (column < getColumnCount()-1)
           return record.getItem(column);
        else {
           return new Object();
        }

    }

    public void setValueAt(Object value, int row, int column) {
        DERow record = (DERow)dataVector.get(row);
        if (column == 0)
            ;
        else if  (column < getColumnCount()-1)
          record.setItem(column,(String)value);
        else
           System.out.println("invalid index");

        fireTableCellUpdated(row, column);
    }

    public int getRowCount() {
        return dataVector.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public boolean hasEmptyRow() {
        if (dataVector.size() == 0) return false;
        DERow record = (DERow)dataVector.get(dataVector.size() - 1);
        if (record.isEmpty())
            return true;
        else
            return false;
    }

    public void addEmptyRow() {
        dataVector.add(new DERow());
        fireTableRowsInserted(
           dataVector.size() - 1,
           dataVector.size() - 1);
    }
}