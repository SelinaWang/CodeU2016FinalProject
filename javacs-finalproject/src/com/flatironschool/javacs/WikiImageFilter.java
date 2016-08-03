package com.flatironschool.javacs;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;

import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.api.services.vision.v1.Vision;

/*
 * Finds images in link and determines whether the images
 * are possibly NSFW using the Google Vision API for
 * Google Images SafeSearch.
 * 
 */
public class WikiImageFilter {
	private Elements images;

	public WikiImageFilter(Elements images) {
		this.images = images;
	}

	public void filterImages(Elements images) {
		// check each image for nsfw content
		// establish api connection at some point?
		List<String> urlList = new ArrayList<String>();

		for (Element image: images) {
			String imgURL = image.absUrl("src");
			urlList.add(imgURL);
		}

		for (String url: urlList) {

		}
	}
















}