import java.nio.file.Path; 
import java.nio.file.Paths; 
import java.net.URI; 
/////////////// server imports
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import javax.xml.transform.Result;

////////////////////

class ClientHandler implements Runnable
{

    Socket s;
    Statement st;
    private static final ThreadLocal<String> CWD=new ThreadLocal<String>();
    public static  String  original_path;
                        
                 
             

    public ClientHandler(Socket s,Statement st)
    {
        this.s = s;
        this.st=st;
       
    }
    

 public void run()
    {
        //String original_path_2 = ClientHandler.pwd();
        //String original_path = original_path_2.substring(2);
       // System.out.print(Server.original_path);
            
        try 
        {
            //3.create I/O streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            int session_id;
            while(true)
            {

            //a. request account number
                dos.writeUTF("Welcome, sign in or sign up [1,2]?");
                dos.flush();
                String choice = dis.readUTF();
                
                //---------------------------------sign in ------------------------------
                if(choice.equals("1"))
                {
                    String email;
                    ResultSet rs;
                    do
                    {
                         dos.writeUTF("Enter your Email: ");
                         dos.flush();
                         email = dis.readUTF();
                         rs=st.executeQuery("select * from clients where Email='"+email+"'");
                    }
                    while(rs.next()==false);
                    
                    
                  
                    String password;
                    int fetch_password=0;
                    int id=0;
                    int hashed_pass=0;
                    do
                    {
                         //boolean val = Is_Duplicate(email);
                        dos.writeUTF("Enter your password: ");
                        dos.flush();
                        password = dis.readUTF();
                        rs=st.executeQuery("select ID,Password from clients where Email='"+email+"'");
                        while(rs.next())
                        {
                            id=rs.getInt("ID");
                            fetch_password=rs.getInt("Password");
                        }
                       hashed_pass=hash_fun(password,id); 

                    }
                    
                    while(fetch_password!=(hashed_pass));

                   
                   CWD.set(System.getProperty("user.dir"));
                   ClientHandler.cd("root"+id+"\\home");
                                            
                }
                //---------------------------------sign up------------------------------
             if(choice.equals("2"))
                {
                    
                    dos.writeUTF("Enter your Email: ");
                    dos.flush();
                    String email = dis.readUTF();
                    //boolean val = Is_Duplicate(email);
                    ResultSet rs=st.executeQuery("select * from clients where Email='"+email+"'");
                    while(rs.next()!=false)
                    {
                        dos.writeUTF("Duplicate email, Enter another one: ");
                        dos.flush();
                        email = dis.readUTF();
                    }
                  st.executeUpdate("insert into clients (Email) values ('"+email+"')");

                    dos.writeUTF("Enter your password: ");
                    dos.flush();
                    String password = dis.readUTF();
                    rs=st.executeQuery("select ID from clients where Email='"+email+"'");
                    int id=0;
                     while(rs.next())
                     {
                         id=rs.getInt("ID");
                     }
                     int new_pass=hash_fun(password,id);
                     st.executeUpdate("update clients SET Password="+new_pass+" where Email='"+email+"'");

                    /*Server.email_list.add(email);
                    Server.password_list.add(password);
                    Server.id_list.add(Server.random_id++);*/
                   // int id=Server.random_id++;
                    //int index2 = Server.email_list.indexOf(email);
                    session_id = Server.random_id;

                    //ClientHandler.cd(ClientHandler.CWD);
                     
                    CWD.set(System.getProperty("user.dir"));
                    String my_path=ClientHandler.createDirectory("root"+ id);
                    ClientHandler.cd("root" + id); 
                    ClientHandler.createDirectory("home");
                     ClientHandler.cd("home"); 
                    ClientHandler.original_path=System.getProperty("user.dir")+"\\"+"root"+id;
                    st.executeUpdate("update clients SET home_path='"+ClientHandler.original_path+"' where Email='"+email+"'");
                }

                //String str1 = "";
                String str2 = "";
                String command="";
                boolean log_flag=true;
                boolean first_time=true;
                String str4;
            if(true) //logged in flag instead of true 
            {
                do
                {
                    if(first_time){
                 dos.writeUTF(
                   "cat: Create file\n"
                  +"rn: Rename file\n"
                  +"rm: Delete file\n"
                  +"mkdir: Create directory\n"
                  +"rmdir: Delete directory\n"
                  +"ls: View Files in Directory\n"
                  +"cp: Copy File\n"
                  +"mv: move file \n"
                  +"cd: change dir\n"
                  +"pwd: show current working directory\n"
                  +"download: download file\n"
                  +"upload: upload file\n"
                  +"logout\n"
                  +"__________________________________________\n"
                  +"Please enter a command:\n");
                    dos.flush();
                    first_time=false;
                    }
                        command = dis.readUTF();

                   switch (command)
                  {
                       case "download":
                              dos.writeUTF("download_flag");
                              dos.flush();
                              str2 = dis.readUTF();  //recieve file name from client 
                              FileInputStream fr=new FileInputStream(str2); //server sends from its current working directory
                              byte b[]=new byte[20002];
                              fr.read(b,0,b.length);
                              OutputStream os=s.getOutputStream();
                              os.write(b,0,b.length);  
                              dos.writeUTF("successful download");
                              dos.flush();
                           break;
                       case "upload":
                            dos.writeUTF("upload_flag");
                              dos.flush();
                           String fileName=dis.readUTF();
                           InputStream is=s.getInputStream();
                           byte B[]=new byte[20002];
                           FileOutputStream f=new FileOutputStream(fileName);
                           is.read(B,0,B.length);
                           f.write(B,0,B.length);  
                           dos.writeUTF("successful upload");
                              dos.flush();
                           break;
                 case "cat": // create new file 
                    dos.writeUTF("Enter the file name you want to create the file \n");
                    dos.flush();
                    str2 = dis.readUTF();

                    if (str2.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();
                    }
                    else
                    {
                      dos.writeUTF(ClientHandler.createFile(str2));
                      dos.flush();
                    }

                    break;

                  case "rn": 
                    dos.writeUTF("Enter the name of the file you want to rename \n");
                    dos.flush();
                    str2 = dis.readUTF();
                    if (str2.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();
                    }
                    else
                    {
                      dos.writeUTF("Enter the new name of the file\n");
                      dos.flush();
                      str4 = dis.readUTF();
                      dos.writeUTF(ClientHandler.reNameFile(str2, str4));
                      dos.flush();

                    }
                    break;

                  case "rm": 
                    dos.writeUTF("Enter the name of the file which you want to delete\n ");
                    dos.flush();
                    str2 = dis.readUTF();
                    if (str2.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();
                    }
                    else
                    {
                       dos.writeUTF(ClientHandler.deleteFile(str2));
                      dos.flush();
                    }
                    break;

                   case "mkdir": 
                     dos.writeUTF("Enter the directory you want to create");
                     dos.flush();
                     str4 = dis.readUTF();
                     if (str4.isEmpty())
                     {
                       dos.writeUTF("\nInvalid input");
                       dos.flush();
                     }
                     else
                     {
                       dos.writeUTF(ClientHandler.createDirectory(str4));
                       dos.flush();
                     }
                     break;

                   case "rmdir": 
                     dos.writeUTF("Enter the directory you want to delete \n");
                     dos.flush();
                     str4 = dis.readUTF();
                     if (str4.isEmpty())
                     {
                       dos.writeUTF("\nInvalid input\n");
                       dos.flush();

                     }
                     else
                     {
                      dos.writeUTF(ClientHandler.deleteDirectory(str4));
                      dos.flush();

                     }
                     break;

                  case "ls": 
                      dos.writeUTF(ClientHandler.ls());
                      dos.flush();
                     break;

                  case "cp": 
                    dos.writeUTF("Enter the file name to copy  with extension\n");
                    dos.flush();
                    str2 = dis.readUTF();
                    dos.writeUTF("Enter dir path\n");
                    dos.flush();
                    str4 = dis.readUTF();
                    dos.writeUTF("Enter copy name  with extension\n");
                    dos.flush();
                    String cp_name = dis.readUTF();
                    if (str2.isEmpty() || str4.isEmpty()|| cp_name.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();

                    }
                    else
                    {
                      dos.writeUTF(ClientHandler.copyFile(str2,cp_name,str4));
                      dos.flush(); 
                    }
                    break;

                  case "mv": 
                    dos.writeUTF("Enter the file you want to move with extension\n ");
                    dos.flush();
                    str2 = dis.readUTF();
                    dos.writeUTF("Enter destination dir path \n");
                    dos.flush();
                    str4 = dis.readUTF();
                    if (str2.isEmpty() || str4.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();

                    }
                    else
                    {
                      dos.writeUTF(ClientHandler.moveFile(str2,str4));
                      dos.flush();
                    }

                    break;

                  case "cd": 
                    dos.writeUTF("Enter local dir-name or path \n");
                    dos.flush();
                    str2 = dis.readUTF();
                    int error = 0;
                    
                    
                    if (str2.isEmpty())
                    {
                      dos.writeUTF("\nInvalid input\n");
                      dos.flush();
                    }
                    else
                    {
                        String msg = ClientHandler.cd(str2);
                        if(msg.equals("Outside your root directory"))
                        {
                            dos.writeUTF("Outside your root directory");
                            dos.flush();
                        }
                        else
                        {
                            
                            String path = ClientHandler.pwd();
                            int size = path.length();
                            int index = path.indexOf("home");
                            dos.writeUTF("your CWD: " + path.substring(index - 1)  + "\n");
                            dos.flush();
                        }
                    }
                    break;

                    case "pwd": 
                        String path = ClientHandler.pwd();
                        int size = path.length();
                        int index = path.indexOf("home");
                        dos.writeUTF(path.substring(index - 1)  + "\n");
                        dos.flush();
                     break;

                     case "logout":
                        log_flag=false;
                      break; 

                   default: 
                     dos.writeUTF("\nInvalid Choice\n ");
                     dos.flush();
                   }
                 } while(log_flag); // end of do while 
            }
            if(log_flag == false)
            {
               // System.out.print(Server.original_path);
                dos.writeUTF("bye");
                dos.flush();
                //cd(Server.original_path);
                break;
            }
        }
            //5.close connection
            dis.close();
            dos.close();
            s.close();
        } // end of try
        catch(Exception e)
        {
          System.out.println(e.getMessage());
        } // end of catch
    }  // end of void run
    
    
      
