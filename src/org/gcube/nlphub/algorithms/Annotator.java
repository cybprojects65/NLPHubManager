package org.gcube.nlphub.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gcube.nlphub.algorithms.Merger.SegmentsComparator;

public class Annotator {

	public static void main(String[] args) throws Exception {
		File json = new File("output.json");
		File annotation = new File("annotation.txt");
		List<String> algorithms = new ArrayList<String>();
		
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.ENGLISH_NER_CORENLP");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.ENGLISH_EVENTS_RECOGNITION_NER");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.OPEN_NLP_ENGLISH_PIPELINE");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.TAGME_ENGLISH_NER");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.ENGLISH_NAMED_ENTITY_RECOGNIZER");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.ANNIE_PLUS_MEASUREMENTS");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.ENGLISH_NAMED_ENTITY_RECOGNIZER_FOR_TWEETS");
		algorithms.add("org.gcube.dataanalysis.wps.statisticalmanager.synchserver.mappedclasses.transducerers.KEYWORDS_NER_ENGLISH");
		
		new Annotator(null).annotate(json, annotation, algorithms);
	}
	
	List<String> annotations;
	
	public Annotator(List<String> annotations) {
		this.annotations = annotations;
	}
	
	public void annotate(File json, File output) throws Exception{
		annotate(json,output,"MERGED",false);
	}
	
	public void annotate(File json, File output, List<String> algorithms) throws Exception{
		
		annotate(json,output);
		
		for (String alg:algorithms) {
			annotate(json,output,alg,true);
			
		}
	}
	
	public void annotate(File json, File output, String algorithm, boolean append) throws Exception {

		//String algorithm = "MERGED";

		EntitiesParser parser = new EntitiesParser(annotations);

		byte[] encoded = Files.readAllBytes(Paths.get(json.getPath()));
		String content = new String(encoded, "UTF-8");
		
			LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
			try {
				map = JsonMapper.parse(content);
			}catch(Exception e) {
				e.printStackTrace();
			}
			String text = "";
			for (String key : map.keySet()) {
				if (key.equals("text")) {
					text = (String) map.get(key);
				}
				if (key.equals(algorithm)) {
					Object value = map.get(key);

					if (value instanceof Map<?, ?>) {
						LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
						parser.populateMap(valueMap);
						break;
					}
				}
			}
		
			// System.out.println(parser.entities);

		LinkedHashMap<String, String> annotations = new LinkedHashMap<String, String>();
		//System.out.println("Algorithm "+algorithm);
		for (String key : parser.entities.keySet()) {
			//System.out.println("Entity "+key);
			int cursor = 0;
			String newText = "";
			List<int[]> intervals = parser.entities.get(key);
			
			Collections.sort(intervals,new Merger().new SegmentsComparator());
			
			for (int[] interval : intervals) {

				int i1 = interval[0];
				int i2 = interval[1];
				
				String subtext = "[" + text.substring(i1, i2) + "]";
				// newText.substring(0, i1) = text.substring(0, i1)
				//System.out.println("Interval "+i1+" , "+i2+" Cursor "+cursor+" Subtext:"+subtext);
				if (i1<cursor)i1=cursor;
				newText += text.substring(cursor, i1) + subtext;
				cursor = i2;
			}
			newText += "";
			int nt = text.length();
			if (cursor < text.length())
				newText += text.substring(cursor);

			annotations.put(key, newText);
			//System.out.println(newText);

		}

		//FileWriter fw = new FileWriter(output,append);
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output,append), "UTF-8"));
		
		fw.write("\n##" + algorithm.substring(algorithm.lastIndexOf(".")+1)+ "##\n");
		for (String key : annotations.keySet()) {
			fw.write("#" + key + "#:");
			fw.write(annotations.get(key));
			fw.write("\n");
		}
		
		fw.flush();
		fw.close();
	}

}
