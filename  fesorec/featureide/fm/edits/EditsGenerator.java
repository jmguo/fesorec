package featureide.fm.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import featureide.fm.edits.EditAction;
import featureide.fm.edits.EditAction.EditObjectType;
import featureide.fm.edits.EditAction.EditType;
import featureide.fm.eval.EvaluationSpec;
import featureide.fm.eval.EvaluationSpec.OperationType;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureGroup;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.FeatureGroup.FeatureGroupType;
import featureide.fm.util.Utility;

public class EditsGenerator {

	private static EditActionGenerator[] generators;
	private static EditActionGenerator[] allAddRemoveGenerators;
	private static EditActionGenerator[] allSetGenerators;

	static {
		createGenerators();
	}

	private static EditsGenerator instance = new EditsGenerator();

	public static EditsGenerator getInstance() {
		return instance;
	}

	public List<EditAction> generateEdits(long id, EvaluationSpec spec,
			FeatureModel model) {
		EditActionGenerator[] editGenerators = getGeneratorsByType(spec
				.getOperationType());

		return generateEdits(id, spec.getOperationNum(), editGenerators, spec
				.getOperationType(), model);
	}

	public List<EditAction> generateEdits(long id, int numOfEdits,
			EditActionGenerator[] editGenerators, OperationType genType,
			FeatureModel featureModel) {
		List<EditAction> actions = new ArrayList<EditAction>(numOfEdits);
		Random random = new Random(id + System.currentTimeMillis() % 19999);

		for (int i = 0; i < numOfEdits; i++) {
			int index = random.nextInt(editGenerators.length);
			actions.add(editGenerators[index].generate(random, featureModel,
					genType));
		}

		return actions;
	}

	private static EditActionGenerator[] getGeneratorsByType(
			OperationType operationType) {
		switch (operationType) {
		case AllAdd:
		case AllRemove:
			return allAddRemoveGenerators;
		case AllSet:
			return allSetGenerators;
		case Random:
			return generators;
		}
		return generators;
	}

	private static EditActionGenerator[] createGenerators() {
		if (generators != null)
			return generators;

		int typeCount = EditAction.EditObjectType.values().length;

		generators = new EditActionGenerator[typeCount];
		generators[0] = new CompoundEditGenerator();
		generators[1] = new LayerEditGenerator();
		generators[2] = new FeatureGroupEditGenerator();
		generators[3] = new RequireEditGenerator();
		generators[4] = new ExcludeEditGenerator();
		generators[5] = new NameEditGenerator();
		generators[6] = new GroupTypeEditGenerator();
		generators[7] = new OptionalityEditGenerator();
		generators[8] = new CardinalityEditGenerator();

		allAddRemoveGenerators = new EditActionGenerator[] { generators[0],
				generators[1], generators[2], generators[3], generators[4] };

		allSetGenerators = new EditActionGenerator[] { generators[5],
				generators[6], generators[7], generators[8] };
		return generators;
	}

}

interface EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType);

}

class CompoundEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditObjectType(EditObjectType.Compound);

		if (operationType == OperationType.Random) {
			operationType = random.nextBoolean() ? OperationType.AllAdd
					: OperationType.AllRemove;
		}
		if (operationType == OperationType.AllAdd) {
			action.setEditType(EditType.Add);
			action.addParameter("targetFeature", Utility.getRandomFeature(
					model, random, false));

			Feature newCompound = new Feature(Utility.getNewFeatureName(true));
			action.addParameter("newCompound", newCompound);
		} else {
			action.setEditType(EditType.Remove);
			action.addParameter("compound", Utility.getRandomCompound(model,
					random, false));
		}

		return action;
	}
}

class LayerEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditObjectType(EditObjectType.Layer);

		if (operationType == OperationType.Random) {
			operationType = random.nextBoolean() ? OperationType.AllAdd
					: OperationType.AllRemove;
		}
		if (operationType == OperationType.AllAdd) {
			action.setEditType(EditType.Add);

			Feature layer = new Feature(Utility.getNewFeatureName(false));
			Feature targetCompound = Utility.getRandomCompound(model, random,
					false);

			action.addParameter("newLayer", layer);
			action.addParameter("targetCompound", targetCompound);

		} else {
			action.setEditType(EditType.Remove);
			action.addParameter("layer", Utility.getRandomLayer(model, random,
					false));
		}

		return action;
	}
}

class FeatureGroupEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditObjectType(EditObjectType.FeatureGroup);

		if (operationType == OperationType.Random) {
			operationType = random.nextBoolean() ? OperationType.AllAdd
					: OperationType.AllRemove;
		}
		if (operationType == OperationType.AllAdd) {
			action.setEditType(EditType.Add);
			FeatureGroup group = new FeatureGroup();

			List<Feature> children = new ArrayList<Feature>();
			for (int i = 0; i < random.nextInt(3); i++) {
				children.add(new Feature(Utility.getNewFeatureName(false)));
			}
			group.setChildren(children);
			action.addParameter("group", group);

			action.addParameter("targetCompound", Utility.getRandomCompound(
					model, random, false));
		} else {
			action.setEditType(EditType.Remove);
		}

		return action;
	}
}

class RequireEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();

		if (operationType == OperationType.Random) {
			operationType = random.nextBoolean() ? OperationType.AllAdd
					: OperationType.AllRemove;
		}
		if (operationType == OperationType.AllAdd) {
			action.setEditType(EditType.Add);
			action.addParameter("featureA", Utility.getRandomFeature(model,
					random, false).getName());
			action.addParameter("featureB", Utility.getRandomFeature(model,
					random, false).getName());
		} else {
			action.setEditType(EditType.Remove);
			action.addParameter("constraint", model.getConstraint(random
					.nextInt(model.getConstraintCount())));
		}
		action.setEditObjectType(EditObjectType.Require);

		return action;
	}
}

class ExcludeEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();

		if (operationType == OperationType.Random) {
			operationType = random.nextBoolean() ? OperationType.AllAdd
					: OperationType.AllRemove;
		}
		if (operationType == OperationType.AllAdd) {
			action.setEditType(EditType.Add);
			action.addParameter("featureA", Utility.getRandomFeature(model,
					random, false).getName());
			action.addParameter("featureB", Utility.getRandomFeature(model,
					random, false).getName());
		} else {
			action.setEditType(EditType.Remove);
			action.addParameter("constraint", model.getConstraint(random
					.nextInt(model.getConstraintCount())));
		}

		action.setEditObjectType(EditObjectType.Exclude);

		return action;
	}
}

class NameEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditType(EditType.Set);
		action.setEditObjectType(EditObjectType.Name);

		action.addParameter("newName", Utility.getRandomString(5, random));
		action.addParameter("feature", Utility.getRandomFeature(model, random,
				false));

		return action;
	}
}

class GroupTypeEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditType(EditType.Set);
		action.setEditObjectType(EditObjectType.GroupType);

		action.addParameter("feature", Utility.getRandomCompound(model, random,
				false));
		FeatureGroupType type;
		if (random.nextBoolean()) {
			type = FeatureGroupType.Alternative;
		} else {
			if (random.nextBoolean()) {
				type = FeatureGroupType.Or;
			} else {
				type = FeatureGroupType.And;
			}
		}
		action.addParameter("newGroupType", type);
		return action;
	}
}

class OptionalityEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditType(EditType.Set);
		action.setEditObjectType(EditObjectType.Optionality);

		action.addParameter("mandatory", Boolean.valueOf(random.nextBoolean()));
		action.addParameter("feature", Utility.getRandomFeature(model, random,
				false));
		return action;
	}
}

class CardinalityEditGenerator implements EditActionGenerator {
	public EditAction generate(final Random random, FeatureModel model,
			OperationType operationType) {
		EditAction action = new EditAction();
		action.setEditType(EditType.Set);
		action.setEditObjectType(EditObjectType.Cardinality);

		action.addParameter("minCard", random.nextInt(5));
		action.addParameter("maxCard", random.nextInt(5));
		action.addParameter("feature", Utility.getRandomFeature(model, random,
				false));
		return action;
	}
}