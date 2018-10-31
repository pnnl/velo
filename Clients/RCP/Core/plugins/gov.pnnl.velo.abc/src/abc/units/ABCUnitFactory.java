package abc.units;

/**
 * A factory to manage the application ABCUnit class.
 * By default an instance of ABCUnit is used but this can be 
 * overridden by the applications.
 * The unit class is managed as a singleton.
 * @author karen
 *
 */
public class ABCUnitFactory {
	
	static ABCUnits unitClass = null;
	
	public static ABCUnits getABCUnits() {
		if (unitClass == null) {
			unitClass = new ABCUnits();
		}
		return unitClass;
	}
	
	public static void setABCUnits(ABCUnits abcunit) {
		unitClass = abcunit;
	}

}
