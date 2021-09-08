package org.gcube.nlphub.algorithms;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntitiesParser {

	public LinkedHashMap<String, List<int[]>> globalEntities = new LinkedHashMap<String, List<int[]>>();
	public LinkedHashMap<String, List<int[]>> entities = new LinkedHashMap<String, List<int[]>>();
	public LinkedHashMap<String, LinkedHashMap<String, List<int[]>>> entitiesRegistry = new LinkedHashMap<String, LinkedHashMap<String, List<int[]>>>();

	boolean entitiesState = false;
	boolean entityState = false;
	boolean indicesState = false;
	String currentKey = "";
	int indexCount = 0;
	List<String> annotations;
	
	public EntitiesParser(List<String> annotations) {
		this.annotations = annotations;
	}
	
	public LinkedHashMap<String, List<int[]>> getParsedEntities() {
		return entities;
	}

	public LinkedHashMap<String, List<int[]>> getParsedGlobalEntities() {
		return globalEntities;
	}

	public void parse(File jsonFile) throws Exception {
		parse(jsonFile, false);
	}

	public void filterEntities() {
		LinkedHashMap<String, List<int[]>> newentities = new LinkedHashMap<String, List<int[]>>();
		
		for (String entity:entities.keySet()) {
			if (annotations==null)
				newentities.put(entity, entities.get(entity));
			else if (annotations.contains(entity)){
				newentities.put(entity, entities.get(entity));
			}
		}
		entities = newentities;
		
	}
	
	public String adjustJson(String json) {
		String json2 = json.replace(",,", ",");
		json2 = json.replace(",}", "}");
		return json2;
	}
	public void parse(File jsonFile, boolean visualise) throws Exception {

		byte[] encoded = Files.readAllBytes(Paths.get(jsonFile.getPath()));
		String json = new String(encoded, "UTF-8");
		String json2 = adjustJson(json);
		
		System.out.println("Parsing output - "+jsonFile.getName());
		
		if ((json2.length() != json.length())) {
			System.out.println("Remanaging file "+jsonFile.getName());
			FileWriter fw = new FileWriter(jsonFile);
			fw.write(json2);
			fw.flush();
			fw.close();
			json=json2;
		}
		
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		try {
			System.out.println("File "+jsonFile.getName()+" \n"+json);
			map = JsonMapper.parse(json);
		}catch(Exception e) {
			System.out.println("Unparsable Json file");
			e.printStackTrace();
		}
		
		initParser();
		populateMap(map);
		filterEntities();
		enrichGlobalMap();
		
		if (visualise) {
			System.out.println("Local entities");
			visualiseEntities();
			System.out.println("Global entities");
			visualiseMergedEntities();
		}
	}

	public void parseAll(File[] jsonFiles) throws Exception {
		for (File jsonFile : jsonFiles) {
			if (jsonFile != null)
				parse(jsonFile);
		}
	}

	public void parseAll(Map<String, File> jsonFiles) throws Exception {

		for (String id : jsonFiles.keySet()) {
			File file = jsonFiles.get(id);
			if (file != null && file.exists()) {
				parse(file);
				int esize = entities.size(); 
				if (esize>0) {
					System.out.println("ID "+id+" found "+entities.size()+" entities");
					entitiesRegistry.put(id, entities);
				}else
					System.out.println("ID "+id+" found NO entity");
			}
		}
	}

	public void visualiseEntities() {
		for (String entity : entities.keySet()) {
			System.out.println(entity);
			List<int[]> intervals = entities.get(entity);
			if (intervals != null) {
				for (int[] interval : intervals) {
					System.out.println("\t" + interval[0] + "," + interval[1]);
				}
			}
		}
	}

	public void visualiseMergedEntities() {
		for (String entity : globalEntities.keySet()) {
			//System.out.println(entity);
			List<int[]> intervals = globalEntities.get(entity);
			if (intervals != null) {
				System.out.println("N. of "+entity+":"+intervals.size());
				//for (int[] interval : intervals) {
					//System.out.println("\t" + interval[0] + "," + interval[1]);
				//}
			}
		}
	}

	public static void visualiseMap(LinkedHashMap<String, Object> map) {

		for (String key : map.keySet()) {
			// System.out.println("Key: " + key);
			Object value = map.get(key);
			if (value instanceof Map<?, ?>) {
				LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;
				// System.out.println("Value map ...");
				visualiseMap(valueMap);
			} else if (value instanceof String) {
				String valueMap = (String) value;
				// System.out.println("Value string: " + valueMap);
			}
		}
	}

	public void initParser() {
		entities = new LinkedHashMap<String, List<int[]>>();
		entitiesState = false;
		entityState = false;
		indicesState = false;
		currentKey = "";
		indexCount = 0;
	}

	public void enrichGlobalMap() {

		for (String entity : entities.keySet()) {
			List<int[]> globalindices = globalEntities.get(entity);
			if (globalindices == null)
				globalindices = new ArrayList<int[]>();

			List<int[]> indices = entities.get(entity);
			Merger merger = new Merger();
			List<int[]> allindices = new ArrayList<int[]>();
			allindices.addAll(globalindices);
			allindices.addAll(indices);
			allindices = merger.mergeAll(allindices);
			if (allindices.size()>0)
				globalEntities.put(entity, allindices);

		}
	}

	public void populateMap(LinkedHashMap<String, Object> map) {

		for (String key : map.keySet()) {
			//System.out.println("Key: " + key);

			Object value = map.get(key);

			if (value instanceof Map<?, ?>) {
				LinkedHashMap<String, Object> valueMap = (LinkedHashMap<String, Object>) value;

				if (key.equalsIgnoreCase("entities")) {
					// System.out.println("entities state");
					entitiesState = true;
					populateMap(valueMap);
					entitiesState = false;
					
				} else if (key.startsWith("indices")) {
					//System.out.println("indices state "+key);
					indicesState = true;
					//System.out.println(valueMap);
					populateMap(valueMap);
					
				} else if (entitiesState && !indicesState) {
					//System.out.println("entity " + key + " state");
					entityState = true;
					currentKey = key;
					indexCount = 1;
					populateMap(valueMap);
					// System.out.println("OFF indices state");
					entityState = false;
					indicesState = false;
					if (entities.get(currentKey)==null || entities.get(currentKey).size()==0)
						entities.remove(currentKey);
				} else {
					//System.out.println("other state");
					populateMap(valueMap);
				}

			} else if (value instanceof String) {
				String valueS = (String) value;
				// System.out.println("Value string: " + valueS);

				if (indicesState) {
					// System.out.println("Collecting idx: " + valueS);
					List<int[]> indices = entities.get(currentKey);
					if (indices == null) {
						indices = new ArrayList<int[]>();
					}

					if (indices.size() < indexCount) {
						int[] idx = { Integer.parseInt(valueS), -1 };
						indices.add(idx);
					} else {
						int[] idx = indices.get(indexCount - 1);
						idx[1] = Integer.parseInt(valueS);
						indices.set(indexCount - 1, idx);
						indexCount++;
						indicesState = false;
					}
					entities.put(currentKey, indices);

				}
			}
		}
	}

}
