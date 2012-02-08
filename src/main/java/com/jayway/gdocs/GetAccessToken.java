package com.jayway.gdocs;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

public class GetAccessToken {
	public static void main(String[] args) throws Exception {
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey(GoogleDocsLab.CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(GoogleDocsLab.CONSUMER_SECRET);
		oauthParameters.setScope("https://docs.google.com/feeds/");	// restrict token to Google Docs
		oauthParameters.setOAuthCallback("http://localhost:8080/token");

		GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
		oauthHelper.getUnauthorizedRequestToken(oauthParameters);
		
		Server server = new Server(8080);
		QueryStringCatcher queryStringCatcher = new QueryStringCatcher("token");
		server.setHandler(queryStringCatcher);
		server.start();

		URI uri = new URI(oauthHelper.createUserAuthorizationUrl(oauthParameters));
		System.out.println("-------------");

		if (Desktop.getDesktop() != null) {
			Desktop.getDesktop().browse(uri);
			
			System.out.println("Your browser have navigated to the following URL:");
			System.out.println(uri);
		} else {
			System.out.println("Please open the following URL in your browser:");
			System.out.println(uri);
		}
		
		System.out.println("Please login and grant access!");
		System.out.println(".... waiting ....");
		
		
		String queryString = queryStringCatcher.waitForRequest();
		Thread.sleep(100);
		server.stop();

		oauthHelper.getOAuthParametersFromCallback(queryString, oauthParameters);

		String accessToken = oauthHelper.getAccessToken(oauthParameters);
		String accessTokenSecret = oauthParameters.getOAuthTokenSecret();
		System.out.println("-------------");
		System.out.println("OAuth Access Token: " + accessToken);
		System.out.println("OAuth Access Token's Secret: " + accessTokenSecret);

		new AccessToken(accessToken, accessTokenSecret).save("accessToken.properties");
		System.out.println("Done.");
	}
}

class QueryStringCatcher extends AbstractHandler {
	
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
