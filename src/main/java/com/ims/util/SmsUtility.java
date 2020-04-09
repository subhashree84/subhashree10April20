package com.ims.util;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;

public class SmsUtility {
	public void sendSms(String message, List<String> contactNmuber,String smsUrl,String smsSecretappkey) throws IOException {
		JSONObject messageBody = new JSONObject();
		messageBody.put("message", message);
		messageBody.put("phoneNumber", contactNmuber);
		CloseableHttpClient httpClient1 = HttpClientBuilder.create().build();
		try {
			HttpPost request = new HttpPost(smsUrl);
			StringEntity params = new StringEntity(messageBody.toString());
			request.addHeader("content-type", "application/json");
			request.addHeader("secretappkey", smsSecretappkey);
			request.setEntity(params);
			httpClient1.execute(request);
		} catch (Exception ex) {
		} finally {
			httpClient1.close();
		}

	}

}
