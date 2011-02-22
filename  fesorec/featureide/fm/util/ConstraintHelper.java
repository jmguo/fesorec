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

import org.prop4j.And;
import org.prop4j.Implies;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.prop4j.Not;

import featureide.fm.model.Constraint;
import featureide.fm.model.Constraint.ConstraintType;

/**
 * Helper methods for converting between {@link Node} and {@link ConstraintNode}
 * and others utilities.
 */
public class ConstraintHelper {

	public static Constraint createConstraint(Node node) {
		Constraint constraint;

		String text = node.toString(NodeWriter.textualSymbols);
		if (text.contains("implies")) {
			String[] parts = text.split(" ");
			constraint = new Constraint(parts[0], parts[2].replace(";", ""),
					ConstraintType.require);
		} else {
			text = text.replace("not (", "").replace(")", "");
			String[] parts = text.split(" ");
			constraint = new Constraint(parts[0], parts[2].replace(";", ""),
					ConstraintType.exclude);
		}
		constraint.setPropNode(node);
		return constraint;
	}

	public static Node createNode(Constraint constraint) {
		Node node = null;
		switch (constraint.getConstraintType()) {
		case require:
			node = new Implies(new Literal(constraint.getFeatureA()),
					new Literal(constraint.getFeatureB()));
			break;
		case exclude:
			node = new Not(new And(new Literal(constraint.getFeatureA()),
					new Literal(constraint.getFeatureB())));
			break;
		}

		return node;
	}

}
