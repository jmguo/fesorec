package featureide.fm.model;

public enum EvolutionStrategy {

	DeleteAllChildren, MergeChildrenToParent, MergeChildToAnotherCompound, Random;

	public String toString() {
		switch (this) {
		case DeleteAllChildren:
			return "RemoveAllChildren";
		case MergeChildrenToParent:
			return "ReconnectChildrenToParent";
		case MergeChildToAnotherCompound:
			return "ReconnectChildrenToAnotherNF";
		case Random:
			return "Arbitrary";
		default:
			return "Arbitrary";
		}
	}
}
