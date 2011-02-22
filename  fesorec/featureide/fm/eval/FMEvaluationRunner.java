package featureide.fm.eval;

import featureide.fm.io.guidsl.FeatureModelChecker;
import featureide.fm.io.guidsl.ModelCheckFailure;
import featureide.fm.model.FeatureModel;
import featureide.fm.operations.OperationPerformer;
import featureide.fm.util.TimedWoker;

public class FMEvaluationRunner {

	public static EvaluationReport[] evaluate(Evaluation evaluation) {
		EvaluationSpec[] specs = evaluation.createSpecs();
		System.out.print("Start ready task now...");
		evaluate(specs[0]);
		System.out.println("Ready task finished...");
		EvaluationReport[] reports = new EvaluationReport[specs.length];
		for (int i = 0; i < specs.length; i++) {
			System.out.print("Runing " + (i + 1) + " Experiments...");
			reports[i] = evaluate(specs[i]);
			System.out.println("Done! GC and Sleep...");
			System.gc();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("\n" + EvaluationReport.header);
		for (EvaluationReport report : reports) {
			System.out.println(report);
		}
		return reports;
	}

	public static EvaluationReport evaluate(final EvaluationSpec spec) {

		final EvaluationReport report = new EvaluationReport(spec);

		System.out.print("Generating Models...");
		final EvaluationModel evaluationModel = EvaluationModelFactory
				.generateModels(spec);

		System.out.print("Running Evaluations...");
		for (final String id : evaluationModel.getModelMap().keySet()) {
			final FeatureModel fm = evaluationModel.getModel(id);
			final TimedWoker worker = new TimedWoker() {
				public void excute() {
					if (spec.getOperationNum() <= 0) {
						return;
					}
					OperationPerformer performer = new OperationPerformer(fm,
							spec);
					performer.performOperations();

				}
			};
			worker.run();
			report.addUsedTime(worker.getStopWatch().getElapsedTime());

			boolean clearModel = true;
			if (spec.isDoPostCheck()) {
				// System.out.println("Running Post Checking...");
				TimedWoker validaterA = new TimedWoker() {
					public void excute() {
						ModelCheckFailure failure = null;
						if ((failure = FeatureModelChecker.satCheck(fm)) != null) {
							report.addSatCheckFail(failure);
							String path = "failed_" + spec.getModelNum() + id
									+ ".m";
							FeatureModelChecker.saveFailedModel(path, fm);
						}
					}
				};
				validaterA.run();
				report.addSatTime(validaterA.getStopWatch().getElapsedTime());

				TimedWoker validaterB = new TimedWoker() {
					public void excute() {
						ModelCheckFailure failure = null;
						if ((failure = FeatureModelChecker.guidslCheck(fm)) != null) {
							report.addGuildsCheckFail(failure);
						}
					}
				};

				validaterB.run();
				report
						.addGuidslTime(validaterB.getStopWatch()
								.getElapsedTime());
				if (clearModel) {
					fm.clear();
				}

			}
		}

		return report;
	}

}
