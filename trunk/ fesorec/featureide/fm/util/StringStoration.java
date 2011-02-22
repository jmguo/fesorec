package featureide.fm.util;

import java.util.HashMap;
import java.util.Map;

public class StringStoration {

	private static String[] compoundPool;
	private static String[] layerPool;
	public static int capacity = 10005;
	private static Map<String, String> map = new HashMap<String, String>();
	static {
		initStoration();
	}

	public static void initStoration() {
		compoundPool = new String[capacity];
		layerPool = new String[capacity];
		for (int i = 0; i < capacity; i++) {
			compoundPool[i] = "A" + i;
			layerPool[i] = "C" + i;
			map.put(layerPool[i], compoundPool[i]);
		}
	}

	public static String getCompoundName(int i) {
		return compoundPool[i];
	}

	public static String getLayerName(int i) {
		return layerPool[i];
	}

	public static String changeToCompoundName(String layerName) {
		return map.get(layerName);
	}

}
