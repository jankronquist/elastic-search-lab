package com.jayway.search;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class RunElasticSearch {
	
	static Client client;

	public static void main(String[] args) throws Exception {
		Node node = nodeBuilder().local(true).node();
		client = node.client();
		
		System.out.println("[Press any key to quit]");
		System.in.read();
		node.close();
	}
}
