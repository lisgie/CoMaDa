package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.util.Set;

import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;


public class WSNSSHServerModuleHelpController extends WSNSSHController {

	public TerminalString helpCommand(SSHCommand sshCmd) {
		TerminalString output = new TerminalString();

		output.append("Available commands: exit");

		Set<String> cmds = this.getServerModule().getRegisteredController().keySet();
		for (String cmd: cmds) {
			output.append(", "+cmd);
		}
		output.append("\r\n");

		return output;
	}

}
