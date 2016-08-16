package de.tum.in.net.WSNDataFramework.Modules.TinyOS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Topology;
import de.tum.in.net.WSNDataFramework.WSNDriver;
import de.tum.in.net.WSNDataFramework.Modules.FileLogger.WSNFileLoggerModule;


/**
 * TinyOS Driver
 * 
 * @author André Freitag
 */
public class TinyOSDriver extends WSNDriver {

	/* constructors */
	public TinyOSDriver() {
		this(6106, 4739);
	}
	public TinyOSDriver(int blipPort, int udpPort) {
		_blipPort = blipPort;
		_udpPort = udpPort;
		_blipTerminal = new BLIPTerminalClient(blipPort);

		_setIdling("Nothing received yet. Is the IP-Tunnel open?");
	}


	/* public methods */
	/**
	 * sets the interval the topology is fetched from TinyOS.
	 * 
	 * @param seconds
	 * @return
	 */
	public TinyOSDriver setTopologyUpdateInterval(int seconds) {
		_topologyUpdateInterval = seconds;
		return this;
	}

	/**
	 * gets the interval the topology is tetched from TinyOS.
	 * 
	 * @return interval in seconds
	 */
	public int getTopologyUpdateInterval() {
		return _topologyUpdateInterval;
	}

	/**
	 * sends "rebuild" to the blip telnet console, triggering off a topology rebuild.
	 * 
	 * @return
	 */
	public boolean doBlipTopologyRebuild() {
		try {
			return _doTelnetCommand("rebuild");
		} catch (InterruptedException e) {
			return false;
		}
	}
	public String doBlipStatsCommand() {
		try {
			return _getTelnetCommandResponse("stats");
		} catch (InterruptedException e) {
			return null;
		}
	}

	/**
	 * sets the port to the telnet blip console.
	 * 
	 * @param port
	 * @return this for fluent interface

	public TinyOSDriver setBlipTelnetPort(int port) {
		_blipPort = port;

		return this;
	}*/

	/**
	 * gets the currently set port to the telnet blip console.
	 * 
	 * @return port
	 */
	public int getBlipTelnetPort() {
		return _blipPort;
	}

	/**
	 * notify the driver that the IP-Tunnel was closed
	 */
	public void notifyOfClosedTunnel() {
		//_setError("IP-Tunnel closed!");
	}
	/**
	 * notify the driver that the IP-Tunnel was opened
	 */
	public void notifyOfOpenedTunnel() {
		_setIdling("Nothing received yet. Is the IP-Tunnel open?");
	}


