package featureide.fm.io.guidsl;

import featureide.fm.model.FeatureModel;

public class ModelCheckFailure {
	public static enum FailureType {
		SATFailure, GuidslFailure
	}

	private FeatureModel model;
	private String errorMsg;
	private FailureType type;

	/**
	 * 
	 */
	public ModelCheckFailure() {
		super();
	}

	/**
	 * @return the model
	 */
	public FeatureModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            the model to set
	 */
	public void setModel(FeatureModel model) {
		this.model = model;
	}

	/**
	 * @return the errorMsg
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * @param errorMsg
	 *            the errorMsg to set
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return the type
	 */
	public FailureType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(FailureType type) {
		this.type = type;
	}

}
