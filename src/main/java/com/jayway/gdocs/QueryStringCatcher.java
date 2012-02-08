package com.jayway.gdocs;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class QueryStringCatcher extends AbstractHandler {
	
	private java.util.concurrent.BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	private final String urlFilter;

	public QueryStringCatcher(String urlFilter) {
		this.urlFilter = urlFilter;
	}

	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		if (request.getRequestURL().toString().contains(urlFilter)) {
			queue.add(request.getQueryString());
			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			response.getWriter().println("OK");
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			baseRequest.setHandled(true);
		}
	}
	
	public String waitForRequest() throws InterruptedException {
		return queue.take();
	}
}
