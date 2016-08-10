package com.flatironschool.javacs;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 * 
 */
public class JedisIndex {

	private Jedis jedis;
	private String websiteOpt; //either wiki or stackoverflow

	/**
	 * Constructor.
	 * 
	 * @param jedis
	 */
	public JedisIndex(Jedis jedis, String websiteOpt) throws IOException {

		this.jedis = jedis;
		this.websiteOpt = websiteOpt;

	}

	
	/**
	 * Returns the Redis key for a given search term.
	 * 
	 * @return Redis key.
	 */
	private String urlSetKey(String term) {

		return websiteOpt + "URLSet:" + term;
	}
	
	/**
	 * Returns the Redis key for a URL's TermCounter.
	 * 
	 * @return Redis key.
	 */
	private String termCounterKey(String url) {


		return websiteOpt + "TermCounter:" + url;

	}


	private String pageRankKey(String term) {

		return websiteOpt + "PageRank:" + term;
	}
	/**
	 * Checks whether we have a TermCounter for a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public boolean isIndexed(String url) {
		String redisKey = termCounterKey(url);
		return jedis.exists(redisKey);
	}
	
	/**
	 * Adds a URL to the set associated with `term`.
	 * 
	 * @param term
	 * @param tc
	 */
	public void add(String term, TermCounter tc) {

		jedis.sadd(urlSetKey(term), tc.getLabel());
	}

	/**
	 * Looks up a search term and returns a set of URLs.
	 * 
	 * @param term
	 * @return Set of URLs.
	 */
	public Set<String> getURLs(String term) {
		Set<String> set = jedis.smembers(urlSetKey(term));
		return set;
	}

	/**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCounts(String term) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Set<String> urls = getURLs(term);
		for (String url: urls) {
			Integer count = getCount(url, term);
			map.put(url, count);
		}
		return map;
	}

	/**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCountsFaster(String term) {
		// convert the set of strings to a list so we get the
		// same traversal order every time
		List<String> urls = new ArrayList<String>();
		urls.addAll(getURLs(term));

		// construct a transaction to perform all lookups
		Transaction t = jedis.multi();
		for (String url: urls) {
			String redisKey = termCounterKey(url);
			t.hget(redisKey, term);
		}
		List<Object> res = t.exec();

		// iterate the results and make the map
		Map<String, Integer> map = new HashMap<String, Integer>();
		int i = 0;
		for (String url: urls) {
			//System.out.println(url);
			Integer count = new Integer((String) res.get(i++));
			map.put(url, count);
		}
		return map;
	}

	/**
	 * Returns the number of times the given term appears at the given URL.
	 * 
	 * @param url
	 * @param term
	 * @return
	 */
	public Integer getCount(String url, String term) {
		String redisKey = termCounterKey(url);
		String count = jedis.hget(redisKey, term);
		return new Integer(count);
	}



	/**
	 * Add a page to the index.
	 * 
	 * @param url         URL of the page.
	 * @param aListElements - List of Elements of question,answers, and title of questions.
	 * @param questions -  linked and related questions as elements
	 */
	public void indexPage(String url, List<Elements> aListElements, Elements questions) throws IOException {
		System.out.println("Indexing " + url);
		
		// make a TermCounter and count the terms in the paragraphs
		TermCounter tc = new TermCounter(url);


		//processing for Termcounter and PageRanker
		for(Elements elements : aListElements) {
			if(elements != null) {
				tc.processElements(elements);
			}

		}


		//wikipedia case
		if(websiteOpt.equals("wiki")) {
			if (questions != null) {

				for (Element term : questions) {

					for (Element a : term.select("a[href]")) {

						if (a.attr("href").startsWith("/wiki/")) {

							WikiPageRanker pr = new WikiPageRanker("https://en.wikipedia.org" + a.attr("href"));
							//System.out.println(a.toString());
							pr.processElements(a.attr("title"));
							//System.out.println("Term : " + a.attr("title") + " Link: " + a.attr("href"));
							pushPageRankerToRedisWiki(pr);
						}

					}
				}
			}
		}else if(websiteOpt.equals("stackoverflow")) {

			//When questions are not null, store in jedis
			if (questions != null) {

				//going though each question
				for (Element questionTerm : questions) {

					PageRanker pr = null;
					//FSystem.out.println(questionTerm);

					//get hyperlink for the question and create new pageranker for question url.
					pr = new PageRanker("https://stackoverflow.com" + questionTerm.attr("href"));
					pr.processElements(questionTerm);


					// push the contents of the PageRanker to Redis
					pushPageRankerToRedis(pr);
				}

			}
		}


		// push the contents of the TermCounter to Redis
		pushTermCounterToRedis(tc);


	}

