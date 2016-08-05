package com.flatironschool.javacs;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a map of common stop words.
 * 
 */

public class StopWords {

	// map from stop words to null
	private Map<String, null> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public StopWords(Map<String, null> map) {
		this.map = map;
	}

	/**
	 * Builds a map with some common stop words
	 * 
	 * @param
	 * @return
	 */
	public static StopWords build() {
		Map<String, null> map = new HashMap<String, null>();

		try(BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"))) {
			String line;
 
            while ((line = bufferedReader.readLine()) != null) {
                map.put(line, null);
            }
            reader.close();
        }
		return new StopWords(map);
	}

	/**
	 * Determines whether a term is a stop word
	 * 
	 * @param term
	 * @return boolean
	 */

	public static boolean exists(String term) {
		return map.containsKey(term);
	}
	
}