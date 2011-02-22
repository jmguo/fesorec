package featureide.fm.eval;

import java.io.File;
import java.util.Map.Entry;

import featureide.fm.generator.ModelGenerator;
import featureide.fm.io.guidsl.FeatureModelReader;
import featureide.fm.io.guidsl.FeatureModelWriter;
import featureide.fm.model.FeatureModel;

public class EvaluationModelFactory {

	public static ModelGenerator modelGenerator;
	public static String modelPath = "Models\\";

	static {
		init();
	}

	public static void init() {
		modelGenerator = ModelGenerator.getInstance();
	}

	public static EvaluationModel generateModels(EvaluationSpec spec) {
		switch (spec.getModelGenType()) {
		case CreateNotSave:
			return generateModelsNotSave(spec);
		case CreateAndSave:
			return generateModelAndSave(spec);
		case Load:
			return loadFeatureModels(spec);
		}
		return null;
	}

	private static EvaluationModel generateModelsNotSave(EvaluationSpec spec) {
		EvaluationModel evaluationModel = new EvaluationModel();

		for (int i = 0; i < spec.getRepeateNum(); i++) {
			int id = i + spec.getStartId();
			FeatureModel model = modelGenerator.generateFeatureModel(id, spec);

			evaluationModel.addFeatureModel(String.valueOf(id), model);

		}

		return evaluationModel;
	}

	private static EvaluationModel generateModelAndSave(EvaluationSpec spec) {
		EvaluationModel evaluationModel = generateModelsNotSave(spec);

		FeatureModelWriter writer = new FeatureModelWriter(null);
		File dir = new File(Evaluation.rootPath + modelPath
				+ spec.getModelDir() + spec.getModelNum());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		for (Entry<String, FeatureModel> entry : evaluationModel.getModelMap()
				.entrySet()) {
			String id = entry.getKey();
			FeatureModel model = entry.getValue();

			writer.setFeatureModel(model);
			String filepath = "FM-" + id + ".m";
			writer.writeToFile(new File(dir, filepath));

			evaluationModel.addFeatureModel(String.valueOf(id), model);
		}

		return evaluationModel;
	}

	private static EvaluationModel loadFeatureModels(EvaluationSpec spec) {
		EvaluationModel evaluationModel = new EvaluationModel();

		FeatureModelReader reader = new FeatureModelReader(null);
		ModelGenerator modelGenerator = ModelGenerator.getInstance();

		File dir = new File(Evaluation.rootPath + modelPath
				+ spec.getModelDir() + spec.getModelNum());
		if (!dir.exists())
			throw new RuntimeException("The directory \"" + spec.getModelDir()
					+ "\" not exists! Load Models failed.");
		for (int i = 0; i < spec.getRepeateNum(); i++) {
			int id = i + spec.getStartId();

			String filepath = "FM-" + id + ".m";
			File file = new File(dir, filepath);
			FeatureModel fm = new FeatureModel();
			reader.setFeatureModel(fm);
			try {
				reader.readFromFile(file);
			} catch (Exception e) {
				e.printStackTrace();
				fm = modelGenerator.generateFeatureModel(id, spec);
			}
			evaluationModel.addFeatureModel(String.valueOf(id), fm);
		}

		return evaluationModel;
	}

}
