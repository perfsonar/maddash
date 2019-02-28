package net.es.jsnow.oauth;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;

/**
 * Utilities for accessing resources protected by OAuth. Will obtain token given credentials, and if
 * already has token will attempt to refresh token before obtaining a new one.
 *
 * Based on tutorial at https://www.ibm.com/developerworks/library/se-oauthjavapt1/index.html
 */
public class OAuthUtils {
    private static Logger log = Logger.getLogger(OAuthUtils.class);

	/**
	 * Send an HTTP GET request for a OAuth protected resource
	 * @param resourceURL the URL to access
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
	public static JsonObject getProtectedJsonResource(String resourceURL, OAuth2Details oauthDetails) {
        JsonObject response = null;
        HttpGet get = new HttpGet(resourceURL);
        try {
            response = protectedResourceRequest(get, oauthDetails);
        }finally {
            get.releaseConnection();
        }
        return response;
    }

	/**
	 * Send an HTTP POST request for a OAuth protected resource
	 * @param resourceURL the URL to access
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
    public static JsonObject postProtectedJsonResource(String resourceURL, JsonObject body, OAuth2Details oauthDetails) {
        JsonObject response = null;
        HttpPost post = new HttpPost(resourceURL);
        post.addHeader("Content-Type", "application/json");
        try {
            post.setEntity(new StringEntity(body + ""));
            response = protectedResourceRequest(post, oauthDetails);
        }catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }finally{
            post.releaseConnection();
        }

        return response;
    }

	/**
	 * Send an HTTP PUT request for a OAuth protected resource
	 * @param resourceURL the URL to access
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
	public static JsonObject putProtectedJsonResource(String resourceURL, JsonObject body, OAuth2Details oauthDetails) {
		JsonObject response = null;
		HttpPut put = new HttpPut(resourceURL);
		put.addHeader("Content-Type", "application/json");
		try {
			put.setEntity(new StringEntity(body + ""));
			response = protectedResourceRequest(put, oauthDetails);
		}catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}finally{
			put.releaseConnection();
		}

		return response;
	}

	/**
	 * Sent an HTTP PATCH request for a OAuth protected resource
	 * @param resourceURL the URL to access
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
	public static JsonObject patchProtectedJsonResource(String resourceURL, JsonObject body, OAuth2Details oauthDetails) {
		JsonObject response = null;
		HttpPatch patch = new HttpPatch(resourceURL);
		patch.addHeader("Content-Type", "application/json");
		try {
			patch.setEntity(new StringEntity(body + ""));
			response = protectedResourceRequest(patch, oauthDetails);
		}catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage());
		}finally{
			patch.releaseConnection();
		}

		return response;
	}

	/**
	 * Sent an HTTP DELETE request for a OAuth protected resource
	 * @param resourceURL the URL to access
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
	public static JsonObject deleteProtectedJsonResource(String resourceURL, OAuth2Details oauthDetails) {
		JsonObject response = null;
		HttpDelete delete = new HttpDelete(resourceURL);
		response = protectedResourceRequest(delete, oauthDetails);
		delete.releaseConnection();

		return response;
	}

	/**
	 * Generic funtion that accepts an httpRequest of anty type and attempts to access the resource
	 * @param httpRequest the HTTP request to perform
	 * @param oauthDetails the OAuth parameters
	 * @return the returned JSON object
	 */
	public static JsonObject protectedResourceRequest(HttpRequestBase httpRequest, OAuth2Details oauthDetails) {


		httpRequest.addHeader(OAuthConstants.AUTHORIZATION,
				getAuthorizationHeaderForAccessToken(oauthDetails
						.getAccessToken()));
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		int code = -1;
        JsonObject responseJsonObj = null;
		try {
			response = client.execute(httpRequest);
			code = response.getStatusLine().getStatusCode();
			if (code >= 400) {
				// Access token is invalid or expired.Regenerate the access token
				log.debug("Access token is invalid or expired. Regenerating access token.");
                OAuth2Details newOAuthDetails = retrieveAccessToken(oauthDetails);
				if (isValid(newOAuthDetails.getAccessToken())) {
					// update the access token
					log.debug("new access token: " + newOAuthDetails.getAccessToken());
					oauthDetails.setAccessToken(newOAuthDetails.getAccessToken());
                    if (isValid(newOAuthDetails.getRefreshToken())) {
                        oauthDetails.setRefreshToken(newOAuthDetails.getRefreshToken());
                    }
					httpRequest.removeHeaders(OAuthConstants.AUTHORIZATION);
					httpRequest.addHeader(OAuthConstants.AUTHORIZATION,
							getAuthorizationHeaderForAccessToken(oauthDetails
									.getAccessToken()));
					httpRequest.releaseConnection();
					response = client.execute(httpRequest);
					code = response.getStatusLine().getStatusCode();
					if (code >= 400) {
						throw new RuntimeException(
								"Could not access protected resource. Server returned http code: "
										+ code);

					}

				} else {
					throw new RuntimeException("Could not regenerate access token");
				}

			}

            responseJsonObj = handleJsonResponse(response);

		}catch (Exception e) {
			log.debug("Error getting protected resource: " + e.getMessage());
		}

		return responseJsonObj;
	}

