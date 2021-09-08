package org.gcube.nlphub.utils;

public class ISOtoLanguage {
		
	
	public static String Iso2Language(String language) {
		language = language.toLowerCase();
		if (language.equals("it"))
			return "Italian";
		else if (language.equals("de"))
			return "German";
		else if (language.equals("en"))
			return "English";
		else if (language.equals("fr"))
			return "French";
		else if (language.equals("es"))
			return "Spanish";
		else
			return "NotSupported";
	}
	
}
