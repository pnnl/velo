package vabc;


import java.awt.Color;
import java.awt.Font;


/**
 * Class that holds fonts, colors, and styles in general used by ABC.
 * This class provides a single static style class that can be overridden by
 * the application.
 * 
 * To change the style, you can:
 *   a) subclass and set the static fields to new values - this will provide changes  at the theme level
 *   b) subclass and override individual mthods for finer control
 *   c) mix and match a) and b)
 *   To support a), the fields are intentionally NOT final
 *   
 *   TODO border width
 *   TODO fix field border color - using the field background color currently 
 * @author karen
 *
 */
public class ABCStyle {

	// Fonts
	protected static java.awt.Font FONT = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 11);
	protected static java.awt.Font LARGE_BOLD = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 12);
	protected static java.awt.Font LARGE = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN, 12);
	protected static java.awt.Font BOLD = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD,11);
	protected static java.awt.Font SMALL = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.PLAIN,9);
	protected static java.awt.Font ITALIC = new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.ITALIC, 11);
	protected static java.awt.Font BOLD_ITALIC = new java.awt.Font(java.awt.Font.SANS_SERIF,(java.awt.Font.BOLD|java.awt.Font.ITALIC), 11);
	
	// Colors
	protected static java.awt.Color TEXT = new java.awt.Color(40, 40,40);
	protected static java.awt.Color FIELD_BORDER = new java.awt.Color(160, 160,160);
	protected static java.awt.Color OPTIONAL = new java.awt.Color(100, 100, 100);
	protected static java.awt.Color BACKGROUND = new java.awt.Color(248, 248, 248);
	protected static java.awt.Color SELECTION = new java.awt.Color(255, 194, 70); // Orangish yellow?
	protected static java.awt.Color ERROR = new java.awt.Color(242, 46, 17);
	protected static java.awt.Color WARNING = new java.awt.Color(241, 171, 65);
	protected static java.awt.Color YELLOW = new java.awt.Color(255, 254, 198);
	protected static java.awt.Color FIELD_BACKGROUND = java.awt.Color.WHITE; 
	protected static java.awt.Color GRADIENT_COLOR = new java.awt.Color(153, 204, 255);
	protected static java.awt.Color BORDER = java.awt.Color.LIGHT_GRAY;
	protected static java.awt.Color DISABLED = new java.awt.Color(240, 240, 240);
  protected static java.awt.Color SET_LABEL_COLOR = new java.awt.Color(240, 240, 240);
	
	protected static java.awt.Color ECLIPSE_TAB_BLUE = new java.awt.Color(153, 180, 209);
	protected static java.awt.Color ECLIPSE_TAB_LIGHT_BLUE = new java.awt.Color(195, 208, 221);
	protected static java.awt.Color ECLIPSE_TAB_WHITE = new java.awt.Color(243, 243, 243);
	protected static java.awt.Color ECLIPSE_TAB_GRAY = new java.awt.Color(105, 105, 105);
	protected static java.awt.Color EDITOR_BLUE = new java.awt.Color(33, 57, 156);

	private static ABCStyle theInstance = null;
	
	public static void setStyle(ABCStyle style) {
		theInstance = style;
	}

	public static ABCStyle style() {
		if (theInstance == null) {
			theInstance = new ABCStyle();  // default to this class
		}
		return theInstance;
	}

	protected ABCStyle() {
	}
	

	////////////////////////
	// Fonts
	////////////////////////
	public Font getDefaultFont() { return FONT; }
	public Font getRequiredFont() { return BOLD; }  
	public Font getOptionalFont() { return ITALIC; } 
	public Font getBorderFont() { return BOLD_ITALIC; }

	public Font[] getFonts() { return new Font[]{FONT, LARGE_BOLD, LARGE, BOLD, SMALL, ITALIC, BOLD_ITALIC };}
	
	////////////////////////
	// Overall default colors
	////////////////////////
	public Color getBackgroundColor() { return BACKGROUND; }
	public Color getForegroundColor() { return TEXT; }
	public Color getSelectionColor() { return SELECTION; }
	public Color getRequiredColor() { return TEXT; }  // label
	public Color getOptionalColor() { return TEXT; }  // label
	public Color getFieldBorderColor() { return FIELD_BORDER; }  
	public Color getHeaderGradientColor() { return GRADIENT_COLOR; } 
	public Color getBorderColor() { return BORDER; }
	public Color getDefaultColor() { return EDITOR_BLUE; } // this is default value color
	                                        

	////////////////////////
	// Input field colors
	////////////////////////
	public Color getLabelBackgroundColor() { return FIELD_BACKGROUND; }
	public Color getFieldBackgroundColor() { return FIELD_BACKGROUND; }
	public Color getFieldForegroundColor() { return EDITOR_BLUE; }
	public Color getTextDisabledColor() { return Color.GRAY;}  // text disabled. ComboBox used GRID_LINE for this
	public Color getFieldDisabledColor() { return DISABLED;}  // text disabled. ComboBox used GRID_LINE for this
	
	// These two no longer seem to be used....
	public Color getWarningColor() { return WARNING; }
	public Color getErrorColor() { return ERROR; }

	////////////////////////
	// Groups - used to try to draw attention to logically organized fields
	////////////////////////
	public Color getGroupBackgroundColor() { return ECLIPSE_TAB_BLUE; }
	public Color getSelectedGroupBorderColor() { return SELECTION; } //YELLOW; }
	
	////////////////////////
	// Dockable tab stuff
	////////////////////////
	public Color getTabBackgroundColor() { return ECLIPSE_TAB_WHITE; }
	public Color getTabBackgroundSelectedColor() { return ECLIPSE_TAB_LIGHT_BLUE; }
	public Color getTabBackgroundFocusedColor() { return ECLIPSE_TAB_BLUE; }
	

	public Color getTabForegroundColor() { return TEXT; }
	public Color getTabForegroundSelectedColor() { return TEXT; }
	public Color getTabForegroundFocusedColor() { return TEXT; }
	
	
	////////////////////////
	// Units
	////////////////////////
	public Color getUnitPopupBackground() { return FIELD_BACKGROUND; }
	public Color getUnitPopupForeground() { return EDITOR_BLUE; }
	
	////////////////////////
	// Tree Structure - not sure if this is really part of ABC
	////////////////////////
	public Color getTreeGroupColor() { return TEXT; }
	public Color getTreeLeafColor() { return EDITOR_BLUE; }

  public Color getSetLabelColor() { return ECLIPSE_TAB_GRAY; } //SET_LABEL_COLOR; }
	
}
