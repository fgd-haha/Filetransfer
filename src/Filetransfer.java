import java.util.Scanner;

public class Filetransfer
{
    public static void main(String[] args) throws Exception {
        System.out.println("Print 1 to open Server and 2 to open Client :");
        Scanner a = new Scanner(System.in);
        try{
        int b = a.nextInt();
        while(true) {
            if (b ==1) {
                Server.server();
                break;
            }
            else if (b == 2){
                Client.client();
                break;
            }
            else {
                System.out.println("Error, Please print again:");
                b = a.nextInt();
            }
        }
        }catch (Exception e){
            System.out.println("Connect error !");
        }
    }
}
