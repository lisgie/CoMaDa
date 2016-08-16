package de.tum.in.net.WSNDataFramework;



/**
 * Protocol used to parse the incoming WSN data stream
 * 
 * @author André Freitag
 */
public abstract class WSNProtocol {

	/**
	 * @return the {@link WSNApp} this protocol belongs to
	 */
	public WSNApp app() {
		return _app;
	}

	/**
	 * @return the human readable name of this protocol
	 */
	abstract public String getName();

	/**
	 * takes an incoming packet, parses it and returns the parsed version.<br/>
	 * the returned packet is then passed along to the next {@link WSNProtocol} in the stack.<br/>
	 * may throw an {@link WSNProtocolUnsupportedPacketException} if another {@link WSNProtocolPacket} type was expected.<br/>
	 * may return a derived version of {@link WSNProtocolPacket} fitted to the next protocol in the stack.
	 * 
	 * @param packet {@link WSNProtocolPacket} to be parsed
	 * @return the parsed and perhaps enriched {@link WSNProtocolPacket} || NULL to stop further propagation
	 */
	abstract public WSNProtocolPacket process(WSNProtocolPacket packet) throws WSNProtocolException;

	/**
	 * @return the current {@link WSNProtocolStatus} of this protocol
	 */
	public WSNProtocolStatus getStatus() {
		return _status;
	}


	/* protected methods */
	/**
	 * sets the current {@link WSNProtocolStatus} of this protocol
	 * @param status
	 * @param msg
	 * @return this for fluent interface
	 */
	protected WSNProtocol _setStatus(WSNProtocolStatus.STATUS status, String msg) {
		_status = new WSNProtocolStatus(status, msg);
		return this;
	}
	/**
	 * sets the current {@link WSNProtocolStatus} of this protocol.
	 * omits setting the status message.
	 * 
	 * @param status
	 * @return this for fluent interface
	 */
	protected WSNProtocol _setStatus(WSNProtocolStatus.STATUS status) {
		_status = new WSNProtocolStatus(status);
		return this;
	}


	/* protected member */
	WSNApp _app = null;
	WSNProtocolStatus _status = new WSNProtocolStatus(WSNProtocolStatus.STATUS.IDLING);



	/* helper methods only visible to the WSNDataFramework packet */
	/**
	 * sets the WSNApp this Protocol belongs to.
	 * 
	 * @param app
	 * @return this for fluent interface
	 */
	final WSNProtocol setApp(WSNApp app) {
		_app = app;
		return this;
	}



	/* helper */
	/**
	 * represents the status of a module
	 * 
	 * @author André Freitag
	 */
	public static class WSNProtocolStatus {
		public enum STATUS {
			RUNNING, IDLING, ERROR
		}

		/**
		 * constructor
		 * 
		 * @param status
		 * @param message
		 */
		public WSNProtocolStatus(WSNProtocolStatus.STATUS status) {
			_status = status;
			_message = null;
		}
		/**
		 * constructor
		 * 
		 * @param status
		 * @param message
		 */
		public WSNProtocolStatus(WSNProtocolStatus.STATUS status, String message) {
			_status = status;
			_message = message;
		}

		/**
		 * gets WSNModuleStatus.STATUS.
		 */
		public STATUS getStatus() {
			return _status;
		}
		/**
		 * gets attached message.
		 */
		public String getMessage() {
			return _message;
		}

		/* protected member */
		protected STATUS _status;
		protected String _message;
	}
}
