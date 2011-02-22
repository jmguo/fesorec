package preprocessing;

import java.util.*;
import java.io.*;
import featureide.fm.model.Feature;

public class ConvertToMMKP {
	public static final int MAX_SIZE = 400;
	
	private LinkedList<Feature> subTreeRoots = new LinkedList<Feature>();
	
	public void setSubTreeRoots(LinkedList<Feature> rs){
		subTreeRoots = rs;
	}
	public LinkedList<Feature> getSubTreeRoots(){
		return subTreeRoots;
	}

	public void filterCartesianFlattening(Feature root){
		subTreeRoots.add(root);
		findSub(root);
		cut();
		for(Feature subTreeRoot:subTreeRoots){
			if(subTreeRoot.hasChildren()){
				flattenAll(subTreeRoot);
			}
			else{
				Feature newChild = new Feature();
				newChild.setName(subTreeRoot.getName());
				newChild.setCost(subTreeRoot.getCost());
				newChild.setValue(subTreeRoot.getValue());
				subTreeRoot.addChild(newChild);
			}
		}
	}
	
	private void findSub(Feature feature){
		if(!feature.isMandatory()){
			subTreeRoots.add(feature);
		}
		else{
			for(Feature child:feature.getChildren()){
				findSub(child);
			}
		}
	}
	
	private void cut(){
		for(Feature subTreeRoot:subTreeRoots){
			if(subTreeRoot.getParent() != null){
				subTreeRoot.getParent().removeChild(subTreeRoot);
			}
		}
	}
	
	private void flattenAll(Feature feature){
		if(!feature.isMandatory()){
			optionalFeatureToXOR(feature);
		}
		if(feature.getChildrenCount()!= 0){
			for(Feature child:feature.getChildren()){
				flattenAll(child);
			}
			if(feature.isOr()){
				flattenOr(feature);
			}
			else if(feature.isAnd()){
				flattenAnd(feature);
			}
			else{
				flattenAlternative(feature);
			}
		}
	}
	
	private void optionalFeatureToXOR(Feature feature){
		Feature temp = new Feature();
		temp.setName("");
		Feature zero = new Feature();
		zero.setName("");
		zero.setCost(0);
		zero.setValue(0);
		
		temp.changeToAlternative();
		feature.getParent().replaceChild(feature,temp);
		temp.addChild(feature);
		temp.addChild(zero);
	}
	
	private void flattenOr(Feature feature){
		OrToAnd(feature);
		feature.changeToAnd();
		flattenAnd(feature);
	}
	
	private void flattenAnd(Feature feature){
		if(feature.getChildrenCount() != 0){
			LinkedList<Feature> newChildren = new LinkedList<Feature>();
			newChildren = getFollows(feature.getFirstChild());
			feature.setChildren(newChildren);
		}
	}
	
	private void flattenAlternative(Feature feature){
		LinkedList<Feature> removes = new LinkedList<Feature>();
		for(Feature child:feature.getChildren()){
			if(child.getChildrenCount() != 0){
				removes.add(child);
			}
		}
		for(Feature child:removes){
			for(Feature grandchild:child.getChildren()){
				feature.addChild(grandchild);
			}
			feature.removeChild(child);
		}
	}
	
	private void OrToAnd(Feature feature){
		for(Feature child:feature.getChildren()){
			if(child.getChildrenCount() == 0){
				optionalFeatureToXOR(child);
			}
			else{
				Feature zero = new Feature();
				zero.setCost(0);
				zero.setValue(0);
				zero.setName("");
				child.addChild(zero);
			}
		
		}
	}
	
	private LinkedList<Feature> getFollows(Feature feature){
		LinkedList<Feature> follows = new LinkedList<Feature>();
		LinkedList<Feature> generates = new LinkedList<Feature>();
		if(feature != feature.getParent().getLastChild()){
			Feature next = new Feature();
			next = feature.getParent().getChildren().get(feature.getParent().getChildIndex(feature)+1);
			follows = getFollows(next);
			if(feature.getChildrenCount() == 0){
				for(Feature follow:follows){
					Feature generate = new Feature();
					generate.setCost(follow.getCost() + feature.getCost());
					generate.setValue(follow.getValue() + feature.getValue());
					generate.setName(follow.getName() + feature.getName());
					generates.add(generate);
				}
			}
			else{
				for(Feature follow:follows){
					for(Feature child:feature.getChildren()){
						Feature generate = new Feature();
						generate.setCost(follow.getCost() + child.getCost());
						generate.setValue(follow.getValue() + child.getValue());
						generate.setName(follow.getName() + child.getName());
						generates.add(generate);
					}
				}
			}
			
		}
		else{
			if(feature.getChildrenCount() ==0){
				generates.add(feature);
			}
			else{
				generates = feature.getChildren();
			}
		}
		if(generates.size()>MAX_SIZE){
//			generates = sortFilter(generates);
			generates = randomFilter(generates);
		}
		return generates;
	}
	
