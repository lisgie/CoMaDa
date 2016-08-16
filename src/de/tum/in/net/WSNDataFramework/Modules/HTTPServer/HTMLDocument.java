package de.tum.in.net.WSNDataFramework.Modules.HTTPServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * helps to build a HTML document (tries to generate valid html5).
 * most methods offer a fluent interface.
 * 
 * @author André Freitag
 *
 */
public class HTMLDocument {

	/**
	 * constructs empty HTMLDocument
	 */
	public HTMLDocument() {
	}
	/**
	 * constructs HTMLDocument from given file.
	 * 
	 * @param file
	 */
	public HTMLDocument(File file) {
		this.addHtml(file);
	}


	/**
	 * assembles HTML document
	 * 
	 * @return HTML string
	 */
	public byte[] toBytes() {
		try {
			return this.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	/**
	 * calls this.toString()
	 */
	@Override
	public String toString() {
		String html = this._assemble();
		return html;
	}


	/**
	 * gets document title
	 * @return
	 */
	public String getTitle() {
		return this._title;
	}
	/**
	 * sets document title
	 * 
	 * @param title
	 * @return this for fluent interface
	 */
	public HTMLDocument setTitle(String title) {
		this._title = title;

		return this;
	}

	/**
	 * gets the ID of the default container. NULL references the <body> element.
	 * 
	 * @return containerID | NULL for <body>
	 */
	public String getDefaultContainer() {
		return _defaultContainerID;
	}

	/**
	 * sets the ID of the default container. NULL references the <body> element.
	 * 
	 * @param containerID
	 * @return hits for fluent interface
	 */
	public HTMLDocument setDefaultContainer(String containerID) {
		_defaultContainerID = containerID;

		return this;
	}


	/**
	 * gets the value of a previously set JS variable.
	 * 
	 * @param name
	 * @return
	 */
	public Object getVar(String name) {
		return _jsVars.get(name);
	}
	/**
	 * adds a JS variable to the top of the document's <head> element.
	 * val is decoded using JSONValue.toJSONString().
	 * 
	 * @param name
	 * @return
	 */
	public HTMLDocument addVar(String name, Object val) {
		_jsVars.put(name, val);

		return this;
	}


	/**
	 * Adds a HTML block to the default container.
	 * Merges the <head> sections, appends the <body> of the `html` to the default container.
	 * Ill formed HTML gets transformed to well formed XML.
	 * 
	 * @return this for fluent interface
	 */
	public HTMLDocument addHtml(String html) {
		return this.addHtml(html, this.getDefaultContainer());
	}
	/**
	 * Adds a HTML block to a specific container.
	 * Merges the <head> sections, appends the <body> of the `html` to the element #containerID of the document (or <body> if NULL passed).
	 * Ill formed HTML gets transformed to well formed XML.
	 * 
	 * @return this for fluent interface
	 */
	public HTMLDocument addHtml(String html, String containerID) {
		if (!_html.containsKey(containerID)) {
			_html.put(containerID, new LinkedList<String>());
		}
		_html.get(containerID).add(html);

		return this;
	}
	/**
	 * Adds a HTML block from a specific file to the default container.
	 * Merges the <head> section, appends the <body> of the file to the default default container.
	 * Ill formed HTML gets transformed to well formed XML.
	 * 
	 * @param file
	 * @return this
	 */
	public HTMLDocument addHtml(File file) {
		this.addHtml(file, this.getDefaultContainer());

		return this;
	}
	/**
	 * Adds a HTML block from a specific file to a specific container.
	 * Merges the <head> sections, appends the <body> of the file to the element #containerID of the document (or <body> if NULL passed).
	 * Ill formed HTML gets transformed to well formed XML.
	 * 
	 * @param file
	 * @param containerID or NULL to reference the <body> element
	 * @return this
	 */
	public HTMLDocument addHtml(File file, String containerID) {
		if (!_localHtml.containsKey(containerID)) {
			_localHtml.put(containerID, new ArrayList<File>());
		}
		_localHtml.get(containerID).add(file);

		return this;
	}


	/**
	 * adds a JS block
	 * 
	 * @param code
	 * @return this for fluent interface
	 */
	public HTMLDocument addJs(String code) {
		_js.add(code);

		return this;
	}

	/**
	 * adds a CSS block
	 * 
	 * @param style
	 * @return this for fluent interface
	 */
	public HTMLDocument addCss(String style) {
		_css.add(style);

		return this;
	}
	/**
	 * adds a local JS reference (will get substituted into body rather than linked and loaded at browser runtime)
	 * 
	 * @param file
	 * @return this for fluent interface
	 */
	public HTMLDocument addLocalJs(File file) {
		_localJs.add(file);

		return this;
	}
	/**
	 * adds a local CSS reference (will get substituted into body rather than linked and loaded at browser runtime)
	 * 
	 * @param file
	 * @return this for fluent interface
	 */
	public HTMLDocument addLocalCss(File file) {
		_localCss.add(file);

		return this;
	}
	/**
	 * adds a external JS reference (will get linked and loaded at browser runtime)
	 * 
	 * @param url
	 * @return this for fluent interface
	 */
	public HTMLDocument addExternalJs(String url) {
		_externalJs.add(url);

		return this;
	}

	/**
	 * adds a external CSS reference (will get linked and loaded at browser runtime)
	 * 
	 * @param url
	 * @return this for fluent interface
	 */
	public HTMLDocument addExternalCss(String url) {
		_externalCss.add(url);

		return this;
	}




	/*** protected helper ***/
	protected String _assemble() {

		/* assemble given js/css */
		ArrayList<String> cssToLoad=new ArrayList<String>();
		cssToLoad.addAll(_externalCss); // collect CSS references

		ArrayList<String> jsToLoad=new ArrayList<String>();
		jsToLoad.addAll(_externalJs); // collect JS references

		String jsVars=""; // assemble JS variables
		for (String vname: _jsVars.keySet()) {
			String val=null;
			try {
				val = JSONValue.toJSONString(_jsVars.get(vname));
			} catch (Exception e) {
			}

			if (val != null) {
				jsVars += vname + "=" + val + ";";
			}
		}

		String css=""; // assemble inline CSS
		for (String c: _css) {
			css += c + ";\n\n";
		}
		for (File f: _localCss) {
			css += _readFileContent(f) + ";\n\n";
		}

		String js=""; // assemble inline JS
		for (String j: _js) {
			js += j + ";\n\n";
		}
		for (File f: _localJs) {
			js += _readFileContent(f) + ";\n\n";
		}

		/*
		 * assemble given html
		 * use Jsoup for parsing & getting html container since given HTML may not be valid XML..
		 */
		Document doc = Document.createShell("");
		doc.head().append("<title>"+this.getTitle()+"</title>"); // Title is a required element of a HTML document.

		// append JS vars
		doc.head().append("<script type=\"text/javascript\">" + jsVars + "</script>");

		// handle given body HTML
		if (_html.containsKey(null)) {
			for (String h: _html.get(null)) {
				_mergeHtml(h, doc, null);
			}
		}
		if (_localHtml.containsKey(null)) {
			for (File f: _localHtml.get(null)) {
				_mergeHtml(_readFileContent(f), doc, null);
			}
		}
		for (String cID: _html.keySet()) {
			if (cID==null) continue;
			for (String h: _html.get(cID)) {
				_mergeHtml(h, doc, cID);
			}
		}
		for (String cID: _localHtml.keySet()) {
			if (cID==null) continue;
			for (File f: _localHtml.get(cID)) {
				_mergeHtml(_readFileContent(f), doc, cID);
			}
		}

		// append external CSS / JS
		for (String l: cssToLoad) {
			doc.head().append("<link rel=\"stylesheet\" href=\""+l+"\"/>");
		}
		for (String l: jsToLoad) {
			doc.head().append("<script type=\"text/javascript\" src=\""+l+"\"></script>");
		}

		// append inline CSS / JS
		if (!css.trim().equals("")) {
			doc.head().append("<style type=\"text/css\">"+css+"</style>");
		}
		if (!js.trim().equals("")) {
			doc.head().append("<script type=\"text/javascript\">"+js+"</script>");
		}

		return doc.html();
		/* build XHTML1.1 document */
		//		XMLBuilder builder = new XMLBuilder("-//W3C//DTD XHTML 1.1//EN", "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd");
		//		builder.el("html", new String[]{"xmlns","http://www.w3.org/1999/xhtml"});
		//
		//		builder.el("head");
		//			try {
		//				builder.xml(doc.head().html());
		//			} catch (SAXException e) {
		//			}
		//			for (String l: cssToLoad) {
		//				builder.el("link", new String[]{
		//										"rel", "stylesheet",
		//										"href", l}).close();
		//			}
		//			for (String l: jsToLoad) {
		//				builder.el("script", new String[]{
		//										"type", "text/javascript",
		//										"src", l}).text(" ").close();
		//			}
		//			if (!css.trim().equals(""))
		//				builder.el("style", new String[]{"type", "text/css"}).text(css).close();
		//			if (!js.trim().equals(""))
		//				builder.el("script", new String[]{"type", "text/javascript"}).text(js).close();
		//			builder.close();
		//
		//		builder.el("body");
		//			try {
		//				builder.xml(doc.body().html());
		//			} catch (SAXException e) {
		//			}
		//			builder.close();
		//
		//		return builder.toString();
	}

	/**
	 * reads the content of a file into a string
	 * 
	 * @param file file handle
	 * @return file content
	 */
	protected String _readFileContent(File file) {
		String str = "";

		try {
			InputStreamReader in=null;
			try {
				in = new InputStreamReader(new FileInputStream(file), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				in = new InputStreamReader(new FileInputStream(file));
			}

			BufferedReader br=new BufferedReader(in);
			String l;
			while ((l = br.readLine()) != null) {
				str += l+"\n";
			}

			in.close();
		} catch (Exception e) {
		}

		return str;
	}

	/**
	 * merges html into doc.
	 * merges <head>: <title>,<base> and <meta> with same "name" or "http-equiv" attributes are substituted. other elements are added.
	 * appends <body> of html to either <body> or container with containerID!=null of doc.
	 * 
	 * @param html
	 * @param doc
	 * @param String containerID (null to reference body)
	 */
	protected void _mergeHtml(String html, Document doc, String containerID) {

		Document newDoc = Jsoup.parse(html);

		/* merge head */
		List<Node> nodes = newDoc.head().childNodes();
		for (Node node: nodes) {
			String nodeName = node.nodeName();

			// only one <title> / <base> element allowed
			if (nodeName.equalsIgnoreCase("title") || nodeName.equalsIgnoreCase("base")) {
				doc.head().getElementsByTag(nodeName).remove();
			}
			// only one <meta> element with same name or http-equiv attribute allowed
			if (nodeName.equalsIgnoreCase("meta")) {
				String metaName = node.attributes().get("name");
				String metaHttpEquiv = node.attributes().get("http-equiv");

				if (metaName != "")
					doc.head().select("meta[name="+metaName+"]").remove();
				if (metaHttpEquiv != "")
					doc.head().select("meta[http-equiv="+metaHttpEquiv+"]").remove();
			}

			doc.head().appendChild(node.clone());
		}


		/* merge body */
		Element body = containerID != null
				? doc.getElementById(containerID)
						: doc.body();
				if (body == null) return;

				body.append(newDoc.body().html());
	}


	/*** protected member ***/
	protected String _title="";
	protected String _defaultContainerID=null;

	protected Map<String,Object> _jsVars=new LinkedHashMap<String,Object>();

	protected Map<String,List<String>> _html=new LinkedHashMap<String,List<String>>();
	protected Map<String,List<File>> _localHtml=new LinkedHashMap<String,List<File>>();

	protected List<String> _js=new LinkedList<String>();
	protected List<String> _css=new LinkedList<String>();
	protected List<File> _localJs=new LinkedList<File>();
	protected List<File> _localCss=new LinkedList<File>();
	protected List<String> _externalJs=new LinkedList<String>();
	protected List<String> _externalCss=new LinkedList<String>();
}
