package aco;

import java.util.HashMap;
import java.util.Random;
import preprocessing.*;

import featureide.fm.model.Constraint;
import featureide.fm.model.FeatureModel;
import featureide.fm.model.Feature;
import featureide.fm.util.TimedWoker;

public class ACO {
	public static final int NUM_ANT = 10;
	public static final int ITERATION = 25;
	public static final int K_BEST = 5;
	private static final double PERCENTAGE = 0.7;
	private static double[] a = {1,1,-1,1};

	private FeatureModel featureModel;
	private double[] pheromone;
	private double[] ratio;
	private boolean[][] select;
	private boolean[][] bestList;
	private int[][] distance;
	private double totalResource = 10000;
	private int numFeatures;
	private FMTransform format;
	private HashMap<Integer,Feature> features = new HashMap<Integer,Feature>();
	private HashMap<String,Feature> names = new HashMap<String,Feature>();
	private HashMap<Feature,Integer> integers = new HashMap<Feature,Integer>();
	
	private double time = 0;
	
	public ACO(FeatureModel featureModel,double totalResource){

		this.featureModel=featureModel;
		this.totalResource=totalResource;
		numFeatures=featureModel.getFeatureCount();
		
	}
	
	public double getTime(){
		return time;
	}
	
	public int start(){
		initHash();
		
		//叶节点值的初始化
		//Initial ini = new Initial();
		//ini.initial(featureModel.getRoot());
		
		//将所选节点标准化，并输出时间
		format = new FMTransform(featureModel);
		distance = new int[numFeatures][numFeatures];
		pheromone = new double[numFeatures];
		ratio = new double[NUM_ANT];
		select = new boolean[NUM_ANT][numFeatures];
		bestList = new boolean[K_BEST][numFeatures];
		
		initHash();
		initDis();
		
		int notFitCount=0;
		for(int i=0;i<NUM_ANT;i++){
			
			if(notFitCount<=10){  // false的概率为1/4
				select[i] = randomSelect();
			}
			else if(notFitCount<=50){         //如果多次任然不符合，就把false的概率增大为3/4
				select[i] = randomSelect2();
			}
			else{
				//select[i]=randomSelect3();
				return -1;
			}
			
			select[i] = format.generate(select[i]);
			ratio[i] = format.getRatio();
			if(!format.checkTotalResource(totalResource)){
				i--;
				notFitCount++;
			}
			else{
				notFitCount=0;
			}
		}	
		
		
		final TimedWoker workerOfACO = new TimedWoker() {
			public void excute() {
				for(int i=0;i<ITERATION;i++){
					if(i != 0){
						chooseRemainsOnce();
					}
					chooseBest();
					calcuPhero();
					updateSelect();
				}
			}
		};
		workerOfACO.run();
		time=workerOfACO.getStopWatch().getElapsedTime()/1000000.0;

		int result = getResult();
		//输出所选节点和排除节点的所有名称
//		format.printChoices();
//		format.printDefinites();
//		format.printExcludes();
//		format.printAll();
		return result;
	}
	public int[] startWithEstimate(){
		int[] rval = new int[2];
		rval[0] = start();
		rval[1] = format.getMaxValue();
		return rval;
	}
	
	private void initHash(){
		int i=0;
		for(Feature feature:featureModel.getFeatures()){
			features.put(i, feature);
			names.put(feature.getName(),feature);
			integers.put(feature, i);
			i++;
		}
	}

	private void initDis(){
		for(int i=0;i<numFeatures;i++){
			for(int j=0;j<numFeatures;j++){
				if(i==j)
					distance[i][j] = 0;
				else
					distance[i][j] = Integer.MAX_VALUE;
			}
		}
		
		distanceCon(integers.get(featureModel.getRoot()));		

	}

	private void distance(int posting){
		Feature parent = features.get(posting).getParent();
		if(parent !=null){
			int postingP = integers.get(features.get(posting).getParent());
			for(int j=0;j<numFeatures;j++){
				if(distance[postingP][j]!=Integer.MAX_VALUE){
					distance[posting][j] = distance[postingP][j]+1;
					distance[j][posting] = distance[j][postingP]+1;
				}
			}
		}
		for(Feature child:features.get(posting).getChildren()){
			distance(integers.get(child));	
		}
	}
	
	private void distanceCon(int posting){
		Feature parent = features.get(posting).getParent();
		if(parent !=null){
			int postingP = integers.get(features.get(posting).getParent());
			for(int j=0;j<numFeatures;j++){
				if(distance[postingP][j]!=Integer.MAX_VALUE){
					if(distance[postingP][j]==-1)
						distance[posting][j] = -1;
					else
						distance[posting][j] = distance[postingP][j]+1;
				}
				if(distance[j][postingP]!=Integer.MAX_VALUE){
					if(distance[j][postingP]==-1)
						distance[j][posting] = -1;
					else
						distance[j][posting] = distance[j][postingP]+1;
				}
			}
			if(parent.isAlternative()){
				for(Feature child:parent.getChildren()){
					int postingB = integers.get(child);
					if(posting != postingB){
						distance[posting][postingB] = -1;
						distance[postingB][posting] = -1;
					}
				}
			}
			for(Constraint constraint:featureModel.getConstraints()){
				if(constraint.getFeatureA() == features.get(posting).getName()){
					if(constraint.getConstraintType()==Constraint.ConstraintType.exclude){
						distance[posting][integers.get(names.get(constraint.getFeatureB()))] = -1;
						distance[integers.get(names.get(constraint.getFeatureB()))][posting] = -1;
					}
					if(constraint.getConstraintType()==Constraint.ConstraintType.require){
						distance[posting][integers.get(names.get(constraint.getFeatureB()))] = 1;
						distance[integers.get(names.get(constraint.getFeatureB()))][posting] = 1;
					}
				}

				if(constraint.getFeatureB() == features.get(posting).getName()){
					if(constraint.getConstraintType()==Constraint.ConstraintType.exclude){
						distance[posting][integers.get(names.get(constraint.getFeatureA()))] = -1;
						distance[integers.get(names.get(constraint.getFeatureA()))][posting] = -1;
					}
					if(constraint.getConstraintType()==Constraint.ConstraintType.require){
						distance[posting][integers.get(names.get(constraint.getFeatureA()))] = 1;
						distance[integers.get(names.get(constraint.getFeatureA()))][posting] = 1;
					}
				}
			}
		}
		for(Feature child:features.get(posting).getChildren()){
			distanceCon(integers.get(child));	
		}
	}
	
