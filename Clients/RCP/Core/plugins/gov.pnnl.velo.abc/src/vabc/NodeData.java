package vabc;


import org.w3c.dom.Node;

/**
 * Legacy, we will delete this soon...
 * Simple data structure to access the XML nodes.
 * 
 * Reads a node and extracts 
 * the information it contains.
 */
public class NodeData {

	private Node node;

	// Attributes from typedef_global
	private String type;
	private String key;
	private String label;

	// Required or not,  not required parameters will 
	// only be validated if the user enters a value
	private Boolean required;
	private Boolean enabled;

	// Value
	private String defaultValue;

	// Units
	private String units;
	private String defaultUnit;

	// Validation
	private String regex;
	private Double absoluteMin;
	private Double absoluteMax;
	private Double suggestedMin;
	private Double suggestedMax;

	// Specifics per node type
	private String objects; // list
	private String selection; // list
	private Integer visibleRows; // list
	
	private String name;

	public NodeData(Node node) {
		this.node = node;

		name = node.getNodeName();
		type = node.getNodeName();
		key = extractData("key");
		label = extractData("label");

		String required = extractData("required");
		if(required != null) {
			this.required = Boolean.valueOf(required);
		} else {
			this.required = false; // So it will always be something
		}

		String enabled = extractData("enabled");
		if(enabled != null) {
			this.enabled = Boolean.valueOf(enabled);
		} else {
			this.enabled = false; // So it will always be something
		}

		defaultValue = extractData("default");
		units = extractData("units"); 
		defaultUnit = extractData("default_unit");

		regex = extractData("regex");

		String absoluteMin = extractData("absolute_min");
		if(absoluteMin != null)
			this.absoluteMin = Double.parseDouble(absoluteMin);

		String absoluteMax = extractData("absolute_max");
		if(absoluteMax != null)
			this.absoluteMax = Double.parseDouble(absoluteMax);

		String suggestedMin = extractData("suggested_min");
		if(suggestedMin != null)
			this.suggestedMin = Double.parseDouble(suggestedMin);

		String suggestedMax = extractData("suggested_max");
		if(suggestedMax != null)
			this.suggestedMax = Double.parseDouble(suggestedMax);

		objects = extractData("type");
		selection = extractData("selection");
		String visibleRows = extractData("visible_rows");
		if(visibleRows != null) {
			this.visibleRows = Integer.parseInt(visibleRows);
		}

	}

	public NodeData() {
		
	}

	@Override
	public String toString() {
		return key + ";" + label + ";" + required + ";" + defaultValue + ";" + units + ";" + defaultUnit + ";" + 
				regex + ";" + absoluteMin + ";" + absoluteMax + ";" + suggestedMin + ";" + suggestedMax + ";" + 
				objects + ";" + selection + ";" + visibleRows;
	}
	
	public String getName() {
		return name;
	}

	public Node getNode() {
		return node;
	}

	public String getKey() {
		return key == null ? label : key;
	}

	public String getLabel() {
		return label;
	}

