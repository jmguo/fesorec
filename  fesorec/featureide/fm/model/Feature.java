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

import java.util.LinkedList;

import featureide.fm.model.FeatureGroup.FeatureGroupType;

/**
 * Provides all properties of a feature. This includes its connections to parent
 * and child features.
 * 
 * @author Thomas Thuem
 * 
 */
public class Feature implements PropertyConstants {
	public static final String NAME_ID = "NAME";
	public static final String TYPE_ID = "TYPE";
	public static final String PARENT_ID = "PARENT";
	public static final String GROUP_ID = "FEATURE_GROUP";

	public static final String DEFAULT_NAME = "Unknow";

	private String name;

	private boolean mandatory = true;

	private boolean and = false;

	private boolean multiple = false;

	private Feature parent;
	
	private int cost = 0;
	
	private int value = 0;

	private LinkedList<Feature> children = new LinkedList<Feature>();

	private FeatureGroup featureGroup;

	public Feature() {
		name = DEFAULT_NAME;
		// initialize feature group
		featureGroup = new FeatureGroup(FeatureGroupType.None, this);
	}

	public Feature(String name) {
		this.name = name;
		// initialize feature group
		featureGroup = new FeatureGroup(FeatureGroupType.None, this);
	}
	
	public int getCost(){
		return cost;
	}
	
	public void setCost(int c){
		cost = c;
	}
	
	public int getValue(){
		return value;
	}
	
	public void setValue(int v){
		value = v;
	}

	public boolean isAnd() {
		return and;
	}

	public boolean isOr() {
		return !and && multiple;
	}

	public boolean isAlternative() {
		return !and && !multiple;
	}

	public void changeToAnd() {
		and = true;
		multiple = false;
		featureGroup.update();
	}

	public void changeToOr() {
		and = false;
		multiple = true;
		featureGroup.update();
	}

	public void changeToAlternative() {
		and = false;
		multiple = false;
		featureGroup.update();
	}

	public void setAND(boolean and) {
		this.and = and;
		featureGroup.update();
	}

	public boolean isMandatorySet() {
		return mandatory;
	}

	public boolean isMandatory() {
		return parent == null || !parent.isAnd() || mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns true if the rule can be writen in a format like 'Ab [Cd] Ef ::
	 * Gh'.
	 */
	public boolean hasInlineRule() {
		return getChildrenCount() > 1 && and && isMandatory() && !multiple;
	}

	public void setParent(Feature newParent) {
		if (newParent == parent)
			return;
		// update the target
		parent = newParent;
	}

	public Feature getParent() {
		return parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public LinkedList<Feature> getChildren() {
		return children;
	}

	public void setChildren(LinkedList<Feature> children) {
		if (this.children == children)
			return;
		for (Feature child : children) {
			child.setParent(this);
		}
		this.children = children;

	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public void addChild(Feature newChild) {
		children.add(newChild);
		newChild.setParent(this);

	}

	public void addChildAtPosition(int index, Feature newChild) {
		children.add(index, newChild);
		newChild.setParent(this);

	}

	public void replaceChild(Feature oldChild, Feature newChild) {
		int index = children.indexOf(oldChild);
		children.set(index, newChild);
		oldChild.setParent(null);
		newChild.setParent(this);
	}

	public void removeChild(Feature child) {
		children.remove(child);
		child.setParent(null);
		featureGroup.update();
	}

	public Feature removeLastChild() {
		Feature child = children.removeLast();
		child.setParent(null);
		featureGroup.update();
		return child;
	}

	public boolean isAncestorOf(Feature next) {
		while (next.getParent() != null) {
			if (next.getParent() == this)
				return true;
			next = next.getParent();
		}
		return false;
	}

	public boolean isFirstChild(Feature child) {
		return children.indexOf(child) == 0;
	}

	public int getChildrenCount() {
		return children.size();
	}

	public Feature getFirstChild() {
		if (children.isEmpty())
			return null;
		return children.get(0);
	}

	public Feature getLastChild() {
		if (!children.isEmpty()) {
			return children.getLast();
		}
		return null;
	}

	public int getChildIndex(Feature feature) {
		return children.indexOf(feature);
	}

	public boolean isAbstract() {
		return hasChildren();
	}

	public boolean isConcrete() {
		return !isAbstract();
	}

	public boolean isLayer() {
		return !isAbstract();
	}

	public boolean canHaveChildren() {
		return hasChildren();
	}

	public boolean isANDPossible() {
		if (parent == null || parent.isAnd())
			return false;
		for (Feature child : children) {
			if (child.isAnd())
				return false;
		}
		return true;
	}

	public FeatureGroup getGroup() {
		return featureGroup;
	}

	@Override
	public Feature clone() {
		Feature feature = new Feature(name);
		for (Feature child : children) {
			feature.addChild(child.clone());
		}
		feature.featureGroup.setChildren(feature.children);
		feature.featureGroup.setGroupType(this.featureGroup.getGroupType());
		feature.featureGroup
				.setCardinality(feature.getGroup().getCardinality());

		feature.and = and;
		feature.mandatory = mandatory;
		feature.multiple = multiple;
		return feature;
	}

	/**
	 * debug only
	 */
	@Override
	public String toString() {
		if (isAbstract())
			return name + " " + featureGroup.toString();
		return name;
	}

}
