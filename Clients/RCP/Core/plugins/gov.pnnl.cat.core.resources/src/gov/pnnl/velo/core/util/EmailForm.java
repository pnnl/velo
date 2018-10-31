/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.velo.core.util;

import gov.pnnl.cat.core.resources.ResourcesPlugin;
import gov.pnnl.velo.model.Email;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

/**
 *
 * @author D3K339
 * @version $Revision: 1.0 $
 */
public class EmailForm extends javax.swing.JFrame {

  private static final long serialVersionUID = 1L;

  // Variables declaration - do not modify
  private javax.swing.JButton attachButton;
  private javax.swing.JLabel attachedLabel;
  private javax.swing.JTextField attachedText;
  private javax.swing.JLabel fromLabel;
  private javax.swing.JTextField fromText;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JTextArea messageText;
  private javax.swing.JButton sendButton;
  private javax.swing.JLabel subjectLabel;
  private javax.swing.JTextField subjectText;
  private javax.swing.JLabel toLabel;
  private javax.swing.JTextField toText;

  private List<File>attachments;
  // End of variables declaration
  
  // TODO: set these in an extension point

  /**
   * Creates new form EmailForm
   * @param from String
   * @param to String
   * @param subject String
   * @param message String
   * @param attachments List<File>
   */
  public EmailForm(String from, String to, String subject, String message, List<File> attachments) {
    initComponents();
    setTitle("Email Sender");
    fromText.setText(from);
    if(to == null) {
      to = EmailConstants.getToEmail();
    }
    toText.setText(to);
    subjectText.setText(subject);
    subjectText.setCaretPosition(0);
    
    messageText.setText(message);
    messageText.setCaretPosition(0);
    
    this.attachments = attachments;
    if(attachments != null) {
      String attTxt = "";

      for(int i = 0; i < attachments.size(); i++) {
        if(i > 0) {
          attTxt += "; ";
        }
        attTxt += attachments.get(i).getAbsolutePath();
      }
      attachedText.setText(attTxt);
    }

    attachButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        attachFile();
      }
    });

    sendButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        sendEmail();
      }
    });

  }

  private void sendEmail() {
    
    String to = toText.getText();
    String from = fromText.getText();
    
    try {
    if(!isValidEmailAddress(to)) {
      throw new RuntimeException("Invalid to address: " + to);
      
    } else if(!isValidEmailAddress(from)) {
      from = from + "@" +  EmailConstants.getFromEmailDomain();
      if(!isValidEmailAddress(from)) {
        throw new RuntimeException("Invalid from address: " + from);  
      }
    } 
    
    String message = "Submitted By: " + fromText.getText() + "\n\n" + messageText.getText();
    String subject = subjectText.getText();
    
    Email email = new Email(from, to, subject, message, attachments);
      ResourcesPlugin.getResourceManager().sendEmail(email);
      dispose();
    
    } catch (Throwable e) {
      ToolErrorHandler.handleError("Sending Email Failed", e, true, null);
    }

  }

  private void attachFile() {
    JFileChooser fileChooser = new JFileChooser();

    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      attachedText.setText(fileChooser.getSelectedFile().getAbsolutePath());
      attachments.clear();
      attachments.add(fileChooser.getSelectedFile());
    }

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    fromLabel = new javax.swing.JLabel();
    toLabel = new javax.swing.JLabel();
    fromText = new javax.swing.JTextField();
    toText = new javax.swing.JTextField();
    jScrollPane2 = new javax.swing.JScrollPane();
    messageText = new javax.swing.JTextArea();
    attachedLabel = new javax.swing.JLabel();
    subjectText = new javax.swing.JTextField();
    subjectLabel = new javax.swing.JLabel();
    attachedText = new javax.swing.JTextField();
    sendButton = new javax.swing.JButton();
    attachButton = new javax.swing.JButton();

    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new java.awt.GridBagLayout());

    fromLabel.setText("From:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(16, 27, 0, 0);
    getContentPane().add(fromLabel, gridBagConstraints);

    toLabel.setText("To:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(10, 27, 0, 0);
    getContentPane().add(toLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 253;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(13, 4, 0, 0);
    getContentPane().add(fromText, gridBagConstraints);

    toText.setEnabled(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 253;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(7, 4, 0, 0);
    getContentPane().add(toText, gridBagConstraints);

    messageText.setColumns(20);
    messageText.setRows(5);
    jScrollPane2.setViewportView(messageText);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.gridwidth = 6;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 380;
    gridBagConstraints.ipady = 253;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(18, 10, 11, 10);
    getContentPane().add(jScrollPane2, gridBagConstraints);

    attachedLabel.setText("Attached:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(12, 27, 0, 0);
    getContentPane().add(attachedLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 328;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(11, 4, 0, 10);
    getContentPane().add(subjectText, gridBagConstraints);

    subjectLabel.setText("Subject:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.insets = new java.awt.Insets(14, 27, 0, 0);
    getContentPane().add(subjectLabel, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridheight = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.ipadx = 328;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(9, 4, 0, 10);
    getContentPane().add(attachedText, gridBagConstraints);

    sendButton.setText("Send");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.ipadx = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(12, 10, 0, 10);
    getContentPane().add(sendButton, gridBagConstraints);

    attachButton.setText("Attach");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.gridheight = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(6, 10, 0, 10);
    getContentPane().add(attachButton, gridBagConstraints);

    pack();
  }// </editor-fold>

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(EmailForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(EmailForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(EmailForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(EmailForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        File file = new File("D:\\Downloads\\SwingEmailSender\\lib\\javax.mail.jar");
        List<File>att = new ArrayList<File>();
        att.add(file);
        new EmailForm("from", "to", "subject", "message", att).setVisible(true);
      }
    });
  }
  
  /**
   * Method openEmailDialog.
   * @param subject String
   */
  public static void openEmailDialog(String subject) {
   openEmailDialog(null, null, subject, null, null);
  }

  public static void openEmailDialog(final String to, String frm, final String subject, List<File> attch) {
    openEmailDialog(to, frm, subject, null, attch);
  }
  
  /**
   * Method openEmailDialog.
   * @param to String
   * @param frm String
   * @param subject String
   * @param attch List<File>
   */
  public static void openEmailDialog(final String to, String frm, final String subject, final String message, List<File> attch) {
    try {
      if(attch == null) {
        attch = new ArrayList<File>();
      }
      final List<File> attachments = attch;
      
      // Send email :
      if(frm == null) {
        frm = ResourcesPlugin.getSecurityManager().getUsername();
      }
      final String from = frm;
       
      SwingUtilities.invokeLater(new Runnable() {
        
        @Override
        public void run() {
          new EmailForm(from, to, subject, message, attachments).setVisible(true);              
          
        }
      });
      
      
    } catch (Throwable e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  
  /**
  * Validate the form of an email address.
  *
  * <P>Return <tt>true</tt> only if 
  *<ul> 
  * <li> <tt>aEmailAddress</tt> can successfully construct an 
  * {@link javax.mail.internet.InternetAddress} 
  * <li> when parsed with "@" as delimiter, <tt>aEmailAddress</tt> contains 
  * two tokens which satisfy {@link hirondelle.web4j.util.Util#textHasContent}.
  *</ul>
  *
  *<P> The second condition arises since local email addresses, simply of the form
  * "<tt>albert</tt>", for example, are valid for 
  * {@link javax.mail.internet.InternetAddress}, but almost always undesired.
  * @param aEmailAddress String
   * @return boolean
   */
  public static boolean isValidEmailAddress(String aEmailAddress){
    if (aEmailAddress == null) return false;
    boolean result = true;
    try {
      InternetAddress emailAddr = new InternetAddress(aEmailAddress);
      if (! hasNameAndDomain(aEmailAddress)) {
        result = false;
      }
    }
    catch (AddressException ex){
      result = false;
    }
    return result;
  }

  /**
   * Method hasNameAndDomain.
   * @param aEmailAddress String
   * @return boolean
   */
  private static boolean hasNameAndDomain(String aEmailAddress){
    String[] tokens = aEmailAddress.split("@");
    return 
      tokens.length == 2 &&
      (tokens[0] != null && !tokens[0].trim().isEmpty()) && 
      (tokens[1] != null && !tokens[1].trim().isEmpty()) 
    ;
  }

  
}
