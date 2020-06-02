package controller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;
import controller.URL_Data;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.events.Namespace;
import controller.cacheFormer;
import com.google.protobuf.TextFormat.ParseException;

import java.util.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.*;

@SuppressWarnings("serial")
@WebServlet("/queryProcessor")
public class queryProcessor extends HttpServlet implements Runnable {

    String searchQuery;
    String Country ;
    String SearchType;
    String trends ;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub


        searchQuery = (request.getParameter("query"));
        Country = (request.getParameter("country")); //string format ( ['country name','logo'] )
        SearchType = (request.getParameter("searchType")); // options sent (Text,Image)
        trends = (request.getParameter("Trends")); // options sent (Text,Image)
        boolean ifImage=SearchType.contains("Image");

        System.out.println(Country);
        String code = Country.split(",")[1];
        code = code.substring(1,code.length()-2);

        
    if(trends.contains("Trends"))
    {
      ArrayList<String> names = null ;
      String nameList="<h2>Top Trending</h2><br>";
      String message=FormHTMLPage();

      try {
        names =getTrends(code);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if(names!=null)
      {
        for(int i=0;i<=names.size();i++)
        {
          nameList=nameList + i + "- " + names.get(i) + " <br>";
        }
        message=message+nameList+"</body></html> ";

      }
      else
      {
        message=message+"There is no names to be viewed <br> </body></html>";
      }
      response.getWriter().println(message);
      return;
    }

    (new Thread(new queryProcessor())).start();

    boolean isPhrase = false;
      Character firstChar,lastChar;
      // Pre-Processing the search query

      firstChar = searchQuery.charAt(0);
      lastChar =  searchQuery.charAt(searchQuery.length()-1);
      if (firstChar.equals("\"")&&lastChar.equals("\"")) isPhrase = true;
      searchQuery.replace("\"", "");
      String [] words = searchQuery.split("\\s+");


      //jdbc_demo.wordStopper ws= new jdbc_demo.wordStopper();
      controller.Stemmer st = new controller.Stemmer();

      //open connection with DB
      try {
    Driver.DB.make_connection();
  } catch (SQLException e1) {
    // TODO Auto-generated catch block
    e1.printStackTrace();
  }
      List<ResultSet> toRanker = new ArrayList<>();
      //loop at each word
      wordStopper.Stopwords();
      if(!isPhrase) {
          for (int i = 0; i < words.length; ++i) {
              if (!wordStopper.isStopword(words[i])) {
                  String ret = st.stem_input_word(words[i]);
                  String get_query = "SELECT * FROM `indexer` WHERE `word` = '" + ret + "';";
                  ResultSet ret_query = null;
        try {
          ret_query = Driver.DB.execute_select_query(get_query);
        } catch (SQLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
                //  ret_query.first();
                  toRanker.add(ret_query);
              }
          }
      }
      else {
          String phrase = "", firstWord = "", stemmed = "";
          boolean gotFirstWord = false;
          for (int i = 0; i < words.length; ++i) {
              if (!wordStopper.isStopword(words[i])) {
                  stemmed = st.stem_input_word(words[i]);
                  phrase += stemmed;
                  if (!gotFirstWord) {
                      firstWord = words[i];
                      gotFirstWord = true;
                  }
              }
          }
          String phrase_query = "SELECT * FROM `indexer` WHERE `word` = '" + firstWord + "';";
          ResultSet phrase_res = null;
    try {
      phrase_res = Driver.DB.execute_select_query(phrase_query);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
          try {
      if (!phrase_res.next()) {                        // No Results
            System.out.println("No records found");    //==> to be modified to resemble the way data returns from the ranker
        } else {
            do {
                String phraseURL = phrase_res.getString("url");
                String getURLContent = "SELECT * FROM `phrase_searching` WHERE `url` = '" + phraseURL + "' AND `document` like '%" + phrase + "%';";
                ResultSet urlContents = Driver.DB.execute_select_query(getURLContent);
                if (!urlContents.next())
                    phrase_res.deleteRow();
                // Get data from the current row and use it
            } while (phrase_res.next());
            phrase_res.first();
            toRanker.add(phrase_res);
        }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
      }
     // jdbc_demo.ranker rankerObj= new jdbc_demo.ranker();
      try {
         List<String> urlBag = ranker.Ranker(toRanker, isPhrase,ifImage,code,"");
         cacheFormer.MakeCache(urlBag);         
         getServletConfig().getServletContext().getRequestDispatcher("/SearchResults.jsp?page=1?pagenum="+urlBag.size()).forward(request,response);
         
      } catch (IOException e) {
          e.printStackTrace();
      } catch (SQLException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  } 
   
    }

    public void run() {
        System.out.println("Hello from a thread!");
        ArrayList<String> namee = GetNames(searchQuery);
        try {
            modifyTrends(namee,Country);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void modifyTrends(ArrayList<String> namee, String count) throws Exception{
        String currentName;
        Driver.DB.make_connection();

        for(int i=0;i<namee.size();i++)
        {
            currentName=namee.get(i);
            try{
                String query="SELECT `Name` FROM `TRENDS` WHERE `Name`=\""+currentName+"\" AND `Country`=\""+count+"\";";
                ResultSet result = Driver.DB.execute_select_query(query);
                if(!result.next())
                {
                    post(currentName,count);
                }
                else
                {
                    modifyRank(currentName,count);
                }
            } catch(Exception e){System.out.println(e);}
            finally {
                System.out.println("Insert Completed.");
            }
        }

    }
    //
    public static void modifyRank(String namee, String country) throws Exception{

        try{
            String Query ="SELECT `Rank` FROM `TRENDS` WHERE `Country`=\""+country+"\" AND `Name`=\""+namee+"\";";
            ResultSet result = Driver.DB.execute_select_query(Query);
            String theRank = result.getString("Rank");
            int up_Rank = Integer.parseInt(theRank);
            up_Rank++;
            Query= "UPDATE `TRENDS` SET `Rank`="+up_Rank+" WHERE `Country`=\""+country+"\" AND `Name`=\""+namee+"\";";
            Driver.DB.execute_update_quere(Query);

        } catch(Exception e){System.out.println(e);}
        finally {
            System.out.println("update Completed.");
        }
    }
    //
    public ArrayList<String> GetNames(String SearchText) {
        Annotation document =
                new Annotation(SearchText);
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,entitymentions");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(document);
        String test="PERSON";
        String test2;
        String test3;
        ArrayList<String> NamesList = new ArrayList<String>();

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
                test2=entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
                if(test2 != null && test2.contains(test)) {
                    test3= entityMention.toString();
                    NamesList.add(test3);
                }

            }
        }
      /*
          for(int i =0;i<NamesList.size();i++)
          {
            System.out.println(NamesList.get(i));
          }*/

        return NamesList;
    }
  
    public static Connection getConnection() throws Exception{
        try{
            String driver = "com.mysql.jdbc.Driver";
            String url = "http://e9f121e6.ngrok.io/?fbclid=IwAR1cT0DY1Qtv_MNSW7BcC4zAsObGNbyC45EGo6-VnWz44HmgxLr7WBVrcbM";
            String username = "pmauser";
            String password = "pmauser";
            Class.forName(driver);
            Connection conn = DriverManager.getConnection(url,username,password);
            System.out.println("Connected");
            return conn;
        } catch(Exception e){System.out.println(e);}


        return null;
    }
    //
    public void post(String name, String country) throws Exception{
        int rank =0 ;
        try{
            String query="INSERT INTO `TRENDS` (`Name`, `Rank`, `Country`) VALUES (\""+name+"\", "+rank + ", \""+country+"\");";
            Driver.DB.execute_insert_quere(query);
        } catch(Exception e){System.out.println(e);}
        finally {
            System.out.println("Insert Completed.");
        }
    }

    public static ArrayList<String> getTrends(String count ) throws Exception{
        try{
          Driver.DB.make_connection();
          String get_query ="SELECT `Name` FROM `TRENDS` WHERE `Country`=\""+count+"\" ORDER BY `Rank` DESC LIMIT 10;";
            ResultSet result = Driver.DB.execute_select_query(get_query);
            ArrayList<String> namesList = new ArrayList<String>();
            while(result.next()){
                // System.out.println(result.getString("Name"));
                namesList.add(result.getString("Name"));
            }
            System.out.println("names have been returned!");
            return namesList;

        }catch(Exception e){System.out.println(e);}
        return null;
    }

    public  String FormHTMLPage( ) {
        String message="<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "<head>\r\n" +
                "<meta charset=\"ISO-8859-1\">\r\n" +
                "<title>Search Engine</title>\r\n" +
                "\r\n" +
                "<style>\r\n" +
                "* {\r\n" +
                "  box-sizing: border-box;\r\n" +
                "}\r\n" +
                "\r\n" +
                "\r\n" +
                "#myInput {\r\n" +
                "  background-image: url('/css/searchicon.png');\r\n" +
                "  background-position: 10px 12px;\r\n" +
                "  background-repeat: no-repeat;\r\n" +
                "  width: 70%;\r\n" +
                "  font-size: 16px;\r\n" +
                "  padding: 12px 20px 12px 40px;\r\n" +
                "  border: 1px solid #ddd;\r\n" +
                "  margin-bottom: 12px;\r\n" +
                "  border-radius: 15px;\r\n" +
                "  border: 1px #000 solid;\r\n" +
                "\r\n" +
                "  \r\n" +
                "}\r\n" +
                "\r\n" +
                "#Country{\r\n" +
                "  background:#E9E8E8;\r\n" +
                "  \r\n" +
                "}\r\n" +
                "#myUL {\r\n" +
                "  list-style-type: none;\r\n" +
                "  padding: 0;\r\n" +
                "  margin: 0;\r\n" +
                "}\r\n" +
                "\r\n" +
                "#myUL li a {\r\n" +
                "  border: 1px solid #ddd;\r\n" +
                "  margin-top: -1px; /* Prevent double borders */\r\n" +
                "  background-color: #f6f6f6;\r\n" +
                "  padding: 12px;\r\n" +
                "  text-decoration: none;\r\n" +
                "  font-size: 18px;\r\n" +
                "  color: black;\r\n" +
                "  display: block\r\n" +
                "}\r\n" +
                "\r\n" +
                "#myUL li a:hover:not(.header) {\r\n" +
                "  background-color: #eee;\r\n" +
                "}\r\n" +
                "</style>\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "\r\n" +
                "<form action=\"<%=request.getContextPath()%>/queryProcessor\" method=\"post\" id=\"HomePage\">\r\n" +
                "\r\n" +
                "<h2>Find All What You Want</h2>\r\n" +
                "\r\n" +
                "<input type=\"text\" id=\"myInput\" name=\"query\" onkeyup=\"myFunction()\" placeholder=\"Search for ..\" title=\"Search\" required>\r\n" +
                "<br>\r\n" +
                "<select id=\"Country\" name=\"country\" required >\r\n" +
                "    <option value=\"\">-- sellect the Country --</option>\r\n" +
                " <option value=\"['Nigeria','NG']\">Nigeria</option>\r\n" +
                " <option value=\"['Afghanistan','AF']\">Afghanistan</option>\r\n" +
                " <option value=\"['Albania','AL']\">Albania</option>\r\n" +
                " <option value=\"['Algeria','DZ']\">Algeria</option>\r\n" +
                " <option value=\"['American Samoa','AS']\">American Samoa</option>\r\n" +
                " <option value=\"['Andorra','AD']\">Andorra</option>\r\n" +
                " <option value=\"['Angola','AO']\">Angola</option>\r\n" +
                " <option value=\"['Anguilla','AI']\">Anguilla</option>\r\n" +
                " <option value=\"['Antarctica','AQ']\">Antarctica</option>\r\n" +
                " <option value=\"['Antigua and Barbuda','AG']\">Antigua and Barbuda</option>\r\n" +
                " <option value=\"['Argentina','AR']\">Argentina</option>\r\n" +
                " <option value=\"['Armenia','AM']\">Armenia</option>\r\n" +
                " <option value=\"['Aruba','AW']\">Aruba</option>\r\n" +
                " <option value=\"['Australia','AU']\">Australia</option>\r\n" +
                " <option value=\"['Austria','AT']\">Austria</option>\r\n" +
                " <option value=\"['Azerbaijan','AZ']\">Azerbaijan</option>\r\n" +
                " <option value=\"['Bahamas','BS']\">Bahamas</option>\r\n" +
                " <option value=\"['Bahrain','BH']\">Bahrain</option>\r\n" +
                " <option value=\"['Bangladesh','BD']\">Bangladesh</option>\r\n" +
                " <option value=\"['Barbados','BB']\">Barbados</option>\r\n" +
                " <option value=\"['Belarus','BY']\">Belarus</option>\r\n" +
                " <option value=\"['Belgium','BE']\">Belgium</option>\r\n" +
                " <option value=\"['Belize','BZ']\">Belize</option>\r\n" +
                " <option value=\"['Benin','BJ']\">Benin</option>\r\n" +
                " <option value=\"['Bermuda','BM']\">Bermuda</option>\r\n" +
                " <option value=\"['Bhutan','BT']\">Bhutan</option>\r\n" +
                " \r\n" +
                " <option value=\"['Burkina Faso','BF']\">Burkina Faso</option>\r\n" +
                " <option value=\"['Burundi','BI']\">Burundi</option>\r\n" +
                " <option value=\"['Cambodia','KH']\">Cambodia</option>\r\n" +
                " <option value=\"['Cameroon','CM']\">Cameroon</option>\r\n" +
                " <option value=\"['Canada','CA']\">Canada</option>\r\n" +
                " <option value=\"['Cape Verde','CV']\">Cape Verde</option>\r\n" +
                " <option value=\"['Cayman Islands','KY']\">Cayman Islands</option>\r\n" +
                " <option value=\"['Central African Republic','CF']\">Central African Republic</option>\r\n" +
                " <option value=\"['Chad','TD']\">Chad</option>\r\n" +
                " <option value=\"['Chile','CL']\">Chile</option>\r\n" +
                " <option value=\"['China','CN']\">China</option>\r\n" +
                " <option value=\"['Christmas Island','CX']\">Christmas Island</option>\r\n" +
                " <option value=\"['Cocos (Keeling) Islands','CC']\">Cocos (Keeling) Islands</option>\r\n" +
                " <option value=\"['Colombia','CO']\">Colombia</option>\r\n" +
                " <option value=\"['Comoros','KM']\">Comoros</option>\r\n" +
                " <option value=\"['Congo','CG']\">Congo</option>\r\n" +
                " <option value=\"['Congo, the Democratic Republic of the','CD']\">\"Congo, the Democratic Republic of the\"</option>\r\n" +
                " <option value=\"['Cook Islands','CK']\">Cook Islands</option>\r\n" +
                " <option value=\"['Costa Rica','CR']\">Costa Rica</option>\r\n" +
                " <option value=\"['Croatia','HR']\">Croatia</option>\r\n" +
                " <option value=\"['Cuba','CU']\">Cuba</option>\r\n" +
                " <option value=\"['Cyprus','CY']\">Cyprus</option>\r\n" +
                " <option value=\"['Czech Republic','CZ']\">Czech Republic</option>\r\n" +
                " <option value=\"['Denmark','DK']\">Denmark</option>\r\n" +
                " <option value=\"['Djibouti','DJ']\">Djibouti</option>\r\n" +
                " <option value=\"['Dominica','DM']\">Dominica</option>\r\n" +
                " <option value=\"['Dominican Republic','DO']\">Dominican Republic</option>\r\n" +
                " <option value=\"['Ecuador','EC']\">Ecuador</option>\r\n" +
                " <option value=\"['Egypt','EG']\">Egypt</option>\r\n" +
                " <option value=\"['El Salvador','SV']\">El Salvador</option>\r\n" +
                " <option value=\"['Equatorial Guinea','GQ']\">Equatorial Guinea</option>\r\n" +
                " <option value=\"['Eritrea','ER']\">Eritrea</option>\r\n" +
                " <option value=\"['Estonia','EE']\">Estonia</option>\r\n" +
                " <option value=\"['Ethiopia','ET']\">Ethiopia</option>\r\n" +
                " <option value=\"['Fiji','FJ']\">Fiji</option>\r\n" +
                " <option value=\"['Finland','FI']\">Finland</option>\r\n" +
                " <option value=\"['France','FR']\">France</option>\r\n" +
                " <option value=\"['French Guiana','GF']\">French Guiana</option>\r\n" +
                " <option value=\"['French Polynesia','PF']\">French Polynesia</option>\r\n" +
                " <option value=\"['French Southern Territories','TF']\">French Southern Territories</option>\r\n" +
                " <option value=\"['Gabon','GA']\">Gabon</option>\r\n" +
                " <option value=\"['Gambia','GM']\">Gambia</option>\r\n" +
                " <option value=\"['Georgia','GE']\">Georgia</option>\r\n" +
                " <option value=\"['Germany','DE']\">Germany</option>\r\n" +
                " <option value=\"['Ghana','GH']\">Ghana</option>\r\n" +
                " <option value=\"['Gibraltar','GI']\">Gibraltar</option>\r\n" +
                " <option value=\"['Greece','GR']\">Greece</option>\r\n" +
                " <option value=\"['Greenland','GL']\">Greenland</option>\r\n" +
                " <option value=\"['Grenada','GD']\">Grenada</option>\r\n" +
                " <option value=\"['Guadeloupe','GP']\">Guadeloupe</option>\r\n" +
                " <option value=\"['Guam','GU']\">Guam</option>\r\n" +
                " <option value=\"['Guatemala','GT']\">Guatemala</option>\r\n" +
                " <option value=\"['Guernsey','GG']\">Guernsey</option>\r\n" +
                " <option value=\"['Guinea','GN']\">Guinea</option>\r\n" +
                " <option value=\"['Guinea-Bissau','GW']\">Guinea-Bissau</option>\r\n" +
                " <option value=\"['Guyana','GY']\">Guyana</option>\r\n" +
                " <option value=\"['Haiti','HT']\">Haiti</option>\r\n" +
                " <option value=\"['Hungary','HU']\">Hungary</option>\r\n" +
                " <option value=\"['Iceland','IS']\">Iceland</option>\r\n" +
                " <option value=\"['India','IN']\">India</option>\r\n" +
                " <option value=\"['Indonesia','ID']\">Indonesia</option>\r\n" +
                " <option value=\"['Iran, Islamic Republic of','IR']\">\"Iran, Islamic Republic of\"</option>\r\n" +
                " <option value=\"['Iraq','IQ']\">Iraq</option>\r\n" +
                " <option value=\"['Ireland','IE']\">Ireland</option>\r\n" +
                " <option value=\"['Isle of Man','IM']\">Isle of Man</option>\r\n" +
                " <option value=\"['Israel','IL']\">Israel</option>\r\n" +
                " <option value=\"['Italy','IT']\">Italy</option>\r\n" +
                " <option value=\"['Jamaica','JM']\">Jamaica</option>\r\n" +
                " <option value=\"['Japan','JP']\">Japan</option>\r\n" +
                " <option value=\"['Jersey','JE']\">Jersey</option>\r\n" +
                " <option value=\"['Jordan','JO']\">Jordan</option>\r\n" +
                " <option value=\"['Kazakhstan','KZ']\">Kazakhstan</option>\r\n" +
                " <option value=\"['Kenya','KE']\">Kenya</option>\r\n" +
                " <option value=\"['Kiribati','KI']\">Kiribati</option>\r\n" +
                " <option value=\"['Korea, Democratic People's Republic of','KP']\">\"Korea, Democratic People's Republic of\"</option>\r\n" +
                " <option value=\"['Korea, Republic of','KR']\">\"Korea, Republic of\"</option>\r\n" +
                " <option value=\"['Kuwait','KW']\">Kuwait</option>\r\n" +
                " <option value=\"[Kyrgyzstan','KG']\">yrgyzstan</option>\r\n" +
                " <option value=\"['Lao People`s Democratic Republic','LA']\">Lao People's Democratic Republic</option>\r\n" +
                " <option value=\"['Latvia','LV']\">Latvia</option>\r\n" +
                " <option value=\"['Lebanon','LB']\">Lebanon</option>\r\n" +
                " <option value=\"['Lesotho','LS']\">Lesotho</option>\r\n" +
                " <option value=\"['Liberia','LR']\">Liberia</option>\r\n" +
                " <option value=\"['Libya','LY']\">Libya</option>\r\n" +
                " <option value=\"['Liechtenstein','LI']\">Liechtenstein</option>\r\n" +
                " <option value=\"['Lithuania','LT']\">Lithuania</option>\r\n" +
                " <option value=\"['Luxembourg','LU']\">Luxembourg</option>\r\n" +
                " <option value=\"['Macao','MO']\">Macao</option>\r\n" +
                " <option value=\"['Malawi','MW']\">Malawi</option>\r\n" +
                " <option value=\"['Malaysia','MY']\">Malaysia</option>\r\n" +
                " <option value=\"['Maldives','MV']\">Maldives</option>\r\n" +
                " <option value=\"['Mali','ML']\">Mali</option>\r\n" +
                " <option value=\"['Malta','MT']\">Malta</option>\r\n" +
                " <option value=\"['Marshall Islands','MH']\">Marshall Islands</option>\r\n" +
                " <option value=\"['Martinique','MQ']\">Martinique</option>\r\n" +
                " <option value=\"['Mauritania','MR']\">Mauritania</option>\r\n" +
                " <option value=\"['Mauritius','MU']\">Mauritius</option>\r\n" +
                " <option value=\"['Mayotte','YT']\">Mayotte</option>\r\n" +
                " <option value=\"['Mexico','MX']\">Mexico</option>\r\n" +
                " <option value=\"['Mongolia','MN']\">Mongolia</option>\r\n" +
                " <option value=\"['Montenegro','ME']\">Montenegro</option>\r\n" +
                " <option value=\"['Montserrat','MS']\">Montserrat</option>\r\n" +
                " <option value=\"['Morocco','MA']\">Morocco</option>\r\n" +
                " <option value=\"['Mozambique','MZ']\">Mozambique</option>\r\n" +
                " <option value=\"['Myanmar','MM']\">Myanmar</option>\r\n" +
                " <option value=\"['Namibia','NA']\">Namibia</option>\r\n" +
                " <option value=\"['Nauru','NR']\">Nauru</option>\r\n" +
                " <option value=\"['Nepal','NP']\">Nepal</option>\r\n" +
                " <option value=\"['Netherlands','NL']\">Netherlands</option>\r\n" +
                " <option value=\"['New Caledonia','NC']\">New Caledonia</option>\r\n" +
                " <option value=\"['New Zealand','NZ']\">New Zealand</option>\r\n" +
                " <option value=\"['Nicaragua','NI']\">Nicaragua</option>\r\n" +
                " <option value=\"['Niger','NE']\">Niger</option>\r\n" +
                " <option value=\"['Niue','NU']\">Niue</option>\r\n" +
                " <option value=\"['Oman','OM']\">Oman</option>\r\n" +
                " <option value=\"['Pakistan','PK']\">Pakistan</option>\r\n" +
                " <option value=\"['Palau','PW']\">Palau</option>\r\n" +
                " <option value=\"['Palestine, State of','PS']\">\"Palestine, State of\"</option>\r\n" +
                " <option value=\"['Panama','PA']\">Panama</option>\r\n" +
                " <option value=\"['Papua New Guinea','PG']\">Papua New Guinea</option>\r\n" +
                " <option value=\"['Paraguay','PY']\">Paraguay</option>\r\n" +
                " <option value=\"['Peru','PE']\">Peru</option>\r\n" +
                " <option value=\"['Philippines','PH']\">Philippines</option>\r\n" +
                " <option value=\"['Pitcairn','PN']\">Pitcairn</option>\r\n" +
                " <option value=\"['Poland','PL']\">Poland</option>\r\n" +
                " <option value=\"['Portugal','PT']\">Portugal</option>\r\n" +
                " <option value=\"['Puerto Rico','PR']\">Puerto Rico</option>\r\n" +
                " <option value=\"['Qatar','QA']\">Qatar</option>\r\n" +
                " <option value=\"['Romania','RO']\">Romania</option>\r\n" +
                " <option value=\"['Russian Federation','RU']\">Russian Federation</option>\r\n" +
                " <option value=\"['Rwanda','RW']\">Rwanda</option>\r\n" +
                " <option value=\"['Saint Lucia','LC']\">Saint Lucia</option>\r\n" +
                " <option value=\"['Samoa','WS']\">Samoa</option>\r\n" +
                " <option value=\"['San Marino','SM']\">San Marino</option>\r\n" +
                " <option value=\"['Sao Tome and Principe','ST']\">Sao Tome and Principe</option>\r\n" +
                " <option value=\"['Saudi Arabia','SA']\">Saudi Arabia</option>\r\n" +
                " <option value=\"['Senegal','SN']\">Senegal</option>\r\n" +
                " <option value=\"['Serbia','RS']\">Serbia</option>\r\n" +
                " <option value=\"['Seychelles','SC']\">Seychelles</option>\r\n" +
                " <option value=\"['Sierra Leone','SL']\">Sierra Leone</option>\r\n" +
                " <option value=\"['Singapore','SG']\">Singapore</option>\r\n" +
                " <option value=\"['Sint Maarten (Dutch part)','SX']\">Sint Maarten (Dutch part)</option>\r\n" +
                " <option value=\"['Slovakia','SK']\">Slovakia</option>\r\n" +
                " <option value=\"['Slovenia','SI']\">Slovenia</option>\r\n" +
                " <option value=\"['Solomon Islands','SB']\">Solomon Islands</option>\r\n" +
                " <option value=\"['Somalia','SO']\">Somalia</option>\r\n" +
                " <option value=\"['South Africa','ZA']\">South Africa</option>\r\n" +
                " <option value=\"['South Sudan','SS']\">South Sudan</option>\r\n" +
                " <option value=\"['Spain','ES']\">Spain</option>\r\n" +
                " <option value=\"['Sri Lanka','LK']\">Sri Lanka</option>\r\n" +
                " <option value=\"['Sudan','SD']\">Sudan</option>\r\n" +
                " <option value=\"['Suriname','SR']\">Suriname</option>\r\n" +
                " <option value=\"['Swaziland','SZ']\">Swaziland</option>\r\n" +
                " <option value=\"['Sweden','SE']\">Sweden</option>\r\n" +
                " <option value=\"['Switzerland','CH']\">Switzerland</option>\r\n" +
                " <option value=\"['Syrian Arab Republic','SY']\">Syrian Arab Republic</option>\r\n" +
                " <option value=\"['Taiwan, Province of China','TW']\">\"Taiwan, Province of China\"</option>\r\n" +
                " <option value=\"['Tajikistan','TJ']\">Tajikistan</option>\r\n" +
                " <option value=\"['Tanzania, United Republic of','TZ']\">\"Tanzania, United Republic of\"</option>\r\n" +
                " <option value=\"['Thailand','TH']\">Thailand</option>\r\n" +
                " <option value=\"['Timor-Leste','TL']\">Timor-Leste</option>\r\n" +
                " <option value=\"['Togo','TG']\">Togo</option>\r\n" +
                " <option value=\"['Tokelau','TK']\">Tokelau</option>\r\n" +
                " <option value=\"['Tonga','TO']\">Tonga</option>\r\n" +
                " <option value=\"['Trinidad and Tobago','TT']\">Trinidad and Tobago</option>\r\n" +
                " <option value=\"['Tunisia','TN']\">Tunisia</option>\r\n" +
                " <option value=\"['Turkey','TR']\">Turkey</option>\r\n" +
                " <option value=\"['Turkmenistan','TM']\">Turkmenistan</option>\r\n" +
                " <option value=\"['Turks and Caicos Islands','TC']\">Turks and Caicos Islands</option>\r\n" +
                " <option value=\"['Tuvalu','TV']\">Tuvalu</option>\r\n" +
                " <option value=\"['Uganda','UG']\">Uganda</option>\r\n" +
                " <option value=\"['Ukraine','UA']\">Ukraine</option>\r\n" +
                " <option value=\"['United Arab Emirates','AE']\">United Arab Emirates</option>\r\n" +
                " <option value=\"['United Kingdom','GB']\">United Kingdom</option>\r\n" +
                " <option value=\"['United States','US']\">United States</option>\r\n" +
                " <option value=\"['United States Minor Outlying Islands','UM']\">United States Minor Outlying Islands</option>\r\n" +
                " <option value=\"['Uruguay','UY']\">Uruguay</option>\r\n" +
                " <option value=\"['Uzbekistan','UZ']\">Uzbekistan</option>\r\n" +
                " <option value=\"['Vanuatu','VU']\">Vanuatu</option>\r\n" +
                " <option value=\"['Venezuela, Bolivarian Republic of','VE']\">\"Venezuela, Bolivarian Republic of\"</option>\r\n" +
                " <option value=\"['Viet Nam','VN']\">Viet Nam</option>\r\n" +
                " <option value=\"['Virgin Islands, British','VG']\">\"Virgin Islands, British\"</option>\r\n" +
                " <option value=\"['Virgin Islands, U.S.','VI']\">\"Virgin Islands, U.S.\"</option>\r\n" +
                " <option value=\"['Wallis and Futuna','WF']\">Wallis and Futuna</option>\r\n" +
                " <option value=\"['Western Sahara','EH']\">Western Sahara</option>\r\n" +
                " <option value=\"['Yemen','YE']\">Yemen</option>\r\n" +
                " <option value=\"['Zambia','ZM']\">Zambia</option>\r\n" +
                " <option value=\"['Zimbabwe','ZW']\">Zimbabwe</option>\r\n" +
                "</select> \r\n" +
                "<br>\r\n" +
                " <input type=\"radio\" name=\"searchType\" value=\"Text\"  checked=\"checked\"/> Text\r\n" +
                " <input type=\"radio\" name=\"searchType\" value=\"Image\" /> Image\r\n" +
                " <input type=\"checkbox\" name=\"Trends\" value=\"Trends\" /> Trends\r\n" +
                "\r\n" +
                " <br>\r\n" +
                " <input type=\"submit\" value=\"search\"  /><br><br>";
        return message;
    }
   
    public queryProcessor() {
    }
      
    
    
}
