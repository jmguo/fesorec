import java.util.Calendar;
import java.util.LinkedList;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureModel;
import featureide.fm.util.TimedWoker;
import featureide.fm.generator.*;
import preprocessing.*;
import bblp.*;
import mheu.*;
import ga.*;
import aco.*;

import java.io.*;

public class EntranceACOsmall {
	/**
	 * @param args
	 */ 	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BufferedWriter bufferedWriter,bufferedWriter2;
		
		EntranceACOsmall entrance=new EntranceACOsmall();
		
		double totalTimeToMMKP,totalTimeOfBBLP,totalTimeOfMHEU,totalTimeOfACO,totalTimeOfACOInit;
		
		double totalRateOfMHEU,totalRateOfACO;
		
		double totalResultOfBBLP;
		
		int[] ACOResult=new int[100];
		int[] MHEUResult=new int[100];
		
		try{
			
			bufferedWriter=new BufferedWriter(new FileWriter("DetailData-200.txt"));
			bufferedWriter2=new BufferedWriter(new FileWriter("AverageData-200.txt"));
			
			String mp="Iteration100\\",modelPath;
			String sb;
			int[] dataSetNum={10,50,100,200};
			for(int i=0;i<dataSetNum.length;i++){
				totalTimeToMMKP=totalTimeOfBBLP=totalTimeOfMHEU=0;
				totalTimeOfACO=0;
				totalTimeOfACOInit=0;
				
				totalRateOfMHEU=totalRateOfACO=0;
				
				totalResultOfBBLP=0;
				
				int count=0;
				
				for(int j=100;j<200;j++){
					modelPath=mp+dataSetNum[i]+"\\FM-"+j+".m";
					
					entrance.initEntrance(modelPath);
					entrance.useMMKP();
					entrance.useACO();
					
					if(entrance.resultOfBBLP<=0 || entrance.resultOfACO<=0){
						System.out.println("skip..."+j);
						continue;
					} 
					
					System.out.println(modelPath);
					bufferedWriter.write(modelPath+"\n");
					
					/****************************输出时间*************************/
					sb="Time:\n"+
					entrance.timeToMMKP+"\t"+
					entrance.timeOfBBLP+"\t"+
					entrance.timeOfMHEU+"\t"+
					entrance.timeOfACO+"\t"+
					entrance.timeOfACOInit;
				
					bufferedWriter.write(sb+"\n");
					/************************************************************/
					
					
					
					/****************************计算总时间************************/
					totalTimeToMMKP+=entrance.timeToMMKP;
					totalTimeOfBBLP+=entrance.timeOfBBLP;
					totalTimeOfMHEU+=entrance.timeOfMHEU;					
					totalTimeOfACO+=entrance.timeOfACO;
					totalTimeOfACOInit+=entrance.timeOfACOInit;
					/***********************************************************/
					
					
					
					/*************************把ACO和MHEU的结果保存以计算方差*********************/
					ACOResult[count]=entrance.resultOfACO;
					MHEUResult[count]=entrance.resultOfMHEU;
					/**************************************************************************/
					
					totalResultOfBBLP+=entrance.resultOfBBLP;
					
					
					
					/****************************输出优化率*****************************/
					double rateOfMHEU,rateOfACO;
					rateOfMHEU=(double)entrance.resultOfMHEU/entrance.resultOfBBLP;
					rateOfACO=(double)entrance.resultOfACO/entrance.resultOfBBLP;
					 
					sb="OptRate:\n"+
						rateOfMHEU+"\t"+			
						rateOfACO+"\t";
					
					bufferedWriter.write(sb+"\n");
					/****************************************************************/
					
					
					
					/*****************************计算总优化率*************************/
					totalRateOfMHEU+=rateOfMHEU;
					totalRateOfACO+=rateOfACO;
					/****************************************************************/
					
					count++; 
				}
				
				
				/***********************输出平均时间和优化率以及方差*****************************/
				sb=	totalTimeToMMKP/count+"\t"+
					totalTimeOfBBLP/count+"\t"+
					totalTimeOfMHEU/count+"\t"+
					(totalTimeToMMKP+totalTimeOfBBLP)/count+"\t"+
					(totalTimeToMMKP+totalTimeOfMHEU)/count+"\t"+
					totalTimeOfACO/count+"\t"+
					totalTimeOfACOInit/count+"\t"+
					totalResultOfBBLP/count+"\t"+
					totalRateOfMHEU/count+"\t"+
					totalRateOfACO/count+"\t"+
					entrance.computeVariance(ACOResult,count)+"\t"+
					entrance.computeVariance(MHEUResult,count);
				
				bufferedWriter2.write(sb+"\n");
				/****************************************************************/
			}
			
			if(bufferedWriter!=null){
				bufferedWriter.close();
			}
			if(bufferedWriter2!=null){
				bufferedWriter2.close();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}		
	}
	
