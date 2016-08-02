
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


public class SOFFetcher {
	private long lastRequestTime = -1;
	private long minInterval = 1000;



	public Document getDocument(String url) throws IOException {

		sleepIfNeeded();

		Connection conn = Jsoup.connect(url);
		return conn.get();
	}



	/**
	 * Fetches and parses a URL string, returning a list of paragraph elements.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Elements fetchStackoverflow(Document doc) throws IOException {


		Elements body = doc.select("body");

		//Elements body = doc.select("body content a");

		return body;

	}

	public Elements readStackoverflow(Document doc, String className) throws IOException {


		Elements question = doc.getElementsByClass(className).select("p");
		return question;
	}

	public Elements allRelatedAndLinked(Document doc) throws IOException {

		Elements all = new Elements();
		Elements headerElements = readHeader(doc);
		Elements relatedLinked = relatedAndLinked(doc);


		if(headerElements != null ) {
			all = headerElements;
		}
		if(relatedLinked != null) {
			all.addAll(relatedLinked);
		}

		return all;
	}

	private Elements readHeader(Document doc) throws IOException {

		Element questionHeader = doc.getElementById("question-header");
		Elements headers = questionHeader == null? null : questionHeader.select("h1");
		return headers;
	}

	private Elements relatedAndLinked(Document doc) throws IOException {

		Elements linked = doc.getElementsByClass("question-hyperlink");
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
