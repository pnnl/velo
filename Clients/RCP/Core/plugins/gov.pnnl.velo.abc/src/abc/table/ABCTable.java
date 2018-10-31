package abc.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vabc.ABCConstants;
import vabc.ABCStyle;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.IABCUserObject;
import vabc.NodeWrapper;
import abc.containers.ABCComponent;
import abc.containers.ABCPrimitive;
import abc.table.components.ABCComboBox;
import abc.table.components.ABCComboBox.ComboBox;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.Key;
import datamodel.collections.DataItemMap;

public class ABCTable extends JTable implements ABCPrimitive {

  private static final long serialVersionUID = 1L;
  
  private IABCDataProvider dataProvider;
  private IABCActionProvider actionProvider;
  
  private DataItemMap hijacked;
  private List<ABCDataItem> abcItems;
  private List<ABCTableRow> rows;

  private String identifier;

  private HashSet<String> errors;
  private int[] uiHints = new int[]{20, 20, 20, 40, 60, 100, 0};

  public ABCComponent abcParent;
  public IABCUserObject userObject;

  /**
   * 
   * Create an arbitrary table size based off the given node
   * 
   * @param parent
   * @param node
   * @param dataProvider
   * @param actionProvider
   */
  public ABCTable(ABCComponent parent, Node node, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    userObject = parent.getUserObject();

    // Setup table
    initialize(parent, dataProvider, actionProvider);

    // Initialize data
    initialize(node);

    hideShouldBuildRows(node);

    // Formatting
    applyColumnWidthHints(false, false);

  }
  
  /**
   * Create a single row table with the given data item
   * 
   * @param parent
   * @param node
   * @param dataItem
   * @param dataProvider
   * @param actionProvider
   */
  public ABCTable(ABCComponent parent, Node node, DataItem dataItem, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    userObject = parent.getUserObject();

    // Setup table
    initialize(parent, dataProvider, actionProvider);

    // Initialize data
    ABCTableRow model = newRowModel(new NodeWrapper(node), dataItem);
    getRows().add(model);	
    merge(model);

    hideShouldBuildRows(node);

    // Formatting
    applyColumnWidthHints(false, false);
  }

  /**
   * Create a single row table with the given data item
   * 
   * @param parent
   * @param data
   * @param dataProvider
   * @param actionProvider
   */
  public ABCTable(ABCComponent parent, NodeWrapper data, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    userObject = parent.getUserObject();

    // Setup table
    initialize(parent, dataProvider, actionProvider);

    // Initialize data
    ABCTableRow model = newRowModel(data);
    getRows().add(model);	
    merge(model);

    // Formatting
    applyColumnWidthHints(false, false);		
  }	

