import java.util.Calendar;
import java.util.LinkedList;
import featureide.fm.model.Feature;
import featureide.fm.model.FeatureModel;
import featureide.fm.util.TimedWoker;
import featureide.fm.generator.*;
import preprocessing.*;
import mheu.*;
import ga.*;

import java.io.*;

public class EntranceGALarge {
	/**
	 * @param args
	 */ 	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		BufferedWriter bufferedWriter,bufferedWriter2;
		
		EntranceGALarge entrance=new EntranceGALarge();
		
		double totalTimeToMMKP,totalTimeOfMHEU,totalTimeOfGA,totalTimeOfGAInit;
		
		double totalRateOfMHEU,totalRateOfGA;
		
		double totalEstimateGA;
		double totalEstimateMHEU;
		
		int[] GAResult=new int[100];
		int[] MHEUResult=new int[100];
		
		bufferedWriter2=null;
		
		try{
			
			//bufferedWriter=new BufferedWriter(new FileWriter("DetailData-bigger-1000.txt"));
			
			
			String mp="Iteration100\\",modelPath;
			String sb;
			int[] dataSetNum={500, 1000, 2000};
			for(int i=0;i<dataSetNum.length;i++){
				
				String tmpath="testResults\\500-2000\\AverageData-"+dataSetNum[i]+".txt";
				
				bufferedWriter2=new BufferedWriter(new FileWriter(tmpath));
				
				totalTimeToMMKP=totalTimeOfMHEU=0;
				totalTimeOfGA=0;
				totalTimeOfGAInit=0;
				
				totalRateOfMHEU=totalRateOfGA=0;
				
				totalEstimateGA=0;
				totalEstimateMHEU=0;
				
				int count=0;
				
				for(int j=107;j<200;j++){
					if(j==100||j==103||j==104) continue;
					
					modelPath=mp+dataSetNum[i]+"\\FM-"+j+".m";
					System.out.println(modelPath);
					
					entrance.initEntrance(modelPath);
					entrance.useMMKPForEstimate();
					entrance.useMMKP();
					entrance.useGA(); 
					
					if(entrance.resultOfGA<=0){
						System.out.println("skip..."+j);
						continue;
					}
					
					//bufferedWriter.write(modelPath+"\n");
					
					/****************************输出时间*************************/
					//sb="Time:\n"+
					//entrance.timeToMMKP+"\t"+
					//entrance.timeOfMHEU+"\t"+
					//entrance.timeOfGA+"\t"+
					//entrance.timeOfGAInit;
					
					//System.out.println(sb);
				
					//bufferedWriter.write(sb+"\n");
					/************************************************************/
					
					
					
					/****************************计算总时间************************/
					totalTimeToMMKP+=entrance.timeToMMKP;
					totalTimeOfMHEU+=entrance.timeOfMHEU;					
					totalTimeOfGA+=entrance.timeOfGA;
					totalTimeOfGAInit+=entrance.timeOfGAInit;
					/***********************************************************/
					
					
					
					/*************************把GA和MHEU的结果保存以计算方差*********************/
					GAResult[count]=entrance.resultOfGA;
					MHEUResult[count]=entrance.resultOfMHEU;
					/**************************************************************************/
					
					
					
					/****************************输出优化率*****************************/
					double rateOfMHEU,rateOfGA;
					
					//int estimateMaxValue=Math.max(entrance.estimateMaxValueGA, entrance.estimateMaxValueMHEU);
					//int estimateMaxValue=entrance.estimateMaxValueGA;
					rateOfMHEU=(double)entrance.resultOfMHEU/entrance.estimateMaxValueMHEU;
					rateOfGA=(double)entrance.resultOfGA/entrance.estimateMaxValueGA;
					 
					//System.out.println("EstimateMaxValueGA="+entrance.estimateMaxValueGA);
					
					//sb="OptRate:\n"+
					//	rateOfMHEU+"\t"+			
					//	rateOfGA+"\t";
					
					//System.out.println(sb);
					//bufferedWriter.write(sb+"\n");
					/****************************************************************/
					
					
					totalEstimateGA+=entrance.estimateMaxValueGA;
					totalEstimateMHEU+=entrance.estimateMaxValueMHEU;
					
					
					/*****************************计算总优化率*************************/
					totalRateOfMHEU+=rateOfMHEU;
					totalRateOfGA+=rateOfGA;
					/****************************************************************/
					
					count++; 
				}
				
				
				/***********************输出平均时间和优化率以及方差*****************************/
				sb=	totalTimeToMMKP/count+"\t"+
					totalTimeOfMHEU/count+"\t"+
					(totalTimeToMMKP+totalTimeOfMHEU)/count+"\t"+
					totalTimeOfGA/count+"\t"+
					totalTimeOfGAInit/count+"\t"+
					totalEstimateMHEU/count+"\t"+
					totalRateOfMHEU/count+"\t"+
					totalEstimateGA/count+"\t"+
					totalRateOfGA/count+"\t"+
					entrance.computeVariance(GAResult,count)+"\t"+
					entrance.computeVariance(MHEUResult,count);
				
				bufferedWriter2.write(sb+"\n");
				/****************************************************************/
				
				if(bufferedWriter2!=null){
					bufferedWriter2.close();
				}
				
			}
			
			//if(bufferedWriter!=null){
			//	bufferedWriter.close();
			//}
			if(bufferedWriter2!=null){
				bufferedWriter2.close();
			}
		}
		catch(Exception e){
			try{
				if(bufferedWriter2!=null){
					bufferedWriter2.flush();
					bufferedWriter2.close();
				}
			}
			catch(Exception e2){
				
			}
		}		
	}
	
	private FeatureModel featureModel,featureModelForFCF;
	private FeatureModel featureModelForEstimate;
	private double totalResource;   // total resource, assumed only one resource constrain
	
	private int groupNum;  // the number of groups
	private int[] len;   //the length of each group
	private int[][] required,value;
	
	
	public double timeToMMKP,timeOfMHEU;
	public double timeOfGA;
	public int resultOfGA,resultOfMHEU;
	public double timeOfGAWithoutInit,timeOfGAInit;
	
	public int estimateMaxValueMHEU;
	public int estimateMaxValueGA;
		
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
		
		
		featureModelForEstimate=mg.loadFeatureModel(modelPath);
		cnt=0;
		for(Feature feature:featureModelForEstimate.getFeatures()){
			feature.setCost(cost[cnt]);
			feature.setValue(cost[cnt]*Initial.vratio + Initial.Vc);
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
	
	private void useMMKPForEstimate(){
		
		
		int groupNum2;  // the number of groups
		int[] len2;   //the length of each group
		int[][] required2,value2;
		
		ConvertToMMKP con = new ConvertToMMKP();
		con.filterCartesianFlattening(featureModelForEstimate.getRoot());
		
		LinkedList<Feature> subTreeRoots = 	new LinkedList<Feature>();
		subTreeRoots = con.getSubTreeRoots(); 
		//con.print();
		//con.printFile();
		
		double totalResource2=Integer.MAX_VALUE;
		
		groupNum2 = subTreeRoots.size();
		len2 = new int[groupNum2];
		for(int i=0;i<groupNum2;i++){
			len2[i] = subTreeRoots.get(i).getChildrenCount();
		}
		required2 = new int [groupNum2][];
		value2 = new int [groupNum2][];
		for(int i=0;i<groupNum2;i++){
			required2[i] = new int[len2[i]];
			value2[i] = new int[len2[i]];
			for(int j=0;j<len2[i];j++){
				required2[i][j] = subTreeRoots.get(i).getChildren().get(j).getCost();
				value2[i][j] = subTreeRoots.get(i).getChildren().get(j).getValue();
			}
		}
		MHEU mheu=new MHEU(groupNum2,len2,totalResource2,required2,value2);
		int[] rval = mheu.solve();
		
		if(rval == null){
			estimateMaxValueMHEU = -1;
		}
		else{
			estimateMaxValueMHEU=rval[0];
		}
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
				//totalResource=Integer.MAX_VALUE;
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
		
		final TimedWoker workerOfMHEU = new TimedWoker() {
			public void excute() {
				useMHEU(groupNum,len,totalResource,required,value);
			}
		};
		workerOfMHEU.run();
		timeOfMHEU=workerOfMHEU.getStopWatch().getElapsedTime()/1000000.0;
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
		//System.out.println("EstimateMaxValueMHEU="+estimateMaxValueMHEU);
	}
	
	private void useGA(){
	
		final TimedWoker workerOfGA = new TimedWoker() {
			public void excute() {
				GA ga=new GA(featureModel,totalResource);
				int[] rval=ga.solve();
				if(rval==null){
					resultOfGA=-1;
				}
				else{
					resultOfGA = rval[0];
					estimateMaxValueGA = rval[1];
				}
				timeOfGAWithoutInit=ga.getTime();
			}
		};
		workerOfGA.run();
		timeOfGA=workerOfGA.getStopWatch().getElapsedTime()/1000000.0;
		
		timeOfGAInit=timeOfGA-timeOfGAWithoutInit;
		
		System.out.println("Using GA: " + resultOfGA);
		//System.out.println("EstimateMaxValueGA="+estimateMaxValueGA);
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
