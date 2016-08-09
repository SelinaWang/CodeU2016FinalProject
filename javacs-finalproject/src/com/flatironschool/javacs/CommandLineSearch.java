package com.flatironschool.javacs;

import java.io.Console;
import java.util.Arrays;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.StringTokenizer;

import redis.clients.jedis.Jedis;

public class CommandLineSearch { //change to commandlinesearch

    /**
     * Performs a search and makes a WikiSearch object.
     * 
     * @param term
     * @param index
     * @return
     */
    public static WikiSearch search(String term, JedisIndex index) {
        Map<String, Integer> map = index.getCounts(term);
        return new WikiSearch(map);
    }

    /**
     *  Initiates search, takes in search terms through command line Scanner
     *
     */
    public static void initSearch() throws IOException {
        // make a JedisIndex
        Jedis jedis = JedisMaker.make();
        JedisIndex index = new JedisIndex(jedis);

        // build stopwords dictionary and initialize scanner input
        StopWords sw = StopWords.build();
        sw.printStopWords();

        Scanner input = new Scanner(System.in);

        System.out.println("Search with team 22 <3 (type QUIT to exit)");
        // System.out.println("Wikipedia or StackOverflow (enter one)");
        // // Take in input from command line
        // String where = input.nextLine();
        // where = where.toLowerCase();
        // where = where.replaceAll("\\s","");

        // if (where.equals("wikipedia")) { // lower case me ty
        //     System.out.println("Searching Wikipedia...");
        // } else if (where.equals("wikipedia")) {
        //     System.out.println("Searching StackOverflow...");
        // } else {
        //     System.out.println("Invalid input, terminating."); // stack overflow search temp disabled
        //     return;
        // }

        while (true) {
            System.out.println("Enter search term:");
            String searchTerms = input.nextLine();
    
            StringTokenizer st = new StringTokenizer(searchTerms);
    
            WikiSearch searchResults = search("", index);
            WikiSearch prevSearch = search("", index);
            // combines tokens with AND if needed
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if(token.equals("QUIT")) {
                    System.out.println("Exiting search...");
                    return;
                }
                if(sw.exists(token)) {
                    continue;
                }
                System.out.println(token);
                System.out.println(sw.exists(token));
                WikiSearch thisSearch = search(token, index);
                searchResults = prevSearch.and(thisSearch);
                prevSearch = thisSearch;
            }
    
            System.out.println("Searching for: " + searchTerms);
            System.out.println("Printing results: ");
            searchResults.print();
        }
    }
    
    public static void main (String args[]) throws IOException {
        // initiate search
        initSearch();
    }
}