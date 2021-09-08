package org.gcube.nlphub.sai;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.List;

import org.gcube.nlphub.algorithms.JsonBuilder;
import org.gcube.nlphub.infra.Algorithm;
import org.gcube.nlphub.infra.InfraInfoRetrieval;
import org.gcube.nlphub.infra.NLPFileManager;
import org.gcube.nlphub.utils.ISOtoLanguage;

public class NLPInfo {

	
	public static void main(String[ ] args) throws Exception{
		
		String language = "en";
		if (args!=null && args.length>0)
			language = args[0];
		
		System.out.println("Received ISO language "+language);
		language = ISOtoLanguage.Iso2Language(language);
		System.out.println("Interpreted language name "+language);
		
		File output = new File ("info.json");
		
		List<String> annotations = null;
		InfraInfoRetrieval infraRetrieval = new InfraInfoRetrieval();
		
		NLPFileManager nlp = new NLPFileManager(infraRetrieval, language, annotations);
		nlp.extractAlgorithms();
		List<Algorithm> algorithms = nlp.algorithms;
		
		String algorithmsInfo = JsonBuilder.toJson(algorithms);
		
		System.out.println(algorithmsInfo);
		
		//fw = new FileWriter(output);
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"));
		
		fw.write(algorithmsInfo);
		fw.flush();
		fw.close();
	}
}
