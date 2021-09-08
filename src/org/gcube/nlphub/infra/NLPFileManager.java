package org.gcube.nlphub.infra;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NLPFileManager {

	public static String metadataFileURL = "http://data.d4science.org/bnp4UDNyb21lSURkQXdDUnlSS0JkVWgzWk1KMUxWVGZHbWJQNStIS0N6Yz0";
	public List<Algorithm> algorithms = new ArrayList<Algorithm>();
	public InfraInfoRetrieval infraRetrieval;
	public String DMcapabilities;
	public String language;
	public List<String> annotations;

	public NLPFileManager(InfraInfoRetrieval infraRetrieval, String language, List<String> annotations) throws Exception {
		this.infraRetrieval = infraRetrieval;
		this.language = language;
		this.annotations = annotations;

		System.out.println("Retrieving DM capabilities");
		this.DMcapabilities = infraRetrieval.getDataMinerCapabilities();
	}

	public NLPFileManager(InfraInfoRetrieval infraRetrieval, String language) throws Exception {
		this.infraRetrieval = infraRetrieval;
		this.language = language;
		this.annotations = null;

		System.out.println("Retrieving DM capabilities");
		this.DMcapabilities = infraRetrieval.getDataMinerCapabilities();
	}
	
	public void extractAlgorithms() throws Exception {

		URL url = new URL(metadataFileURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		if (connection.getResponseCode() == 200) {
			InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
			BufferedReader br = new BufferedReader(streamReader);
			Stream<String> lines = br.lines();
			lines.forEach(s -> {
				try {
					parseLine(s);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		System.out.println("Algorithms:\n" + algorithms);
	}

	public static List<String> parseCVSString(String row, String delimiter) throws Exception {

		List<String> elements = new ArrayList<String>();
		String phrase = row;
		int idxdelim = -1;
		boolean quot = false;
		phrase = phrase.trim();
		while ((idxdelim = phrase.indexOf(delimiter)) >= 0) {
			quot = phrase.startsWith("\"");
			if (quot) {
				phrase = phrase.substring(1);
				String quoted = "";
				if (phrase.startsWith("\""))
					phrase = phrase.substring(1);
				else {
					Pattern regexp = Pattern.compile("[^\\\\]\"");
					
					boolean matching = Pattern.matches("[^\\\\]\"",phrase);

					if (matching) {
						int i0 = regexp.matcher(phrase).start();
						quoted = phrase.substring(0, i0 + 1).trim();
						phrase = phrase.substring(i0 + 2).trim();
					}
				}

				if (phrase.startsWith(delimiter))
					phrase = phrase.substring(1);

				elements.add(quoted);

			} else {
				elements.add(phrase.substring(0, idxdelim));
				phrase = phrase.substring(idxdelim + 1).trim();
			}
		}
		if (phrase.startsWith("\""))
			phrase = phrase.substring(1);

		if (phrase.endsWith("\""))
			phrase = phrase.substring(0, phrase.length() - 1);

		elements.add(phrase);

		return elements;
	}

	public void parseLine(String s) throws Exception {

		List<String> elements = parseCVSString(s, ",");

		String status = (elements.get(7));
		String language = elements.get(4);
		String annotationsS = elements.get(3);

		if (status.equals("OK") && language.equalsIgnoreCase(this.language)) {

			String[] annotationArray = annotationsS.split(",");
			boolean compliant = false;
			for (String annot : annotationArray) {
				if (annotations==null || annotations.contains(annot.trim())) {
					compliant = true;
					break;
				}
			}

			if (compliant) {
				String id = elements.get(2);

				if (DMcapabilities.contains(id)) {
					System.out.println("Analysing Algorithm " + id);
					String inputName = infraRetrieval.getDataMinerProcessDescription(id);
					
					boolean supportsList = inputName.contains("annotationsList");
					String toSearch = "<Input minOccurs=\"1\" maxOccurs=\"1\">";
					inputName = inputName.substring(inputName.indexOf(toSearch) + toSearch.length());
					toSearch = "<ows:Identifier>";
					inputName = inputName.substring(inputName.indexOf(toSearch) + toSearch.length());
					inputName = inputName.substring(0, inputName.indexOf("</ows:"));
					System.out.println("Input Name " + inputName);

					Algorithm algo = new Algorithm();
					algo.name = elements.get(0);
					algo.type = elements.get(1);
					algo.id = id;
					algo.annotations = Arrays.asList(annotationArray);
					algo.language = language;
					algo.outputTypeDescription = elements.get(5);
					algo.outputType = elements.get(6);
					algo.status = status;
					algo.parameterName = inputName;
					algo.supportsAnnotationList = supportsList;
					
					algorithms.add(algo);

				}
			}
		}
	}

}
