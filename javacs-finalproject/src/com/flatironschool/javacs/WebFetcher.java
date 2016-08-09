
package com.flatironschool.javacs;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


//make connection to given url and contain method that returns elements as requested.

public class WebFetcher implements Runnable {

	private long lastRequestTime = -1;
	private long minInterval = 250;


	private String url;
	private LinkedBlockingQueue<DocAndUrl> queue;

	public WebFetcher(String url, LinkedBlockingQueue<DocAndUrl> queue) {
		this.url = url;
		this.queue = queue;
	}

	/**
	 *
	 * @return document of given url - html text
	 * @throws IOException
     */
	public void getDocument() throws IOException {

		//sleepIfNeeded();

		Connection conn = Jsoup.connect(this.url);
		Document doc = conn.get();
		DocAndUrl docAndUrl = new DocAndUrl(this.url, doc);


		this.queue.offer(docAndUrl);
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

	@Override
	public void run() {
		try {
			getDocument();
		} catch (Exception e) {

			//e.printStackTrace();
			//System.out.println(e.toString());

			//catch 404 exception
			if (e.toString().contains("Status=404")) {
				///e.printStackTrace();
				System.out.println("this is 404!!!!!!!!!");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}

			}
		}
	}



}

class DocAndUrl {

	private String url;
	private Document document;

	public DocAndUrl(String url, Document document) {
		this.url = url;
		this.document = document;
	}

	//getters

	public String getURL() {return this.url;};

	public Document getDocument() { return this.document; };
}
