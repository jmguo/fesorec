package featureide.fm.edits;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditAction {
	public static enum EditType {
		Add, Remove, Set
	}

	public static enum EditObjectType {
		Compound, Layer, FeatureGroup, Require, Exclude, Name, GroupType, Optionality, Cardinality
	}

	private EditType editType;
	private EditObjectType editObjectType;
	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	public EditAction() {
	}

	/**
	 * @param editType
	 * @param editObjectType
	 */
	public EditAction(EditType editType, EditObjectType editObjectType) {
		super();
		this.editType = editType;
		this.editObjectType = editObjectType;
	}

	/**
	 * @return the editType
	 */
	public EditType getEditType() {
		return editType;
	}

	/**
	 * @param editType
	 *            the editType to set
	 */
	public void setEditType(EditType editType) {
		this.editType = editType;
	}

	/**
	 * @return the editObjectType
	 */
	public EditObjectType getEditObjectType() {
		return editObjectType;
	}

	/**
	 * @param editObjectType
	 *            the editObjectType to set
	 */
	public void setEditObjectType(EditObjectType editObjectType) {
		this.editObjectType = editObjectType;
	}

	/**
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void addParameter(String name, Object obj) {
		this.parameters.put(name, obj);
	}

	public Object getParameter(String name) {
		return this.parameters.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EditAction [editObjectType=").append(editObjectType)
				.append(", editType=").append(editType).append(", parameters=")
				.append(parameters).append("]");
		return builder.toString();
	}

}
