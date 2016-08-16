package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.simple.JSONValue;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.NodeCollection;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.WSNProtocol;
import de.tum.in.net.WSNDataFramework.Crypto.TinyTO;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest.URIArgs;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.UnsignedDataInputStream;
import de.tum.in.net.WSNDataFramework.Modules.IPFIX.WSNTinyIPFIXModule;
import de.tum.in.net.WSNDataFramework.Modules.TinyOS.TinyOSDriver;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;






/**
 * Index controller.
 * 
 * @author André Freitag
 *
 */
public class WSNHTTPIndexController extends WSNHTTPController {

	/**
	 * Index action.
	 * 
	 * @param request
	 * @param response
	 */
	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));

		// load module states
		Map<String, List<String>> jsModules = new TreeMap<String, List<String>>();

		if (this.module()!=null && this.module().app()!=null) {
			WSNModule[] modules = this.module().app().modules();

			for (WSNModule m: modules) {
				jsModules.put(
						m.getClass().getName(),
						Arrays.asList(new String[]{
								m.getName()!=null ? m.getName() : m.getClass().getName(),
										m.getStatus().getStatus().toString(),
										m.getStatus().getMessage()
						})
						);
			}
		}

		doc.addVar("wsn_driver", this.module()!=null&&this.module().app()!=null&&this.module().app().getDriver()!=null ? this.module().app().getDriver().getName() : "NONE");
		List<String> jsProtocols=new ArrayList<String>();
		if (this.module()!=null && this.module().app()!=null && this.module().app().getProtocolStack()!=null) {
			for (WSNProtocol p: this.module().app().getProtocolStack()) {
				jsProtocols.add(p.getName());
			}
		}
		doc.addVar("wsn_protocols", jsProtocols);

		doc.addVar("module_states", jsModules);


		response.body = doc.toBytes();
	}

	/**
	 * Hardware-Default-Action
	 * @param request
	 * @param response
	 */
	public void hardwareAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("hardware.html"));

		response.body = doc.toBytes();
	}

	/**
	 * Visualisation-Default-Action
	 * @param request
	 * @param response
	 */
	public void visualisationAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("visualisation.html"));

		response.body = doc.toBytes();
	}

	/**
	 * nodes action
	 * 
	 * @param request
	 * @param response
	 */
	public void nodesAction(HTTPRequest request, HTTPResponse response) {
		try {HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("nodes.html"));

		doc.addVar("wsnnodes", this.getServerModule().getNodesVar());
		//doc.addHtml(this.getServerModule().getTemplate("node.html"));
		doc.addHtml(this.getServerModule().getTemplate("topology.html"));

		response.body = doc.toBytes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * AJAX perform node action
	 * 
	 * @param request
	 * @param resposne
	 */
	public void performnodeactionAction(HTTPRequest request, HTTPResponse response) {
		Map<String,Object> ajaxRet = new HashMap<String,Object>();

		String err=null;

		Node node = this.getServerModule().app().wsn().node(request.arguments.get("nodeID").toString());
		if (node != null) {
			Class<? extends NodeAction> nodeActionClass=null;
			for (Class<? extends NodeAction> na: node.getOfferedActions()) {
				if (na.getName().equals(request.arguments.get("action").toString())) {
					nodeActionClass = na;
					break;
				}
			}

			if (nodeActionClass != null) {
				NodeAction action = null;
				try {
					Map<String,Object> params = new HashMap<String,Object>();
					for (String param: request.arguments.get("params").keySet()) {
						params.put(param, request.arguments.get("params").get(param).toString());
					}
					action = NodeAction.instantiate(nodeActionClass, params);
				} catch (Exception e) {
					err = "couldn't instantiate action: " + e.getMessage();
				}

				if (action != null) {
					try {
						node.performAction(action);
						// TODO: asynchronously propagate action result!

						ajaxRet.put("msg", "action performed");
					} catch (Exception e) {
						err = "couldn't perform action: " + e.getMessage();
					}
				}
			} else {
				err = "action not available";
			}
		} else {
			err = "node not available";
		}

		if (err != null)
			ajaxRet.put("err", err);
		response.body = JSONValue.toJSONString(ajaxRet).getBytes();
	}

	/**
	 * log files
	 * 
	 * @param request
	 * @param response
	 */
	public void logsAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("logs.html"))
				.addVar("log_link", this.module().app().getFilesDirectory());

		response.body = doc.toBytes();
	}
	/**
	 * index action
	 * 
	 * @param request
	 * @param response
	 */
	public void protocolsAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("protocols.html"));
		try {

			doc.addVar("wsnnodes", this.getServerModule().getNodesVar());
			doc.addHtml(this.getServerModule().getTemplate("topology.html"));
			doc.addHtml(this.getServerModule().getTemplate("node.html"));

			response.body = doc.toBytes();
		} catch(Exception e) {
			e.printStackTrace();
		}
		TinyOSDriver tinyOSDriver = module().app().module(
				TinyOSDriver.class);
		if(tinyOSDriver == null){
			System.out.println("TinyOSDriver does not yet exist");
		} else {
			URIArgs doPullArgs = request.arguments.get("pull_submit");
			URIArgs pullIdArgs = request.arguments.get("node_id");
			if(doPullArgs != null && pullIdArgs != null && pullIdArgs.isString()){
				try {
					int pullId = Integer.parseInt(pullIdArgs.toString());
					if(pullId < 0 || pullId > 10000){
						return;
					}
					
					NodeCollection nodes = module().app().wsn().nodes();
					Node pullNode = nodes.get(new NodeID(pullIdArgs.toString()));
					System.out.println("Number of nodes in the WSN is " + nodes.size());
					if(pullNode != null){
						DatagramSocket socket = tinyOSDriver.getServerSocket();
						
						InetSocketAddress pullNodeAddress = pullNode.getAddress();
				
						byte[] dataAsBytes = generateDummyPacket();
//						byte[] ciphertext = TinyTO.encryptECIES(pullNodeAddress, dataAsBytes);
//						socket.send(new DatagramPacket(ciphertext, ciphertext.length, pullNodeAddress.getAddress(), pullNodeAddress.getPort()));
						socket.send(new DatagramPacket(dataAsBytes, dataAsBytes.length, pullNodeAddress.getAddress(), pullNodeAddress.getPort()));
					} else {
						System.err.println("Node with ID " + pullIdArgs + " was not found!");
					}
	
				} catch (NumberFormatException e){
					System.err.println("The provided node id " + pullIdArgs + " is not valid. Pull request denied.");
					return;
				} catch (IOException e){
					e.printStackTrace();
				}

				
			}
		}
		
	}
	
	private byte[] generateDummyPacket(){
		ByteArrayOutputStream packetAsStream = new ByteArrayOutputStream();
		short version = 10;
		short length = 24;
		int exportTime = 0;
		int sequenceNr = 0;
		int observation = 0;
		short setId = 300;
		short setLength = 8;
		int payload = 42;
		
		try {
			packetAsStream.write(shortToByte(version));
			packetAsStream.write(shortToByte(length));
			packetAsStream.write(intToByte(exportTime));
			packetAsStream.write(intToByte(sequenceNr));
			packetAsStream.write(intToByte(observation));
			packetAsStream.write(shortToByte(setId));
			packetAsStream.write(shortToByte(setLength));
			packetAsStream.write(intToByte(payload));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return packetAsStream.toByteArray();
	}
	
	private byte[] shortToByte(short a){
		return ByteBuffer.allocate(2).putShort(a).array();
	}
	
	private byte[] intToByte(int a){
		return ByteBuffer.allocate(4).putInt(a).array();
	}
	/**
	 * index action
	 * 
	 * @param request
	 * @param response
	 */
	public void dynamicAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("dynamic.html"));
		try {

			doc.addVar("wsnnodes", this.getServerModule().getNodesVar());
			doc.addHtml(this.getServerModule().getTemplate("topology.html"));
			doc.addHtml(this.getServerModule().getTemplate("node.html"));

			response.body = doc.toBytes();
		}catch(Exception e) {e.printStackTrace();}
	}


	/**
	 * ajax update action
	 * 
	 * @param request
	 * @param response
	 */
	public void protocolsupdateAction(HTTPRequest request, HTTPResponse response) {
		String log=null;
		if (this.module()!=null && this.module() instanceof WSNHTTPServerModule) {
			WSNHTTPServerModule m = (WSNHTTPServerModule) this.module();
			log = m.getProtocolsLog();
		}


		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		jsonResult.put("text", log);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}
	/**
	 * ajax update action
	 * 
	 * @param request
	 * @param response
	 */
	public void rawhexupdateAction(HTTPRequest request, HTTPResponse response) {
		String log=null;
		if (this.module()!=null && this.module() instanceof WSNHTTPServerModule) {
			WSNHTTPServerModule m = (WSNHTTPServerModule) this.module();
			log = m.getRawhexLog();
		}


		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		jsonResult.put("text", log);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	/**
	 * Publications
	 * 
	 * @param request
	 * @param response
	 */
	public void publicationsAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("publications.html"));

		response.body = doc.toBytes();
	}


	/**
	 * "under constructoin"
	 * 
	 * @param request
	 * @param response
	 */
	public void underconstructionAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("under_construction.html"));

		response.body = doc.toBytes();
	}

	/**
	 * AJAX Number-Convert-Tool
	 * 
	 * @param request
	 * @param resposne
	 */
	public void numberconverterAction(HTTPRequest request, HTTPResponse response) {
		Map<String,Object> ajaxRet = new HashMap<String,Object>();

		// add entry if requested
		if (request.arguments.containsKey("add")) {
			_numberConverterEntries.put(request.arguments.get("add").get("hex").toString(), true);
		}
		// remove entry if requested
		if (request.arguments.containsKey("rem")) {
			_numberConverterEntries.remove(request.arguments.get("rem").get("hex").toString());
		}

		// set "open" / "closed"
		if (request.arguments.containsKey("status")) {
			_numberConverterOpen = request.arguments.get("status").toString().equals("open") ? true : false;
		}

		response.body = JSONValue.toJSONString(ajaxRet).getBytes();
	}
	protected Map<String,Boolean> _numberConverterEntries = new LinkedHashMap<String,Boolean>();
	protected boolean _numberConverterOpen=false;


	/**
	 * shuts WSN down
	 * 
	 * @param request
	 * @param response
	 */
	public void shutdownAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("shutdown.html"));

		response.body = doc.toBytes();
	}

	public void shutdownajaxAction(HTTPRequest request, HTTPResponse response) {
		if (this.module()!=null && this.module().app()!=null) {
			this.module().app().shutdown();
		}
	}

}
