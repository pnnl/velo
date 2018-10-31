package abc.units;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Duration;
import javax.measure.unit.CompoundUnit;
import javax.measure.unit.NonSI;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import static javax.measure.unit.SI.*;
import static javax.measure.unit.NonSI.*;



/**
 * Concrete unit class that holds a few basic unit families.
 * Applications should use this as an example and add their appropriate unit familes, units, alii, and converters.
 * 
 * @author port091
 */
public class ABCUnits extends AbstractABCUnits {
	
	public static final Unit<?> PSI = POUND.divide(INCH.pow(2));	

	public ABCUnits() {
		registerAlii();
		defineFamilies();
		mapAliiToUnits();
		
		registerUnitConverter(PASCAL.divide(NonSI.G).getConverterTo(PSI));
	}
	
	
	/**
	 * Register alternative spellings for various unit names.
	 * e.g. year can be written as year, yr, y
	 *    	UnitFormat.getInstance(Locale.US).label(YEAR_JULIEN, "yr");
	 *    
	 * You may also want to register labels to control how it displays in the UI.
	 *      e.g. UnitFormat.getInstance(Locale.US).label(CELSIUS, "°C"); 
	 */
	@Override
	public void registerAlii() {
		// Register these first - not sure why but comment existed prior to refactor

		// Pressure
		UnitFormat.getInstance().label(PSI, "psi");;

		// Temperature
		//UnitFormat.getInstance(Locale.US).label(CELSIUS, "ï¿½C");	 // Not sure why this was chosen at one point
    UnitFormat.getInstance(Locale.US).label(CELSIUS, "°C");	

		// These are needed so that users can just supply a simple C or F in input files
		UnitFormat.getInstance(Locale.US).alias(CELSIUS, "C");	
		UnitFormat.getInstance(Locale.US).alias(FAHRENHEIT, "F");	


		// Years
		UnitFormat.getInstance(Locale.US).label(get_year_julien(), "y");
		for(String year: new String[]{"year", "yr", "y", "years"}) {
			UnitFormat.getInstance(Locale.US).alias(get_year_julien(), year);
		}

                // Hours
		UnitFormat.getInstance(Locale.US).label(HOUR, "h");
		for(String hour: new String[]{"hour", "hours", "h", "hr"}) {
			UnitFormat.getInstance(Locale.US).alias(HOUR, hour);
		}

		// Days
		UnitFormat.getInstance(Locale.US).label(DAY, "d");
		for(String day: new String[]{"day", "d", "days"}) {
			UnitFormat.getInstance(Locale.US).alias(DAY, day);
		}
		
		// Kg?
		UnitFormat.getInstance(Locale.US).alias(KILOGRAM, "Kg");


	}
	
	/**
	 * Defines a few of the very most common unit families.  
	 * Any units expected in an abc XML file should be added here.
	 */
	@Override
	public void defineFamilies() {

		registerUnitFamily("area", new Unit<?>[]{
				METER.pow(2),
				CENTIMETER.pow(2), 
				MILLIMETER.pow(2), 
				FOOT.pow(2), 
				INCH.pow(2)});

			registerUnitFamily("length",  new Unit<?>[]{
					METER, 
					CENTIMETER,
					MILLIMETER, 
					FOOT, 
					INCH});

			registerUnitFamily("pressure", new Unit<?>[]{
					PASCAL, 
					SI.KILO(PASCAL),
					PSI, 
					BAR,
					ATMOSPHERE});

			registerUnitFamily("mass", new Unit<?>[]{
					KILOGRAM, 
					GRAM,
					SI.MILLI(GRAM),
					POUND}); 

			registerUnitFamily("temperature", new Unit<?>[]{
					KELVIN, 
					CELSIUS, 
					FAHRENHEIT});	
			
			registerUnitFamily("time",  new Unit<?>[]{
					get_year_julien(), 
					DAY,
					HOUR, 
					SECOND,
					SI.MILLI(SECOND)});	

			registerUnitFamily("volume", new Unit<?>[]{
					METER.pow(3), 
					CENTIMETER.pow(3),
					MILLIMETER.pow(3), 
					FOOT.pow(3), 
					INCH.pow(3),
					});
			
			registerUnitFamily("weight", new Unit<?>[]{
					KILOGRAM,
					GRAM,
					MILLI(GRAM),
					POUND});

			registerUnitFamily("velocity",  new Unit<?>[]{
					MILLIMETER.divide(get_year_julien()), 
					METER.divide(DAY), 
					METER.divide(HOUR), 
					METER.divide(SECOND), 
					FOOT.divide(SECOND),
					METER.divide(get_year_julien())});
			
			
			
	}

