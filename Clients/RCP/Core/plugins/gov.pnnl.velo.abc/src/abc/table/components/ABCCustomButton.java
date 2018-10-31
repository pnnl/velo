package abc.table.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import vabc.IABCAction;
import vabc.ABCStyle;
import vabc.SwingUtils;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;

public class ABCCustomButton extends ABCTableCell {

  private static final long serialVersionUID = 1L;

  protected Button button;

  public ABCCustomButton(ABCDataItem primaryItem, Object object, IABCAction onClick, int startIndex, int columnSpan, int rowSpan) {
    super(new JCheckBox(), primaryItem, startIndex, columnSpan, rowSpan);
    button = new Button(primaryItem, object, onClick);
    button.setToolTipText(primaryItem.getTooltip());
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        button.doAction();
      }      
    });
    initialize();
  }

  @Override
  public void updateData(String change) {  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    if(observable == null || observable.equals(primaryItem)) {
      if(change != null && change.getChange().equals(ABCDataItem.IS_SELECTED) && change.getDetail().equals(ABCDataItem.BUTTON)) {
        button.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(ABCDataItem.BUTTON)));
      }
    }
  }

  @Override
  public JComponent getComponent() {
    return button;
  }

  public static class Button extends JButton implements ABCCellEditor {

    private Object object;    
    private IABCAction abcAction;
    private ABCDataItem primaryItem;

    private static final long serialVersionUID = 1L;
    // Disable the enter key, table parent will handle it
    public Button(ABCDataItem primaryItem, Object object, IABCAction abcAction) {

      this.primaryItem = primaryItem;
      this.abcAction = abcAction;
      this.object = object;
      JButton images = SwingUtils.newButton("edit", 16, "");

      this.setIcon(images.getIcon());
      this.setRolloverIcon(images.getRolloverIcon());
      this.setPressedIcon(images.getPressedIcon());

      setOpaque(true);
      setBorderPainted(true);
      setContentAreaFilled(false);
      setFocusPainted(false);
      setBackground(ABCStyle.style().getBackgroundColor());

      InputMap im = getInputMap(JComponent.WHEN_FOCUSED); 
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "doNothing");
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doClick");

      getActionMap().put("doClick", new AbstractAction() {
        private static final long serialVersionUID = 1L;
        @Override public void actionPerformed(ActionEvent e) { 
          doAction();
        }   
      });      
    }

    public void doAction() {
      System.out.println("Calling do action");
      Button.this.abcAction.doAction(Button.this.object, primaryItem.getHijacked());    
    }

    @Override
    public void stopEditing() {    } 

  }

  @Override
  public boolean isSelected() {
    return primaryItem.isButtonSelected();
  }

  @Override
  public void setSelected(boolean isSelected, Object source) {
    primaryItem.setButtonSelected(isSelected, source);
  }

}
