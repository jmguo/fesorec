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
package featureide.fm.util;

import java.util.HashMap;
import java.util.LinkedList;

import org.prop4j.And;
import org.prop4j.AtMost;
import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Or;

import featureide.fm.model.Constraint;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureModel;

/**
 * Takes a feature model as input and returns a propositional formula
 * representing the valid feature combinations.
 * 
 * @author Thomas Thuem
 */
public class NodeCreator {

	private static final HashMap<Object, Node> EMPTY_MAP = new HashMap<Object, Node>();

	public static Node createNodes(FeatureModel featureModel) {
		return createNodes(featureModel, EMPTY_MAP);
	}

	public static Node createNodes(FeatureModel featureModel,
			HashMap<Object, Node> replacingMap) {
		Feature root = featureModel.getRoot();
		LinkedList<Node> nodes = new LinkedList<Node>();
		if (root != null) {
			nodes.add(new Literal(getVariable(root, featureModel)));
			// convert grammar rules into propositional formulas
			createNodes(nodes, root, featureModel, true);
			// add extra constraints
			for (Constraint constraint : featureModel.getConstraints())
				nodes.add(constraint.getPropNode().clone());
		}
		return new And(nodes);
	}

	public static Node createNodesForConstraints(FeatureModel featureModel) {
		LinkedList<Node> nodes = new LinkedList<Node>();
		for (Constraint constraint : featureModel.getConstraints())
			nodes.add(constraint.getPropNode().clone());
		return new And(nodes);
	}

	private static void createNodes(LinkedList<Node> nodes,
			Feature rootFeature, FeatureModel featureModel, boolean recursive) {
		if (rootFeature == null || !rootFeature.hasChildren())
			return;

		String s = getVariable(rootFeature, featureModel);

		Node[] children = new Node[rootFeature.getChildrenCount()];
		for (int i = 0; i < children.length; i++) {
			String var = getVariable(rootFeature.getChildren().get(i),
					featureModel);
			children[i] = new Literal(var);
		}
		Node definition = children.length == 1 ? children[0] : new Or(children);

		if (rootFeature.isAnd()) {// &&
			LinkedList<Node> manChildren = new LinkedList<Node>();
			for (Feature feature : rootFeature.getChildren())
				if (feature.isMandatory()) {
					String var = getVariable(feature, featureModel);
					manChildren.add(new Literal(var));
				}

			// add constraints for all mandatory children S => (A & B)
			if (manChildren.size() == 1)
				nodes.add(new Implies(new Literal(s), manChildren.getFirst()));
			else if (manChildren.size() > 1)
				nodes.add(new Implies(new Literal(s), new And(manChildren)));

			if (rootFeature.isAbstract())
				nodes.add(new Equals(definition, new Literal(s)));
			else
				nodes.add(new Implies(definition, new Literal(s)));

		} else {
			// add constraint S <=> (A | B | C)
			nodes.add(new Equals(new Literal(s), definition));
			if (rootFeature.isAlternative()) {
				// add constraint atmost1(A, B, C)
				if (children.length > 1)
					nodes.add(new AtMost(1, Node.clone(children)));
			}
		}

		if (recursive)
			for (Feature feature : rootFeature.getChildren())
				createNodes(nodes, feature, featureModel, true);
	}

	public static String getVariable(Feature feature, FeatureModel featureModel) {
		return feature.getName();
	}

}
