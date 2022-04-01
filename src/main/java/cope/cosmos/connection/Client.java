package cope.cosmos.connection;

import cope.cosmos.client.Cosmos;
import cope.cosmos.client.Cosmos.ClientType;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author linustouchtips
 * @since 03/20/2021
 */
public class Client {

    // server connection socket
    private Socket socket;

    public Client(String server, int port) {

        // try to connect to server
        try {
            try {
                socket = new Socket(server, port);

                // successful connection
                System.out.println("Connected to " + server + " at port #" + port);

            } catch (UnknownHostException exception) { // host doesn't exist

                // print error message if in development environment
                if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                    exception.printStackTrace();
                }
            }
        } catch (IOException exception) {

            // print error message if in development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Attempts to connect to a server
     * @param server The server name
     * @param port The server port number
     * @throws IOException connection exception
     */
    public void connect(String server, int port) throws IOException {
        socket = new Socket(server, port);
    }

    /**
     * Attempts to disconnect from the current server connection
     */
    public void disconnect() {

        // attempt to close the socket
        try {
            socket.close();
        } catch (IOException exception) {

            // print error message if in development environment
            if (Cosmos.CLIENT_TYPE.equals(ClientType.DEVELOPMENT)) {
                exception.printStackTrace();
            }
        }
    }
}
