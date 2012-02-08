package com.jayway.search;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.Arrays;
import java.util.Date;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Base64;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

public class ElasticSearchLabSolution {
	
	public static void main(String[] args) throws Exception {
		Node node = nodeBuilder().local(true).node();
		// wait for startup and recovery to complete
		Thread.sleep(1000);
		Client client = node.client();
		
		// @BEGIN_VERSION es-java-api

		GetResponse gr = client.prepareGet("twitter", "tweet", "1")
		        .execute()
		        .actionGet();
		System.out.println(gr.getSource());
		
		IndexResponse ir = client.prepareIndex("twitter", "tweet", "3")
			    .setSource(jsonBuilder()
			                .startObject()
			                    .field("user", "kimchy")
			                    .field("postDate", new Date())
			                    .field("message", "trying out Java API")
			                .endObject()
			              )
			    .execute()
			    .actionGet();

		SearchResponse sr = client.prepareSearch("twitter")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        // different types of queries
		        .setQuery(termQuery("user", "kimchy"))
//		        .setQuery(QueryBuilders.fuzzyQuery("user", "kimchi"))
//		        .setQuery(QueryBuilders.rangeQuery("post_date").lt(new Date()).gt(new Date()))
//		        .setQuery(QueryBuilders.boolQuery().must(termQuery("user", "kimchy")).must(termQuery("message", "good")))
		        .setFrom(0).setSize(60).setExplain(true)
		        .execute()
		        .actionGet();
		
		for (SearchHit hit : sr.getHits()) {
			System.out.println(hit.getId() + ": " + hit.getSource());
		}

		// @BEGIN_VERSION es-java-documents
		String fileName = "pom.xml";
		IndexResponse ir2 = client.prepareIndex("test", "attachment", fileName)
			      .setSource(jsonBuilder()
			                  .startObject()
			                      .field("file", Base64.encodeFromFile(fileName))
			                  .endObject()
			                )
			      .execute()
			      .actionGet();

		SearchResponse sr2 = client.prepareSearch("test")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(termQuery("file", "elasticsearch"))
		        .setFrom(0).setSize(60).setExplain(true)
			// @BEGIN_VERSION es-highlighting
		        .addHighlightedField("file")
			// @END_VERSION es-highlighting
		        .execute()
		        .actionGet();
		
		for (SearchHit hit : sr2.getHits()) {
			System.out.println(hit.getId() + ": " + hit.sourceAsMap().keySet());
			// @BEGIN_VERSION es-highlighting
	        for (HighlightField field : hit.getHighlightFields().values()) {
				System.out.println(" " + Arrays.asList(field.fragments()));
			}
			// @END_VERSION es-highlighting
		}
		// @END_VERSION es-java-documents
		// @END_VERSION es-java-api

		node.close();
	}
}
