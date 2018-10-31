package abc.table.components;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JTextField;

import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import vabc.ABCConstants;
import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;

public class ABCLabel extends ABCTableCell {

  private static final long serialVersionUID = 1L;
  ABCDataItem dataItem;

  public ABCLabel(int startIndex, int columnSpan, int rowSpan) {
    super(new TextField(null), (abc.validation.ABCDataItem)null, startIndex, columnSpan, rowSpan);
  }

  public ABCLabel(ABCDataItem dataItem, int startIndex, int columnSpan, int rowSpan) {
    super(new TextField(dataItem), null, startIndex, columnSpan, rowSpan);
    this.dataItem = dataItem;
    dataItem.addObserver(this);
  }

  @Override
  public void updateData(String change) {
    if(change.equals(DataItem.ALIAS)) {
      ((TextField)getComponent()).setText(dataItem.getAlias());
    }    
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    JComponent component = (JComponent) getComponent();
    if(dataItem != null) {
      component.setFont(dataItem.getLabelFont());
      component.setForeground(dataItem.getLabelColor());
      component.setBackground(dataItem.getLabelBackgroundColor());
    }
  } 

  @Override
  public boolean shouldObserve(DataModelObservable observable) {
    return observable.equals(dataItem);
  }

  @Override
  public boolean isEditable() {
    return false;
  }

  public static class TextField extends JTextField {		

    private static final long serialVersionUID = 1L;

    public TextField(ABCDataItem dataItem) {
      setText(dataItem != null ? dataItem.getAlias() : "");
      setToolTipText(dataItem != null ? dataItem.getTooltip() : null);     
      setBorder(null);
      ABCStyle style = ABCStyle.style();
      setHorizontalAlignment(JTextField.RIGHT);      
      setBackground(ABCStyle.style().getBackgroundColor());
      setFont(dataItem != null && dataItem.isRequired() ? style.getRequiredFont() : style.getOptionalFont());
      setForeground(dataItem != null && dataItem.isRequired() ? style.getRequiredColor() : style.getOptionalColor());
      // Custom for set labels
      if(dataItem != null && dataItem.getKey().getKey().equals(ABCConstants.Key.SET_LABEL)) {
        setFont(style.getRequiredFont());
        setHorizontalAlignment(JTextField.LEFT);        
      }
    }    
  }
}
