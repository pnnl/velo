package abc.units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import static javax.measure.unit.NonSI.*;



/**
 * Class to hold families of units (eg time), the units that belong to a family (eg seconds minutes days...) and unit converters
 * Units are saved as strings for easy reference
 * @author port091
 */
public abstract class AbstractABCUnits {

	// Map of unit families
	// Private to enforce adding of alii automatically
	private Map<String, List<Unit<?>>> UNIT_FAMILIES = new HashMap<String, List<Unit<?>>>();

	// Map of all aliases to the unit that produced them (because 1/cm does not parse, silly thing...)
	protected Map<String, Unit<?>> US_UNIT_ALIAS = new HashMap<String, Unit<?>>(); 

	protected List<UnitConverter> UNIT_CONVERTERS = new ArrayList<UnitConverter>();

	private static final Unit<Duration> YEAR_JULIEN = DAY.times(365.25);
	
	
	
	/**
	 * Returns days per year as 365.25.
	 * Override if you define it differently e.g. rounded to 365 for long term climate models
	 * @return
	 */
	public Unit<Duration> get_year_julien() {
		return YEAR_JULIEN;
	}
	
	/**
	 * Register alternative spellings for various unit names.
	 * e.g. year can be written as year, yr, y
	 *    	UnitFormat.getInstance(Locale.US).label(YEAR_JULIEN, "yr");
	 *    The first alternative will be used as the preferred spelling.
	 */
	public abstract void registerAlii();
	

	/**
	 * Define all unit families expected in the abc Xml document.
	 * Any units expected in an abc XML file should be added here.
	 */
	public abstract void defineFamilies();

	/**
	 * Returns the components of compound units.
	 * For example, velocity is a compound unit of length/time
	 * and would be returned as a list of three items:
	 *    1. {length units}
	 *    2. / (separator)
	 *    3 {time units}
	 * The units should be of type String[]
	 * TODO get rid of Object return type or make a class.
	 * @param family
	 * @return
	 */
	public abstract List<Object> getUnitParts(String family);


	/**
	 * Converts value from one unit to another.  
	 * Subclass if the units package doesn't know what how to perform the required conversion
	 * In this case, null will be returned.
	 * @param value
	 * @param convertFrom
	 * @param convertTo
	 * @return null if not compatible.
	 */
	public ConvertedValue convertValue(String value, String convertFrom, String convertTo) {
		System.out.println("convert value "+convertFrom + " "+convertTo);
		
		Unit<?> convertToUnit = unit(convertTo);
		Unit<?> convertFromUnit = unit(convertFrom);

		// Automatic conversion possible
		if(convertFromUnit.isCompatible(convertToUnit)) 
			return new ConvertedValue(convertFromUnit.getConverterTo(convertToUnit).convert(Double.parseDouble(value)));

		for (UnitConverter converter: UNIT_CONVERTERS) {

		
		}

		return null;
	}

	public boolean isCompatible(String from, String to) {
		Unit<?> convertToUnit = unit(to);
		Unit<?> convertFromUnit = unit(from);

		// Automatic conversion possible
		return convertFromUnit.isCompatible(convertToUnit);
	  
	}

	public boolean isFamilyMember(String family, String possibleMember) {
	  Unit<?> lookingFor = unit(possibleMember);
	  if (lookingFor != null) {
	    List<Unit<?>> members = UNIT_FAMILIES.get(family);
	    for (Unit<?> member: members) {
	      if (member.equals(lookingFor)) {
	        return true;
	      }

	    }
	  }
          return false;
	  
	}

	/**
	 * Replace or add a list of units to a family
	 * @param family
	 * @param units
	 */
	protected void registerUnitFamily(String family, Unit<?>[] units) {
	  clearFamily(family); // Clear existing in case an override has LESS units in a family
		for(Unit<?> unit: units)
			registerUnit(family, unit);		
	}

        public void registerUnitFamily( String family, List<Unit<?>> units) {
            clearFamily(family); // Clear existing in case an override has LESS units in a family
	    for(Unit<?> unit: units) {
	        registerUnit(family, unit);
            }
        }
	
	/**
	 * Get list of all registered family names.
	 * @return
	 */
	public Set<String> getFamilies() {
		return UNIT_FAMILIES.keySet();
	}
	
