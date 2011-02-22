package featureide.fm.operations;

import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import featureide.fm.eval.EvaluationSpec;
import featureide.fm.eval.EvaluationSpec.OperationType;
import featureide.fm.model.Constraint;
import featureide.fm.model.EvolutionStrategy;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.Constraint.ConstraintType;
import featureide.fm.util.Utility;

public class OperationPerformer {

	private Random rand = new Random(System.currentTimeMillis() / 59999);
	private FeatureModel featureModel;
	private EvaluationSpec spec;

	ConstraintChainValidator constraintValidator;

	public OperationPerformer(FeatureModel model, EvaluationSpec spec) {
		featureModel = model;
		this.spec = spec;
		if (spec.getOperationType() != OperationType.AllSet
		/* && spec.getOperationType() != OperationType.AllRemove */) {
			constraintValidator = new ConstraintChainValidator(featureModel);
			constraintValidator.initGraphs();
		}
	}

	public void performOperations() {
		if (spec.getOperationNum() <= 0) {
			return;
		}
		if (spec.getEvolutionStrategy() != EvolutionStrategy.Random) {
			performESOpereations();
			return;
		}

		switch (spec.getOperationType()) {
		case AllAdd:
			performAddOperations(spec.getOperationNum());
			break;
		case AllRemove:
			performRemoveOperations(spec.getOperationNum());
			break;
		case AllSet:
			performSetOperations(spec.getOperationNum());
			break;
		case Random:
			performRandomOperations();
			break;
		default:
			break;
		}

		/*
		 * Operation completed. Clear all the references.
		 */
		featureModel = null;
		if (spec.getOperationType() != OperationType.AllSet
				&& spec.getOperationType() != OperationType.AllRemove) {
			constraintValidator = null;
		}
	}

	private void performESOpereations() {
		for (int i = 0; i < spec.getOperationNum(); i++) {
			performRemoveCompound();
		}
	}

	private void performRandomOperations() {
		int eachNum = spec.getOperationNum() / 3;
		performSetOperations(eachNum);
		performAddOperations(eachNum);
		performRemoveOperations(eachNum);
	}

	private void performSetOperations(int operationNum) {
		if (operationNum == 4) {
			performSetName();
			performSetGroupType();
			performSetCardinality();
			performSetOptional();
			return;
		}

		for (int i = 0; i < operationNum; i++) {
			int index = rand.nextInt(4);
			switch (index) {
			case 0:
				performSetName();
				break;
			case 1:
				performSetGroupType();
				break;
			case 2:
				performSetCardinality();
				break;
			case 3:
				performSetOptional();
				break;
			default:
				break;
			}
		}
	}

	private void performAddOperations(int operationNum) {
		if (operationNum == 12) {
			int num = operationNum / 4;
			for (int i = 0; i < num; i++) {
				performAddRequire();
				performAddExclude();
				performAddLayer();
				performAddCompound();
			}
			return;
		}
		for (int i = 0; i < operationNum; i++) {
			int index = rand.nextInt(4);
			switch (index) {
			case 0:
				performAddRequire();
				break;
			case 1:
				performAddExclude();
				break;
			case 2:
				performAddLayer();
				break;
			case 3:
				performAddCompound();
				break;
			default:
				break;
			}
		}

	}

	private void performRemoveOperations(int operationNum) {
		if (operationNum == 12) {
			int num = operationNum / 4;
			for (int i = 0; i < num; i++) {
				performRemoveRequire();
				performRemoveExclude();
				performRemoveLayer();
				performRemoveCompound();
			}
			return;
		}

		for (int i = 0; i < operationNum; i++) {
			int index = rand.nextInt(4);
			switch (index) {
			case 0:
				performRemoveRequire();
				break;
			case 1:
				performRemoveExclude();
				break;
			case 2:
				performRemoveLayer();
				break;
			case 3:
				performRemoveCompound();
				break;
			default:
				break;
			}
		}
	}