	private LinkedList<Feature> randomFilter(LinkedList<Feature> features){
		LinkedList<Feature> suivivors = new LinkedList<Feature>();
		Random ran = new Random();
		int size = features.size();
		for(int i=0;i<MAX_SIZE;i++){
			suivivors.add(features.get(ran.nextInt(size)));
		}
		return suivivors;
	}
	
	private LinkedList<Feature> sortFilter(LinkedList<Feature> features){
		LinkedList<Feature> survivors = new LinkedList<Feature>();
		int size = features.size();
		double ratios[] = new double[size];
		int positions[] = new int[size];
		int i=0;
		for(Feature feature:features){
			positions[i] = i;
			if(feature.getCost() == 0)
				ratios[i] = 0;
			else
				ratios[i] = (double)feature.getValue()/feature.getCost();
			i++;
		}
		quickSort(ratios,positions,0,size-1);
		for(i=0;survivors.size()<MAX_SIZE;i++){
			if(i == size)
				break;
			if(i+1!=size && features.get(positions[i]).getCost() == features.get(positions[i+1]).getCost())
				continue;
			survivors.add(features.get(positions[i]));
		}
		return survivors;
	}
	
	private void quickSort(double[] ratios,int[] positions,int left,int right){
		int i,j;
		int middle,iTemp;
		i=left;
		j=right;
		middle = positions[left];
		do{
			while((ratios[positions[i]]<ratios[middle])&&(i<right))
				i++;
			while((ratios[positions[j]]>ratios[middle])&&(j>left))
				j--;
			if(i<=j){
				iTemp = positions[i];
				positions[i] = positions[j];
				positions[j] = iTemp;
				i++;
				j--;
			}
		}while(i<=j);
		if(left<j)
			quickSort(ratios,positions,left,j);
		if(right>i)
			quickSort(ratios,positions,i,right);
	}
	
	public void print(){

		for(Feature subTreeRoot:subTreeRoots){
			System.out.println(subTreeRoot.getChildrenCount());
			if(subTreeRoot.getChildrenCount() !=0){
				for(Feature child:subTreeRoot.getChildren()){
					System.out.println(child.getName()+"    "+child.getCost()+"    "+child.getValue());
				}
			}
			else{
				System.out.println(subTreeRoot.getName()+"    "+subTreeRoot.getCost()+"    "+subTreeRoot.getValue());
			}
			
		}
	}
	
	public void printFile(){
		try{
			PrintWriter out = new PrintWriter(new FileWriter("out"));
			for(Feature subTreeRoot:subTreeRoots){
				out.println(subTreeRoot.getChildrenCount());
				if(subTreeRoot.getChildrenCount() !=0){
					for(Feature child:subTreeRoot.getChildren()){
						out.println(child.getName()+"    "+child.getCost()+"    "+child.getValue());
					}
				}
				else{
					out.println(subTreeRoot.getName()+"    "+subTreeRoot.getCost()+"    "+subTreeRoot.getValue());
				}
			
			}
			out.close();
		}
		catch(Exception e){
			System.out.print("Can't open the file!");
		}
	}
	
	public int getTotalResource(){
		int sum=0;
		for(Feature subTreeRoot:subTreeRoots){
			int temp=0;
			if(subTreeRoot.getChildrenCount() !=0){
				for(Feature child:subTreeRoot.getChildren()){
					if(child.getCost() > temp)
						temp = child.getCost();
				}
			}
			else{
				temp = subTreeRoot.getCost();
			}
			sum += temp;
		}
		return sum*4/5;	//set totalResource=sum*0.8
	}
	
}
