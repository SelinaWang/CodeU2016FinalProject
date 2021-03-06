package com.flatironschool.javacs;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Chohee on 7/30/16.
 */
public class WikiPageRanker {


    //maps term to relevance score
    private Map<String, Integer> map ;
    private String url;

    public WikiPageRanker(String url) {
        this.url = url;
        map = new HashMap<>();
    }

    public String getURL() {
        return this.url;
    }

    /**
     * Returns the total of all counts.
     *
     * @return
     */
    public int size() {
        int total = 0;
        for (Integer value: map.values()) {
            total += value;
        }
        return total;
    }

    /**
     * Takes Element and counts their words.
     * Element - the question
     * @param contents
     */
    public void processElements(String contents) {

            processText(contents);

    }



    /**
     * Splits `text` into words and counts them.
     *
     * @param text  The text to process.
     */
    public void processText(String text) {
        // replace punctuation with spaces, convert to lower case, and split on whitespace
        String[] array = text.replaceAll("\\pP", " ").toLowerCase().split("\\s+");

        for (int i=0; i<array.length; i++) {

            String term = array[i];

            //do not store the or a.
            if(!StopWords.set.contains(term))
                incrementTermCount(term);
        }
    }

    /**
     * Increments the counter associated with `term`.
     *
     * @param term
     */
    public void incrementTermCount(String term) {
        // System.out.println(term);
        put(term, get(term) + 1);
    }

    /**
     * Adds a term to the map with a given count.
     *
     * @param term
     * @param count
     */
    public void put(String term, int count) {
        map.put(term, count);
    }

    /**
     * Returns the count associated with this term, or 0 if it is unseen.
     *
     * @param term
     * @return
     */
    public Integer get(String term) {
        Integer count = map.get(term);
        return count == null ? 0 : count;
    }

    /**
     * Returns the set of terms that have been counted.
     *
     * @return
     */
    public Set<String> keySet() {
        return map.keySet();
    }



}
