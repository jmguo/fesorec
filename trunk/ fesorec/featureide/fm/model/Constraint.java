package featureide.fm.model;

import org.prop4j.Node;

import featureide.fm.util.ConstraintHelper;

/**
 * Our representation of a constraint. Our constraint has two type: require &
 * exclude. This is different from the original one defined in Feature IDE's
 * feature model.
 */
public class Constraint {

	public static enum ConstraintType {
		require, exclude
	}

	private String featureA;
	private String featureB;
	private ConstraintType constraintType;

	private Node propNode;

	/**
	 * @param featureA
	 * @param featureB
	 * @param constraintType
	 */
	public Constraint(String featureA, String featureB,
			ConstraintType constraintType) {
		super();
		this.featureA = featureA;
		this.featureB = featureB;
		this.constraintType = constraintType;
	}

	/**
	 * @return the featureA
	 */
	public String getFeatureA() {
		return featureA;
	}

	/**
	 * @param featureA
	 *            the featureA to set
	 */
	public void setFeatureA(String featureA) {
		this.featureA = featureA;
		if (propNode != null)
			propNode = ConstraintHelper.createNode(this);
	}

	/**
	 * @return the featureB
	 */
	public String getFeatureB() {
		return featureB;
	}

	/**
	 * @param featureB
	 *            the featureB to set
	 */
	public void setFeatureB(String featureB) {
		this.featureB = featureB;
		if (propNode != null)
			propNode = ConstraintHelper.createNode(this);
	}

	/**
	 * @return the constraintType
	 */
	public ConstraintType getConstraintType() {
		return constraintType;
	}

	/**
	 * @param constraintType
	 *            the constraintType to set
	 */
	public void setConstraintType(ConstraintType constraintType) {
		this.constraintType = constraintType;
		if (propNode != null)
			propNode = ConstraintHelper.createNode(this);
	}

	/**
	 * @return the propNode
	 */
	public Node getPropNode() {
		if (propNode == null)
			propNode = ConstraintHelper.createNode(this);
		return propNode;
	}

	/**
	 * @param propNode
	 *            the propNode to set
	 */
	public void setPropNode(Node propNode) {
		this.propNode = propNode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(featureA).append(" ").append(constraintType).append(" ")
				.append(featureB);
		return builder.toString();
	}

	public String toString(String[] symbols) {
		return getPropNode().toString(symbols);
	}

	@Override
	public Constraint clone() {
		return new Constraint(featureA, featureB, constraintType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((constraintType == null) ? 0 : constraintType.hashCode());
		result = prime * result
				+ ((featureA == null) ? 0 : featureA.hashCode());
		result = prime * result
				+ ((featureB == null) ? 0 : featureB.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Constraint other = (Constraint) obj;
		if (constraintType == null) {
			if (other.constraintType != null)
				return false;
		} else if (!constraintType.equals(other.constraintType))
			return false;
		if (featureA == null) {
			if (other.featureA != null)
				return false;
		} else if (!featureA.equals(other.featureA))
			return false;
		if (featureB == null) {
			if (other.featureB != null)
				return false;
		} else if (!featureB.equals(other.featureB))
			return false;
		return true;
	}
}
