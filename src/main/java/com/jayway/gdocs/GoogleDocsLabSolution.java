// @BEGIN_VERSION gdocs-list
package com.jayway.gdocs;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Base64;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import com.google.common.collect.ImmutableMap;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.extensions.LastModifiedBy;
import com.google.gdata.data.media.MediaSource;

public class GoogleDocsLabSolution {
	// this program has not been registered with Google
	static String CONSUMER_KEY = "anonymous";
	static String CONSUMER_SECRET = "anonymous";
	
	static DocsService client;

	public static void main(String[] args) throws Exception {
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
		AccessToken.load("accessToken.properties").prepare(oauthParameters);
		
		client = new DocsService("lab-service");
		client.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
		showAllDocs();
	}

	public static void showAllDocs() throws Exception {
		URL feedUri = new URL("https://docs.google.com/feeds/default/private/full/");
		DocumentListFeed feed = client.getFeed(feedUri, DocumentListFeed.class);

		// @BEGIN_VERSION integration
		Node node = nodeBuilder().local(true).node();
		// wait for startup and recovery to complete
		Thread.sleep(5000);
		Client client = node.client();
		// @END_VERSION integration

		for (DocumentListEntry entry : feed.getEntries()) {
			printDocumentEntry(entry);
			// @BEGIN_VERSION gdocs-download
			String fileName = download(entry);
			// @END_VERSION gdocs-download
			// @BEGIN_VERSION integration
			client.prepareIndex("test", "attachment", fileName)
				      .setSource(jsonBuilder()
				                  .startObject()
				                      .field("file", Base64.encodeFromFile(fileName))
				                  .endObject()
				                )
				      .execute()
				      .actionGet();
			// @END_VERSION integration
		}
		// @BEGIN_VERSION integration
		SearchResponse response = client.prepareSearch("test")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(termQuery("file", "something"))
		        .setFrom(0).setSize(60).setExplain(true)
		        .addHighlightedField("file")
		        .execute()
		        .actionGet();
		
		for (SearchHit hit : response.getHits()) {
			System.out.println(hit.getId() + ": " + hit.sourceAsMap().keySet());
	        for (HighlightField field : hit.getHighlightFields().values()) {
				System.out.println(" " + Arrays.asList(field.fragments()));
			}
		}
		node.close();
		// @END_VERSION integration
	}
	
	// @BEGIN_VERSION gdocs-download
	private static final Map<String, String> exportFormats = ImmutableMap.of("spreadsheet" , "xls", "document", "doc", "presentation", "ppt");
	
	private static String determineExportFormat(DocumentListEntry entry) {
		String format = exportFormats.get(entry.getType());
		if (format == null) {
			return "pdf";
		}
		return format;
	}

	private static String download(DocumentListEntry entry) throws Exception {
		String exportFormat = determineExportFormat(entry);
		String fileName = URLEncoder.encode(entry.getResourceId(), "UTF-8") + "." + exportFormat;
		String url = ((com.google.gdata.data.MediaContent)entry.getContent()).getUri();
		MediaContent mc = new MediaContent();
		mc.setUri(url + "&exportFormat=" + exportFormat);
		MediaSource ms = client.getMedia(mc);
		IOUtils.copy(ms.getInputStream(), new FileOutputStream(fileName));
		return fileName;
	}
	// @END_VERSION gdocs-download

	public static void printDocumentEntry(DocumentListEntry doc) {
		String resourceId = doc.getResourceId();
		String docType = resourceId.substring(0, resourceId.lastIndexOf(':'));

		System.out.println("'" + doc.getTitle().getPlainText() + "' ("
				+ docType + ")");
		System.out.println("  link to Google Docs: "
				+ doc.getHtmlLink().getHref());
		System.out.println("  resource id: " + resourceId);

		// print the parent folder the document is in
		if (!doc.getFolders().isEmpty()) {
			System.out.println("  in folder: " + doc.getFolders());
		}

		// print the timestamp the document was last viewed
		DateTime lastViewed = doc.getLastViewed();
		if (lastViewed != null) {
			System.out.println("  last viewed: " + lastViewed.toString());
		}

		// print who made that modification
		LastModifiedBy lastModifiedBy = doc.getLastModifiedBy();
		if (lastModifiedBy != null) {
			System.out.println("  updated by: " + lastModifiedBy.getName()
					+ " - " + lastModifiedBy.getEmail());
		}

		// print other useful metadata
		System.out.println("  last updated: " + doc.getUpdated().toString());
		System.out.println("  viewed by user? " + doc.isViewed());
		System.out.println("  writersCanInvite? "
				+ doc.isWritersCanInvite().toString());
		System.out.println("  hidden? " + doc.isHidden());
		System.out.println("  starrred? " + doc.isStarred());
		System.out.println();
	}
}
//@END_VERSION gdocs-list