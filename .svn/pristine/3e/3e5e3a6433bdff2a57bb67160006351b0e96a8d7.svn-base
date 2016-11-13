package de.tum.in.net.WSNDataFramework;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import de.tum.in.net.WSNDataFramework.Node.Datum;

/**
 * Describes how to translate one Data-Unit to another using JavaScript (see conf.xml).
 * (like how to translate °C into °F)
 * 
 * @author André Freitag
 */
public class MeasurementUnitTranslation {

	/* constructors */
	/**
	 * Constructs a UnitTranslation Object.
	 * 
	 * @param unitType initializes {@link #getUnitType() this.getUnitType()}
	 * @param originalUnit initializes {@link #getOriginalUnit() this.getOriginalUnit()}
	 * @param destinationUnit initializes {@link #getDestinationUnit() this.getDestinationUnit()}
	 * @param jScript JavaScript that does the translation (variable `value` within the script contains the value to translate)
	 * @throws ScriptException
	 */
	public MeasurementUnitTranslation(String unitType, String originalUnit, String destinationUnit, String jScript) throws ScriptException {
		this(unitType, originalUnit, destinationUnit);
		this._script = _scriptCompiler.compile(jScript);
	}
	/**
	 * protected constructor, used for initialization
	 * 
	 * @param type
	 * @param from
	 * @param to
	 * @see #UnitTranslation(String, String, String, String)
	 */
	protected MeasurementUnitTranslation(String type, String from, String to) {
		this._unitType = type;
		this._originalUnit = from;
		this._destinationUnit = to;
	}


	/* public getter */
	/**
	 * Unit Type (like Temperature)
	 * 
	 * @see #UnitTranslation(String, String, String, String)
	 */
	public String getUnitType() {
		return _unitType;
	}
	/**
	 * Original Unit (like °C)
	 * 
	 * @see #UnitTranslation(String, String, String, String)
	 */
	public String getOriginalUnit() {
		return _originalUnit;
	}
	/**
	 * Destination Unit (like °F)
	 * 
	 * @see #UnitTranslation(String, String, String, String)
	 */
	public String getDestinationUnit() {
		return _destinationUnit;
	}

	/* public methods */
	/**
	 * Translates a Value
	 * 
	 * @param value to translate
	 * @return translated value
	 * @throws ScriptException if something goes wrong
	 */
	public Object translate(Object value) throws ScriptException {
		Bindings bindings = _script.getEngine().createBindings();
		bindings.put("value", value);

		return _script.eval(bindings);
	}
	/**
	 * Applys the represented Translation to a WSNNode.
	 * 
	 * @param node
	 * @return true if translation was applied || false
	 */
	public boolean apply(Node node) {
		boolean ret = false;

		for (Node.Datum field: node.data()) {
			if ((field.getType()!=null ? field.getType().equals(_unitType) : _unitType==null)
					&& (field.getUnit()!=null ? field.getUnit().equals(_originalUnit) : _originalUnit==null)) {
				try {
					node.data().update(
							new Datum(field.getID().toString(), field.getName(), field.getType(), this.translate(field.getValue()), _destinationUnit)
							);
					ret = true;
				} catch (ScriptException e) {
					System.err.println("Couldn't apply Unit-Translation: " + _originalUnit + " -> " + _destinationUnit);
					e.printStackTrace();
				}
			}
		}

		return ret;
	}


	/* overridden methods */
	/**
	 * two UnitTranslations are considered equal if type,from and to equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MeasurementUnitTranslation) {
			MeasurementUnitTranslation ut = (MeasurementUnitTranslation) obj;

			boolean equals = ut._unitType!=null ? ut._unitType.equals(this._unitType) : this._unitType==null;
			equals = equals && (ut._originalUnit!=null ? ut._originalUnit.equals(this._originalUnit) : this._originalUnit==null);
			equals = equals && (ut._destinationUnit!=null ? ut._destinationUnit.equals(this._destinationUnit) : this._destinationUnit==null);
			return equals;
		}

		return false;
	}
	/**
	 * overridden hashcode (if two UnitTranslation equals() they have the same hashcode)
	 */
	@Override
	public int hashCode() {
		return new String(this._unitType.hashCode()+"|"+this._originalUnit.hashCode()+"|"+this._destinationUnit.hashCode()).hashCode();
	}
	@Override
	public String toString() {
		return "("+this._unitType+") " + this._originalUnit + " -> " + this._destinationUnit;
	}


	/* protected member */
	protected String _unitType;
	protected String _originalUnit;
	protected String _destinationUnit;

	protected CompiledScript _script;
	protected static Compilable _scriptCompiler = (Compilable) new ScriptEngineManager().getEngineByName("js");
}
