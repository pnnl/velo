package abc.test;

import gov.pnnl.velo.util.ClasspathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.w3c.dom.Node;

import vabc.IABCAction;
import vabc.ABCDocument;
import vabc.IABCErrorHandler;
import vabc.IABCDataProvider;
import vabc.IABCActionProvider;
import vabc.IABCUserObject;
import abc.containers.ABC;
import datamodel.DataItem;
import datamodel.Key;

public class ABCExample implements IABCDataProvider, IABCErrorHandler, IABCActionProvider {

  ABCDocument abcdoc;
  IABCErrorHandler abcErrorInterface;
  
  List<AgniGroupItem> customItems = new ArrayList<AgniGroupItem>();
  {
    customItems.add(new AgniGroupItem());
    customItems.add(new AgniGroupItem());
  }

  public ABCExample(IABCErrorHandler abcErrorInterface) {
    this.abcErrorInterface = abcErrorInterface;    
  }

  public static void main(String[] args) {
    JFrame testFrame = new JFrame();
    ABCExample example = new ABCExample(null);
    //KLS  remove after testing  ABC abcObject = new ABC(ABC.getNode(new Key("Expanded")), example, example);	
    ABC abcObject = new ABC(example.getDocument(), new Key("ABCExample"), example, example, example);	
    //ABC abcObject = new ABC(abcdoc, "Garbage", example, example);	
    testFrame.add(abcObject);
    testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //testFrame.setSize(600, 600);
    testFrame.pack();
    testFrame.setVisible(true);
  }

  @Override
  public Object[] getDisabledItems(String dataType) {
    return new String[] {};
  }

  @Override
  public Object[] getObjects(String dataType) {
    if (dataType.equals("primary_species"))
    {
      return new Object[]{"p1","p2","p3","p4","p5"};
    }
    if (dataType.equals("secondary_species"))
    {
      return new Object[]{"s1","s2","s3","s4","s5"};
    }
    if (dataType.equals("gas_species"))
    {
      return new Object[]{"g1","g2","g3","g4","g5"};
    }
    if (dataType.equals("redox_species"))
    {
      return new Object[]{"r1","r2","r3","r4","r5"};
    }
    if (dataType.equals("mineral_species"))
    {
      return new Object[]{"m1","m2","m3","m4","m5"};
    }
    if (dataType.equals("surface_sites"))
    {
      return new Object[]{"z1","z2","z3","z4","z5"};
    }
    if (dataType.equals("selected_minerals"))
    {
      return new Object[]{"m1","m2","m3"};
    }
    if (dataType.equals("selected_primaries"))
    {
      return new Object[]{"p1","p2","p3"};
    }
    if (dataType.equals("selected_gases"))
    {
      return new Object[]{"g1","g2","g3"};
    }
    return new Object[]{dataType};
  }


  @Override
  public String getLabel(String objectIdentifier) {
    return objectIdentifier.toUpperCase();
  }

  @Override
  public Object getObject(String objectIdentifier) {

    if(objectIdentifier.equals("agni_parameter_group")) {
      AgniGroupItem item = new AgniGroupItem();
      customItems.add(item);
      return item;
    }   
    if(objectIdentifier.equals("lagrit_rows")) {
      AgniGroupItem item = new AgniGroupItem();
      customItems.add(item);
      return item;
    }
    return null;
  }

  @Override
  public boolean shouldShow(Node node) {
    return true;
  }

  @Override
  public String getIdentifier(Object object) {
    // Expanded list
    if(object instanceof DataItem) {
      System.out.println("\tTODO: Need to make an object out of a data item: " + object);
    }   
    return object.toString().toLowerCase();
  }

  @Override
  public void pushErrors(String key, List<String> errors) {
    if(abcErrorInterface != null)
      abcErrorInterface.pushErrors(key, errors);
  }

  @Override
  public void clearErrors(String key) {
    if(abcErrorInterface != null)
      abcErrorInterface.clearErrors(key);
  }

  @Override
  public void removeObject(Object object) {
    customItems.remove(object);

  }

  public ABCDocument getDocument() {
    if(abcdoc == null) {
      try {
        // Set System L&F
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        File xmlfile = ClasspathUtils.getFileFromClassFolder( ABCDocument.class, "params.xml");
        abcdoc = ABCDocument.load(xmlfile);
        abcdoc.getRuleEvaluator().setDataHandler(this);
        //abcdoc.setRuleEvaluator(new RuleExtensionExample(abcdoc));
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return abcdoc;    
  }


  @Override
  public IABCAction getCustomAction(String key) {   
    return new IABCAction() {
      @Override
      public void doAction(Object object, DataItem dataItem) {            
        JOptionPane.showMessageDialog(null, "TODO: Implement this part");
        dataItem.setValue("test", "ABCAction");
        //      ((AgniGroupItem)object).ncIdentifier.setValue("1", Source.INTERNAL); 
        /**
         * Reset layout example for Amanzi agni tool...
          abcComponent = new ABC(abcTool, identifier);
          setLayout(new BorderLayout());
          add(abcComponent);
         */
      }
    };
  }
  
  /*
  @Override
  public String getRuleVariable(String variable) {
    if (variable.equalsIgnoreCase("$x"))
      return "3.14529";
    if (variable.equalsIgnoreCase("$xxx"))
      return "5";
    return null;
  }
  */


  public class AgniGroupItem implements IABCUserObject {

    DataItem ncFile;
    DataItem ncIdentifier;
    DataItem ncDesOrder;
    DataItem ncIndex;

    String ncFileStr;

    public AgniGroupItem () {
      ncFile = new DataItem(new Key("nc_file"));
    }

    @Override
    public DataItem getItem(String key) {
      if(key.equals("nc_file"))
        return ncFile;
      if(key.equals("nc_identifier"))
        return ncIdentifier;
      if(key.equals("nc_des_order"))
        return ncDesOrder;
      if(key.equals("nc_index"))
        return ncIndex;
      return null;
    }

    @Override
    public void initializeItem(String key, DataItem item) {
      if(key.equals("nc_file"))
        ncFile = item;
      if(key.equals("nc_identifier"))
        ncIdentifier = item;
      if(key.equals("nc_des_order"))
        ncDesOrder = item;
      if(key.equals("nc_index"))
        ncIndex = item;
    }

    @Override
    public DataItem getIdentifier() {
      return ncFile;
    }

    @Override
    public IABCUserObject copy() {
      return this; // Not a very good copy :)
    }    
  }

  @Override
  public void addObject(Object object) {

    // Don't really need to handle this in the example

  }
}
