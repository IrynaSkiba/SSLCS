package client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.work();
        } catch (IOException e) {
            System.out.println("Клиент сдох");
            e.printStackTrace();
        }
    }
}
