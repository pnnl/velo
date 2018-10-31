package abc.containers;

import java.awt.Dimension;

import org.w3c.dom.Node;

import vabc.IABCUserObject;



/**
 *  Auto building component - Columns
 * 
 *	Contains the layout manager to create a 
 *	2 columns with each section flowing onto the next column 
 * |  1  |  2  |
 * |  3  |  4  |
 * etc.
 */
public class ABCColumns extends ABCComponent {

  private static final long serialVersionUID = 1L;

  private ABCSection leftColumn;
  private ABCSection rightColumn;

  private int leftColumnWidth = 300;
  private int rightColumnWidth = 300; 

  public ABCColumns(ABC abcParent, String key, String label, Node node, IABCUserObject userObject) {
    super(abcParent, key, label, null, userObject);

    leftColumn = new ABCSection(abcParent, null, null, null, node, null);
    rightColumn = new ABCSection(abcParent, null, null, null);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(leftColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(rightColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(rightColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(leftColumn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

  }

  public void setColumnWidths(int widths[]) {
    leftColumnWidth = widths[0];
    rightColumnWidth = widths[1];
    leftColumn.setPreferredSize(new Dimension(leftColumnWidth, leftColumn.getPreferredSize().height));
    rightColumn.setPreferredSize(new Dimension(rightColumnWidth, rightColumn.getPreferredSize().height));
    setPreferredSize(new Dimension(leftColumnWidth + rightColumnWidth, 200));
    this.validate();
  }

  @Override
  public void addComponentToUI(ABCComponent component) {
    double leftHeight = leftColumn.getPreferredSize().getHeight();
    double rightHeight = rightColumn.getPreferredSize().getHeight();	
    if(leftHeight > rightHeight) {
      rightColumn.addComponentToUI(component);
    } else {
      leftColumn.addComponentToUI(component);
    }
  }

  @Override
  public boolean isDynamic() {
    return false;
  }
}
