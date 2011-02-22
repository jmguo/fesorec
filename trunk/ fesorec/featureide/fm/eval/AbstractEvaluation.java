package featureide.fm.eval;

import featureide.fm.util.TimedWoker;
import featureide.fm.util.XStreamUtil;

public abstract class AbstractEvaluation implements Evaluation {

	@Override
	public void doEvaluate() {
		System.out.println("Running " + getName() + "...");
		TimedWoker worker = new TimedWoker() {
			public void excute() {
				EvaluationReport[] reports = FMEvaluationRunner
						.evaluate(AbstractEvaluation.this);
				for (EvaluationReport report : reports) {
					XStreamUtil.save(report);
				}
			}
		};
		worker.run();
		System.out.println(worker.getStopWatch().getElapsedTimeString());
	}

}