 public static int hash_fun(String password,int id)
{
    int sum=0;
    for(int i=0;i<password.length();i++)
    {
        int c=password.charAt(i);
        sum+=c;
        
    }
    sum+=id;
    return sum;
}   
final static String fileSeparator = System.getProperty("file.separator");
  //this.CWD= System.getProperty("user.dir");
  public static String pwd() {
   // return Paths.get(".").toAbsolutePath().normalize().toString(); 
   return CWD.get();
  }
  
//  public void download (String local_file_to_send_from_server) throws Exception{
// 
//  FileInputStream fr=new FileInputStream(local_file_to_send_from_server); //server sends from its current working directory
//  byte b[]=new byte[20002];
//  fr.read(b,0,b.length);
//  OutputStream os=s.getOutputStream();
//  os.write(b,0,b.length);
//  //os.close();
//  //s.shutdownOutput(); 
//  }

  public static String  createDirectory(String dir_name)
  {
    String message;
    File localFile = new File( pwd()+fileSeparator+dir_name);
    if (localFile.exists())
    {
      message="\nDirectory already exists\n";
    }
    else
    {
      localFile.mkdirs();
      message="\nDirectory created successfully\n";
    }
    return message;
  }

 public static String deleteDirectory(String dir_name)
  {
    String message="";
    File d= new File (pwd()+fileSeparator+dir_name);
    if (d.isDirectory())
    {
      if(delete_dir_recursivly(d)) message="\n"+ dir_name+" successfully deleted"+"\n";
      else {message="\nThere is some issue deleting your dir\n";}
//        FileUtils.deleteDirectory(d);
    }
    else
    {
      message+="\nInvalid Directory\n";
    }
    return message;
  }
  
