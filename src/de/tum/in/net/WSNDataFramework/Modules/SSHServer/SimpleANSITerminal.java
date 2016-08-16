package de.tum.in.net.WSNDataFramework.Modules.SSHServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tum.in.net.WSNDataFramework.Modules.SSHServer.SSHCommandHandler.SSHCommand;


/**
 * Simple Text Terminal that understands and uses ANSI Escape sequences.
 * 
 * @author André Freitag
 *
 */
public class SimpleANSITerminal {


	/**
	 * Creates SimpleANSITerminal. Uses ISO-8859-1 charset for in & output.
	 * 
	 * @param in
	 * @param out
	 */
	public SimpleANSITerminal(InputStream in, OutputStream out, Map<String,SSHCommandHandler> handler) {
		this(in, out, "ISO-8859-1", handler);
	}
	/**
	 * Creates SimpleANSITerminal using given charset for in & output.
	 * 
	 * @param in
	 * @param out
	 * @param charset
	 */
	public SimpleANSITerminal(InputStream in, OutputStream out, String charset, Map<String,SSHCommandHandler> handler) {
		_in = in;
		_out = out;
		_charset = Charset.forName(charset);
		_handler = handler!=null ? handler : new HashMap<String,SSHCommandHandler>();
		_remoteTerminal = new RemoteTerminal(_out, _charset);

		_startTerminal();
	}

	/**
	 * Stops terminal.
	 * Closes working Thread.
	 * Streams must be closed manually!
	 */
	public void stop() {
		synchronized (_thread) {
			_thread.interrupt();
		}
	}

	/* messages */
	/**
	 * @return welcome message
	 */
	public TerminalString getWelcomeMessage() {
		return
				new TerminalString()
		.startColor(TerminalString.Color.BLUE, null, true)
		.append("Welcome to the WSN-Terminal.")
		.endColor();
	}
	/**
	 * @return exit message
	 */
	public TerminalString getExitMessage() {
		return new TerminalString("See you next time :)");
	}
	/**
	 * @return prompt message
	 */
	public TerminalString getPromptMessage() {
		return
				new TerminalString()
		.startColor(TerminalString.Color.RED, null, true)
		.append("WSN #> ")
		.endColor();
	}
	/**
	 * @return newline sequence
	 */
	public String getNL() {
		return "\r\n";
	}