	/* overriden methods */
	@Override
	public String getName() {
		return "TinyOS WSN Driver";
	}
	/**
	 * shutdown handler
	 */
	@Override
	protected void _shutdown() {
		_serverSocket.close();
		_topologyThread.interrupt();
		_packetThread.interrupt();
		_blipTerminal.shutdown();
	}
	@Override
	protected void _run() {
		// topology updater
		_topologyThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					_updateTopology();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		_topologyThread.setName("Module Worker " + this.getClass().getName() + " - Topology Updater");
		_topologyThread.start();

		// packet fetcher
		_packetThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					_fetchPacket();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		_packetThread.setName("Module Worker " + this.getClass().getName() + " - Packet Fetcher");
		_packetThread.start();

		// wait for both
		try {
			_packetThread.join();
			_topologyThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	protected void _updateTopology() {
		try{
			while (!Thread.interrupted()) {
				try {

					Topology topology = _getTopologyFromTelnet();
					this.app().wsn().replaceTopology(topology);

				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {

					e.printStackTrace();
				}

				Thread.sleep(_topologyUpdateInterval*1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	protected void _fetchPacket() {
		try {
			_serverSocket = new DatagramSocket(_udpPort);

			while (!Thread.interrupted()) {
				// receive datagram
				DataPacket p;
				try {
					p = _receiveDataPacket();
					_setRunning("up and running");
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}


				if (p!=null && p.data!=null && p.data.length != 0) {
					// PROCESS packet
					_processPacket(p.data, p.address);
				} else { // no packet received, socket broken
					System.err.println("socket closed");
					break;
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} finally {
			try {
				_serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * receive another data packet. blocks thread until a new packet is available.
	 * 
	 * @return data packet
	 * @throws Exception 
	 */
	protected DataPacket _receiveDataPacket() throws Exception {
		if (_serverSocket.isClosed()) return null;

		byte[] buf = new byte[2048];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		_serverSocket.receive(p);
		byte[] data = new byte[p.getLength()];
		System.arraycopy(p.getData(), 0, data, 0, data.length);

		// log raw packets
		if (this.app().module(WSNFileLoggerModule.class) != null) {

			String dataHex = toHexString(data);
			dataHex +="\n";

			File log = this.app().module(WSNFileLoggerModule.class).getLogFile("listener-capture-hex.txt");

			FileOutputStream fos = new FileOutputStream(log,true);
			fos.write(dataHex.getBytes());
			fos.close();
		}

		if(p.getSocketAddress() instanceof InetSocketAddress){
			return new DataPacket(data, (InetSocketAddress)p.getSocketAddress());
		} else {
			System.err.println("Received packet has no InetAddress!");
			return new DataPacket(data, null);
		}
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


	/* protected methods */
	protected Topology _getTopologyFromTelnet() throws InterruptedException {

		try {
			/* fetch topology file from the tinyos blip telnet console */
			File tempFile = this.app().getTemporaryFile();
			if (!tempFile.canRead()) return null;


			// send telnet command
			_doTelnetCommand("dot " + tempFile.getAbsolutePath());


			/* read file content */
			Set<Topology.Link> links = new HashSet<Topology.Link>();
			Set<NodeID> nodes = new HashSet<NodeID>();
			NodeID baseNodeID=null;

			BufferedReader br = new BufferedReader(new FileReader(tempFile));
			String line;
			while ((line=br.readLine()) != null) {
				Matcher m = Pattern.compile("\"(.*?)\" -> \"(.*?)\" \\[label=\"(.*?)\"\\]").matcher(line);

				if (m.find()) {
					try {
						// parse line
						NodeID nodeID = new Node.NodeID(Long.toString(Long.parseLong(m.group(1).substring(2), 16)));
						NodeID sendsToID = new Node.NodeID(Long.toString(Long.parseLong(m.group(2).substring(2), 16)));
						double weight = Double.parseDouble(m.group(3));

						// add link
						links.add(new Topology.Link(nodeID, sendsToID, weight));

						// add every found nodeID to the nodes set
						nodes.add(nodeID);
						nodes.add(sendsToID);
					} catch (Exception e) {
						System.err.println("Couldn't parse TinyOS Topology");
						e.printStackTrace();
					}
				}
				//System.out.println(line);
			}
			br.close();
			tempFile.delete();

			// try to figure out what nodeID belongs to the Base Station (Base Station is represented by NULL in the WSN Framework)
			// the Base Station is the only node that only receives data and does not send to any other node..
			// => remove every source node from the nodes set and take the last remaining one as Base Station
			for (Topology.Link link: links) {
				nodes.remove(link.getSource());
			}
			try {
				baseNodeID = nodes.iterator().next();
			} catch (NoSuchElementException e) {}

			// create a WSNTopology, replace every occurrence of baseNodeId with null
			Topology topology = new Topology();
			for (Topology.Link link: links) {
				topology.addLink(new Topology.Link(link.getSource().equals(baseNodeID)?null:link.getSource(), link.getTarget().equals(baseNodeID)?null:link.getTarget(), link.getWeight()));
			}

			return topology;
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			System.err.println("Couldn't parse TinyOS Topology2");
			e.printStackTrace();
		}

		return null;
	}
	/**
	 * sends a command to the tinyOS blip console
	 * @param cmd
	 * @throws InterruptedException
	 */
	public boolean _doTelnetCommand(String cmd) throws InterruptedException {
		return _getTelnetCommandResponse(cmd, 0) != null;
	}
	/**
	 * sends a command to the tinyOS blip console
	 * @param cmd
	 * @throws InterruptedException
	 */
	public String _getTelnetCommandResponse(String cmd, int milliSecondsToWaitForOutput) throws InterruptedException {
		return _blipTerminal.sendCommand(cmd, milliSecondsToWaitForOutput);
	}
	/**
	 * sends a command to the tinyOS blip console
	 * @param cmd
	 * @throws InterruptedException
	 */
	public String _getTelnetCommandResponse(String cmd) throws InterruptedException {
		return _getTelnetCommandResponse(cmd, 1000);
	}


	/* protected member */
	protected int _blipPort;
	protected int _udpPort;
	protected int _topologyUpdateInterval=20;

	protected Thread _topologyThread=null;
	protected Thread _packetThread=null;

	protected DatagramSocket _serverSocket;
	
	public DatagramSocket getServerSocket(){
		return _serverSocket;
	}

	protected BLIPTerminalClient _blipTerminal=null;
}