  @Override
  public boolean show(Key key, boolean shouldShow) {
    boolean found = false;
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        item.setShowing(shouldShow, this);
        found = true;
      }
    }
    return found;
  }

  @Override
  public boolean enable(Key key, boolean shouldEnable) {
    boolean found = false;
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        item.setEnabled(key, shouldEnable, this);
        found = true;
      }
    }
    return found;
  }

  @Override
  public boolean readOnly(Key key, boolean isReadOnly) {
    boolean found = false;
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        item.setReadOnly(isReadOnly, this);
        found = true;
      }
    }
    return found;
  }
  
  @Override
  public boolean select(Key key, boolean shouldSelect) {    
    boolean found = false;
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        item.setValueSelected(shouldSelect, this);
        found = true;
      }
    }
    return found;    
  }

  @Override
  public boolean exists(Key key) {
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        return true;
      }
    }
    return false; 
  }

  @Override
  public DataItem find(Key key) {
    for(ABCDataItem item: abcItems) {
      if(item.matchesKey(key)) {
        return item;
      }
    }
    return null; 
  }

  @Override
  public boolean isActive(Key key) {    
    boolean active = false;
    for(ABCDataItem item: abcItems) {
      // Items may be shared between many abc data items
      // all we need is one to be active for this item to be active?
      if(key == null || item.matches(key)) {
        if(item.isActive())
          active = true;
      }
    }
    return active;
  }

  @Override
  public boolean isSelected(Key key) {
    for(ABCDataItem item: abcItems) {
      if(key == null || item.matches(key)) {
        return item.isValueSelected() || item.isBackgroundSelected();     
      }
    }
    return false; // Didn't find it?
  }

  public void hideLabel() {
    uiHints = new int[]{0, 0, 0, this.uiHints[3], this.uiHints[4], 80, this.uiHints[6]};
    applyColumnWidthHints(false, true);
  }

  public void reduceFieldSize() {
    uiHints = new int[]{this.uiHints[0], this.uiHints[1], this.uiHints[2], 20, 20, 80,  this.uiHints[6]};
    applyColumnWidthHints(false, false);
  }

  public void hideUnit() {
    uiHints = new int[]{this.uiHints[0], this.uiHints[1], this.uiHints[2], this.uiHints[3], this.uiHints[4], 0, this.uiHints[6]};
    applyColumnWidthHints(false, false);		
  }

  public void showFileButton() {
    uiHints[6] = 70;
    applyColumnWidthHints(true, false);		
  }

  @Override
  public void addFocusListener(FocusListener l) {
    // Add a listener to the table and the cells in it
    super.addFocusListener(l);
    if(rows == null)
      return; // No cells to add focus to
    for(ABCTableRow row: rows) {
      for(ABCTableCell cell: row) {
        cell.getComponent().addFocusListener(l);
      }
    }    
  }

  public void setBackgroundSelected(boolean shouldSelect) { 
    this.setBackground(shouldSelect ? ABCStyle.style().getSelectionColor() : ABCStyle.style().getBackgroundColor());
    for(ABCDataItem item: abcItems) {      
      item.setBackgroundSelected(shouldSelect, this);
    }
  }

  public void startEditing() {
    this.transferFocusToFirst();
  }
  
  public void stopEditing() {
    TableCellEditor editor = this.getCellEditor();
    if(editor != null)
      editor.stopCellEditing();
  }

  private void initialize(ABCComponent parent, IABCDataProvider dataProvider, IABCActionProvider actionProvider) {

    // Globals
    this.abcParent = parent;
    
    this.dataProvider = dataProvider;
    this.actionProvider = actionProvider;
    
    errors = new HashSet<String>();
    rows = new ArrayList<ABCTableRow>();
    this.hijacked = new DataItemMap(null, true, false);
    this.abcItems = new ArrayList<ABCDataItem>();
    identifier = UUID.randomUUID().toString();

    setTableHeader(null); 
    setBackground(ABCStyle.style().getBackgroundColor());
    putClientProperty("terminateEditOnFocusLost", true);

    // For tabbing
    ActionMap am = getActionMap();
    InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "selectNextRowCell");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "selectPreviousRowCell");	

    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "selectNextRowCellDown");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), "selectNextRowCellDown");

    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "selectNextRowCellUp");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), "selectNextRowCellUp");		

    am.put("selectNextRowCell", new NextCellAction(this, KeyEvent.VK_KP_RIGHT));
    am.put("selectPreviousRowCell", new NextCellAction(this, KeyEvent.VK_KP_LEFT));			
    am.put("selectNextRowCellDown", new NextCellAction(this, KeyEvent.VK_KP_DOWN));	
    am.put("selectNextRowCellUp", new NextCellAction(this, KeyEvent.VK_KP_UP));	

    setCellSelectionEnabled(true);

    setModel(new ABCTableModel(this));
    setUI(new ABCTableUI(this));

    setRowHeight(ABCConstants.ROW_HEIGHT);
    setRowMargin(2);

    getColumnModel().setColumnMargin(4);
  }

  public int getLabelLength() {
    return abcParent.getLabelLength();
  }

  private void applyColumnWidthHints(boolean setMax, boolean override) {
    
    // Check to see if we have a set label, we'll need to handle that differently if we do
    boolean hasSetLabel = false;
    for(ABCTableRow row: this.getRows()) {
      for(ABCDataItem item: row.getItems()) {
        if(item.getKey().equals(ABCConstants.Key.SET_LABEL)) {
          for(Font font: ABCStyle.style().getFonts()) {
            int width = this.getFontMetrics(font).stringWidth(item.getKey().getAlias());
            if(width > uiHints[0]) {
              uiHints[0] = width;
              hasSetLabel = true;
            }
          }
        }
      }
    }
    
    int labelLength = this.getLabelLength() + 6; 
    int unitLength = abcParent.getUnitLength() + 6;
    int currentLength = uiHints[0] + uiHints[1] + uiHints[2];
    // Add additional label space to the second column
    int secondColumn = !override && currentLength != 0 && currentLength < labelLength ? uiHints[1] + (labelLength - currentLength) : uiHints[1];

    int unitColumn = !override && uiHints[5] != 0 && uiHints[5] < unitLength ? unitLength : uiHints[5];
    Dimension preferredSize = new Dimension(0, ABCConstants.ROW_HEIGHT);
    for(int i = 0; i < getColumnCount(); i++) {
      int width = i == 1 ? secondColumn : i == 5 ? unitColumn : uiHints[i];
      getColumnModel().getColumn(i).setPreferredWidth(width);
      // Set a max so boxes don't expand across the screen
      getColumnModel().getColumn(i).setMaxWidth( (i<3||i>4) ? width*2 : width*4); 
      if(width == 0) {
        getColumnModel().getColumn(i).setMinWidth(0);
        getColumnModel().getColumn(i).setMaxWidth(0);
      }
      if(setMax)
        getColumnModel().getColumn(i).setMaxWidth(width);				
      preferredSize.width += width;
    }
    setPreferredSize(preferredSize);		// Update preferred size
  }

  private void addRows(Node node, ABCTableRow parent, Key group, String type) {		
    String name = node.getNodeName();	
    if(!name.startsWith("#")) {
      if(name.equals(ABCConstants.Key.GROUP)) {
        Node key = node.getAttributes().getNamedItem(ABCConstants.Key.KEY);
        Node label = node.getAttributes().getNamedItem(ABCConstants.Key.LABEL);
        group = new Key(key != null ? key.getNodeValue() : null, label != null ? label.getNodeValue() : null);
      }			
      ABCTableRow model = newRowModel(new NodeWrapper(node), parent, group);
      if(model != null) {
        rows.add(model);	 
        merge(model);
        if(name.equals(ABCConstants.Key.CHOICE))
          parent = model;
      }
    }

    // Need the sub groups/choices too
    if(name.equals(ABCConstants.Key.CHOICE) || name.equals(ABCConstants.Key.GROUP)) {
      NodeList children = node.getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        addRows(child, parent, group, type);
      }
    } 
  }

  private void hideShouldBuildRows(Node node) {		
    String name = node.getNodeName();	
    if(!name.startsWith("#")) {
      if (!dataProvider.shouldShow(node)) {
        // At this level, we want to only hide a specific row
        Node labelNode = node.getAttributes().getNamedItem(ABCConstants.Key.LABEL);
        Node keyNode = node.getAttributes().getNamedItem(ABCConstants.Key.KEY);
        this.show(new Key(keyNode != null ? keyNode.getNodeValue() : null, labelNode != null ? labelNode.getNodeValue() : null), false);
      }
    }

    // Need the sub groups/choices too
    if(node.hasChildNodes()) {
      NodeList children = node.getChildNodes();
      for(int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        hideShouldBuildRows(child);
      }
    } 
  }

  @Override
  public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
    if(row < 0 || column < 0) return super.getCellRect(row, column, includeSpacing);
    if(getRowModel(row) ==  null)
      System.out.println("No row model for row: " + row);
    ABCTableCell abcTableCell = getRowModel(row).getCell(column);
    row = getRowStartIndex(row);
    Rectangle start = super.getCellRect(row, abcTableCell.getStartIndex(), false);
    Rectangle end = super.getCellRect(row + abcTableCell.getRowSpan() - 1, abcTableCell.getEndIndex(), false);
    return new Rectangle(start.x, start.y, end.x - start.x + end.width, end.y - start.y + end.height);
  }

  @Override
  public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    int selectedRow = getSelectedRow();
    int selectedColumn = getSelectedColumn();
    int newSelectedRow =  -1; 
    int newSelectedColumn = -1;

    ABCTableCell newEditor = null;

    if(rowIndex >= 0) {
      newSelectedRow = getRowStartIndex(rowIndex);
      newEditor = getRowModel(newSelectedRow).getCell(columnIndex);
      newSelectedColumn = newEditor.getStartIndex();
    }

    if(selectedRow == newSelectedRow && selectedColumn == newSelectedColumn) {
      return; // Same, no change in selection
    }

    // Stop editing the current editor
    if(selectedRow > 0) {
      ABCTableCell currentEditor = getRowModel(selectedRow).getCell(selectedColumn);
      if(currentEditor != null)
        currentEditor.stopCellEditing();
    }

    super.changeSelection(newSelectedRow, newSelectedColumn, toggle, extend);

    // Start editing the new editor
    if(newEditor != null && newEditor.isEditable() && editCellAt(getSelectedRow(), getSelectedColumn())) {
      getEditorComponent().requestFocus();  
    }
  }


  @Override
  public Component prepareEditor(TableCellEditor editor, int row, int column) {
    ((ABCTableCell)editor).startCellEditing();
    return ((ABCTableCell)editor).getComponent();
  }

  public class NextCellAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private ABCTable table;
    private int key;

    public NextCellAction(ABCTable table, int key) {
      this.table = table;
      this.key = key;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int rowIndex = getSelectedRow();
      if(rowIndex < 1)
        rowIndex = getEditingRow();
      
      if(key == KeyEvent.VK_KP_UP)
        rowIndex--;
      if(key == KeyEvent.VK_KP_DOWN)
        rowIndex++;
      ABCTableCell nextCell = null;
      if(key == KeyEvent.VK_KP_UP || key == KeyEvent.VK_KP_LEFT) {
        while(rowIndex >= 0) {
          ABCTableRow model = getRowModel(rowIndex);
          nextCell = model.getPreviousEditableCell(key == KeyEvent.VK_KP_UP);
          if(nextCell != null)
            break;
          rowIndex--;
        }
      } else {
        while(rowIndex < getRowCount()) {
          ABCTableRow model = getRowModel(rowIndex);
          nextCell = model.getNextEditableCell(key == KeyEvent.VK_KP_DOWN);
          if(nextCell != null)
            break;
          rowIndex++;
        }
      }			
      if(nextCell != null) {
        // TODO: Find a better way around this hack...
        if(nextCell instanceof ABCComboBox) {
           ((ComboBox)nextCell.getComponent()).tabbedTo = true;
        }
        if(table.isEditing())
          table.getCellEditor().stopCellEditing();
        table.changeSelection(rowIndex, nextCell.getStartIndex(), false, false);
        if(table.editCellAt(table.getSelectedRow(), table.getSelectedColumn())) {
          table.getEditorComponent().requestFocus();	
        }
      } else {
        // TODO: this is buggy...
        /*
          int selectedRow = getSelectedRow();
          if(selectedRow >= 0)
            getRowModel(selectedRow).select(null, false); // Unselect previous ABCTableCell
          System.out.println("end of the line... transfering focus to next component");
          abcParent.selectNextComponent(ABCTable.this, selectedRow < rowIndex);	
         */			
      }
    }
  }
  
  public int getEditingRow() {
    int rowCount = 0;
    for(ABCTableRow row: getRows()) {
      if(row.isVisible()) {
        if(row.isEditing())
          return rowCount;
        rowCount += row.rowSpan;
      } 
    }
    return -1;
  }

  public ABCTableRow newRowModel(NodeWrapper nodeWrapper, ABCTableRow parent, Key group) {
    ABCTableRow model = new ABCTableRow(this, nodeWrapper, parent, group, hijacked, dataProvider, actionProvider);
    if(!model.isEmpty()) {
      return model;
    }		
    return null;
  }

  private ABCTableRow newRowModel(NodeWrapper nodeWrapper, DataItem dataItem) {
    ABCTableRow model = new ABCTableRow(this, nodeWrapper, abcParent, dataItem, hijacked, dataProvider, actionProvider);
    if(!model.isEmpty()) {
      return model;
    }
    return null;
  }

  public ABCTableRow newRowModel(NodeWrapper nodeWrapper) {
    ABCTableRow model = new ABCTableRow(this, nodeWrapper, this.hijacked, dataProvider, actionProvider);
    if(!model.isEmpty()) {
      return model;
    }
    return null;
  }

  public ABCTableRow getRowModel(int rowIndex) {
    int rowCount = -1;
    for(ABCTableRow row: getRows()) {			
      if(row.isVisible())
        rowCount += row.rowSpan;
      if(rowIndex <= rowCount) {
        return row;
      }
    }
    return null;
  }

  public int indexOf(ABCTableRow tableRow) {
    int index = -1;
    for(ABCTableRow row: getRows()) {
      if(row.isVisible())
        index += row.rowSpan;
      if(row.equals(tableRow))
        return index;
    }
    return -1; // Didn't find it
  }

  public int getRowStartIndex(int rowIndex) {
    int rowCount = 0;
    for(ABCTableRow row: getRows()) {
      if(row.isVisible()) {
        if(rowIndex < rowCount + row.rowSpan)
          return rowCount;
        rowCount += row.rowSpan;
      }	
    }
    return -1;
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(super.getPreferredSize().width, getRowCount() * getRowHeight());
  }

  @Override
  public TableCellEditor getCellEditor(int rowIndex, int columnIndex) {
    return getRowModel(rowIndex).getCell(columnIndex);
  }

  @Override
  public TableCellRenderer getCellRenderer(int rowIndex, int columnIndex) {
    return getRowModel(rowIndex).getCell(columnIndex);
  }

  @Override
  public Component initialize(Node node) {
    NodeList children = node.getChildNodes();
    for(int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      addRows(child, null, null, null);
    }
    return this;
  }

  @Override
  public void rebuild() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setDefaults() {
    // TODO Auto-generated method stub

  }

  private void merge(ABCTableRow model) {
    for(DataItem item: model.getItems()) {    
      this.abcItems.add((ABCDataItem)item);
      if(!this.hijacked.contains(((ABCDataItem)item).getHijacked())) // Don't add shared items
        this.hijacked.add(((ABCDataItem)item).getHijacked());
    }
  }

  @Override
  public String toString() {
    return hijacked.toString();
  }

  @Override
  public Map<String, List<String>> getErrors() {
    Map<String, List<String>> errors = new HashMap<String, List<String>>();
    errors.put(identifier, new ArrayList<String>(this.errors));
    return errors;
  }
  
  public List<ABCDataItem> getABCDataItems() {
    return abcItems;
  }

  public List<ABCTableRow> getRows() {
    return rows;
  }

  public DataItemMap getData() {
    return hijacked;
  }

  @Override
  public void transferFocusToFirst() {
    for(int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
      for(int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++) {
        if(this.isCellEditable(rowIndex, columnIndex)) {
          this.changeSelection(rowIndex, columnIndex, false, false);
          if(editCellAt(getSelectedRow(), getSelectedColumn())) {
            getEditorComponent().requestFocus();  
          }
          return;
        }
      }
    }
  }

  @Override
  public void transferFocusToLast() {
    System.out.println("Attempting to select last editable component");
    for(int rowIndex = getRowCount() - 1; rowIndex >= 0; rowIndex--) {
      for(int columnIndex = getColumnCount() -1; columnIndex >= 0; columnIndex--) {
        if(this.isCellEditable(rowIndex, columnIndex)) {
          this.changeSelection(rowIndex, columnIndex, false, false);
          return;
        }
      }
    }		
  }

}
