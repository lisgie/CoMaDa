package de.tum.in.net.WSNDataFramework.Modules.Contiki;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTMLDocument;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPResponse;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPController;

/**
 * @author		Sebastian Pinegger
 * @version		1.0
 * @since		28.08.2015
 * 
 * This class handles all the HTTP Request concerning the contiki interface
 * in the WSNFramework. It can be found under localhost:8000/contiki
 * */

public class HTTPController extends WSNHTTPController {
	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));
		response.body = doc.toBytes();
	}
	
	
	/**
	 * @param: json file with command configurations
	 * @return: json file with the output of the shell and information on RAM and ROM
	 * 
	 * This function calls the domake function in WSNContikiModule
	 * to execute the shell command, which is also constructed in it
	 * */
	public void domakeAction(HTTPRequest request, HTTPResponse response){
		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		ArrayList<String> outputMake = new ArrayList<String>();
		ArrayList<String> outputRamRom = new ArrayList<String>();
		
		//Read parameters from json file
		String platform = request.arguments.get("platform").toString();
		String cmdClean = "make clean";
		String cmd = "make TARGET=" + platform;
		String workingDr = request.arguments.get("workingDr").toString();
		
		//Start Commands
		WSNContikiModule module = (WSNContikiModule)this.module();
		module.callShell(cmdClean, workingDr);
		outputMake = module.callShell(cmd, workingDr);
		
		
		//Convert to output of shell to JSON
		String jsonString = "";
		for(int i = 0; i < outputMake.size(); i++){
			jsonString = jsonString + outputMake.get(i) + " <br>";
		}
		jsonResult.put("output", jsonString);
		
		//Get and add RAM and ROM to JSON
		String projectName = outputMake.get(outputMake.size()-2);
		projectName = projectName.substring(projectName.lastIndexOf(" ")+1);
		String cmdRamRom = "msp430-size " + projectName;
		outputRamRom = module.callShell(cmdRamRom, workingDr);
		int ram = 0;
		int rom = 0;
		String[] informationRamRom = outputRamRom.get(1).split("\\s+");
		rom = Integer.parseInt(informationRamRom[1]);
		ram = Integer.parseInt(informationRamRom[2]);
		ram = ram + Integer.parseInt(informationRamRom[3]);
		jsonResult.put("ram", String.valueOf(ram));
		jsonResult.put("rom", String.valueOf(rom));
		
		//Return shell output to HTML
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}
	
	/**
	 * @param: json file with command configurations
	 * @return: json file with the output of the shell after the install command
	 * */
	public void installAction(HTTPRequest request, HTTPResponse response){
		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		ArrayList<String> outputGetName = new ArrayList<String>();
		ArrayList<String> outputInstall = new ArrayList<String>();
		String projectName = "";
		
		String workingDr = request.arguments.get("workingDr").toString();
		String platform = request.arguments.get("platform").toString();
		
		//1. Get the file name from the workind directory
		String cmdGetName = "ls";
		WSNContikiModule module = (WSNContikiModule)this.module();
		outputGetName = module.callShell(cmdGetName, workingDr);
		for(int i = 0; i < outputGetName.size(); i++){
			if(outputGetName.get(i).endsWith("." + platform)){
				projectName = outputGetName.get(i).replace("." + platform, "");
			}
		}
		
		//Call shell to install .sky file
		String cmdInstall = "make TARGET=" + platform;
		cmdInstall = cmdInstall + " ";
		cmdInstall = cmdInstall + projectName;
		cmdInstall = cmdInstall + ".upload";
		outputInstall = module.callShell(cmdInstall, workingDr);
		
		//Convert to output of shell to JSON
		String jsonString = "";
		for(int i = 0; i < outputInstall.size(); i++){
			jsonString = jsonString + outputInstall.get(i) + " <br>";
		}
		jsonResult.put("output", jsonString);
		
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}
	
	/**
	 * Populate configurations for node sensor configuration
	 * */
	public void getnodeconfigurationAction(HTTPRequest request, HTTPResponse response){
		WSNContikiModule module = (WSNContikiModule)this.module();
		JSONObject configs = module.getConfigs();
		
		response.body = JSONValue.toJSONString(configs).getBytes();
	}
}