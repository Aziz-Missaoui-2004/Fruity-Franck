package frank.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameConfig {
	private final Map<String, String> values = new HashMap<>();

	public GameConfig(String path) {
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				int eq = line.indexOf('=');
				if (eq < 0)
					continue;
				String key = line.substring(0, eq).trim();
				String value = line.substring(eq + 1).trim();
				values.put(key, value);
			}
		} catch (IOException e) {
			System.err.println("[GameConfig] impossible de lire " + path + " — valeurs par défaut utilisées. ("
					+ e.getMessage() + ")");

		}
	}
	/** Renvoie un entier, ou la valeur par défaut si la clé est absente/invalide. */
	public int getInt(String key, int defaultValue) {
		String v = values.get(key);
		if (v == null)
			return defaultValue;
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return defaultValue;
		}

	}
	/** Renvoie un double, ou la valeur par défaut si la clé est absente/invalide. */
	public Double getDouble(String key, Double defaultValue) {
		String v = values.get(key);
		if (v == null) {
			return defaultValue;
		}
		try {
			return Double.parseDouble(v); 
		} catch (NumberFormatException e) {
			return defaultValue;
		}

	}
	/** Renvoie une chaîne, ou la valeur par défaut. */
	public String getString(String key, String defaultValue) {
	    return values.getOrDefault(key, defaultValue);
	}

}
