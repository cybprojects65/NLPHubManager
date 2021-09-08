package org.gcube.nlphub.test;

import java.util.ArrayList;
import java.util.List;

import org.gcube.nlphub.infra.InfraInfoRetrieval;
import org.gcube.nlphub.infra.NLPFileManager;

public class RetrieveAlgorithms {

	public static void main(String[] args) throws Exception{
		String language = "Italian";
		List<String> annotations = new ArrayList<>();
		annotations.add("Person");
		
		InfraInfoRetrieval infraRetrieval = new InfraInfoRetrieval();
		
		NLPFileManager nlp = new NLPFileManager(infraRetrieval,language,annotations);
		nlp.extractAlgorithms();
		
	}
	
	
}
