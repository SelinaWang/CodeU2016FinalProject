package com.flatironschool.javacs;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Chohee on 7/30/16.
 */
public class WebCrawler {

    private JedisIndex index;

    //Queue of urls to be indexed
    private Queue<String> urlsToIndex;


    // private Queue<String> urls = new LinkedBlockingQueue<>();


    public WebCrawler(JedisIndex index, Queue<String> urlsToIndex) throws IOException {
        this.index = index;
        this.urlsToIndex= urlsToIndex;
    }





    /**
     * Fetches and parses a URL string, returning a list of paragraph elements.
     *
     * @param doc
     * @return
     * @throws IOException
     */
    public Elements fetchStackoverflow(Document doc) throws IOException {


        Elements body = doc.select("body");

        //Elements body = doc.select("body content a");

        return body;

    }

    /**
     *
     * Return paragraphs of given class name
     * ex) if class name is question, it will return all paragraphs in question div class.
     * @param doc
     * @param className
     * @return paragraphs
     * @throws IOException
     */
    public Elements readStackoverflow(Document doc, String className) throws IOException {


        Elements paragraphs = doc.getElementsByClass(className).select("p");
        return paragraphs;

    }

    /**
     *
     * @param doc
     * @return title of the question
     * @throws IOException
     */
    public Elements readHeadQeustion(Document doc) throws IOException {

        Element questionHeader = doc.getElementById("question-header");
        Elements headers = questionHeader == null? null : questionHeader.select("h1");
        return headers;
    }


    /**
     *
     * @param doc
     * @return paragraphs of related or linked questions
     * @throws IOException
     */
    public Elements relatedAndLinked(Document doc) throws IOException {

        Elements linked = doc.select("div.linked a.question-hyperlink ");
        Elements related = doc.select("div.related a.question-hyperlink");
        //Elements linked = doc.getElementsByClass("linked").getElementsByClass("question-hyperlink");

        linked.addAll(related);
        //System.out.println(linked.toString());
        return linked;
    }


    public String crawl(String url, Document doc) throws IOException, InterruptedException {

        //if url has been already indexed, do nothing.
        if(index.isIndexed(url))  {
            System.out.println(url + " is already indexed");
            //if(urlsToIndex.isEmpty())
                //findInternalLinks(this.fetchStackoverflow(doc));

            return null;
        }

//        Document doc = wf.getDocument(url);
        Elements body = this.fetchStackoverflow(doc);


        //get question paragraphs, get answer paragraphs, get related and linked paragraphs.
        List<Elements> eleList = new ArrayList<>();

        if(whatIsTheURL(url).equals("wiki")) {

            eleList.addAll(Arrays.asList(body.select("div#bodyContent p")));
            index.indexPage(url, eleList, body.select("div#bodyContent p"));
        }else if(whatIsTheURL(url).equals("stackoverflow")){
            eleList.addAll(Arrays.asList(this.readStackoverflow(doc, "question"), this.readStackoverflow(doc, "answer"), this.readHeadQeustion(doc)));
            index.indexPage(url, eleList, this.relatedAndLinked(doc));
        }



        findInternalLinks(body);
        return url;
    }


    public String whatIsTheURL(String url) {

        if(url.contains("wikipedia.org")) {
            return "wiki";
        }else if(url.contains("stackoverflow.com")) {
            return "stackoverflow";
        }

        return null;
    }

    /**
     * Find internal links in current url.
     * @param contents
     */
    private void findInternalLinks(Elements contents) {

        for(Element elements : contents ) {

            Elements links = elements.select("a[href]");

                for(Element link : links) {

                    if(link.hasAttr("href")) { //&& link.hasClass("question-hyperlink")) {

                        String possibleLink = link.attr("href");

                        if(possibleLink.startsWith("/questions/") && !possibleLink.startsWith("/questions/tagged/") && !possibleLink.startsWith("/questions/ask")) {
                            String newURL = "https://stackoverflow.com" + possibleLink;
                            //System.out.println(newURL);
                           // System.out.println(newURL);
                            urlsToIndex.offer(newURL);
                        }else if(possibleLink.startsWith("/wiki/")) {
                            String newURL = "https://en.wikipedia.org" + possibleLink;
                            urlsToIndex.offer(newURL);
                        }
                    }
                }

        }

    }

    public static void main(String[] args) {


        JedisIndex index = null;
        WebCrawler test = null;
        Queue<String> queue = new LinkedList<>();
        try {
          // index = new JedisIndex(new JedisMaker().make());
            test = new WebCrawler(index, queue);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

