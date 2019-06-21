package it.unimib.disco.utils.tests;

import java.util.logging.Logger;

import it.unimib.disco.net.SocketServer;

public class SocketServerMain {

	public static final int DEFAULT_BIND_PORT = 1337;

	private static final Logger _logger = Logger.getLogger(SocketServerMain.class.getName());
	
	public static void main(String[] args) {
		
		int argPort = DEFAULT_BIND_PORT;
		
		if (args.length > 0)
			argPort = Integer.parseInt(args[0]);
		
		try {
			
			SocketServer server = new SocketServer(argPort);
			server.call();
		}
		catch (Exception e) {
			_logger.severe(e.getMessage());
		}
	}
}
