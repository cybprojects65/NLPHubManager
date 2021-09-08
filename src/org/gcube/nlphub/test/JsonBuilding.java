package org.gcube.nlphub.test;

import java.util.List;

import org.gcube.nlphub.algorithms.JsonBuilder;
import org.gcube.nlphub.infra.Algorithm;
import org.gcube.nlphub.infra.InfraInfoRetrieval;
import org.gcube.nlphub.infra.NLPFileManager;

public class JsonBuilding {

	
	public static void main(String[ ] args) throws Exception{
		String language = "Italian";
		List<String> annotations = null;
		InfraInfoRetrieval infraRetrieval = new InfraInfoRetrieval();
		
		NLPFileManager nlp = new NLPFileManager(infraRetrieval, language, annotations);
		nlp.extractAlgorithms();
		List<Algorithm> algorithms = nlp.algorithms;
		System.out.println(JsonBuilder.toJson(algorithms));

	}
}
