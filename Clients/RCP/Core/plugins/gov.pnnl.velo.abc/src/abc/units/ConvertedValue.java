package abc.units;

import java.text.DecimalFormat;

/**
 * So we can ask for converted values in any format.  Converter returns double.
 * @author port091
 *
 */
public class ConvertedValue {

	private Double value;

	/**
	 * Safe constructor, uses default value 0.0
	 * @param value
	 */
	public ConvertedValue(Double value) {
		this.value = value == null ? 0.0 : value;
	}

	public double asDouble() { return this.value; }

	public float asFloat() { return value.floatValue(); }

	public int asInteger() { return value.intValue(); }

	public String asString() { return String.valueOf(value); }
	
	public String asFormattedExponentialString() { return new DecimalFormat("0.0000E00").format(value); }
}
