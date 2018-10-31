package abc.containers;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import datamodel.DataItem;
import datamodel.collections.DataItemMap;
import abc.table.ABCTable;
import vabc.ABCConstants;
import vabc.ABCStyle;
import vabc.IABCDataProvider;
import vabc.IABCUserObject;
import vabc.SwingUtils;

/**
 * Create a container where every section is part of a row
 * 
 * @author port091
 *
 */
public class ABCCustomTable extends ABCComponent implements FocusListener {
  private static final long serialVersionUID = 1L;

  private List<Node> columnNodes;
  private Map<String, Boolean> columnHeadings;

  private List<ParallelGroup> columnGoups;
  private JToolBar toolBar;

  private IABCDataProvider dataProvider;
  private SequentialGroup verticalGroup;
  private GroupLayout layout;

  private String tableUserObject;

  private List<Row> rows;

  private boolean singleLine;
  private boolean fixedSize;
    
  private int labelLength = 0;
  private int unitLength = 0;
    
  public ABCCustomTable(ABC abcParent, String key, String label, Node node, IABCUserObject abcUserObject) {
    super(abcParent, key, label, null, abcUserObject);

    this.dataProvider = abcParent.dataProvider;

    NodeList children = node.getChildNodes();
    rows = new ArrayList<Row>();

    columnNodes = new ArrayList<Node>();
    columnHeadings = new LinkedHashMap<String, Boolean>();

    Node userObjectNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.USER_OBJECT) : null;
    tableUserObject = userObjectNode != null ? userObjectNode.getNodeValue() : null;

