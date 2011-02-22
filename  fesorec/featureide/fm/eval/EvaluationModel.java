package featureide.fm.eval;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import featureide.fm.model.FeatureModel;

public class EvaluationModel {
	private Map<String, FeatureModel> modelMap = new LinkedHashMap<String, FeatureModel>();

	public void addFeatureModel(String id, FeatureModel model) {
		modelMap.put(id, model);
	}

	public Collection<FeatureModel> getModels() {
		return modelMap.values();
	}

	public Map<String, FeatureModel> getModelMap() {
		return modelMap;
	}

	public FeatureModel getModel(String id) {
		return modelMap.get(id);
	}

	public void clear() {
		modelMap.clear();
	}
}
