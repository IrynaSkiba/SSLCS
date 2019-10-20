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

    private Token token;
    private boolean transferFlag = false;

    public ClientSocket(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        bufferedReader = new BufferedReader(inputStreamReader);
        out = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        //проверяем сохранен ли этот клиент в хэшмапе
        if (Server.tokens.get(socket.getInetAddress()) != null)
            token = Server.tokens.get(socket.getInetAddress()); //просто запоминаем ссылку на токен для проверки прерванной передачи
        else
            token = new Token(false, 0, 0, null);

        start();
    }

    @Override
    public void run() {
        String word, command;
        //тут надо добавить отсылку токена!!! с флагом false
        try {
            sendStartInformation();
        } catch (IOException e) {
            System.out.println("да у тебя куча проблем, крошка!");
            e.printStackTrace();
        }

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
            if (transferFlag) {
                //здесь мы понимаем, что клиент отключился
                //если он передавал файл, то надо установить флаг true в токене
                token.setFlag(true);
                System.out.println("токен был сохранен"); //это вылазит, если отключается клиент
            } else System.out.println("передачи файла не было");
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

    private void sendStartInformation() throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeBoolean(token.isFlag());
        dos.writeLong(token.getSize());
        dos.writeLong(token.getPosition());
        //or send();
        if (token.isFlag())
            send(token.getName());
        else send("Not found!");
        if (token.isFlag()) getFullFile();
    }

    private void getFullFile() throws IOException {
        File file = new File("C:\\Users\\Skiba Iryna\\Desktop\\files\\" + token.getName());

        FileOutputStream output = new FileOutputStream(file, true); //дописываем в конец
        try {

            //пропускаем, то что уже записано, но надо избавиться от этой херни
            for (int j = 0; j < token.getPosition(); j++) {
                in.read();
            }

            int i = 1000;
            for (int j = 0; j < (token.getSize() - token.getPosition()) / 1000; j++) {
                while (i != 0) {
                    output.write(in.read());
                    i--;

                    //   token.incrementPosition();
                }
                output.flush();
                i = 1000;
            }

            long k = (token.getSize() - token.getPosition()) % 1000;
            while (k != 0) {
                output.write(in.read());
                k--;

                //   token.incrementPosition();
            }
            output.flush();
            System.out.println("File successfully created!");

        } catch (IOException e) {
            System.out.println("Повторная передача сдохла!");
            e.printStackTrace();
        } finally {
            output.close();
            Server.tokens.remove(socket.getInetAddress()); //тупо удаляем токен, т.е. есть только 1 попытка повторной передачи файла

        }
    }

    private void upload(String name) throws IOException {
        File file = new File("C:\\Users\\Skiba Iryna\\Desktop\\files\\" + name);
        long length = Long.parseLong(bufferedReader.readLine());

        token.setName(name);
        token.setSize(length);
        token.setPosition(0);

        transferFlag = true;

        Server.tokens.put(socket.getInetAddress(), token);

        FileOutputStream output = new FileOutputStream(file);
        try {
            int i = 1000;
            for (int j = 0; j < length / 1000; j++) {
                while (i != 0) {
                    output.write(in.read());
                    i--;

                    token.incrementPosition();
                }
                output.flush();
                i = 1000;
            }

            long k = length % 1000;
            while (k != 0) {
                output.write(in.read());
                k--;

                token.incrementPosition();
            }
            output.flush();

            Server.tokens.remove(socket.getInetAddress()); //если передача успешная, то удаляем токен

            transferFlag = false;

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