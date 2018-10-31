package abc.containers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import datamodel.DataModelObservable;

import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import vabc.ABCConstants;
import vabc.IABCDataProvider;
import vabc.ABCStyle;
import vabc.IABCUserObject;
import vabc.StringUtils;
import vabc.SwingUtils;
import abc.table.ABCTable;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable.DataModelChange;
import datamodel.DataModelObservable.DataModelObserver;
import datamodel.Key;
import datamodel.collections.DataItemMap;
import datamodel.collections.RecursiveDataItemMap;

public class ABCExpandedList extends ABCComponent implements DataModelObserver, FocusListener {

  private static final long serialVersionUID = 1L;

  private IABCDataProvider dataProvider;

  private Map<ABCTable, ABCComponent> expandedItems;
  private Map<DataItem, ABCTable> listItems;

  private Node keyNode = null;
  private String keyNodeKey = "expanded_list_item";
  private Node sectionNode = null;

  private JScrollPane leftSideWrapper;  
  private SequentialPanel itemListPanel;
  private JPanel openItemPanel;
  private Separator separator;
  private ABCTable selected;

  private String userObject;
  private IABCUserObject currentObject;
  
  private List<DataItem> sortedOrder;
  
  private boolean autoSort;
  
  public ABCExpandedList(ABC abcParent, String key, String label, Node node, IABCUserObject abcUserObject) {
    super(abcParent, key, label, null, abcUserObject);

    this.dataProvider = abcParent.dataProvider; 
    this.setBorder(null);
    this.autoSort = true;
    
    expandedItems = new HashMap<ABCTable, ABCComponent>();
    listItems = new LinkedHashMap<DataItem, ABCTable>();

    rCollection.addObserver(this);

    NodeList children = node.getChildNodes();
    String listLabel = label;

    if(node.getAttributes().getNamedItem(ABCConstants.Key.AUTO_SORT) != null) {
      autoSort = new Boolean(node.getAttributes().getNamedItem(ABCConstants.Key.AUTO_SORT).getTextContent());
    }
    
    Node userObjectNode = node.hasAttributes() ? node.getAttributes().getNamedItem(ABCConstants.Key.USER_OBJECT) : null;
    this.userObject = userObjectNode != null ? userObjectNode.getNodeValue() : null;

    for(int i = 0; i < children.getLength(); i++) {
      if(children.item(i).getNodeName().equals(ABCConstants.Key.KEY)) {
        // We actually want the key's child
        for(int j = 0; j < children.item(i).getChildNodes().getLength(); j++) {
          if(!children.item(i).getChildNodes().item(j).getNodeName().startsWith("#")) {
            keyNode = children.item(i).getChildNodes().item(j);
            // TODO: is the key required?
            if(keyNode.getAttributes().getNamedItem(ABCConstants.Key.KEY) != null)
              keyNodeKey = keyNode.getAttributes().getNamedItem(ABCConstants.Key.KEY).getNodeValue();	
            if(keyNode.getAttributes().getNamedItem(ABCConstants.Key.LABEL) != null)
              listLabel = keyNode.getAttributes().getNamedItem(ABCConstants.Key.LABEL).getNodeValue();	
          }
        }
      }

      if(children.item(i).getNodeName().equals(ABCConstants.Key.SECTION))
        sectionNode = children.item(i);
    }

    JButton addButton = new JButton();
    addButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {				
        if(userObject != null) {
          IABCDataProvider provider = ABCExpandedList.this.abcParent.dataProvider;
          if(ABCExpandedList.super.getUserObject() != null && ABCExpandedList.super.getUserObject() instanceof IABCDataProvider) {
            provider = (IABCDataProvider) ABCExpandedList.super.getUserObject();
          }
          Object potentialUserObject = provider.getObject(userObject);
          if(potentialUserObject instanceof IABCUserObject) {
            currentObject = (IABCUserObject)potentialUserObject;       
          } else {
            currentObject = new DataItemMap(new DataItem(new Key(keyNodeKey), null, null, UUID.randomUUID().toString()));
          }
        } else {
          currentObject = new DataItemMap(new DataItem(new Key(keyNodeKey), null, null, UUID.randomUUID().toString()));
        }        
        addItem(true);  
      }
    });		

    addButton.setIcon(SwingUtils.getImageIcon("add", 16));		

    JButton deleteButton = new JButton();
    deleteButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(selected != null)
          remove(selected.getData().first());
      }
    });		
    deleteButton.setIcon(SwingUtils.getImageIcon("delete", 16));	

    JButton copyButton = new JButton();
    copyButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

        if(selected != null && selected.userObject != null) {
          
          // Request a copy
          IABCUserObject copy = selected.userObject.copy();
          
          IABCDataProvider provider = ABCExpandedList.this.abcParent.dataProvider;
          if(ABCExpandedList.super.getUserObject() != null && ABCExpandedList.super.getUserObject() instanceof IABCDataProvider) {
            provider = (IABCDataProvider) ABCExpandedList.super.getUserObject();
          }
          
          // Register the new object
          provider.addObject(copy);
         
          currentObject = copy;
          addItem(true);
          
        }        
      }
    });		

    copyButton.setIcon(SwingUtils.getImageIcon("copy", 16));	

    final String expandedListLabel = label;
    JButton summaryButton = new JButton();
    summaryButton.setToolTipText("Open summary view");
    summaryButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {

        JFrame jFrame = new JFrame();
        jFrame.setTitle(userObject != null ? new Key(userObject, null).getAlias() + " summary" : "Summary");
        jFrame.setSize(new Dimension(400, 600));
        jFrame.setLocationRelativeTo(ABCExpandedList.this);
        jFrame.setLayout(new BorderLayout());
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        JTextPane textPane = new JTextPane();        
        textPane.setContentType("text/html");
        textPane.setEditable(false);
        StringBuffer text = new StringBuffer();
        text.append("<html>");
        if(sortedOrder != null) {
          for(DataItem item: sortedOrder) {
           text.append("<b>" + item.getValue() + "</b><br><hr>"); 
           RecursiveDataItemMap data = ABCExpandedList.this.rCollection.get(item);
           text.append(StringUtils.collectionToHTML(expandedItems.get(listItems.get(item)), data, dataProvider, "&nbsp&nbsp"));           
          }
        }
        text.append("</html>");
        textPane.setFont(ABCStyle.style().getDefaultFont());
        textPane.setText(text.toString());
        jFrame.add(new JScrollPane(textPane));
        jFrame.setVisible(true);
      }
    }); 
    summaryButton.setIcon(SwingUtils.getImageIcon("document_find", 16)); 
    
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(addButton);
    toolBar.add(copyButton);
    toolBar.add(deleteButton);
    toolBar.add(summaryButton);

    itemListPanel = new SequentialPanel() {
      private static final long serialVersionUID = 1L;
      @Override
      public Dimension getPreferredSize() { 
        int leftSideHeight = expandedItems.size() * (ABCConstants.ROW_HEIGHT);
        int rightSideHeight = openItemPanel.getPreferredSize().height - 26; 
        return new Dimension(super.getPreferredSize().width, Math.max(leftSideHeight, rightSideHeight));
      }
    };

    JLabel jLabel = new JLabel(listLabel);
    JPanel leftSide = new JPanel();
    javax.swing.GroupLayout leftMarginLayout = new javax.swing.GroupLayout(leftSide);
    leftSide.setLayout(leftMarginLayout);
    leftSide.setPreferredSize(new Dimension(160, 180));

    leftSideWrapper = new JScrollPane(itemListPanel);
    leftSideWrapper.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if(separator != null)
          separator.repaint();
      }      
    });
    leftSideWrapper.getVerticalScrollBar().setUnitIncrement(9);
    leftSideWrapper.setBorder(null);
    leftSideWrapper.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    leftSideWrapper.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ABCStyle.style().getBorderColor()));

    leftMarginLayout.setHorizontalGroup(
        leftMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, leftMarginLayout.createSequentialGroup()
            .addGroup(leftMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addGroup(leftMarginLayout.createSequentialGroup()
                    .addGap(2)
                    .addComponent(jLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                    .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(leftSideWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            ));

    leftMarginLayout.setVerticalGroup(
        leftMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(leftMarginLayout.createSequentialGroup()
            .addGroup(leftMarginLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(leftSideWrapper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            ));

    openItemPanel = new JPanel();		
    JPanel itemWrapper = new JPanel();
    itemWrapper.setLayout(new BorderLayout());    
    openItemPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(toolBar.getPreferredSize().height, 0, 0, 0),
        BorderFactory.createMatteBorder(1, 1, 0, 1, ABCStyle.style().getBorderColor())));   
    itemWrapper.add(openItemPanel);  
    Color bg = ABCStyle.style().getBackgroundColor();    

    setBackground(bg);
    itemListPanel.setBackground(bg);
    leftSide.setBackground(bg);
    openItemPanel.setBackground(bg);
    toolBar.setBackground(bg);
    addButton.setBackground(bg);
    copyButton.setBackground(bg);
    deleteButton.setBackground(bg);
    itemWrapper.setBackground(bg);

    separator = new Separator();			
    separator.setOrientation(javax.swing.SwingConstants.VERTICAL);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(this);
    this.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(leftSide, 200, 200, 200)
            .addComponent(separator, 13, 13, 13)
            .addComponent(itemWrapper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            )
        );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)         
                .addComponent(itemWrapper, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
               .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)           
                .addComponent(leftSide, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
            )
        );

    // Initialize?

    // Grab the initial list and add them??
    if(userObject != null) {
      IABCDataProvider provider = dataProvider;
      if(super.getUserObject() != null && super.getUserObject() instanceof IABCDataProvider) {
        provider = (IABCDataProvider)super.getUserObject();
      }

      Object[] objects = provider.getObjects(userObject);
      for(Object obj: objects) {
        if(obj instanceof IABCUserObject) {
          currentObject = ((IABCUserObject)obj);
          addItem(selected == null);  
        }
      }
    }  
    
    itemListPanel.addComponent(null, null, false, false);
   
  }
  
  @Override
  public IABCUserObject getUserObject() {
    return currentObject;
  } 

  @Override
  public Dimension getPreferredSize() {
    return openItemPanel.getPreferredSize();
  }

  private class SequentialPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    Map<DataItem, ABCTable> rows;

    public SequentialPanel() {
      rows = new LinkedHashMap<DataItem, ABCTable>();
    }

    @Override
    public void remove(Component component) {
      super.remove(component);
      for(DataItem item: new ArrayList<DataItem>(rows.keySet()))
        if(rows.get(item).equals(component)) rows.remove(item);
    }

    public void addComponent(DataItem dataItem, ABCTable newTable, boolean expandVertically, boolean select)  {
      
    //  long startTime = System.currentTimeMillis();
      
      int editingRow = -1;
      int editingColumn = -1;
      if(selected != null) {
        editingRow = selected.getEditingRow();
        editingColumn = selected.getEditingColumn();
      }
                  
      // Only update if the change causes the ordering to change...
      
      if(sortedOrder != null && newTable == null) {
        List<DataItem> items = new ArrayList<DataItem>(rows.keySet());
        if(autoSort)
          Collections.sort(items);  
        // Make sure our previous list and our new list are the same size
        boolean sameOrder = sortedOrder.size() == items.size();
        if(sameOrder) { 
          // If they are then check that they are stored the same way
          for(int i = 0; i < sortedOrder.size(); i++) {
            if(!sortedOrder.get(i).equals(items.get(i))) {
              sameOrder = false;
              break;
            }
          }
        }
        if(sameOrder) {
          return;
        }        
      }
      
      this.removeAll(); // Clear the panel
      /*
       * Reset layout
       */
      GroupLayout groupLayout = new GroupLayout(this);
      this.setLayout(groupLayout);
      ParallelGroup parallelGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
      SequentialGroup sequentialGroup = groupLayout.createSequentialGroup();
      groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(groupLayout.createSequentialGroup().addGroup(parallelGroup)));
      groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
          .addGroup(sequentialGroup));

      // Insert the new item
      if(newTable != null) rows.put(dataItem, newTable); 

      // Grab the keys and sort time
      sortedOrder = new ArrayList<DataItem>(rows.keySet());
      if(autoSort)
        Collections.sort(sortedOrder);	

      // Put the items back in order
      for(DataItem item: sortedOrder) {
        ABCTable table = rows.get(item);
        parallelGroup.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
        sequentialGroup.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, expandVertically ? Short.MAX_VALUE : GroupLayout.PREFERRED_SIZE);
      }

      this.validate();
      this.repaint();
      
      // If we're adding a new table      
      if(select && newTable != null) {
        
        if(selected != null) {
          // Stop editing the current one (in case there is one)
          selected.setBackgroundSelected(false);
          selected.stopEditing();  
        }
        
        // Edit the new one
        newTable.setBackgroundSelected(true);
        newTable.startEditing();         
        selected = newTable;            
        
      } else if(selected != null) {
        
        // Make sure we're still editing the other one
        selected.setBackgroundSelected(true); // (should be?)
        selected.editCellAt(editingRow, editingColumn);
      }     

      separator.showUIForSelectedItem();
      
     // long endTime = System.currentTimeMillis();
     // long duration = (endTime - startTime);  
      // System.out.println("Duration: " + duration); 40 ms for a bunch of times...
    } 
  }

  private class Separator extends JSeparator {
    private static final long serialVersionUID = -3236128112402860875L;

    @Override
    public void paint(Graphics g) {
      int height = this.getHeight();
      g.setColor(ABCStyle.style().getBorderColor());
      for(int h = 0; h < height; h+= 10) {
        g.drawLine(7, h, 7, h+5);	// Draw a dotted line
      }

      // Find the location of the selected list item?
      if(selected != null) {
        int y1 = selected.getY()+32-leftSideWrapper.getViewport().getViewPosition().y;
        int y2 = selected.getY()+33-leftSideWrapper.getViewport().getViewPosition().y;
        if(y1 > 24) {
          g.setColor(ABCStyle.style().getBorderColor());
          g.drawRect(-1, y1, getWidth()+2, selected.getHeight()-12);		
          g.setColor(ABCStyle.style().getSelectionColor());
          g.drawRect(-1, y2, getWidth()+2, selected.getHeight()-14);		
        }
      }

    }
    public void showUIForSelectedItem() {
      openItemPanel.removeAll();
      openItemPanel.setLayout(new BorderLayout());
      if(selected != null) {
        openItemPanel.add(expandedItems.get(selected));
      }
      openItemPanel.revalidate();
      openItemPanel.repaint();
      this.revalidate();
      this.repaint();
    }
  }

  private void addItem(boolean select) {
    
    DataItem identifier = this.currentObject.getIdentifier();
       
    // Create the list component
    ABCTable keyTable = new ABCTable(ABCExpandedList.this, keyNode, identifier, dataProvider, abcParent.actionProvider); 
    keyTable.hideLabel();
    keyTable.addFocusListener(ABCExpandedList.this);

    // Create the view component
    ABCComponent view = new ABC(identifier, sectionNode, dataProvider, abcParent.errorHandler, abcParent.actionProvider, abcParent).getView();
    view.setBorder(null);

    // Add to maps
    expandedItems.put(keyTable, view);  
    listItems.put(keyTable.getData().first(), keyTable);

    // Add to primitives map
    primitives.add(keyTable);

    // Add to UI
    ABCExpandedList.this.addComponent(view); 

    itemListPanel.addComponent(keyTable.getData().first(), keyTable, false, select);
   
    if(select)
      keyTable.requestFocus();     
      
  }

  private void remove(DataItem identifier) {

    // Find the right item
    ABCTable table = null;// listItems.get(change.childId);
    ABCTable next = null;
    for(DataItem item: listItems.keySet()) {
      if(identifier.matches(item)) {
        table = listItems.get(item);
        break;
      }
    }

    // Select the next one?
    if(sortedOrder != null) {
      int indexOf = sortedOrder.indexOf(identifier);
      if(indexOf >= 0) {
        sortedOrder.remove(indexOf);
        if(sortedOrder.size() > indexOf) {
          next = listItems.get(sortedOrder.get(indexOf));       
        } else if(indexOf > 0 && sortedOrder.size() > indexOf -1) {
          next = listItems.get(sortedOrder.get(indexOf-1));          
        }
      }
    }
    
    expandedItems.get(table);
    ABCExpandedList.this.getABCComponents().remove(expandedItems.get(table));
    //select(null);
    itemListPanel.remove(table);
    itemListPanel.validate();
    itemListPanel.repaint();

    this.getRCollection().remove(identifier,  ABCExpandedList.this);
    primitives.remove(table);

    // Remove from maps
    expandedItems.remove(table);
    listItems.remove(table.getData().first());
   
    
    // request it gets removed from the provider
    IABCDataProvider provider = dataProvider;
    if(ABCExpandedList.super.getUserObject() != null && ABCExpandedList.super.getUserObject() instanceof IABCDataProvider) {
      provider = (IABCDataProvider)ABCExpandedList.super.getUserObject();
    }
    provider.removeObject(table.userObject);

    this.selected = next;
    
    if(selected != null) {
      selected.startEditing(); // Should we start editing it?
      selected.setBackgroundSelected(true);
    } 
    
    separator.showUIForSelectedItem();     
    
  }

  @Override
  public void update(DataModelObservable observable, DataModelChange dataModelChange) {

    if(dataModelChange.getSource().equals(this))
      return;

    if(observable instanceof DataItem) {

      if(dataModelChange.getChange().equals(DataItem.VALUE) || dataModelChange.getChange().equals(DataItem.UNIT)) {
        if(listItems.containsKey(observable))
          itemListPanel.addComponent((DataItem)observable, null, false, false);	// Update the ordering...	
      }

    } else if(dataModelChange.getChange().equals(RecursiveDataItemMap.CHILD_COLLECTIONS)) {

      if(!observable.equals(rCollection))  // One of our children, they will handle the event
        return; // Not our level

      if(dataModelChange.getDetail().equals(RecursiveDataItemMap.ADDED)) {

        if(dataModelChange.getNewValue() instanceof IABCUserObject) {
          
          currentObject =  (IABCUserObject)dataModelChange.getNewValue();
          addItem(selected == null);
          
        }        
        
      } else if(dataModelChange.getDetail().equals(RecursiveDataItemMap.REMOVED)) {

        remove((DataItem)dataModelChange.getOldValue());

      }
    }
  }

  @Override
  public void focusGained(FocusEvent e) {

    Object source = e.getSource();
    if(!(source instanceof JComponent)) {
      System.out.println("TODO: Debug ABCExpandedList:396" + source);
      return;
    }
   
    Component table = (Component)source;
    while(!(table instanceof ABCTable)) {
    	table = (Component) table.getParent();
    	if(table == null)
    		return; // Failed...
    }

    ABCTable newSelection = (ABCTable)table;
    
    if(newSelection == selected)
      return; // Do nothing
    
    if(selected != null) {
      selected.setBackgroundSelected(false);
      selected.stopEditing();
    }    
    
    selected = newSelection;
    
    if(selected != null)
      selected.setBackgroundSelected(true);

    separator.showUIForSelectedItem();
  }

  @Override public void focusLost(FocusEvent e) { 
    
    // Our editor loses focus when we resort some times, catch it and give focus back
    if(selected != null) {
      Component editor = selected.getEditorComponent();
      if(editor != null) {
        editor.requestFocusInWindow();
      }      
    }
    
  }
  @Override public void addComponentToUI(ABCComponent component) { }

  @Override
  public boolean shouldObserve(DataModelObservable observable) {

    // Observe any maps or recursive maps
    if(observable instanceof DataItemMap || observable instanceof RecursiveDataItemMap) {
      return true;
    }

    Object[] items = listItems.keySet().toArray();
    for(int i = 0; i < items.length; i++) {
      DataItem listItem = (DataItem) items[i];
      if(observable.equals(listItem))
        return !(observable instanceof ABCDataItem);
    }

    if(observable instanceof ABCDataItem) {
      for(DataItem child: rCollection.keySet()) {
        if(rCollection.get(child).contains(((ABCDataItem)observable).getHijacked())) {
          ((ABCDataItem)observable).addParentItem(child);
        }
      }
      return false; // Don't need to observe these
    }

    return true;
  }

  @Override
  public void startObserving(DataModelObservable observable) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stopObserving(DataModelObservable observable) {
    // TODO Auto-generated method stub

  }
  

  @Override
  public boolean isDynamic() {
    return true;
  }


}	
