package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.tum.in.net.WSNDataFramework.AggregationNode;
import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.Datum;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Topology;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolPacketProcessedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolRawPacketReceivedEvent;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeAction;
import de.tum.in.net.WSNDataFramework.NodeAction.NodeActionParam;



/**
 * Module for handling Client Communication via HTTP.
 * @author André Freitag
 *
 */
public class WSNHTTPServerModule extends WSNModule {


	/**
	 * set up WSNHTTPServerModule. uses "html" as workingDirectory path.
	 * 
	 * @param port port to listen to
	 * @throws IOException if the given port is already in use
	 */
	public WSNHTTPServerModule(int port) throws IOException {
		this(port, "html");
	}
	/**
	 * set up WSNHTTPServerModule.
	 * 
	 * @param port port to listen to
	 * @param workingDirectory path to fetch files from. NULL for virtual mode.
	 * @throws IOException if the given port is already in use
	 */
	public WSNHTTPServerModule(int port, String workingDirectory) {

		try {
			// start HTTP server
			_server = new HTTPServer(port, workingDirectory);

			// register default controller
			try {
				// register index controller
				this.registerController(
						_server.getDefaultController(),
						WSNHTTPIndexController.class,
						this);

				// register settings controller
				this.registerController(
						"settings",
						WSNHTTPSettingsController.class,
						this);

				// register help controller
				this.registerController(
						"help",
						WSNHTTPHelpController.class,
						this);

				// register links
				this.registerLink(new String[]{"Home"}, "/")
				.setLinkSortOrder(new String[]{"Home"}, -999);
				this.registerLink(new String[]{"Network Management"}, "/index/hardware")
				.setLinkSortOrder(new String[]{"Network Management"}, -888);
				this.registerLink(new String[]{"Network Management", "Operating Systems"}, "/index/hardware")
				.setLinkSortOrder(new String[]{"Network Management", "Operating Systems"}, -999);
				this.registerLink(new String[]{"Network Management", "Data Management"}, "/index/hardware")
				.setLinkSortOrder(new String[]{"Network Management", "Data Management"}, -888);
				this.registerLink(new String[]{"Network Management", "Data Management", "Log-Files"}, "/index/logs")
				.setLinkSortOrder(new String[]{"Network Management", "Data Management", "Log-Files"}, 999);
				this.registerLink(new String[]{"Visualisation"}, "/index/visualisation")
				.setLinkSortOrder(new String[]{"Visualisation"}, -777);
				this.registerLink(new String[]{"Visualisation", "Topology"}, "/index/nodes")
				.setLinkSortOrder(new String[]{"Visualisation", "Topology"}, 777);
				this.registerLink(new String[]{"Visualisation", "Data-Packets"}, "/index/protocols");
				this.registerLink(new String[]{"Visualisation", "Dynamic View"}, "/index/dynamic");

				/*this.registerLink(new String[]{"Settings"}, "/settings/index")
				.setLinkSortOrder(new String[]{"Settings"}, 1100);
				this.registerLink(new String[]{"Settings","Measurement-Units"}, "/settings/units")
				.setLinkSortOrder(new String[]{"Settings","Measurement-Units"}, 0);*/
				this.registerLink(new String[]{"Network Management", "Tools"}, "/settings/units")
				.setLinkSortOrder(new String[]{"Network Management", "Tools"}, 999);

				this.registerLink(new String[]{"About"}, "/help/index")
				.setLinkSortOrder(new String[]{"About"}, 10000);
				this.registerLink(new String[]{"About","FAQ"}, "/help/faq")
				.setLinkSortOrder(new String[]{"About","FAQ"}, 0);
				this.registerLink(new String[]{"About","Interesting Links"}, "/help/links")
				.setLinkSortOrder(new String[]{"About","Interesting Links"}, 20);
				this.registerLink(new String[]{"About","Publications"}, "/help/publications")
				.setLinkSortOrder(new String[]{"About","Publications"}, 30);
				this.registerLink(new String[]{"About","Impressum"}, "/help/impressum")
				.setLinkSortOrder(new String[]{"About","Impressum"}, 40);


				this.registerWidget("widget:protocols", "/index/widgets/protocols/protocolWidget.js");
				this.registerWidget("widget:rawhex", "/index/widgets/protocols/rawhexWidget.js");
				this.registerWidget("widget:topology", "/index/widgets/topology/topologyWidget.js");

				_setRunning("up and running");
			} catch (InstantiationException e) {
				_setError("Couldn't create Index-Controller: " + e.getMessage());
			}
		} catch (IOException e1) {
			_setError("Couldn't start HTTP Server: " + e1.getMessage());
		}
	}

