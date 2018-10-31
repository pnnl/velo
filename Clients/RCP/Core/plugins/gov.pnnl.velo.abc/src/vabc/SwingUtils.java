package vabc;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SwingUtils {

	/**
	 * Returns an image icon from the icons/16x16 folder
	 * for the specified image
	 * 
	 * Assumes file exists and is a png
	 * 
	 * @param iconName (just the name, no extension)
	 * @return
	 */
	public static ImageIcon getImageIcon(String iconName, int iconSize) {
		// For now the size must be either 16 or 32
		if(iconSize != 16 && iconSize != 32)
			return null;

		// Get the icons folder
		File iconsFolder = new File(StaticFileUtils.getInstance().getBaseIconFolder(), iconSize+"x"+iconSize);

		
		// Append .png in case the user didn't provide the type
		if(iconName.indexOf('.') < 0)
			iconName += ".png";

		// Check if the file exists
		if(Arrays.asList(iconsFolder.list()).contains(iconName)) {
			return new ImageIcon(new File(iconsFolder, iconName).getAbsolutePath());	
		}

		// File didn't exist, should we throw an exception?
		return null;
	}
	
	public static ImageIcon getImageIcon(File iconFile) {
	  return new ImageIcon(iconFile.getAbsolutePath());
	}

	public static void setFont(Font font, JComponent component) {
		component.setFont(font);
		for(Component child: component.getComponents()) {
			if(child instanceof JComponent)
				setFont(font, (JComponent)child);
		}
	}
	
	public static JButton newButton(AbstractAction action) {
	  javax.swing.JButton newButton = null;
	  String tooltip = (String) action.getValue(Action.NAME);	  
		ImageIcon icon = (ImageIcon)action.getValue(Action.SMALL_ICON);
		
		if(icon != null) {
		  newButton = new JButton(icon);
		  
		  File iconFolder = new File(icon.toString()).getParentFile();
		  String iconName = new File(icon.toString()).getName().split("\\.")[0];
		  
		  File pressed = new File(iconFolder, iconName + "_pressed.png");
		  File rollover = new File(iconFolder, iconName + "_rollover.png");
		  
		  
		  if(pressed.exists())
		    newButton.setPressedIcon(new ImageIcon(pressed.getAbsolutePath()));
		  if(rollover.exists())
		    newButton.setRolloverIcon(new ImageIcon(rollover.getAbsolutePath()));

		} else {
		  newButton = new JButton(tooltip);
		}
	
		newButton.setToolTipText(tooltip);
		newButton.setBorderPainted(false); // For Mac
		newButton.setContentAreaFilled(false);
		newButton.setFocusable(false);
		newButton.setAction(action);

		return newButton;

	}	
	
  /**
   * Returns a button set up with icon, pressed icon, rollover icon, and toolerrorString.
   */
  public static JButton newButton(File iconFolder, String buttonName, String tooltip, AbstractAction action) {

    File base  = new File(iconFolder.getAbsolutePath() + "/" + buttonName + ".png");
    File pressed = new File(iconFolder.getAbsolutePath() + "/" + buttonName + "_pressed.png");
    File rollover = new File(iconFolder.getAbsolutePath() + "/" + buttonName + "_rollover.png");

    javax.swing.JButton newButton = new JButton(new ImageIcon(base.getAbsolutePath()));
    newButton.setPressedIcon(new ImageIcon(pressed.getAbsolutePath()));
    newButton.setRolloverIcon(new ImageIcon(rollover.getAbsolutePath()));
    newButton.setToolTipText(tooltip);
    newButton.setBorderPainted(false); // For Mac
    newButton.setContentAreaFilled(false);
    newButton.setFocusable(false);
    
    if(action != null) {
      newButton.setAction(action);
    }
    return newButton;

  }
	
	/**
	 * Returns a button set up with icon, pressed icon, rollover icon, and tooltip.
	 */
	public static JButton newButton(String button, int iconSize, String tooltip) {

		// Get the icons folder
		File iconsFolder = new File(StaticFileUtils.getInstance().getBaseIconFolder(), iconSize+"x"+iconSize); 
				
		File base  = new File(iconsFolder.getAbsolutePath() + "/" + button + ".png");
		File pressed = new File(iconsFolder.getAbsolutePath() + "/" + button + "_pressed.png");
		File rollover = new File(iconsFolder.getAbsolutePath() + "/" + button + "_rollover.png");

		javax.swing.JButton newButton = new JButton(new ImageIcon(base.getAbsolutePath()));
		newButton.setPressedIcon(new ImageIcon(pressed.getAbsolutePath()));
		newButton.setRolloverIcon(new ImageIcon(rollover.getAbsolutePath()));
		newButton.setToolTipText(tooltip);
		newButton.setBorderPainted(false); // For Mac
		newButton.setContentAreaFilled(false);
		newButton.setFocusable(false);
		return newButton;

	}	

	/**
	 * Displays an error message without modality relative to given location (location can be null)
	 */
	public static void showErrorMessage(String errorMessage, Component location) {
		JOptionPane optionPane = new JOptionPane(errorMessage, JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null, new Object[]{"OK"}, "OK");
		final JDialog dialog = optionPane.createDialog("Error");
		dialog.setLocationRelativeTo(location);
		dialog.setModalityType(ModalityType.MODELESS);		
		dialog.setVisible(true);
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("value")) {
					dialog.dispose();
				}
			}
		});
	}

	/**
	 * Displays an error message without modality relative to given location (location can be null)
	 */
	public static void showMessage(String title, String message, Component location) {
		JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{"OK"}, "OK");
		final JDialog dialog = optionPane.createDialog(title);
		dialog.setLocationRelativeTo(location);
		dialog.setModalityType(ModalityType.MODELESS);		
		dialog.setVisible(true);
		optionPane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("value")) {
					dialog.dispose();
				}
			}
		});
	}
	
	public static JDialog getFileBroswerDialog(final AbstractAction action, String title, JComponent relativeTo) {
		FileChooser chooser = new SwingUtils().new FileChooser();
		final JDialog dialog = chooser.createDialog(null);
		title = title == null ? "Browse..." : title;
		dialog.setTitle(title);
		chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);

		dialog.setModalityType(ModalityType.MODELESS);
		dialog.setLocationRelativeTo(relativeTo);

		chooser.addActionListener(new javax.swing.AbstractAction() {

			private static final long serialVersionUID = -6947170085961533964L;

			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = (JFileChooser) evt.getSource();
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
					evt.setSource(chooser.getSelectedFile()); // So we can get at the file
					action.actionPerformed(evt);
					dialog.setVisible(false);
				} else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
					dialog.setVisible(false);
				}
			}
		});

		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				dialog.setVisible(false); // ??
			}
		});
		
		return dialog;
	}	
	
	/*
	 * Opens a file chooser, if file is selected name is added to provided text field and file is uploaded to working directory
	 */
	public static void browseFile(final JTextField field, final File directory, String title) {
		FileChooser chooser = new SwingUtils().new FileChooser();
		final JDialog dialog = chooser.createDialog(null);
		title = title == null ? "Browse..." : title;
		dialog.setTitle(title);
		chooser.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);

		dialog.setModalityType(ModalityType.MODELESS);
		dialog.setLocationRelativeTo(field);

		chooser.addActionListener(new javax.swing.AbstractAction() {

			private static final long serialVersionUID = -6947170085961533964L;

			public void actionPerformed(ActionEvent evt) {
				JFileChooser chooser = (JFileChooser) evt.getSource();
				if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
					try {
						// get the files
						java.io.File file = chooser.getSelectedFile();
						// copy the file to local working dir
						// TODO FileUtils.copyFileToDirectory(file, directory);
						// set file path
						if(field != null)
							field.setText(file.getName());

					} catch (Throwable ex) {
						ex.printStackTrace();
					}
					dialog.dispose();
				} else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
					dialog.dispose();
				}
			}
		});

		dialog.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				dialog.dispose();
			}
		});
		dialog.setVisible(true);
	}	
	
	/** Wrapper for file chooser */
	public class FileChooser extends JFileChooser {
		private static final long serialVersionUID = -5184333234413736663L;
		public javax.swing.JDialog createDialog(Component parent) throws java.awt.HeadlessException {
			return super.createDialog(parent);
		}
	}

}
