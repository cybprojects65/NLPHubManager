package org.gcube.nlphub.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gcube.nlphub.algorithms.EntitiesParser;
import org.gcube.nlphub.algorithms.JsonBuilder;
import org.gcube.nlphub.algorithms.JsonMapper;
import org.gcube.nlphub.algorithms.Merger;
import org.gcube.nlphub.algorithms.TextPreprocessor;

public class JsonReading {

	public static void main(String[] args) throws Exception {
		//File f1 = new File("./json_tests/1530089366224_11f5f71c-900e-499f-b2be-614ff98a42a3_ILCoutput.json");
		File [] files = new File("./json_tests/").listFiles();
		EntitiesParser parser = new EntitiesParser(null);
		
		LinkedHashMap<String, File> jsonFiles = new LinkedHashMap<String,File>();
		int i = 0;
		
		for (File file:files) {
			jsonFiles.put("A"+i, file);
			i++;
		}
		parser.parseAll(jsonFiles);
		
		parser.visualiseMergedEntities();
		
		System.out.println(parser.entitiesRegistry);
		
		JsonBuilder builder = new JsonBuilder();
		String text = "Abbiamo scelto la rotta più breve, quella che passa vicino alle coste dell'Africa. Tutti i modelli meteo sono d'accordo, puntiamo su una rotta poco comune, che non ho mai fatto, ma che dovrebbe funzionare bene\". Ci hanno pensato su quasi 48 ore Giovanni Soldini e il suo equipaggio, prima di decidere la strategia e quindi la rotta per affrontare il più velocemente possibile la risalita dell'oceano Atlantico, dopo il passaggio di capo di Buona Speranza. In barca a vela non è detto che andare dritto per dritto sul traguardo sia la rotta più rapida, e qui in particolare, in Atlantico, per risalire verso nord dalle latitudini meridionali, la rotta abituale punta a ovest, verso le coste del Brasile, per poi stringere verso oriente, verso le Canarie e poi lo stretto di Gibilterra. E' la rotta per aggirare i cosiddetti doldrums, zone di mancanza di vento, vicine alle latitudini subtropicali e all'Equatore, all'altezza dei deserti terrestri, chiamate anche le \"latitudini dei cavalli\" perché leggenda vuole che i grandi bastimenti spagnoli buttassero in mare gli animali vivi trasportati, per risparmiare acqua e cibo nella bonaccia e alleggerire il carico. E Soldini? Ha deciso di percorrere proprio quelle acque insidiose, convinto però che riuscirà a trovare correnti e refoli che lo spingeranno veloci per superare l'Equatore e le sue calme, distanti circa 1.000 miglia.";
		
		System.out.println(builder.toJson(TextPreprocessor.escapeForJson(text), parser));
		
	}

	
}
