package com.eclipse.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class JettyServer {
	
	public static void main(String[] args) {
		Server server = new Server(4889);
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		handler.addServletWithMapping(DFResponse.class, "/dialogflow");
		try {
			server.start();
			server.join();	
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}