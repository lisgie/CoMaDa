package de.tum.in.net.WSNDataFramework.Modules.TinyOS;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.net.telnet.TelnetClient;

/**
 * Allows to communicate with the TinyOS BLIP terminal
 * 
 * @author andre
 */
public class BLIPTerminalClient {

	/* constructors */
	/**
	 * standard constructor, uses default port
	 */
	public BLIPTerminalClient() {
		this(6106);
	}
	/**
	 * constructor, uses given port
	 * 
	 * @param port
	 */
	public BLIPTerminalClient(int port) {
		_port = port;
		_start();
	}



	/* public methods */
	/**
	 * Sends a command to the terminal.
	 * Waits for `wait` seconds after the first response is received and returns everything that was printed on the terminal in this time.
	 * 
	 * @param cmd command to send
	 * @param wait milliseconds to wait after first response before returning.
	 * @return response || NULL if failed
	 * @throws InterruptedException
	 */
	public synchronized String sendCommand(String cmd, int wait) throws InterruptedException {
		if (wait < 0 || _isShutdown)
			return null;

		synchronized (_freeToUseMonitor) {
			// wait for the terminal to be free to use
			while (_freeToUse != true) {
				try {
					_freeToUseMonitor.wait();
				} catch (InterruptedException e) {
					throw e;
				}
			}
			System.err.println("FREE TO USE");
			// clear response buffer
			_response.delete(0, _response.length());

			// send command
			synchronized (_responseMonitor) {
				if (!_sendCommand(cmd)) {
					return null;
				}
				System.err.println("SENT");

				// wait for response
				try {
					_responseMonitor.wait();
				} catch (InterruptedException e) {
					throw e;
				}
				System.err.println("RESPONSE");
			}


			// wait additional time
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				throw e;
			}

			// return response
			return _response.toString();
		}
	}

	public void shutdown() {
		_isShutdown = true;
		if (_thread != null) {
			_thread.interrupt();
			try {
				_thread.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean isShutdown() {
		return _isShutdown;
	}



	/* protected methods */
	protected void _start() {
		_thread = new Thread(new Runnable() {

			@Override
			public void run() {
				_thread();
			}

		});
		_thread.setName("TinyOS BLIPTerminalClient");
		_thread.start();
	}
	protected void _connectionInit() throws SocketException, IOException, InterruptedException {
		if (_telnet != null) {
			_connectionShutdown();
		}
		try {
			_telnet = new TelnetClient();
			_telnet.connect(InetAddress.getLocalHost(), _port);
		} catch (UnknownHostException e) {
			System.err.println("TinyOS - BLIP: localhost unknown O.o");
		}

		Thread.sleep(_waitAfterInit);
	}
	protected void _connectionShutdown() throws IOException {
		synchronized (_telnet) {
			if (_telnet != null) {
				_telnet.disconnect();
				_telnet = null;
			}
		}
	}
	protected boolean _sendCommand(String cmd) throws InterruptedException {
		if (_telnet != null) {
			synchronized (_telnet) {
				Thread.sleep(_waitBeforeSend);
				if (_telnet.isConnected() && _telnet.isAvailable()) {
					PrintStream out = new PrintStream(_telnet.getOutputStream());
					out.println(cmd);
					out.flush();

					return true;
				}
			}
		}

		return false;
	}
	protected void _thread() {
		InputStream in=null;
		while (!Thread.interrupted()) {
			// establish connection
			if (_telnet == null) {
				try {
					_connectionInit();
					in = _telnet.getInputStream();
				} catch (InterruptedException e) {
					System.err.println("INT");
					break;
				} catch (InterruptedIOException e) {
					System.err.println("INT");
					break;
				} catch (Exception e) {
					synchronized (_freeToUseMonitor) {
						_freeToUse = false;
						_freeToUseMonitor.notify();
					}
					//System.err.println("TinyOS - BLIP: couldn't init");
					//e.printStackTrace();
					try {
						Thread.sleep(_waitRetry);
					} catch (InterruptedException e1) {
						break;
					}
					continue;
				}
			}
			synchronized (_telnet) {
				if (!_telnet.isConnected() || !_telnet.isAvailable()) {
					try {
						_connectionInit();
						in = _telnet.getInputStream();
					} catch (InterruptedException e) {
						System.err.println("INT");
						break;
					} catch (InterruptedIOException e) {
						System.err.println("INT");
						break;
					} catch (Exception e) {
						synchronized (_freeToUseMonitor) {
							_freeToUse = false;
							_freeToUseMonitor.notify();
						}
						//System.err.println("TinyOS - BLIP: couldn't init");
						//e.printStackTrace();
						try {
							Thread.sleep(_waitRetry);
						} catch (InterruptedException e1) {
							break;
						}
						continue;
					}
				}
			}

			// signal: FREE TO USE
			synchronized (_freeToUseMonitor) {
				_freeToUse = true;
				_freeToUseMonitor.notify();
			}


			// wait for input
			try {
				System.err.println("waiting..");
				int c = -1;
				while ((c=in.read()) != -1) {
					synchronized (_responseMonitor) {
						_response.append((char)c);
						_responseMonitor.notify();
					}
				}
			} catch (InterruptedIOException e1) {
				break;
			} catch (IOException e2) {
			} finally {// shut down
				try {
					_connectionShutdown();
					synchronized (_freeToUseMonitor) {
						_freeToUse = false;
						_freeToUseMonitor.notify();
					}
				} catch (InterruptedIOException e) {
					System.err.println("INT");
					break;
				} catch (IOException e) {
					System.err.println("TinyOS - BLIP: couldn't shut down");
					e.printStackTrace();
				}
			}

			System.err.println("FERTIG");
		}

		if (_telnet != null) {
			try {
				_telnet.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/* protected member */
	protected int _port;
	protected TelnetClient _telnet=null;
	protected Boolean _freeToUse=false;
	protected Object _freeToUseMonitor=new Object();
	protected StringBuffer _response=new StringBuffer();
	protected Object _responseMonitor=new Object();
	protected int _waitRetry=2000;
	protected int _waitAfterInit=5000;
	protected int _waitBeforeSend=2000;
	protected Thread _thread=null;
	protected boolean _isShutdown=false;
}
