package vabc;

// Configuration file
public class ABCConstants {


	public static final int ROW_HEIGHT = 26;
	
	public static class Key {

		// Component types			
		public static final String SET = "set";		
		public static final String SECTION = "section";
		public static final String TABS = "tabs"; 
		public static final String COLUMNS = "columns";		
		public static final String TABLE = "table";		
		public static final String EXPANDED_LIST = "expanded_list";
		public static final String COMPONENT = "component";		
    public static final String CUSTOM_TABLE = "custom_table";   
    public static final String COLUMN = "column";  

		// Element types
		public final static String LIST = "list"; // can be validated: list
		public final static String CHOICE = "choice";
		public final static String GROUP = "group";
		public final static String CHECK_BOX = "check_box";
		public final static String DOUBLE = "double"; // can be validated: double
		public final static String INTEGER = "integer"; // can be validated: integer
		public final static String STRING = "string"; // can be validated: string
		public final static String FIELD = "textarea"; // can be validated: string
		public final static String FILE = "file"; // can be validated: string, could be file?
		public final static String CALCULATION = "calculation";
		public final static String LOGICAL_EXPRESSION = "contextrule";
		public final static String COMMENT = "comment"; // any old text

		// Attributes
		public final static String KEY = "key";
		public final static String LABEL = "label";
    public final static String SET_LABEL = "set_label";
		public final static String UNITS = "units";
		public final static String ENABLED = "enabled";
		public final static String REQUIRED = "required";	
		public final static String DEFAULT = "default";
		public final static String DEFAULT_UNIT = "default_unit";
		public final static String TYPE = "type";
		public static final String NAME = "name";
		public static final String DISABLED_ITEMS = "disabled_items";
		public static final String ALIGNMENT = "alignment";
		public static final String ALIGNMENT_HORIZONTAL = "horizontal";
		public static final String ALIGNMENT_VERTICAL = "vertical";
		public static final String INTERNAL_DEFAULT = "internal_default";
		public static final String READONLY = "readonly";
    public final static String USER_OBJECT = "user_object"; 
    public final static String HAS_ACTION = "has_action"; 
    public final static String TIP = "help"; 
    public final static String COLLAPSED = "collapse"; 
    public static final String COLLAPSIBLE = "collapsible";
		
		// Validation
		public final static String ABSOLUTE_MIN = "absolute_min";
		public final static String ABSOLUTE_MAX = "absolute_max";
		public final static String SUGGESTED_MIN = "suggested_min";
		public final static String SUGGESTED_MAX = "suggested_max";
		public final static String REGEX = "regex";
		public static final String COULD_BE_FILE = "could_be_file";			
		
		// Special terms
		public final static String NOT_SET = "not_set";
    public static final String VISIBLE_ROWS = "visible_rows";

    public static final String AUTO_SORT = "auto_sort";
    
    // Fixing table size for rows
    public final static String FIXED = "fixed";
    

    public final static String UUID_SEPERATOR = ":";
    public static final String LINK = "link";
	}


}
