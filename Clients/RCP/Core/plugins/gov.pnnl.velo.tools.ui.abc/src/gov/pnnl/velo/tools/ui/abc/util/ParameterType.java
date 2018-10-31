package gov.pnnl.velo.tools.ui.abc.util;
public class ParameterType {

	// For the parameter type class
	public static enum Type {
		SERALIZABLE("seralizable"),
		UI_PARAMETER("ui_parameter"), 
		META_DATA("meta_data"), 
		FILE("file");
		private String type;
		private Type(String type) {
			this.type = type;
		}
		@Override
		public String toString() {
			return type;
		}
	}

	private Type type;	
	private String detail; // data model object, meta data key, file name

	public ParameterType(Type type, String detail) {
		this.type = type;
		this.detail = detail;
	}

	public Type getType() {
		return type;
	}

	public String getDetail() {
		return detail;
	}

	@Override
	public int hashCode() {
		return (detail + "" + type).hashCode();
	}	
}