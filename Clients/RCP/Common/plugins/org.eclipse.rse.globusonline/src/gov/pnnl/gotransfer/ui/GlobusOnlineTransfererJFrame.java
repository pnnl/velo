package gov.pnnl.gotransfer.ui;

import gov.pnnl.gotransfer.model.UsernamePassword;
import gov.pnnl.gotransfer.service.GlobusOnlineTransferer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.globusonline.transfer.JSONTransferAPIClient;

public class GlobusOnlineTransfererJFrame extends JFrame implements ActionListener {
  //keys for encryption/decryption
  private static byte[] keyBytes = "Koolcat1".getBytes();
  private static byte[] ivBytes = "Koolcat1".getBytes();
  
  private static final long serialVersionUID = 1L;
  private String fromEndpoint = "";
  private String toEndpoint = "";
  private UsernamePassword fromEndpointUsernamePassword = new UsernamePassword("", "");
  private UsernamePassword toEndpointUsernamePassword = new UsernamePassword("", "");
  private UsernamePassword goUsernamePassword = new UsernamePassword("", "");

  private MonitorForFilesToTransfer logMessages;

  private static String PASSWORD_FILE = "go_authentications.txt";
  private static String PREFENCES_FILE = "go_prefs.txt";
  private static String PASSWORD_FILE_FOLDER = ".goAutoTransferer";
  private Map credentials = new HashMap();
//private Map<String, UsernamePassword> credentials = new HashMap<String, UsernamePassword>();
  
  private File credentialsFile;
  private File prefFile;

  private GlobusOnlineTransferer transferer = new GlobusOnlineTransferer();
  
