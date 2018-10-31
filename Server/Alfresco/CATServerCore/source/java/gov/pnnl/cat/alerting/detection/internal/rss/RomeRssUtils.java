/*******************************************************************************
 * .
 *                           Velo 1.0
 *  ----------------------------------------------------------
 * 
 *            Pacific Northwest National Laboratory
 *                      Richland, WA 99352
 * 
 *                      Copyright (c) 2013
 *            Pacific Northwest National Laboratory
 *                 Battelle Memorial Institute
 * 
 *   Velo is an open-source collaborative content management
 *                and job execution environment
 *              distributed under the terms of the
 *           Educational Community License (ECL) 2.0
 *   A copy of the license is included with this distribution
 *                   in the LICENSE.TXT file
 * 
 *                        ACKNOWLEDGMENT
 *                        --------------
 * 
 * This software and its documentation were developed at Pacific 
 * Northwest National Laboratory, a multiprogram national
 * laboratory, operated for the U.S. Department of Energy by 
 * Battelle under Contract Number DE-AC05-76RL01830.
 ******************************************************************************/
package gov.pnnl.cat.alerting.detection.internal.rss;

import gov.pnnl.cat.alerting.AlertingConstants;
import gov.pnnl.cat.alerting.alerts.AlertManagementService;
import gov.pnnl.cat.alerting.alerts.Event;
import gov.pnnl.cat.alerting.alerts.internal.ActorImpl;
import gov.pnnl.cat.alerting.alerts.internal.EventImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.fetcher.impl.LinkedHashMapFeedInfoCache;

/**
 */
public class RomeRssUtils {

	/**
	 * Given a rss url and a date, get all entries published or updated after the date
	 * @param rssUrl
	 * @param date
	
	
	 * @return List<SyndEntry>
	 * @throws Exception */
	public static  List<SyndEntry> getRSSEntriesAfterDate(String rssUrl, Date date) throws Exception {

		FeedFetcherCache feedInfoCache = LinkedHashMapFeedInfoCache.getInstance();
		FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
		SyndFeed feed = feedFetcher.retrieveFeed(new URL(rssUrl));

		// we couldn't retrieve the feed, return null
		if (feed == null) {
			return null;
		}

		// no date specified, return all entries
		if (date == null) {
			return feed.getEntries();
		}

		// iterate through entries looking for ones published or updated after our date
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		for (SyndEntry entry : (List<SyndEntry>)feed.getEntries()) {
			if (entry.getPublishedDate() != null && date.before(entry.getPublishedDate())) {
				// add this entry if the published date is after the date of interest
				entries.add(entry);
			} else if (entry.getUpdatedDate() != null && date.before(entry.getUpdatedDate())) {
				// add this entry if the updated date is after the date of interest
				entries.add(entry);
			}
		}
		return entries;
	}

	/**
	 * Convert a ROME SyndEntry object to an Event object we can use for alerting
	 * @param alertManagementService
	 * @param rssEntries
	
	 * @return List<Event>
	 */
	public static List<Event> convertSyndEntriesToEvents(AlertManagementService alertManagementService, List<SyndEntry> rssEntries) {
		List<Event> events = new ArrayList<Event>();
		for (SyndEntry syndEntry : rssEntries) {
			Event event;
			if (alertManagementService == null) {
				event = new EventImpl();
			} else {
				event = alertManagementService.newEvent();
			}
			// TODO: figure out new or updated
			event.setChangeType(AlertingConstants.CHANGE_TYPE_NEW);
			
			ActorImpl actor = new ActorImpl();
	    actor.setUsername(syndEntry.getAuthor());
	    
			event.setEventPerpetrator(actor);

			if (syndEntry.getUpdatedDate() != null) {
				event.setEventTime(syndEntry.getUpdatedDate());
			} else {
				event.setEventTime(syndEntry.getPublishedDate());
			}
			event.setResourceName(syndEntry.getTitle());
			try {
				event.setResourceURL(new URL(syndEntry.getLink()));
			} catch (MalformedURLException me) {
				;
			}
			events.add(event);
		}
		return events;
	}

	/**
	 * Test program
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Timestamp ts = Timestamp.valueOf("2008-02-22 10:30:00");
			Date d = new Date(ts.getTime());

			List<SyndEntry> rssEntries = getRSSEntriesAfterDate("http://dsgillen.blogspot.com/feeds/posts/default", d);
			List<Event> events = convertSyndEntriesToEvents(null, rssEntries);
			System.out.println("Found " + events.size() + " events");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* In the future, we may want to do secure RSS, requiring name and password. 
	 * Here is how to do it in ROME
	 * 
String feed = "http://yoursite.com/index.rdf";
URL feedUrl = new URL(feed)
HttpURLConnection httpcon = (HttpURLConnection)feedUrl.openConnection();
String encoding = new sun.misc.BASE64Encoder().
  encode("username:password".getBytes());
httpcon.setRequestProperty ("Authorization", "Basic " + encoding);
SyndFeedInput input = new SyndFeedInput();
SyndFeed feed = input.build(new XmlReader(httpcon));

	 */
}
