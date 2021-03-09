package tourGuide.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

import org.json.JSONException;
import org.json.JSONObject;

public class HTTPRequestService {
	/**
	 * Constructor
	 */
	public HTTPRequestService() {
	}

	/**
	 * Api GET request
	 * 
	 * @param url
	 * @param urlParams
	 * @return JSON api response
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject getReq(String url, Map<String, String> urlParams) throws IOException, JSONException {
		StringBuilder urlWithParams = new StringBuilder();
		urlWithParams.append(url).append(this.getURLParamsString(urlParams));
		URL reqUrl = new URL(urlWithParams.toString());
		HttpURLConnection con = (HttpURLConnection) reqUrl.openConnection();
		con.setRequestMethod("GET");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setConnectTimeout(20000);
		con.setReadTimeout(20000);
		String res = this.getResponse(con);
		con.disconnect();
		return new JSONObject(res);
	}

	/**
	 * Api POST request with a form
	 * 
	 * @param url
	 * @param formParams
	 * @return JSON api response
	 * @throws IOException
	 * @throws JSONException
	 */
	public JSONObject postFormReq(String url, Map<String, String> formParams) throws IOException, JSONException {
		StringJoiner sj = new StringJoiner("&");
		for (Map.Entry<String, String> entry : formParams.entrySet())
			sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);

		URL reqUrl = new URL(url);
		HttpURLConnection con = (HttpURLConnection) reqUrl.openConnection();
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setConnectTimeout(20000);
		con.setReadTimeout(20000);
		con.setFixedLengthStreamingMode(out.length);
		con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		con.connect();
		try (OutputStream os = con.getOutputStream()) {
			os.write(out);
		}
		String res = this.getResponse(con);
		con.disconnect();
		return new JSONObject(res);
	}

	/**
	 * Set URL parameters
	 * 
	 * @param params
	 * @return URL parameters
	 * @throws UnsupportedEncodingException
	 */
	private static String getURLParamsString(Map<String, String> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();

		if (params != null && params.size() >= 1) {
			result.append("?");
			for (Map.Entry<String, String> entry : params.entrySet()) {
				result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
				result.append("&");
			}
		}

		String resultString = result.toString();
		return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
	}

	/**
	 * Parse request response into JSON
	 * 
	 * @param con
	 * @return JSON
	 * @throws IOException
	 */
	private static String getResponse(HttpURLConnection con) throws IOException {
		StringBuilder res = new StringBuilder();
		res.append("{");
		res.append("\"status\" :").append("\"").append(con.getResponseCode()).append("\",");
		res.append("\"message\" :").append("\"").append(con.getResponseMessage()).append("\",");
		res.append("\"content\" :");
		try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				res.append(inputLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		res.append("}");

		return res.toString();
	}
}