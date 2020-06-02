package controller;

import controller.Driver;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class Crawler {
    private float frequency;
    private float threshold;

    public Crawler(int seed){
        this.frequency = 50;
        this.threshold = 5000;
    }

    public static void main(String[] args) throws InterruptedException, IOException {
            //Set<String> x =getImages(parsePage("https://www.javatpoint.com/java-tutorial"));
        //int seed = Integer.parseInt(args[1]); // to be read as an argument
        int seed = 10;
        seed = Math.min(seed,12);
        Set<String> visitedSet = new HashSet<>();
        Set<String> syncVisitedSet = Collections.synchronizedSet(visitedSet);
        List<String>  urlQueue = new ArrayList<>();
        FileWriter myUrls = new FileWriter ("myUrls.txt",true);
        FileWriter indexer = new FileWriter("indexer.txt",true);
        //if (myUrls.exists() || indexer.exists()) {
        int num =0;
        BufferedReader Reader = null;
        try {
            String row;
            Reader = new BufferedReader(new FileReader("myUrls.txt"));
            while ((row = Reader.readLine()) != null){
                String[] data = row.split("--");
                syncVisitedSet.add(data[0]);
                urlQueue.add(data[1]);
                num ++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //}
        CsvWriter dB = new CsvWriter(myUrls, indexer, urlQueue, num);
        final String[] initialSeed = {"https://en.wikipedia.org","https://www.Espn.com","https://www.nationalgeographic.com","https://www.quora.com","https://www.answers.com","https://Bleacherreport.com","https://www.theguardian.com","https://www.stackoverflow.com","https://www.javatpoint.com","https://www.bbc.com"};
        ArrayList<Thread> crawlerAssistants = new ArrayList<>();
        for (int i=0; i< seed; i++){
            crawlerAssistants.add(new Thread(new CrawlingThread(dB,initialSeed[i], syncVisitedSet)));
            crawlerAssistants.get(i).setName(((Integer)i).toString());
            crawlerAssistants.get(i).start();
        }
        for (int i=0; i< seed; i++)
            crawlerAssistants.get(i).join();
        System.out.println("5000 pages reached");
    }

    public static class CsvWriter {
        private PrintWriter urlWriter, indexerWriter;
        private StringBuilder urlSb, indexerSb;
        private int count;
        private BufferedReader csvReader ;
        private List<String> urlStore;
        private Random rand = new Random();

        public CsvWriter(FileWriter urlFile, FileWriter indexerFile, List<String> mainQueue, int count) {
            urlWriter = new PrintWriter(urlFile);
            indexerWriter = new PrintWriter(indexerFile);
            //csvReader = new BufferedReader(new FileReader(urlFilePath));
            urlStore = mainQueue;
            this.count = count;
        }
        public int getCount(){
            return count;
        }
        public List<String> getStore(){ return urlStore; }
        public int next() {int s = getStore().size(); if (s>0) return ThreadLocalRandom.current().nextInt(s-1); else return -1;}
        public void incrementCount(){
            count ++;
        }
        //public BufferedReader getCsvReader(){return  csvReader;}
        public void writeData(String row, boolean url, String base) {
            if(url) {
                urlSb = new StringBuilder();
                urlSb.append(base+"--"+row);
                urlSb.append("\r\n");
                urlWriter.write(urlSb.toString());
                urlWriter.flush();
                urlStore.add(row);
            }
            else{
                indexerSb = new StringBuilder();
                indexerSb.append(base+"--"+row);
                indexerSb.append("\r\n");
                //System.out.println(count);
                indexerWriter.write(indexerSb.toString());
                indexerWriter.flush();
            }
            //System.out.println("done!");
        }
    }
}
