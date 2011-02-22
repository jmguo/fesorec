package featureide.fm.eval.experiments;

import featureide.fm.eval.AbstractEvaluation;
import featureide.fm.eval.EvaluationReport;
import featureide.fm.eval.EvaluationSpec;
import featureide.fm.eval.FMEvaluationRunner;
import featureide.fm.eval.EvaluationSpec.OperationType;
import featureide.fm.eval.EvaluationSpec.ModelGenType;
import featureide.fm.io.guidsl.ModelCheckFailure;
import featureide.fm.model.EvolutionStrategy;

public class ValidityEvaluation extends AbstractEvaluation {

	final static int[] editSizes = new int[] { 10, 20, 40, 60, 80, 100 };

	final static int modelSize = 1000;

	public OperationType operationType;

	public ValidityEvaluation(OperationType operationType) {
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
			spec.setModelDir("ValidityEvaluation\\");
			spec.setReportDir("ValidityEvaluation\\");
			spec.setModelNum(modelSize);
			spec.setEvolutionStrategy(EvolutionStrategy.Random);
			spec.setDoPostCheck(true);

			specs[i] = spec;
		}
		return specs;
	}

	@Override
	public void doEvaluate() {
		System.out.println("Running " + getName() + "...");
		final EvaluationReport[] reports;
		reports = FMEvaluationRunner.evaluate(this);

		for (EvaluationReport report : reports) {
			System.out.println("Total Feature Model Count: "
					+ report.getSpec().getRepeateNum());
			System.out.println("SAT check Failures: "
					+ report.getSatCheckFails().size());

			// for (ModelCheckFailure failure : report.getSatCheckFails()) {
			// System.out.println("Model:");
			// System.out.println(new FeatureModelWriter(failure.getModel())
			// .writeToString());
			// System.out.println("Actions");
			// for (EditAction action : failure.getActions()) {
			// System.out.println(action);
			// }
			// }

			System.out.println("Guidsl check Failures: "
					+ report.getGuidslCheckFails().size());

			for (ModelCheckFailure failure : report.getGuidslCheckFails()) {
				System.out.println("Error: " + failure.getErrorMsg());
				// System.out.println("Model:");
				// System.out.println(new FeatureModelWriter(failure.getModel())
				// .writeToString());
				// System.out.println("Actions");
				// for (EditAction action : failure.getActions()) {
				// System.out.println(action);
				// }
			}
		}

	}

	@Override
	public String getName() {
		return getClass().getSimpleName() + ": " + operationType;
	}

}