	private boolean[] randomSelect(){
		Random ran = new Random();
		boolean[] s = new boolean[featureModel.getFeatureCount()];
		for(int i=0;i<s.length;i++)
			s[i] = ran.nextBoolean();
		return s;
	}

	// false 的概率为3/4,true的概率为1/4
	private boolean[] randomSelect2(){
		Random ran = new Random();
		boolean[] s = new boolean[featureModel.getFeatureCount()];
		for(int i=0;i<s.length;i++)
			s[i] = ( ran.nextBoolean() && ran.nextBoolean());  
			//s[i] = false;   //   初始化清空
		return s;
	}
	
	// 只有一个true
	private boolean[] randomSelect3(){
		boolean[] s = new boolean[featureModel.getFeatureCount()];
		Random ran = new Random();
		
		int pos=ran.nextInt(featureModel.getFeatureCount());
		
		s[pos] = true;  
		
		return s;
	}

	private void chooseRemainsOnce() {
		for (int i = 0; i < NUM_ANT; i++) {
			select[i] = format.generate(select[i]);
			calcuDistance(i);
			int[] posting = sortSM(i);
			int chance=numFeatures/2;
			for (int j = 0; j < numFeatures; j++) {
				if (posting[j] == -1)
					continue;
				if (format.addChoice(posting[j]) && format.checkTotalResource(totalResource)) {
					select[i] = format.getAddString(select[i]);     
				} 
				else{
					chance--;
				}
				if(chance == 0)
					break;
			}
			ratio[i] = format.getRatio();
		}
//		printSelect();
	}

	private int[] sortSM(int ant) {
		int dis[] = new int[numFeatures];
		int order[] = new int[numFeatures];
		double SM[] = new double[numFeatures];
		dis = calcuDistance(ant);
		for (int i = 0; i < numFeatures; i++) {
			if (!select[ant][i]) {
				SM[i] = Math.pow((double) 1 / dis[i], 1)
						* Math.pow(pheromone[i], 2);
				order[i] = i;
			} else {
				SM[i] = 0;
				order[i] = i;
			}
		}
		quickSort(SM, order, 0, numFeatures - 1);
		for (int i = 0; i < numFeatures; i++) {
			if (select[ant][order[i]])
				order[i] = -1;
		}
		return order;
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
		
	private int[] calcuDistance(int ant){
		int dis[] = new int[numFeatures];
		for(int i=0;i<numFeatures;i++){
			dis[i] = 100000;
			for(int j=0;j<numFeatures;j++){
				if(select[ant][j]){
					if(distance[i][j]<dis[i])
						dis[i] = distance[i][j];
				}
			}
		}
		return dis;
	} 
	
	private void chooseBest(){
		for(int i=0;i<K_BEST;i++){
			int temp=0;
			for(int j=0;j<NUM_ANT;j++){
				if(ratio[j] > ratio[temp]){
					temp = j;
				}
			}
			bestList[i] = select[temp].clone();
			ratio[temp]=0;
		}
	}

	private void calcuPhero() {
		int sumOfSelect = 0, sumOfBest = 0;
		int[] numOfSelect = new int[featureModel.getFeatureCount()];
		int[] numOfBest = new int[featureModel.getFeatureCount()];
		for (int i = 0; i < NUM_ANT; i++) {
			for (int j = 0; j < select[i].length; j++)
				if (select[i][j]) {
					sumOfSelect++;
					numOfSelect[j]++;
				}
		}
		for (int i = 0; i < K_BEST; i++) {
			for (int j = 0; j < bestList[i].length; j++)
				if (bestList[i][j]) {
					sumOfBest++;
					numOfBest[j]++;
				}
		}
		for (int i = 0; i < pheromone.length; i++) {
			pheromone[i] = a[0] * numOfBest[i] / sumOfBest 
						+ a[1] * numOfBest[i] / sumOfBest 
						+ a[2] * numOfSelect[i]	/ sumOfSelect 
//						+ a[3];
						+ a[3] * (double)Initial.Rc/features.get(i).getCost();
		}
	}
	
	private void updateSelect(){
		Random ran = new Random();
		int ant = ran.nextInt(K_BEST);
		
		for(int i=0;i<NUM_ANT;i++){
			for(int j=0;j<select[i].length;j++){
				if(bestList[ant][j]==true && ran.nextDouble()<PERCENTAGE){
					select[i][j] = true;
				}
				else{
					select[i][j] = false;
				}
			}
			select[i] = format.generate(select[i]);
			if(!format.checkTotalResource(totalResource)){
				select[i] = bestList[ant].clone();	
			}
		}
	}

	private int getResult(){
		int mvalue = 0;
		int mant = 0;
		for(int i=0;i<NUM_ANT;i++){
			int value = 0;
			for(int j=0;j<numFeatures;j++){
				if(select[i][j])
					value += features.get(j).getValue();
			}
			if(value > mvalue){
				mvalue = value;
				mant = i;
			}
		}
		format.generate(select[mant]);
		return mvalue;
	}
	

}
