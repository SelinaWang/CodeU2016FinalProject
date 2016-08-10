package com.flatironschool.javacs;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Chohee on 8/9/16.
 */
public class MainSearch {

    List<String> result = new ArrayList<>();
    String term;
    JedisIndex index;

    public MainSearch(String term, JedisIndex index) {
        this.term = term;
        this.index = index;
    }

    public void processingData() {

        StopWords dict = StopWords.build();
        // jedisIndex.main();
        jedisIndex.loadIndex(jedisIndex, source);
        new TFIDF(jedis).processTfIdf();
        // new RankURL(jedis).pushToRedis();

    }
}
