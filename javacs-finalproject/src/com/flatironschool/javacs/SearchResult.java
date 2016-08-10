package com.flatironschool.javacs;

import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by Chohee on 8/9/16.
 */
public class SearchResult {


    private List<String> result = new ArrayList<>();
    private String webstieOpt;
    private String[] searchTerm;
    private Jedis jedis;

    //websiteOpt = either stackoverflow or wiki
    public SearchResult(String websiteOpt, String[] searchTerm, Jedis jedis) {
        this.webstieOpt = websiteOpt;
        this.searchTerm= searchTerm;
        this.jedis = jedis;
    }

    public List<String> getResult() {

        Map<String, Double> map = new HashMap<>();

        for (String word : searchTerm) {

            String term = word.toLowerCase();

            System.out.println(term);
            Set<String> urls = jedis.zrevrange(webstieOpt + "PageRank:" + term, 0, 100);

            if (urls.isEmpty()) {
                continue;
            }

            for (String url : urls) {
                map.put(url, 0.0);

            }

        }

        for (String url : map.keySet()) {

            for (String term : searchTerm) {
                if (jedis.zrank(webstieOpt + "TF-IDF:" + url, term) != null) {

                    Double score = map.get(url) + jedis.zscore(webstieOpt + "TF-IDF:" + url, term);
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

            result.add(key);
            count++;
        }

        return result;
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


}
