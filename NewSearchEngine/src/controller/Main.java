package controller;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.util.*;
import java.sql.Connection;

import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class Main {

    public static void main(String[] args) {
    	
    	CacheManager cm = CacheManager.getInstance();

		//2. Create a cache called "cache1"
		cm.addCache("cache1");

		//3. Get a cache called "cache1"
		Cache cache = cm.getCache("cache1");

		//4. Put few elements in cache
		cache.put(new Element("1","Jan"));
		cache.put(new Element("2","Feb"));
		cache.put(new Element("3","Mar"));

		//5. Get element from cache
		Element ele = cache.get("1");

		//6. Print out the element
		String output = (ele == null ? null : ele.getObjectValue().toString());
		System.out.println(output);

		//7. Is key in cache?
		System.out.println(cache.isKeyInCache("1"));
		System.out.println(cache.isKeyInCache("5"));

		//8. shut down the cache manager
		cm.shutdown();
    	
    	
    }
    
}
//    public void modifyTrends(ArrayList<String> namee, String count) throws Exception{
//    	String currentName;
//    	Connection con = getConnection();
//    	Create_Trend_Table(con);
//    	
//    	for(int i=0;i<namee.size();i++)
//    	{
//    		currentName=namee.get(i);
//    		try{
//                java.sql.PreparedStatement statement = con.prepareStatement("SELECT Name FROM TRENDS WHERE Name=currentName AND Country=count");
//                ResultSet result = statement.executeQuery();
//                if(result==null)
//                {
//                	post(con,currentName,count);
//                }
//                else
//                {
//                	modifyRank(con,currentName,count);
//                }
//            } catch(Exception e){System.out.println(e);}
//            finally {
//                System.out.println("Insert Completed.");
//            }
//    	}
//        
//    }
////
//    public static void modifyRank(Connection con,String namee, String country) throws Exception{
//    	
//        try{
//        	//Connection con = getConnection();
//            java.sql.PreparedStatement statement = con.prepareStatement("SELECT Rank FROM TRENDS WHERE Country=count AND Name=namee");
//            ResultSet result = statement.executeQuery();
//           String theRank = result.getString("Rank");
//           int up_Rank = Integer.parseInt(theRank);
//           up_Rank++;
//           java.sql.PreparedStatement statement2 = con.prepareStatement("UPDATE TRENDS SET Rank=up_Rank WHERE Country=count AND Name=namee");
//           ResultSet result2 = statement.executeQuery();
//     
//        } catch(Exception e){System.out.println(e);}
//        finally {
//            System.out.println("update Completed.");
//        }
//    }
////
//    public ArrayList<String> GetNames(String SearchText) {
//    	Annotation document =
//    	        new Annotation("John Smith visited Los Angeles on Tuesday. Emy adam left Los Angeles on Wednesday with reham");
//    			Properties props = new Properties();
//    	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,entitymentions");
//    	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//    	    pipeline.annotate(document);
//    	    String test="PERSON";
//    	    String test2;
//    	    String test3;
//    	    ArrayList<String> NamesList = new ArrayList<String>();
//    	    
//    	    for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
//    	      for (CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
//    	         test2=entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
//  	    	  if(test2 != null && test2.contains(test)) {
//  	    		test3= entityMention.toString();
//  	    		NamesList.add(test3);
//  	    	  }
//  	   
//    	      }
//    	    }
//    	/*    
//    	    for(int i =0;i<NamesList.size();i++)
//    	    {
//    	    	System.out.println(NamesList.get(i));
//    	    }*/
//    	    
//    	    return NamesList;
//    }
////    
//    public static void Create_Trend_Table(Connection con) throws Exception{
//  	  try{
//  		//Connection con = getConnection();
//  		java.sql.PreparedStatement create = con.prepareStatement("CREATE TABLE IF NOT EXISTS TRENDS(Name VARCHAR (30) NOT NULL,Rank INT NOT NULL,Country VARCHAR (20) NOT NULL,PRIMARY KEY (Name"); 
//  		create.executeUpdate();
//  	  } catch(Exception e){System.out.println(e);}
//  	  finally {System.out.println("function completed");}
//  	 }
// //   
//    public static Connection getConnection() throws Exception{
//    	  try{
//    	   String driver = "com.mysql.jdbc.Driver";
//    	   String url = "jdbc:mysql://24.196.52.166:3306/testdb";
//    	   String username = "username";
//    	   String password = "password";
//    	   Class.forName(driver);
//    	   
//    	   Connection conn = DriverManager.getConnection(url,username,password);
//    	   System.out.println("Connected");
//    	   return conn;
//    	  } catch(Exception e){System.out.println(e);}
//    	  
//    	  
//    	  return null;
//    	 }
////
//    public void post(Connection con ,String name, String country) throws Exception{
//        int rank =0 ;  
//        try{
//            //Connection con = getConnection();
//            java.sql.PreparedStatement posted = con.prepareStatement("INSERT INTO TRENDS (Name, Rank, Country) VALUES ('"+name+"', '"+rank+"', '"+country+"')");
//            posted.executeUpdate();
//        } catch(Exception e){System.out.println(e);}
//        finally {
//            System.out.println("Insert Completed.");
//        }
//    }
//
//    public static ArrayList<String> getTrends(String count ) throws Exception{
//        try{
//            Connection con = getConnection();
//            java.sql.PreparedStatement statement = con.prepareStatement("SELECT Name FROM TRENDS WHERE Country=count ORDER BY Rank DESC LIMIT 10");
//           
//            ResultSet result = statement.executeQuery();
//           
//            ArrayList<String> namesList = new ArrayList<String>();
//            while(result.next()){
//               // System.out.println(result.getString("Name"));              
//            	namesList.add(result.getString("Name"));
//            }
//            System.out.println("names have been returned!");
//            return namesList;
//           
//        }catch(Exception e){System.out.println(e);}
//        return null;
//    }
//    
    

