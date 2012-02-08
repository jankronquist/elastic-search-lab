package com.jayway.gdocs;

import java.awt.Desktop;
import java.net.URI;

import org.eclipse.jetty.server.Server;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;

public class GetAccessToken {
	static String CONSUMER_KEY = "anonymous";
	static String CONSUMER_SECRET = "anonymous";
	
	public static void main(String[] args) throws Exception {
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey(CONSUMER_KEY);
		oauthParameters.setOAuthConsumerSecret(CONSUMER_SECRET);
		getTokens(oauthParameters);
	}

	private static void getTokens(GoogleOAuthParameters oauthParameters) throws Exception {
		oauthParameters.setScope("https://docs.google.com/feeds/");
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
