import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.jamesfrost.robotsio.RobotsDisallowedException;
import me.jamesfrost.robotsio.RobotsParser;

public class CrawlingThread implements Runnable {
    private Crawler.CsvWriter writer;
    Set<String> visitedSet;
    private boolean firstCrawl;
    private String firstSeed;

    public CrawlingThread(Crawler.CsvWriter writer, String firstSeed, Set<String> visitedSet) {
        this.writer = writer;
        firstCrawl = true;
        this.firstSeed = firstSeed;
        this.visitedSet = visitedSet;
    }

    @Override
    public void run() {
        doWork();
    }

    public void doWork() {
        int currentCount = 0;
        while (true) {
            String url = "";
            if (firstCrawl) {
                url = firstSeed;
                firstCrawl = false;
            } else {
                int next = writer.next();
                if (next == -1 || next == writer.getStore().size()) continue;
                synchronized (writer) {
                    //currentCount = writer.getCount();
                    //writer.incrementCount();
                    if (writer.getCount() <= 5000) {
                        if ((url = writer.getStore().get(next)) != null) {
                            while (visitedSet.contains(url) && (url != null)) {
                                url = writer.getStore().get(next);
                                writer.getStore().remove(next);
                            }
                            //write = true;
                        }
                    } else {
                        try {
                            writer.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    visitedSet.add(url);
                }
            }
            if(((Character)url.charAt(url.length()-1)).equals("/")) url = url.substring(0,url.length()-1);
            String html = parsePage(url);
            String data = getPageDetails(html);
            String details = url + "----" + data;
            Set<String> urls = getUrls(html, url);
            Set<String> images = getImages(html,url);
            String stripped = Jsoup.clean(html, Whitelist.none());
            synchronized (writer) {
                //writer.writeData(details,false);
                if (!stripped.isEmpty()){
                    currentCount = writer.getCount();
                    writer.incrementCount();
                }
                Iterator<String> itr = images.iterator();
                while (itr.hasNext())
                    writer.writeData(itr.next(), false, url);
                System.out.println(details + " Thread Name " + Thread.currentThread().getName() + " "+currentCount);
                itr = urls.iterator();
                while (itr.hasNext())
                    writer.writeData(itr.next(), true, url);
            }
            String id = ((Integer) currentCount).toString();
            String bold = getPageHeaders(url);
            PrintWriter contentWriter = null, boldWriter = null;
            String current = id + ".txt";
            try {
                contentWriter = new PrintWriter("content" + "/" + current, "UTF-8");
                boldWriter = new PrintWriter("bold" + "/" + current, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            contentWriter.println(details+stripped);
            boldWriter.println(bold);
            contentWriter.close();
            boldWriter.close();
        }
    }

    public String parsePage(String urlToVisit) {
        String html = "";
        RobotsParser robotsParser = new RobotsParser();

        try {
            if (!robotsParser.isAllowed(urlToVisit)) return html;
            URL url = new URL(urlToVisit);
            URLConnection urlcon = url.openConnection();
            InputStream stream = urlcon.getInputStream();
            int i;
            while ((i = stream.read()) != -1) {
                html += ((char) i);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return html;
    }

    public String getPageDetails(String html) {
        Pattern p1 = Pattern.compile("<title>(.+?)</title>");
        Matcher title = p1.matcher(html);
        Pattern p2 = Pattern.compile("datetime=(.+?) ");
        Matcher date = p2.matcher(html);
        String details = "";
        if (date.find()) {
            details += date.group(1);
            details = details.substring(0, 11) + "----";
        } else
            details += "0000-00-00----";
        if (title.find())
            details += title.group(1)+"----";
        else
            details += "No Title----";
        return details;
    }

    public String getPageHeaders(String html) {
        //Pattern p2 = Pattern.compile("<h2>(.+?)</h2>");
        //Matcher bold = p2.matcher(html);
        Document doc = Jsoup.parse(html);
        Elements hTags = doc.select("h1, h2, h3, h4, h5, h6");
        String headerText = hTags.text();
        //String headerText = "";
        //while (bold.find())
         //   headerText += (bold.group(1)) + ",";
        return headerText;
    }

    public Set<String> getUrls(String html, String baseURL) {
        Set<String> urls = new HashSet<>();
        Pattern p1 = Pattern.compile("href=\"(.*?)\"");
        Matcher links = p1.matcher(html);
        //int limit = 0;
        while (links.find()){ //&& limit < 20
            String url = links.group(1);
            //System.out.println("working");
            if (url.startsWith("http") && !url.startsWith("https"))
                url.replaceFirst("http","https");
            else if (url.startsWith("//"))
                url = "https:" + url;
            else if (url.startsWith("/"))
                url = baseURL + url;
            else if (!url.startsWith("https"))
                url = baseURL + '/' +url;

            urls.add(url); // this variable should contain the URL details to be stored
            //limit ++;
        }
        return urls;
    }

    public Set<String> getImages(String html, String baseURL) {
        Set<String> imageURLS = new HashSet<>();
        Pattern p1 = Pattern.compile("<img(.*?)/>");
        Matcher images = p1.matcher(html);
        Pattern p2 = Pattern.compile("src=\"(.*?)\"");
        Pattern p3 = Pattern.compile("alt=\"(.*?)\"");
        while (images.find()) {
            String img = images.group(1), toRet;
            Matcher src = p2.matcher(img);
            Matcher alt = p3.matcher(img);
            if (src.find()) {
                if(!src.group(1).startsWith("https"))
                    if (src.group(1).startsWith("//upload.wikimedia.org"))
                        toRet = "https:"+src.group(1);
                    else
                        toRet = baseURL+src.group(1);
                else
                    toRet = src.group(1);
                if (alt.find()) {
                    //toRet = alt.group(1) + ", " + toRet;
                    imageURLS.add(toRet);
//                    try {
//                        URL url = new URL(toRet.split(",")[1]);
//                        URLConnection conn = url.openConnection();
//                        conn.connect();
//                        imageURLS.add(toRet);
//                        //System.out.println(toRet);
//                    } catch (MalformedURLException e) {
//                        continue;
//                        // the URL is not in a valid form
//                    } catch (IOException e) {
//                        continue;
//                        // the connection couldn't be established
//                    }
                }
            }
        }
        return imageURLS;
    }
}