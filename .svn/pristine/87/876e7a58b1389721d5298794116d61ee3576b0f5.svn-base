package de.tum.in.net.WSNDataFramework.Modules.TinyOS;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.net.WSNDataFramework.WSNDriver;
import de.tum.in.net.WSNDataFramework.WSNModule;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPServerModule;


/**
 * WSN Module that helps handling with TinyOS 2.1.1.
 * 
 * @author André Freitag
 */
public class TinyOSHelperModule_Old extends WSNModule {


	/**
	 * constructor
	 * 
	 * @param pathToTinyOs
	 * @param nodeProgramDirs directories to use for node programming
	 */
	public TinyOSHelperModule_Old(String pathToTinyOs, Map<String,String> nodeProgramDirs) {
		this.setTinyOSPath(pathToTinyOs);
		_nodeProgramDirs = nodeProgramDirs;
	}
	/**
	 * constructor
	 * 
	 * @param pathToTinyOs
	 */
	public TinyOSHelperModule_Old(String pathToTinyOs) {
		this(pathToTinyOs, null);

		Map<String,String> dirs = new HashMap<String,String>();
		//dirs.put("TinyIPFIX", "/home/tinyos/Desktop/2011-04-13-Code-new - BA-Thomas/tinyIPFIX_BLIP_multiplatform");
		dirs.put("Data Collector", "/home/tinyos/Documents/TinyIPFIXAggregation - BA-Benjamin/tinyIPFIX_BLIP_multiplatform_v2");
		dirs.put("Aggregator Node", "/home/tinyos/Documents/TinyIPFIXAggregation - BA-Benjamin/aggregator_v3");
		
		_nodeProgramDirs = dirs;
	}


	/**
	 * execute `make` in a specific directory given specific arguments.
	 */
	public ExecTracker doMake(String cmd, String workingDirectory) {
		if (cmd==null || workingDirectory==null || !cmd.matches("^(\\w+=(.*?\\s+))?make(.*)$"))
			return null;

		return _exec(cmd, workingDirectory);
	}

	/**
	 * opens a IP tunnel
	 * 
	 * @param dev device the base station is connected to (e.g. /dev/ttyUSB0)
	 * @param target the type of the base station (e.g. telosb)
	 * @param sudoPassword password to use for `sudo`, if NULL the tunnel is opened as current user
	 * @return ExecTracker to the process's stdin+stderr (stream output is merged as it comes in)
	 */
	public ExecTracker openIPTunnel(String dev, String target, String sudoPassword) {

		this.killIPTunnel(_sudoPassword).waitForFinish();
		// remove nodes etc
		if (this.app() != null) {
			this.app().wsn().reset();

			if (this.app().module(WSNHTTPServerModule.class)!=null) {
				this.app().module(WSNHTTPServerModule.class).clearProtocolLog();
			}
		}

		// open tunnel
		_sudoPassword=sudoPassword;

		String cmdline = "./driver/ip-driver "+dev+" "+target;
		if (sudoPassword != null) {
			cmdline = "echo \""+sudoPassword.replace("\"", "\\\"")+"\" | sudo -S -p \"\" " + cmdline; // prepend SUDO with given password and hidden prompt.
		}

		final WSNDriver driver = this.app()!=null ? this.app().getDriver() : null;
		ExecTracker et = _exec(cmdline, _tinyOSPath+"support/sdk/c/blip");
		et.registerCallback(new ExecutionFinishedCallback() {

			@Override
			public void executionFinished(ExecTracker tracker) {
				if (driver != null && driver instanceof TinyOSDriver) {
					((TinyOSDriver)driver).notifyOfClosedTunnel();
				}
			}

		});
		return et;
	}

	/**
	 * kills an open IP tunnnel
	 * 
	 * @param sudoPassword
	 * @return
	 */
	public ExecTracker killIPTunnel(String sudoPassword) {

		String cmdline = "killall ip-driver";
		if (sudoPassword != null) {
			cmdline = "echo \""+sudoPassword.replace("\"", "\\\"")+"\" | sudo -S -p \"\" " + cmdline; // prepend SUDO with given password and hidden prompt.
		}

		return _exec(cmdline, _tinyOSPath);
	}

	/**
	 * sets path to tiny os
	 * 
	 * @param pathToTinyOs
	 * @return
	 */
	public TinyOSHelperModule_Old setTinyOSPath(String pathToTinyOs) {
		_tinyOSPath = pathToTinyOs;
		if (_tinyOSPath.charAt(_tinyOSPath.length()-1) != '/')
			_tinyOSPath += '/';

		_scanDirectory();

		return this;
	}

