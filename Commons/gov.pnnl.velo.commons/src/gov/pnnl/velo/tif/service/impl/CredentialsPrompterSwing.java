/**
 * 
 */
package gov.pnnl.velo.tif.service.impl;

import gov.pnnl.velo.tif.service.CredentialsPrompter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.kepler.ssh.RemoteExec;

/**
 * Use Swing JOptionPane to prompt user for password/passphrase
 */
public class CredentialsPrompterSwing implements CredentialsPrompter {
  
  /* (non-Javadoc)
   * @see gov.pnnl.velo.tif.service.PasswordDialogProvider#displayPasswordDialog(java.lang.String)
   */
  @Override
  public String promptForCredentials(String title, String message, String errMessage, String[] prompts, boolean[] echo) {
    
    final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
        GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
        new Insets(0, 0, 0, 0), 0, 0);
    String passpki = null;
    JPanel panel = new JPanel();
    panel.setLayout(new GridBagLayout());

    gbc.weightx = 1.0;
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.gridx = 0;
    panel.add(new JLabel(message), gbc);
    gbc.gridy++;

    gbc.gridwidth = GridBagConstraints.RELATIVE;

    JTextField[] texts = new JTextField[prompts.length];
    for (int i = 0; i < prompts.length; i++) {
      gbc.fill = GridBagConstraints.NONE;
      gbc.gridx = 0;
      gbc.weightx = 1;
      panel.add(new JLabel(prompts[i]), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weighty = 1;
      if (echo[i]) {
        texts[i] = new JTextField(20);
      } else {
        texts[i] = new JPasswordField(20);
      }
      panel.add(texts[i], gbc);
      gbc.gridy++;
    }

    if (JOptionPane.showConfirmDialog(RemoteExec.getParentComponent(), panel, title, JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
      String[] response = new String[prompts.length];
      for (int i = 0; i < prompts.length; i++) {
        response[i] = texts[i].getText();
        passpki = response[i];
      }
      return passpki;
      
    } else {
      return null; // cancel
    }

  }

}
