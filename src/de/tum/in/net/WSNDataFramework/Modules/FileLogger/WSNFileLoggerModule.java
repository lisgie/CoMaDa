package de.tum.in.net.WSNDataFramework.Modules.FileLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Events.WSNNodeUpdatedEvent;


public class WSNFileLoggerModule extends WSNModule {

	public WSNFileLoggerModule() {
		this(null);
	}
	public WSNFileLoggerModule(String path) {
		_logDir = path != null ? new File(path) : null;
	}

	/**
	 * logs a given message to the shared log file.
	 * 
	 * @param message
	 * @return this for fluent interface
	 */
	public WSNFileLoggerModule log(String message) {
		_initDirs();
		synchronized(_sharedLogFile) {
			try {
				FileWriter fw = new FileWriter(_sharedLogFile,true);
				//System.out.println(_logDir);
				fw.write(message);
				fw.close();
			} catch (IOException e) {
				System.out.println("Couldn't write to logfile '"+_sharedLogFile.getName()+"'!");
			}
		}
		return this;
	}
	/**
	 * logs a given message to the log file of the given node
	 * 
	 * @param message
	 * @param node
	 * @return this for fluent interface;
	 */
	public WSNFileLoggerModule log(String message, Node node) {
		if (message==null || node==null) return this;

		_initDirs();
		synchronized (_nodeLogFiles) {
			if (!_nodeLogFiles.containsKey(node.getNodeID())) {
				String fileName = "log." + _startTime + "." + node.getNodeID();
				_nodeLogFiles.put(node.getNodeID(), new File(_logDir.getAbsolutePath()+"/"+fileName));
				_nodeLogFileHeaders.put(node.getNodeID(), null);

				try {
					FileWriter fw = new FileWriter(_nodeLogFiles.get(node.getNodeID()),false);
					fw.write("# Node:"+node.getNodeID());
					fw.close();
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}

			try {
				FileWriter fw = new FileWriter(_nodeLogFiles.get(node.getNodeID()),true);

				// update header
				boolean headerUpToDate = true;
				if (_nodeLogFileHeaders.get(node.getNodeID()) == null || _nodeLogFileHeaders.get(node.getNodeID()).size() <= 0) {
					headerUpToDate = false;
				} else {
					int i=0;
					for (Node.Datum field: node.data()) {
						if (i >= _nodeLogFileHeaders.get(node.getNodeID()).size() || !_nodeLogFileHeaders.get(node.getNodeID()).get(i++).equals(_columnName(field))) {
							headerUpToDate=false;
						}
					}
					headerUpToDate = headerUpToDate && (i==_nodeLogFileHeaders.get(node.getNodeID()).size());
				}
				if (!headerUpToDate) {
					_nodeLogFileHeaders.put(node.getNodeID(), new ArrayList<String>());

					//# time	NodeTime (s)	NodeID	Sound 	Light (lumen)
					fw.write(System.getProperty("line.separator")+"# time");
					for (Node.Datum field: node.data()) {
						String colName = _columnName(field);
						fw.write("\t"+colName);
						_nodeLogFileHeaders.get(node.getNodeID()).add(colName);
					}
					fw.write(System.getProperty("line.separator"));
				}

				// write message
				fw.write(message);

				// close stream
				fw.close();
			} catch (IOException e) {
				System.out.println("Couldn't write to logfile '"+_nodeLogFiles.get(node.getNodeID()).getName()+"'!");
			}
		}

		return this;
	}
	public File getLogFile(String name) {
		_initDirs();
		return new File(_logDir.getAbsolutePath()+"/"+name);
	}

	/* overridden methods */
	@Override
	public String getName() {
		return "File Logger";
	}

	@Override
	protected void _init() {
		_startTime = new SimpleDateFormat("yyyy-MM-dd.HHmm").format(new Date());

		_subscribeTo(WSNNodeUpdatedEvent.class, "_event");

		_setRunning("up and running");
	}
	protected void _initDirs() {
		String fileName = "log." + _startTime;
		if (_logDir == null) {
			_logDir = this.app().getFile(fileName);
			_logDir.delete();
			_logDir.mkdirs();
		}
		if (_sharedLogFile == null) {
			_sharedLogFile = new File(_logDir.getAbsolutePath()+"/"+fileName);
		}
	}

	protected void _event(WSNNodeUpdatedEvent eve) {
		if (eve==null || eve.node()==null) return;


		String receiveTime = new SimpleDateFormat("HH:mm:ss").format(new Date());

		// log to shared file
		String log = eve.node().getNodeID() + " - " + receiveTime + System.getProperty("line.separator");
		for (Node.Datum field: eve.node().data()) {
			log += "\t";
			log += field.getID() + " (" + (field.getName()!=null?field.getName():field.getType()) + "): ";
			log += field.getValue().toString() + " " + field.getUnit();
			log += System.getProperty("line.separator");
		}
		log += System.getProperty("line.separator");
		this.log(log);

		// log to node specific file
		//# time	NodeTime (s)	NodeID	Sound 	Light (lumen)
		log = receiveTime;
		for (Node.Datum field: eve.node().data()) {
			log += "\t" + (field.getValue()!=null?field.getValue().toString():"");
		}
		log += System.getProperty("line.separator");
		this.log(log, eve.node());
	}

	protected String _columnName(Node.Datum field) {
		if (field == null) return "";

		String colName = (field.getName()!=null ? field.getName() : field.getValue()) + (field.getUnit()!=null&&!field.getUnit().equals("") ? " ("+field.getUnit()+")" : "");
		return colName;
	}

	/* protected member */
	protected String _startTime=null;
	protected File _logDir=null;
	protected File _sharedLogFile;
	protected Map<NodeID,File> _nodeLogFiles=new HashMap<NodeID,File>();
	protected Map<NodeID,List<String>> _nodeLogFileHeaders=new HashMap<NodeID,List<String>>();
}