	/**
	 * name
	 */
	@Override
	public String getName() {
		return "HTTP Server";
	}

	/**
	 * get this server's working directory.
	 * 
	 * @return path to working directory | NULL if module is virtual (no working directory given)
	 */
	public String getWorkingDirectory() {
		return _server.getWorkingDirectory();
	}
	/**
	 * set this server's working directory.
	 * working directory used to fetch files from.
	 * 
	 * @param workingDirectory
	 * @return this for fluent interface
	 */
	public WSNHTTPServerModule setWorkingDirectory(String workingDirectory) {
		_server.setWorkingDirectory(workingDirectory);

		return this;
	}


	/**
	 * register a WSNHTTPController. each controller name may only be registered once.
	 * Handler gets called each time `/name/*` is requested.
	 * Uses `this.getWorkingDirectory()/controllerName` as working directory for the controller.
	 * 
	 * @param name controllerName
	 * @param controllerClass (must offer default constructor)
	 * @param module module the controller gets registered for
	 * @return newly created controller instance | NULL if controller was already registered
	 * @throws InstantiationException
	 */
	public <T extends WSNHTTPController> T registerController(String name, Class<T> controllerClass, WSNModule module) throws InstantiationException {
		T controller=null;
		try {
			controller = controllerClass.newInstance();
			controller.setModule(module)
			.setServerModule(this)
			.setWorkingDirectory(this.getWorkingDirectory()+"/"+name);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}

		if (controller != null && !_server.registerController(name, controller)) {
			controller = null;
		}

		return controller;
	}
	/**
	 * register a link to be shown in main template.
	 * 
	 * @return this for fluent interface
	 */
	public WSNHTTPServerModule registerLink(String[] menuItem, String link) {

		// add categories
		List<String> cur=new ArrayList<String>();
		for (int i=0; i<menuItem.length; i++) {
			if (!_registeredLinks.containsKey(cur)) {
				_registeredLinks.put(new ArrayList<String>(cur), new ArrayList<MenuLink>());
			}

			if (i < menuItem.length-1) {
				String[] nextArray = new String[cur.size()+1];
				for (int j=0; j<cur.size(); j++) {
					nextArray[j] = cur.get(j);
				}
				nextArray[nextArray.length-1] = menuItem[i];

				MenuLink ml = new MenuLink(nextArray, 0, null);
				if (ml.href==null && _registeredLinks.get(cur).contains(ml)) {
					MenuLink ml2 = _registeredLinks.get(cur).get(_registeredLinks.get(cur).indexOf(ml));
					ml.href = ml2.href;
					ml.order = ml2.order;
				}
				_registeredLinks.get(cur).add(ml);
			}

			cur.add(menuItem[i]);
		}

		// add link
		List<String> category = new ArrayList<String>();
		for (int i=0; i<menuItem.length-1; i++) {
			category.add(menuItem[i]);
		}
		MenuLink ml = new MenuLink(menuItem, 0, link);
		if (ml.href==null && _registeredLinks.get(category).contains(ml)) {
			MenuLink ml2 = _registeredLinks.get(category).get(_registeredLinks.get(category).indexOf(ml));
			ml.href = ml2.href;
			ml.order = ml2.order;
		}
		_registeredLinks.get(category).add(ml);

		return this;
	}
	/**
	 * adjust the ordering of the links.
	 * 
	 * @param menuItem
	 * @param order integer stating the importance of a link, the lesser the number the more important the link
	 * @return
	 */
	public WSNHTTPServerModule setLinkSortOrder(String[] menuItem, int order) {
		MenuLink mlo = new MenuLink(menuItem, order, null);

		if (_registeredLinks.containsKey(mlo.category)) {
			List<String> tmp = new ArrayList<String>();
			for (int i=0; i<menuItem.length; i++) {
				tmp.add(menuItem[i]);
			}
			for (MenuLink ml: _registeredLinks.get(mlo.category)) {
				if (ml.menuItem.equals(tmp)) {
					ml.order = order;
				}
			}
		}

		return this;
	}

