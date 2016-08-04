
package com.flatironschool.javacs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


//make connection to given url and contain method that returns elements as requested.

public class SOFFetcher {

	private long lastRequestTime = -1;
	private long minInterval = 500;


	/**
	 *
	 * @param url
	 * @return document of given url - html text
	 * @throws IOException
     */
	public Document getDocument(String url) throws IOException {

		sleepIfNeeded();

		Connection conn = Jsoup.connect(url);
		return conn.get();
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



	/**
	 * Rate limits by waiting at least the minimum interval between requests.
	 */
	private void sleepIfNeeded() {
		if (lastRequestTime != -1) {
			long currentTime = System.currentTimeMillis();
			long nextRequestTime = lastRequestTime + minInterval;
			if (currentTime < nextRequestTime) {
				try {
					//System.out.println("Sleeping until " + nextRequestTime);
					Thread.sleep(nextRequestTime - currentTime);
				} catch (InterruptedException e) {
					System.err.println("Warning: sleep interrupted in fetchWikipedia.");
				}
			}
		}
		lastRequestTime = System.currentTimeMillis();
	}

	public static void main(String[] arsg) {

		String url = "http://stackoverflow.com/questions/12672428/css-max-width-wont-shrink?noredirect=1&lq=1";

		SOFFetcher test = new SOFFetcher();


		Elements all = new Elements();
		try {
			Elements header = test.relatedAndLinked(test.getDocument(url));
			Elements linked = test.relatedAndLinked(test.getDocument(url));

			all.addAll(header);
			all.addAll(linked);

		} catch (IOException e) {
			e.printStackTrace();
		}



	}
}
