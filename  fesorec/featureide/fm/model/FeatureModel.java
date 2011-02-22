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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.prop4j.Node;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import featureide.fm.io.guidsl.FeatureModelWriter;
import featureide.fm.model.FeatureGroup.FeatureGroupType;
import featureide.fm.util.NodeCreator;

/**
 * The model representation of the feature tree that notifies listeners of
 * changes in the tree.
 * 
 * @author Thomas Thuem
 * 
 */
public class FeatureModel implements PropertyConstants {

	/**
	 * the root feature
	 */
	private Feature root;

	/**
	 * all comment lines from the model file without line number at which they
	 * occur
	 */
	private String comments;

	/**
	 * This string saves the annotations from the model file as they were read,
	 * because they were not yet used.
	 */
	private String annotations;

	/**
	 * a map containing all features
	 */
	private Map<String, Feature> featureTable = new HashMap<String, Feature>();

	/**
	 * constraints list(require/exclude).
	 */
	private Set<Constraint> constraintSet = new HashSet<Constraint>();
	private LinkedList<Constraint> constraintList = new LinkedList<Constraint>();

	private boolean abstractFeatures;

	public FeatureModel() {
		abstractFeatures = true;
	}

	public void clear() {
		for (Feature feature : featureTable.values()) {
			feature.getChildren().clear();
			feature.setParent(null);
		}
		featureTable.clear();

		constraintSet.clear();
		constraintList.clear();
		root = null;
		abstractFeatures = true;
	}

	public Feature getRoot() {
		return root;
	}

	public void setRoot(Feature root) {
		this.root = root;
	}

	public boolean addFeature(Feature feature) {
		String name = feature.getName();
		if (featureTable.containsKey(name))
			return false;
		featureTable.put(name, feature);
		return true;
	}

	public void deleteFeatureFromTable(Feature feature) {
		featureTable.remove(feature.getName());
	}

	public void deleteFeatureFromTable(String featureName) {
		featureTable.remove(featureName);
	}

	public boolean deleteSingleFeature(final Feature feature) {
		// the root can not be deleted
		if (feature == root)
			return false;

		// check if it exists
		final String name = feature.getName();
		if (!featureTable.containsKey(name))
			return false;

		Feature parent = feature.getParent();
		// delete feature
		boolean deleteParent = parent.isAbstract()
				&& parent.getChildrenCount() == 1;
		parent.removeChild(feature);
		deleteFeatureFromTable(feature);
		processConstraints(feature);

		// delete parent if it has no children
		if (deleteParent)
			return deleteSingleFeature(parent);

		return true;
	}

	public boolean deleteFeatureWithDescendants(final Feature feature) {
		// the root can not be deleted
		if (feature == root)
			return false;

		// check if it exists
		final String name = feature.getName();
		if (!featureTable.containsKey(name))
			return false;

		Feature parent = feature.getParent();

		if (feature.hasChildren()) {
			deleteChildFeatures(feature);
		}

		// delete feature
		boolean deleteParent = parent.isAbstract()
				&& parent.getChildrenCount() == 1;

		parent.removeChild(feature);
		deleteFeatureFromTable(feature);
		processConstraints(feature);

		// delete parent if it has no children
		if (deleteParent)
			return deleteSingleFeature(parent);
		return true;
	}

	private void deleteChildFeatures(Feature feature) {
		Queue<Feature> queue = new LinkedList<Feature>();
		Set<String> nameSet = new HashSet<String>();

		queue.offer(feature);
		while (!queue.isEmpty()) {
			Feature element = queue.poll();
			if (element.hasChildren()) {
				for (Feature f : element.getChildren()) {
					queue.offer(f);
				}
			}
			nameSet.add(element.getName());
		}

		// Delete from feature table
		for (String name : nameSet) {
			deleteFeatureFromTable(name);
		}
		// Process constraints
		Iterator<Constraint> iterator = constraintList.iterator();
		while (iterator.hasNext()) {
			Constraint constraint = iterator.next();
			String nameA = constraint.getFeatureA();
			String nameB = constraint.getFeatureB();
			if (nameSet.contains(nameA) || nameSet.contains(nameB)) {
				iterator.remove();
				constraintSet.remove(constraint);
			}

		}
	}

	public boolean deleteFeatureAndMergeChildren(final Feature featureToDel,
			Feature targetFeature, FeatureGroupType mergeType) {
		// the root can not be deleted
		if (featureToDel == root)
			return false;

		// check if it exists
		final String name = featureToDel.getName();
		if (!featureTable.containsKey(name))
			return false;

		if (!targetFeature.canHaveChildren())
			return false;

		switch (mergeType) {
		case Alternative:
			targetFeature.changeToAlternative();
			break;
		case Or:
			targetFeature.changeToOr();
			break;
		case And:
			targetFeature.changeToAnd();
			break;
		default:
			break;
		}
		// merge children to target feature

		int index = targetFeature.getChildrenCount();
		try {
			while (featureToDel.hasChildren())
				targetFeature.addChildAtPosition(index, featureToDel
						.removeLastChild());
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			System.err.println(featureToDel);
			System.err.println(targetFeature);
			FeatureModelWriter writer = new FeatureModelWriter(this);
			System.err.println(writer.writeToString());
			throw e;
		}

		Feature parent = featureToDel.getParent();
		// delete feature
		parent.removeChild(featureToDel);
		deleteFeatureFromTable(featureToDel);
		processConstraints(featureToDel);

		// delete parent if it has no children
		boolean deleteParent = parent.isAbstract()
				&& parent.getChildrenCount() == 1;

		if (deleteParent)
			return deleteSingleFeature(parent);

		return true;
	}

