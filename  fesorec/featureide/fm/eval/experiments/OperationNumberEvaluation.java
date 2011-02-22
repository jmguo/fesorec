package featureide.fm.eval.experiments;

import featureide.fm.eval.AbstractEvaluation;
import featureide.fm.eval.EvaluationSpec;
import featureide.fm.eval.EvaluationSpec.OperationType;
import featureide.fm.eval.EvaluationSpec.ModelGenType;
import featureide.fm.model.EvolutionStrategy;

public class OperationNumberEvaluation extends AbstractEvaluation {
	final static int[] editSizes = new int[] { 0, 5, 10, 20, 30, 40, 50, 60,
			70, 80, 90, 100 };

	final static int modelSize = 1000;

	public OperationType operationType;

	public OperationNumberEvaluation(OperationType operationType) {
		this.operationType = operationType;
	}

	public EvaluationSpec[] createSpecs() {
		EvaluationSpec[] specs = new EvaluationSpec[editSizes.length];
		for (int i = 0; i < specs.length; i++) {
			EvaluationSpec spec = new EvaluationSpec();
			spec.setStartId(100);
			spec.setRepeateNum(200);
			spec.setModelGenType(ModelGenType.CreateNotSave);
			spec.setOperation(editSizes[i]);
			spec.setOperationType(operationType);
			spec.setModelDir("EditNumbers\\");
			spec.setReportDir("EditNumbers\\");
			spec.setModelNum(modelSize);
			spec.setEvolutionStrategy(EvolutionStrategy.Random);

			specs[i] = spec;
		}
		return specs;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName() + ": " + operationType;
	}
}
