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
public class TFIDF {

    private Jedis jedis;

    private double totalDocuments; //total number of documents that is indexed - number of keys in TermCounters
    private Map<String, String> idf; //Map term to idf score
    private String websiteOtp;

    private DecimalFormat f = new DecimalFormat("##.00");


    public TFIDF(Jedis jedis, String websiteOtp) {

        this.jedis = jedis;
        this.websiteOtp = websiteOtp;
        idf = new HashMap<>();
        totalDocuments = jedis.keys(websiteOtp + "TermCounter:*").size();

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


        Set<String> terms = jedis.keys(websiteOtp + "URLSet:*");


        for(String key : terms){

            double idfScore = Math.log(getTotalDocumentSize()/jedis.smembers(key).size());
            idf.put(getKeyForIdf(key) , f.format(idfScore));
        }


    }




    /**
     *
     * @param key
     * @return valid key for idf map
     * Example : stackoverflowURLSet:foo  ====> foo
     */
    private String getKeyForIdf(String key) {
        if(websiteOtp.equals("wiki")) {
            return key.substring(11);
        }else {
            return key.substring(20);
        }
    }


    /**
     *
     * @param key
     * @return Valid key for TF-IDF
     * stackoverflowTermCounter:hhh ->
     */
    private String getTfIdfKey(String key) {
        if(websiteOtp.equals("wiki")) {
            return websiteOtp + "TF-IDF:" + key.substring(16);
        }else {
            return websiteOtp + "TF-IDF:" +key.substring(25);
        }
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

        //get all urls in term counter.
        Set<String> urls = jedis.keys(websiteOtp + "TermCounter:*");

        Map<String, String> map;


        //going though the url in the url set
        for(String url : urls) {

            //get member from current url, which is a map that maps from term to term frequency
            map = jedis.hgetAll(url);

            //going though each term
            for(String key : map.keySet()) {

                //get total score = idf *( 1 + log of tf)
                Double tf_scheme3 = 1 + Math.log(Double.parseDouble(map.get(key)));
                String totalScore = f.format(getIdfOfTerm(key) * tf_scheme3);

                //store in jedis
                jedis.zadd(getTfIdfKey(url), Double.parseDouble(totalScore), key);

              //  jedis.hset(getTfIdfKey(url), key, f.format(totalScore).toString());

            }
        }

    }


}


