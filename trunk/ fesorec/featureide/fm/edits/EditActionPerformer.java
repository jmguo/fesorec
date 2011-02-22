package featureide.fm.edits;

import java.util.Random;

import featureide.fm.model.Constraint;
import featureide.fm.model.EvolutionStrategy;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureGroup;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.Constraint.ConstraintType;
import featureide.fm.model.FeatureGroup.FeatureGroupType;
import featureide.fm.util.Utility;

public class EditActionPerformer {

	private FeatureModel featureModel;
	private Random random = new Random(System.currentTimeMillis() % 997);

	public static int editNotExcutedCounter = 0;

	private static EditActionPerformer instance = new EditActionPerformer();

	public static EditActionPerformer getInstance() {
		return instance;
	}

	/**
	 * @param featureModel
	 */
	public EditActionPerformer() {
	}

	public void perform(EditAction action, FeatureModel featureModel,
			EvolutionStrategy evolutionStrategy) {
		this.featureModel = featureModel;

		switch (action.getEditObjectType()) {
		case Compound:
			performCompoundEdit(action, evolutionStrategy);
			break;
		case Layer:
			performLayerEdit(action);
			break;
		case FeatureGroup:
			performFeatureGroupEdit(action);
			break;
		case Require:
			performConstraintEdit(action, ConstraintType.require);
			break;
		case Exclude:
			performConstraintEdit(action, ConstraintType.exclude);
			break;
		case Name:
			performNameEdit(action);
			break;
		case GroupType:
			performGroupTypeEdit(action);
			break;
		case Optionality:
			performOptionalityEdit(action);
			break;
		case Cardinality:
			performCardinalityEdit(action);
			break;
		}
	}

	public void performCompoundEdit(EditAction action,
			EvolutionStrategy evolutionStrategy) {
		switch (action.getEditType()) {
		case Add:
			Feature newCompound = (Feature) action.getParameter("newCompound");
			Feature targetFeature = (Feature) action
					.getParameter("targetFeature");
			targetFeature = safeGetFeature(targetFeature);
			if (targetFeature != null) {
				featureModel.addFeature(newCompound);

				Feature parent = targetFeature.getParent();
				if (parent == null) {
					newCompound.addChild(targetFeature);
					featureModel.setRoot(newCompound);
				} else {
					parent.replaceChild(targetFeature, newCompound);
					newCompound.addChild(targetFeature);
				}
			} else {
				EditActionPerformer.editNotExcutedCounter++;

			}
			break;
		case Remove:
			Feature compound = (Feature) action.getParameter("compound");
			compound = safeGetCompound(compound);
			if (compound != null) {
				if (evolutionStrategy == EvolutionStrategy.Random) {
					int index = random.nextInt(3);
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
						if (compound == compound.getParent()) {
							throw new RuntimeException(compound.getName()
									+ " 's parent is itself!");
						}
						featureModel.deleteFeatureAndMergeChildren(compound,
								compound.getParent(), compound.getParent()
										.getGroup().getGroupType());
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
								otherCompound, otherCompound.getGroup()
										.getGroupType());
					}
					break;
				}

			} else {
				EditActionPerformer.editNotExcutedCounter++;
			}