	public WSNHTTPServerModule registerWidget(String widgetName, String templateUrl) {
		_widgets.put(widgetName, templateUrl);
		return this;
	}

	/**
	 * get registered controller
	 * 
	 * @return
	 */
	public Map<String,WSNHTTPController> getRegisteredControllers() {
		HashMap<String,WSNHTTPController> ret = new HashMap<String,WSNHTTPController>();

		Map<String,HTTPController> controller = _server.getRegisteredControllers();
		for (String cn: controller.keySet()) {
			HTTPController c = controller.get(cn);
			if (c instanceof WSNHTTPController) {
				ret.put(cn, (WSNHTTPController)c);
			}
		}

		return ret;
	}

	/**
	 * gets the main HTML template for this Server.
	 * 
	 * @return main template
	 */
	@SuppressWarnings("unchecked")
	public HTMLDocument getMainTemplate(HTTPRequest request) {
		HTMLDocument doc = new HTMLDocument();

		if (_server.getWorkingDirectory() != null) {
			File f = new File(_server.getWorkingDirectory() + "main.html");
			if (f.exists() && f.isFile()) {
				// load file
				doc = new HTMLDocument(f).setDefaultContainer("body");

				// assemble driver var
				Map<String,String> jsDriver = new HashMap<String,String>();
				jsDriver.put("name", this.app()!=null&&this.app().getDriver()!=null ? this.app().getDriver().getName() : null);
				jsDriver.put("status", this.app()!=null&&this.app().getDriver()!=null ? this.app().getDriver().getStatus().getStatus().toString().toLowerCase() : "error");
				jsDriver.put("msg", this.app()!=null&&this.app().getDriver()!=null ? this.app().getDriver().getStatus().getMessage() : "no driver specified");
				doc.addVar("driver", jsDriver);

				// add default action / controller name
				Map<String,String> uriDefaults=new LinkedHashMap<String,String>();
				uriDefaults.put("controller", _server.getDefaultController());
				uriDefaults.put("action", _server.getDefaultAction());

				doc.addVar("uri_defaults", uriDefaults);

				// navigation links
				Map<String,Object> jsLinks=new LinkedHashMap<String,Object>();
				List<String> jsCurrentLink=null;
				if (_registeredLinks.size() > 0) {

					//					for (List<String> l: _registeredLinksOrder.keySet()) {
					//						System.out.println(l);
					//						System.out.print("->");
					//						System.out.println(_registeredLinksOrder.get(l));
					//					}


					Queue<LinkIterationQueueItem> queue = new LinkedList<LinkIterationQueueItem>();
					queue.add(new LinkIterationQueueItem(_registeredLinks.get(new ArrayList<String>()), new ArrayList<String>()));
					String hrefForRequest=null;

					while (!queue.isEmpty()) {
						LinkIterationQueueItem qItem = queue.remove();
						if (qItem.links==null) continue;
						Collections.sort(qItem.links);

						for (MenuLink ml: qItem.links) {
							qItem.curLink.add(ml.name);
							Map<String,Object> curCat = jsLinks;
							for (String c: ml.category) {
								Object oc = curCat.get(c);

								if (oc instanceof Map) {
									curCat = (Map<String,Object>) oc;
								} else {
									curCat = null;
								}
							}

							if (curCat != null) {
								if (ml.isCategory(_registeredLinks)) {
									HashMap<String,Object> catEntry = new LinkedHashMap<String,Object>();
									catEntry.put("", ml.href);
									curCat.put(ml.name, catEntry);
									queue.add(new LinkIterationQueueItem(_registeredLinks.get(ml.menuItem), new ArrayList<String>(qItem.curLink)));
								} else {
									curCat.put(ml.name, ml.href);
								}

								// mark as possible link for the given request
								if ((hrefForRequest==null || (ml.href!=null && ml.href.length() > hrefForRequest.length()) &&_hrefMayBeForRequest(ml.href, request))) {
									hrefForRequest = ml.href;
									jsCurrentLink = new ArrayList<String>(qItem.curLink);
								}
							}
							qItem.curLink.remove(qItem.curLink.size()-1);
						}
					}

				}

				doc.addVar("module_links", jsLinks);
				doc.addVar("current_link", jsCurrentLink);

				// add Number-Converter var
				Map<String,Object> jsNumberConverterCache = new HashMap<String,Object>();
				jsNumberConverterCache.put("entries", ((WSNHTTPIndexController)this.getRegisteredControllers().get("index"))._numberConverterEntries);
				jsNumberConverterCache.put("open", ((WSNHTTPIndexController)this.getRegisteredControllers().get("index"))._numberConverterOpen);
				doc.addVar("numberConverterCache", jsNumberConverterCache);
			}
		}

		doc.addVar("widgets", _widgets);
		// TODO: create widget for node.html
		doc.addHtml(this.getTemplate("node.html"));

		return doc;
	}
	protected class LinkIterationQueueItem {
		public List<MenuLink> links;
		public List<String> curLink;