	/**
	 * Process the constraint changes when deleting a feature.
	 */
	private void processConstraints(Feature featureToDelete) {
		try {
			String featureName = featureToDelete.getName();
			Iterator<Constraint> iterator = constraintList.iterator();
			while (iterator.hasNext()) {
				Constraint constraint = iterator.next();
				if (constraint.getFeatureA().equals(featureName)
						|| constraint.getFeatureB().equals(featureName)) {
					iterator.remove();
					constraintSet.remove(constraint);
				}
			}

			// Constraint[] constraints = constraintList
			// .toArray(new Constraint[0]);
			// for (int i = 0; i < constraints.length; i++) {
			// Constraint constraint = constraints[i];
			// if (constraint.getFeatureA().equals(featureName)
			// || constraint.getFeatureB().equals(featureName)) {
			// removeConstraint(constraint);
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void renameConstraints(String oldName, String NewName) {
		for (Constraint constraint : constraintList) {
			if (constraint.getFeatureA().equals(oldName)) {
				constraint.setFeatureA(NewName);
			} else if (constraint.getFeatureB().equals(oldName)) {
				constraint.setFeatureB(NewName);
			}

		}
	}

	public Feature getFeature(String name) {
		if (featureTable.isEmpty()) {
			// create the root feature (it is the only one without a reference)
			root = new Feature(name);
			addFeature(root);
			return root;
		}
		return featureTable.get(name);
	}

	public boolean renameFeature(String oldName, String newName) {
		if (!featureTable.containsKey(oldName)
				|| featureTable.containsKey(newName))
			return false;
		Feature feature = featureTable.remove(oldName);
		feature.setName(newName);
		featureTable.put(newName, feature);
		renameConstraints(oldName, newName);
		return true;
	}

	public boolean containsLayer(String featureName) {
		Feature feature = featureTable.get(featureName);
		return feature != null && feature.isLayer();
	}

	public boolean containsCompound(String featureName) {
		Feature feature = featureTable.get(featureName);
		return feature != null && feature.isAbstract();
	}

	public boolean containsFeature(String featureName) {
		return featureTable.containsKey(featureName);
	}

	public boolean containsConstraint(Constraint constraint) {
		return constraintSet.contains(constraint);
	}

	public Collection<Feature> getFeatures() {
		return Collections.unmodifiableCollection(featureTable.values());
	}

	public void replaceRoot(Feature feature) {
		featureTable.remove(root.getName());
		root = feature;
	}

	public Set<String> getFeatureNames() {
		return Collections.unmodifiableSet(featureTable.keySet());
	}

	public void addConstraint(Constraint constraint) {

		constraintList.add(constraint);
		constraintSet.add(constraint);
	}

	public Constraint getConstraint(int index) {
		return constraintList.get(index);
	}

	public int getConstraintCount() {
		return constraintList.size();
	}

	public List<Constraint> getConstraints() {
		return Collections.unmodifiableList(constraintList);
	}

	public void removeConstraint(Constraint consNode) {
		if (consNode == null)
			return;
		constraintSet.remove(consNode);
		constraintList.remove(consNode);
	}

	public int getFeatureCount() {
		return featureTable.size();
	}

	public int getCompoundCount() {
		int number = 0;
		for (Feature feature : getFeatures())
			if (feature.isAbstract())
				number++;
		return number;
	}

	public int getLayerCount() {
		int number = 0;
		for (Feature feature : getFeatures())
			if (feature.isConcrete())
				number++;
		return number;
	}

	public boolean isValid() throws TimeoutException {
		Node root = NodeCreator.createNodes(this);
		return new SatSolver(root, 1000).isSatisfiable();
	}

	public void hasAbstractFeatures(boolean abstractFeatures) {
		this.abstractFeatures = abstractFeatures;
	}

	public boolean hasAbstractFeatures() {
		return abstractFeatures;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("size: ").append(featureTable.size()).append("\n");
		for (Feature feature : featureTable.values())
			builder.append(feature).append("\n");

		builder.append("\n");

		for (Constraint constraint : constraintList) {
			builder.append(constraint).append("\n");
		}

		return builder.toString();
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @param comments
	 *            the comments to set
	 */
	public void setComments(String comments) {
		this.comments = comments;
	}

	/**
	 * @return the annotations
	 */
	public String getAnnotations() {
		return annotations;
	}

	/**
	 * @param annotations
	 *            the annotations to set
	 */
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

}
