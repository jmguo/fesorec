package featureide.fm.io.guidsl;

import java.io.File;

import org.prop4j.Node;
import org.prop4j.SatSolver;
import org.sat4j.specs.TimeoutException;

import featureide.fm.io.UnsupportedModelException;
import featureide.fm.io.guidsl.ModelCheckFailure.FailureType;
import featureide.fm.model.FeatureModel;
import featureide.fm.util.NodeCreator;

public class FeatureModelChecker {

	private static FeatureModelWriter fw = new FeatureModelWriter();
	private static FeatureModelReader fr = new FeatureModelReader();

	public static ModelCheckFailure satCheck(FeatureModel fm) {
		try {
			Node root = NodeCreator.createNodes(fm);
			if (new SatSolver(root, 1000).isSatisfiable()) {
				return null;
			} else {
				ModelCheckFailure failure = new ModelCheckFailure();
				failure.setErrorMsg("SAT failed");
				failure.setType(FailureType.SATFailure);
				// failure.setModel(fm);
				return failure;
			}
		} catch (TimeoutException e1) {
			e1.printStackTrace();
			ModelCheckFailure failure = new ModelCheckFailure();
			failure.setErrorMsg("Time out!");
			failure.setType(FailureType.SATFailure);
			// failure.setModel(fm);
			return failure;
		}

	}

	public static ModelCheckFailure guidslCheck(FeatureModel fm) {
		fw.setFeatureModel(fm);
		String content = fw.writeToString();

		FeatureModel newModel = new FeatureModel();
		fr.setFeatureModel(newModel);
		try {
			fr.readFromString(content);
		} catch (UnsupportedModelException e) {
			if (fm.getFeatureCount() <= 1)
				return null;
			ModelCheckFailure failure = new ModelCheckFailure();
			failure.setErrorMsg(e.getMessage());
			failure.setType(FailureType.GuidslFailure);
			// failure.setModel(fm);
			return failure;
		}
		return null;
	}

	public static void saveFailedModel(String path, FeatureModel fm) {
		fw.setFeatureModel(fm);

		fw.writeToFile(new File(path));
	}
}
