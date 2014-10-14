package com.maxwit.witim;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public class SingletonConnection extends XMPPTCPConnection {
	private static SingletonConnection connection = null;
	public static String server = "127.0.0.1";
	public static String username = "";
	public static String password = "";
	public static String conName = "";

	private SingletonConnection(ConnectionConfiguration config) {
		super(config);
	}

	public static SingletonConnection getInstance() {
		if (connection == null) {
			ConnectionConfiguration config = new ConnectionConfiguration(
					server, 5222);
			config.setSecurityMode(SecurityMode.disabled);
			config.setSendPresence(false);
			connection = new SingletonConnection(config);
		}

		return connection;
	}
}