    Node visibleRowsNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.VISIBLE_ROWS) : null;
    singleLine = visibleRowsNode != null ? Integer.parseInt(visibleRowsNode.getNodeValue()) == 1 : false;
    
    Node fixedRowsNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.FIXED) : null;
    fixedSize = fixedRowsNode != null ? Boolean.parseBoolean(fixedRowsNode.getNodeValue()) : false;
   
    for(int i = 0; i < children.getLength(); i++) {
      if(children.item(i).getNodeName().equals(ABCConstants.Key.COLUMN)) {
        columnNodes.add(children.item(i));
        if(children.item(i).hasAttributes() && children.item(i).getAttributes().getNamedItem(ABCConstants.Key.LABEL) != null) {
          boolean required = false;
          if(children.item(i).hasAttributes() && children.item(i).getAttributes().getNamedItem(ABCConstants.Key.REQUIRED) != null)
            required = new Boolean(children.item(i).getAttributes().getNamedItem(ABCConstants.Key.REQUIRED).getNodeValue());
          columnHeadings.put(children.item(i).getAttributes().getNamedItem(ABCConstants.Key.LABEL).getNodeValue(), required);
        } else {
          columnHeadings.put("", false); // Empty heading
        }
      }
    }

    toolBar = new JToolBar();
    toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
    toolBar.setFloatable(false);
    toolBar.setAlignmentX(RIGHT_ALIGNMENT);
    toolBar.setRollover(true);
    addToolbarActions(toolBar);
    toolBar.setBackground(ABCStyle.style().getBackgroundColor());

    if(singleLine || fixedSize)
      toolBar.setVisible(false); // Hide the buttons if we're doing a single line
    
    // UI
    resetTheTable();
    
    // Data
    if(!singleLine) {
      
      // Grab the initial list and add them??
      if(tableUserObject != null) {
        Object[] objects = dataProvider.getObjects(tableUserObject);
        for(Object obj: objects) {
          if(obj instanceof IABCUserObject) {
            new Row(((IABCUserObject)obj));
          }
        }
      }  
      
    } else {
      // Add the single row
      new Row();
    } 
  }
  
  private void resetTheTable() {
 
    // UI
    this.removeAll();
    
    layout = new GroupLayout(this);
    this.setLayout(layout);

    SequentialGroup horizontalGroup = layout.createSequentialGroup(); 
    verticalGroup = layout.createSequentialGroup();

    columnGoups = new ArrayList<ParallelGroup>();
    for(int i = 0; i < columnNodes.size(); i++) {
      ParallelGroup p = layout.createParallelGroup(GroupLayout.Alignment.TRAILING);
      columnGoups.add(p);
      horizontalGroup.addGroup(p);
    }

    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(toolBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addGroup(horizontalGroup)))
        );

    verticalGroup.addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE);

    layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(verticalGroup));

    if(!singleLine && !columnHeadings.isEmpty()) {
      ParallelGroup row = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false);
      List<String> labels = new ArrayList<String>(columnHeadings.keySet());
      for(int i = 0; i < columnGoups.size(); i++) {
        JPanel header = new JPanel();
        JLabel headingLabel = new JLabel(labels.get(i));
        headingLabel.setFont(columnHeadings.get(labels.get(i)) ? ABCStyle.style().getRequiredFont() : ABCStyle.style().getBorderFont());
        headingLabel.setBackground(ABCStyle.style().getTabBackgroundColor());
        headingLabel.setForeground(ABCStyle.style().getTabForegroundColor());
        header.setBackground(ABCStyle.style().getTabBackgroundColor());
        header.add(headingLabel);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, i == 0 ? 1 : 0, 1, 1, ABCStyle.style().getBorderColor()), header.getBorder()));
        columnGoups.get(i).addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);   
        row.addComponent(header, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
      }    
      verticalGroup.addGroup(row);  
    }
    
    for(Row row: rows) {
      row.initUI();
    }
  }
  
  private class Row {
    private List<ABCTable> columns;
    private IABCUserObject rowUserObject;
    public boolean isSelected;
    private ParallelGroup theRow;
    
    public Row() { 
      rows.add(this);
      if(ABCCustomTable.this.tableUserObject != null) {
        // Add a new one...
        Object objc = dataProvider.getObject(ABCCustomTable.this.tableUserObject);
        if(objc instanceof IABCUserObject) {
          rowUserObject = (IABCUserObject)objc;
        }
      } else {
        rowUserObject = new DataItemMap(null); // By default
      }
      initData();
      initUI();
    }

    public Row(IABCUserObject userObject) {  
      rows.add(this);
      this.rowUserObject = userObject;
      initData();
      initUI();
    }

    public void addColumn(ABCTable column) {
      if(columns == null)
        columns = new ArrayList<ABCTable>();
      columns.add(column);
    }

    public void select(boolean select) {
      isSelected = select;
      for(ABCTable column: columns) {
        column.setBackgroundSelected(select);
        column.repaint();
        column.revalidate();
      }
    }
    
    public void initData() {
      
      for(int i = 0; i < columnGoups.size(); i++) {
        Node columnNode =  columnNodes.get(i);      
     
        labelLength = getMaxLabelLength(0, columnNode, ABCConstants.Key.LABEL);
        unitLength = getMaxLabelLength(0, columnNode, ABCConstants.Key.UNITS);
        int temp = getMaxLabelLength(0, columnNode, ABCConstants.Key.DEFAULT_UNIT);
        if(temp > unitLength)
          unitLength = temp;
        
        ABCTable table = new ABCTable(ABCCustomTable.this, columnNode, dataProvider, abcParent.actionProvider);
        
        if(labelLength == 0)
          table.hideLabel();
        if(unitLength == 0)
          table.hideUnit();        
        
        primitives.add(table);
        if(!ABCCustomTable.this.singleLine) {
          if(!rCollection.keySet().contains(rowUserObject.getIdentifier()))
            rCollection.put(rowUserObject.getIdentifier(), table.getData(), this);
          else
            rCollection.get(rowUserObject.getIdentifier()).merge(table.getData(),this);    
          table.addFocusListener(ABCCustomTable.this); 
        } else {
          rCollection.merge(table.getData(), this); // No reason to nest the single line
        }

        addColumn(table);
      
        if(!ABCCustomTable.this.singleLine)
          table.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, i == 0 ? 1 : 0, 1, i == (columnGoups.size() - 1) ? 1 : 0, ABCStyle.style().getBorderColor()), table.getBorder()));       
      }   
    }
    
    public void initUI() {
      theRow = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false);
      for(int i = 0; i < columnGoups.size(); i++) {
        ABCTable table = columns.get(i);
        columnGoups.get(i).addComponent(table, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);   
        theRow.addComponent(table, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
      }   
      verticalGroup.addGroup(theRow);  
    }
  }
  
  @Override
  public IABCUserObject getUserObject() {
    if(!rows.isEmpty())
      return rows.get(rows.size()-1).rowUserObject;
    return null;
  }

  private int getMaxLabelLength(int max, Node node, String attribute) {

    if(node.hasAttributes() && node.getAttributes().getNamedItem(attribute) != null) {
      if (!node.getNodeName().equals(ABCConstants.Key.GROUP) && !node.getNodeName().equals(ABCConstants.Key.COLUMN)) {
        String label = node.getAttributes().getNamedItem(attribute).getNodeValue().toString();
        for(Font font: ABCStyle.style().getFonts()) {
          int length = this.getFontMetrics(font).stringWidth(label);
          if(length > max)
            max = length;       
        }
      }
    }

    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      int childLength = getMaxLabelLength(max, children.item(i), attribute);
      if(childLength > max)
        max = childLength;
    }

    return max;
  }
  
  protected void addToolbarActions(JToolBar toolbar) {

    JButton addButton =  SwingUtils.newButton("add", 16, "Add");    
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new Row();
        ABCCustomTable.this.validate();
        ABCCustomTable.this.repaint();
      }
    });

    JButton deleteButton =  SwingUtils.newButton("delete", 16, "Delete");  
    deleteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Row selected = null;
        int index = -1;
        for(int i = 0; i < rows.size(); i++) {
          if(rows.get(i).isSelected) {
            selected = rows.get(i);
            index = i;
            break;
          }
        }
        if(selected != null) {
          DataItem identifier = null;
          if(selected.rowUserObject != null) { // Let the backing object update
            dataProvider.removeObject(selected.rowUserObject);
            identifier = selected.rowUserObject.getIdentifier();
          } for(ABCTable table: selected.columns) {
            ABCCustomTable.this.remove(table); 
            primitives.remove(table);
          }
          if(identifier != null)
            rCollection.remove(identifier,  ABCCustomTable.this);
          rows.remove(selected);
          resetTheTable();
        }
        
        if(index >= 0 && index < rows.size()) {
          rows.get(index).select(true);
        } else if(index > 0 && index - 1 < rows.size()) {
          rows.get(index - 1).select(true);          
        }
      }
    });

    JButton loadButton = SwingUtils.newButton("import", 16, "Import");
    loadButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //loadParameters();
      }
    });
    loadButton.setEnabled(false);    

    JButton upButton = SwingUtils.newButton("arrow_up_green", 16, "Move up");
    upButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveUp();
      }
    });
    

    JButton downButton = SwingUtils.newButton("arrow_down_blue", 16, "Move down");
    downButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        moveDown();
      }
    });

    toolbar.add(addButton);
    toolbar.add(deleteButton);
    toolbar.add(loadButton);
    toolbar.add(upButton);
    toolbar.add(downButton);
    
  }

  public void moveUp() {
    for(int i = 1; i < rows.size(); i++) {
      if(rows.get(i).isSelected) {
        rows.add(i - 1, rows.remove(i));
        break;
      }
    }
    resetTheTable();
  }

  public void moveDown() {
    for(int i = 0; i < rows.size() - 1; i++) {
      if(rows.get(i).isSelected) {
        rows.add(i + 1, rows.remove(i));
        break;
      }
    }
    resetTheTable();
  }
  
  @Override
  public void focusGained(FocusEvent e) {
    Object source = e.getSource();
    if(!(source instanceof JComponent)) {
      System.out.println("TODO: Debug ABCCustomTable:346" + source);
      return;
    }
    ABCTable selected = (ABCTable)(source instanceof ABCTable ? source : ((JComponent)source).getParent()); 
    for(Row row: rows) {
      if(!row.columns.contains(selected))
        row.select(false); // Unselect
    }
    for(Row row: rows) {
      if(row.columns.contains(selected)) {
        row.select(true);
      }
    }
  }

  @Override
  public void focusLost(FocusEvent e) {   }

  @Override
  public void addComponentToUI(ABCComponent component) { }

  @Override
  public boolean isDynamic() {
    return true;
  }


}
