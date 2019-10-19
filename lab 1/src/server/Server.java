package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket serverSocket;
    public ClientSocket clientSocket;
    public static final int PORT = 4004; //8080 ??

    public Server() throws IOException {
        serverSocket = new ServerSocket(PORT);
    }

    public void work() throws IOException {
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                clientSocket = new ClientSocket(socket);
            }
        } finally {
            serverSocket.close();
        }
    }
}
