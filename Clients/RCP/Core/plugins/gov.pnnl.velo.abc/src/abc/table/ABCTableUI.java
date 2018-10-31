package abc.table;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;

import vabc.ABCStyle;

public class ABCTableUI extends BasicTableUI {

  private ABCTable table;

  public ABCTableUI(ABCTable table) {
    this.table = table;			
  }

  public void paint(Graphics g, JComponent c) {
    int columnMargin = table.getColumnModel().getColumnMargin();		
    int rowMargin = table.getRowMargin();
    Rectangle clip = g.getClipBounds();	
    int startRow = table.rowAtPoint(new Point(clip.x, clip.y));
    int endRow = table.rowAtPoint(new Point(clip.x, clip.y + clip.height));
    endRow = endRow < 0 ? table.getRowCount() - 1 : endRow;		
    List<ABCTableCell> drawnCells = new ArrayList<ABCTableCell>();
    for(int rowIndex = startRow; rowIndex <= endRow; rowIndex++) {
      ABCTableRow row = table.getRowModel(rowIndex);
      if (row == null) continue;
      for(int i = 0; i < row.size(); i++) {
        ABCTableCell abcCell = row.get(i);
        Rectangle cellRect = table.getCellRect(rowIndex, abcCell.getStartIndex(), false);
        if(drawnCells.contains(abcCell))
          continue;
        drawnCells.add(abcCell);
        // Draw the background, to group things together
        if(row.isChild() && abcCell.isEditable() && abcCell.getStartIndex() != (table.getColumnCount() - 1)) {

          boolean connectsRight = row.size() > i+2 ? row.get(i+1).isEditable() : false;	
          boolean connectsLeft = i - 1 >= 0 ? row.get(i - 1).isEditable() : false;
          boolean bottom = row.isLastChild();
          int xStart = cellRect.x - 1;
          int xEnd = cellRect.x + cellRect.width + (connectsRight ? columnMargin : 0);
          int yStart = cellRect.y - (rowMargin + 1);
          int yEnd = cellRect.y + cellRect.height;	
          // Borders
          g.setColor(ABCStyle.style().getGroupBackgroundColor());
          if(!connectsLeft) g.drawLine(xStart, yStart, xStart, yEnd);
          if(!connectsRight) g.drawLine(xEnd, yStart, xEnd, yEnd);						
          if(bottom) g.drawLine(xStart, yEnd, xEnd, yEnd);
          // Fill							
          g.fillRect(xStart + 1, yStart + 1, xEnd - xStart - 1, yEnd - yStart - 1);

        } else if(abcCell.isEditable() && row.hasChildren()) {
          Color backgroundColor = ABCStyle.style().getBackgroundColor();

          if(row.isExpanded() && rowIndex != table.getColumnCount() - 1) {
            g.setColor(ABCStyle.style().getGroupBackgroundColor());
          } else {
            g.setColor(backgroundColor);
          }											
          g.drawRect(cellRect.x - 1, cellRect.y - 1, cellRect.width + 1, cellRect.height + 2);						
        }	

        // Draw the ABCTableCell
        if(table.isEditing() && abcCell.isSelected()) {
          abcCell.getComponent().setBounds(cellRect);	// Draw editor, don't need this?
          abcCell.getComponent().validate();
        } else { // Draw renderer
          rendererPane.paintComponent(g, abcCell.getComponent(), table, cellRect.x, 
              cellRect.y, cellRect.width, cellRect.height, true);						
        }
      }
    }	
  }

}
