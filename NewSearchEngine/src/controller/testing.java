package controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

public class testing {
	public static void main(String[] args) throws SQLException, IOException {
		// TODO Auto-generated method stub
		
		/*
		 * read from file into vector of strings
		 */
		Vector<String>vec=new Vector<String>();
		
		Map< String,Integer> hm =  new HashMap< String,Integer>();
		Vector<String>header=new Vector<String>();
		String URL=null;
		String document="";
		//-------------------------------------------------------------------------------------------
		
		Driver.DB.make_connection();
//		// get the files
		for(int q=1;q<2001;++q) {
			vec.clear();
			header.clear();
			document="";
			URL="";
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

		                }
		                else if(i==2) {
		                	String[] header_split = data[i].split("\\s+");
		                	for (String a : header_split) { 
		                		header.add(a);
		                	}
		                }
		                else {
		                	int ss=0;
		                	String[] content_split = data[i].split("\\s+");
		                	for (String a : content_split) {
		                		String aa="";
		                		for(int r=0;r<a.length();++r) {
//		                			System.out.println(a);
			                		if(a.charAt(r) == '\"' || a.charAt(r)== '\'' || a.charAt(r)=='\\')
			                			continue;
			                		aa+=a.charAt(r);
		                		}
		                		if(ss<header.size())
		                			ss++;
		                		else
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
			
		    
		    for(int i=0;i<header.size();++i) {
				  document+=header.get(i)+" ";
			   }
			document+='|';
			int mn=0;
			if(vec.size()<50) {
				mn=vec.size();
			}
			else {
				mn=50;
			}
			for(int i=0;i<mn;++i) {
					document+=vec.get(i)+" ";
		   }
		   
			System.out.println(document);
			String update_query = "update `phrase_searching` set `glance` = \""+document+"\" where `id` = "+q+" ;" ;
//			System.out.println(update_query);
			Driver.DB.execute_update_quere(update_query);
	   
		} // looping on all files end
		
	}
}
