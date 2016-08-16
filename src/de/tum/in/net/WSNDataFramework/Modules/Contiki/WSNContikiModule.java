package de.tum.in.net.WSNDataFramework.Modules.Contiki;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPServerModule;

public class WSNContikiModule extends WSNModule {
	
	//private Process shellCommunicator;
	private BufferedReader stdInput;
	
	@Override
	public String getName()  {
		return "Contiki";
	}

	@Override
	protected void _init() {
		_moduleDependent(WSNHTTPServerModule.class, "_moduleDep");
		_setIdling("contiki");
	}

	protected void _moduleDep(WSNHTTPServerModule m) {
		try{
			m.registerController("contiki", HTTPController.class, this)
			.getServerModule().registerLink(new String[]{"Network Management","Operating Systems","Contiki"}, "/contiki")
			.setLinkSortOrder(new String[]{"Network Management","Operating Systems","Contiki"}, 50);
		}catch(InstantiationException e){
			System.err.println("The Controller could not be registred.");
		}
	}
	
	/**
	 * This Method is used to communicate with the shell
	 * */
	public ArrayList<String> callShell(String cmd, String workingDr){
		ArrayList<String> output = new ArrayList<String>();
		ArrayList<String> fullCommand = new ArrayList<String>();

		//Construct the commands in an array for 
		fullCommand = commandConstructor(cmd);
		
		//Shell call and read
		try {
			Process p = null;
			ProcessBuilder pb = new ProcessBuilder(fullCommand.toArray(new String[fullCommand.size()]));
			pb.directory(new File(workingDr));
			p = pb.start();
			stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String thisLine = null;
			while ((thisLine = stdInput.readLine()) != null){
				output.add(thisLine);
			}
		} catch (IOException e) {
			System.err.println("Something went wrong with the command: " + cmd);
			output.add("Something went wrong.");
			e.printStackTrace();
		}
		return output;
	}
	
	
	/**
	 * In this method are all the project names, working directions and descriptions stored.
	 * It is call by HTTPController, when the GUI request all the configuration parameters.
	 * */
	public JSONObject getConfigs(){
		//The first argument is the name of the project, the second one is the working directory
		//Add node projects
		JSONObject helloWorld = new JSONObject();
		helloWorld.put("projectName", "Hello World");
		helloWorld.put("workingDr", "/home/livio/workspace/contiki-2.7/examples/hello");
		helloWorld.put("description", "This project is a sample project of the Contiki platform.");
		
		JSONObject node = new JSONObject();
		node.put("projectName", "TinyIPFIX");
		node.put("workingDr", "/home/livio/workspace/TelosB_TinyIPFIX");
		node.put("description", "This module is the TinyIPFIX Protocol for Contiki.");
		
		//Merge node configurations
		JSONArray nodes = new JSONArray();
		nodes.add(helloWorld);
		nodes.add(node);
		
		//Add border projects
		JSONObject borderNode = new JSONObject();
		borderNode.put("projectName", "Border Router");
		borderNode.put("workingDr", "/home/livio/workspace/contiki-2.7/examples/ipv6/rpl-border-router");
		borderNode.put("description", "This configuration is the basic border router configuration.");
		
		//Merge border configurations
		JSONArray borders = new JSONArray();
		borders.add(borderNode);
		
		//Add platforms
		JSONObject sky = new JSONObject();
		sky.put("platformName", "sky");
		
		JSONObject esp = new JSONObject();
		esp.put("platformName", "esp");
		
		
		//Merge platforms
		JSONArray platforms = new JSONArray();
		platforms.add(esp);
		platforms.add(sky);
		
		//Merge nodes configs and border router configs
		JSONObject configs = new JSONObject();
		configs.put("nodes", nodes);
		configs.put("borders", borders);
		configs.put("platforms", platforms);
		
		return configs;
	}
	
	/**
	 * @param: String as written in shell
	 * @return: ArrayList of String, so it can be executed in the ProcessBuilder
	 * */
	private ArrayList<String> commandConstructor(String cmd){
		ArrayList<String> fullCommand = new ArrayList<String>();
		String[] tokens = cmd.split(" ");
		for(String token : tokens){
			fullCommand.add(token);
		}
		return fullCommand;
	}
}
