package featureide.fm.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Utility {

	private static SimpleDateFormat myFmt = new SimpleDateFormat(
			"MM-dd.HH.mm.ss");

	private static Random random = new Random();

	public static String getDateString(Date date) {
		return myFmt.format(date);
	}

	public static String getRandomString(int length, Random random) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int c = random.nextInt(26) + 'a';
			builder.append((char) c);
		}
		return builder.toString();
	}

	public static String getNewFeatureName(boolean isAbstract) {
		String name = isAbstract ? "A" + getRandomString(4, random) : "C"
				+ getRandomString(4, random);
		return name;
	}

	/*
	 * public static Feature getRandomLayer(FeatureModel model, Random random,
	 * boolean safely) {
	 * 
	 * if (safely) { Feature[] layers = model.getAllLayers(); if (layers.length
	 * == 0) return null;
	 * 
	 * return layers[random.nextInt(layers.length)]; } else { int featureSize =
	 * model.getNumberOfFeatures(); Feature feature = null; while (feature ==
	 * null) { int rand = random.nextInt(featureSize); feature =
	 * model.getFeature(StringStoration.getLayerName(rand)); } return feature; }
	 * 
	 * }
	 * 
	 * public static Feature getRandomCompound(FeatureModel model, Random
	 * random, boolean safely) { if (safely) { Feature[] compounds =
	 * model.getAllCompounds(); if (compounds.length == 0) return null; return
	 * compounds[random.nextInt(compounds.length)]; } else { int featureSize =
	 * model.getNumberOfFeatures(); Feature feature = null; while (feature ==
	 * null) { int rand = random.nextInt(featureSize); feature =
	 * model.getFeature(StringStoration .getCompoundName(rand)); }
	 * 
	 * return feature; }
	 * 
	 * }
	 * 
	 * public static Feature getRandomFeature(FeatureModel model, Random random,
	 * boolean safely) { if (safely) { Feature[] features =
	 * model.getAllFeatures(); if (features.length == 0) return null; return
	 * features[random.nextInt(features.length)]; } else { int featureSize =
	 * model.getNumberOfFeatures(); Feature feature = null; while (feature ==
	 * null) { int rand = random.nextInt(featureSize); feature =
	 * model.getFeature(StringStoration .getCompoundName(rand)); if (feature ==
	 * null) { feature = model.getFeature(StringStoration .getLayerName(rand));
	 * } }
	 * 
	 * return feature; }
	 * 
	 * }
	 */
}
