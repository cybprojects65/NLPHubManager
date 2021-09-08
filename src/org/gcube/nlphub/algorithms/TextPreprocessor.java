package org.gcube.nlphub.algorithms;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TextPreprocessor {
	
	
	
	public static String preprocess(File textFile) throws Exception {
		try {
		System.out.println(Paths.get(textFile.getPath()));
		byte[] encoded = Files.readAllBytes(Paths.get(textFile.getPath()));
		String content = new String(encoded, "UTF-8");
		content = replaceDirtyCharacters(content);
		return content;
		}catch (Exception e) {
			System.out.println("Eror while reading file "+e.getMessage());
			throw e;
		}
	}

	
	public static String getLanguageRecognizerDigest(String content) {
		int minToken = 100;
		
		content = content.trim();

		String[] tokens = content.split("\\s");
		String digest = "";
		int len = (minToken <= tokens.length) ? minToken : tokens.length;
		for(int i=0; i<len; i++) {
			digest += tokens[i] + " ";
		}
		return escapeContent(digest.trim());
	}
	
	public static int countTokens(String content) {
		return content.split("\\s").length;
	}
	
	public static String escapeContent(String content) {
		content = content.replace("\\\\", " ");
		content = content.replace("\"", " ");
		content = content.replace(";", " ");
		content = content.replace("=", " ");
		return content;
	}
	
	public static String escapeForJson(String content) {
		
		content = content.replace("\\\\", " ");
		content = content.replace("\"", " ");
		content = content.replace(";", " ");
		content = content.replace("=", " ");
		content = content.replace("[", " ");
		content = content.replace("]", " ");
		content = content.replace("{", " ");
		content = content.replace("}", " ");
		
		return content;
	}
	
	public static String replaceDirtyCharacters(String source) {
		
		char c = 0;
		for (int i = 0; i < source.length(); i++) {
			c = source.charAt(i);
			if (!((c >= 33 && c <= 90) || (c >= 97 && c <= 122) || (c >= 128 && c <= 167) || (c >= 180 && c <= 183) || (c >= 210 && c <= 212) || (c >= 214 && c <= 216) || (c >= 224 && c<=255))) {
				source = source.replace(source.substring(i, i + 1), " ");
			}
		}
		
		source = source.replaceAll("[\\s]+", " ").trim();
		source = source.replaceAll("<", " ").trim();
		source = source.replaceAll(">", " ").trim();
		return source;
	}
	
	
}
