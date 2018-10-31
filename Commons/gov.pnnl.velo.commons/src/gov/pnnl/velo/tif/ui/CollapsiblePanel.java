package gov.pnnl.velo.tif.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.SystemColor;

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.plaf.UIManagerExt;

public class CollapsiblePanel extends JXPanel {
  
  private JXTaskPane taskPane;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public CollapsiblePanel(String title) {
    changeUIdefaults();
    taskPane = new JXTaskPane();
    taskPane.setTitle(title);
    super.setLayout(new BorderLayout());
    super.add(taskPane, BorderLayout.CENTER);
    
  }
  
  /* (non-Javadoc)
   * @see java.awt.Component#getBackground()
   */
  @Override
  public Color getBackground() {
    if(taskPane != null) {
      return taskPane.getBackground();
    } else {
      return super.getBackground();
    }
  }
  
  public void setCollapsed(boolean collapsed) {
    taskPane.setCollapsed(collapsed);
  }
  
  /* (non-Javadoc)
   * @see java.awt.Container#add(java.awt.Component)
   */
  @Override
  public Component add(Component comp) {
    if(taskPane != null) {
      return taskPane.add(comp);
    } else {
      return super.add(comp);
    }
  }
  
  /* (non-Javadoc)
   * @see java.awt.Container#removeAll()
   */
  @Override
  public void removeAll() {
    if(taskPane != null) {
      taskPane.removeAll();
    }
  }

  /* (non-Javadoc)
   * @see java.awt.Container#add(java.awt.Component, java.lang.Object)
   */
  @Override
  public void add(Component comp, Object constraints) {
    if(taskPane != null) {
      taskPane.add(comp, constraints);
    } else {
      super.add(comp, constraints);
    }
  }

  /* (non-Javadoc)
   * @see java.awt.Container#setLayout(java.awt.LayoutManager)
   */
  @Override
  public void setLayout(LayoutManager mgr) {
    if(taskPane != null) {
      taskPane.setLayout(mgr);
    } else {
      super.setLayout(mgr);
    }
  }

  private void changeUIdefaults() {

    // setting taskpane defaults
    Color lightGray = new Color(220,220,220);
    UIManager.put("TaskPane.titleBackgroundGradientStart", lightGray);
    UIManager.put("TaskPane.titleBackgroundGradientEnd", lightGray);

    Font taskPaneFont = UIManagerExt.getSafeFont("Label.font", new Font(
        "Dialog", Font.PLAIN, 12));
    taskPaneFont = taskPaneFont.deriveFont(Font.BOLD);

    UIManager.put("TaskPane.font", new FontUIResource(taskPaneFont));
    UIManager.put("TaskPane.background", UIManagerExt.getSafeColor("List.background",
        new ColorUIResource(Color.decode("#005C5C"))));
    UIManager.put("TaskPane.titleOver", new ColorUIResource(SystemColor.menuText));
    UIManager.put("TaskPane.titleForeground", new ColorUIResource(SystemColor.menuText));
    UIManager.put("TaskPane.specialTitleForeground", new ColorUIResource(SystemColor.menuText.brighter()));
    UIManager.put("TaskPane.animate", Boolean.TRUE);
    UIManager.put("TaskPane.focusInputMap", new UIDefaults.LazyInputMap(new Object[] {
        "ENTER", "toggleCollapsed",
        "SPACE", "toggleCollapsed"}));
  }
}