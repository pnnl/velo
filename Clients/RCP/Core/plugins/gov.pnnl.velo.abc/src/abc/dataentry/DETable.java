package abc.dataentry;


import java.awt.Color;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.SwingUtilities;

// Based on http://www.codejava.net/java-se/swing/editable-jtable-example
// Move columnNames to tablemodel
// Pass tablemodel in
// construct table model with NodeWrapper

public class DETable extends JPanel {
  public static final String[] columnNames = {
      //" ", "I(min)", "I(max)", "J(min)","J(max)","K(min)","K(max)", ""
      //" ", "Log Capillary Head (m)", "Aqueous Relative Permeability", ""
      " ", "Parent solute", "Progeny solute", "Chain decay fraction", ""
  };
  String ninetynine = "99";   // TODO for calculating width of row number

  protected JTable table;
  protected JScrollPane scroller;
  protected DETableModel tableModel;

  public DETable() {
    initComponent();
  }
  public void initComponent() {
    tableModel = new DETableModel(columnNames);
    tableModel.addTableModelListener(new DETable.InteractiveTableModelListener());
    table = new JTable();
    table.setModel(tableModel);
    table.setSurrendersFocusOnKeystroke(true);
    if (!tableModel.hasEmptyRow()) {
      tableModel.addEmptyRow();
    }
    
    // Compute a width for column 0.
    int width = 15;
    TableColumn       aColumn   = table.getColumnModel().getColumn(0);
    TableCellRenderer aRenderer = table.getTableHeader().getDefaultRenderer();
    if (aRenderer!=null) {
      Component aComponent = aRenderer.getTableCellRendererComponent(table,
          aColumn.getHeaderValue(),
          false, false, -1, 0);
      Font  aFont       = aComponent.getFont();
      Color aBackground = aComponent.getBackground();
      Color aForeground = aComponent.getForeground();

      FontMetrics metrics = getFontMetrics(aFont);
      width = metrics.stringWidth("999"); // +insets.right+insets.left

    }
    System.out.println("width"+width);

    
    

    TableColumn column = table.getColumnModel().getColumn(0);
    column.setPreferredWidth(width); 
    column.setMaxWidth(width);
    column.setCellRenderer(new LabelRenderer());


    final JPopupMenu popupMenu = new JPopupMenu();
    JMenuItem deleteItem = new JMenuItem("Delete Row");
    deleteItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        //JOptionPane.showMessageDialog(frame, "Right-click performed on table and choose DELETE");
        System.out.println("xxx");
      }
    });
    popupMenu.add(deleteItem);

    popupMenu.addPopupMenuListener(new PopupMenuListener() {

      @Override
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            int rowAtPoint = table.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table));
            if (rowAtPoint > -1) {
              table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
            }
          }
        });
      }

      @Override
      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void popupMenuCanceled(PopupMenuEvent e) {
        // TODO Auto-generated method stub

      }
    });

    table.setComponentPopupMenu(popupMenu);

    scroller = new javax.swing.JScrollPane(table);
    table.setPreferredScrollableViewportSize(new java.awt.Dimension(500, 300));
    //TableColumn hidden = table.getColumnModel().getColumn(InteractiveTableModel.HIDDEN_INDEX);
    TableColumn hidden = table.getColumnModel().getColumn(columnNames.length-1);
    hidden.setMinWidth(2);
    hidden.setPreferredWidth(2);
    hidden.setMaxWidth(2);
    //hidden.setCellRenderer(new InteractiveRenderer(InteractiveTableModel.HIDDEN_INDEX));
    hidden.setCellRenderer(new InteractiveRenderer(columnNames.length-1));

    setLayout(new BorderLayout());
    add(scroller, BorderLayout.CENTER);
  }

  public void highlightLastRow(int row) {
    int lastrow = tableModel.getRowCount();
    if (row == lastrow - 1) {
      table.setRowSelectionInterval(lastrow - 1, lastrow - 1);
    } else {
      table.setRowSelectionInterval(row + 1, row + 1);
    }

    table.setColumnSelectionInterval(0, 0);
  }

  class LabelRenderer extends DefaultTableCellRenderer {
    public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row,
        int column)
    {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      c.setBackground(Color.LIGHT_GRAY);
      return c;
    }
  }
  class InteractiveRenderer extends DefaultTableCellRenderer {
    protected int interactiveColumn;

    public InteractiveRenderer(int interactiveColumn) {
      this.interactiveColumn = interactiveColumn;
    }

    public Component getTableCellRendererComponent(JTable table,
        Object value, boolean isSelected, boolean hasFocus, int row,
        int column)
    {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (column == interactiveColumn && hasFocus) {
        if ((DETable.this.tableModel.getRowCount() - 1) == row &&
            !DETable.this.tableModel.hasEmptyRow())
        {
          DETable.this.tableModel.addEmptyRow();
        }

        highlightLastRow(row);
      }

      return c;
    }
  }

  public class InteractiveTableModelListener implements TableModelListener {
    public void tableChanged(TableModelEvent evt) {
      if (evt.getType() == TableModelEvent.UPDATE) {
        int column = evt.getColumn();
        int row = evt.getFirstRow();
        System.out.println("row: " + row + " column: " + column);
        table.setColumnSelectionInterval(column + 1, column + 1);
        table.setRowSelectionInterval(row, row);
      }
    }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      JFrame frame = new JFrame("Popup DataEntry Prototype");
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
          System.exit(0);
        }
      });
      frame.getContentPane().add(new DETable());
      frame.pack();
      frame.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}




