import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Server {
    private static final int PORT = 8888;
    private static final String savePath = "./";

    public static void main(String[] args) {
        try {
            startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void startServer() throws Exception {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server begin!");
        ExecutorService pool = Executors.newFixedThreadPool(5);

        class socketThread implements Callable<Void> {
            private Socket socket;

            private socketThread(Socket socket) {
                this.socket = socket;
            }

            @Override
            public Void call() {
                try (DataInputStream in = new DataInputStream(socket.getInputStream());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                    RSA Rsa = new RSA();
                    String RSAPublicKey = Rsa.getPublicKey();

                    out.writeUTF(RSAPublicKey);
                    out.flush();

                    String AESKeyEncoded = in.readUTF();
                    byte[] AESKeyBytes = RSA.decrypt(Base64.getDecoder().decode(AESKeyEncoded), Rsa.getPrivateKey());
                    String AESKey = Base64.getEncoder().encodeToString(AESKeyBytes);

                    receiveFile(in, AESKey);
                    System.out.println("\n\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        while (true) {
            System.out.println("listening:  ");

            try {
                Socket socket = server.accept();
                Callable<Void> task = new socketThread(socket);
                pool.submit(task);
            } catch (IOException ignored) {}
        }
    }


    private static void receiveFile(DataInputStream in, String Key) throws Exception {
        System.out.println("Start  receiving:  ");
        String fileName = in.readUTF();
        long len;
        System.out.println("FileName:  " + fileName);
        len = in.readLong();
        System.out.println("FileSize:  " + len / 1024 + " KB");
        DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath + fileName)));

        long received = 0;
        int receivedRate = 0;
        while (true) {
            int read = in.readInt();
            received += read;
            if (read == -1) {
                break;
            }
            if ((int) (received * 100 / len) > receivedRate) {
                receivedRate = (int) (received * 100 / len);
                System.out.println(fileName +  "     " + receivedRate + "%");
            }
            String str = in.readUTF();
            byte[] buf = AES.decrypt(Base64.getDecoder().decode(str), Key);
            fileOut.write(buf, 0, read);
        }
        fileOut.close();
        System.out.println("Have Received , Saved as:   " + savePath + fileName);
    }
}

