/**
 * Created on 2016/12/10.
 */
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;


class Server {
    private static final int PORT = 8888;
    private static final String savePath = "./";

    static void server() throws Exception {
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT);
            System.out.println("Server begin!");

            while (true) {
                System.out.println("listening:  ");
                Socket socket = server.accept();

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                RSA Rsa = new RSA();
                String RSAPublicKey = Rsa.getPublicKey();

                out.writeUTF(RSAPublicKey);
                out.flush();

                String AESKeyEncoded = in.readUTF();
                byte[] AESKeyBytes = RSA.decrypt(Base64.getDecoder().decode(AESKeyEncoded), Rsa.getPrivateKey());
                String AESKey = Base64.getEncoder().encodeToString(AESKeyBytes);

                receiveFile(in, AESKey);
                System.out.println("\n\n\n");
            }
        } catch (IOException ignored) {
            System.out.println("server closed");
        }
    }


    private static void receiveFile(DataInputStream in, String Key) throws Exception {
        System.out.println("Start  receiving:  ");
        String fileName = in.readUTF();
        long len = 0;
        System.out.println("FileName:  " + fileName);
        len = in.readLong();
        System.out.println("FileSize:  " + len/1024 + " KB");
        DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath + fileName)));

        long received = 0;
        int receivedRate = 0;
        while(true) {
            int read = in.readInt();
            received += read;
            if (read == -1) {
                break;
            }
            if ((int) (received * 100 / len) > receivedRate) {
                receivedRate = (int) (received * 100 / len);
                System.out.println(receivedRate + "%");
            }
            String str = in.readUTF();
            byte[] buf = AES.decrypt(Base64.getDecoder().decode(str), Key);
            fileOut.write(buf, 0, read);
        }
        fileOut.close();
        System.out.println("Have Received , Saved as:   " + savePath + fileName);
    }
}

