package de.tum.in.net.WSNDataFramework.Modules.Web;

import java.util.Scanner;

import de.tum.in.net.WSNDataFramework.AggregationNode;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.CUSTOM.TUMI8AggregationNode;
import de.tum.in.net.WSNDataFramework.Events.WSNDatastreamChangeEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeUpdatedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolPacketProcessedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNProtocolRawPacketReceivedEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNTopologyUpdatedEvent;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.WSNCosmModule;

public class WSNWebModule extends WSNModule{
	
	private WSNSQLConnector _connector;
	private WSNDataUploader _uploader;
	private String _link;
	private CosmAPI _cosm = null;
	
	public WSNWebModule() {
		//ask user for name and pw, and call other constructor with it
		this(getUsername(), getPassword());
	}

	public WSNWebModule(String user, String pw) {
		_connector = new WSNSQLConnector();
		if(_connector.isConnected()) {
			if(_connector.isAuthorized(user, pw)) {
				//prepare DB for new WSN
				_connector.addWSN(user);
				_link = _connector.getLink();
				_uploader = new WSNDataUploader(user, pw);
				_setRunning("up and running");
			}
			else {
				System.out.println("Authentication failed: The Username / Password combination you entered is either not valid or does not have the permition to upload a WSN");
				_setError("Authentication failed");
			}
		}
		else {
			//inform that connection to given DB could not be established			
			_setError("DB not found");
		}
	}
	
	@Override
	public String getName() {
		return "Web Visualization";
	}

	@Override
	protected void _init() {
		if(_connector.isConnected()){
			this._subscribeTo(WSNTopologyUpdatedEvent.class, "_event");
			this._subscribeTo(WSNNodeUpdatedEvent.class, "_event");
			this._subscribeTo(WSNProtocolPacketProcessedEvent.class, "_event");
			this._subscribeTo(WSNProtocolRawPacketReceivedEvent.class, "_event");
			this._subscribeTo(WSNDatastreamChangeEvent.class, "_event");
			
			MyShutdownHook hook = new MyShutdownHook(_connector);
			hook.attachShutdownHook();
		}
	}

	@Override
	protected void _run() {
		try {
			// wait for module to shutdown
			this.waitForShutdown();

		} catch (InterruptedException e) {
		} finally {
			if(_connector.isConnected()) {
				_connector.cleanUp(); 
			}
		}
	}
	
	protected void _event(WSNTopologyUpdatedEvent event) {
		_uploader.updateTopology(event.topology(), _link);
	}
	
	protected void _event(WSNNodeUpdatedEvent event) {
		if(event.node().getClass() == TUMI8AggregationNode.class) {
			_uploader.updateAggregator((AggregationNode) event.node(), _link);		
		} else {
			_uploader.updateNode(event.node(), _link);
		}
	}
	
	protected void _event(WSNProtocolPacketProcessedEvent event) {
		_uploader.updateProtocol(event.packet().info().toString().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"), _link);
	}
	
	protected void _event(WSNProtocolRawPacketReceivedEvent event) {
		_uploader.updateProtocol(event.packet().info().toString().replaceAll("(\r\n|\n\r|\r|\n)", "<br />"), _link);
	}
	
	protected void _event(WSNDatastreamChangeEvent event) {
		if(_cosm == null){
			_cosm = this.app().module(WSNCosmModule.class).getCosmAPI();
		}
		if(!(event.activeFeeds().isEmpty())){
			for(String feedID : event.activeFeeds()) {
				Feed feed = _cosm.getFeed(feedID);
				_uploader.updateDatastream(feed, _link);
			}
		} 
	}
	
	private static String getUsername() {
		System.out.println("Please state your Username on the Website");
		Scanner scan = new Scanner(System.in);
		String username = scan.nextLine();
//		scan.close();
		return username;
	}
	
	private static String getPassword() {
		System.out.println("Please state your Password");
		Scanner scan = new Scanner(System.in);
		String password = scan.nextLine();
		scan.close();
		return password;
	}
}
