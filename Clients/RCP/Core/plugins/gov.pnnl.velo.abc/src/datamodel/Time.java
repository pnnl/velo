package datamodel;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import abc.units.ABCUnitFactory;

public class Time extends NamedItem implements Comparable<Time> {

	private static final long serialVersionUID = 1L;
	
	Measure<Double, Duration> time;
	
	public Time() {
		this(0.0, "s");
	}
	
	public Time(DataItem dataItem) {
	  this(dataItem.getValue(), dataItem.getUnit());
	}
	
	@SuppressWarnings("unchecked")
  public Time(String time, String unit) {
	  super(time);
    this.time = Measure.valueOf(Double.parseDouble(time), (Unit<Duration>)ABCUnitFactory.getABCUnits().unit("year"));
	}

	@SuppressWarnings("unchecked")
	public Time(double time, String unit) {
		this.time = Measure.valueOf(time, (Unit<Duration>)ABCUnitFactory.getABCUnits().unit("year"));
	}
	
	public static Time parseTime(String time) {
		time = time.replaceAll("[;, ]", "").trim();
		
		// May or may not have a unit, split at the last numerical index
		int lastNumber = 0;
		for(int i = 0; i < time.length(); i++) {
			if(Character.isDigit(time.charAt(i)))
					lastNumber = i;
		}
		if(lastNumber < time.length() - 1) {
			String value = time.substring(0, lastNumber+1);			
			String unit = time.substring(lastNumber+1, time.length());
			return new Time(value, unit);
		} else {
			// No unit
			return new Time(time, "s");
		}
	}	
	
	private Measure<Double, Duration> getTime() {
		return time;
	}

	@Override
	public String toString() {
		if(getAlias() != null) {
			return getAlias() + time.getUnit().toString();
		}
		return time.toString();
	}
	
	@Override
	public int compareTo(Time compareTo) {
		return time.compareTo(compareTo.getTime());
	}
}
