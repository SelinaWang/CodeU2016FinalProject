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

    private double totalDocuments; //total number of documents that is indexed - number of keys in TermCounters
    private Map<String, String> idf; //Map term to idf score

    private DecimalFormat f = new DecimalFormat("##.00");


    public TfIdf(Jedis jedis) {

        this.jedis = jedis;
        idf = new HashMap<>();
        totalDocuments = jedis.keys("TermCounter:*").size();
    }


    /**
     *
     * @return total number of urls that are indexed
     */
    public double getTotalDocumentSize() {
        return totalDocuments;
    }



    /**
     * Going though each term in URLSet and count how many members(urls) are there.
     * idf = log of fraction of number of urls that are indexed over number of urls that contain a term.
     */
    private void processIdf() {


        Set<String> terms = jedis.keys("URLSet:*");


        for(String key : terms){

          //  System.out.println("Total Document : " + getTotalDocumentSize() + " Contained Document : " + jedis.smembers(key).size());
            double idfScore = logBaseTwo(getTotalDocumentSize()/jedis.smembers(key).size());
            idf.put(getKeyForIdf(key) , f.format(idfScore));
           // System.out.println(getKeyForIdf(key) + " : " +  f.format(idfScore));
        }

        //System.out.println(idf.size());

    }


    /**
     *
     * @param score
     * @return log base 2 score
     */
    private double logBaseTwo(double score) {
      //  System.out.println(score);
       // System.out.println(Math.log(score)/Math.log(2));
        return Math.log(score)/Math.log(2);
    }


    /**
     *
     * @param key
     * @return valid key for idf map
     * Example : URLSet:foo  ====> foo
     */
    private String getKeyForIdf(String key) {
        return key.substring(7);
    }


    /**
     *
     * @param key
     * @return Valid key for TF-IDF
     */
    private String getTfIdfKey(String key) {
        //System.out.println(key);
        return "TF-IDF:" + key.substring(12);
    }



    /**
     *
     * @param term
     * @return idf score of the given term
     */
    private double getIdfOfTerm(String term) {
        return Double.parseDouble(idf.get(term)) ;
    }


    /**
     *  Store the data to redis.
     *  Key form -> "TF-IDF:https://stackoverflow.com~"
     */
    public void processTfIdf() {

        processIdf();

        Set<String> urls = jedis.keys("TermCounter:*");
        Map<String, String> map;

        for(String url : urls) {

            map = jedis.hgetAll(url);

            for(String key : map.keySet()) {

                String totalScore = f.format(getIdfOfTerm(key) + Double.parseDouble(map.get(key)));
                jedis.zadd(getTfIdfKey(url), Double.parseDouble(totalScore), key);
              //  jedis.hset(getTfIdfKey(url), key, f.format(totalScore).toString());

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