		public LinkIterationQueueItem(List<MenuLink> links, List<String> curLink) {
			this.links = links;
			this.curLink = curLink;
		}
	}
	protected boolean _hrefMayBeForRequest(String href, HTTPRequest request) {
		HTTPRequest hrefRequest = _server.replaceDefaultRequest(new HTTPRequest(href, ""));


		boolean ok=true;
		ok = ok && (hrefRequest.controller!=null ? hrefRequest.controller.equals(request.controller) : request.controller==null);
		ok = ok && (hrefRequest.action!=null ? hrefRequest.action.equals(request.action) : request.action==null);
		ok = ok && ((hrefRequest.actionpath!=null ? hrefRequest.actionpath.startsWith(request.actionpath) : request.actionpath==null)
				|| (request.actionpath!=null ? request.actionpath.startsWith(hrefRequest.actionpath) : hrefRequest.actionpath==null));

		return ok;
	}

	/**
	 * gets a file handle to a specific HTML/JS template of a specific controller
	 * 
	 * @param name
	 * @param controller
	 * @return
	 */
	public File getTemplate(String name) {
		return this.getTemplate(name, "index");
	}	/**
	 * gets a file handle to a specific HTML/JS template
	 * 
	 * @param name
	 * @param controller
	 * @return
	 */
	public File getTemplate(String name, String controller) {
		return new File(_server.getWorkingDirectory()+controller+"/templates/"+name);
	}