	/* protected helper */
	/**
	 * start listening to the stream
	 */
	protected void _startTerminal() {
		if (_thread.isAlive()) return;

		/* welcome user */
		_remoteTerminal
		.put(this.getWelcomeMessage())
		.put(this.getNL() + this.getNL());
		_updateCommandPrompt();

		/* start listener thread */
		synchronized(_thread) {
			_thread.start();
		}
	}
	/**
	 * listener thread
	 */
	protected void _thread() {
		try {
			if (_in == null) return;

			synchronized (_in) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(_in, _charset));
				int charsRead;
				char[] buf = new char[256];
				while((charsRead=reader.read(buf)) > 0) {
					_handleInput(Arrays.copyOf(buf, charsRead));
				}
			}
		} catch (IOException e) {
			SimpleANSITerminal.this.stop();
		}
	}
	/**
	 * handles input (gets called from _thread)
	 * 
	 * @param input
	 */
	protected void _handleInput(char[] input) {

		synchronized (_rawInputBuffer) {
			// append to _rawInputBuffer
			for (int i=0; i<input.length; i++)
				_rawInputBuffer.add(input[i]);

			// parse input
			ArrayList<Character> tempBuffer = new ArrayList<Character>(_rawInputBuffer);
			while(!tempBuffer.isEmpty()) {

				int i=0;
				char c = tempBuffer.get(i++);
				boolean handled=false;


				if (c == (char)27) { // escape character -> handle

					if (tempBuffer.size() > 1) {
						char tmp = tempBuffer.get(i++);
						if (tmp == (char)91) { // CSI sequence -> treat as ANSI escape sequence

							// assemble sequence
							ArrayList<Character> escapeSequence=new ArrayList<Character>();
							escapeSequence.add(c);
							escapeSequence.add(tmp);
							while (i<tempBuffer.size() && !handled) {

								tmp = tempBuffer.get(i++);
								escapeSequence.add(tmp);

								// sequence end: character from ASCII @ to ~
								if (tmp >= 64 && tmp <= 126) {
									handled=true;
								}
							}

							_handleCSISequence(escapeSequence);

						} else { // non CSI sequence -> discard
							handled=true;
						}
					}

				} else if (c == 13 || c == 10) { // \r || \n
					if (c == 13 && i<tempBuffer.size() && tempBuffer.get(i) == 10) { // skip \r if it is followed by \n
						c = tempBuffer.get(i++);
					}
					_handleControlCode(c);
					handled=true;
				} else if (c == 127 || c >= 0 && c <= 31) { // backspace ||  C0 control code -> handle
					_handleControlCode(c);
					handled=true;
				} else { // other character -> append to _inputBuffer
					_inputBuffer.append(c);
					handled=true;
				}

				if (!handled) { // if not handled -> stop parsing, wait for more input
					break;
				} else { // removed handled characters
					for (int j=0; j<i; j++) {
						tempBuffer.remove(0);
					}
				}


				// update command prompt (for each processed character)
				_updateCommandPrompt();
			}
			_rawInputBuffer = tempBuffer;
		}

	}
	/**
	 * handles a given control code (gets called from _handleInput)
	 * 
	 * @param c
	 */
	protected void _handleControlCode(char c) {
		boolean echo=false;

		/* handle control code */
		if (c == 127 || c == 24) { // delete / cancel ('backspace')
			_inputBuffer.remove(_inputBuffer.getPos()-1);
		} else if (c == 13 || c == 10) { // \r / \n
			/* handle command */
			String cmd = _inputBuffer.getContent();
			_inputBuffer.clear();

			_handleCommand(cmd);
		}


		/* append character to _inputBuffer */
		if (echo) {
			_inputBuffer.append(c);
		}
	}
	/**
	 * handles a given CSI sequence (gets called from _handleInput)
	 * 
	 * @param seq
	 */
	protected void _handleCSISequence(List<Character> seq) {
		// TODO: seq element may be null!

		Integer[] cmd = RemoteTerminal.parseCSISequence(seq);
		if (cmd == null) return;

		if (cmd[0] == 65) { // cursor up -> history up
			if (cmd.length > 2) return;

			int times = cmd.length>1
					? (cmd[1]>0 ? cmd[1] : 1)
							: 1;
					if (_cmdHistory.pos() < 0)
						_cmdHistory.save(_inputBuffer.toString());
					_inputBuffer.setContent(_cmdHistory.up(times).get());

					_updateCommandPrompt();

		} else if (cmd[0] == 66) { // cursor down -> history down
			if (cmd.length > 2) return;

			int times = cmd.length>1
					? (cmd[1]>0 ? cmd[1] : 1)
							: 1;
					_inputBuffer.setContent(_cmdHistory.down(times).get());

					_updateCommandPrompt();

		} else if (cmd[0] == 67) { // cursor forward
			if (cmd.length > 2) return;

			int times = cmd.length>1 ? cmd[1] : 1;

			_inputBuffer.setPos(_inputBuffer.getPos() + times);
			_updateCommandPrompt();

		} else if (cmd[0] == 68) { // cursor backward
			if (cmd.length > 2) return;

			int times = cmd.length>1 ? cmd[1] : 1;

			_inputBuffer.setPos(_inputBuffer.getPos() - times);
			_updateCommandPrompt();

		} else if (cmd[0] == 126) { // ~ (special PuTTY? codes)
			if (cmd.length < 2) return;

			if (cmd[1] == 1) { // "POS1"
				_inputBuffer.setPos(0);
			} else if (cmd[1] == 3) { // "DEL"
				_inputBuffer.remove(_inputBuffer.getPos());
			} else if (cmd[1] == 4) { // "END"
				_inputBuffer.setPos(_inputBuffer.length());
			}

		}
	}
	/**
	 * handles a given command (gets called from _handleInput)
	 * 
	 * @param cmdline
	 */
	protected void _handleCommand(String cmdline) {
		cmdline = cmdline.trim();

		// add to history
		if (!cmdline.equals(""))
			_cmdHistory.push(cmdline);

		// parse commandline
		SSHCommand cmd = new SSHCommand(cmdline);

		// execute cmd
		if (_handler.containsKey(cmd.cmd)) {
			_remoteTerminal
			.put(this.getNL())
			.put(_handler.get(cmd.cmd).handleSSHCommand(cmd))
			.put(new TerminalString().endColor().append(this.getNL()))
			.flush();
		} else if (cmdline.equals("")) {
			_remoteTerminal
			.put(this.getNL())
			.flush();
		} else {
			_remoteTerminal
			.put(this.getNL())
			.put("command not found")
			.put(this.getNL())
			.flush();
		}
	}

	/**
	 * updates the command prompt
	 */
	protected void _updateCommandPrompt() {
		_remoteTerminal
		.eraseLine()
		.setColumnPos(0)
		.put(this.getPromptMessage())
		.put(_inputBuffer.toString())
		.setColumnPos(this.getPromptMessage().length() + _inputBuffer.getPos())
		.flush();
	}



	/* protected member */
	protected InputStream _in=null;
	protected OutputStream _out=null;
	protected Charset _charset=null;

	protected RemoteTerminal _remoteTerminal=null;
	protected Map<String,SSHCommandHandler> _handler=null;

	protected ArrayList<Character> _rawInputBuffer=new ArrayList<Character>();
	protected InputBuffer _inputBuffer=new InputBuffer();
	protected CommandHistory _cmdHistory=new CommandHistory(50);

	protected Thread _thread=new Thread(new Runnable(){
		@Override
		public void run() {
			_thread();
		}});



	/* static helper */
	/**
	 *  class for assembling a string to send to the terminal.
	 */
	public static class TerminalString {
		/**
		 * constructs empty TerminalString.
		 */
		public TerminalString() {
		}
		/**
		 * constructs and initializes TerminalString with s.
		 * @param s
		 */
		public TerminalString(String s) {
			this();
			this.append(s);
		}
		/**
		 * changes color for strings appended in the future.
		 * 
		 * @param textColor NULL for terminal's default
		 * @return this (for fluent interface)
		 */
		public TerminalString startColor(Color textColor) {
			return this.startColor(textColor, null, false);
		}
		/**
		 * changes color for strings appended in the future.
		 * 
		 * @param textColor NULL for terminal's default
		 * @param backgroundColor NULL for terminal's default
		 * @return this (for fluent interface)
		 */
		public TerminalString startColor(Color textColor, Color backgroundColor) {
			return this.startColor(textColor, backgroundColor, false);
		}
		/**
		 * changes color for strings appended in the future.
		 * 
		 * @param textColor NULL for terminal's default
		 * @param backgroundColor NULL for terminal's default
		 * @param bright
		 * @return this (for fluent interface)
		 */
		public TerminalString startColor(Color textColor, Color backgroundColor, boolean bright) {
			int tcv = textColor!=null ? 30+textColor.value() : 39;
			int bcv = backgroundColor!=null ? 40+backgroundColor.value() : 49;
			int brightv = bright ? 1 : 22;

			_components.add(RemoteTerminal.assembleCSISequence(109, new int[]{tcv, bcv, brightv}));

			return this;
		}
		/**
		 * sets color to default for strings appended in the future.
		 * 
		 * @return this (for fluent interface)
		 */
		public TerminalString endColor() {
			this.startColor(null,null,false);
			return this;
		}
		/**
		 * appends a string.
		 * 
		 * @param s
		 * @return this (for fluent interface)
		 */
		public TerminalString append(String s) {
			_components.add(s);
			_strlen += s.length();

			return this;
		}

		/**
		 * assembles string.
		 * 
		 * @return
		 */
		public byte[] assemble(Charset charset) {
			// assemble
			ArrayList<Byte> bytes = new ArrayList<Byte>();
			for (Object o: _components) {
				if (o instanceof String) {
					String s = (String) o;

					byte[] bs = s.getBytes(charset);
					for (int i=0; i<bs.length; i++) {
						bytes.add(bs[i]);
					}
				} else if (o instanceof byte[]) {
					byte[] bs = (byte[]) o;
					for (int i=0; i<bs.length; i++) {
						bytes.add(bs[i]);
					}
				}
			}

			// convert to byte[]
			byte[] ret = new byte[bytes.size()];

			for (int i=0; i<bytes.size(); i++) {
				ret[i] = (bytes.get(i) != null) ? bytes.get(i) : 0;
			}

			return ret;
		}

		/**
		 * gets string length.
		 * 
		 * @return
		 */
		public int length() {
			return _strlen;
		}

		public enum Color {
			BLACK(0), RED(1), GREEN(2), YELLOW(3), BLUE(4), MAGENTA(5), CYAN(6), WHITE(7);

			Color(int val) {
				_val = val;
			}
			int value() {
				return _val;
			}
			private final int _val;
		}

		/* protected member */
		protected ArrayList<Object> _components=new ArrayList<Object>();
		protected int _strlen=0;
	}
	/**
	 * helper class for communicating with remote terminal.
	 * 
	 * @author André Freitag
	 */
	protected static class RemoteTerminal {
		public RemoteTerminal(OutputStream out, Charset charset) {
			_out=out;
			_charset=charset;
		}
		/**
		 * outputs some bytes to the terminal
		 * 
		 * @param output
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal put(byte[] output) {
			if (output == null) return this;

			synchronized (_out) {
				try {
					_out.write(output);
				} catch (IOException e) {
				}
			}

			return this;
		}
		/**
		 * outputs a string to the terminal (uses SimpleANSITerminal._charset for decoding)
		 * 
		 * @param output
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal put(String output) {
			if (output == null) return this;

			this.put(output.getBytes(_charset));

			return this;
		}
		/**
		 * outputs a string to the terminal (uses SimpleANSITerminal._charset for decoding)
		 * 
		 * @param output
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal put(TerminalString output) {
			if (output == null) return this;

			this.put(output.assemble(_charset));

			return this;
		}

		/**
		 * set cursor's column position (starting with 0)
		 * 
		 * @param pos
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal setColumnPos(int pos) {
			this.put(assembleCSISequence(71, new int[]{pos+1}));

			return this;
		}

		/**
		 * erase current line
		 * 
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal eraseLine() {
			this.put(assembleCSISequence(75, new int[]{2}));

			return this;
		}

		/**
		 * flushes output stream
		 * 
		 * @return this (for fluent interface)
		 */
		public RemoteTerminal flush() {
			synchronized (_out) {
				try {
					_out.flush();
				} catch (IOException e) {
				}
			}

			return this;
		}



		/* protected member */
		protected OutputStream _out=null;
		protected Charset _charset=null;




		/* static helper */
		/**
		 * assembles a CSI sequence
		 * 
		 * @param cmd
		 * @param args
		 * @return CSI byte sequence
		 */
		public static byte[] assembleCSISequence(int cmd, int[] args) {
			if (cmd < 64 || cmd > 126) return new byte[0]; // cmd must be a character from ASCII @ to ~


			String ret="";

			ret += (char)27; // ESC
			ret += (char)91; // [

			if (args != null && args.length > 0) { // args
				for (int i=0; i<args.length; i++) {
					String sArg = "" + args[i];
					for (int j=0; j<sArg.length(); j++) {
						ret += (char)(48 + Integer.valueOf(sArg.substring(j,j+1))); // arg
					}
					ret += (char)59; // ;
				}
				ret = ret.substring(0, ret.length()-1);
			}

			ret += (char)cmd; // cmd

			return ret.getBytes();
		}
		/**
		 * parses a CSI sequence
		 * 
		 * @param sequence
		 * @return array(0: command, i: arguments)
		 */
		public static Integer[] parseCSISequence(List<Character> sequence) {
			if (sequence.size() < 3) return null; // at least 3 characters: ESC [ cmd
			if (sequence.get(0)!=27 || sequence.get(1)!=91) return null; // must start with ESC [

			int cmd = sequence.get(sequence.size()-1);
			if (cmd < 64 || cmd > 126) return null; // cmd must be a character from ASCII @ to ~

			ArrayList<Integer> ret = new ArrayList<Integer>();
			ret.add(cmd);

			Integer arg=null;
			for (int i=2; i<sequence.size()-1; i++) {
				Character c = sequence.get(i);
				if (c == null) return null; // invalid sequence

				if (c == 59) { // ASCII semicolon -> new argument starts
					ret.add(arg);
					arg=null;
				} else {
					int tmp = (int)sequence.get(i) - 48;
					if (tmp < 0 || tmp > 9) return null; // arguments must be a number or a semicolon

					arg = ((arg!=null)?arg*10:0) + tmp;
				}
			}
			if (arg != null) {
				ret.add(arg);
			}

			return ret.toArray(new Integer[0]);
		}


	}
	/**
	 * helper class for handling the input from a prompt.
	 * (keeps record of the input string + cursor position)
	 * 
	 * @author André Freitag
	 */
	protected class InputBuffer {
		/**
		 * gets buffer position.
		 * 
		 * @return position
		 */
		public int getPos() {
			return _bufferPos;
		}
		/**
		 * sets buffer position.
		 * adjusts it if it is out of range.
		 * 
		 * @param pos
		 * @return this (for fluent interface)
		 */
		public InputBuffer setPos(int pos) {
			synchronized (_bufferPos) {
				_bufferPos = pos;
				if (_bufferPos > _buffer.length())
					_bufferPos = _buffer.length();
				if (_bufferPos < 0)
					_bufferPos = 0;
			}

			return this;
		}
		/**
		 * gets the content
		 * 
		 * @return buffer content
		 */
		public String getContent() {
			return _buffer.toString();
		}
		/**
		 * sets the content.
		 * 
		 * @param s
		 * @return this (for fluent interface)
		 */
		public InputBuffer setContent(String s) {
			this.remove(0,_buffer.length());
			this.append(s);

			return this;
		}
		/**
		 * gets the length of the content.
		 * 
		 * @return content lenght
		 */
		public int length() {
			return _buffer.length();
		}
		/**
		 * appends a character at the right position and adjusts it.
		 * 
		 * @param c
		 * @return this (for fluent interface)
		 */
		public InputBuffer append(char c) {
			this.append(""+c);

			return this;
		}
		/**
		 * appends a string at right position and adjusts it.
		 * 
		 * @param c
		 * @return this (for fluent interface)
		 */
		public InputBuffer append(String s) {
			synchronized (_buffer) {
				_buffer.insert(_bufferPos, s);
				this.setPos(_bufferPos+s.length());
			}

			return this;
		}
		/**
		 * removes a character from the input buffer and adjusts position.
		 * 
		 * @param pos
		 * @return this (for fluent interface)
		 */
		public InputBuffer remove(int pos) {
			this.remove(pos, 1);

			return this;
		}
		/**
		 * removes a character sequence from the input buffer and adjusts position.
		 * 
		 * @param startPos
		 * @param count how much characters to remove
		 * @return this (for fluent interface)
		 */
		public InputBuffer remove(int startPos, int count) {
			synchronized (_buffer) {
				if (startPos < 0 || startPos >= _buffer.length()
						|| count <= 0) return this;

				int endPos = startPos + count;
				if (endPos > _buffer.length())
					endPos = _buffer.length();

				_buffer.delete(startPos, endPos);
				int newInputBufferPos = (endPos <= _bufferPos)
						? _bufferPos - (endPos-startPos)
								: _bufferPos;
						this.setPos(newInputBufferPos);
			}

			return this;
		}
		public InputBuffer clear() {
			_buffer = new StringBuffer();
			_bufferPos=0;

			return this;
		}
		/**
		 * overrides toString() -> returns this.getContent()
		 */
		@Override
		public String toString() {
			return this.getContent();
		}


		/* protected member */
		protected StringBuffer _buffer=new StringBuffer();
		protected Integer _bufferPos=0;
	}
	/**
	 * helper class for maintaining a command history.
	 * 
	 * @author André Freitag
	 */
	protected class CommandHistory {
		/**
		 * constructs a CommandHistory.
		 * 
		 * @param inputBuffer
		 * @param maxSize
		 */
		public CommandHistory(int maxSize) {
			_maxSize = maxSize;
		}

		/**
		 * get entry currently pointing at.
		 * 
		 * @return current entry || last saved() value if currently not walking in history (getPosition()==-1)
		 */
		public String get() {
			return _pos > -1
					? _history.get(_pos)
							: _saved;
		}
		/**
		 * get current position.
		 * 
		 * @return -1 if currently not walking in history || current position in history
		 */
		public int pos() {
			return _pos;
		}
		/**
		 * gets size of history.
		 * 
		 * @return size
		 */
		public int size() {
			return _history.size();
		}

		/**
		 * pushes a new command to the history and rewinds().
		 * 
		 * @param cmd
		 * @return this (for fluent interface)
		 */
		public CommandHistory push(String cmd) {

			if (_history.size()<=0 || !cmd.trim().equals(_history.getFirst())) {
				_history.addFirst(cmd.trim());

				if (_history.size() > _maxSize) {
					_history.subList(_maxSize, _history.size()).clear();
				}
			}

			this.rewind();

			return this;
		}
		/**
		 * saves a value that will get returned from get() if bottom of history is reached (internal pointer==-1).
		 * enables to add a temporary bottom element that will not permanently get saved to the history.
		 * 
		 * @param val
		 * @return this (for fluent interface)
		 */
		public CommandHistory save(String val) {
			_saved = val;
			return this;
		}


		/**
		 * go `n` steps up in history.
		 * 
		 * @param n
		 * @return this (for fluent interface)
		 */
		public CommandHistory up(int n) {
			if (n > 0) {
				_setPos(_pos + n);
			}

			return this;
		}
		/**
		 * go `n` steps down in history.
		 * 
		 * @param n
		 * @return this (for fluent interface)
		 */
		public CommandHistory down(int n) {
			if (n > 0) {
				_setPos(_pos - n);
			}

			return this;
		}
		/**
		 * goes to a specific position in history.
		 * 
		 * @param pos
		 * @return this (for fluent interface)
		 */
		public CommandHistory setPos(int pos) {
			_setPos(pos);
			return this;
		}
		/**
		 * sets internal pointer to -1. sets saved value to ""
		 * means: currently not walking in history, user input is active.
		 * 
		 * @return first entry
		 */
		public void rewind() {
			_pos = -1;
			_saved = "";
		}


		/* protected helper */
		/**
		 * sets internal pointer. adjusts it if out of range.
		 * 
		 * @param pos
		 */
		protected void _setPos(int pos) {
			_pos = pos;
			if (_pos < -1)
				_pos = -1;
			if (_pos >= _history.size())
				_pos = _history.size()-1;
		}



		/* protected member */
		protected int _maxSize=0;
		protected LinkedList<String> _history=new LinkedList<String>();
		protected int _pos=-1;
		protected String _saved="";
	}



}
