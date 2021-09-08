package org.gcube.nlphub.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

public class HttpRequest {
	
	public static String getPage(String endpoint, String requestParameters) {
		String result = null;
		if (endpoint.startsWith("http://")) {
			// Send a GET request to the servlet
			try {
				// Send data
				String urlStr = endpoint;
				if (requestParameters != null && requestParameters.length() > 0) {
					urlStr += "?" + requestParameters;
				}
				URL url = new URL(urlStr);
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(120000);
				conn.setReadTimeout(120000);

				// Get the response
				BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
				rd.close();
				result = sb.toString();
			} catch (Exception e) {
				System.out.println("error sending get request"+e.getMessage());
			}
		}
		return result;
	}

	
	public static int checkUrl(String url, final String username, final String password) {
		int checkConn = -1;
		try {
			if ((username != null) && (password != null)) {
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password.toCharArray());
					}

				});
			}

			URL checkurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) checkurl.openConnection();
			checkConn = conn.getResponseCode();
			conn.disconnect();
		} catch (Exception e) {
			System.out.println("error in url "+e.getLocalizedMessage());
		}
		return checkConn;
	}

	public static void downloadFile(String fileurl, String localFile) throws Exception {
		URL smpFile = new URL(fileurl);
		URLConnection uc = (URLConnection) smpFile.openConnection();
		InputStream is = uc.getInputStream();
		System.out.println("Retrieving from " + fileurl + " to :" + localFile);
		inputStreamToFile(is, localFile);
		is.close();
		is = null;
		System.gc();
	}

	public static void inputStreamToFile(InputStream is, String path) throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(new File(path));
		byte buf[] = new byte[1024];
		int len = 0;
		while ((len = is.read(buf)) > 0)
			out.write(buf, 0, len);
		out.close();
	}


}