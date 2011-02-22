package featureide.fm.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.sat4j.specs.TimeoutException;

import featureide.fm.eval.EvaluationSpec;
import featureide.fm.io.IFeatureModelReader;
import featureide.fm.io.UnsupportedModelException;
import featureide.fm.io.guidsl.FeatureModelReader;
import featureide.fm.io.guidsl.FeatureModelWriter;
import featureide.fm.model.Constraint;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.Constraint.ConstraintType;
import featureide.fm.util.StringStoration;

public class ModelGenerator {

	public final static int maxChildren = 10;
	public final static float constraitFactor = 0.1f;

	private static ModelGenerator instance = new ModelGenerator();

	public static ModelGenerator getInstance() {
		return instance;
	}

	public FeatureModel generateFeatureModel(long id, EvaluationSpec spec) {
		FeatureModel fm = generateFeatureModel(id, spec.getModelNum());
		if (spec.isDoPostCheck()) {
			while (true) {
				boolean isValid = false;
				try {
					isValid = fm.isValid();
				} catch (TimeoutException e) {
				}
				if (isValid)
					break;
				fm.clear();
				fm = generateFeatureModel(id, spec.getModelNum());
			}
		}

		return fm;
	}

	public FeatureModel generateFeatureModel(long id, int numberOfFeatures) {
		int seed = (int) System.currentTimeMillis() % 19999;
		Random random = new Random(id + seed);
		FeatureModel fm = generateFeatureDiagram(random, numberOfFeatures);
		generateConstraints(fm, random,
				(int) (numberOfFeatures * constraitFactor));
		return fm;
	}

	public FeatureModel loadFeatureModel(String filepath) {
		FeatureModel fm = new FeatureModel();
		IFeatureModelReader reader = new FeatureModelReader(fm);
		try {
			reader.readFromFile(new File(filepath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedModelException e) {
			e.printStackTrace();
		}
		return fm;
	}

	public void saveFeatureModel(FeatureModel model, String filepath) {
		FeatureModelWriter writer = new FeatureModelWriter(model);
		writer.writeToFile(new File(filepath));
	}

	public FeatureModel generateFeatureDiagram(Random random,
			int numberOfFeatures) {
		FeatureModel fm = new FeatureModel();
		List<Feature> leaves = new LinkedList<Feature>();
		leaves.add(fm.getFeature(StringStoration.getLayerName(1)));
		int count = 1;
		while (count < numberOfFeatures) {
			int parentIndex = random.nextInt(leaves.size());
			Feature parent = leaves.remove(parentIndex);
			fm.renameFeature(parent.getName(), StringStoration
					.changeToCompoundName(parent.getName()));
			int childrenCount = random.nextInt(maxChildren) + 1;
			childrenCount = Math.min(childrenCount, numberOfFeatures - count);
			for (int i = 1; i <= childrenCount; i++) {
				// Feature child = new Feature("C" + (count + i));
				Feature child = new Feature(StringStoration.getLayerName(count
						+ i));
				fm.addFeature(child);
				parent.addChild(child);
				leaves.add(child);
			}
			if (random.nextBoolean()) {
				parent.changeToAnd();
				for (Feature child : parent.getChildren())
					child.setMandatory(random.nextBoolean());
			} else {
				if (random.nextBoolean())
					parent.changeToOr();
				else
					parent.changeToAlternative();
			}

			count += childrenCount;
		}
		return fm;
	}

	public void generateConstraints(FeatureModel fm, Random random,
			int numberOfConstraints) {
		List<String> names = new ArrayList<String>(fm.getFeatureNames());
		names.remove(fm.getRoot().getName());

		for (int i = 0; i < numberOfConstraints;) {
			String featureA = names.remove(random.nextInt(names.size()));
			String featureB = names.remove(random.nextInt(names.size()));

			ConstraintType constraintType = random.nextBoolean() ? ConstraintType.require
					: ConstraintType.exclude;

			Constraint constraint = new Constraint(featureA, featureB,
					constraintType);

			fm.addConstraint(constraint);
			i++;
		}

		// Feature[][] descendantss = FMDescendantHelper.getDescendants(fm);
		// for (int i = 0; i < numberOfConstraints;) {
		// Feature[] descendantsA = descendantss[random
		// .nextInt(descendantss.length)];
		// Feature[] descendantsB = descendantss[random
		// .nextInt(descendantss.length)];
		// while (descendantsB == descendantsA) {
		// descendantsB = descendantss[random.nextInt(descendantss.length)];
		// }
		//
		// String featureA = descendantsA[random.nextInt(descendantsA.length)]
		// .getName();
		// String featureB = descendantsB[random.nextInt(descendantsB.length)]
		// .getName();
		// ConstraintType constraintType = random.nextBoolean() ?
		// ConstraintType.require
		// : ConstraintType.exclude;
		// Constraint constraint = new Constraint(featureA, featureB,
		// constraintType);
		//
		// fm.addConstraint(constraint);
		// i++;
		// }
		// descendantss = null;
	}

	// public static class FMDescendantHelper {
	// private static List<Feature> all = new ArrayList<Feature>();
	// private static Stack<Feature> stack = new Stack<Feature>();
	//
	// private static List<Feature> children = new ArrayList<Feature>();
	//
	// public static Feature[][] getDescendants(FeatureModel fm) {
	// Feature root = fm.getRoot();
	// while (root.getChildrenCount() == 1) {
	// root = root.getFirstChild();
	// }
	// children.addAll(root.getChildren());
	//
	// Feature[][] subTrees = new Feature[children.size()][];
	// for (int i = 0; i < subTrees.length; i++) {
	// subTrees[i] = getDescendants(children.get(i));
	// }
	// children.clear();
	//
	// return subTrees;
	// }
	//
	// public static Feature[] getDescendants(Feature feature) {
	// stack.push(feature);
	// while (!stack.isEmpty()) {
	// Feature f = stack.pop();
	// all.add(f);
	// for (Feature child : f.getChildren())
	// stack.push(child);
	// }
	// Feature[] decendants = all.toArray(new Feature[0]);
	// all.clear();
	// return decendants;
	// }
	// }

}
