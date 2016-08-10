package com.flatironschool.javacs;


import java.io.IOException;

import java.util.Set;

import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.net.URLDecoder;


/**
 * Represents a map of common stop words.
 *
 */

public class StopWords {

    // map from stop words to null
    public static Set<String> set;

    /**
     * Constructor.
     *
     * @param set
     */
    public StopWords(Set<String> set) {
        this.set = set;
    }

    /**
     * Builds a map with some common stop words
     *
     * @param
     * @return
     */
    public static StopWords build() throws IOException{
        Set<String> ourSet = new HashSet<String>();

        String filename = "resources/stopwords.txt";
        URL fileURL = StopWords.class.getClassLoader().getResource(filename);
        String filepath = URLDecoder.decode(fileURL.getFile(), "UTF-8");

        BufferedReader br;

        try{
            br = new BufferedReader(new FileReader(filepath));

            while (true) {
                String line = br.readLine();

                if (line == null) break;
                ourSet.add(line);
            }
            br.close();
        } catch (FileNotFoundException e1) {
            System.out.println("File not found: " + filename);
            return null;
        }
        finally {
            return new StopWords(ourSet);
        }
    }

    public Set<String> getStopWord() {
        return set;
    }

    public void printStopWords() {
        int i = 0;
        for(String str: set) {
            System.out.print(i + " ");
            System.out.println(str);
            i++;
        }

    }

    /**
     * Determines whether a term is a stop word
     *
     * @param term
     * @return boolean
     */

    public boolean exists(String term) {
        return set.contains(term);
    }

    public static void main(String[] args) throws IOException {
        StopWords dict = StopWords.build();
        System.out.println("Printing built dictionary");
        dict.printStopWords();
    }

}

