// Copyright 2023 Axini B.V. https://www.axini.com, see: LICENSE.txt.

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// SmartDoorServer: WebSocketServer for the SmartDoor SUT.
public class SmartDoorServer extends WebSocketServer {
    private static Logger logger =
        LoggerFactory.getLogger(SmartDoorServer.class);

    private String      manufacturer;
    private SmartDoor   door;
    private WebSocket   client_connection;

    public SmartDoorServer(String manufacturer, InetSocketAddress address) {
        super(address);
        this.manufacturer = manufacturer;
        this.door = null;
        this.client_connection = null;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("Client connected");
        if (client_connection != null) {
            logger.error("Another client tried to connect to this WebSocketServer");
            conn.close();
        }
        else {
            client_connection = conn;
            door = new SmartDoor(this, manufacturer);
            logger.info("SmartDoor created: " + manufacturer);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Client disconnected");
        client_connection = null;
        door = null;
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Received message: " + message);

        String[] arr = message.split(":");
        String action = arr[0];
        String param = (arr.length == 2) ? arr[1] : null;

        switch(action) {
        case "RESET":
            reset_sut(param);
            break;
        default:
            door.handle_input(message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("Error occurred: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
        setConnectionLostTimeout(0);
        setConnectionLostTimeout(100);
    }

    public void send(String message) {
        logger.info("Sending message: " + message);
        if (client_connection == null)
            logger.error("WebSocket connection is not yet initialised");
        else
            client_connection.send(message);
    }

    private void reset_sut(String manufacturer) {
        if (manufacturer == null) {
            logger.info("Resetting SUT");
            door = new SmartDoor(this, this.manufacturer);
            send("RESET_PERFORMED");
        } else {
            if (SmartDoor.MANUFACTURERS.contains(manufacturer)) {
                logger.info("Resetting SUT and change manufacturer to: " +
                             manufacturer);
                this.manufacturer = manufacturer;
            }
            else {
                // unknown manufacturer
                logger.error("Unknown manufacturer: " + manufacturer + "!");
                logger.info("Resetting SUT to: " + SmartDoor.M_AXINI + " instead.");
                this.manufacturer = SmartDoor.M_AXINI;
            }
            door = new SmartDoor(this, this.manufacturer);
            send("RESET_PERFORMED");
        }
    }
}
