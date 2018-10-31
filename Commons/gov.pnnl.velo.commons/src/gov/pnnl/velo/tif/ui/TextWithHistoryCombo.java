package gov.pnnl.velo.tif.ui;

import java.awt.Component;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

public class TextWithHistoryCombo extends JComboBox {
  private String defaultValue;
  private String historyParameterKey;
  
  public TextWithHistoryCombo(String defaultValue, String historyParameterKey) {
    super();
    this.defaultValue = defaultValue;
    this.historyParameterKey = historyParameterKey;
    
    setRenderer(new ComboBoxRenderer(this));

  }
  
  protected ComboBoxModel getCombModel() {
    // first put the default value as the first item
    Vector<String> values = new Vector<String>();
    values.add(defaultValue);
    
    // now look up the previously entered values from history
    Vector<String> historyValues = getValuesFromHistory();
    values.addAll(historyValues);
    
    String[] valueArray = values.toArray(new String[values.size()]);
    DefaultComboBoxModel model = new DefaultComboBoxModel(valueArray);
    return model;
  }
  
  private Vector<String> getValuesFromHistory() {
    return new Vector<String>();
//    mainPanel.codeDetails.renderer.setRegistryPath(executableStr);
//    mainPanel.codeDetails.executable.addItem(executableStr);
//
//
//    this.simulator = coderegistry.fetch("simulator");
//    String simulatorId = simulator;
//    // simulator - code id of simulator. lookup code in machine.
//    if (simulator != null && !simulator.isEmpty()) { // no path separator
//      // check if path can be got from machine registry. It could be a code
//      // id
//      String pathToSimulator = machine.getCode(simulator);
//      System.out.println("Simulator path from machine config as "
//          + simulatorId);
//      if (pathToSimulator != null && !pathToSimulator.isEmpty()) {
//        //mainPanel.codeDetails.simulatorPath.setText(pathToSimulator);
//        this.registrySim = pathToSimulator;
//        mainPanel.codeDetails.renderer.setRegistryPath(pathToSimulator);
//        mainPanel.codeDetails.simulatorCombo.addItem(pathToSimulator);
//      }
//
//    }

  }

  class ComboBoxRenderer extends JPanel implements ListCellRenderer
  {
    private static final long serialVersionUID = 1L;
    
    String registryPath = null;
    JLabel text;
    public ComboBoxRenderer(JComboBox combo) {
      text = new JLabel();
      text.setOpaque(true);
      text.setFont(combo.getFont());
    }
    public void setRegistryPath(String path){
      registryPath = path;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      
      if (isSelected) {
        setBackground(UIManager.getColor("List.selectionBackground"));
        setForeground(UIManager.getColor("List.selectionForeground"));
        
      } else {
        setBackground(UIManager.getColor("List.background"));
        setForeground(UIManager.getColor("List.foreground"));
      }
      text.setText(value.toString());
      text.setBackground(getBackground());
      text.setForeground(getForeground());
      // TODO: why are we setting weird background colors?
//      if (index>-1 && value.toString().equals(registryPath)) {
//        text.setForeground(Color.BLUE);
//        //text.setFont(new Font(text.getFont().getName(), Font.BOLD, text.getFont().getSize()));
//      }else{
//        text.setForeground(Color.BLACK);
//        //text.setFont(text.getFont());
//      }
      return text;
    }

  }
}