	/**
	 * Formats a unit string.
	 * For example, though the label might have been set to °C for the UI, we may want
	 * to serialize this to simply C.
	 * Also the units library tends to like to use the notation s^-1 instead of 1/s so
	 * we override this for velocity units.
	 * Subclasses should override this to add support for their custom units
	 * @param unit
	 * @return
	 */
  public String formatUnit(String unit) {
    if (unit == null)
      return "";
    if (unit.isEmpty())
      return "";
    if(unit.equals("year"))
      return "yr";
    if(unit.equals("h"))
      return "hr";
    if(unit.equals("day"))
      return "d";
    if(unit.equals("ft*s^-1"))  
      return "ft/s";
    if(unit.equals("m*h^-1"))
      return "m/hr";
    if(unit.equals("m*s^-1"))
      return "m/s";
    if(unit.equals("m*day^-1"))
      return ("m/day");
    if(unit.equals("mm*year^-1"))
      return ("mm/yr");
    if(unit.equals("°C"))
      return "C";
    if(unit.equals("°F"))
      return "F";
    if(unit.equals("pa"))
      return "Pa";
    return unit;    
  }
	
  public String getStandardUnit(String unit) {
    String formattedUnit = formatUnit(unit);
    Unit<?> standardUnit = unit(formattedUnit);
    return standardUnit != null ? standardUnit.toString() : unit;
  }
  
  public String getStandardCompoundUnit(String unit) {

    // Not good enough..., giving up, really shouldn't need this
    // unless our compound units got formatted at some point
    
    // Nieve approach, assume the parts are all /
    String[] individualUnits = (unit.split("/"));
    String returnUnit = "";
    for(String individualUnit: individualUnits) {
      returnUnit += (returnUnit.length() == 0 ? "" : "/") + getStandardUnit(individualUnit);      
    }
   
    return returnUnit; 
    /*  
    // Some units will be reformatted: mg·h/ft²
    // lb/(in²·mm)
    
    // Nieve approach, this is a pain to undo
    List<String> individualUnits = new ArrayList<String>();
    
    if(unit.startsWith("lb/(in²·")) {
      unit = "psi/" + unit.substring(8);
    }
    unit = unit.replaceAll("\\)", "");
    unit = unit.replaceAll("\\(", "");
    
    for(String splitOnMultiply: unit.split("·")) {
      for(String splitOnDivide: splitOnMultiply.split("/")) {        
        individualUnits.add(splitOnDivide.trim());
      }
    }
    Unit<?> standardUnit = unit(unit);
    
    // Some ordering of the individual units will work...
    for(int i = 0; i < 20; i++) {
      Collections.shuffle(individualUnits);      
      String arrayAsUnit = "";
      for(String individualUnit: individualUnits) 
        arrayAsUnit += (arrayAsUnit.length() == 0 ? "" : "/") + individualUnit;
      Unit<?> unitAttempt = unit(arrayAsUnit);
      if(unitAttempt.equals(standardUnit)) {
        // Found it
        return arrayAsUnit;
      }    
    }

    System.err.println("ABCUnits:248, couldn't find standard unit for comound unit: " + unit);
    return unit; // Gave up
    */
  }
	