	private FeatureModel featureModel,featureModelForFCF;
	private double totalResource;   // total resource, assumed only one resource constrain
	
	private int groupNum;  // the number of groups
	private int[] len;   //the length of each group
	private int[][] required,value;
	
	
	public double timeToMMKP,timeOfBBLP,timeOfMHEU;
	public double timeOfACO;
	public double resultOfBBLP;
	public int resultOfMHEU;
	public int resultOfACO;
	public double timeOfACOWithoutInit,timeOfACOInit;
		
	public void initEntrance(String modelPath){
		
		//totalResource=Integer.MAX_VALUE;
		//totalResource=500;
			
		//读模型
		ModelGenerator mg = new ModelGenerator();
		
		//初始化featureModel
		featureModel = mg.loadFeatureModel(modelPath);
		Initial ini = new Initial();
		ini.initial(featureModel.getRoot());
		
		
		int cnt=0;
		int size=featureModel.getFeatures().size();
		int[] cost=new int[size];
		int[] value=new int[size];
		for(Feature feature:featureModel.getFeatures()){
			cost[cnt]=feature.getCost();
			value[cnt]=feature.getValue();
			cnt++;
		}
		
		//初始化featureModelForFCF
		featureModelForFCF=mg.loadFeatureModel(modelPath);
		cnt=0;
		for(Feature feature:featureModelForFCF.getFeatures()){
			feature.setCost(cost[cnt]);
			feature.setValue(value[cnt]);
			cnt++;
		}
		
		/*
		System.out.println("FeatureModel");
		for(Feature feature:featureModel.getFeatures()){
			System.out.println(feature.getName()+":	"+feature.getValue()+",	"+feature.getCost());
		}
		System.out.println("FeatureModelForFCF");
		for(Feature feature:featureModelForFCF.getFeatures()){
			System.out.println(feature.getName()+":	"+feature.getValue()+",	"+feature.getCost());
		}
		*/
	}
	
	private void useMMKP(){
			
		final TimedWoker workerToMMKP = new TimedWoker() {
			public void excute() {	
			
				ConvertToMMKP con = new ConvertToMMKP();
				con.filterCartesianFlattening(featureModelForFCF.getRoot());
				
				LinkedList<Feature> subTreeRoots = 	new LinkedList<Feature>();
				subTreeRoots = con.getSubTreeRoots(); 
				//con.print();
				//con.printFile();
				
				groupNum = subTreeRoots.size();
				//totalResource = Integer.MAX_VALUE;
				totalResource = con.getTotalResource();
				len = new int[groupNum];
				for(int i=0;i<groupNum;i++){
					len[i] = subTreeRoots.get(i).getChildrenCount();
				}
				required = new int [groupNum][];
				value = new int [groupNum][];
				for(int i=0;i<groupNum;i++){
					required[i] = new int[len[i]];
					value[i] = new int[len[i]];
					for(int j=0;j<len[i];j++){
						required[i][j] = subTreeRoots.get(i).getChildren().get(j).getCost();
						value[i][j] = subTreeRoots.get(i).getChildren().get(j).getValue();
					}
				}
			}
		};
		workerToMMKP.run();
		timeToMMKP=workerToMMKP.getStopWatch().getElapsedTime()/1000000.0;
		
		
		////输出groups
		/*for(int i=0;i<groupNum;i++){
			System.out.println("\n\nGroup "+i);
			for(int j=0;j<len[i];j++){
				System.out.println(subTreeRoots.get(i).getChildren().get(j).getName()+
						" "+required[i][j]+" "+value[i][j]);
			}
		}*/
		
		final TimedWoker workerOfBBLP = new TimedWoker() {
			public void excute() {
				useBBLP(groupNum,len,totalResource,required,value);
			}
		};
		workerOfBBLP.run();
		timeOfBBLP=workerOfBBLP.getStopWatch().getElapsedTime()/1000000.0;
		
		final TimedWoker workerOfMHEU = new TimedWoker() {
			public void excute() {
				useMHEU(groupNum,len,totalResource,required,value);
			}
		};
		workerOfMHEU.run();
		timeOfMHEU=workerOfMHEU.getStopWatch().getElapsedTime()/1000000.0;
	}
	
