package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.util.Map;

import de.tum.in.net.WSNDataFramework.Node;
import de.tum.in.net.WSNDataFramework.Node.NodeID;
import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;


public class WSNSSHServerModuleNodesController extends WSNSSHController {

	public TerminalString helpCommand(SSHCommand sshCmd) {
		TerminalString output = new TerminalString();

		Map<NodeID,Node> nodes = this.getServerModule().app().wsn().nodes();
		for (Node node: nodes.values()) {
			output.append("\r\n"+node.getNodeID()+":\r\n");

			for (Node.Datum field: node.data()) {
				output.append("     "+field.getID()+" ("+(field.getName()!=null?field.getName():field.getType())+"): ");
				output.append(field.getValue()!=null?field.getValue().toString():"NULL");
				output.append(" " + field.getUnit());
				output.append("\r\n");
			}
		}

		return output;
	}

}