	/**
	 * Get all units in the specified family.
	 * @param family
	 * @return null if not found
	 */
	public List<Unit<?>> getUnits(String family) {
		return UNIT_FAMILIES.get(family);
		
	}

	public String[] getUnitFamily(String family) {
		if ( UNIT_FAMILIES.get(family) == null) {
			// TODO should this throw an exception - forgot what Ellen said
			if (!family.isEmpty() ) System.err.println("family not found: "+family +UNIT_FAMILIES.size());
			return new String[] {family};
		}
		String[] unitFamily = new String[UNIT_FAMILIES.get(family).size()];
		for(int i = 0; i < unitFamily.length; i++) 
			unitFamily[i] = UnitFormat.getInstance(Locale.US).format(UNIT_FAMILIES.get(family).get(i));
		return unitFamily;
	}
	
	public boolean doesFamilyExist(String family) {
		return UNIT_FAMILIES.get(family) != null;
	}

	// Gets the default form of the unit - similar to new alii methods above.
	// Get rid of these or the other ones.
	// This one doesn't let you specify a default alii format
	
	/**
	 * Returns the unit considering the alii first.
	 * @param unit
	 * @return
	 */
	public String getUnit(String unit) {
		if(US_UNIT_ALIAS.containsKey(unit)) // If we know about it
			return unit; // Already good
		// If we don't try the unit package - note, this may fail
		if(unit.startsWith("1"))
			return unit; // Darn package cannot handle units that start with 1/
		return UnitFormat.getInstance(Locale.US).format(Unit.valueOf(unit)); 
	}
		
	public Unit<?> unit(String unit) {   
  	if(US_UNIT_ALIAS.containsKey(unit)) // If we know about it
			return US_UNIT_ALIAS.get(unit);
		if(unit.startsWith("1"))
			return US_UNIT_ALIAS.get(unit); // Darn package cannot handle units that start with 1/
                try {
		return Unit.valueOf(unit); // If we don't try the unit package - note, this may fail
                 } catch (Exception ex) {
		  System.err.println("Rare units problem for: "+unit+ " " +ex.getMessage());
		}
		return null;
	}
	
	protected void clearFamily(String family) {
		if(UNIT_FAMILIES.containsKey(family))  {
		   UNIT_FAMILIES.remove(family);
		}
	  
	}
	
	
	/**
	 * Append a unit to a current unit family
	 * @param family
	 * @param unit
	 */
	public void registerUnit(String family, Unit<?> unit) {
		if(!UNIT_FAMILIES.containsKey(family)) 
			UNIT_FAMILIES.put(family, new ArrayList<Unit<?>>());		

		//Not sure why but I was getting duplicate units so make sure not to add if its there already
		List<Unit<?>> units = UNIT_FAMILIES.get(family);
		if (units.contains(unit)) return;

		UNIT_FAMILIES.get(family).add(unit);		
		US_UNIT_ALIAS.put(UnitFormat.getInstance(Locale.US).format(unit), unit);
	}
	
	/**
	 * This method is a work around for the fact that this package isn't that flexible
	 * finding unit alii and in particular is not good with things that start with 1 as 
	 * in 1/m^3.
	 * @param unit
	 * @param alias
	 */
	public void registerAlias(Unit<?> unit, String alias) {
		  if (unit != null) US_UNIT_ALIAS.put(alias, unit);
	}


	/**
	 * TODO what exactly does this do?? and why?
	 * Package not necessarily working so not such what to do.
	 */
	protected void mapAliiToUnits() {
		for(String unitFamily: UNIT_FAMILIES.keySet()) {			
			for(Unit<?> unit: UNIT_FAMILIES.get(unitFamily)) {
				US_UNIT_ALIAS.put(UnitFormat.getInstance(Locale.US).format(unit), unit);
			}
		}
	}

	
	/**
	 * Register a unit conversion
	 */
	public void registerUnitConverter(UnitConverter unitConverter) {
		UNIT_CONVERTERS.add(unitConverter);
	}
	

	/**
	 * Returns true if the unit is expressed as a mathmatical relationship of other units.
	 *     eg. velocity is length/time
	 * @param family
	 * @return
	 */
	public boolean isCompoundUnit(String family) { 
		return !getUnitParts(family).isEmpty();
	}

	
	
}
