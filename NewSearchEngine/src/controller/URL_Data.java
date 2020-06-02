package controller;

import java.sql.ResultSet;
import java.sql.SQLException;

public class URL_Data {

	private static String URL;  
	private static String title;  
	private static String partOfContent;  
	
	public URL_Data(String url,String data,String content){  
		URL=url;  
		title =data;  
		partOfContent=content;  
		
    } 
    public static URL_Data getRecords(String url){    
    	URL_Data urlObj=null;				
    	try {
    		Driver.DB.make_connection();
			String getURLContent = "SELECT `glance` FROM `phrase_searching` WHERE url = \"" + url + "\";";
            ResultSet urlContents = Driver.DB.execute_select_query(getURLContent);
            
            if (!urlContents.next()) {
                System.out.println("No record found");
            }
            else{
                String glance = urlContents.getString("glance");
                String [] data = glance.split("|");
                String title = data[0];
                String partOfContent = data[1];
       
                urlObj=new URL_Data(url,title,partOfContent);  
               
            }  		
	        } catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
		return urlObj;  
    } 
    
    public static String getURL(){  
        
        return URL;  
    } 
    
    public static String getTitle(){  
        return title;  
    } 
    
    public static String getpartOfContent(){  
        return partOfContent;  
    } 
    
}