	private void useBBLP(int groupNum,int[] len,double totalResource,int[][] required,int[][] value){
		
		BBLP bblp=new BBLP(groupNum,len,totalResource,required,value);
		resultOfBBLP=bblp.solve();
		
		System.out.println("Using BBLP: "+resultOfBBLP);
	}
	
	private void useMHEU(int groupNum,int[] len,double totalResource,int[][] required,int[][] value){
				
		MHEU mheu=new MHEU(groupNum,len,totalResource,required,value);
		int[] rval = mheu.solve();
		
		if(rval == null){
			resultOfMHEU = -1;
		}
		else{
			resultOfMHEU=rval[0];
			//estimateMaxValueMHEU=rval[1];
		}
		
		System.out.println("Using MHEU: "+resultOfMHEU);
	}
/*	
	private void useACO(){
	
		final TimedWoker workerOfACO = new TimedWoker() {
			public void excute() {
				ACO ACO=new ACO(featureModel,totalResource);
				resultOfACO =ACO.solveWithoutEstimate();
				timeOfACOWithoutInit=ACO.getTime();
				System.out.println("Using ACO: " + resultOfACO);
			}
		};
		workerOfACO.run();
		timeOfACO=workerOfACO.getStopWatch().getElapsedTime()/1000000.0;
		
		timeOfACOInit=timeOfACO-timeOfACOWithoutInit;
		
		System.out.println("Time of ACO: " + timeOfACO);
	}
*/	
	private void useACO(){
		
		final TimedWoker workerOfACO = new TimedWoker() {
			public void excute() {
				ACO aco=new ACO(featureModel,totalResource);
				resultOfACO = aco.start();
				timeOfACOWithoutInit=aco.getTime();
				System.out.println("Using ACO: " + resultOfACO);
			}
		};
		workerOfACO.run();
		timeOfACO=workerOfACO.getStopWatch().getElapsedTime()/1000000.0;
		timeOfACOInit=timeOfACO-timeOfACOWithoutInit;
		
		System.out.println("Time of ACO: " + timeOfACO);
	}
	
	private double computeVariance(int[] var, int count){
		int smax=Integer.MIN_VALUE,smin=Integer.MAX_VALUE;
		for(int i=0;i<count;i++){
			if(var[i]>smax){
				smax=var[i];
			}
			if(var[i]<smin){
				smin=var[i];
			}
		}
		
		double[] normalVar=new double[count];
		for(int i=0;i<count;i++){
			normalVar[i]=(double)(var[i]-smin)/(smax-smin);
		}
		
		double sum=0;
		for(int i=0;i<count;i++){
			sum+=normalVar[i];
		}
		
		double avg=sum/count;
		
		sum=0;
		for(int i=0;i<count;i++){
			sum+=(normalVar[i]-avg)*(normalVar[i]-avg);
		}
	
		return sum/count;
	}
}
