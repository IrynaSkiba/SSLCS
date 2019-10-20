package client;

import java.io.*;
import java.net.Socket;

//upload C:\Users\Skiba Iryna\Desktop\check.txt
//upload C:\Users\Skiba Iryna\Desktop\с.jpg
public class Client {
    private Socket socket;
    public static final int PORT = 4004;
    private InputStream in;
    private OutputStream out;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private BufferedReader consoleReader;

    public Client() throws IOException {
        socket = new Socket("127.0.0.1", PORT);
        in = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(in);
        bufferedReader = new BufferedReader(inputStreamReader);
        out = socket.getOutputStream();
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
        bufferedWriter = new BufferedWriter(outputStreamWriter);

        consoleReader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void work() {
        try {
            getStartInformation();
        } catch (IOException e) {
            System.out.println("у тебя проблемы на клиенте!!");
            e.printStackTrace();
        }

        try {
            try {
                System.out.println("Вы что-то хотели сказать? Введите это здесь:");
                String consoleWord, serverAnswer, command;

                //цикл выполнения команд
                while (true) {
                    consoleWord = consoleReader.readLine();
                    command = getCommand(consoleWord);

                    //если это команда загрузки на сервер
                    if (isUploadCommand(command)) {
                        uploadCommand(consoleWord);
                        command = null; //вроде не нужно
                        continue;
                    }

                    //отправляем строку серверу
                    bufferedWriter.write(consoleWord + " \n");
                    bufferedWriter.flush();

                    if (consoleWord.equals("close")) {
                        break;
                    }

                    serverAnswer = bufferedReader.readLine();
                    System.out.println(serverAnswer);
                }
            } finally {
                System.out.println("Клиент был закрыт...");
                socket.close();
                in.close();
                out.close();
                consoleReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getStartInformation() throws IOException {
        DataInputStream dis = new DataInputStream(in);
        boolean flag = dis.readBoolean();
        long size = dis.readLong();
        long pos = dis.readLong();
        String name = bufferedReader.readLine();
        if (flag) continueUpload(size, pos, name);
    }

    private void continueUpload(long size, long position, String name) throws IOException {
        File file = new File("C:\\Users\\Skiba Iryna\\Desktop\\" + name);

        if (file.exists() && file.isFile()) {
            FileInputStream input = new FileInputStream(file);
            try {
                //пропускаем, то что уже записано, но надо избавиться от этой херни
                for (int j = 0; j < position; j++) {
                    input.read();
                }

                int i = 1000;
                for (int j = 0; j < size / 1000; j++) {
                    while (i != 0) {
                        out.write(input.read());
                        i--;
                    }
                    out.flush();
                    i = 1000;
                }

                //докачаваем остатки
                long f = size % 1000;
                while (f != 0) {
                    out.write(input.read());
                    f--;
                }
                out.flush();
            } finally {
                //закрываем файл
                input.close();
            }
        } else {
            System.out.println("File isn't exist!");
        }
    }

    private String getCommand(String consoleWord) {
        String command = null;
        if (consoleWord.contains(" ")) {
            command = consoleWord.substring(0, consoleWord.indexOf(" "));
        }
        return command;
    }

    private boolean isUploadCommand(String command) {
        return (command != null && command.equals("upload"));
    }

    private void uploadCommand(String consoleWord) throws IOException {
        File file = new File(consoleWord.substring(consoleWord.indexOf(" ") + 1));

        if (file.exists() && file.isFile()) {
            //отправляем команду и имя файла
            bufferedWriter.write(getCommand(consoleWord) + " " + file.getName() + "\n");
            FileInputStream input = new FileInputStream(file);
            try {
                //отправляем размер файла
                bufferedWriter.write(file.length() + "\n");
                bufferedWriter.flush();

                int i = 1000;
                for (int j = 0; j < file.length() / 1000; j++) {
                    while (i != 0) {
                        out.write(input.read());
                        i--;
                    }
                    out.flush();
                    i = 1000;
                }

                //докачаваем остатки
                long f = file.length() % 1000;
                while (f != 0) {
                    out.write(input.read());
                    f--;
                }
                out.flush();
            } finally {
                //закрываем файл
                input.close();
            }
        } else {
            System.out.println("File isn't exist!");
        }


    }
}