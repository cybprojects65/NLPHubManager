package org.gcube.nlphub.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gcube.nlphub.infra.Algorithm;

public class JsonBuilder {
	
	public static String listToJson(List<String> list) {
		return list.toString().replace("[","[\"").replace("]","\"]").replace(",","\",\"").replaceAll(" +", "");
	}
	public static String toJson(List<Algorithm> algorithms) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("{\"algorithms\":[");
		int i = 0;
		int nalgor = algorithms.size();
		List<String> allAnnotations = new ArrayList<String>();
		
		for (Algorithm a:algorithms) {
			
			sb.append("{\""+a.id+"\":{");
			sb.append("\"name\":\""+a.name+"\", ");
			sb.append("\"type\":\""+a.type+"\", ");
			sb.append("\"annotations\":"+listToJson(a.annotations)+", ");
			sb.append("\"language\":\""+a.language+"\", ");
			sb.append("\"outputTypeDescription\":\""+a.outputTypeDescription+"\", ");
			sb.append("\"outputType\":\""+a.outputType+"\", ");
			sb.append("\"status\":\""+a.status+"\", ");
			String inputs = "\""+a.parameterName+"\"";
			if (a.supportsAnnotationList)
				inputs += ",\"annotationsList\"";
			sb.append("\"inputParameters\":["+inputs+"]");
			
			sb.append("}}");
			if (i<nalgor-1)
				sb.append(",");
			
			for(String annotation:a.annotations) {
				String ann = annotation.trim();
				if (!allAnnotations.contains(ann))
					allAnnotations.add(ann);
			}
			i++;
		}
		sb.append("],");

		/*
		Set<String> hs = new HashSet<String>();
		hs.addAll(allAnnotations);
		allAnnotations.clear();
		allAnnotations.addAll(hs);
		*/
		sb.append("\"all_NER_annotations\":");
		sb.append(listToJson(allAnnotations));
		sb.append("}");
		return sb.toString();
	}
	
	
	public static String toJson(String text, EntitiesParser parser) {
		StringBuffer body = new StringBuffer();
		
		String globals = "\"MERGED\": {"+entitiesToJson(parser.globalEntities)+"}";
		body.append(globals);
		
		int j = 0;
		int algs = parser.entitiesRegistry.keySet().size();
		
		if (algs > 0) {
			body.append(",");
		}
		for (String algorithm: parser.entitiesRegistry.keySet()) {
			String subjson= "\""+algorithm+"\": {"+entitiesToJson(parser.entitiesRegistry.get(algorithm))+"}";
			body.append(subjson);
			if (j<(algs-1))
				body.append(",");	
			j++;
		}
		
		String t = "{ \"text\": \""+text+"\" , "+body.toString() +"}";
		
		return t;
		
	}
	
	public static String entitiesToJson(Map<String, List<int[]>> entities) {
		
		int j = 0;
		int ents = entities.keySet().size();
		StringBuffer t = new StringBuffer();
		t.append("\"entities\": {");
		for (String entity: entities.keySet()) {
			
			List<int[]> indices = entities.get(entity);
			if (indices == null) continue;
			
			int i = 0;
			int nidx = indices.size();
			t.append("\""+entity+"\":[ ");
			for (int[] idx:indices) {
				t.append("{\"indices\": ["+idx[0]+", "+idx[1]+"]}");
				if (i< nidx-1)
					t.append(",");
				i++;
			}
			t.append("]");
			if (j<(ents-1)) {
				t.append(", ");
			}
			j++;
		}
		t.append("}");
		
		return t.toString();
	}
	
}
