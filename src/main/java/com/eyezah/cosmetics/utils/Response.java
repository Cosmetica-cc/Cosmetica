package com.eyezah.cosmetics.utils;

import java.io.IOException;
import java.util.OptionalInt;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Response {
	private Response(StatusLine status, HttpEntity entity) {
		this.status = status;
		this.entity = entity;
	}
	
	private final StatusLine status;
	private final HttpEntity entity;

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
		return this.entity;
	}

	public static Response request(String request) throws ParseException, IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			final HttpGet get = new HttpGet(request);

			try (CloseableHttpResponse response = client.execute(get)) {
				return new Response(response.getStatusLine(), response.getEntity());
			}
		}
	}
}
