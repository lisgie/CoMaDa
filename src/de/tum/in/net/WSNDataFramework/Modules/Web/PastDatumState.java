package de.tum.in.net.WSNDataFramework.Modules.Web;

public class PastDatumState {
	
	private String _datumID;
	private String _name;
	private String _type;
	private String _value;
	private String _unit;
	
	public PastDatumState(String datumId, String name, String type, String value, String unit) {
		_datumID = datumId;
		_name = name;
		_type = type;
		_value = value;
		_unit = unit;
	}
	
	public String getID() {
		return _datumID;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getType() {
		return _type;
	}
	
	public String getValue() {
		return _value;
	}
	
	public String getUnit() {
		return _unit;
	}
}