	/**
	 * assembles a Map of all nodes of the WSN for passing to a HTMLDocument.
	 * 
	 * @return
	 */
	public Map<String,Map<String,Object>> getNodesVar() {
		// load node values
		Map<String,Map<String,Object>> jsNodeData = new HashMap<String,Map<String,Object>>();

		/*
		 * 			1: {
						nodeID: 1,
						fields: [
						 	{fieldID: "1",
						 	 type: "NodeID",
						 	 value: "1",
						 	 unit: null,
						 	 name: "NodeID"},

						 	{fieldID: "2",
						 	 type: "Temperature",
						 	 value: "32",
						 	 unit: "°C",
						 	 name: "Temperature"}
						],

					 	sendsTo: [null],
					 	(aggregates: [],)
					 	(info: {x: y})
					},

		 */
		Map<NodeID,Node> nodes = this.app().wsn().nodes();
		Topology topology = this.app().wsn().topology();

		for (NodeID nodeID: nodes.keySet()) {
			Node node = nodes.get(nodeID);
			Map<String,Object> nodeEntry = new HashMap<String,Object>();

			nodeEntry.put("nodeID", node.getNodeID().toString());

			// add fields
			List<Map<String,String>> fieldsEntries = new ArrayList<Map<String,String>>();
			for (Datum field: node.data()) {
				Map<String,String> fieldEntry = new HashMap<String,String>();

				fieldEntry.put("fieldID", field.getID().toString());
				fieldEntry.put("type", field.getType());
				fieldEntry.put("value", field.getValue()!=null ? field.getValue().toString() : null);
				fieldEntry.put("unit", field.getUnit());
				fieldEntry.put("name", field.getName());

				fieldsEntries.add(fieldEntry);
			}
			nodeEntry.put("fields", fieldsEntries);

			// add topology
			List<String> sendsToEntry = new ArrayList<String>();

			if (topology != null) {
				Set<Topology.Link> links = topology.getLinks(node.getNodeID());
				for (Topology.Link link: links) {
					if (link.getSource().equals(node.getNodeID())) {
						sendsToEntry.add(link.getTarget() != null ? link.getTarget().toString() : null);
					}
				}
			}
			if (sendsToEntry.size() <= 0) { // if no topology information is available assume this node sends to the base station (NULL)
				sendsToEntry.add(null);
			}
			nodeEntry.put("sendsTo", sendsToEntry);

			// add aggregation info (if available)
			if (node instanceof AggregationNode) {
				AggregationNode aNode = (AggregationNode) node;

				List<String> aggrEntry = new ArrayList<String>();
				for (Node n: aNode.aggregatedNodes) {
					aggrEntry.add(n.getNodeID().toString());
				}

				nodeEntry.put("aggregates", aggrEntry);
			}

			// add additional info
			if (node.getMetadata().size() > 0) {
				nodeEntry.put("info", node.getMetadata());
			}

			// add actions
			List<Map<String,Object>> actionDescrs = null;
			if (node.getOfferedActions().length > 0) {
				actionDescrs = new ArrayList<Map<String,Object>>();

				for (Class<? extends NodeAction> action: Arrays.asList(node.getOfferedActions())) {
					Map<String, Object> actionDescr = new HashMap<String, Object>();

					// add info
					actionDescr.put("fullname", action.getName());
					actionDescr.put("name", action.getSimpleName());

					// add params
					List<String> params = new ArrayList<String>();
					for (Field field: action.getFields()) {
						if (field.getAnnotation(NodeActionParam.class) != null) {
							params.add(field.getName());
						}
					}
					actionDescr.put("params", params);

					actionDescrs.add(actionDescr);
				}
			}
			nodeEntry.put("actions", actionDescrs);

			jsNodeData.put(node.getNodeID().toString(), nodeEntry);
		}

		return jsNodeData.size()>0 ? jsNodeData : null;
	}
	public String getRawhexLog() {
		return _rawhexLog.toString();
	}
	public void rawhexLog(String str) {
		_rawhexLog(str);
	}
	public void clearRawhexLog() {
		_rawhexLog = new StringBuffer();
	}
	protected void _rawhexLog(String str) {
		_rawhexLog.append(str);
		_rawhexLog.append(System.getProperty( "line.separator" ));

		if (_rawhexLog.length() > MAX_LOG_SIZE) {
			_rawhexLog.delete(0, _rawhexLog.length()-MAX_LOG_SIZE);
		}
	}
	public String getProtocolsLog() {
		return _packetLog.toString();
	}
	public void protocolLog(String str) {
		_protocolLog(str);
	}
	public void clearProtocolLog() {
		_packetLog = new StringBuffer();
	}
	protected void _protocolLog(String str) {
		_packetLog.append(str);
		_packetLog.append(System.getProperty( "line.separator" ));

		if (_packetLog.length() > MAX_LOG_SIZE) {
			_packetLog.delete(0, _packetLog.length()-MAX_LOG_SIZE);
		}
	}

