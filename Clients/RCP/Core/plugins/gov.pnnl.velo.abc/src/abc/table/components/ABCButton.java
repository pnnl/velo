package abc.table.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import datamodel.DataModelObservable;
import datamodel.DataModelObservable.DataModelChange;
import vabc.ABCStyle;
import vabc.SwingUtils;
import abc.table.ABCTableCell;
import abc.validation.ABCDataItem;

public class ABCButton extends ABCTableCell {

  private static final long serialVersionUID = 1L;

  protected Button button;
  public ABCButton(ABCDataItem primaryItem, int startIndex, int columnSpan, int rowSpan) {
    super(new JCheckBox(), primaryItem, startIndex, columnSpan, rowSpan);
    button = new Button(primaryItem);
    button.setToolTipText(primaryItem.getTooltip());
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        button.launchFileDialog();;
      }      
    });
    initialize();
  }

  @Override public void updateData(String detail) {   }

  @Override
  public void paintComponent(DataModelObservable observable, DataModelChange change) {
    if(observable == null || observable.equals(primaryItem)) {
      if(change != null && change.getChange().equals(ABCDataItem.IS_SELECTED) && change.getDetail().equals(ABCDataItem.BUTTON)) {
        button.setBorder(BorderFactory.createLineBorder(primaryItem.getBorderColor(ABCDataItem.BUTTON)));
      }
    }
    if(change != null && change.getChange().equals(ABCDataItem.COULD_BE_FILE)) {
      button.setVisible(primaryItem.couldBeFile());
    }
  }

  @Override
  public JComponent getComponent() {
    return button;
  }

  public static class Button extends JButton implements ABCCellEditor {

    private ABCDataItem primaryItem;
    private File previousDirectory;
    //	private JDialog browserDialog;

    private static final long serialVersionUID = 1L;
    // Disable the enter key, table parent will handle it
    public Button(ABCDataItem primaryItem) {
      super("Browse");
      this.primaryItem = primaryItem;
     // JButton images = SwingUtils.newButton("import", 16, "");

     // this.setIcon(images.getIcon());
     // this.setRolloverIcon(images.getRolloverIcon());
     // this.setPressedIcon(images.getPressedIcon());

    //  setOpaque(true);
    //  setBorderPainted(true);
    //  setContentAreaFilled(false);
    //  setFocusPainted(false);
    //  setBackground(ABCStyle.style().getBackgroundColor());
      
      setEnabled(primaryItem.isEnabled());

      InputMap im = getInputMap(JComponent.WHEN_FOCUSED);	
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "doNothing");
      im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "doClick");

      getActionMap().put("doClick", new AbstractAction() {
        private static final long serialVersionUID = 1L;
        @Override public void actionPerformed(ActionEvent e) { 
          launchFileDialog();
        }		
      });
    }


    private void launchFileDialog() {
      JFileChooser chooser = new JFileChooser("Choose file for " + primaryItem.getAlias().toLowerCase());
      if(previousDirectory != null)
        chooser.setCurrentDirectory(previousDirectory);
      chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(true);			
      if(chooser.showOpenDialog(Button.this) == JFileChooser.APPROVE_OPTION) {
        if(Button.this.primaryItem.couldBeFile())
          Button.this.primaryItem.setValue(((File)chooser.getSelectedFile()).getAbsolutePath(), Button.this, true);
      }
      previousDirectory = chooser.getCurrentDirectory();
    }

    @Override
    public void stopEditing() {   } 

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
