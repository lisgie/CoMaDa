package de.tum.in.net.WSNDataFramework.Modules.Cosm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.Datum;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Events.WSNDatastreamChangeEvent;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeUpdatedEvent;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed;
import de.tum.in.net.WSNDataFramework.Modules.Cosm.CosmAPI.Feed.DataStream;
import de.tum.in.net.WSNDataFramework.Modules.FileLogger.WSNFileLoggerModule;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPServerModule;


/**
 * Module uploading data to Cosm.com
 * @author André Freitag
 *
 */
public class WSNCosmModule extends WSNModule {



	/**
	 * constructors. uses "TUMI8" Cosm account.
	 * 
	 * @throws CosmException
	 */
	public WSNCosmModule() throws CosmException {
		this("tumi8", "c8R0rBphAZ2b31khePtdK0DSOrqSAKxZTW5sMkZKZGhqMD0g");
	}

	/**
	 * constructor.
	 * 
	 * @param username
	 * @param apiKey
	 * @throws CosmException
	 */
	public WSNCosmModule(String username, String apiKey) throws CosmException {
		_cosm = new CosmAPI(username, apiKey);
	}

	/**
	 * assigns a node to a specific feed (will update all fitting datastreams whenever new data comes in)
	 * 
	 * @param nodeID
	 * @param feedID
	 * @return
	 */
	public WSNCosmModule assignFeed(String nodeID, String feedID) {
		synchronized (_nodeAssignments) {
			_nodeAssignments.put(nodeID, feedID);
			_activeFeeds.add(feedID);
		}

		return this;
	}
	public String createFeed(String title) {
		String id = this.getCosmAPI()!=null ? this.getCosmAPI().createFeed(title) : null;
		if (id != null) {
			_activeFeeds.add(id);
		}
		return id;
	}

	public boolean isFeedActive(String feedID) {
		return _activeFeeds.contains(feedID);
	}

	/**
	 * return node=>feed assignments
	 * 
	 * @return Map(nodeID=>feedID)
	 */
	public Map<String,String> getAssignments() {
		return new HashMap<String,String>(_nodeAssignments);
	}

	/**
	 * gets underlying CosmAPI object
	 */
	public CosmAPI getCosmAPI() {
		return _cosm;
	}

	public void setDiagramSaveInterval(int minutes) {
		_imgSaveInterval = minutes;
	}



	/* overridden */
	@Override
	public String getName() {
		return "Cosm Uploader";
	}

	@Override
	protected void _init() {

		_moduleDependent(WSNHTTPServerModule.class, "_moduleDep", "_moduleRem");

		this._subscribeTo(WSNNodeUpdatedEvent.class, "_onEvent");

		_setRunning("up and running");
	}
	@Override
	protected void _run() {
		while (!Thread.interrupted()) {
			if (_lastSave==null || (Calendar.getInstance().getTimeInMillis()-_lastSave)/1000/60 > _imgSaveInterval) {

				if (this.app().module(WSNFileLoggerModule.class) != null) {
					// save
					for (String nodeID: _nodeAssignments.keySet()) {
						String feedID = _nodeAssignments.get(nodeID);

						Feed feed = _cosm.getFeed(feedID);
						if (feed != null) {
							for (DataStream str: feed.streams) {
								String url = "https://api.cosm.com/v2/feeds/"+feedID+"/datastreams/"+str.id+".png?width=500&height=200&colour=%23f15a24&duration=5minutes&title=HumiditySensironSHT11&show_axis_labels=true&detailed_grid=true&scale=auto&timezone=+1";
								System.out.println(url);
								byte[] img = _downloadImage(url);

								FileOutputStream fos;
								try {
									fos = new FileOutputStream(this.app().module(WSNFileLoggerModule.class).getLogFile("cosm."+feedID+"."+str.id+"."+new SimpleDateFormat("yyyy-MM-dd.HHmm").format(new Date())+".png"));
									fos.write(img);
									fos.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

				try {
					_lastSave = Calendar.getInstance().getTimeInMillis();
					Thread.sleep(1000*10);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}

	protected void _moduleDep(WSNHTTPServerModule m) {
		try {
			m.registerController("cosm", HTTPController.class, this)
			.getServerModule().registerLink(new String[]{"Visualisation","COSM"}, "/cosm");

			m.registerWidget("widget:cosm", "/cosm/widgets/cosm/cosmWidget.js");
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}


	protected void _onEvent(WSNNodeUpdatedEvent eve) {

		if (_lastUpdate!=null && (Calendar.getInstance().getTimeInMillis()-_lastUpdate)/1000 < _maxUpdateInterval)
			return;

		try {
			synchronized (_nodeAssignments) {
				for (String nodeID: _nodeAssignments.keySet()) {
					try {
						Node node = this.app().wsn().node(nodeID);
						Feed feed = this.getCosmAPI().getFeed(_nodeAssignments.get(nodeID));

						Map<String,DataStream> streams = new HashMap<String,DataStream>();
						for (DataStream stream: feed.streams) {
							streams.put(stream.id, stream);
						}

						if (node!=null && feed!=null) {
							// update feed
							for (Datum field: node.data()) {
								if (field.getValue()==null) continue;

								//DataStream stream = streams.get(field.name!=null ? field.name : field.type);
								DataStream stream = streams.get(field.getType());

								if (stream != null) {
									// update stream
									this.getCosmAPI().updateStream(Double.parseDouble(field.getValue().toString()), stream.id, feed.id);
								}
							}
							// update node
							node.setMetadata(
									"Cosm-Uploads",
									((node.getMetadata("Cosm-Uploads", Integer.class) != null)
											? node.getMetadata("Cosm-Uploads", Integer.class) + 1
													: 1)
									);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			
			}
			_lastUpdate = Calendar.getInstance().getTimeInMillis();
			//System.out.println(".. uploaded to Cosm!");
			if (this.app()!=null && this.app().module(WSNFileLoggerModule.class)!=null) {
				this.app().module(WSNFileLoggerModule.class).log("upload to cosm\r\n");
			}
			if (this.app()!=null && this.app().module(WSNHTTPServerModule.class)!=null) {
				this.app().module(WSNHTTPServerModule.class).protocolLog("upload to cosm\r\n");
			}
			this.app().fireEvent(new WSNDatastreamChangeEvent(_activeFeeds));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	protected byte[] _downloadImage(String urlstr) {
		try {
			URL url = new URL(urlstr);

			
			Authenticator.setDefault(new Authenticator() {
				 @Override
				        protected PasswordAuthentication getPasswordAuthentication() {
				         return new PasswordAuthentication(
				   "tumi8", "tinyos".toCharArray());
				        }
				});
			
			// First set the default cookie manager.
			CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1!=(n=in.read(buf)))
			{
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	/* protected member */
	protected CosmAPI _cosm;
	protected int _maxUpdateInterval=30; // maximum update interval in seconds
	protected Long _lastUpdate=null;
	protected int _imgSaveInterval=1;
	protected Long _lastSave=null;
	protected Set<String> _activeFeeds=new HashSet<String>();

	protected Map<String,String> _nodeAssignments=new HashMap<String,String>();
}
