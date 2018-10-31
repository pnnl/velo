package gov.pnnl.velo.tif.ui;

import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import java.awt.SystemColor;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

public class QueueInfoPanel extends JPanel {

  /**
   * Create the panel.
   */
  public QueueInfoPanel() {
    setBackground(UIManager.getColor("info"));
    setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Queue Details", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
    GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[]{97, 166, 0};
    gridBagLayout.rowHeights = new int[]{39, 39, 39, 39};
    gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
    setLayout(gridBagLayout);
    
    JLabel lblNodes = new JLabel("Nodes:");
    lblNodes.setForeground(new Color(0, 102, 255));
    lblNodes.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
    GridBagConstraints gbc_lblNodes = new GridBagConstraints();
    gbc_lblNodes.anchor = GridBagConstraints.WEST;
    gbc_lblNodes.insets = new Insets(0, 4, 5, 5);
    gbc_lblNodes.gridx = 0;
    gbc_lblNodes.gridy = 0;
    add(lblNodes, gbc_lblNodes);
    
    JLabel lblNodesinfo = new JLabel("nodesInfo");
    lblNodesinfo.setForeground(SystemColor.inactiveCaptionText);
    lblNodesinfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
    GridBagConstraints gbc_lblNodesinfo = new GridBagConstraints();
    gbc_lblNodesinfo.anchor = GridBagConstraints.WEST;
    gbc_lblNodesinfo.insets = new Insets(0, 0, 5, 0);
    gbc_lblNodesinfo.gridx = 1;
    gbc_lblNodesinfo.gridy = 0;
    add(lblNodesinfo, gbc_lblNodesinfo);
    
    JLabel lblProcessors = new JLabel("Processors:");
    lblProcessors.setForeground(new Color(0, 102, 255));
    lblProcessors.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
    GridBagConstraints gbc_lblProcessors = new GridBagConstraints();
    gbc_lblProcessors.anchor = GridBagConstraints.WEST;
    gbc_lblProcessors.insets = new Insets(0, 4, 5, 5);
    gbc_lblProcessors.gridx = 0;
    gbc_lblProcessors.gridy = 1;
    add(lblProcessors, gbc_lblProcessors);
    
    JLabel lblProcessorsinfo = new JLabel("processorsInfo");
    lblProcessorsinfo.setForeground(SystemColor.inactiveCaptionText);
    lblProcessorsinfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
    GridBagConstraints gbc_lblProcessorsinfo = new GridBagConstraints();
    gbc_lblProcessorsinfo.insets = new Insets(0, 0, 5, 0);
    gbc_lblProcessorsinfo.anchor = GridBagConstraints.WEST;
    gbc_lblProcessorsinfo.gridx = 1;
    gbc_lblProcessorsinfo.gridy = 1;
    add(lblProcessorsinfo, gbc_lblProcessorsinfo);
    
    JLabel lblWalltime = new JLabel("Max Walltime:");
    lblWalltime.setForeground(new Color(0, 102, 255));
    lblWalltime.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
    GridBagConstraints gbc_lblWalltime = new GridBagConstraints();
    gbc_lblWalltime.anchor = GridBagConstraints.WEST;
    gbc_lblWalltime.insets = new Insets(0, 4, 0, 5);
    gbc_lblWalltime.gridx = 0;
    gbc_lblWalltime.gridy = 2;
    add(lblWalltime, gbc_lblWalltime);
    
    JLabel lblWalltimeinfo = new JLabel("walltimeInfo");
    lblWalltimeinfo.setForeground(SystemColor.inactiveCaptionText);
    GridBagConstraints gbc_lblWalltimeinfo = new GridBagConstraints();
    gbc_lblWalltimeinfo.anchor = GridBagConstraints.WEST;
    gbc_lblWalltimeinfo.insets = new Insets(0, 0, 5, 0);
    gbc_lblWalltimeinfo.gridx = 1;
    gbc_lblWalltimeinfo.gridy = 2;
    add(lblWalltimeinfo, gbc_lblWalltimeinfo);

  }

}
