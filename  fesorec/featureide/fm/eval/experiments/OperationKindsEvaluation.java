package featureide.fm.eval.experiments;

import featureide.fm.eval.AbstractEvaluation;
import featureide.fm.eval.EvaluationSpec;
import featureide.fm.eval.EvaluationSpec.ModelGenType;
import featureide.fm.eval.EvaluationSpec.OperationType;
import featureide.fm.model.EvolutionStrategy;

public class OperationKindsEvaluation extends AbstractEvaluation {

	final static int[] sizes = new int[] { 10, 50, 100, 200, 500, 1000, 2000,
			5000, 10000 };

	public OperationType operationType;

	public OperationKindsEvaluation(OperationType operationType) {
		this.operationType = operationType;
	}

	public EvaluationSpec[] createSpecs() {
		EvaluationSpec[] specs = new EvaluationSpec[sizes.length];
		for (int i = 0; i < specs.length; i++) {
			EvaluationSpec spec = new EvaluationSpec();
			spec.setStartId(100);
			spec.setRepeateNum(200);
			spec.setModelGenType(ModelGenType.CreateNotSave);
			spec.setOperation(12);
			spec.setOperationType(operationType);
			spec.setModelDir("EditKinds\\");
			spec.setReportDir("EditKinds\\");
			spec.setModelNum(sizes[i]);
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
