package com.flatironschool.javacs;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

/**
 * Created by Chohee on 7/30/16.
 */
public class testSearch {

    public static void main(String[] args) {



        Jedis jedis = null;
        String source = getSourceURL(args);
        try {
            jedis = JedisMaker.make();
            jedis.flushAll();
           storeDataIntoRedis(jedis, source);
           // new RankURL(jedis).pushToRedis();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, Double> map = new HashMap<>();

        for(String word : args) {

            String term = word.toLowerCase();

            System.out.println(term);
            Set<String> urls = jedis.zrevrange("PageRank:" + term, 0, 100);

            if(urls.isEmpty()) {
                continue;
            }

            for(String url : urls) {
                map.put(url, 0.0);

            }

        }

        for(String url : map.keySet()) {

            for(String term : args) {
                if (jedis.zrank("TF-IDF:" + url, term) != null ) {

                    Double score = map.get(url) + jedis.zscore("TF-IDF:" + url, term);
                    map.put(url, score);
                }
            }
        }



        Map<String, Double> sortedMap = sortByComparator(map);

        int count = 0;
        for(String key : sortedMap.keySet()) {
            if(count == 10 ) {
                break;
            }

            System.out.println("key : " + key + " score : " + map.get(key));
            count++;
        }


    }

    private static String getSourceURL(String[] args) {

        StringBuffer url = new StringBuffer();
        url.append("http://stackoverflow.com/search?q=");
        for(String word : args) {
            url.append(word + "+");
        }

        return url.toString();
    }

    private static Map<String, Double> sortByComparator(Map<String, Double> unsortedMap) {

        // Convert Map to List
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortedMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();

        for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public static void storeDataIntoRedis(Jedis jedis, String source) throws IOException, InterruptedException {

        StopWords dict = StopWords.build();

       // JedisIndex jedisIndex = new JedisIndex(jedis);
       // jedisIndex.main();
       // jedisIndex.loadIndex(jedisIndex, source);
       // new TFIDF(jedis).processTfIdf();
       // new RankURL(jedis).pushToRedis();
    }
}