  public boolean isUnit(String unitFrom, String unitTo) {
    Unit<?> standardFrom = unit(unitFrom);
    Unit<?> standardTo = unit(unitTo);
    if(standardFrom != null && standardTo != null)
      return standardFrom.equals(standardTo);
    return false; // Can't compare
  }
  
	/**
	 * Converts value from one unit to another.  
	 * 
	 * @param value
	 * @param convertFrom
	 * @param convertTo
	 * @return null if not compatible.
	 */
	@Override
	public ConvertedValue convertValue(String value, String convertFrom, String convertTo) {
    
	  if (convertFrom == null || convertTo == null) {
			return new ConvertedValue(Double.parseDouble(value));
	  }
			
		Unit<?> convertToUnit = unit(convertTo);
		Unit<?> convertFromUnit = unit(convertFrom);

		// Automatic conversion possible
		if(convertFromUnit!= null && convertToUnit != null && convertFromUnit.isCompatible(convertToUnit)) 
			return new ConvertedValue(convertFromUnit.getConverterTo(convertToUnit).convert(Double.parseDouble(value)));

		// TODO Push the override to an akunaUnits?

		// Create the unit converters that require the fluid density value
		// TODO
		
		// Custom conversion needed
		// TODO
		
		// Was not compatible or we don't know how to convert it
		return null;
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

	/**
	 * TODO get rid of Object return type or make a class.
	 * @param family
	 * @return
	 */
	@Override
	public List<Object> getUnitParts(String family) {
		List<Object> parts = new ArrayList<Object>();
    if(!this.doesFamilyExist(family))
      return parts;
		if(family.equals("flux") || family.equals("velocity")) {
			parts.add(getUnitFamily("length"));
			parts.add("/");
			parts.add(getUnitFamily("time"));
		} else if(family.equals("diffusivity")) {
			parts.add(getUnitFamily("area"));
			parts.add("/");
			parts.add(getUnitFamily("time"));
		} else if(family.equals("density")) {
			parts.add(getUnitFamily("weight"));
			parts.add("/");
			parts.add(getUnitFamily("volume"));	
		} else if(family.equals("volumetric flux")) {
			parts.add(getUnitFamily("volume"));
			parts.add("/");
			parts.add(getUnitFamily("area"));
			parts.add("/");
			parts.add(getUnitFamily("time"));	
		} else if(family.equals("mass flux")) {
			parts.add(getUnitFamily("weight"));
			parts.add("/");
			parts.add(getUnitFamily("area"));
			parts.add("/");
			parts.add(getUnitFamily("time"));	
		} else if(family.equals("pressure gradient")) {
			parts.add(getUnitFamily("pressure"));
			parts.add("/");
			parts.add(getUnitFamily("length"));
		} else if(family.equals("inverse density")) {
			parts.add(getUnitFamily("volume"));
			parts.add("/");
			parts.add(getUnitFamily("weight"));
		}
		return parts;
	}
	
	
	
	public static void main (String[] args) {
		// For testing...

		// getInstance returns characters with power in them, and degrees symbol (prefer these if the UI can handle them?)
		System.out.println("Units using UnitFormat.getInstance()");
		ABCUnits abcunits = new ABCUnits();
		
		for(String family: abcunits.getFamilies()) {
			System.out.print("\t" + family + ": ");
			for(Unit<?> unit: abcunits.getUnits(family)) {
				String unitAsString = UnitFormat.getInstance(Locale.US).format(unit);
				System.out.print(unitAsString + ", "); // Make sure we can parse this as well:
				try {
				//	System.out.print("[" + US_UNIT_ALIAS.get(unitAsString) + "], ");
				} catch(Exception e) {
					System.out.print("[ERROR], ");
				}
			}
			System.out.println();
		}	

		System.out.println(Arrays.toString(abcunits.getUnitFamily("area")));
	}
}

