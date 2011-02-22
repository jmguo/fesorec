package mheu;
import java.util.Arrays;

import preprocessing.Initial;


public class MHEU {

	private class ValueAndRequired implements Comparable<ValueAndRequired>{
		int required,value;
		public ValueAndRequired(int required,int value){
			this.required=required;
			this.value=value;
		}
		public int compareTo(ValueAndRequired vr2){
			return value-vr2.value;			
		}
	}
	
	int groupNum;   // the number of groups
	double totalResource;   // total resource, assumed only one resource constrain
	int[] len;   //the length of each group
	ValueAndRequired[][] vr;
	
	
	int[] selected,selectedCopy;
	int consumedResource;
	int totalValue;
	
	public MHEU(int groupNum,int[] len,double totalResource,int[][] required,int[][] value){
		this.groupNum=groupNum;
		this.len=len;
		this.totalResource=totalResource;
		
		vr=new ValueAndRequired[groupNum][];
		for(int i=0;i<groupNum;i++){
			vr[i]=new ValueAndRequired[len[i]];
			for(int j=0;j<len[i];j++){
				vr[i][j]=new ValueAndRequired(required[i][j],value[i][j]);
			}
		}
	}
	
	public int[] solve(){
		selected=new int[groupNum];
		consumedResource=0;  // the amount of resource consumption
		
		if(step1()==-1){
			return null;    // no solution
		}
		step2();
		return getValue(selected);
		
		/*if(step3()==-1){  
			return getValue(selectedCopy);
		}
		else{
			return getValue(selected);				
		}*/
	}
	
	public int solveWithoutEstimate(){
		selected=new int[groupNum];
		consumedResource=0;  // the amount of resource consumption
		
		if(step1()==-1){
			return -1;    // no solution
		}
		step2();
		return getValueWithoutEstimate(selected);
		
		/*if(step3()==-1){  
			return getValue(selectedCopy);
		}
		else{
			return getValue(selected);				
		}*/
	}
	
	private int[] getValue(int[] s){
		int res=0;
		int estimateMaxValue=0;
		
		for(int i=0;i<groupNum;i++){
			res+=vr[i][s[i]].value;
			estimateMaxValue += Initial.Vc + vr[i][s[i]].required*Initial.vratio;
		}
		return new int[]{res,estimateMaxValue};
	}
	
	private int getValueWithoutEstimate(int[] s){
		int res=0;
		
		for(int i=0;i<groupNum;i++){
			res+=vr[i][s[i]].value;
		}
		return res;
	}
	
	private int step1(){
		//step1
		for(int i=0;i<groupNum;i++){
			Arrays.sort(vr[i]);
			selected[i]=0;
			consumedResource+=vr[i][0].required;
		}
		while(true){
			if(consumedResource<=totalResource){
				break;    //ok
			}
			//find highest deltaAij;
			double deltaAmax=0;
			int cnti=-1,cntj=-1;
			for(int i=0;i<groupNum;i++){
				for(int j=selected[i]+1;j<len[i];j++){
					if(vr[i][j].required<vr[i][selected[i]].required){
						double deltaAij=vr[i][selected[i]].required-vr[i][j].required;
						if(deltaAij>deltaAmax){
							deltaAmax=deltaAij;
							cnti=i; cntj=j;
						}
					}
				}
			}
			if(deltaAmax<=0){  // not found
				return -1;
			}
			consumedResource=consumedResource-vr[cnti][selected[cnti]].required+vr[cnti][cntj].required;
			selected[cnti]=cntj;
		}
		return 0;
	}
	
	private int step2(){
		//step2
		while(true){
			double deltaAmax=0,deltaPmax=0;
			int cnti=-1,cntj=-1;
			for(int i=0;i<groupNum;i++){
				for(int j=selected[i]+1;j<len[i];j++){
					if(consumedResource-vr[i][selected[i]].required+vr[i][j].required>totalResource)
						continue;    //must subject to the resource constrain
					
					double deltaAij=vr[i][selected[i]].required-vr[i][j].required;   // right?
					if(deltaAij>deltaAmax){
						deltaAmax=deltaAij;
						cnti=i;
						cntj=j;
					}
					if(deltaAij<=0){    // if no such item
						double deltaP=(vr[i][selected[i]].value-vr[i][j].value)/deltaAij;
						if(deltaP>deltaPmax){
							deltaPmax=deltaP;
							cnti=i;
							cntj=j;
						}
					}
				}
			}
			if(deltaAmax==0 && deltaPmax==0){  // no such item is found
				break;
			}
			
			consumedResource=consumedResource-vr[cnti][selected[cnti]].required+vr[cnti][cntj].required;
			selected[cnti]=cntj;
			
			//if(deltaAmax==0){     // no such item is found
			//	break;
			//}
		}
		
		selectedCopy=new int[groupNum];
		totalValue=0;
		for(int i=0;i<groupNum;i++){
			totalValue+=vr[i][selected[i]].value;
			selectedCopy[i]=selected[i];
		}
		
		return 0;
	}
	
	private int step3(){
		//step3.1
		int newValue=totalValue;
		double deltaPmax=0;
		int cnti=-1,cntj=-1;
		for(int i=0;i<groupNum;i++){
			for(int j=selected[i]+1;j<len[i];j++){
				if(vr[i][selected[i]].value<vr[i][j].value){
					double deltaVij=vr[i][selected[i]].value-vr[i][j].value;
					double deltaAij=vr[i][selected[i]].required-vr[i][j].required;
					//double deltaAij=(double)(vr[i][selected[i]].required-vr[i][j].required) / (totalResource-consumedResource);
					double deltaP=deltaVij/deltaAij;
					if(deltaP>deltaPmax){
						deltaPmax=deltaP;
						cnti=i; cntj=j;
					}
				}
			}
		}
		if(deltaPmax>0){
			newValue=newValue-vr[cnti][selected[cnti]].value+vr[cnti][cntj].value;
			consumedResource=consumedResource-vr[cnti][selected[cnti]].required+vr[cnti][cntj].required;
			selected[cnti]=cntj;
		}
		
		//step3.2
		while(true){
			deltaPmax=0;
			cnti=cntj=-1;
			for(int i=0;i<groupNum;i++){
				for(int j=selected[i]-1;j>=0;j--){
					if(newValue-vr[i][selected[i]].value+vr[i][j].value>totalValue){
						//double deltaAij=(double)(vr[i][selected[i]].required-vr[i][j].required) / (totalResource-consumedResource);
						double deltaAij=vr[i][selected[i]].required-vr[i][j].required;
						double deltaVij=vr[i][selected[i]].value-vr[i][j].value;
						double deltaP=deltaAij/deltaVij;
						if(deltaP>deltaPmax){
							deltaPmax=deltaP;
							cnti=i; cntj=j;
						}
					}
				}
			}
			
			//step3.3
			if(deltaPmax<=0){
				return -1;   //no item found in step 3.2. use the selected; 
			}
			consumedResource=consumedResource-vr[cnti][selected[cnti]].required+vr[cnti][cntj].required;
			selected[cnti]=cntj;
			if(consumedResource<=totalResource){
				step2();
				if(step3()==-1) return -1;
				else return 0;
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int groupNum=3;
		int[] len={2,2,2};
		double totalResource=60;
		int[][] required={{22,17},{17,10},{17,11}};
		int[][] value={{30,20},{26,16},{23,12}};
		MHEU mheu=new MHEU(groupNum,len,totalResource,required,value);
		
	}
}
