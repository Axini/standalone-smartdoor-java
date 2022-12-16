// Copyright 2023 Axini B.V. https://www.axini.com, see: LICENSE.txt.

import java.net.InetSocketAddress;

// Wrapper class to start the SmartDoor SUT.
public class SmartDoorSut {
	public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("usage: java SmartDoorSut <host> <port> <manufacturer>");
            System.exit(1);
        }

		String host = args[0];
		int port = Integer.parseInt(args[1]);
		String manufacturer = args[2];

		InetSocketAddress address = new InetSocketAddress(host, port);
		SmartDoorServer server = new SmartDoorServer(manufacturer, address);

		server.start();
	}
}