  static public boolean delete_dir_recursivly(File path) {
    if (path.exists()) {
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                delete_dir_recursivly(files[i]);
            } else {
                files[i].delete();
            }
        }
    }
    return (path.delete());
}

  public static String createFile(String file_name){
    String message="";
   try { 
          String absoluteFilePath = pwd()+fileSeparator+file_name;
          message="absolute File Path is :"+absoluteFilePath+"\n";
          File localFile = new File(absoluteFilePath);     // locate the file 
            if (localFile.createNewFile())  //Create new file and check if it does not exist 
            message+="File created\n"; 
            else
            message+="File already exists\n"; 
        } 
        catch (Exception e) { 
            System.err.println(e); 
        } 
return message;
      }

      public static String reNameFile(String old_file_name_with_extention, String new_fname_with_extension)
      {
        String message="";
        File localFile1 = new File(pwd()+fileSeparator+old_file_name_with_extention);
        
        if (localFile1.isFile()) //check if it is file and exist in CWD
        {
          String str = localFile1.getParent();
          File localFile2 = new File(str + "\\" + new_fname_with_extension);
          if (localFile1.renameTo(localFile2))
          {
            message="\n File successfully renamed\n";
          }
          else
          {
            message="\nThere is some issue renaming your file\n";
          }
        }
        else
        {
          message="\nFile not found\n";
        }
        return message;
      }

      public static String deleteFile(String file_to_remove_with_extension)
      {
        String message="";
        File localFile = new File(pwd()+fileSeparator+file_to_remove_with_extension);
        
        if (localFile.isFile()) //check if it is file and exist in CWD
        {
          if (localFile.delete())
          {
            message="\nFile successfully deleted\n";
          }
          else
          {
            message="\nThere is some issue deleting the file\n";
          }
          
        }
        else {
          message="\nFile not found\n";
        }
        return message;
      }

      public static String copyFile(String file_to_copy_with_extension,String copy_name_with_extension,String dir_subpath_to_save_at) //hena user hab3t subpath mesh abslute le2no malsh access 3alla root 
      throws IOException
    {
       String message=""; 
     //  Path sub_path = Paths.get(dir_subpath_to_save_at);
     //  URI uri = sub_path.toUri();    // call toUri() to convert subpath in URI (abslute path)
    //   message="URI: "+ uri; // print URI to test it
      String abs_path=ClientHandler.original_path+dir_subpath_to_save_at;
      File localFile1 = new File(pwd()+fileSeparator+file_to_copy_with_extension);
      if (localFile1.isFile())
      {
        File localFile2 = new File(abs_path+fileSeparator+copy_name_with_extension);
        //File localFile2 = new File(uri, file_to_copy_with_extension);
        if(localFile2.createNewFile())  //check if copy name is duplicate 
        {
        FileInputStream fis= new FileInputStream(localFile1);
        FileOutputStream fos= new FileOutputStream(localFile2);
        try
        {
          int i;
          while  ((i=fis.read()) != -1)
          {
            fos.write(i);
          }
          fis.close(); 
          fos.close();
        }
        catch (Exception localException)
        {
          System.out.println(localException);
        }
        message+="\nFile copied successfully\n";
      }
      else {message+="file name is already exists";} //in case eno 3mal two copies f nafs ll dir w bnfs ll copy-name hytl3 error message
    }
      else
      {
        message+="\nInvalid File\n";
      }
      return message;
    }

  public static String moveFile(String file_to_mov_with_extension,String dir_subpath_to_move_at) //hena user hab3t subpath mesh abslute le2no malsh access 3alla root 
    throws IOException
    {
  String x=copyFile(file_to_mov_with_extension,file_to_mov_with_extension,dir_subpath_to_move_at)
  +deleteFile(file_to_mov_with_extension);
      return "\nFile moved successfully\n";
    }

    public static String ls()
    {
      String message="";
     File current_dir = new File(pwd());
     try {
      File[] arrayOfFiles = current_dir.listFiles();
      message="\n" + current_dir.getName()+"\n";
      if (arrayOfFiles != null)
      {
        for (File localFile : arrayOfFiles)
        {
          message+="\t->" + localFile.getName()+"\n";
        }
        message+="\n";
      }
      else
      {
        message+="\nEmpty dir\n";
      }
     } catch (Exception e) {
      System.err.println(e.getMessage()); 
     } 
     return message;    
    }
    
    
    public static String cd(String dirName_or_path)
    {
        String message="";
        Path p= Paths.get(dirName_or_path);
        if(dirName_or_path.equals(".."))  //case path 
        {
            String path = pwd();
            System.out.println(path);
            int i = 0;
            int size = path.length() - 1;
            System.out.println(size);
            while(!"\\".equals(String.valueOf(path.charAt(size - i))))
            {
                i++;
            }

            if(path.indexOf("home") != path.length() - 4)
            {
                String substring = path.substring(0, size - i);
                System.out.println(substring);

                File f = new File(substring); 
                if(f.exists())
                {
                    CWD.set(f.getAbsolutePath());
                }
                else
                {
                    message="dir doesn't exist";
                }
            }
            else
            {
                message = "Outside your root directory";
            }
            

         //System.setProperty("user.dir", ClientHandler.CWD );
         //ClientHandler.CWD=dirName_or_path;
        }
        else if(p.startsWith("/"))  //case path 
        {
          File f = new File(ClientHandler.original_path+dirName_or_path); 
          if(f.exists()){ 
           CWD.set(f.getAbsolutePath());
          }
          else{
              message="dir doesn't exist";
          }
          
         //System.setProperty("user.dir", ClientHandler.CWD );
         //ClientHandler.CWD=dirName_or_path;
        }
         
        else     //case dir-name 
        {
            File f = new File(pwd()+fileSeparator+dirName_or_path);
            if(f.exists())
            {
                //check if dir is inside the CWD
                // System.setProperty("user.dir", (pwd()+fileSeparator+dirName_or_path));  //change CWD
                CWD.set(CWD.get()+fileSeparator+dirName_or_path);
            }
            else
            {
                message="dir doesn't exist";
            }
        }
        return message;
    }

    }