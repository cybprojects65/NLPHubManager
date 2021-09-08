package org.gcube.nlphub.management;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gcube.nlphub.algorithms.Annotator;
import org.gcube.nlphub.algorithms.EntitiesParser;
import org.gcube.nlphub.algorithms.JsonBuilder;
import org.gcube.nlphub.algorithms.TextPreprocessor;
import org.gcube.nlphub.infra.Algorithm;
import org.gcube.nlphub.infra.InfraInfoRetrieval;
import org.gcube.nlphub.infra.InvokeDataMinerViaPost;
import org.gcube.nlphub.infra.NLPFileManager;

public class NLPAlgorithmsManager {

	public static void main(String[] args) throws Exception {
		//String language = "Italian";
		String language = "English";
		List<String> annotations = new ArrayList<>();
		annotations.add("Person");
	    annotations.add("Geopolitical");
		annotations.add("Keyword");
		annotations.add("Sentence");
		annotations.add("Artefact");
		annotations.add("Token");
		File textFile = new File("sampletext_italian.txt");
		NLPAlgorithmsManager manager = new NLPAlgorithmsManager(language, annotations);
		manager.run(textFile);
		
		/*
		ConcurrentHashMap <String,File> jsonOutputs = new ConcurrentHashMap <String,File>();
		jsonOutputs.put("A1", new File("F0f7e2a99-0e82-4641-9561-831668062f23.txt"));
		jsonOutputs.put("A2", new File("F1d420e60-f64d-41c7-94a1-ee1ffce031a2.txt"));
		jsonOutputs.put("A3", new File("F2605571e-c95e-4f4a-b54e-61324a273bbc.txt"));
		jsonOutputs.put("A4", new File("Fd88e20af-51b8-4cb7-b8a8-e98263114a99.txt"));
		jsonOutputs.put("A5", new File("Fe3fc01c3-7ca9-4448-bb3a-b85cb70bfdfb.txt"));
		jsonOutputs.put("A6", new File("F60b47030-7f68-4207-8331-3a504b188865.txt"));
		
		manager.parseOutput(jsonOutputs, TextPreprocessor.preprocess(textFile));
		*/
	}

	public String language;
	public List<String> annotations;
	InfraInfoRetrieval infraRetrieval;
	public static int numberOfThreadsToUse = 15;
	public final ConcurrentHashMap <String,File> jsonOutputs = new ConcurrentHashMap <String,File>();
	public File output = new File("output.json");
	public File outputAnnotation = new File("annotation.txt");
	
	
	public void setOutputJson(File output) {
		this.output=output;
	}
	
	public void setOutputAnnotation(File annotation) {
		this.outputAnnotation=annotation;
	}
	
	public synchronized void addOutput(String algorithmID , File file) {
		jsonOutputs.put(algorithmID, file);
	}
	
	public synchronized int getProcessedOutput() {
		return jsonOutputs.size();
	}
	
	private class DMThread implements Callable<Integer> {

		InvokeDataMinerViaPost poster;
		String id;
		String parameterName;
		String text;
		Algorithm a;
		
		public DMThread(String dataMinerURL, String token, Algorithm a, String text) {
			poster = new InvokeDataMinerViaPost(infraRetrieval.dataMinerURL, infraRetrieval.token);
			this.id = a.id;
			this.parameterName = a.parameterName;
			this.text = text;
			this.a=a;
		}

		public Integer call() {
			long t0 = System.currentTimeMillis();

			try {
				List<String> annotations = null;
				if (a.supportsAnnotationList)
					annotations = a.annotations;
				
				File output = poster.run(id, parameterName, text, annotations);
				
				addOutput(id,output);

			} catch (Exception e) {
				System.out.println("Exception in algorithm " + id + " :" + e.getMessage());
				e.printStackTrace();
				addOutput(id,new File("void"));
			}
			long t1 = System.currentTimeMillis();
			System.out.println("Algorithm " + id + " finished in " + (t1 - t0) + " ms");

			return 0;
		}
	}

	public NLPAlgorithmsManager(String language, List<String> annotations) throws Exception {
		this.language = language;
		this.annotations = annotations;
		infraRetrieval = new InfraInfoRetrieval();
	}

	public void run(File textFile) throws Exception {
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreadsToUse);
		try {
			NLPFileManager nlp = new NLPFileManager(infraRetrieval, language, annotations);
			nlp.extractAlgorithms();
			List<Algorithm> algorithms = nlp.algorithms;
			String text = TextPreprocessor.preprocess(textFile);
			
			PrintWriter p = new PrintWriter("status.txt"); p.println("50");p.close();
			
			for (Algorithm algorithm : algorithms) {
				System.out.println("Running " + algorithm.id);

				// File output = poster.run(algorithm.id,
				// algorithm.parameterName, text);
				DMThread thread = new DMThread(infraRetrieval.dataMinerURL, infraRetrieval.token, algorithm, text);
				executorService.submit(thread);
				//thread.call();

				// jsonOutputs.add(output);
			}
			
			p = new PrintWriter("status.txt"); p.println("60");p.close();
			
			int nAlgorithms = algorithms.size();
			float step = (89f-60f)/(float)nAlgorithms;
			
			while (getProcessedOutput() < nAlgorithms) {
				System.out.println("Waiting for algorithms to finish");
				int status = (int)(getProcessedOutput()*step)+60;
				p = new PrintWriter("status.txt"); p.println(status);p.close();
				Thread.sleep(5000);
			}

			p = new PrintWriter("status.txt"); p.println("90");p.close();
			
			System.out.println("All algorithms finished");

			parseOutput(jsonOutputs, text);
			
		} catch (Exception e) {
			throw e;
		} finally {
			executorService.shutdown();
			
			System.out.println("Deleting aux files");
			for (File output:jsonOutputs.values()) {
				if (output!=null)
					output.delete();
			}
		}
	}

	
	public void parseOutput(Map <String,File> jsonOutputs, String text) throws Exception{
		EntitiesParser parser = new EntitiesParser(annotations);

		parser.parseAll(jsonOutputs);
		
		LinkedHashMap<String, List<int[]>> entities = parser.globalEntities;
		LinkedHashMap<String, List<int[]>> filteredentities = new LinkedHashMap<String, List<int[]>>();
		
		
		for (String annotation : annotations) {
			List<int[]> intervs = entities.get(annotation);
			if (intervs!=null)
				filteredentities.put(annotation, intervs);
		}
		
		parser.globalEntities=filteredentities;
		parser.visualiseMergedEntities();
		
		String overalljson = JsonBuilder.toJson(TextPreprocessor.escapeForJson(text), parser);
		
		
		//FileWriter fw = new FileWriter(output);
		
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		
		fw.write(overalljson);
		fw.flush();
		fw.close();
		
		List<String> algorithms = new ArrayList<String>();
		algorithms.addAll(parser.entitiesRegistry.keySet());
		
		new Annotator(annotations).annotate(output, outputAnnotation, algorithms);
		
	}
}
