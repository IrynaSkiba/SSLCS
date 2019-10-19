package server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server;
        try {
            server = new Server();
            server.work();
        } catch (IOException e) {
            System.out.println("Сервер бросил исключениеее");
            e.printStackTrace();
        }

    }
}
