package de.tum.in.net.WSNDataFramework.Modules.TinyOS;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import de.tum.in.net.WSNDataFramework.WSNModule.WSNModuleStatus;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTMLDocument;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPRequest;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.HTTPResponse;
import de.tum.in.net.WSNDataFramework.Modules.HTTPServer.WSNHTTPController;
import de.tum.in.net.WSNDataFramework.Modules.TinyOS.TinyOSHelperModule.ExecTracker;



public class HTTPController extends WSNHTTPController {

	public void indexAction(HTTPRequest request, HTTPResponse response) {
		HTMLDocument doc = this.getServerModule()
				.getMainTemplate(request)
				.addHtml(this.getFile("index.html"));

		Map<String,Object> jsonTinyos = new LinkedHashMap<String,Object>();
		if (this.module() instanceof TinyOSHelperModule && this.module().getStatus().getStatus()==WSNModuleStatus.STATUS.RUNNING) {
			TinyOSHelperModule module = (TinyOSHelperModule)this.module();

			jsonTinyos.put("running", true);
			jsonTinyos.put("version", module.getVersion());
			jsonTinyos.put("directories", module.getMakeDirectories());
			jsonTinyos.put("devices", module.getTargetDevices());
			jsonTinyos.put("target_platforms", module.getTargetPlatforms());
			jsonTinyos.put("make_extras", module.getMakeExtras());
			jsonTinyos.put("sensorboards", module.getSensorboards());

			if (_execTracker.containsKey("tunnel") && !_execTracker.get("tunnel").isFinished()) {
				jsonTinyos.put("tunnelsRunning", _execTracker.get("tunnel").getContent());
			}
		} else {
			jsonTinyos.put("running", false);
		}
		doc.addVar("tinyos", jsonTinyos);


		response.body = doc.toBytes();
	}

	public void domakeAction(HTTPRequest request, HTTPResponse response) {
		String category = request.arguments.get("category").toString();
		String cmd = request.arguments.get("cmd").toString();
		String dir = request.arguments.get("dir").toString();

		if (_execTracker.containsKey(category) && !_execTracker.get(category).isFinished()) {
			_execTracker.get(category).abort();
		}

		String text="";
		if (this.module() instanceof TinyOSHelperModule && this.module().getStatus().getStatus()==WSNModuleStatus.STATUS.RUNNING) {
			TinyOSHelperModule module = (TinyOSHelperModule)this.module();

			ExecTracker tracker = module.doMake(cmd, dir);
			tracker.waitForNext();
			text = tracker.next();
			_execTracker.put(category, tracker);
		}

		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		jsonResult.put("text", text);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}
	public void dotunnelAction(HTTPRequest request, HTTPResponse response) {
		String category = "tunnel";
		String pw = request.arguments.get("pw").toString();
		if (pw.equals("")) pw=null;
		_sudoPW = pw;
		String device = request.arguments.get("device").toString();
		String target = request.arguments.get("target").toString();

		if (_execTracker.containsKey(category) && !_execTracker.get(category).isFinished()) {
			_execTracker.get(category).abort();
		}

		String text="";
		if (this.module() instanceof TinyOSHelperModule && this.module().getStatus().getStatus()==WSNModuleStatus.STATUS.RUNNING) {
			TinyOSHelperModule module = (TinyOSHelperModule)this.module();

			ExecTracker tracker = module.openIPTunnel(device, target, pw);
			tracker.waitForNext();
			text = tracker.next();
			_execTracker.put(category, tracker);
		}

		Map<String,String> jsonResult = new LinkedHashMap<String,String>();
		jsonResult.put("text", text);
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	public void updateexecAction(HTTPRequest request, HTTPResponse response) {
		String category = request.arguments.get("category").toString();

		String text="";
		boolean finished=true;
		if (_execTracker.containsKey(category)) {
			//_execTracker.get(category).waitForNext();
			text = _execTracker.get(category).next();
			finished = _execTracker.get(category).isFinished();
		}

		Map<String,Object> jsonResult = new LinkedHashMap<String,Object>();
		jsonResult.put("text", text);
		if (finished) {
			jsonResult.put("finished", true);
		}
		response.body = JSONValue.toJSONString(jsonResult).getBytes();
	}

	public void abortexecAction(HTTPRequest request, HTTPResponse response) {
		String category = request.arguments.get("category").toString();

		if (_execTracker.containsKey(category)) {
			_execTracker.get(category).abort();
			_execTracker.remove(category);
		}

		if (category.equals("tunnel")) {
			if (this.module() instanceof TinyOSHelperModule && this.module().getStatus().getStatus()==WSNModuleStatus.STATUS.RUNNING) {
				TinyOSHelperModule module = (TinyOSHelperModule)this.module();

				module.killIPTunnel(_sudoPW);
				_sudoPW=null;
			}
		}
	}

	public void rebuildtopologyAction(HTTPRequest request, HTTPResponse response) {

		Map<String,Object> jsonRet=new HashMap<String,Object>();

		boolean success=false;
		if (this.module()!=null && this.module().app().module(TinyOSDriver.class)!=null) {
			success = this.module().app().module(TinyOSDriver.class).doBlipTopologyRebuild();
			if (!success) {
				jsonRet.put("msg", "couldn't reach telnet console");
			}
		}

		jsonRet.put("success", success);
		response.body = JSONValue.toJSONString(jsonRet).getBytes();
	}

	public void blipconsoleAction(HTTPRequest request, HTTPResponse response) {
		Runtime rt = Runtime.getRuntime();
		try {
			if (this.module().app().module(TinyOSDriver.class) != null) {
				rt.exec("/usr/bin/xterm -hold -e /usr/bin/telnet localhost "+this.module().app().module(TinyOSDriver.class).getBlipTelnetPort());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void blipstatsAction(HTTPRequest request, HTTPResponse response) {

		Map<String,Object> jsonRet=new HashMap<String,Object>();

		boolean success=false;

		if (this.module()!=null && this.module().app().module(TinyOSDriver.class)!=null) {
			String ret = this.module().app().module(TinyOSDriver.class).doBlipStatsCommand();
			success = ret != null;
			jsonRet.put("msg", success ? ret : "couldn't reach telnet console");
		}

		jsonRet.put("success", success);
		response.body = JSONValue.toJSONString(jsonRet).getBytes();
	}

	public void topologypngAction(HTTPRequest request, HTTPResponse response) {
		response.headers.put("Content-Type", Arrays.asList(new String[]{"image/png"}));
		response.body = this.module()!=null && (this.module() instanceof TinyOSHelperModule)
				? ((TinyOSHelperModule)this.module()).getLatestTopologyPNG()
						: null;
	}


	protected Map<String,ExecTracker> _execTracker = new HashMap<String,ExecTracker>();
	protected String _sudoPW=null;
}
