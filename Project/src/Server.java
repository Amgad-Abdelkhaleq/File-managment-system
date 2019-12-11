
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.*;
import javax.xml.transform.Result;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nada
 */
public class Server {
    
    /**
     * @param args the command line arguments
     */
    static int random_id = 0;
    static ArrayList id_list = new ArrayList();
    static ArrayList email_list = new ArrayList();
    static ArrayList password_list = new ArrayList();
    static ArrayList directories_list = new ArrayList();
    //static String original_path2 = FileUtility.pwd();
    //static String original_path = original_path2.substring(2);
     //static String original_path = System.getProperty("user.dir").substring(2);
    
    
    public static void main(String[] args) throws ClassNotFoundException,SQLException{
        // TODO code application logic here
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
         String ConnectionURL="jdbc:sqlserver://localhost;databaseName=db;user=admin;password=123";
        Connection con=DriverManager.getConnection(ConnectionURL);
        Statement st=con.createStatement();
        try
        {
            //1.open server socket
            ServerSocket sv = new ServerSocket(1234);
            System.out.println("Server Running...");
            while (true)
            {
                //2.accept connection
                Socket s = sv.accept();
                System.out.println("Client Accepted...");
                //3. open thread for this client (s)
				// we don't want the server to handle this client, we want a thread to handle it
				//ClientHandler is the runnable name 
                ClientHandler ch = new ClientHandler(s,st);
                Thread t = new Thread(ch); // objet from a thread to run the runnable
                t.start();
				// OR do the following
				// Thread t = new Thread(new ClientHandler());
				// t.start();

            }

            //6.close server
            //sv.close();
        } catch (IOException ex)
        {
            System.out.println(ex.getMessage());
        }
        
    }
}
