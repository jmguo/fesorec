package bblp;
import java.util.*;


public class BBLP {

	private class NODE{
		boolean[] groupStatus;
		int freeGroup,branchingGroup;
		double objectValue,upperBound,avaResource;
		
		public NODE(boolean[] groupStatus,double objectValue,
				int freeGroup,double avaResource,int branchingGroup,double upperBound){
			this.groupStatus=groupStatus;
			this.objectValue=objectValue;
			this.freeGroup=freeGroup;
			this.avaResource=avaResource;
			this.branchingGroup=branchingGroup;
			this.upperBound=upperBound;
			
		}
	}
	
	PriorityQueue<NODE> treePriQueue=new PriorityQueue<NODE>(1,new Comparator<NODE>(){
		public int compare(NODE n1,NODE n2){
			if(n2.upperBound<n1.upperBound){
				return -1;
			}
			else if(n2.upperBound==n1.upperBound){
				return 0;
			}
			else return 1;
		}
	});
	

	int groupNum;  // the number of groups
	double totalResource;   // total resource, assumed only one resource constrain
	int[] len;   //the length of each group
	int[][] required,value;
	
	
	
	public BBLP(int groupNum,int[] len,double totalResource,int[][] required,int[][] value){
		this.totalResource=totalResource;
		this.groupNum=groupNum;
		this.len=len;
		this.required=required;
		this.value=value;
	}
	
	public double solve(){
		double[][] solution;   //solution vector
		boolean[] groupStatus;  //group status
		
		int freeGroup=groupNum;
		double avaResource=totalResource;
		double objectValue=0;
		double upperBound;
		
		//init
		solution=new double[groupNum][];
		for(int i=0;i<groupNum;i++){
			solution[i]=new double[len[i]];
			for(int j=0;j<len[i];j++){
				solution[i][j]=0;
			}
		}
		groupStatus=new boolean[groupNum];
		for(int i=0;i<groupNum;i++){
			groupStatus[i]=false;			
		}
		
		upperBound=solveLP(groupStatus,avaResource,solution);		
		int branchingGroup=findBranchingGroup(solution,groupStatus);
		treePriQueue.add(new NODE(groupStatus,objectValue,freeGroup,
				avaResource,branchingGroup,upperBound));
		
		while(true){
			if(treePriQueue.isEmpty()) return -1;   //нч╫Б
			
			NODE maxLiveNode=treePriQueue.poll();
			
			if(maxLiveNode.freeGroup<=0){
				return maxLiveNode.upperBound;
			}
			
			groupStatus=maxLiveNode.groupStatus;
			branchingGroup=maxLiveNode.branchingGroup;
			
			//if(branchingGroup==-1) continue;
			
			groupStatus[branchingGroup]=true;
			freeGroup=maxLiveNode.freeGroup-1;
			
			for(int j=0;j<len[branchingGroup];j++){
				avaResource=maxLiveNode.avaResource-required[branchingGroup][j];
				objectValue=maxLiveNode.objectValue+value[branchingGroup][j];
				upperBound=solveLP(groupStatus,avaResource,solution);
				if(upperBound>=0){
					upperBound+=objectValue;
					int tmpb=findBranchingGroup(solution,groupStatus);
					treePriQueue.add(new NODE(groupStatus.clone(),objectValue,freeGroup,
							avaResource,tmpb,upperBound));
				}
			}
		}
		
	}
	
	/*private double[][] copyArray(double[][] s){
		double[][] res;
		res=new double[groupNum][];
		for(int i=0;i<groupNum;i++){
			res[i]=new double[len[i]];
			for(int j=0;j<len[i];j++){
				res[i][j]=s[i][j];
			}
		}
		return res;
	}*/
	
	private int findBranchingGroup(double[][] x,boolean[] groupStatus){
		double smax=0;
		int b=-1;
		for(int i=0;i<groupNum;i++){
			if(groupStatus[i]==false){
				b=i;
				for(int j=0;j<len[i];j++){
					if(x[i][j]>smax){
						smax=x[i][j];
						b=i;
					}
				}
			}
		}
		return b;
	}
	
	private double solveLP(boolean[] groupStatus,double avaResource, double[][] solution){
		int totalNum=0,freeGroupNum=0;
		for(int i=0;i<groupNum;i++){
			if(groupStatus[i]==false){
				totalNum+=len[i];
				freeGroupNum++;
			}
		}
		double[][] A=new double[freeGroupNum+1][totalNum+1];
		double[] B=new double[totalNum];
		
		for(int i=0;i<freeGroupNum+1;i++)
			for(int j=0;j<totalNum+1;j++)
				A[i][j]=0;
		
		/* 
		 * xij+xij+...<=1
		 * rij*xij<=avaResource
		*/
		int beginIndex=0;
		int cnt=0;
		for(int i=0;i<groupNum;i++){
			if(groupStatus[i]==false){
				for(int j=0;j<len[i];j++){
					A[cnt][j+beginIndex]=1;
					A[freeGroupNum][j+beginIndex]=required[i][j];
					B[j+beginIndex]=value[i][j];
				}
				A[cnt][totalNum]=1;
				beginIndex+=len[i];
				cnt++;
			}
		}
		A[freeGroupNum][totalNum]=avaResource;
		
		double[] x=new double[totalNum];
		
		LinearProgram lp = new LinearProgram(1,freeGroupNum+1,totalNum,freeGroupNum+1,0,0,A,B);
		double smax=lp.solve(x);
		
		beginIndex=0;
		for(int i=0;i<groupNum;i++){
			if(groupStatus[i]==false){
				for(int j=0;j<len[i];j++){
					solution[i][j]=x[j+beginIndex];
				}
				beginIndex+=len[i];
			}
		}
		
		return smax;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int groupNum=3;
		int[] len={2,2,2};
		int totalResource=40;
		int[][] required={{22,17},{17,10},{17,11}};
		int[][] value={{30,20},{26,16},{23,12}};
		BBLP bblp=new BBLP(groupNum,len,totalResource,required,value);
		double ans=bblp.solve();
		System.out.println(ans==-1?"no solution":ans);

	}

}