	/**
	 *  Pushes the contents of PageRacker to Redis
	 * @param pr
	 */
	public void pushPageRankerToRedisWiki(WikiPageRanker pr) {


		String url = pr.getURL();

		Transaction t = jedis.multi();


		//used the jedis data structure called sorted set.
		for(String term : pr.keySet()) {
			//System.out.println(term);
			t.zincrby(pageRankKey(term), pr.get(term), url);
		}

		t.exec();
	}

	/**
	 *  Pushes the contents of PageRacker to Redis
	 * @param pr
     */
	public void pushPageRankerToRedis(PageRanker pr) {


		String url = pr.getURL();

		Transaction t = jedis.multi();


		//used the jedis data structure called sorted set.
		for(String term : pr.keySet()) {
			//System.out.println(term);
			t.zincrby(pageRankKey(term), pr.get(term), url);
		}

		t.exec();
	}

	/**
	 * Pushes the contents of the TermCounter to Redis.
	 * 
	 * @param tc
	 * @return List of return values from Redis.
	 */
	public List<Object> pushTermCounterToRedis(TermCounter tc) {

		String url = tc.getLabel();
		String hashname = termCounterKey(url);
		
		// if this page has already been indexed; delete the old hash
		if(isIndexed(url))
			jedis.del(hashname);

		Transaction t = jedis.multi();

		// for each term, add an entry in the termcounter and a new
		// member of the index
		for (String term: tc.keySet()) {
			Integer count = tc.get(term);
			t.hset(hashname, term, count.toString());
			t.sadd(urlSetKey(term), url);
		}

		List<Object> res = t.exec();
		return res;
	}


	/**
	 * Returns the set of terms that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> termSet() {
		Set<String> keys = urlSetKeys(websiteOpt);
		Set<String> terms = new HashSet<String>();
		for (String key: keys) {
			String[] array = key.split(":");
			if (array.length < 2) {
				terms.add("");
			} else {
				terms.add(array[1]);
			}
		}
		return terms;
	}

	/**
	 * Returns URLSet keys for the terms that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> urlSetKeys(String websiteOpt) {


		return jedis.keys(websiteOpt + "URLSet:*");
	}

	/**
	 * Returns TermCounter keys for the URLS that have been indexed.
	 * 
	 * Should be used for development and testing, not production.
	 * 
	 * @return
	 */
	public Set<String> termCounterKeys() {

		return jedis.keys(websiteOpt + "TermCounter:*");
	}




	private long transToMilSeconds(double minutes) {

		return (long)(minutes * 60000);
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 * 
	 * @return
	 * @throws IOException
	 */
	public void loadIndex(JedisIndex index, String source) throws IOException, InterruptedException {

		//index.jedis.flushAll();

		ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

		//Thread safe queues
		LinkedBlockingQueue<String> urlsToIndex = new LinkedBlockingQueue<>();
		LinkedBlockingQueue<DocAndUrl> downloadedURLS = new LinkedBlockingQueue<>();


		//starting point here
		if(source.equals("wiki")) {
			source = "https://en.wikipedia.org/wiki/korea";
		}else if(source.equals("stackoverflow")) {
			source = "https://www.stackoverflow.com";
		}

		Runnable run1 = new WebFetcher(source, downloadedURLS);
		new Thread(run1).start();


		//First thread - indexing by invoking Crawler class
		//Crawl the document and url that are downloaded.
		//indexing once a time because jedis is not thread safe

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						DocAndUrl docAndUrl = downloadedURLS.take();
						/**
						if (docAndUrl == null) {
							//Thread.sleep(5000);
							continue;
						}

						 **/
						new WebCrawler(index, urlsToIndex).crawl(docAndUrl.getURL(), docAndUrl.getDocument());
					} catch (InterruptedException e) {
						//e.printStackTrace();
						break;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();


		//Multi threads - fetch urls and put them in a queue
		long startT = System.currentTimeMillis();
		for (;;) {

			String url = urlsToIndex.poll(5, TimeUnit.SECONDS);


			// index a limited number of pages
			// once this limit is hit, stop scheduling new pages to be downloaded
			if(startT + transToMilSeconds(10) < System.currentTimeMillis()) {

				pool.getQueue().clear();
				pool.shutdown();
				pool.awaitTermination(2, TimeUnit.SECONDS);
				break;
			}

			pool.execute(new WebFetcher(url, downloadedURLS));
		}



		pool.shutdown();
		while (!pool.isTerminated()) {
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
		}
		t.interrupt();
		t.join();



	}

	public void close() {
		this.jedis.close();
	}
}
