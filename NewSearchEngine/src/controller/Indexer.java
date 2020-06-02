package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import controller.Driver;

public class Indexer {
	
	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
		/*
		 * read from file into vector of strings
		 */
		Vector<String>vec=new Vector<String>();
		
		Map< String,Integer> hm =  new HashMap< String,Integer>();
		Vector<String>header=new Vector<String>();
		Vector<String>images_URLs=new Vector<String>();
		String URL=null;
		String date=null;
		wordStopper ws= new wordStopper();
		Stemmer st = new Stemmer();
		Driver dr= new Driver();
		String document="";
		//-------------------------------------------------------------------------------------------
		
		Driver.DB.make_connection();
//		// get the files
		for(int q=1;q<5006;++q) {
			hm.clear();
			vec.clear();
			header.clear();
			document="";
			URL="";
			date="";
			images_URLs.clear();
			System.out.println(q);
			
			BufferedReader Reader = null;
		    try {
		        String row;
		        Reader = new BufferedReader(new FileReader("/home/ahmed/education/3rd year/APT/project/from outside/abd/secondrun/content/"+String.valueOf(q)+".txt"));
		        if ((row = Reader.readLine()) != null){
		            String[] data = row.split("----");
		            for (int i=0;i<data.length;++i) 
		                if(i==0) {
		                	//URL=data[i];
		                	for(int r=0;r<data[i].length();++r) {
		                		if(data[i].charAt(r) == '\"' || data[i].charAt(r)== '\'' || data[i].charAt(r)=='\\')
		                			continue;
		                		URL+=data[i].charAt(r);
		                	}
		                }
		                else if(i==1) {
		                	if(data[i].contains("0000-00-00") || data[i].contains("0000.00.00"))
		                		date="0000-01-01";
		                	else {
		                		for(int r=0;r<data[i].length();++r) {
			                		if(data[i].charAt(r) == '\"' || data[i].charAt(r)== '\'' || data[i].charAt(r)=='\\')
			                			continue;
			                		date+=data[i].charAt(r);
		                		//date=data[i];
		                	    }
		                     }
		                }
		                else if(i==2) {
		                	String[] header_split = data[i].split("\\s+");
		                	for (String a : header_split) { 
		                		header.add(a);
		                	}
		                }
		                else {
		                	String[] content_split = data[i].split("\\s+");
		                	for (String a : content_split) {
		                		String aa="";
		                		for(int r=0;r<a.length();++r) {
//		                			System.out.println(a);
			                		if(a.charAt(r) == '\"' || a.charAt(r)== '\'' || a.charAt(r)=='\\')
			                			continue;
			                		aa+=a.charAt(r);
		                		}
//		                		System.out.println(aa);
		                		vec.add(aa);
		                	}
		                }
		        }
		    } catch (FileNotFoundException e1) {
		        e1.printStackTrace();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
			
		    //-----------------------------------------------------------------------------------------------------
			
			
			
			//open connection with DB
			
			wordStopper.Stopwords();
			//loop at each word
			for(int i=0;i<vec.size();++i) {
				if(!wordStopper.isStopword(vec.get(i))) {
					
					//if the word new, add it to the map. else, increment its value
					
					String ret= st.stem_input_word(vec.get(i));
					if(hm.containsKey(ret)) {
						hm.put(ret, hm.get(ret) + 1);
					}
					else {
						hm.put(ret, 1);
					}
					document+=ret;
				}
		   }
		   

		   //make stem and (not necessary stop, will take much time for nothing help) also for header that will make comparing below right
		   
		   for(int i=0;i<header.size();++i) {
			   header.set(i, st.stem_input_word(header.get(i)));
		   }
		   
		   
		   boolean is_header=false;
		   for(Entry<String, Integer> entry : hm.entrySet()) {
			   if(entry.getKey()=="")continue;
			   // is it header or not
			   for(int j=0;j<header.size();++j) {
				   if(header.contains(entry.getKey())) {
					   is_header=true;
					   break;
				   }
			   }
			   
			   // make query
			   //1- get query for the same word and URL
			   //2- if the word is already exits at the same URL, then just update it (get then put)
			   //3- if not insert it as a new record

			  /* String get_query= "select `id` from `indexer` where `word` = \""+entry.getKey()+"\" and `url` = \""+URL+"\";"; 
			   ResultSet ret_query= Driver.DB.execute_select_query(get_query);
			   if(!ret_query.isBeforeFirst()) {*/
				   //insert
				   String insert_query= "insert into `indexer` (`word`, `url`, `header`, `freq`, `date_of_creation`) values ( \""+entry.getKey()+"\" , \""+
				   URL+"\" , "+is_header+" , "+(double)entry.getValue()/vec.size()+" , \""+date+"\" );";
				   Driver.DB.execute_insert_quere(insert_query);
			  /* }
			   else {
				   ret_query.next();
				   String update_query = "update `indexer` set `word`= \""+entry.getKey()+"\", `url` = \""+URL+"\", `header` = "+is_header+","
				   		+ " `freq` = "+(double)entry.getValue()/vec.size()+", `date_of_creation` = \""+date+"\" where `id` = "+ret_query.getString("id")+" ;" ;
				   Driver.DB.execute_update_quere(update_query);
			   }*/
			   is_header=false;
		   }
		   
		 //----------------------------------------------------------------------------------------------------------------//
			
		//phrase searching table manipulation
		// make query
		   //1- get query for the same URL
		   //2- if the URL is already exits at the same URL, then just update it (get then put)
		   //3- if not insert it as a new record
		 /*  String get_query= "select `id` from `phrase_searching` where url = \""+URL+"\";"; 
		   ResultSet ret_query= Driver.DB.execute_select_query(get_query);
		   if(!ret_query.isBeforeFirst()) {*/
			   //insert
			   String insert_query= "insert into `phrase_searching` (`url`, `document`, `date_of_creation`, `rank`) values ( \""+
			   URL+"\" , \""+document+"\" , \""+date+"\" , "+0+" );";
			   Driver.DB.execute_insert_quere(insert_query);
		  /* }
		   else {
			   //System.out.println();
			   ret_query.next();
			   String update_query = "update `phrase_searching` set ( `url` = \""+URL+"\", `document` = \""+document+"\" , `date_of_creation` = \""+date+"\" ) where `id` = "+ret_query.getString("id")+" ;" ;
			   Driver.DB.execute_update_quere(update_query);
		   }*/
	   
		} // looping on all files end
		
		//----------------------------------------------------------------------------------------------------------------//
		
	   //images_url table manipulation
	   
//	   read images urls from the file where page url is pointing to, (if the images_urls are in the same file as above then make
//			  this read above)
	   BufferedReader Reader = null;
	    try {
	        String row;
	        Reader = new BufferedReader(new FileReader("/home/ahmed/education/3rd year/APT/project/from outside/abd/secondrun/myUrls.txt"));
	        while ((row = Reader.readLine()) != null){
	            String[] data = row.split("--");
	            String insert_query= "insert into `RankTable` (`URLfrom` , `URLto` ) values (\""+data[0]+"\" , \""+data[1]+"\" );";
	            Driver.DB.execute_insert_quere(insert_query);
	        }
	
	    } catch (FileNotFoundException e1) {
	        e1.printStackTrace();
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
	// make query
	   //1- get query for the same URL and image URL
	   //2- if the image URL is already exits at the same URL, then just update it (get then put)
	   //3- if not insert it as a new record
	    String row;
	    @SuppressWarnings("resource")
		BufferedReader Reader1 = new BufferedReader(new FileReader("/home/ahmed/education/3rd year/APT/project/from outside/abd/secondrun/indexer.txt"));
        while ((row = Reader1.readLine()) != null)
	    {
        String[] data = row.split("--");
		//   String get_query1= "select `id` from `images_urls` where `page_url` = \""+data[0]+"\" and `image_url`= \""+data[1]+"\";"; 
		  // ResultSet ret_query1= Driver.DB.execute_select_query(get_query1);
		   //if(!ret_query1.isBeforeFirst()) {
			   //insert
			   String insert_query= "insert into `images_urls` (`page_url`, `image_url`) values ( \""+
			   data[0]+"\" , \""+data[1]+"\" );";
			   Driver.DB.execute_insert_quere(insert_query);
		   //}
	   }
	   
	   //-----------------------------------------------------------------------------------------------------------------//
		
	}

}

