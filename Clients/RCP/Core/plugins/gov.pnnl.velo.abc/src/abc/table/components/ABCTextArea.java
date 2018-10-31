package abc.table.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;
import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;

public class ABCTextArea extends ABCTableCell {

  private static final long serialVersionUID = 1L;

  private TextArea textArea;

  public ABCTextArea(ABCDataItem data, int startIndex, int columnSpan, int rowSpan) {
    super(new JTextField(), data, startIndex, columnSpan, rowSpan);
    textArea = new TextArea(data);		
    JScrollPane scrollPane = new JScrollPane(textArea);
    // Redirect focus to the text editor
    scrollPane.addFocusListener(new FocusListener() {
      @Override public void focusGained(FocusEvent e) {	textArea.requestFocus(); }
      @Override public void focusLost(FocusEvent e) {	}			
    });
    scrollPane.setBorder(null);	
    editorComponent = scrollPane;		
    super.initialize();	
  }

  @Override
  public void updateData(String change) {
    textArea.update(change);
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    super.paintComponent(observable, change);
    // TODO Auto-generated method stub

  }

  @Override 
  public boolean stopCellEditing() {
    boolean editingStopped = super.stopCellEditing();
    textArea.stopEditing();
    return editingStopped;
  }

  private static class TextArea extends JTextArea {

    private static final long serialVersionUID = 1L;

    private final ABCDataItem data;
    private boolean internalUpdate = false;

    public TextArea(ABCDataItem data) {

      this.data = data;
      this.setToolTipText(data.getTooltip());     

      super.setText(data.getValue());

      this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

      // Disable the enter key, table parent will handle it
      InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "doNothing");

      this.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          internalUpdate = true;
          TextArea.this.data.getHijacked().setValue(getText(), TextArea.this);		
          internalUpdate = false;	
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          internalUpdate = true;
          TextArea.this.data.getHijacked().setValue(getText(), TextArea.this);					
          internalUpdate = false;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          internalUpdate = true;
          TextArea.this.data.getHijacked().setValue(getText(), TextArea.this);					
          internalUpdate = false;
        }				
      });

      setWrapStyleWord(true);
      setLineWrap(true);
    }

    @Override 
    public void setText(String text) {  }

    public void stopEditing() {
      // UI isn't updating correctly, hide any selection or caret
      getCaret().setSelectionVisible(false);
      getCaret().setVisible(false);
    }

    public void update(String source) {
      if(internalUpdate)
        return; // Nothing to do
      if(source.equals(DataItem.VALUE)) {
        super.setText(data.getValue()); 
      }
    }
  }


}
