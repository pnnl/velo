package abc.table.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalComboBoxButton;

import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.validation.ABCComboListItem;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;

public class ABCComboBox extends ABCTableCell {

  private static final long serialVersionUID = 1L;
  private boolean isUnit;

  public ABCComboBox(List<ABCDataItem> unitItems, ABCDataItem data, boolean isUnit, int startIndex, int columnSpan, int rowSpan) {
    super(new ComboBox(unitItems, data, isUnit), data, unitItems, startIndex, columnSpan, rowSpan);
    this.isUnit = isUnit;
    super.initialize();
  } 

  @Override
  public void updateData(String change) {
    if((isUnit && change.equals(DataItem.UNIT)) || (!isUnit && change.equals(DataItem.VALUE))) {
      ((ComboBox)getComponent()).update(change);
    }
  }
  
  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {

    if(observable == null || observable.equals(primaryItem)) {
      ComboBox component = (ComboBox)getComponent();
      // Default border color for mac:
      component.setBorder(BorderFactory.createLineBorder(ABCStyle.style().getBorderColor()));

      // Something else changed, make sure UI is correct        
      if(isUnit) {
        // Someone is drawing over my border...
        component.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.UNIT)));
        component.setForeground(primaryItem.getUnitForegroundColor());
        component.setBackground(primaryItem.getUnitBackgroundColor());
      } else {
        component.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.VALUE)));   
        component.setForeground(primaryItem.getFieldForegroundColor());
        component.setBackground(primaryItem.getFieldBackgroundColor());
      }        
      component.setFont(primaryItem.getFieldFont());

    } else {
      // Ignore events from the combo box items, the renderer should handle these?
    }       
  }

  @SuppressWarnings("rawtypes")
  public static class ComboBox extends JComboBox implements ABCCellEditor {

    private static final long serialVersionUID = 1L;

    private final ABCDataItem data;
    private final boolean isUnit;
    private JList list;

    // Used to allow tabbing between components and single click to open popup... 
    public Boolean tabbedTo = false;
    
    
    @SuppressWarnings("unchecked")
    public ComboBox(List<ABCDataItem> values, ABCDataItem data, boolean isUnit) {
      super(values.toArray());	

      this.data = data;
      this.isUnit = isUnit;
      this.setToolTipText(data.getTooltip());
 //     this.setOpaque(true); // for mac
      this.setBorder(BorderFactory.createLineBorder(ABCStyle.style().getFieldBorderColor()));
          
      this.setBackground(ABCStyle.style().getFieldBackgroundColor());
      if(isUnit) {
        this.setForeground(data.getUnitForegroundColor());
      } else {
        this.setForeground(data.getFieldForegroundColor());
      }
      
      if(isUnit) {
        if(data.getUnit() != null)
          this.setSelectedItem(data.getUnit());
        else
          data.getHijacked().setUnit(getSelectedItem().toString(), ComboBox.this);
      } else {
        if(data.getValue() != null) {
          // If we have the string version of the combo box item, we'll change it to the key
          this.setSelectedItem(data.getValue()); 
          if(this.getSelection().getKey().getAlias().equals(data.getValue())) {
            data.getHijacked().setValue(getSelection().getKey().getKey(), ComboBox.this);
          }
        } else
          data.getHijacked().setValue(getSelection().getKey().getKey(), ComboBox.this);
      }

      this.setRenderer(new DefaultListCellRenderer() {
        private static final long serialVersionUID = 1L;
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
          if(ComboBox.this.list == null) ComboBox.this.list = list;
          Component orig = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
          if(value instanceof ABCDataItem) {
            ABCDataItem item = (ABCDataItem)value;					
            ABCDataItem abcdi = (ABCDataItem) ComboBox.this.data;    
            this.setToolTipText(item.getTooltip());      
            boolean isDefault = false;
            boolean isAppDefault = false;            
            if(ComboBox.this.isUnit && abcdi.getDefaultUnit() != null && item.getKey().equals(abcdi.getDefaultUnit()))
              isDefault = true;
            if(!ComboBox.this.isUnit && abcdi.getDefaultValue() != null && item.getKey().equals(abcdi.getDefaultValue()))
              isDefault = true;     
            if(item.getKey().equals(abcdi.getAppDefault()))
              isAppDefault = true;            
            Font font = item.getFieldFont();
            if(isAppDefault)
              font = font.deriveFont(Font.BOLD);            
            Color foreground = ABCStyle.style().getFieldForegroundColor();
            if(!item.isEnabled()) {
              foreground = ABCStyle.style().getTextDisabledColor();
            } else if(isDefault) {
              font = font.deriveFont(Font.ITALIC);
              // foreground = ABCStyle.style().getDefaultColor();              
            }            
            orig.setFont(font);
            orig.setForeground(foreground);
            orig.setBackground(isSelected && item.isEnabled() ? ABCStyle.style().getSelectionColor() : ABCStyle.style().getFieldBackgroundColor());
            if(isPopupVisible() && orig instanceof JLabel && isSelected && item.isEnabled()) {
              ((JLabel)orig).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
            }
            if(!item.isVisible()) { 
              if(isSelected) {
                System.out.println(list.getSelectedIndex());
                System.out.println(index);
              }
              JPanel hidden = new JPanel();
              hidden.setPreferredSize(new Dimension(0, 0));
              return hidden;  // If the item is not visible return an empty panel
            }
          }          
          return orig;
        }
      });
    
      this.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          if(!tabbedTo)
            ComboBox.this.showPopup();
          tabbedTo = false;
        }

        @Override
        public void focusLost(FocusEvent e) {  }        
      });
    }

    
    @Override
    public void paint(Graphics g) {

      int height = getHeight();
      int width = getWidth();
            
      g.setColor(getBackground());
      g.fillRect(0, 0, width, height);      
      getBorder().paintBorder(this, g, 0, 0, width, height);

      // The text
      g.setColor(getForeground());
      g.setFont(getFont());
      
      ((Graphics2D)g).setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
      
      g.drawString(getSelectedItem().toString(), 4, height-8);                 
      
      // Add a triangle
      g.setColor(Color.DARK_GRAY);
      g.fillPolygon(new int[]{width-18, width-10, width-14}, new int[]{height-15, height-15, height-11}, 3);
    }
    
    @Override 
    public void setSelectedItem(Object item) { 
      if(item == null) return; // Nothing to update to   
      
      ABCDataItem currentSelection = getSelection();      
      if(currentSelection.equals(item)) return; // No change was made
      
      ABCDataItem newSelection = null;  // Find the new selection
      for(int i = 0; i < getItemCount(); i++) {
        ABCDataItem comboItem = (ABCDataItem)getItemAt(i);
        if(item instanceof ABCDataItem) {
          if(comboItem.equals(item)) {
            newSelection = comboItem;
            break;
          }
        } else {
          if(comboItem.getKey().equals(item)) {
            newSelection = comboItem;
            break;
          }
        }
      }
      
      if(newSelection == null || !newSelection.isEnabled() || !newSelection.isVisible()) 
        return; // Couldn't find the value, it's disabled or it's not visible
      
      // We have a new selection, and we're sure we want to select it
      currentSelection.setGroupSelected(false, ComboBox.this);
      currentSelection.setValue(new Boolean(false).toString(), ABCComboListItem.LIST_ITEM, ComboBox.this);
      
      newSelection.setGroupSelected(true, ComboBox.this);
      newSelection.setValue(new Boolean(true).toString(), ABCComboListItem.LIST_ITEM, ComboBox.this);
            
      // Update the backing data model
      super.setSelectedItem(newSelection);
      
      // Update the primary item
      if(isUnit) {
        ComboBox.this.data.getHijacked().setUnit(newSelection.getAlias(), ComboBox.this);
      } else {
        ComboBox.this.data.getHijacked().setValue(newSelection.getKey().getKey(), ComboBox.this);
      }   
    }
    
    public void update(String change) {
      // We will have already validated the change at the ABCComboBox level, so we know we need to update
      if(isUnit) {
        setSelectedItem(data.getUnit());	
      } else {
        setSelectedItem(data.getValue());
      }
    }

    public void stopEditing() {
      // Needed when we tab off an open menu
      if(list != null) setSelectedItem(list.getSelectedValue()); 
    }

    public ABCDataItem getSelection() {
      return  ((ABCDataItem)this.getSelectedItem());
    }
    
    /**
     * Causes table to fire editing stopped event when combo box is closed
     */
    @Override protected void fireActionEvent() { }
    @Override protected void fireItemStateChanged(ItemEvent e) { }
    
  }
  
  @Override
  public boolean isSelected() {
    if(!isUnit)
      return super.isSelected();
    return primaryItem.isUnitSelected();
  }

  @Override
  public void setSelected(boolean isSelected, Object source) {
    if(!isUnit)
      primaryItem.setValueSelected(isSelected, source);     
    else {
      primaryItem.setUnitSelected(isSelected, source);     
    }
  }

  public ABCDataItem getSelection() {
    return ((ComboBox)getComponent()).getSelection();
  }


}