	@Override
	protected void _init() {
		_subscribeTo(WSNProtocolPacketProcessedEvent.class, "_event");
		_subscribeTo(WSNProtocolRawPacketReceivedEvent.class, "_event");
	}
	/**
	 * worker thread
	 */
	@Override
	protected void _run() {

		try {

			// wait for module to shutdown
			this.waitForShutdown();

		} catch (InterruptedException e) {
		} finally {

			// clean up
			if (_server!=null) {
				_server.stop();
			}

		}

	}
	protected void _event(WSNProtocolPacketProcessedEvent eve) {
		if (eve.packet()!=null)
			_protocolLog(eve.packet().info().toString());
	}
	protected void _event(WSNProtocolRawPacketReceivedEvent eve) {
		if (eve.packet()!=null)
			_rawhexLog(toHexString(eve.packet().getPayload()));
	}
	public static String toHexString(byte[] bytes) {
		char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for ( int j = 0; j < bytes.length; j++ ) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v/16];
			hexChars[j*2 + 1] = hexArray[v%16];
		}
		return new String(hexChars);
	}



	/* protected member */
	protected HTTPServer _server;
	protected Map<List<String>,List<MenuLink>> _registeredLinks=new HashMap<List<String>,List<MenuLink>>();
	protected StringBuffer _packetLog=new StringBuffer();
	protected StringBuffer _rawhexLog=new StringBuffer();
	/** @var only keep the last MAX_LOG_SIZE characters of the log */
	protected static int MAX_LOG_SIZE=4096;
	protected Map<String,String> _widgets=new HashMap<String,String>();

	protected class MenuLink implements Comparable<MenuLink> {
		public List<String> menuItem;
		public List<String> category; // Arrays.copyOf(menuItem, menuItem.length-1);
		public String name; // menuItem[menuItem.length-1];
		public int order;
		public String href; // if href==null -> this item represents a category


		/**
		 * sort order=0 standard, the lesser the value the more important the link
		 * @param menuItem
		 * @param sortOrder
		 */
		public MenuLink(String[] menuItem, int sortOrder, String href) {
			this.menuItem = new ArrayList<String>();
			for (int i=0; i<menuItem.length; i++) {
				this.menuItem.add(menuItem[i]);
			}
			this.category = new ArrayList<String>();
			for (int i=0; i<menuItem.length-1; i++) {
				this.category.add(menuItem[i]);
			}
			this.name = menuItem[menuItem.length-1];
			this.order = sortOrder;
			this.href = href;
		}

		public boolean isCategory(Map<List<String>,List<MenuLink>> menuMap) {
			for (List<String> key: menuMap.keySet()) {
				for (MenuLink link: menuMap.get(key)) {
					if (link!=null && link.category.equals(this.menuItem)) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public int compareTo(MenuLink link) {
			return new Integer(this.order).compareTo(link.order);
		}

		@Override
		public String toString() {
			return menuItem.toString() + " #" + this.order + " -> " + this.href;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MenuLink) {
				MenuLink ml = (MenuLink) obj;

				boolean equals = this.menuItem!=null ? this.menuItem.equals(ml.menuItem) : ml.menuItem==null;
				//equals = equals && (this.href!=null ? this.href.equals(ml.href) : ml.href==null);

				return equals;
			}

			return false;
		}
	}
}