	private void performAddCompound() {
		final Feature newCompound = new Feature(Utility.getNewFeatureName(true));
		final Feature target = getRandomFeature();

		if (target == featureModel.getRoot()) {
			featureModel.setRoot(newCompound);
			newCompound.addChild(target);
		} else {
			final Feature parent = target.getParent();
			parent.replaceChild(target, newCompound);
			newCompound.addChild(target);
		}

		featureModel.addFeature(newCompound);
	}

	private void performRemoveCompound() {
		if (featureModel.getCompoundCount() <= 1) {
			return;
		}
		Feature compound = getRandomCompound();

		EvolutionStrategy evolutionStrategy = spec.getEvolutionStrategy();
		if (evolutionStrategy == EvolutionStrategy.Random) {
			int index = rand.nextInt(3);
			switch (index) {
			case 0:
				evolutionStrategy = EvolutionStrategy.DeleteAllChildren;
				break;
			case 1:
				evolutionStrategy = EvolutionStrategy.MergeChildrenToParent;
				break;
			case 2:
				evolutionStrategy = EvolutionStrategy.MergeChildrenToParent;
			}
		}

		switch (evolutionStrategy) {
		case DeleteAllChildren:
			featureModel.deleteFeatureWithDescendants(compound);
			break;

		case MergeChildrenToParent:
			if (compound != featureModel.getRoot()) {
				if (compound.getParent() == null) {
					System.err.println();
				}
				try {
					featureModel.deleteFeatureAndMergeChildren(compound,
							compound.getParent(), compound.getParent()
									.getGroup().getGroupType());
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			break;

		case MergeChildToAnotherCompound:
			if (compound != featureModel.getRoot()) {
				Feature otherCompound = null;
				Feature parent = compound.getParent();
				for (Feature feature : parent.getChildren()) {
					if (feature != compound && feature.isAbstract()) {
						otherCompound = feature;
						break;
					}
				}
				if (otherCompound == null) {
					otherCompound = parent;
				}
				featureModel.deleteFeatureAndMergeChildren(compound,
						otherCompound, otherCompound.getGroup().getGroupType());
			}
			break;
		}
	}

	private void performAddLayer() {
		if (featureModel.getLayerCount() <= 0) {
			return;
		}
		Feature newLayer = new Feature(Utility.getNewFeatureName(false));
		Feature targetCompound = getRandomCompound();

		featureModel.addFeature(newLayer);
		targetCompound.addChild(newLayer);

	}

	private void performRemoveLayer() {

		Feature layer = getRandomLayer();

		featureModel.deleteSingleFeature(layer);

	}

	private void performAddRequire() {
		// Feature featureA = getRandomFeature();
		// Feature featureB = getRandomFeature();
		// while (featureB == featureA) {
		// featureB = getRandomFeature();
		// }
		Iterator<Feature> iterator = featureModel.getFeatures().iterator();

		Feature featureA = iterator.next();
		int indexB = rand.nextInt(8) + 1;
		for (int i = 0; i < indexB; i++) {
			iterator.next();
		}
		Feature featureB = iterator.next();

		if (!addConstraintPreCheck(featureA, featureB))
			return;

		Constraint constraint = new Constraint(featureA.getName(), featureB
				.getName(), ConstraintType.require);

		if (featureModel.containsConstraint(constraint))
			return;
		if (constraintValidator.addConstraint(constraint)) {
			featureModel.addConstraint(constraint);
		}
	}

	private boolean addConstraintPreCheck(Feature featureA, Feature featureB) {
		if (featureA.getParent() == featureB.getParent())
			return false;

		Feature parent = featureA.getParent();
		while (parent != null) {
			if (featureB == parent)
				return false;
			parent = parent.getParent();
		}

		parent = featureB.getParent();
		while (parent != null) {
			if (featureA == parent)
				return false;
			parent = parent.getParent();
		}

		return true;
	}

	private void performRemoveRequire() {
		if (featureModel.getConstraintCount() <= 0) {
			return;
		}

		final Constraint require = featureModel.getConstraint(rand
				.nextInt(featureModel.getConstraintCount()));

		featureModel.removeConstraint(require);
		if (constraintValidator != null) {
			constraintValidator.removeConstraint(require);
		}
	}

	private void performAddExclude() {
		Iterator<Feature> iterator = featureModel.getFeatures().iterator();

		Feature featureA = iterator.next();
		int indexB = rand.nextInt(8) + 1;
		for (int i = 0; i < indexB; i++) {
			iterator.next();
		}
		Feature featureB = iterator.next();

		if (!addConstraintPreCheck(featureA, featureB))
			return;

		final Constraint constraint = new Constraint(featureA.getName(),
				featureB.getName(), ConstraintType.exclude);

		if (featureModel.containsConstraint(constraint))
			return;
		if (constraintValidator.addConstraint(constraint)) {
			featureModel.addConstraint(constraint);
		}
	}

	private void performRemoveExclude() {
		if (featureModel.getConstraintCount() <= 0) {
			return;
		}
		final Constraint exclude = featureModel.getConstraint(rand
				.nextInt(featureModel.getConstraintCount()));

		if (constraintValidator != null) {
			constraintValidator.removeConstraint(exclude);
		}
		featureModel.removeConstraint(exclude);
	}

	private void performSetName() {
		final Feature feature = getRandomFeature();
		featureModel.renameFeature(feature.getName(), Utility.getRandomString(
				5, rand));
	}

	private void performSetGroupType() {
		Feature feature = getRandomCompound();
		if (feature.isAlternative())
			if (rand.nextBoolean())
				feature.changeToAnd();
			else
				feature.changeToOr();
		else if (feature.isAnd())
			if (rand.nextBoolean())
				feature.changeToAlternative();
			else
				feature.changeToOr();
		else if (rand.nextBoolean())
			feature.changeToAnd();
		else
			feature.changeToAlternative();
	}

	private void performSetOptional() {
		final Feature feature = getRandomFeature();
		final Feature parent = feature.getParent();
		if (parent != null && parent.isAnd() && !parent.isFirstChild(feature)) {
			feature.setMandatory(!feature.isMandatory());
		}
	}

	private void performSetCardinality() {
		Feature compound = getRandomCompound();
		compound.getGroup().setCardinality(rand.nextInt(5), rand.nextInt(5));
	}

	private Feature getRandomLayer() {

		Feature feature = null;
		Collection<Feature> features = featureModel.getFeatures();
		Iterator<Feature> iterator = features.iterator();

		while (iterator.hasNext()) {
			feature = iterator.next();
			if (!feature.isAbstract()) {
				break;
			}
		}
		return feature;

		// Collection<Feature> features = featureModel.getFeatures();
		// int size = featureModel.getLayerCount();
		// int index = rand.nextInt(size) + 1;
		// Iterator<Feature> iterator = features.iterator();
		// for (int i = 1; i < index;) {
		// Feature feature = iterator.next();
		// if (feature.isConcrete()) {
		// ++i;
		// }
		// }
		// return iterator.next();
		// return layerList.get(rand.nextInt(layerList.size()));
	}

	private Feature getRandomCompound() {
		Feature feature = null;
		Collection<Feature> features = featureModel.getFeatures();
		Iterator<Feature> iterator = features.iterator();

		while (iterator.hasNext()) {
			feature = iterator.next();
			if (feature.isAbstract()) {
				break;
			}
		}
		return feature;
		// return compoundList.get(rand.nextInt(compoundList.size()));
	}

	private Feature getRandomFeature() {
		Iterator<Feature> iterator = featureModel.getFeatures().iterator();

		return iterator.next();

		// if (rand.nextBoolean()) {
		// return layerList.get(rand.nextInt(layerList.size()));
		// } else {
		// return compoundList.get(rand.nextInt(compoundList.size()));
		// }
	}

}
