package ga;

import java.util.*;

import featureide.fm.model.FeatureModel;
import featureide.fm.model.Feature;
import preprocessing.*;


//fitness function=V/R

public class GA2 {

	/**
	 * @param args
	 */	
	private boolean[][] choices;
	private double[] rate; 
	private int[] value;
	private int featureSize;
	private double totalResource;
	private static int POPULATION=30;
	private FeatureModel fm;
	
	public GA2(FeatureModel fm,double totalResource){
		this.fm=fm;
		this.totalResource=totalResource;
		
		featureSize=fm.getFeatureCount();
		
		choices=new boolean[POPULATION][];
		rate=new double[POPULATION];
		value=new int[POPULATION];
	}
	
	public int solve(){
		int res=initPopulation();
		if(res==-1) return -1;
		
		for(int generation=0;generation<100;generation++){  //生成的后代数
			boolean[] offspring=crossover();
			mutation(offspring);
			replace(offspring);
			//System.out.println("generation "+generation+":\t"+findMaxValue());
		}
		return findMaxValue();
	}
	
	private int initPopulation(){
		FMTransform format = new FMTransform(fm);
		int notFitCount=0;
		
		for(int i=0;i<POPULATION;i++){
			
			if(notFitCount<=10){  // false的概率为1/4
				choices[i]=randomSelect();
			}
			else if(notFitCount<=50){         //如果多次任然不符合，就把false的概率增大为3/4
				choices[i]=randomSelect2();
			}
			else{
				//choices[i]=randomSelect3();
				return -1;
			}
			
			choices[i]=format.generate(choices[i]);				
			if(!format.checkTotalResource(totalResource)){
				i--;
				notFitCount++;
			}
			else{
				value[i]=format.getTotalValue();
				int tr=format.getTotalResource();
				rate[i]=(double)value[i]/tr;
				notFitCount=0;   //next i, notFitCount to zero
			}
		}
		return 0;
	}
	
	// false 的概率为1/2,true的概率为1/2
	private boolean[] randomSelect(){    
		Random ran = new Random();
		boolean[] s = new boolean[featureSize];
		for(int i=0;i<s.length;i++)
			s[i] = ran.nextBoolean();  
		return s;
	}
	
	// false 的概率为3/4,true的概率为1/4
	private boolean[] randomSelect2(){
		Random ran = new Random();
		boolean[] s = new boolean[featureSize];
		for(int i=0;i<s.length;i++)
			s[i] = ( ran.nextBoolean() && ran.nextBoolean());  
		return s;
	}
	
	// 只有一个true
	private boolean[] randomSelect3(){
		boolean[] s = new boolean[featureSize];
		Random ran = new Random();
		
		int pos=ran.nextInt(featureSize);
		
		s[pos] = true;  
		
		return s;
	}

	private boolean[] crossover(){
		
		boolean[] offspring=new boolean[featureSize];
		
		Random ran=new Random();
		boolean[] p1=choices[Math.abs(ran.nextInt())%POPULATION];
		boolean[] p2=choices[Math.abs(ran.nextInt())%POPULATION];
		
		
		//uniform crossover
		boolean[] crossoverMask=new boolean[featureSize];
		for(int i=0;i<featureSize;i++){
			crossoverMask[i]=ran.nextBoolean();
		}
		for(int i=0;i<featureSize;i++){
			if(crossoverMask[i]){
				offspring[i]=p1[i];
			}
			else{
				offspring[i]=p2[i];
			}
		}
		
		
		////single-point crossover
		/*int midPos=featureSize/2;
		for(int i=0;i<midPos;i++){
			offspring[i]=p1[i];
		}
		for(int i=midPos+1;i<featureSize;i++){
			offspring[i]=p2[i];
		}*/
		
		
		return offspring;
		
	}
	
	private void mutation(boolean[] offspring){
		Random ran=new Random();
		int pos=(Math.abs(ran.nextInt()))%featureSize;
		offspring[pos]=!offspring[pos];
	}
	
	private void replace(boolean[] offspring){
		FMTransform format = new FMTransform(fm);
		boolean[] os=format.generate(offspring);
		if(format.checkTotalResource(totalResource)){
			int tv=format.getTotalValue();
			int tr=format.getTotalResource();
			double r=(double)tv/tr;
			
			//找population中的最小值
			double smin=Integer.MAX_VALUE;
			int k=-1;
			for(int i=0;i<POPULATION;i++){
				if(rate[i]<smin){
					smin=rate[i];
					k=i;
				}
			}
			if(k==-1){
				/*for(int i=0;i<POPULATION;i++){
					System.out.println(rate[i]);
				}*/
				return;
			}
			if(smin<r){
				value[k]=tv;
				rate[k]=r;
				choices[k]=os;
			}
		}
	}
	
	private int findMaxValue(){
		int smax=0;
		int k=-1;
		for(int i=0;i<POPULATION;i++){
			if(value[i]>smax){
				smax=value[i];
				k=i;
			}
		}
		
		/*
		//输出最优的选择
		System.out.println("最优值选择的特征值");
		int cnt=0;
		for(Feature feature:fm.getFeatures()){
			if(choices[k][cnt]){
				System.out.print(feature.getName()+"  ");
			}
			cnt++;
		}
		System.out.println();
		*/
		
		return smax;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
