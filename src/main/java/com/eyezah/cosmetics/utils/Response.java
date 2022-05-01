package com.eyezah.cosmetics.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;

public class Response implements Closeable {
	private Response(CloseableHttpClient client, CloseableHttpResponse response) {
		this.client = client;
		this.response = response;
		this.status = this.response.getStatusLine();
	}
	
	private final CloseableHttpClient client;
	private final CloseableHttpResponse response;
	private final StatusLine status;

	public StatusLine getStatus() {
		return this.status;
	}

	public int getStatusCode() {
		return this.status.getStatusCode();
	}

	public OptionalInt getError() {
		int code = this.getStatusCode();

		if (code >= 200 && code < 300) {
			return OptionalInt.empty();
		} else {
			return OptionalInt.of(code);
		}
	}

	public HttpEntity getEntity() {
		return this.response.getEntity();
	}

	public String getAsString() throws IOException {
		return EntityUtils.toString(this.getEntity(), StandardCharsets.UTF_8);
	}

	public JsonObject getAsJson() throws IOException, JsonParseException {
		String s = EntityUtils.toString(this.getEntity(), StandardCharsets.UTF_8).trim();

		try {
			return PARSER.parse(s).getAsJsonObject();
		} catch (JsonParseException e) {
			Debug.info("Error parsing json: " + s);
			throw e;
		}
	}

	public static Response request(String request) throws ParseException, IOException {
		Response response = requestLenient(request);
		if (response.getError().isPresent()) throw new IOException("Error trying to request " + request + ": " + response.getError().getAsInt());
		return response;
	}

	public static Response requestLenient(String request) throws ParseException, IOException {
		int timeout = 15 * 1000;

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(timeout)
				.setConnectTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();

		CloseableHttpClient client = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.build();

		final HttpGet get = new HttpGet(request);

		CloseableHttpResponse response = client.execute(get);
		return new Response(client, response);
	}

	private static final JsonParser PARSER = new JsonParser();

	@Override
	public void close() throws IOException {
		this.response.close();
		this.client.close();
	}
}
