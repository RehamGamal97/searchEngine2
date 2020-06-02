package controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/userRanks")
public class userRanks extends HttpServlet {
  private static final long serialVersionUID = 1L;
       

    public userRanks() {
        super();
       
    }


  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //get IP Address
    String remoteAddr = "";
    System.out.println(remoteAddr);
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        
        String URL = (request.getParameter("url"));
        String trimmed = URL.substring(URL.indexOf("www.")+4,URL.lastIndexOf('.'));
        try {
      updateUser(trimmed,remoteAddr);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
        
  }

  public void updateUser(String URL ,String remoteAddr)throws Exception {  
        try{
            Driver.DB.make_connection();
              String query="SELECT `freq` FROM `user_preferables` WHERE `user`=\""+remoteAddr+"\" AND `website`=\""+URL+"\";";
              ResultSet result = Driver.DB.execute_select_query(query);
              String freq=null;
             float frequency=1/100000;
              if(result!= null)
              {
                freq=result.getString("freq");
                frequency=Float.parseFloat(freq);
                frequency=frequency+1/100000;
                
                query = "UPDATE `user_preferables` SET `freq`="+frequency+" WHERE `user`=\""+remoteAddr+"\" AND `website`=\""+URL+"\";";
                 Driver.DB.execute_update_quere(query);
              }
              else
              {
                 query = "INSERT INTO `user_preferables` (`user`, `website`, `freq`) VALUES (\""+remoteAddr+"\", \""+URL+"\", "+frequency+");";
                 Driver.DB.execute_insert_quere(query);
              }
        } catch(Exception e){System.out.println(e);}
        finally {
            System.out.println("Insert Completed.");
        }
    }
         
    
  
    public static Connection getConnection() throws Exception{
      try{
       String driver = "com.mysql.jdbc.Driver";
       String url = "http://e9f121e6.ngrok.io";
       String username = "pmauser";
       String password = "pmauser";
       Class.forName(driver);
       Connection conn = DriverManager.getConnection(url,username,password);
       System.out.println("Connected");
       return conn;
      } catch(Exception e){System.out.println(e);}
      
      
      return null;
     }

}