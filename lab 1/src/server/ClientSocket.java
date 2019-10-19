package server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientSocket extends Thread {
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public ClientSocket(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        bufferedReader = new BufferedReader(inputStreamReader);
        out = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
        bufferedWriter = new BufferedWriter(outputStreamWriter);
        start();
    }

    @Override
    public void run() {
        String word, command;
        try {
            while (true) {
                word = bufferedReader.readLine();
                command = word.substring(0, word.indexOf(" "));

                if (command.equals("close")) {
                    System.out.println("Client was closed.");
                    this.closeConnection(); // харакири
                    break;
                }

                if (command.equals("time")) {
                    send(new java.util.Date().toString());
                    System.out.println(word);
                    continue;
                }

                if (command.equals("echo")) {
                    System.out.println("Echo: " + word);
                    send(word.substring(word.indexOf(" ") + 1));
                    continue;
                }

                if (command.equals("upload")) {
                    upload(word.substring(word.indexOf(" ") + 1));
                    continue;
                }

                send("Такой команды нет");
                System.out.println(word + " - такой команды нет");
            }
        } catch (NullPointerException ignored) {
        } catch (SocketException ex) {
            System.out.println("lohhhhhh"); //это вылазит, если отключается клиент
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    private void upload(String name) throws IOException {
        File file = new File("C:\\Users\\Skiba Iryna\\Desktop\\files\\" + name);
        long length = Long.parseLong(bufferedReader.readLine());

        //DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream output = new FileOutputStream(file);
        try {
            int i = 1000;
            for (int j = 0; j < length / 1000; j++) {
                while (i != 0) {
                    output.write(in.read());
                    i--;
                }
                output.flush();
                i = 1000;
            }

            long k = length % 1000;
            while (k != 0) {
                output.write(in.read());
                k--;

            }
            output.flush();
            System.out.println("File successfully created!");
        } finally {
            output.close();

        }
    }

    private void send(String msg) {
        try {
            bufferedWriter.write(msg + "\n");
            bufferedWriter.flush();
        } catch (IOException ignored) {
        }
    }

    private void closeConnection() {
        try {
            if (!socket.isClosed()) {
                socket.close();
                bufferedReader.close();
                bufferedWriter.close();
                this.interrupt();
            }
        } catch (IOException ignored) {
        }
    }
}