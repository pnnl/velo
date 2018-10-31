package abc.table.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import datamodel.DataItem;
import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import vabc.ABCStyle;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;

public class ABCTextField extends ABCTableCell {

	private static final long serialVersionUID = 1L;

	public ABCTextField(ABCDataItem data, int startIndex, int columnSpan, int rowSpan) {
		super(new TextField(data), data, startIndex, columnSpan, rowSpan);
		super.initialize();
	}
	

  @Override
  public void updateData(String change) {
    ((TextField)getComponent()).update(change);    
  }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    super.paintComponent(observable, change);
    ((TextField)getComponent()).setBackground(primaryItem.getFieldBackgroundColor()); 
  }
  
	private static class TextField extends JTextField implements ABCCellEditor {

		private static final long serialVersionUID = 1L;
		private JDialog popupEditor = null;
		private JTextField popupField = null;
		private boolean keyEscape = false;
		private final ABCDataItem data;
		private boolean internalUpdate = false;
		
		@Override
		public void setEnabled(boolean shouldEnable) {
			super.setEnabled(shouldEnable);
			this.setBackground(shouldEnable ? ABCStyle.style().getFieldBackgroundColor() : ABCStyle.style().getFieldDisabledColor());
		}
		
		public TextField(ABCDataItem data) {
			this.data = data;
			
			this.setToolTipText(data.getTooltip());	
			setCaret(new DefaultCaret());
			
			super.setText(data.getValue());	
			setForeground(data instanceof ABCDataItem && ((ABCDataItem)data).isDefaultValue() ? ABCStyle.style().getDefaultColor() : ABCStyle.style().getFieldForegroundColor());
			setBackground(ABCStyle.style().getFieldBackgroundColor());
						
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(ABCStyle.style().getFieldBorderColor(), 1),
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	
			// Disable the enter key, table parent will handle it
			InputMap im = getInputMap(JComponent.WHEN_FOCUSED);
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doNothing");	
			this.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void insertUpdate(DocumentEvent e) {	
				  if(internalUpdate)
				    return;
				  TextField.this.data.setIsFile(false, TextField.this);
					TextField.this.data.getHijacked().setValue(getText(), TextField.this);											
				}

				@Override
				public void removeUpdate(DocumentEvent e) {	
				  if(internalUpdate)
				    return;
          TextField.this.data.setIsFile(false, TextField.this);
					TextField.this.data.getHijacked().setValue(getText(), TextField.this);	
				}

				@Override
				public void changedUpdate(DocumentEvent e) {	
				  if(internalUpdate)
            return;
          TextField.this.data.setIsFile(false, TextField.this);
					TextField.this.data.getHijacked().setValue(getText(), TextField.this);		
				}				
			});
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount() == 3) {
						if(popupEditor == null) {
							createPopupEditor();
						}
						// In case the dialog moved
						popupEditor.setLocationRelativeTo(TextField.this);
						popupField.setText(TextField.this.data.getValue());
						popupEditor.setVisible(true);
					}
				}
			});

		}
		
		public void update(String change) {		  
		  if(change.equals(DataItem.VALUE) || change.equals(ABCDataItem.COULD_BE_FILE) || change.equals(ABCDataItem.IS_FILE)) {
		    String value = data.getHijacked().getValue();
		    if(value == null)
		      value = "";
	      if(data.couldBeFile() && data.isFile()) {
	        String fileName = new File(value).getName();
	        if(!fileName.equals(super.getText())) {
	          internalUpdate = true;
	          super.setText(fileName);
	          internalUpdate = false;
	        }
	      } else {
	        if(!value.equals(super.getText())) {
	          internalUpdate = true;
	          super.setText(value); 
	          internalUpdate = false;
	        }
	      }
	    }
		}

		@Override 
		public void setText(String text) {  }
		
		private void createPopupEditor() {
			popupEditor = new JDialog(); 			
			popupField = new JTextField();
			JPanel contentPane = new JPanel();
			popupField.addKeyListener(new KeyListener() {
				@Override public void keyTyped(KeyEvent e) {}
				@Override public void keyPressed(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) { // Handle enter key and escape key					
					if(e.getKeyCode() == KeyEvent.VK_ENTER) { // Enter was pressed, treat it as a save						
						// If it was a file, check if the new value is a valid path					
					  if(data.couldBeFile()) {
					    // Check to see if the change is still a file
					    File newFile = new File(popupField.getText());
					    if(newFile.exists()) {
					      internalUpdate = true;
					      TextField.super.setText(newFile.getName());
					      internalUpdate = false;
					      data.setValue(popupField.getText(), TextField.this, true);
					    } else {
					      // Not a file, turn of is file
					      internalUpdate = true;
                TextField.super.setText(popupField.getText());
                internalUpdate = false; 
                data.setValue(popupField.getText(), TextField.this, false);
					    }
					  }
						popupEditor.setVisible(false);
						keyEscape = true;
					} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) { // Escape was pressed, don't save changes						
						popupEditor.setVisible(false);
						keyEscape = true;
					} 
				}				
			});
			popupField.addFocusListener(new FocusListener() {
				@Override public void focusGained(FocusEvent e) { }
				@Override
				public void focusLost(FocusEvent e) {
					if(keyEscape) {
						// Don't do anything this time
						keyEscape = false;
						return;
					}
					if(data.couldBeFile() && data.isFile() && new File(popupField.getText()).exists()) {
						data.setValue(popupField.getText(), TextField.this, true);	// Let the change go through						
					} else {
						TextField.super.setText(popupField.getText());
					}
					popupEditor.setVisible(false);
				}				
			});
			
			addFocusListener(new FocusListener() {
				@Override public void focusGained(FocusEvent e) {  }
				@Override public void focusLost(FocusEvent e) {	stopEditing(); }				
			});
						
			popupEditor.setBackground(ABCStyle.style().getFieldBackgroundColor());
			contentPane.setBackground(ABCStyle.style().getFieldBackgroundColor());
						
			contentPane.setLayout(new BorderLayout());			
			contentPane.add(popupField);
			
			popupEditor.setContentPane(contentPane);			
			popupEditor.setUndecorated(true);			
			popupEditor.setSize(new Dimension(600, 30));
		}
	
		public void stopEditing() {
			getCaret().setSelectionVisible(false);
			getCaret().setVisible(false);
		}
	}}
