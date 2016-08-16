package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;




/**
 * Index controller.
 * 
 * @author André Freitag
 *
 */
public class WSNHTTPHelpController extends WSNHTTPController {

	/**
	 * Index-Action
	 * @param request
	 * @param response
	 */
	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));

		response.body = doc.toBytes();
	}

	/**
	 * FAQ
	 * 
	 * @param request
	 * @param response
	 */
	public void faqAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("faq.html"));

		response.body = doc.toBytes();
	}

	/**
	 * Interesting Links
	 * 
	 * @param request
	 * @param response
	 */
	public void linksAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("links.html"));

		response.body = doc.toBytes();
	}

	/**
	 * Publications
	 * 
	 * @param request
	 * @param response
	 */
	public void publicationsAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("publications.html"));

		response.body = doc.toBytes();
	}

	/**
	 * Impressum
	 * 
	 * @param request
	 * @param response
	 */
	public void impressumAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("impressum.html"));

		response.body = doc.toBytes();
	}

}
