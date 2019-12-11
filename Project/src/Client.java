
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nada
 */

public class Client {

    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        // TODO code application logic here
        String path_to_recive_file="";   
        String fileName="";
        Scanner sc = new Scanner(System.in);
        try
        {
            //1.create socket and connect to the server
            //with IP:127.0.0.1(localhost)
            //and with portnumber: 1234
            Socket s = new Socket("127.0.0.1", 1234);
            //2. Create I/O streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            
            //3.perform IO with server 
            while (true)
            {
                //a. receive server command & print to user 
                String srvr_msg = dis.readUTF();           
                if(srvr_msg.equals("bye"))
                {
                    System.out.println("Session ended");
                    break;
                }
                else if(srvr_msg.equals("download_flag")){
                   
                System.out.println("Enter directory full path to recive the file at");
                 path_to_recive_file= sc.next();
                System.out.println("Enter the file name you want to download\n");
                fileName = sc.next();
                 dos.writeUTF(fileName);
                 dos.flush();
                 byte[]b=new byte[20002];
                InputStream is=s.getInputStream();
                FileOutputStream fr=new FileOutputStream(path_to_recive_file+"\\"+fileName);
                is.read(b,0,b.length);
                fr.write(b,0,b.length); 
                }    
                else if(srvr_msg.equals("upload_flag")){
                System.out.println("Enter directory full path to upload the file from");
                 String upload_path = sc.next();
                 System.out.println("Enter file name");
                 String upload_file_name = sc.next();
                 dos.writeUTF(upload_file_name);
                 FileInputStream f=new FileInputStream(upload_path+"\\"+upload_file_name);
                  byte b[]=new byte[20002];
                  f.read(b,0,b.length);
                  OutputStream os=s.getOutputStream();
                  os.write(b,0,b.length);

                }
                else{        
                System.out.println(srvr_msg);
                //b. take command from usr and send to the server
                String usr_msg = sc.next();
                dos.writeUTF(usr_msg);
                dos.flush();
            }
            }
            //4.close connections
            dis.close();
            dos.close();
            s.close();
            
        } 
        catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
    }
    
}
