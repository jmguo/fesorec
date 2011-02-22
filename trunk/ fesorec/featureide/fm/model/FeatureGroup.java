/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2010  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package featureide.fm.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * 
 * @author Administrator
 */
public class FeatureGroup implements PropertyChangeListener {
	public static final String GROUP_TYPE_ID = "GROUP_TYPE";
	public static final String CARDINALITY_ID = "CARDINALITY";

	public static enum FeatureGroupType {
		And, Alternative, Or, None;
		public static int toIntValue(FeatureGroupType en) {
			switch (en) {
			case And:
				return 0;
			case Alternative:
				return 1;
			case Or:
				return 2;
			case None:
				return -1;
			default:
				return -1;
			}
		}
	}

	private FeatureGroupType featureGroupType = FeatureGroupType.None;

	private Cardinality cardinality;

	private List<Feature> features;

	private Feature featureOwner;

	public FeatureGroup() {
	}

	public FeatureGroup(FeatureGroupType type, Feature feature) {
		featureGroupType = type;
		featureOwner = feature;
		features = featureOwner.getChildren();
		cardinality = new Cardinality(this);
	}

	public FeatureGroup(FeatureGroupType type, Feature feature, int minCard,
			int maxCard) {
		featureGroupType = type;
		featureOwner = feature;
		features = featureOwner.getChildren();
		cardinality = new Cardinality(this);
		switch (featureGroupType) {
		case Alternative:
			cardinality.setCardinality(1, 1);
			break;
		case Or:
			cardinality.setCardinality(minCard, maxCard);
		default:
			break;
		}
	}

	public void setChildren(List<Feature> children) {
		features = children;
	}

	/**
	 * @return the groupType
	 */
	public FeatureGroupType getGroupType() {
		return featureGroupType;
	}

	/**
	 * @param featureGroupType
	 *            the groupType to set
	 */
	public void setGroupType(FeatureGroupType featureGroupType) {
		this.featureGroupType = featureGroupType;
	}

	/**
	 * @return the features
	 */
	public List<Feature> getChildren() {
		return features;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String prop = event.getPropertyName();
		if (prop.equals(PropertyConstants.CHILDREN_CHANGED)) {
			Feature feature = (Feature) event.getSource();
			if (feature.isAbstract()) {
				if (feature.isAlternative()) {
					setGroupType(FeatureGroupType.Alternative);
					setCardinality(1, 1);
					return;
				}
				if (feature.isOr()) {
					setGroupType(FeatureGroupType.Or);
					setCardinality(cardinality.getMinCardinality(), cardinality
							.getMaxCardinality());
					return;
				}
			}
			setGroupType(FeatureGroupType.None);
		}
	}

	public String toString() {
		if (featureGroupType == FeatureGroupType.Alternative
				|| featureGroupType == FeatureGroupType.Or) {
			return featureGroupType.toString() + cardinality.toString();
		}
		return featureGroupType.toString();
	}

	/**
	 * @return the cardinality
	 */
	public Cardinality getCardinality() {
		checkGroupType();
		return cardinality;
	}

	/**
	 * @param cardinality
	 *            the cardinality to set
	 */
	public void setCardinality(Cardinality cardinality) {
		checkGroupType();
		this.cardinality.setCardinality(cardinality.getMinCardinality(),
				cardinality.getMaxCardinality());
	}

	public void setCardinality(int minCard, int maxCard) {
		checkGroupType();
		this.cardinality.setCardinality(minCard, maxCard);
	}

	public void update() {
		if (featureOwner.isAbstract()) {
			if (featureOwner.isAlternative()) {
				setGroupType(FeatureGroupType.Alternative);
				setCardinality(1, 1);
				return;
			}
			if (featureOwner.isOr()) {
				setGroupType(FeatureGroupType.Or);
				setCardinality(cardinality.getMinCardinality(), cardinality
						.getMaxCardinality());
				return;
			}
			if (featureOwner.isAnd()) {
				setGroupType(FeatureGroupType.And);
				return;
			}
		}
		setGroupType(FeatureGroupType.None);
	}

	private void checkGroupType() {
		if (!featureGroupType.equals(FeatureGroupType.Alternative)
				&& !featureGroupType.equals(FeatureGroupType.Or)) {
			// throw new RuntimeException(featureGroupType
			// + " can not have cardinality!");
		}
	}
}

class Cardinality {
	public static final String MIN_CARDINALITY_ID = "MIN_CARDINALITY";
	public static final String MAX_CARDINALITY_ID = "MAX_CARDINALITY";

	private int minCardinality = 1;
	private int maxCardinality = 1;

	private FeatureGroup featureGroup;

	public Cardinality(FeatureGroup featureGroup) {
		this.featureGroup = featureGroup;
	}

	public void setCardinality(int minCard, int maxCard) {
		minCardinality = minCard >= 1 ? minCard : 1;
		maxCard = maxCard >= minCard ? maxCard : minCard;
		maxCardinality = maxCard <= featureGroup.getChildren().size() ? maxCard
				: featureGroup.getChildren().size();
	}

	public String toString() {
		return "(" + minCardinality + ", " + maxCardinality + ")";
	}

	public int getMinCardinality() {
		return minCardinality;
	}

	public void setMinCardinality(int minCardinality) {
		this.minCardinality = minCardinality;
	}

	public int getMaxCardinality() {
		return maxCardinality;
	}

	public void setMaxCardinality(int maxCardinality) {
		this.maxCardinality = maxCardinality;
	}

}
