package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tum.in.net.WSNDataFramework.MeasurementUnitTranslation;

/**
 * Settings Controller
 * 
 * @author André Freitag
 */
public class WSNHTTPSettingsController extends WSNHTTPController {

	/**
	 * Index-Action
	 * @param request
	 * @param response
	 */
	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));

		response.body = doc.toBytes();
	}

	/**
	 * units
	 * 
	 * @param request
	 * @param response
	 */
	public void unitsAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("units.html"));

		/* handle POST */
		if (request.arguments.containsKey("setUnitTranslations")) {

			for (String unitType: request.arguments.get("setUnitTranslations").keySet()) {
				for (String originalUnit: request.arguments.get("setUnitTranslations").get(unitType).keySet()) {

					String destinationUnit = request.arguments.get("setUnitTranslations").get(unitType).get(originalUnit).toString();

					if (!destinationUnit.isEmpty()) {
						// activate
						this.getServerModule().app().applyUnitTranslation(unitType, originalUnit, destinationUnit);
					} else {
						// deactivate
						this.getServerModule().app().deactivateUnitTranslation(unitType, originalUnit);
					}

				}
			}

			doc.addVar("success", true);
		}

		// assemble JS-Available-Translations-Var
		/*
			{
				unitType: {
					originalUnit: [
					     {
					    	 unitType: unitType,
					    	 originalUnit: originalUnit,
					    	 destinationUnit: ..
					     }
					]
				}
			}
		 */
		Map<String,Map<String,List<Object>>> jsTranslations = new HashMap<String,Map<String,List<Object>>>();

		Set<MeasurementUnitTranslation> translations = this.getServerModule().app().getUnitTranslations();
		for (MeasurementUnitTranslation translation: translations) {
			if (!jsTranslations.containsKey(translation.getUnitType())) {
				jsTranslations.put(translation.getUnitType(), new HashMap<String,List<Object>>());
			}
			if (!jsTranslations.get(translation.getUnitType()).containsKey(translation.getOriginalUnit())) {
				jsTranslations.get(translation.getUnitType()).put(translation.getOriginalUnit(), new LinkedList<Object>());
			}

			Map<String,String> jsTranslation = new LinkedHashMap<String,String>();
			jsTranslation.put("unitType", translation.getUnitType());
			jsTranslation.put("originalUnit", translation.getOriginalUnit());
			jsTranslation.put("destinationUnit", translation.getDestinationUnit());
			jsTranslations.get(translation.getUnitType()).get(translation.getOriginalUnit()).add(jsTranslation);
		}

		// assemble JS-Active-Translations-Var
		/*
			unitType: {
				{
					originalUnit: destinationUnit
				}
			}
		 */
		Map<String,Map<String,String>> jsActiveTranslations = new HashMap<String,Map<String,String>>();

		Set<MeasurementUnitTranslation> activeTranslations = this.getServerModule().app().getUnitTranslationsApplied();
		for (MeasurementUnitTranslation translation: activeTranslations) {
			if (!jsActiveTranslations.containsKey(translation.getUnitType())) {
				jsActiveTranslations.put(translation.getUnitType(), new HashMap<String,String>());
			}

			jsActiveTranslations.get(translation.getUnitType()).put(translation.getOriginalUnit(), translation.getDestinationUnit());
		}


		// add vars
		doc.addVar("availableTranslations", jsTranslations);
		doc.addVar("activeTranslations", jsActiveTranslations);

		response.body = doc.toBytes();
	}

}
