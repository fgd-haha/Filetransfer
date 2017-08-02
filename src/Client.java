import java.io.*;
import java.net.Socket;
import java.util.Base64;

class Client {
    private static final String SERVER = "49.140.87.155";
    private static final int PORT = 8888;
    private static final int TIMEOUT = 15000;
    private static final String FILEPATH = "D:\\Code\\Python\\StudyNotes\\mysql.md";


    static void client() {
        Socket socket;
        try{
            socket = new Socket(SERVER, PORT);
            socket.setSoTimeout(TIMEOUT);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String AESKey = AES.getSecretKey();
            String RSAPublicKey = in.readUTF();
            String AESKeyEncoded = Base64.getEncoder().encodeToString(RSA.encrypt(Base64.getDecoder().decode(AESKey),RSAPublicKey));

            out.writeUTF(AESKeyEncoded);
            out.flush();

            sendFile(out,AESKey);
            socket.close();
        }catch (Exception server){
            System.out.println("Connect  Failed  !");
        }
    }


    private static void sendFile(DataOutputStream out, String AESKey) throws IOException
    {
        try
        {
            File file = new File(FILEPATH);
            DataInputStream fileIn = new DataInputStream(new BufferedInputStream(new FileInputStream(FILEPATH)));
            System.out.println("FileName:  " + file.getName());
            out.writeUTF(file.getName());
            out.flush();
            System.out.println("FileSize:  " + file.length()/1024 + " KB");
            out.writeLong(file.length());
            out.flush();

            long transfered = 0;
            int transferedRate = 0;
            while (true)
            {
                byte[] buf = new byte[4096];
                int read = fileIn.read(buf);
                out.writeInt(read);
                transfered += read;
                if(read == -1)
                    break;
                if ((int) (transfered * 100 / file.length()) > transferedRate) {
                    transferedRate = (int) (transfered * 100 / file.length());
                    System.out.println(transferedRate + "%");
                }
                try {
                    buf = AES.encrypt(buf, AESKey);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                String str = Base64.getEncoder().encodeToString(buf);
                out.writeUTF(str);
                out.flush();
            }
            System.out.println("Have   Transfered  !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}