	/**
	 * Handles retrieving the OAuth access token First it tries to refresh the token, then tries
	 * client credentials to get token, and finally tries username and password
	 * @param oauthDetails the current OAuthDetails
	 * @return OAuth parameters containing new token information
	 */
	public static OAuth2Details retrieveAccessToken(OAuth2Details oauthDetails) {
		HttpPost post = new HttpPost(oauthDetails.getAuthenticationServerUrl());
		String clientId = oauthDetails.getClientId();
		String clientSecret = oauthDetails.getClientSecret();
		String scope = oauthDetails.getScope();

		List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
		parametersBody.add(0, new BasicNameValuePair(OAuthConstants.GRANT_TYPE,
				OAuthConstants.REFRESH_TOKEN)); //keep this first in list so update below works
		parametersBody.add(new BasicNameValuePair(OAuthConstants.USERNAME,
				oauthDetails.getUsername()));
		parametersBody.add(new BasicNameValuePair(OAuthConstants.PASSWORD,
				oauthDetails.getPassword()));
        if (isValid(oauthDetails.getRefreshToken())) {
            parametersBody.add(new BasicNameValuePair(OAuthConstants.REFRESH_TOKEN,
                    oauthDetails.getRefreshToken()));
        }
		if (isValid(clientId)) {
			parametersBody.add(new BasicNameValuePair(OAuthConstants.CLIENT_ID,
					clientId));
		}
		if (isValid(clientSecret)) {
			parametersBody.add(new BasicNameValuePair(
					OAuthConstants.CLIENT_SECRET, clientSecret));
		}
		if (isValid(scope)) {
			parametersBody.add(new BasicNameValuePair(OAuthConstants.SCOPE,
					scope));
		}

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		String accessToken = null;
		String refreshToken = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(parametersBody, HTTP.UTF_8));
            log.debug("Attempting to refresh access token with parameters: " + parametersBody);
			response = client.execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code >= 400) {
                post.releaseConnection();
				// Add Basic Authorization header
				post.addHeader(
						OAuthConstants.AUTHORIZATION,
						getBasicAuthorizationHeader(oauthDetails.getClientId(),
								oauthDetails.getClientSecret()));
                //Remove refresh_token grant type
                parametersBody.remove(0);
                parametersBody.add(0, new BasicNameValuePair(OAuthConstants.GRANT_TYPE,
                        OAuthConstants.PASSWORD));
                post.setEntity(new UrlEncodedFormEntity(parametersBody, HTTP.UTF_8));
				log.debug("Refresh did not work, trying BASIC auth with client credentials and parameters: " + parametersBody);
				response = client.execute(post);
				code = response.getStatusLine().getStatusCode();
				if (code >= 400) {
                    post.releaseConnection();
					log.debug("Client credentials did not work, trying username and password");
					post.addHeader(
							OAuthConstants.AUTHORIZATION,
							getBasicAuthorizationHeader(
									oauthDetails.getUsername(),
									oauthDetails.getPassword()));
					response = client.execute(post);
					code = response.getStatusLine().getStatusCode();
					if (code >= 400) {
						throw new RuntimeException(
								"Could not retrieve access token for user: "
										+ oauthDetails.getUsername());
					}
				}

			}
			JsonObject responseObj = handleJsonResponse(response);
			accessToken = responseObj.getString(OAuthConstants.ACCESS_TOKEN);
            refreshToken = responseObj.getString(OAuthConstants.REFRESH_TOKEN);
			log.debug("Got Access Token: " + accessToken);
			log.debug("Got Refresh Token: " + refreshToken);
		}catch (Exception e) {
            log.debug("Error getting access token: " + e.getMessage());
        }

        OAuth2Details newOAuthDetails = new OAuth2Details();
        newOAuthDetails.setAccessToken(accessToken);
        newOAuthDetails.setRefreshToken(refreshToken);
		return newOAuthDetails;
	}

	/**
	 * Extracts JSON from body of an HTTP response
	 * @param response the HTTP response to process
	 * @return a JsonObject based on body of response
	 */
	public static JsonObject handleJsonResponse(HttpResponse response) {
		JsonObject oauthLoginResponse = null;
		String contentType = response.getEntity().getContentType().getValue();
        try {
            oauthLoginResponse = (JsonObject) Json.createReader(new StringReader(EntityUtils.toString(response.getEntity()))).readObject();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
		log.debug("********** Response Received **********");
		log.debug(oauthLoginResponse.toString());
		return oauthLoginResponse;
	}


	/**
	 * Generates OAuth access token HTTP Authorization header
	 * @param accessToken the access token to include in header
	 * @return the header value
	 */
	public static String getAuthorizationHeaderForAccessToken(String accessToken) {
		return OAuthConstants.BEARER + " " + accessToken;
	}

	/**
	 * Generate HTTP BASIC header value
	 * @param username the username to include in header
	 * @param password the password to include in header
	 * @return the header value
	 */
	public static String getBasicAuthorizationHeader(String username, String password) {
		return OAuthConstants.BASIC + " "
				+ encodeCredentials(username, password);
	}

	/**
	 * Encodes username and password of HTTP BASIC header
	 * @param username username to encode
	 * @param password password to encode
	 * @return the encoded credentials
	 */
	public static String encodeCredentials(String username, String password) {
		String cred = username + ":" + password;
		String encodedValue = null;
		byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
		encodedValue = new String(encodedBytes);
		log.debug("encodedBytes " + new String(encodedBytes));

		byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
		log.debug("decodedBytes " + new String(decodedBytes));

		return encodedValue;

	}

	/**
	 * Verifies a string is not null and non-empty
	 * @param str the string to check
	 * @return true if valid, false otherwise
	 */
	public static boolean isValid(String str) {
		return (str != null && str.trim().length() > 0);
	}

}
