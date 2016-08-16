package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * HTTP Controller. Handles HTTP requests.
 * Dynamically calls request.action+"Action"(HTTPRequest,HTTPResponse) to handle a request.
 * If a called URI can be mapped to a real existing file within this controller's working directory, the file is passed to the client and no action is called (only the basic handleRequest()).
 * The arguments may be omitted. Calls best fitting method.
 * 
 * 
 * @author André Freitag
 *
 */
public class HTTPController {


	/**
	 * Creates a HTTPController without working directory.
	 * 
	 */
	public HTTPController() {
	}
	/**
	 * Creates a HTTPController with given working directory.
	 * 
	 * @param workingDirectory or NULL for virtual mode.
	 */
	public HTTPController(String workingDirectory) {
		this.setWorkingDirectory(workingDirectory);
	}


	/**
	 * gets this controller's working directory.
	 * 
	 * @return
	 */
	public String getWorkingDirectory() {
		return _workingDirectory;
	}
	/**
	 * sets this controller's working directory.
	 * 
	 * @param dir
	 * @return this for fluent interface
	 */
	public HTTPController setWorkingDirectory(String dir) {
		_workingDirectory = dir;
		if (_workingDirectory != null && _workingDirectory.charAt(_workingDirectory.length()-1) != '/')
			_workingDirectory += '/';

		return this;
	}

	/**
	 * gets a File from the working directory.
	 * 
	 * @param relPath path relative to the working directory
	 * @return File handle | NULL if file not found
	 */
	public File getFile(String relPath) {
		File file=null;

		if (this.getWorkingDirectory() != null) {
			try {
				String filePath = this.getWorkingDirectory()+"/"+relPath;
				file = new File(filePath);
			} catch (Exception e) {
			}
		}

		return file != null && file.exists() && file.isFile()
				? file
						: null;
	}


	/**
	 * handles a request and calls specific "*Action" method.
	 * 
	 * @param request
	 * @param response
	 * @return TRUE if request was handled, FALSE otherwise
	 */
	public boolean handleRequest(HTTPRequest request, HTTPResponse response) {

		boolean ret=false;

		Class<?>[] args = new Class<?>[]{HTTPRequest.class, HTTPResponse.class};
		for (int i=args.length; i>=0; i--) {
			try {

				Method m = this.getClass().getMethod(request.action.toLowerCase()+"Action", Arrays.copyOf(args, i));
				m.invoke(this, Arrays.copyOf(new Object[]{request, response}, i));

				ret = true;
				break; // break after successfully having invoked a method

			} catch (NoSuchMethodException e) {
				//e.printStackTrace();
			} catch (Exception e) {
				ret = true;
				e.printStackTrace();
			}
		}

		return ret;
	}

	/**
	 * Tries to map a requestURI to an existing file in the working directory.
	 * 
	 * @param requestURI
	 * @return File handle if mapping was successful, NULL else
	 */
	protected File _mapWorkingDirectoryFile(String requestURI) {

		String relPath = requestURI.substring(requestURI.indexOf("/",1)+1);
		return this.getFile(relPath);
	}



	/* protected member */
	String _workingDirectory=null;
}
