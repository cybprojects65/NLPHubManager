package org.gcube.nlphub.infra;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.gcube.nlphub.utils.HttpRequest;

public class InfraInfoRetrieval {

		public static String informationURL = "https://registry.d4science.org/icproxy/gcube/service//ServiceEndpoint/DataAnalysis/DataMiner?gcube-token=";
		public static String getCapabilitiesURL = "/wps/WebProcessingService?Request=GetCapabilities&Service=WPS&gcube-token=";
		public static String describeProcessURL = "/wps/WebProcessingService?Request=DescribeProcess&Service=WPS&Version=1.0.0&Identifier=#ID#&gcube-token=";
			
		public String token;
		public String username;
		public String VRE;
		public String dataMinerURL;
		
		public InfraInfoRetrieval() throws Exception{
			List<String> lines = Files.readAllLines(new File("./globalvariables.csv").toPath());
			System.out.println("List of files "+Arrays.toString(new File("./").listFiles()));
			for (String line:lines) {
				line = line.replace("\"", "");
				if (line.startsWith("gcube_username")){
					username = line.substring(line.indexOf(",")+1);
				}
				else if (line.startsWith("gcube_context")){
					VRE = line.substring(line.indexOf(",")+1);
				}
				else if (line.startsWith("gcube_token")){
					token = line.substring(line.indexOf(",")+1);
				}
			}
			System.out.println("info: "+username+":"+VRE+":"+token);
			System.out.println("Retrieving DM URL");
			long t0 = System.currentTimeMillis();
			dataMinerURL = getDataMinerURL();
			long t1 = System.currentTimeMillis();
			System.out.println("Retrieved in "+(t1-t0)+" ms");
		}
		
		public String getDataMinerURL() throws Exception{
			
			File DM = downloadFile(informationURL+token);
			String found = retrieveDMLink(DM);
			System.out.println("DM url "+found);
			DM.delete();
			return found;
			
		}
			
		public String getDataMinerCapabilities() throws Exception{
			
			File capa = downloadFile("https://"+dataMinerURL+getCapabilitiesURL+token);
			String lines = new String(Files.readAllBytes(capa.toPath()), "UTF-8");
			capa.delete();
			return lines;
		}

		public String getDataMinerProcessDescription(String ID) throws Exception{
			File describe = downloadFile("https://"+dataMinerURL+describeProcessURL.replace("#ID#", ID)+token);
			String lines = new String(Files.readAllBytes(describe.toPath()), "UTF-8");
			describe.delete();
			return lines;
		}
		
		public static String retrieveDMLink(File file) throws Exception{
			String lines = new String(Files.readAllBytes(file.toPath()), "UTF-8");
			lines = lines.substring(lines.indexOf("<HostedOn>")+10,lines.indexOf("</HostedOn>"));
			return lines;
		}
		
		public static File downloadFile(String url) throws Exception{
			
			/*
			URL infoXML = new URL(url);
			File file = new File ("is_"+UUID.randomUUID()+".txt");
			
			ReadableByteChannel rbc = Channels.newChannel(infoXML.openStream());
			FileOutputStream fos = new FileOutputStream(file);
			
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			rbc.close();
			*/
			File file = new File ("is_"+UUID.randomUUID()+".txt");
			HttpRequest.downloadFile(url, file.getAbsolutePath());
			
			return file;
		}

	
}
