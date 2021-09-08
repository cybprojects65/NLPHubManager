package org.gcube.nlphub.infra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.gcube.nlphub.utils.HttpRequest;

public class InvokeDataMinerViaPost {

	static String dataMinerProcessUrl = "http://#DM#/wps/WebProcessingService?request=Execute&service=WPS&Version=1.0.0";
	//static String token = "3a8e6a79-1ae0-413f-9121-0d59e5f2cea2-843339462";
	
	String dataMinerUrl = "";
	String token = "";
	int refreshTime = 1000;
	
	public InvokeDataMinerViaPost(String dataMinerURL, String token) {
		this.dataMinerUrl = dataMinerURL;
		this.token=token;
	}
	
	private static void pipe(Reader reader, Writer writer) throws IOException {
		char[] buf = new char[1024];
		int read = 0;
		while ((read = reader.read(buf)) >= 0) {
			writer.write(buf, 0, read);
		}
		writer.flush();
	}
	
	public static void postData(Reader data, URL endpoint, Writer output) throws Exception {
		HttpURLConnection urlc = null;
		try {
			urlc = (HttpURLConnection) endpoint.openConnection();
			try {
				urlc.setRequestMethod("POST");
			} catch (ProtocolException e) {
				throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
			}
			urlc.setDoOutput(true);
			urlc.setDoInput(true);
			urlc.setUseCaches(false);
			urlc.setAllowUserInteraction(false);
			urlc.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");

			OutputStream out = urlc.getOutputStream();

			try {
				Writer writer = new OutputStreamWriter(out, "UTF-8");
				pipe(data, writer);
				writer.close();
			} catch (IOException e) {
				throw new Exception("IOException while posting data", e);
			} finally {
				if (out != null)
					out.close();
			}

			InputStream in = urlc.getInputStream();
			try {
				Reader reader = new InputStreamReader(in);
				pipe(reader, output);
				reader.close();
			} catch (IOException e) {
				throw new Exception("IOException while reading response", e);
			} finally {
				if (in != null)
					in.close();
			}

		} catch (IOException e) {
			throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
		} finally {
			if (urlc != null)
				urlc.disconnect();
		}
	}
	
	public static String getStatus(String endpoint) {
		String result = null;
		
			// Send a GET request to the servlet
			try {
				// Send data
				String urlStr = endpoint;
				
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
				e.printStackTrace();
			}
		
		return result;
	}
	

	
	
	public File run(String algorithm, String parameter, String text, List<String> annotations) throws Exception{
		
		File template = null;
		String annotationsList = "";
		
		if (annotations==null)
			template = new File("AlgorithmsTemplate.txt");
		else {
			template = new File("AlgorithmsTemplateAnnotationsList.txt");
			annotationsList = annotations.toString().replace("[", "").replace("]", "").replace(",", "|").replaceAll(" +", "");
			System.out.println("Annotation List required: "+annotationsList+" to "+algorithm);
		}
		byte[] encoded = Files.readAllBytes(Paths.get(template.getPath()));
		String content = new String(encoded, "UTF-8");
		
		content = content.replace("#ID#", algorithm);
		content = content.replace("#PARAMETERID#", parameter);
		content = content.replace("#CONTENT#", text);
		content = content.replace("#ANNOTATIONS#", annotationsList);
		
		File tempFile = new File(algorithm.substring(algorithm.lastIndexOf(".")+1)+"_"+UUID.randomUUID()+".txt");
		
		BufferedWriter bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
		bw1.write(content);
		bw1.flush();
		bw1.close();
		
		
		StringWriter sw = new StringWriter();
		FileReader fr = new FileReader(tempFile);
		
		postData(fr , new URL(dataMinerProcessUrl.replace("#DM#",dataMinerUrl)+"&gcube-token="+token), sw);
		
		fr.close();
		
		String answer = sw.toString();
		
		String statusLocation = answer.substring(answer.indexOf("statusLocation=\"")+"statusLocation=\"".length(), answer.indexOf("\">")); 
		
		//System.out.println(sw.toString());
		System.out.println("Algorithm "+algorithm+" managed by machine "+statusLocation);
		
		String status = getStatus(statusLocation+"&gcube-token="+token);
		
		while (!(status.contains("wps:ProcessSucceeded") || status.contains("wps:ProcessFailed"))){
			//System.out.println(status);
			status = getStatus(statusLocation+"&gcube-token="+token);
			Thread.sleep(refreshTime);
		}
		
		
		//System.out.println(status);
		tempFile.delete();
		
		if (status.contains("wps:ProcessFailed")) {
			System.out.println("Process Failed!");
			throw new Exception("Process failed");
		}
		else{
			String UrlToOutput = status.substring(status.lastIndexOf("<d4science:Data>")+"<d4science:Data>".length(), status.lastIndexOf("</d4science:Data>"));
			System.out.println(algorithm+" - Url to output:"+UrlToOutput);
			tempFile = new File("F"+UUID.randomUUID()+".txt");
			HttpRequest.downloadFile(UrlToOutput, tempFile.getAbsolutePath());
			return tempFile;
		}
	}
	
	
}
