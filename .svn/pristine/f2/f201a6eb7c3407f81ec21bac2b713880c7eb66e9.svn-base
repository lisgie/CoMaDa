package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SimpleANSITerminal.TerminalString;


/**
 * handles a SSH command
 * 
 * @author André Freitag
 */
public interface SSHCommandHandler {
	/**
	 * handles a SSH command
	 * 
	 * @param cmd
	 * @return terminal output
	 */
	public TerminalString handleSSHCommand(SSHCommand cmd);
	
	/**
	 * represents a called command line.
	 * 
	 * @author André Freitag
	 */
	public static class SSHCommand {
		
		/** original command line */
		public String commandline;
		
		/** command (first command line argument) */
		public String                cmd;
		/** arguments (command line arguments not starting with - or --) */
		public List<String>          args;
		/** dashed arguments (-xArg, where x is the character and Arg the String) */
		public Map<Character,String> dargs;
		/** double dashed arguments (--arg, where arg is the String) */
		public List<String>          ddargs;
		
		
		/**
		 * constructor.
		 * 
		 * @param commandline
		 */
		public SSHCommand(String commandline) {
			/*** initialize member ***/
			this.commandline = commandline;
			this.args = new ArrayList<String>();
			this.dargs = new HashMap<Character,String>();
			this.ddargs = new ArrayList<String>();
			
			
			/*** parse commandline ***/
			// strip strings and replace with "i
			ArrayList<String> strs=new ArrayList<String>();
			commandline = _substituteStrings(commandline, strs);
			
			// group arguments in: cmd (first arg), dashed args (-xArg), double dashed args (--arg), normal args (arg).
			String[] temp = commandline.split(" ");
			
			this.cmd = temp[0];
			
			for (int i=1; i<temp.length; i++) {
				String a = temp[i];
				
				if (a.startsWith("--")) { // double dashed
					a = a.substring(2);
					this.ddargs.add(_resubstituteStrings(a, strs));
				} else if (a.startsWith("-")) { // dashed
					a = a.substring(1);
					a = _resubstituteStrings(a, strs);
					
					if (a.length() > 0) {
						char x = a.charAt(0);
						String rest="";
						
						if (a.length() > 1) {
							rest = a.substring(1);
						}
						
						this.dargs.put(x, rest);
					}
				} else { // normal
					this.args.add(_resubstituteStrings(a, strs));
				}
			}
		}
		/**
		 * extracts strings (marked with "") from input, adds them to substitutes and substitute them in input with "i, where i is the index of the extracted string in substitutes.
		 * 
		 * @param input
		 * @param substitutes
		 * @return input with substituted strings
		 */
		protected String _substituteStrings(String input, List<String> substitutes) {
			
			Matcher m = Pattern.compile("\"(.*?)(\"|$)").matcher(input);
			StringBuffer sb = new StringBuffer();
			for (int i=substitutes.size(); m.find(); i++) {
				m.appendReplacement(sb, "\""+i);
				substitutes.add(m.group(1));
			}
			m.appendTail(sb); 
			
			return sb.toString();
		}
		/**
		 * resubstitutes strings into input. replaces occurences of "i within input with the entry of substitutes with index i.
		 * does NOT wrap resubstituted strings in "".
		 * 
		 * @param input
		 * @param substitutes
		 * @return input with reinserted strings
		 */
		protected String _resubstituteStrings(String input, List<String> substitutes) {
			
			Matcher m = Pattern.compile("\"(\\d)").matcher(input);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				int i = Integer.parseInt(m.group(1));
				
				m.appendReplacement(sb, i<substitutes.size() ? substitutes.get(i) : m.group());
			}
			m.appendTail(sb); 
			
			return sb.toString();
		}
	}
}
