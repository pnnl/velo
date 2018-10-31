package gov.pnnl.velo.tools.ui.abc.panels;

import java.awt.BorderLayout;

import javax.swing.JComponent;

import vabc.ABCDocument;
import vabc.IABCActionProvider;
import vabc.IABCDataProvider;
import vabc.IABCErrorHandler;
import abc.containers.ABC;
import abc.test.ABCExample;
import datamodel.Key;
import gov.pnnl.velo.tools.ui.abc.ToolUIABCDefault;

/**
 * Tool used for auto generated UI panels
 * NOTE: To use this panel, the simulationTool (ToolUISwingDefault)
 *  	 	 MUST implement the abc.Container interface!
 * @author port091
 *
 */
public class ABCToolPanel extends AbstractToolPanel {

  private static final long serialVersionUID = 1L;

  private JComponent abcComponent;
  
  private IABCDataProvider dataProvider;
  private IABCActionProvider actionProvider;
  private IABCErrorHandler errorHandler;
  
  private ABCDocument abcDocument;

  public ABCToolPanel(Key key) {
    super(key);
  }

  @Override
  public void initializePanel(final ToolUIABCDefault tool) {

    dataProvider = tool.getDataProvider();
    actionProvider = tool.getActionProvider();
    errorHandler = tool;
 
    abcDocument = tool.getDocument();
    

    if(dataProvider == null || abcDocument == null) {
      ABCExample example = new ABCExample((IABCErrorHandler)tool);
      dataProvider = example;      // Do the example
      actionProvider = example;
      abcDocument = example.getDocument();
    } 

    abcComponent = new ABC(abcDocument, identifier, dataProvider, errorHandler, actionProvider);
    setLayout(new BorderLayout());
    add(abcComponent);

  }

  @Override
  public boolean hasErrors() {
    return false; // TODO
  }

  @Override
  public boolean isComplete() {
    return !hasErrors();  // Child classes might want to change this
  }
}