	public Boolean isRequired() {
		if(required == null)
			return false;
		return required;
	}
	public Boolean isEnabled() {
		if(enabled == null)
			return false;
		return enabled;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public String getUnits() {
		return units;
	}

	public String getDefaultUnit() {
		return defaultUnit;
	}

	public String getRegex() {
		return regex;
	}

	public Double getAbsoluteMin() {
		return absoluteMin;
	}

	public Double getAbsoluteMax() {
		return absoluteMax;
	}

	public Double getSuggestedMin() {
		return suggestedMin;
	}

	public Double getSuggestedMax() {
		return suggestedMax;
	}

	public String getObjects() {
		return objects;
	}

	public String getSelection() {
		return selection;
	}

	public Integer getVisibleRows() {
		return visibleRows;
	}
	
	public void setNode(Node node) {
		this.node = node;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void setDefaultUnit(String defaultUnit) {
		this.defaultUnit = defaultUnit;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public void setAbsoluteMin(Double absoluteMin) {
		this.absoluteMin = absoluteMin;
	}

	public void setAbsoluteMax(Double absoluteMax) {
		this.absoluteMax = absoluteMax;
	}

	public void setSuggestedMin(Double suggestedMin) {
		this.suggestedMin = suggestedMin;
	}

	public void setSuggestedMax(Double suggestedMax) {
		this.suggestedMax = suggestedMax;
	}

	public void setObjects(String objects) {
		this.objects = objects;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public void setVisibleRows(Integer visibleRows) {
		this.visibleRows = visibleRows;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String extractData(String attribute) {
		Node attributeNode = node.getAttributes().getNamedItem(attribute);
		if(attributeNode != null)
			return attributeNode.getNodeValue();
		return null;
	}

	public String getFlatConstraint() {
		String regex = getRegex();
		String defaultValue = getDefaultValue();
	
		if(regex != null)
			return regex;
	
		Double absoulteMin = getAbsoluteMin();
		Double suggestedMin = getSuggestedMin();
		Double absoluteMax = getAbsoluteMax();
		Double suggestedMax = getSuggestedMax();

		String absoulteMinValue = (absoulteMin != null ? String.valueOf(absoulteMin) : "");
		String suggestedMinValue = (suggestedMin != null ? String.valueOf(suggestedMin) : "");
		String absoulteMaxValue = (absoluteMax != null ? String.valueOf(absoluteMax) : "");
		String suggestedMaxValue = (suggestedMax != null ? String.valueOf(suggestedMax) : "");

		if(absoulteMin == null && suggestedMin == null && absoluteMax == null && suggestedMax == null) { // no constraints
			return "";
		}

		String defaultUnit = getDefaultUnit();

		// Will be in the form: [absolute_min, [suggested_min, suggested_max], absolute_max], default
		// Blanks will be filled in with "none"
		
		String topRow =  defaultValue + (defaultUnit != null ? " " + defaultUnit : "");
		String secondRow = "[" + absoulteMinValue + ", [" + suggestedMinValue + ", " + suggestedMaxValue +"], " + absoulteMaxValue + "]";
	
		if(defaultValue == null)
			return "";
		if(defaultValue.isEmpty()) {
			return secondRow;
		} else {
			return topRow+", "+secondRow;
		}
	}
	
	public String getConstraint() {
		String regex = getRegex();
		String defaultValue = getDefaultValue();
	
		if(regex != null)
			return "<html><i>" +  regex + "</i></html>";
	
		Double absoulteMin = getAbsoluteMin();
		Double suggestedMin = getSuggestedMin();
		Double absoluteMax = getAbsoluteMin();
		Double suggestedMax = getSuggestedMax();

		String absoulteMinValue = (absoulteMin != null ? String.valueOf(absoulteMin) : "");
		String suggestedMinValue = (suggestedMin != null ? String.valueOf(suggestedMin) : "");
		String absoulteMaxValue = (absoluteMax != null ? String.valueOf(absoluteMax) : "");
		String suggestedMaxValue = (suggestedMax != null ? String.valueOf(suggestedMax) : "");

		if(absoulteMin == null && suggestedMin == null && absoluteMax == null && suggestedMax == null) { // no constraints
			return "";
		}

		String defaultUnit = getDefaultUnit();

		// Will be in the form: [absolute_min, [suggested_min, suggested_max], absolute_max], default
		// Blanks will be filled in with "none"
		
		String topRow = "<i><b>" + defaultValue + (defaultUnit != null ? " " + defaultUnit : "") + "</b></i>";
		String secondRow = "<i>[" + absoulteMinValue + ", <font color=\"#E64517\">[" + suggestedMinValue + ", " + absoulteMaxValue +"]</font>, " + suggestedMaxValue + "]</i>";
	
		if(defaultValue == null)
			return "";
		if(defaultValue.isEmpty()) {
			return "<html>"+secondRow+"</html>";
		} else {
			return "<html><table cellpadding=\"=0\"><tr><td height=\"2px\">"+topRow+"</td></tr><tr><td height=\"0px\">"+secondRow+"</td></tr></table></html>";
		}
	}

	public boolean hasUnits() {
		return getUnits() != null;
	}

	public boolean isType(String type) {
		return this.type.equals(type);
	}

	public String getType() {
		return type;
	}


}
