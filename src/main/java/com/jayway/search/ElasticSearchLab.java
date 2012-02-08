package com.jayway.search;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class ElasticSearchLab {
	
	public static void main(String[] args) throws Exception {
		Node node = nodeBuilder().local(true).node();
		// wait for startup and recovery to complete
		Thread.sleep(1000);
		Client client = node.client();
		
		// TODO: write your code here
		
		node.close();
	}
}
