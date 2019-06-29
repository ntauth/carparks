package it.unimib.disco.tests;

import java.util.logging.Logger;

import it.unimib.disco.config.NetConfig;
import it.unimib.disco.net.SocketServer;

/**
 * @brief Test class for @see SocketServer
 *
 */
public class SocketServerMain {

	private static final Logger _logger = Logger.getLogger(SocketServerMain.class.getName());
	
	public static void main(String[] args) {
		
		int argPort = NetConfig.DEFAULT_PLATFORM_PORT;
		
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
