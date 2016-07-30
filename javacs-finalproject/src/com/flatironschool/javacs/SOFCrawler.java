package com.flatironschool.javacs;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Chohee on 7/30/16.
 */
public class SOFCrawler {

    //keeps track of where we start
    private final String source;

    private JedisIndex index;

    //Queue of urls to be indexed
    private Queue<String> urls = new LinkedList<>();

    final static WikiFetcher wf = new WikiFetcher();

    public SOFCrawler(String source, JedisIndex index) {
        this.source = source;
        this.index = index;
        urls.offer(source);
    }

    //return the number of urls in the set
    public int queueSize(){
        return urls.size();
    }

    public String crawl() throws IOException {

        if(urls.isEmpty()) return null;

        String url = urls.poll();
        Elements contents = wf.fetchWikipedia(url);

        if(index.isIndexed(url))  {
            System.out.println(url + " is already indexed");
            return null;
        }

        //when index, figure out which part to index
        index.indexPage(url, contents);
        findInternalLinks(contents);

        return url;
    }


    private void findInternalLinks(Elements contents) {

        for(Element elements : contents ) {

            Elements links = elements.select("a[href]");

                for(Element link : links) {

                    if(link.hasAttr("href") && link.hasClass("question-hyperlink")) {

                        String possibleLink = link.attr("href");

                        if(possibleLink.startsWith("/questions/") && !possibleLink.startsWith("/questions/tagged/")) {
                            String newURL = "https://stackoverflow.com" + possibleLink;
                            System.out.println(newURL);
                            urls.offer(newURL);
                        }
                    }
                }

        }

    }

    public static void main(String[] args) throws IOException {

        Jedis jedis = JedisMaker.make();
        JedisIndex index = new JedisIndex(jedis);

        SOFCrawler test = new SOFCrawler("https://stackoverflow.com", index);

/** testing
        for(int i = 0; i < 100; i++) {
            String res = test.crawl();
            if(res == null)
                i--;
        }
 **/

    }



}