	/**
	 * gets path to tiny os
	 * @return
	 */
	public String getTinyOSPath() {
		return _tinyOSPath;
	}

	/**
	 * get tinyOS version.
	 */
	public String getVersion() {
		return _tinyOSVersion;
	}

	/**
	 * get directories used for make commands
	 * 
	 * @return Map(category => List(directory))
	 */
	public Map<String,Map<String,String>> getMakeDirectories() {
		Map<String,Map<String,String>> dirs = new LinkedHashMap<String,Map<String,String>>();

		dirs.put("base_station", new LinkedHashMap<String,String>());
		dirs.get("base_station").put("IPBaseStation", _tinyOSPath+"apps/IPBaseStation");

		dirs.put("nodes", new LinkedHashMap<String,String>());
		if (_nodeProgramDirs != null)
			dirs.get("nodes").putAll(_nodeProgramDirs);


		return dirs;
	}

	/**
	 * gets available target devices (e.g. /dev/ttyUSB0).
	 * 
	 * @return
	 */
	public List<String> getTargetDevices() {
		List<String> ret = new LinkedList<String>();

		File dir = new File("/dev");
		if (!dir.isDirectory()) return ret;

		// get devices
		String[] devices = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("ttyUSB(\\d+)");
			}
		});
		for (String dev: devices) {
			ret.add(dir+"/"+dev);
		}

		return ret;
	}

	/**
	 * get tinyOS target platforms.
	 */
	public Map<String,List<String>> getTargetPlatforms() {
		return _targets;
	}

	/**
	 * get tinyOS make extras.
	 */
	public Map<String,List<String>> getMakeExtras() {
		return _extras;
	}

	/**
	 * get tinyOS sensorboards.
	 */
	public List<String> getSensorboards() {
		return _sensorboards;
	}
	public byte[] getLatestTopologyPNG() {
		return _latestTopologyPNG;
	}




	/* overridden */
	@Override
	public String getName() {
		return "TinyOS 2.1.1";
	}
	@Override
	protected void _init() {
		_moduleDependent(WSNHTTPServerModule.class, "_moduleDep", "_moduleRem");
	}

	@Override
	protected void _shutdown() {
		this.killIPTunnel(_sudoPassword);
	}

	@Override
	protected void _run() {
		while (!Thread.interrupted()) {
			try {
				try {
					Thread.sleep(_topologyUpdateInterval*1000);
				} catch (InterruptedException e) {
					break;
				}

				// get driver
				TinyOSDriver tos = this.app()!=null && this.app().module(TinyOSDriver.class)!=null ? this.app().module(TinyOSDriver.class) : null;
				if (tos == null) continue;

				// create temp files
				File dotFile = this.app().getTemporaryFile();
				File dotGraphFile = this.app().getTemporaryFile();
				if (!dotFile.canWrite() || !dotGraphFile.canWrite()) continue;

				// fetch topology file from the tinyos blip telnet console
				tos._doTelnetCommand("dot " + dotFile.getAbsolutePath());

				// convert dot file to png graph
				ExecTracker exec = _exec("dot -Tpng \""+dotFile.getAbsolutePath()+"\" -o \""+dotGraphFile.getAbsolutePath()+"\"", "/tmp");
				exec.waitForFinish();

				// read graph to byte array
				ByteArrayOutputStream ous=null;
				InputStream ios=null;
				try {
					byte []buffer = new byte[4096];
					ous = new ByteArrayOutputStream();
					ios = new FileInputStream(dotGraphFile);
					int read = 0;
					while ( (read = ios.read(buffer)) != -1 ) {
						ous.write(buffer, 0, read);
					}
				} finally {
					try {
						if (ous != null)
							ous.close();
					} catch (IOException e){
						e.printStackTrace();
					}

					try {
						if (ios != null)
							ios.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				_latestTopologyPNG = ous.toByteArray();
				System.out.println("png: " + _latestTopologyPNG.length);

				//dotFile.delete();
				//dotGraphFile.delete();
				try {
					Thread.sleep(_topologyUpdateInterval*1000);
				} catch (InterruptedException e) {
					break;
				}
			} catch (Exception e) {
				System.err.println("Couldn't create TinyOS Topology Graph");
				e.printStackTrace();
			}
		}
	}

	protected void _moduleDep(WSNHTTPServerModule m) {
		try {
			m.registerController("tinyos", HTTPController.class, this)
			.getServerModule().registerLink(new String[]{"Network Management","Operating Systems","TinyOS"}, "/tinyos")
			.setLinkSortOrder(new String[]{"Network Management","Operating Systems","TinyOS"}, -999);

			m.registerWidget("widget:tinyostopopng", "/tinyos/widgets/tinyostopopng/tinyostopopngWidget.js");
		} catch (InstantiationException e) {
		}
	}


	/* protected helper */

	protected void _scanDirectory() {
		// reinitialize member
		File dir = new File(_tinyOSPath);
		_correctDir = dir.exists() && dir.isDirectory();
		_tinyOSVersion=null;
		_targets=new HashMap<String,List<String>>();
		_extras=new HashMap<String,List<String>>();
		_sensorboards=new ArrayList<String>();

		// determine tinyOS version
		Matcher m = Pattern.compile("\\-(.*?)/$").matcher(_tinyOSPath);
		if (m.find()) {
			_tinyOSVersion = m.group(1);
		}

		/*** scan for make options ***/
		String rootName = _tinyOSPath + "support/make";

		dir = new File(rootName);
		if (dir.isDirectory()) {
			// get sub directories
			List<String> subDirs = new ArrayList<String>();
			subDirs.add("");

			String[] fileList = dir.list();
			for (String fName: fileList) {
				if (new File(rootName + "/" + fName).isDirectory()) {
					subDirs.add(fName);
				}
			}

			// go through sub directories
			for (String sdName: subDirs) {
				dir = new File(rootName + "/" + sdName);
				if (!dir.isDirectory()) continue;

				// get targets
				String[] targetsList = dir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".target");
					}
				});
				if (targetsList.length > 0) {
					_targets.put(sdName, new ArrayList<String>());
					for (String target: targetsList) {
						_targets.get(sdName).add(target.substring(0,target.lastIndexOf(".")));
					}
				}
				// get extras
				String[] extrasList = dir.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".extra");
					}
				});
				if (extrasList.length > 0) {
					_extras.put(sdName, new ArrayList<String>());
					for (String extra: extrasList) {
						_extras.get(sdName).add(extra.substring(0,extra.lastIndexOf(".")));
					}
				}
			}
		}

		/*** scan for sensorboards ***/
		_sensorboards.add("");
		_sensorboards.add("nil");

		rootName = _tinyOSPath + "tos/sensorboards";
		dir = new File(rootName);
		if (dir.isDirectory()) {
			// get sub directories
			String[] fileList = dir.list();
			for (String fName: fileList) {
				if (new File(rootName+"/"+fName).isDirectory()) {
					_sensorboards.add(fName);
				}
			}
		}


		// determine state
		_correctDir = _correctDir && (_targets.size()>0 || _extras.size()>0 || _sensorboards.size()>0);
		if (_correctDir) {
			_setRunning("tinyOS" + (_tinyOSVersion!=null?"-"+_tinyOSVersion.trim():"") + " found.");
		} else {
			_setError("TinyOS installation not found.");
		}
	}

	/**
	 * executes a specific command on an interactive BASH
	 * 
	 * @param cmdline command to execute
	 * @param workingDirectory working directory where to execute the command in
	 * @return PipedReader to the process's stdin+stderr (stream output is merged as it comes in)
	 */
	protected ExecTracker _exec(final String cmdline, final String workingDirectory) {
		final ExecTracker tracker = new ExecTracker();

		new Thread(new Runnable() {

			@Override
			public void run() {

				/*** prepare process ***/
				List<String> command = new ArrayList<String>();
				command.add("/bin/bash");
				//command.add("-l");
				command.add("-i"); // start as interactive shell -> this way .bashrc is loaded
				command.add("-c");
				command.add(cmdline);

				ProcessBuilder builder = new ProcessBuilder(command);
				builder.directory(new File(workingDirectory));
				builder.redirectErrorStream(true);

				/*** start process ***/
				Process process=null;
				try {
					process = builder.start();

					BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

					int read=0;
					boolean exited=false;
					char[] buf=new char[1024];
					try {
						while (!tracker.aborted() && !exited) {

							if (in.ready()) {
								read = in.read(buf);
								if (read >= 0) {
									synchronized (tracker) {
										tracker.put(new String(Arrays.copyOf(buf,read)));
									}
								} else {
									exited = true;
								}

							} else {

								try {
									process.exitValue();
									exited=true;
								}catch(Exception e){}
								Thread.sleep(500);

							}

						}
					} catch (Exception e) {
					}

					if (!tracker.aborted()) {
						process.waitFor();
					} else {
						process.destroy();
						process.waitFor();
					}
				} catch (IOException e) {
				} catch (InterruptedException e) {
				}


				/*** clean up ***/
				int result=-1;
				if (process != null && !tracker.aborted()) {
					result = process.exitValue();
				}
				tracker.setFinished(result);
			}

		}).start();


		return tracker;
	}
	public static class ExecTracker {

		/**
		 * get newly added content since last next()/getContent() call.
		 * 
		 * @return
		 */
		public String next() {
			String ret="";

			synchronized (_buffer) {
				synchronized (_pos) {
					if (_buffer.length() > _pos) {
						ret = _buffer.substring(_pos);
						_pos = _buffer.length();
					}
				}
			}

			return ret;
		}
		/**
		 * get whole content.
		 * 
		 * @return
		 */
		public String getContent() {
			String content;

			synchronized(_buffer) {
				synchronized(_pos) {
					content = _buffer.toString();
					_pos = _buffer.length();
				}
			}

			return content;
		}
		/**
		 * checks if execution was aborted.
		 * 
		 * @return
		 */
		public boolean aborted() {
			return _aborted;
		}
		/**
		 * checks if execution is finished.
		 * 
		 * @return
		 */
		public boolean isFinished() {
			return _returnValue!=null;
		}
		/**
		 * gets return value of execution || NULL if not finished yet.
		 * @return
		 */
		public Integer returnValue() {
			return _returnValue;
		}

		/**
		 * put content.
		 * 
		 * @param str
		 */
		public void put(String str) {
			synchronized (_buffer) {
				_buffer.append(str);

				if (_buffer.length() > ExecTracker.maxLength) {
					_buffer.delete(0, _buffer.length()-ExecTracker.maxLength);
				}
			}

			synchronized (_nextAvailable) {
				_nextAvailable.notifyAll();
			}
		}
		/**
		 * mark execution as finished.
		 * 
		 * @param returnValue
		 */
		public void setFinished(int returnValue) {
			if (this.isFinished()) return;

			_returnValue = returnValue;
			synchronized(this) {
				this.notifyAll();
			}

			// call registered callbacks
			for (ExecutionFinishedCallback cb: _executionFinishedCallbacks) {
				if (cb == null) continue;

				cb.executionFinished(this);
			}
		}
		public void abort() {
			if (this.aborted()) return;

			_aborted = true;
		}


		/**
		 * waits for this execution to finish.
		 */
		public void waitForFinish() {
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException e) {
			}
		}
		/**
		 * waits until new input is available / execution finished
		 */
		public void waitForNext() {
			try {
				synchronized (_nextAvailable) {
					_nextAvailable.wait();
				}
			} catch (InterruptedException e) {
			}
		}
		/**
		 * register callback to be called as soon as execution finishes
		 * 
		 * @param cb
		 */
		public void registerCallback(ExecutionFinishedCallback cb) {
			if (cb == null) return;

			_executionFinishedCallbacks.add(cb);

			// call cb immediately if execution is already finished
			if (_returnValue != null) {
				cb.executionFinished(this);
			}
		}

		/* protected member */
		protected boolean _aborted=false;
		protected Integer _returnValue=null;
		protected StringBuffer _buffer=new StringBuffer();
		protected Integer _pos=0;
		protected Object _nextAvailable=new Object();
		protected List<ExecutionFinishedCallback> _executionFinishedCallbacks = new ArrayList<ExecutionFinishedCallback>();

		public static final int maxLength=4096;
	}

	public abstract class ExecutionFinishedCallback {
		public abstract void executionFinished(ExecTracker tracker);
	}



	/* protected member */
	protected String _tinyOSPath;
	protected boolean _correctDir=false;
	protected String _tinyOSVersion=null;
	protected Map<String,String> _nodeProgramDirs=null;
	protected Map<String,List<String>> _targets=new HashMap<String,List<String>>();
	protected Map<String,List<String>> _extras=new HashMap<String,List<String>>();
	protected List<String> _sensorboards=new ArrayList<String>();
	protected int _topologyUpdateInterval=10;
	protected byte[] _latestTopologyPNG=null;

	protected String _sudoPassword=null;
}