  public static void main(String[] args) {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        new GlobusOnlineTransfererJFrame();
      }
    });
  }

  private class MonitorForFilesToTransfer extends SwingWorker {
//	  private class MonitorForFilesToTransfer extends SwingWorker<Void, String> {
//    @Override
//	    protected void process(List<String> chunks) {
	protected void process(List chunks) {
//      addStatusMessage(chunks.toArray(new String[chunks.size()]));
//      for (String string : chunks) {
//        addStatusMessage(string);
//      }
    }
	
//    @Override
    protected Object doInBackground() throws Exception {

      try{
      transferer.setCanceled(false);
      transferer.setFromEndpoint(fromEndpoint);
      transferer.setToEndpoint(toEndpoint);
      transferer.setFromEndpointPath(fromPathTextField.getText());
      transferer.setToEndpointPath(toPathTextField.getText());

      List preferences = new ArrayList();
//    List<String> preferences = new ArrayList<String>();
      preferences.add("fromEndpoint:" +fromEndpoint);
      preferences.add("fromEndpointPath:" +fromPathTextField.getText());
      preferences.add("toEndpoint:" +toEndpoint);
      preferences.add("toEndpointPath:" +toPathTextField.getText());
      FileUtils.writeLines(prefFile, preferences);
//      publish("Saved transfer preferences.");
      
      //first get the GO token and activate both endpoints:
      String token = transferer.authenticateToGlobusOnline(goUsernamePassword.getUsername(), goUsernamePassword.getPassword());
//      publisMessages(transferer);
      if(token == null){
//        publish("Unable to authenticate to Globus Online.  Try starting transfer again in a moment.");
      }else{//authentication to GO worked, now try to activate endpoints:
    	  /*
        JSONTransferAPIClient client = transferer.activateEndpoint(fromEndpoint, fromEndpointUsernamePassword.getUsername(), fromEndpointUsernamePassword.getPassword());
//        publisMessages(transferer);
        if(client == null){
//          publish("Unable to activate endpoint: " +fromEndpoint + ". Try starting transfer again in a moment.");
        }else{//from endpoint activated, now try the other
          transferer.setJsonTransferClient(client);
          client = transferer.activateEndpoint(toEndpoint, toEndpointUsernamePassword.getUsername(), toEndpointUsernamePassword.getPassword());
//          publisMessages(transferer);
          if(client == null){
//            publish("Unable to activate endpoint: " +toEndpoint+ ". Try starting transfer again in a moment.");
          }else{//to endpoint activated, now start the transferer thread
            Thread testThread = new Thread(transferer);
            transferer.setRunning(true);
            testThread.start();
            
            
            //here's the meat of this swingworker 
            while (!isCancelled() && transferer.isRunning()) {
              Thread.sleep(1000);
//              publisMessages(transferer);
            }
            
            
            
            transferer.setCanceled(true);
          }
        }
        */
      }
      }catch (Exception e){
//        publisMessages(transferer);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        throw e;
      }
      
      startButton.setEnabled(true);
      stopButton.setEnabled(false);
      return null;
    }

/*
    private void publisMessages(GlobusOnlineTransferer transferer) {
      List messages = transferer.getMessages();
//      List<String> messages = transferer.getMessages();
      int size = messages.size();
      for(int idx=0;idx<size;idx++) {
    	  String message = (String)messages.get(idx);
//      for (String message : messages) {
//          for (String message : messages) {
        publish(message);
      }
    }
  */

  }

  public GlobusOnlineTransfererJFrame() {
    super("Auto Transfer Files Via Globus Online");
    new File(System.getProperty("user.home") + "/" + PASSWORD_FILE_FOLDER).mkdirs();
    this.credentialsFile = new File(System.getProperty("user.home") + "/" + PASSWORD_FILE_FOLDER + "/" + PASSWORD_FILE);
    new File(System.getProperty("user.home") + "/" + PASSWORD_FILE_FOLDER).mkdirs();
    this.prefFile = new File(System.getProperty("user.home") + "/" + PASSWORD_FILE_FOLDER + "/" + PREFENCES_FILE);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    goUsernamePassword = getStoredCredsForEndpoint("GlobusOnline");
    // ask for the go username and password if we don't already have it stored:
    if (goUsernamePassword == null) {
      goUsernamePassword = collectEndpointUsernamePassword("GlobusOnline");
    }
    
    initComponents();
    // Display the window.
    pack();
    setVisible(true);
    
    try {
      if (prefFile.exists()) {
        List preferences;
//        List<String> preferences;
        preferences = FileUtils.readLines(prefFile);
//        for (String line : preferences) {
        int size=preferences.size();
        for(int idx=0;idx<size;idx++) {
          String line = (String)preferences.get(idx);
//            for (String line : preferences) {
          String value = line.substring(line.indexOf(":") +1);
          if (line.startsWith("fromEndpointPath")) {
            fromPathTextField.setText(value);
          } else if (line.startsWith("fromEndpoint")) {
            fromEndpoint = value;
            fromEndpointComboBox.setSelectedItem(value);
          } else if (line.startsWith("toEndpointPath")) {
            toPathTextField.setText(value);
          } else if (line.startsWith("toEndpoint")) {
            toEndpoint = value;
            toEndpointComboBox.setSelectedItem(value);
          }
        }
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

  public void actionPerformed(ActionEvent e) {
    if ("Start" == e.getActionCommand() || "Stop" == e.getActionCommand()) {
      if("Start" == e.getActionCommand() && (!requiredInputsEntered() || !promptForCredsIfNeeded())){
//        JOptionPane.showMessageDialog(this, "All fields are required.  You need to fill out every field and provide usernames and passwords for Globus Online and both endpoints before starting file transfers.");
      }
      else if ("Start" == e.getActionCommand()) {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
//        (this.logMessages = new MonitorForFilesToTransfer()).execute();
      } else if ("Stop" == e.getActionCommand()) {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        if(logMessages != null){
          logMessages.cancel(false);
        }
        logMessages = null;
      }
    }else if ("Reset Password Cache" == e.getActionCommand() ){
      resetPasswordCache();
    }

  }

  private void resetPasswordCache() {
    if (credentialsFile.exists()) {
      //wipe out the old contents of the file
      try {
        FileUtils.writeStringToFile(credentialsFile, "");
      } catch (IOException e1) { }
    }
    fromEndpointUsernamePassword = new UsernamePassword("", "");
    toEndpointUsernamePassword = new UsernamePassword("", "");
    goUsernamePassword = new UsernamePassword("", "");
    
    goUsernamePassword = collectEndpointUsernamePassword("GlobusOnline");
    if(fromEndpoint != null || !fromEndpoint.isEmpty()){
      fromEndpointUsernamePassword = collectEndpointUsernamePassword(fromEndpoint);
    }
    if(toEndpoint != null || !toEndpoint.isEmpty()){
      toEndpointUsernamePassword = collectEndpointUsernamePassword(toEndpoint);
    }
  }

  public boolean promptForCredsIfNeeded(){
    if(goUsernamePassword == null || goUsernamePassword.getPassword().isEmpty() || goUsernamePassword.getUsername().isEmpty()){
      goUsernamePassword = collectEndpointUsernamePassword("GlobusOnline");
    }
    if(fromEndpointUsernamePassword == null || fromEndpointUsernamePassword.getPassword().isEmpty() || fromEndpointUsernamePassword.getUsername().isEmpty()){
      fromEndpointUsernamePassword = collectEndpointUsernamePassword(fromEndpoint);
    }
    if(toEndpointUsernamePassword == null || toEndpointUsernamePassword.getPassword().isEmpty() || toEndpointUsernamePassword.getUsername().isEmpty()){
      toEndpointUsernamePassword = collectEndpointUsernamePassword(toEndpoint);
    }
    return goUsernamePassword != null && fromEndpointUsernamePassword != null && toEndpointUsernamePassword != null;
  }
  
  public boolean requiredInputsEntered() {
    if(((String)fromEndpointComboBox.getSelectedItem()).isEmpty() ||
       ((String)toEndpointComboBox.getSelectedItem()).isEmpty() || 
       fromPathTextField.getText().isEmpty() ||
       toPathTextField.getText().isEmpty() ){
      return false;
    }
    //also check that paths have slashes going the right direction:
    if(fromPathTextField.getText().contains("\\")){
      fromPathTextField.setText(fromPathTextField.getText().replaceAll("\\\\", "/"));
      fromPathTextField.repaint();
      fromPathTextField.revalidate();
    }
    if(toPathTextField.getText().contains("\\")){
      toPathTextField.setText(toPathTextField.getText().replaceAll("\\\\", "/"));
      toPathTextField.repaint();
      toPathTextField.revalidate();
    }
    return true;
  }

  public void addStatusMessage(String message) {
//	int size = messages.size();
//	for(int idx=0;idx<size;idx) {
//	  String message = messages.get(idx);
//	for (String message : messages) {
//	        for (String message : messages) {
      jTextArea1.append(message + "\n");
//    }
    //this should make the text area scoll to the end
    jTextArea1.select(jTextArea1.getHeight()+100000,0);
    jTextArea1.repaint();
    jTextArea1.revalidate();
  }

  private UsernamePassword collectEndpointUsernamePassword(String endpoint) {
    PasswordDialog p = new PasswordDialog(null, "Provide Credentials for " + endpoint);
    if (p.showDialog()) {

      ObjectOutputStream oos = null;
      try {
        //store in the hashmap (that gets written to file) the username/password encrypted, but return unencrypted for UI to store in memory
        credentials.put(endpoint, new UsernamePassword(new String(encrypt(p.getName())), new String(encrypt(p.getPass()))));
        // Save encrypted password to file!
        FileOutputStream fos = new FileOutputStream(credentialsFile);
        oos = new ObjectOutputStream(fos);
        oos.writeObject(credentials);
      } catch (Exception e) {
        addStatusMessage("Unable to cache credentials.");
        e.printStackTrace();
      } finally {
        if(oos != null)  {
          try {oos.close();} catch(Throwable e){};
        }
      }
      return new UsernamePassword(p.getName(), p.getPass());
    }
    return null;
  }



  private UsernamePassword getStoredCredsForEndpoint(String endpoint) {
    if (credentialsFile.exists()) {
      ObjectInputStream ois = null;
      try {
        FileInputStream fis = new FileInputStream(credentialsFile);
        fis = new FileInputStream(credentialsFile);
        ois = new ObjectInputStream(fis);
        credentials = (Map)ois.readObject();
        if(credentials.containsKey(endpoint)){
          UsernamePassword userPass = (UsernamePassword)credentials.get(endpoint);
          return new UsernamePassword(decrypt(userPass.getUsername().getBytes()), decrypt(userPass.getPassword().getBytes()));
        }
      } catch (Exception e) {
      } finally {
        if(ois != null)  {
          try {ois.close();} catch(Throwable e){};
        }
      }     
    }
    return null;
  }
  
  
  private byte[] encrypt (String stringToEncrypt) throws Exception {
    
    //Let's assume the bytes to encrypt are in
    byte[] inputBytes = stringToEncrypt.getBytes();
    
    // wrap key data in Key/IV specs to pass to cipher
    SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
       
    // create the cipher with the algorithm you choose
    // see javadoc for Cipher class for more info, e.g.
    Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    
    cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
    byte[] encrypted = new byte[cipher.getOutputSize(inputBytes.length)];
    int enc_len = cipher.update(inputBytes, 0, inputBytes.length, encrypted, 0);
    enc_len += cipher.doFinal(encrypted, enc_len);
    
    return encrypted;

  }
  
  private String decrypt (byte[] encrypted) throws Exception {
    // wrap key data in Key/IV specs to pass to cipher
    SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
    IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
       
    // create the cipher with the algorithm you choose
    // see javadoc for Cipher class for more info, e.g.
    Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
    
    cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
    int len = encrypted.length;
    byte[] decryptedBytes = new byte[cipher.getOutputSize(len)];
    int dec_len = cipher.update(encrypted, 0, len, decryptedBytes, 0);
    dec_len += cipher.doFinal(decryptedBytes, dec_len);    

    String decryptedString = new String(decryptedBytes);
    return decryptedString.trim();
  }

  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
   */
//  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jLabel2 = new javax.swing.JLabel();
    jLabel3 = new javax.swing.JLabel();
    fromPathTextField = new javax.swing.JTextField();
    jPanel1 = new javax.swing.JPanel();
    fromEndpointComboBox = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    toEndpointComboBox = new javax.swing.JComboBox();
    toPathTextField = new javax.swing.JTextField();
    startButton = new javax.swing.JButton();
    stopButton = new javax.swing.JButton();
    clearPasswordsButton = new javax.swing.JButton();
    jLabel1 = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jSeparator1 = new javax.swing.JSeparator();
    jLabel7 = new javax.swing.JLabel();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextArea1 = new javax.swing.JTextArea();

    fromEndpointComboBox.addActionListener(new ActionListener() {
//      @Override
      public void actionPerformed(ActionEvent arg0) {
        String selection = (String) fromEndpointComboBox.getSelectedItem();
        if (selection.isEmpty()) {
          fromEndpointUsernamePassword = new UsernamePassword("", "");
        } else {
          fromEndpointUsernamePassword = getStoredCredsForEndpoint(selection);
          fromEndpoint = selection;
          if (fromEndpointUsernamePassword == null) {
            fromEndpointUsernamePassword = collectEndpointUsernamePassword(selection);
          } 
        }
      }
    });
    
    toEndpointComboBox.addActionListener(new ActionListener() {
//      @Override
      public void actionPerformed(ActionEvent arg0) {
        String selection = (String) toEndpointComboBox.getSelectedItem();
        if (selection.isEmpty()) {
          toEndpointUsernamePassword = new UsernamePassword("", "");
        } else {
          toEndpointUsernamePassword = getStoredCredsForEndpoint(selection);
          toEndpoint = selection;
          if (toEndpointUsernamePassword == null) {
            toEndpointUsernamePassword = collectEndpointUsernamePassword(selection);
          } 
        }
      }
    });

//    startButton.addActionListener(this);
//    stopButton.addActionListener(this);
//    clearPasswordsButton.addActionListener(this);

    setLayout(new java.awt.GridBagLayout());

    jLabel2.setText("Transfer From Endpoint:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridwidth = 7;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
    getContentPane().add(jLabel2, gridBagConstraints);

    jLabel3.setText("Transfer From Path:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
    getContentPane().add(jLabel3, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 12;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.gridwidth = 14;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.ipadx = 356;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 13, 0, 10);
    getContentPane().add(fromPathTextField, gridBagConstraints);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
    jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    getContentPane().add(jPanel1, gridBagConstraints);

    fromEndpointComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT, GlobusOnlineTransferer.PIC_DTN_ENDPOINT }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 12;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.ipadx = 5;
    gridBagConstraints.ipady = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(17, 13, 0, 0);
    getContentPane().add(fromEndpointComboBox, gridBagConstraints);

    jLabel4.setText("Transfer To Endpoint:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridwidth = 5;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(20, 10, 0, 0);
    getContentPane().add(jLabel4, gridBagConstraints);

    jLabel5.setText("Transfer To Path:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(21, 10, 0, 0);
    getContentPane().add(jLabel5, gridBagConstraints);

    toEndpointComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "", GlobusOnlineTransferer.PIC_DTN_ENDPOINT, GlobusOnlineTransferer.APS_CLUTCH_ENDPOINT }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 12;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.ipadx = 5;
    gridBagConstraints.ipady = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(17, 13, 0, 0);
    getContentPane().add(toEndpointComboBox, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 12;
    gridBagConstraints.gridy = 9;
    gridBagConstraints.gridwidth = 14;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.ipadx = 356;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 13, 0, 10);
    getContentPane().add(toPathTextField, gridBagConstraints);

    startButton.setText("Start");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 10, 12, 0);
    getContentPane().add(startButton, gridBagConstraints);

    stopButton.setText("Stop");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 6, 12, 0);
    getContentPane().add(stopButton, gridBagConstraints);

    clearPasswordsButton.setText("Reset Password Cache");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 7;
    gridBagConstraints.gridy = 14;
    gridBagConstraints.gridwidth = 8;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 14, 12, 0);
    getContentPane().add(clearPasswordsButton, gridBagConstraints);

    jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
    jLabel1.setText("Monitors \"From Endpoint\" for new files to be automatically transferred to \"To Endpoint\"");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 13;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(18, 10, 0, 0);
    getContentPane().add(jLabel1, gridBagConstraints);

    jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
    jLabel6.setText("Transfer Files");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 11;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(13, 10, 0, 0);
    getContentPane().add(jLabel6, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 11;
    gridBagConstraints.gridwidth = 25;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 496;
    gridBagConstraints.ipady = 9;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(11, 10, 0, 10);
    getContentPane().add(jSeparator1, gridBagConstraints);

    jLabel7.setText("Status:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 12;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 0);
    getContentPane().add(jLabel7, gridBagConstraints);

    jTextArea1.setColumns(1);
    jTextArea1.setRows(5);
    jScrollPane1.setViewportView(jTextArea1);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 13;
    gridBagConstraints.gridwidth = 25;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 474;
    gridBagConstraints.ipady = 154;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
    getContentPane().add(jScrollPane1, gridBagConstraints);
  }// </editor-fold>
   // Variables declaration - do not modify

  private javax.swing.JButton startButton;
  private javax.swing.JButton stopButton;
  private javax.swing.JButton clearPasswordsButton;
  private javax.swing.JComboBox fromEndpointComboBox;
  private javax.swing.JComboBox toEndpointComboBox;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JTextArea jTextArea1;
  private javax.swing.JTextField fromPathTextField;
  private javax.swing.JTextField toPathTextField;
  // End of variables declaration
}
