package featureide.fm.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import featureide.fm.io.guidsl.ModelCheckFailure;

public class EvaluationReport {
	public static final String header = "MoNum\tEdNum\tEType\tRepeat\tAvgTime\tVariance";
	public static final String dirPath = "Reports\\";

	private EvaluationSpec spec;
	private List<Double> usedTimes;
	private List<Double> guidslTimes;
	private List<Double> satTimes;

	private double modelGenTime;
	private double averageTime;
	private double avgGuidslTime;
	private double avgSatTime;
	private double variance;
	private boolean generated;

	private List<ModelCheckFailure> satCheckFails = new ArrayList<ModelCheckFailure>();;
	private List<ModelCheckFailure> guidslCheckFails = new ArrayList<ModelCheckFailure>();;

	public EvaluationReport() {
		guidslTimes = new ArrayList<Double>();
		satTimes = new ArrayList<Double>();
	}

	public EvaluationReport(EvaluationSpec spec) {
		this.spec = spec;
		usedTimes = new ArrayList<Double>(spec.getRepeateNum());
		guidslTimes = new ArrayList<Double>(spec.getRepeateNum());
		satTimes = new ArrayList<Double>(spec.getRepeateNum());
	}

	public void addSatCheckFail(ModelCheckFailure fm) {
		satCheckFails.add(fm);
	}

	public void addGuildsCheckFail(ModelCheckFailure fm) {
		guidslCheckFails.add(fm);
	}

	public void addUsedTime(long nanoSeconds) {
		usedTimes.add(nanoSeconds / 1000000.0);
	}

	public void addGuidslTime(long nanoSeconds) {
		guidslTimes.add(nanoSeconds / 1000000.0);
	}

	public void addSatTime(long nanoSeconds) {
		satTimes.add(nanoSeconds / 1000000.0);
	}

	/**
	 * @return the guidslTimes
	 */
	public List<Double> getGuidslTimes() {
		return guidslTimes;
	}

	/**
	 * @return the satTimes
	 */
	public List<Double> getSatTimes() {
		return satTimes;
	}

	/**
	 * @return the satCheckFails
	 */
	public List<ModelCheckFailure> getSatCheckFails() {
		return satCheckFails;
	}

	/**
	 * @return the guidslCheckFails
	 */
	public List<ModelCheckFailure> getGuidslCheckFails() {
		return guidslCheckFails;
	}

	public String getName() {
		return String.format("report_%s_%05d_%03d_%s", spec.getOperationType()
				.toString(), spec.getModelNum(), spec.getOperationNum(), spec
				.getEvolutionStrategy());
	}

	/**
	 * @return the modelGenTime
	 */
	public double getModelGenTime() {
		return modelGenTime;
	}

	/**
	 * @param modelGenTime
	 *            the modelGenTime to set
	 */
	public void setModelGenTime(double modelGenTime) {
		this.modelGenTime = modelGenTime;
	}

	public void generate() {
		List<Double> newList = new ArrayList<Double>(usedTimes);
		Collections.sort(newList);
		newList.set(newList.size() - 1, newList.get(spec.getRepeateNum() / 2));
		double totalTime = 0.0;
		for (double time : newList) {
			totalTime += time;
		}
		averageTime = totalTime / spec.getRepeateNum();

		List<Double> times = newList;
		double squareTotal = 0;
		for (double time : times) {
			double difference = time - averageTime;
			squareTotal += difference * difference;
		}
		variance = squareTotal / spec.getRepeateNum();

		totalTime = 0.0;
		for (double time : guidslTimes) {
			totalTime += time;
		}
		avgGuidslTime = totalTime / spec.getRepeateNum();

		totalTime = 0.0;
		for (double time : satTimes) {
			totalTime += time;
		}
		avgSatTime = totalTime / spec.getRepeateNum();

		generated = true;
	}

	/**
	 * @return the spec
	 */
	public EvaluationSpec getSpec() {
		return spec;
	}

	/**
	 * @return the usedTimes, in milliseconds
	 */
	public List<Double> getUsedTimes() {
		return usedTimes;
	}

	/**
	 * @return the avrerageTime
	 */
	public double getAverageTime() {
		if (!generated)
			generate();
		return averageTime;
	}

	/**
	 * @return the avgGuidslTime
	 */
	public double getAvgGuidslTime() {
		return avgGuidslTime;
	}

	/**
	 * @return the avgSatTime
	 */
	public double getAvgSatTime() {
		return avgSatTime;
	}

	/**
	 * @return the variance
	 */
	public double getVariance() {
		if (!generated)
			generate();
		return variance;
	}

	@Override
	public String toString() {
		if (!generated)
			generate();
		// String s = "";
		// for (long time : usedTimes) {
		// s += time + "\t";
		// }
		// s += "\n";
		return String.format("%d\t%d\t%s\t%d\t%fms\t%f", spec.getModelNum(),
				spec.getOperationNum(), spec.getOperationType(), spec
						.getRepeateNum(), averageTime, variance);
	}
}
