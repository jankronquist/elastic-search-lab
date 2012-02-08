package com.jayway.search;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class EmbedElastic {
	
	static Client client;

	public static void main(String[] args) throws Exception {
		Node node = nodeBuilder().local(true).node();
		client = node.client();
		
		IndexResponse response3 = client.prepareIndex("twitter", "tweet", "1")
		        .setSource(jsonBuilder()
		                    .startObject()
		                        .field("user", "kimchy")
		                        .field("postDate", new Date())
		                        .field("message", "trying out Elastic Search")
		                    .endObject()
		                  )
		        .execute()
		        .actionGet();
		
		indexDocument("presentation%3A0AVXSUau7i_zVZGozOTlidl8wZ3FzNjRnZHI.ppt");
		indexDocument("document%3A18CyBcDGLXYFucXVTS3WhlWPEzx74T4tiuAHy_OgXMzE.doc");
		indexDocument("fn6742.pdf");

		System.out.println("Inserted documents");
		
		
//		QueryBuilder qb = termQuery("user", "kimchy");
		QueryBuilder qb = termQuery("qwords", "presentation");

		SearchResponse scrollResp = client.prepareSearch("files")
		                .setSearchType(SearchType.SCAN)
		                .setScroll(new TimeValue(60000))
		                .setQuery(qb.buildAsBytes())
		                .addHighlightedField("qwords")
		                .setSize(100).execute().actionGet(); //100 hits per shard will be returned for each scroll
		//Scroll until no hits are returned
		while (true) {
		    scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
		    boolean hitsRead = false;
		    for (SearchHit hit : scrollResp.getHits()) {
		        hitsRead = true;
		        System.out.println("Hit id=" + hit.getId());
		        if (hit.getHighlightFields().size() == 0) {
		        	System.out.println("No highlights!");
		        }
		        for (Entry<String, HighlightField> entry : hit.getHighlightFields().entrySet()) {
					System.out.format(" %s - %s\n", entry.getKey(), Arrays.asList(entry.getValue().fragments()));
				}
		    }
		    //Break condition: No hits are returned
		    if (!hitsRead) {
		        break;
		    }
		}

		System.out.println("query completed");
		
		System.in.read();
		// on shutdown

		node.close();

	}

	private static void indexDocument(String fileName) throws IOException,
			FileNotFoundException, SAXException, TikaException {
		client.prepareIndex("files", "file", fileName)
		        .setSource(jsonBuilder()
		                    .startObject()
		                        .field("fileName", fileName)
		                        .field("qwords", toQwords(fileName))
		                    .endObject()
		                  )
		        .execute()
		        .actionGet();
	}

	private static String toQwords(String file) throws FileNotFoundException,
			IOException, SAXException, TikaException {
		InputStream input = new FileInputStream(file);
		ContentHandler textHandler = new BodyContentHandler();
		Metadata metadata = new Metadata();
		Parser parser = new AutoDetectParser();
		parser.parse(input, textHandler, metadata, new ParseContext());
		input.close();
		String string = textHandler.toString();
		return string;
	}
}
