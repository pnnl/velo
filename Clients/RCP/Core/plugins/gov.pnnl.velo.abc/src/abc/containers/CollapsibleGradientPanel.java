package abc.containers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.border.Border;

import vabc.ABCStyle;
import vabc.SwingUtils;

public class CollapsibleGradientPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JLabel label;             // The title
	protected Icon upIcon, downIcon;    // The collapse and open icons
	protected JLabel upDownIconLabel;
	protected Dimension gdim = null;
	GradientPanel titlePanel;
	int BIGMAX = 10000; // just big to not constrain
	JPanel content;

	public CollapsibleGradientPanel(String title, Icon icon, Border outerBorder) {

		setLayout(new BorderLayout());
		content = new JPanel();

		label = new JLabel(title, icon, JLabel.LEADING);
		label.setFont(ABCStyle.style().getBorderFont());
		label.setForeground(ABCStyle.style().getForegroundColor());

		upIcon = SwingUtils.getImageIcon("navigate_up2.png", 16); 
		downIcon = SwingUtils.getImageIcon("navigate_down2.png", 16); 
		upDownIconLabel = new JLabel( "  ", upIcon, JLabel.TRAILING);

		titlePanel = new GradientPanel();
		titlePanel.setLayout(new BorderLayout());
		titlePanel.add(label, BorderLayout.WEST);
		titlePanel.add(upDownIconLabel, BorderLayout.EAST);
		int borderOffset = 2;
		if(icon == null) {
			borderOffset += 1;
		}
		titlePanel.setBorder(BorderFactory.createEmptyBorder(borderOffset, 4, borderOffset, 1));

		if (outerBorder == null) {
			setBorder(BorderFactory.createLineBorder(ABCStyle.style().getBorderColor()));

		} else {
			setBorder(BorderFactory.createCompoundBorder(outerBorder,
					BorderFactory.createLineBorder(ABCStyle.style().getBorderColor())));
		}

		GroupLayout groupLayout = new GroupLayout(this);
		this.setLayout(groupLayout);
		ParallelGroup parallelGroup = groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
		SequentialGroup sequentialGroup = groupLayout.createSequentialGroup();
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup().addGroup(parallelGroup)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(sequentialGroup));			

		parallelGroup.addComponent(titlePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		sequentialGroup.addComponent(titlePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);

		parallelGroup.addComponent(content, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		sequentialGroup.addComponent(content, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE);

		// Get the size of the titlepanel.  We'll use the height for the closed size
		if (gdim == null) gdim = titlePanel.getPreferredSize();

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (content.isShowing())  {
					content.setVisible(false);
					upDownIconLabel.setIcon(downIcon);
					CollapsibleGradientPanel.this.setMaximumSize(new Dimension(BIGMAX,gdim.height));
				} else {
					content.setVisible(true);
					upDownIconLabel.setIcon(upIcon);
					CollapsibleGradientPanel.this.setMaximumSize(new Dimension(BIGMAX,BIGMAX));
				}
			}
		});
	}

	/**
	 * Open or close the panel programatically.
	 * @param shouldCollapse
	 */
	public void collapse(boolean shouldCollapse) {
		//if (content.isShowing() == true && shouldCollapse) {
		if (shouldCollapse) {
					content.setVisible(false);
					upDownIconLabel.setIcon(downIcon);
					CollapsibleGradientPanel.this.setMaximumSize(new Dimension(BIGMAX,gdim.height));
		} else if (!content.isShowing() && !shouldCollapse) {
					content.setVisible(true);
					upDownIconLabel.setIcon(upIcon);
					CollapsibleGradientPanel.this.setMaximumSize(new Dimension(BIGMAX,BIGMAX));
		}
		
	}
	
	// Returns the collapsible content panel where children should be added
	public JPanel getContent() {
		return content;
	}

	public void setTitle(String label, Icon icon) {
		this.label.setText(label);
		this.label.setIcon(icon);
	}

	private class GradientPanel extends JPanel {
		private static final long serialVersionUID = -6385751027379193053L;
		private Color controlColor, bg;

		private GradientPanel() {
			this.controlColor = ABCStyle.style().getTabBackgroundFocusedColor();
			this.bg =  ABCStyle.style().getTabBackgroundColor();
			setBackground(controlColor);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			if (isOpaque()) {
				int width = getWidth();
				int height = getHeight();

				Graphics2D g2 = (Graphics2D) g;
				Paint oldPaint = g2.getPaint();

				g2.setPaint(new GradientPaint(0, 0, getBackground(), width, 0, bg));
				g2.fillRect(0, 0, width, height);
				//g2.fillRoundRect(0, 0, width, height,50,50);
				g2.setPaint(oldPaint);
			}
		}
	}
}