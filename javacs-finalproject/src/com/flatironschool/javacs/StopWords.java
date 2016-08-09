package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a map of common stop words.
 * 
 */

public class StopWords {

	// map from stop words to null
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public StopWords(Map<String, Integer> map) {
		this.map = map;
	}

	/**
	 * Builds a map with some common stop words
	 * 
	 * @param
	 * @return
	 */
	public static StopWords build() {
		Map<String, Integer> map = new HashMap<String, Integer>();

		try{
			BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"));
			String line;
 
            while ((line = br.readLine()) != null) {
                map.put(line, null);
            }
            br.close();
        }
        finally {
			return new StopWords(map);
		}
	}

	/**
	 * Determines whether a term is a stop word
	 * 
	 * @param term
	 * @return boolean
	 */

	public boolean exists(String term) {
		return map.containsKey(term);
	}

	public static void main(String[] args) throws IOException {
		StopWords dict = StopWords.build();
	}
	
}