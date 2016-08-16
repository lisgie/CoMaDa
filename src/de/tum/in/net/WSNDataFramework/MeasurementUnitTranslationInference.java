package de.tum.in.net.WSNDataFramework;

import javax.script.ScriptException;

/**
 * Inferred Unit Translation.
 * Used to concatenate two UnitTranslations.
 * 
 * @author André Freitag
 */
public class MeasurementUnitTranslationInference extends MeasurementUnitTranslation {

	/* constructors */
	/**
	 * Concatenates two UnitTranslations.
	 * 
	 * @param _originalUnit first UnitTranslation to be executed
	 * @param _destinationUnit second UnitTranslation to be executed
	 * @throws ScriptException
	 */
	public MeasurementUnitTranslationInference(MeasurementUnitTranslation first, MeasurementUnitTranslation second) {
		super(first._unitType, first._originalUnit, second._destinationUnit);

		_script = null;
		_first = first;
		_second = second;
	}


	/* overridden methods */
	@Override
	public Object translate(Object value) throws ScriptException {
		return _second.translate(_first.translate(value));
	}


	/* protected member */
	protected MeasurementUnitTranslation _first;
	protected MeasurementUnitTranslation _second;
}
