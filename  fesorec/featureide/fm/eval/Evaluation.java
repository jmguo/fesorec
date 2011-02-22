package featureide.fm.eval;

public interface Evaluation {
	public static enum EvaluationType {
		FeatureNumber, EditKinds, EditNumber, ES
	}

	public static String rootPath = "E:\\research_develop\\SPL\\Experiments\\";

	public String getName();

	public EvaluationSpec[] createSpecs();

	public void doEvaluate();
}