			break;
		}
	}

	public void performLayerEdit(EditAction action) {
		switch (action.getEditType()) {
		case Add:
			Feature newLayer = (Feature) action.getParameter("newLayer");
			Feature targetCompound = (Feature) action
					.getParameter("targetCompound");
			targetCompound = safeGetCompound(targetCompound);

			if (targetCompound != null) {
				featureModel.addFeature(newLayer);
				targetCompound.addChild(newLayer);
			} else {
				EditActionPerformer.editNotExcutedCounter++;

			}
			break;
		case Remove:
			Feature feature = (Feature) action.getParameter("layer");
			feature = safeGetLayer(feature);
			if (feature != null) {
				featureModel.deleteSingleFeature(feature);
			} else {
				EditActionPerformer.editNotExcutedCounter++;

			}
			break;
		}
	}

	public void performFeatureGroupEdit(EditAction action) {
		switch (action.getEditType()) {
		case Add:
			// TODO: Should merge the group type?
			FeatureGroup group = (FeatureGroup) action.getParameter("group");
			Feature targetCompound = (Feature) action
					.getParameter("targetCompound");
			targetCompound = safeGetCompound(targetCompound);
			if (targetCompound != null) {
				for (Feature f : group.getChildren()) {
					targetCompound.addChild(f);
					featureModel.addFeature(f);
				}
			} else {
				EditActionPerformer.editNotExcutedCounter++;

			}
			break;
		case Remove:

			break;
		}
	}

	public void performConstraintEdit(EditAction action,
			ConstraintType constraintType) {
		switch (action.getEditType()) {
		case Add:
			String nameA = (String) action.getParameter("featureA");
			String nameB = (String) action.getParameter("featureB");

			if (nameA.equals(nameB))
				return;
			if (!(featureModel.containsFeature(nameA) && featureModel
					.containsFeature(nameB))) {
				return;
			}

			Feature featureA = featureModel.getFeature(nameA);
			Feature featureB = featureModel.getFeature(nameB);
			if (featureA.getParent() == featureB.getParent())
				return;

			Feature parent = featureA.getParent();
			while (parent != null) {
				if (featureB == parent)
					return;
				parent = parent.getParent();
			}

			parent = featureB.getParent();
			while (parent != null) {
				if (featureA == parent)
					return;
				parent = parent.getParent();
			}

			Constraint constraint = new Constraint(nameA, nameB, constraintType);

			if (featureModel.containsConstraint(constraint))
				return;
			featureModel.addConstraint(constraint);
			// try {
			// if (!featureModel.isValid()) {
			// featureModel.removeConstraint(constraint);
			// }
			// } catch (TimeoutException e) {
			// featureModel.removeConstraint(constraint);
			// }

			break;
		case Remove:
			Constraint constraint2 = (Constraint) action
					.getParameter("constraint");
			constraint2 = safeGetConstraint(constraint2);
			if (constraint2 != null) {
				featureModel.removeConstraint(constraint2);
			} else {
				EditActionPerformer.editNotExcutedCounter++;

			}
			break;
		}
	}

	public void performNameEdit(EditAction action) {
		String newName = (String) action.getParameter("newName");
		Feature feature = (Feature) action.getParameter("feature");
		feature = safeGetFeature(feature);
		if (feature != null) {
			action.addParameter("oldName", feature.getName());
			featureModel.renameFeature(feature.getName(), newName);
		}
	}

	public void performGroupTypeEdit(EditAction action) {
		FeatureGroupType newType = (FeatureGroupType) action
				.getParameter("newGroupType");
		Feature feature = (Feature) action.getParameter("feature");
		feature = safeGetFeature(feature);
		if (feature == null)
			return;

		switch (newType) {
		case Alternative:
			feature.changeToAlternative();
		case And:
			feature.changeToAnd();
		case Or:
			feature.changeToOr();
		}
	}

	public void performOptionalityEdit(EditAction action) {
		Boolean mandatory = (Boolean) action.getParameter("mandatory");
		Feature feature = (Feature) action.getParameter("feature");
		feature = safeGetFeature(feature);
		if (feature != null)
			feature.setMandatory(mandatory);
	}

	public void performCardinalityEdit(EditAction action) {
		int mincard = (Integer) action.getParameter("minCard");
		int maxcard = (Integer) action.getParameter("maxCard");
		Feature feature = (Feature) action.getParameter("feature");
		feature = safeGetFeature(feature);
		if (feature == null)
			return;

		FeatureGroupType groupType = feature.getGroup().getGroupType();
		if (groupType == FeatureGroupType.And
				|| groupType == FeatureGroupType.None)
			return;
		feature.getGroup().setCardinality(mincard, maxcard);
	}

	private Feature safeGetFeature(Feature feature) {
		if (!featureModel.containsFeature(feature.getName())) {
			feature = Utility.getRandomFeature(featureModel, random, true);
		}
		return feature;
	}

	private Feature safeGetCompound(Feature compound) {
		if (!featureModel.containsFeature(compound.getName())) {
			compound = Utility.getRandomCompound(featureModel, random, true);
		}
		return compound;
	}

	private Feature safeGetLayer(Feature layer) {
		if (!featureModel.containsFeature(layer.getName())) {
			layer = Utility.getRandomLayer(featureModel, random, true);
		}
		return layer;
	}

	private Constraint safeGetConstraint(Constraint constraint) {
		if (featureModel.containsConstraint(constraint))
			return constraint;
		if (featureModel.getConstraintCount() == 0)
			return null;
		return featureModel.getConstraint(random.nextInt(featureModel
				.getConstraintCount()));
	}
}
