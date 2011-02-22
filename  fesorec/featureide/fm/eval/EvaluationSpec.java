package featureide.fm.eval;

import featureide.fm.model.EvolutionStrategy;

public class EvaluationSpec {

	public static enum ModelGenType {
		CreateNotSave, CreateAndSave, Load
	}

	public static enum OperationType {
		AllAdd, AllRemove, AllSet, Random;

		public String toString() {
			if (this == Random) {
				return "Arbitrary";
			} else {
				return name().substring(3);
			}
		}
	}

	private int modelNum;
	private int operationNum;
	private int repeateNum;
	private int startId;

	private ModelGenType modelGenType;
	private OperationType operationType;
	private EvolutionStrategy evolutionStrategy;

	private String modelDir;
	private String reportDir;

	private boolean doPostCheck;

	/**
	 * @return the doPostCheck
	 */
	public boolean isDoPostCheck() {
		return doPostCheck;
	}

	/**
	 * @param doPostCheck
	 *            the doPostCheck to set
	 */
	public void setDoPostCheck(boolean doPostCheck) {
		this.doPostCheck = doPostCheck;
	}

	/**
	 * @return the reportDir
	 */
	public String getReportDir() {
		return reportDir;
	}

	/**
	 * @param reportDir
	 *            the reportDir to set
	 */
	public void setReportDir(String reportDir) {
		this.reportDir = reportDir;
	}

	/**
	 * @return the modelDir
	 */
	public String getModelDir() {
		return modelDir;
	}

	/**
	 * @param modelDir
	 *            the modelDir to set
	 */
	public void setModelDir(String modelDir) {
		this.modelDir = modelDir;
	}

	/**
	 * @return the modelNum
	 */
	public int getModelNum() {
		return modelNum;
	}

	/**
	 * @param modelNum
	 *            the modelNum to set
	 */
	public void setModelNum(int modelNum) {
		this.modelNum = modelNum;
	}

	/**
	 * @return the editNum
	 */
	public int getOperationNum() {
		return operationNum;
	}

	/**
	 * @param editNum
	 *            the editNum to set
	 */
	public void setOperation(int operationNum) {
		this.operationNum = operationNum;
	}

	/**
	 * @return the repeateNum
	 */
	public int getRepeateNum() {
		return repeateNum;
	}

	/**
	 * @param repeateNum
	 *            the repeateNum to set
	 */
	public void setRepeateNum(int repeateNum) {
		this.repeateNum = repeateNum;
	}

	/**
	 * @return the startId
	 */
	public int getStartId() {
		return startId;
	}

	/**
	 * @param startId
	 *            the startId to set
	 */
	public void setStartId(int startId) {
		this.startId = startId;
	}

	/**
	 * @return the modelGenType
	 */
	public ModelGenType getModelGenType() {
		return modelGenType;
	}

	/**
	 * @param modelGenType
	 *            the modelGenType to set
	 */
	public void setModelGenType(ModelGenType modelGenType) {
		this.modelGenType = modelGenType;
	}

	/**
	 * @return the editGenType
	 */
	public OperationType getOperationType() {
		return operationType;
	}

	/**
	 * @param operationType
	 *            the editGenType to set
	 */
	public void setOperationType(OperationType operationType) {
		this.operationType = operationType;
	}

	/**
	 * @return the evolutionStrategy
	 */
	public EvolutionStrategy getEvolutionStrategy() {
		return evolutionStrategy;
	}

	/**
	 * @param evolutionStrategy
	 *            the evolutionStrategy to set
	 */
	public void setEvolutionStrategy(EvolutionStrategy evolutionStrategy) {
		this.evolutionStrategy = evolutionStrategy;
	}

	public EvaluationSpec clone() {
		EvaluationSpec spec = new EvaluationSpec();
		spec.modelNum = modelNum;
		spec.operationNum = operationNum;
		spec.repeateNum = repeateNum;
		spec.startId = startId;
		spec.modelGenType = modelGenType;
		spec.operationType = operationType;
		spec.modelDir = modelDir;
		return spec;
	}

}
