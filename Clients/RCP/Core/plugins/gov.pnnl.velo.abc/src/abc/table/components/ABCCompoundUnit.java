package abc.table.components;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;

import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.units.ABCUnitFactory;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;

public class ABCCompoundUnit extends ABCTableCell {

  private static final long serialVersionUID = 1L;

  public ABCCompoundUnit(ABCDataItem data, String unitFamily, int startIndex, int columnSpan, int rowSpan) {
    super(new TextField(data, unitFamily), data, startIndex, columnSpan, rowSpan);		
    super.initialize();
  }

  @Override
  public void updateData(String change) {
    ((TextField)getComponent()).update(change);   
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    if(observable == null || observable.equals(primaryItem)) {
      TextField component = (TextField)getComponent();
      if(change != null && change.getChange().equals(ABCDataItem.IS_SELECTED)) {
        // Borders
        component.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(DataItem.UNIT)),
            BorderFactory.createEmptyBorder(0, 3, 0, 0)));
      } else {
        // Something else changed, make sure UI is correct        
        component.setForeground(primaryItem.getUnitForegroundColor());
        component.setBackground(primaryItem.getUnitBackgroundColor());
        component.setFont(primaryItem.getFieldFont());
      }   
    } else {
      // Ignore events from the combo box items, the renderer should handle these?
    }
  }

  private static class TextField extends JTextField implements ABCCellEditor {

    private static final long serialVersionUID = 1L;
    private JWindow popupEditor = null;
    private final ABCDataItem data;
    private String unitFamily;
    private CloseKeyAdapter closeAdapter;
    private List<Object> unitObjects;
    private String onOpenUnit = "";
    
    public TextField(ABCDataItem data, String unitFamily) {
      this.data = data;
      this.unitFamily = unitFamily;
      unitObjects = new ArrayList<Object>();
      closeAdapter = new CloseKeyAdapter();
      
      this.setToolTipText(data.getTooltip());      
      this.setEditable(false);	
      super.setText(data.getUnit());	

      this.setSelectionColor(ABCStyle.style().getFieldBackgroundColor());

      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ABCStyle.style().getFieldBorderColor()),
          BorderFactory.createEmptyBorder(0, 3, 0, 0)));

      addFocusListener(new FocusListener() {
        @Override public void focusGained(FocusEvent e) { }
        @Override public void focusLost(FocusEvent e) { 
          if(!e.isTemporary()) {
            closeEditor();
          }
        }	
      });

      InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), new AbstractAction() {				
        private static final long serialVersionUID = 1L;
        @Override
        public void actionPerformed(ActionEvent e) {
          if (popupEditor != null && popupEditor.isVisible())
            closeEditor();
          else
            openEditor();
        }				
      });	

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) { 
          // KLS I was not always getting this event - if first click in field but mouseReleased seemed to work ok
          // so moved call to openEditor
          //openEditor();
        }
        @Override
        public void mouseReleased(MouseEvent e) { 
          openEditor();
        }
      });

    }

    @Override
    public void paint(Graphics g) {
      super.paint(g);
      
      int height = this.getHeight();
      int width = this.getWidth();
            
      // Add a triangle
      g.setColor(Color.DARK_GRAY);
      g.fillPolygon(new int[]{width-18, width-10, width-14}, new int[]{height-15, height-15, height-11}, 3);
    }
    
    @Override 
    public void setText(String text) {  }

    private void createPopupEditor() {
      popupEditor = new JWindow(); 	
     final JPanel contentPane = new JPanel();

      Color bg = ABCStyle.style().getUnitPopupBackground();
      popupEditor.setBackground(bg);
      contentPane.setBackground(bg);

      contentPane.setLayout(new FlowLayout());
      // KLS not sure why but if we have the closeAdapter on the text field,
      // we can't close with return yet we can still open with return?
      // Commented out for now and see if it works on windows also.
      //addKeyListener(closeAdapter);
      
      JComboBox first = null;
      for(Object object: ABCUnitFactory.getABCUnits().getUnitParts(unitFamily)) {
        if(object instanceof Object[]) {
          JComboBox unitCombo = new JComboBox((Object[])object) {
            @Override
            public void paint(Graphics g) {
              super.paint(g);
              boolean karenTryThis = true;
              if(!karenTryThis)
                return;

              int height = this.getPreferredSize().height;
              int width = this.getPreferredSize().width;
              
                    
              g.setColor(getBackground());
              g.fillRect(0, 0, width, height);      
              if (getBorder() != null)
                 getBorder().paintBorder(this, g, 0, 0, width, height);
              else {
                g.setColor(ABCStyle.style().getBorderColor());
                g.drawPolygon(new int[]{1, width-1, width-1, 1}, new int[]{1, 1, height-1, height-1}, 4);
              }

              // The text
              g.setColor(getForeground());
              g.setFont(getFont());
              
              ((Graphics2D)g).setRenderingHint(
                  RenderingHints.KEY_TEXT_ANTIALIASING,
                  RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
              
              g.drawString(getSelectedItem().toString(), 4, height-8);                 
              
              // Add a triangle
              g.setColor(Color.DARK_GRAY);
              g.fillPolygon(new int[]{width-14, width-6, width-10}, new int[]{height-13, height-13, height-9}, 3);
              contentPane.validate();
              contentPane.repaint(); // ??
            }
          };
          unitCombo.setBackground(getBackground()); // Added for mac
          unitCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              String text = ""; // Save the selections
              for(Object object: unitObjects) {
                if(object instanceof JComboBox)
                  text += ((JComboBox)object).getSelectedItem().toString();
                else
                  text += object.toString();
              }
              TextField.this.data.getHijacked().setUnit(text, TextField.this);	
              TextField.super.setText(text);		
            }						
          });
          if(first == null)
            first = unitCombo;
          unitCombo.setFont(ABCStyle.style().getDefaultFont());
          unitCombo.setForeground(ABCStyle.style().getUnitPopupForeground());
          contentPane.add(unitCombo);
          unitCombo.setFocusable(true);
          unitCombo.addKeyListener(closeAdapter);
          unitObjects.add(unitCombo);
        } else if(object instanceof String) {
          JLabel divider = new JLabel((String)object);
          Font font = ABCStyle.style().getDefaultFont();
          divider.setFont(font.deriveFont(font.getSize()+1));  // Should this by a style setting instead??
          divider.setForeground(ABCStyle.style().getUnitPopupForeground());
          contentPane.add(divider);
          unitObjects.add(object);
          divider.addKeyListener(closeAdapter);
        } 
      }
      if(first != null)
        first.requestFocus(); // TODO: focus isn't working on thses combo boxes?
      contentPane.setBorder(BorderFactory.createEtchedBorder());
      popupEditor.setContentPane(contentPane);			
      popupEditor.setSize(contentPane.getPreferredSize());
    }

    public void openEditor() {
      if(!closeAdapter.isEnabled)
        return;
      closeAdapter.isEnabled = false;      
      onOpenUnit = data.getUnit();
      if(popupEditor == null) {
        createPopupEditor();
      }
      String[] parts = data.getUnit().split("[\\*\\/]+");
      int i = 0;
      for(Object object: unitObjects) {
        if(object instanceof JComboBox) {
          ((JComboBox)object).setSelectedItem(parts[i++]);
        }
      }			
      // In case the dialog moved
      popupEditor.setLocationRelativeTo(TextField.this);
      // TODO:	popupEditor.setLocation(popupEditor.getLocation().x + 100, popupEditor.getLocation().y);
      popupEditor.setVisible(true);
      closeAdapter.isEnabled = true;
    }

    public void closeEditor() {
      if(popupEditor == null)
        return; // Nothing to do
      popupEditor.setVisible(false);
    }

    public class CloseKeyAdapter extends KeyAdapter {	
      
      boolean isEnabled = true;
      
      @Override
      public void keyPressed(KeyEvent e) {
        if(!isEnabled)
          return;
        closeAdapter.isEnabled = false;
        if(e.getKeyCode() == KeyEvent.VK_ENTER) { // Enter was pressed, treat it as a save	
          closeEditor();	// TODO: Open is called by something right after this, debug and fix.	
        } else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) { // Escape was pressed, don't save changes		
          if(onOpenUnit != null) {
            TextField.this.data.getHijacked().setUnit(onOpenUnit, TextField.this);	
            TextField.super.setText(onOpenUnit);		
          }
          closeEditor();		
        }
        closeAdapter.isEnabled = true;
      }
    }

    @Override
    public int getSelectionStart() {
      return -1;
    }

    @Override
    public int getSelectionEnd() {
      return -1;
    }

    public void stopEditing() {
      // UI isn't updating correctly, hide any selection or caret
      getCaret().setVisible(false);
    }

    private void update(String change) {
      if(change.equals(DataItem.UNIT)) {
        super.setText(data.getUnit());	
        this.fireActionPerformed();
      }
    }

  }

  @Override
  public boolean isSelected() {
    return primaryItem.isUnitSelected();
  }

  @Override
  public void setSelected(boolean isSelected, Object source) {
    primaryItem.setUnitSelected(isSelected, source);     
  }

}
