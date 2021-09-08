package org.gcube.nlphub.sai;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.gcube.nlphub.management.NLPAlgorithmsManager;
import org.gcube.nlphub.utils.ISOtoLanguage;

public class NLPHubNER {

	
	public static void main(String[] args) throws Exception {

		String language = "it";
		//String annotations = "Person#Geopolitical#Keyword";
		//String annotations = "Person#Emoticon#Measurement";
		//String annotations = "Keyword";
		//String annotations = "Organization#Emoticon#Location#Address#Ratio#Token#Misc#Money#Number#Date#SpaceToken#Sentence#Token#Person#Ordinal#URL#Percent#Keyword#Time#Percentage#Measurement#Date#UserID#Duration#Event#Hashtag#Location";
		String annotations = "Keyword#Person#Location#Geopolitical#Organization";
		
		String textFile = "shortItaText.txt";
		//String textFile = "C:\\Users\\Gianpaolo Coro\\Desktop\\sample text\\italian-text.txt";
		if (args!=null && args.length>1) {
			language=args[0];
			annotations = args[1];
			textFile = args[2];
		}
		language = ISOtoLanguage.Iso2Language(language);
		
		System.out.println("Language "+language);
		System.out.println("Annotations "+annotations);
		System.out.println("textFile "+textFile);
		
		String[] annotationsS = annotations.split("#");
				
		List<String> annotationsList = Arrays.asList(annotationsS);
		File textFileF = new File(textFile);
		File output = new File("output.json");
		File outputAnnotation = new File("annotation.txt");
		
		NLPAlgorithmsManager manager = new NLPAlgorithmsManager(language, annotationsList);
		manager.setOutputJson(output);
		manager.setOutputAnnotation(outputAnnotation);
		
		long t0 = System.currentTimeMillis();
		manager.run(textFileF);
		long t1 = System.currentTimeMillis();
		
		System.out.println("Overall process finished in "+(t1-t0)+" ms");
	}
	
	
}
