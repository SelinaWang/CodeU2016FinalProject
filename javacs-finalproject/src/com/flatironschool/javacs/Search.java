package com.flatironschool.javacs;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by Chohee on 8/9/16.
 */
public class Search {

    public static void main(String[] args) {

        //args[0] is websiteopt and others are search term
        Jedis jedis = null;
        try {
            jedis = new JedisMaker().make();

        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("\n\n");
        List<String> result = new SearchResult
                (args[0].toLowerCase(), new Search().getSearchTerm(args), jedis)
                .getResult();

        for(String term : new Search().getSearchTerm(args))
            System.out.print(term + " ");

        System.out.println("\n");
        int i = 1;
        for(String url : result) {
            System.out.println( i + ") " + url);
            i++;
        }



    }

    private String[] getSearchTerm(String[] input) {

        String[] terms = new String[input.length-1];
        for(int i = 0; i < input.length-1; i++) {
            terms[i] = input[i+1].toLowerCase();
        }

        return terms;
    }

    private void startIndexing(Jedis jedis) throws IOException, InterruptedException {

        Set<String> stopWords = new HashSet<>();
        new StopWords(stopWords).build();
        String[] source = {"wiki", "stackoverflow"};

        //new TFIDF(jedis, "wiki").processTfIdf();
        //new TFIDF(jedis, "stackoverflow").processTfIdf();

        for(int i = 0; i < source.length; i++) {
            JedisIndex index = new JedisIndex(jedis, source[i]);
            index.loadIndex(index, source[i]);
            new TFIDF(jedis, source[i]).processTfIdf();
        }

    }

}
