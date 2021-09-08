package org.gcube.nlphub.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NLPHubCaller {

	
	static String dataMinerProcessUrl = "http://#DM#/wps/WebProcessingService?request=Execute&service=WPS&Version=1.0.0";
	String dataMinerUrl = "";
	String token = "";
	int refreshTime = 1000;
	File outputJsonFile;
	File outputAnnotationFile;
	
	public static void main(String [] args) throws Exception{
		String dataMinerURL = "dataminer-prototypes.d4science.org"; //url of the DM in the RPrototypingLab VRE
		String token = "3a8e6a79-1ae0-413f-9121-0d59e5f2cea2-843339462"; //Token of the RPrototypingLab VRE
		NLPHubCaller caller = new NLPHubCaller(dataMinerURL, token);
		File textFile = new File("sampletext.txt");
		//get complete lists of keywords per language at 
		//https://services.d4science.org/group/rprototypinglab/data-miner?OperatorId=org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.NLPHUB_INFO
		List<String> annotations = new ArrayList<String>();
		//annotations.add("Keyword");
		annotations.add("Person");
		annotations.add("Location");
		
		String language = "it";
		
		caller.run(language, textFile, annotations);
		System.out.println("JSON output is: "+caller.getOutputJsonFile());
		System.out.println("Annotated text is: "+caller.getOutputAnnotationFile());
	}
	
	
	public File getOutputJsonFile() {
		return outputJsonFile;
	}
	
	public File getOutputAnnotationFile() {
		return outputAnnotationFile;
	}
	
	public NLPHubCaller(String dataMinerURL, String token) {
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
	

	
	
	public void run(String language, File textFile, List<String> annotations) throws Exception{
		System.out.println("NLPHUB client has started");
		File template = null;
		String annotationsList = "";
		
			template = new File("NLPHubTemplate.txt");
			annotationsList = annotations.toString().replace("[", "").replace("]", "").replace(",", "|").replaceAll(" +", "");
			System.out.println("Annotation List requested: "+annotationsList);
		byte[] encoded = Files.readAllBytes(Paths.get(template.getPath()));
		String content = new String(encoded, "UTF-8");
		System.out.println("Preprocessing text");
		String text = preprocess(textFile);

		content = content.replace("#LANGUAGE#", language);
		content = content.replace("#CONTENT#", text);
		content = content.replace("#ANNOTATIONS#", annotationsList);
		
		File tempFile = new File("NLPHUB_"+UUID.randomUUID()+".txt");
		FileWriter fw = new FileWriter(tempFile);
		fw.write(content);
		fw.close();
		
		StringWriter sw = new StringWriter();
		FileReader fr = new FileReader(tempFile);
		
		System.out.println("Running...");
		long t0 = System.currentTimeMillis();
		postData(fr , new URL(dataMinerProcessUrl.replace("#DM#",dataMinerUrl)+"&gcube-token="+token), sw);
		
		fr.close();
		
		String answer = sw.toString();
		
		String statusLocation = answer.substring(answer.indexOf("statusLocation=\"")+"statusLocation=\"".length(), answer.indexOf("\">")); 
		
		//System.out.println(sw.toString());
		//System.out.println(statusLocation);
		
		String status = getStatus(statusLocation+"&gcube-token="+token);
		
		while (!(status.contains("wps:ProcessSucceeded") || status.contains("wps:ProcessFailed"))){
			//System.out.println(status);
			status = getStatus(statusLocation+"&gcube-token="+token);
			Thread.sleep(refreshTime);
		}
		long t1 = System.currentTimeMillis();
		
		System.out.println("Finished in "+(t1-t0)+" ms");
		
		//System.out.println(status);
		tempFile.delete();
		
		if (status.contains("wps:ProcessFailed")) {
			System.out.println("Process Failed!");
			throw new Exception("Process failed");
		}
		else{
			status = status.substring(status.indexOf("</d4science:Data>")+"</d4science:Data>".length()+1);
			String UrlToJSON = status.substring(status.indexOf("<d4science:Data>")+"<d4science:Data>".length(), status.indexOf("</d4science:Data>"));
			System.out.println("NLPHub - Url to json output:"+UrlToJSON);
			outputJsonFile = new File("Output_"+UUID.randomUUID()+".json");
			downloadFile(UrlToJSON, outputJsonFile.getAbsolutePath());
			
			status = status.substring(status.indexOf("</d4science:Data>")+"</d4science:Data>".length()+1);
			String UrlToAnnotation = status.substring(status.lastIndexOf("<d4science:Data>")+"<d4science:Data>".length(), status.lastIndexOf("</d4science:Data>"));
			System.out.println("NLPHub - Url to annotation:"+UrlToAnnotation);
			outputAnnotationFile = new File("Annotations_"+UUID.randomUUID()+".txt");
			downloadFile(UrlToAnnotation, outputAnnotationFile.getAbsolutePath());
			
		}
	}
	
	public static void downloadFile(String fileurl, String localFile) throws Exception {
		URL smpFile = new URL(fileurl);
		URLConnection uc = (URLConnection) smpFile.openConnection();
		InputStream is = uc.getInputStream();
		//System.out.println("Retrieving from " + fileurl + " to :" + localFile);
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
	
	public static String preprocess(File textFile) throws Exception {
		try {
		byte[] encoded = Files.readAllBytes(Paths.get(textFile.getPath()));
		String content = new String(encoded, "UTF-8");
		content = cleanCharacters(content);
		return content;
		}catch (Exception e) {
			System.out.println("Eror while reading file "+e.getMessage());
			throw e;
		}
	}
	
public static String cleanCharacters(String source) {
		
		char c = 0;
		for (int i = 0; i < source.length(); i++) {
			c = source.charAt(i);
			if (!((c >= 33 && c <= 90) || (c >= 97 && c <= 122) || (c >= 128 && c <= 167) || (c >= 180 && c <= 183) || (c >= 210 && c <= 212) || (c >= 214 && c <= 216) || (c >= 224 && c<=255))) {
				source = source.replace(source.substring(i, i + 1), " ");
			}
		}
		
		source = source.replaceAll("[\\s]+", " ").trim();
		source = source.replaceAll("<", " ").trim();
		source = source.replaceAll(">", " ").trim();
		return source;
	}
}
