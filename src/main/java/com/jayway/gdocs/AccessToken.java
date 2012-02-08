package com.jayway.gdocs;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

public class AccessToken {
	private static final String ACCESS_TOKEN_SECRET = "accessTokenSecret";
	private static final String ACCESS_TOKEN = "accessToken";
	private final String accessToken;
	private final String accessTokenSecret;

	public AccessToken(String accessToken, String accessTokenSecret) {
		this.accessToken = accessToken;
		this.accessTokenSecret = accessTokenSecret;
	}
	
	public void save(String fileName) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(ACCESS_TOKEN, accessToken);
		properties.setProperty(ACCESS_TOKEN_SECRET, accessTokenSecret);
		properties.store(new FileWriter(fileName), "");
	}

	public static AccessToken load(String fileName) throws IOException {
		Properties properties = new Properties();
		properties.load(new FileReader(fileName));
		return new AccessToken(properties.getProperty(ACCESS_TOKEN), properties.getProperty(ACCESS_TOKEN_SECRET));
	}

	public void prepare(GoogleOAuthParameters oauthParameters) {
		oauthParameters.setOAuthToken(accessToken);
		oauthParameters.setOAuthTokenSecret(accessTokenSecret);
	}
}
