package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Creates a HTTP Server that calls registered controllers.
 * 
 * @author André Freitag
 *
 */
public class HTTPServer {

	/**
	 * creates a HTTP Server that listens on `port`.
	 * 
	 * @param port
	 * @throws IOException
	 */
	public HTTPServer(int port) throws IOException {
		this(port, null);
	}
	/**
	 * creates a HTTP Server with a specific workingDirectory that listens on `port`.
	 * 
	 * @param port
	 * @port workingDirectory directory where to fetch files from
	 * @throws IOException
	 */
	public HTTPServer(int port, String workingDirectory) throws IOException {
		this.setWorkingDirectory(workingDirectory);

		// create server
		_server = HttpServer.create(new InetSocketAddress(port), 5);
		_server.setExecutor(null); // creates a default executor

		// create contexts
		_server.createContext("/", new HTTPServerRequestHandler());

		// start server
		_server.start();
	}
	/**
	 * stops server
	 */
	public void stop() {
		if (_server != null) {
			_server.stop(0);
		}
	}

	/**
	 * get this server's working directory.
	 * 
	 * @return path to working directory | NULL if module is virtual (no working directory given)
	 */
	public String getWorkingDirectory() {
		return _workingDirectory;
	}
	/**
	 * set this server's working directory.
	 * working directory used to fetch files from.
	 * 
	 * @param workingDirectory
	 * @return this for fluent interface
	 */
	public HTTPServer setWorkingDirectory(String workingDirectory) {
		_workingDirectory = workingDirectory;
		if (_workingDirectory != null && _workingDirectory.charAt(_workingDirectory.length()-1) != '/')
			_workingDirectory += '/';

		return this;
	}

	/**
	 * register controller. each controller name may only be registered once.
	 * Handler gets called each time `/name/*` is requested.
	 * Handler not called if URI could be mapped to real existing file within working directory!
	 * 
	 * @param name
	 * @param controller
	 * @return true if the controller was registered correctly, otherwise false (if the controller name was already registered)
	 */
	public boolean registerController(String name, HTTPController controller) {
		if (!this._controller.containsKey(name)) {
			this._controller.put(name, controller);
			return true;
		}

		return false;
	}
	/**
	 * gets all registered controllers.
	 * 
	 * @return
	 */
	public Map<String,HTTPController> getRegisteredControllers() {
		return new HashMap<String,HTTPController>(_controller);
	}

	/**
	 * gets default controller
	 * @return
	 */
	public String getDefaultController() {
		return "index";
	}
	/**
	 * gets default action
	 * 
	 * @return
	 */
	public String getDefaultAction() {
		return "index";
	}
	/**
	 * Replaces Default-Controller + Default-Action in an request.
	 * Modifies the given request + returns it.
	 * 
	 * @param request
	 * @return the given request
	 */
	public HTTPRequest replaceDefaultRequest(HTTPRequest request) {
		if (request.controller!=null && request.controller.equals("")) {
			request.controller = this.getDefaultController();
		}
		if (request.action!=null && request.action.equals("")) {
			request.action = this.getDefaultAction();
		}

		return request;
	}




	/* protected helper */
	/**
	 * request handler
	 * 
	 * @author André Freitag
	 *
	 */
	protected class HTTPServerRequestHandler implements HttpHandler {

		/**
		 * handles basic requests and calls controllers
		 */
		@Override
		public void handle(HttpExchange arg) throws IOException {


			/* parse request */
			String requestURI = arg.getRequestURI().getSchemeSpecificPart();

			String requestBody="";
			if (arg.getRequestBody().available() > 0) {
				InputStreamReader isr = new InputStreamReader(arg.getRequestBody());

				char[] buf = new char[arg.getRequestBody().available()];
				isr.read(buf);
				requestBody = new String(buf);

				isr.close();
			}


			/* create HTTPRequest + HTTPResponse */
			HTTPRequest request = HTTPServer.this.replaceDefaultRequest(new HTTPRequest(requestURI, requestBody));
			HTTPResponse response = new HTTPResponse();


			/* output file / call controller */
			boolean handled=false;

			File realFile = _mapFile(request.requestURI);
			if (realFile != null) { // URI could be mapped to file
				// set content type
				String mimeType = _getMimeType(realFile.getName());
				if (mimeType != null)
					response.headers.put("Content-Type", Arrays.asList(mimeType));


				// read file
				ByteArrayOutputStream buf = new ByteArrayOutputStream();
				FileInputStream is = new FileInputStream(realFile);
				try {

					byte[] temp = new byte[1024];
					int read;
					while((read = is.read(temp)) > 0){
						buf.write(temp, 0, read);
					}

					response.body = buf.toByteArray();
					handled = true;
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				} finally {
					is.close();
				}
			} else { // call controller
				HTTPController controller = _controller.get(request.controller);
				if (controller != null) {
					try {
						handled = controller.handleRequest(request, response);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}


			/* send response */
			if (!handled)
				response.responseCode = 404;
			arg.getResponseHeaders().putAll(response.headers);
			arg.sendResponseHeaders(response.responseCode, response.body.length);

			OutputStream os = arg.getResponseBody();
			os.write(response.body);
			os.close();
		}

	}



	/* protected helper */
	/**
	 * maps a requestURI to a file
	 * 
	 * @param requestURI
	 * @return file URI could be mapped | NULL otherwise
	 */
	protected File _mapFile(String requestURI) {
		File file=null;

		if (this.getWorkingDirectory() != null) {
			try {
				String filePath = this.getWorkingDirectory()+"/"+requestURI;
				file = new File(filePath);
			} catch (Exception e) {
			}
		}

		return file != null && file.exists() && file.isFile()
				? file
						: null;
	}

	/**
	 * gets mime-type by filename.
	 * currently uses URLConnection.guessContentTypeFromName()
	 * 
	 * @param filename
	 * @return mime-type or null
	 */
	protected String _getMimeType(String filename) {
		return URLConnection.guessContentTypeFromName(filename);
	}



	/* protected member */
	protected HttpServer _server;
	protected String _workingDirectory=null;
	protected HashMap<String,HTTPController> _controller=new HashMap<String,HTTPController>();
}
