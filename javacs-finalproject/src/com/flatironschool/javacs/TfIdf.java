package com.flatironschool.javacs;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Chohee on 8/3/16.
 */
public class TfIdf {

    private Jedis jedis;
    private DecimalFormat f = new DecimalFormat("##.00");
    Map<String, String> idf;
    private double totalDocuments; //total number of documents that is indexed - number of keys in TermCounters

    public TfIdf(Jedis jedis) {

        this.jedis = jedis;
        idf = new HashMap<>();
        totalDocuments = jedis.keys("TermCounter:*").size();
    }

    public double getTotalDocumentSize() {
        return totalDocuments;
    }

    // going though each term in url sets and count how many urls are there

    //find idf - number of document over number of document it appears
    private void processIdf() {


        Set<String> terms = jedis.keys("URLSet:*");
       // System.out.println(terms.size());


        for(String key : terms){

            double idfScore = getTotalDocumentSize()/jedis.smembers(key).size();
            idf.put(getKeyForIdf(key) , f.format(idfScore));
           // System.out.println(getKeyForIdf(key) + " : " +  f.format(idfScore));
        }

        //System.out.println(idf.size());

    }

    private String getKeyForIdf(String key) {
        return key.substring(7);
    }


    private double getIdfOfTerm(String term) {
        return Double.parseDouble(idf.get(term)) ;
    }

    private String getTfIdfKey(String key) {
        return "TF-IDF:" + key.substring(12);
    }

    public void processTfIdf() {

        processIdf();

        Set<String> urls = jedis.keys("TermCounter:*");
        Map<String, String> map;

        for(String url : urls) {

            map = jedis.hgetAll(url);

            for(String key : map.keySet()) {

                Double totalScore = getIdfOfTerm(key) + Double.parseDouble(map.get(key));
                jedis.hset(getTfIdfKey(url), key, f.format(totalScore).toString());

            }
        }

    }

    public static void main(String[] args) {


        Jedis jedis = new Jedis();
        try {
           jedis = JedisMaker.make();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TfIdf test = new TfIdf(jedis);

        test.processIdf();
        //System.out.println(idf.size());
        //System.out.println(key);

       // System.out.println(test.getTotalDocumentSize());
        test.processTfIdf();

    }
}


