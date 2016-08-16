package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.util.Set;

import de.tum.in.net.WSNDataFramework.Topology;
import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;


public class WSNSSHServerModuleTopologyController extends WSNSSHController {

	public TerminalString helpCommand(SSHCommand sshCmd) {
		TerminalString output = new TerminalString("\r\n");

		Topology topology = this.getServerModule().app().wsn().topology();
		if (topology != null) {
			Set<Topology.Link> links = topology.getLinks();
			for (Topology.Link link: links) {
				output.append(link.getSource()!=null?link.getSource().toString():"base station");
				output.append(" -> ");
				output.append(link.getTarget()!=null?link.getTarget().toString():"base station");
				output.append("\r\n");
			}
		} else {
			output.append("topology not available");
		}

		return output;
	}


}
