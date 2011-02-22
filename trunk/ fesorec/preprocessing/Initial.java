package preprocessing;
import featureide.fm.model.Feature;
import java.util.*;

public class Initial {
	public static final int Rc = 10;
	public static int vratio=10;
	public static final int Vc = 20;
	private Random ran = new Random();
	
	public void initial(Feature feature){
		for(Feature child:feature.getChildren()){
			initial(child);
		}
		if(feature.getChildrenCount() == 0){
			feature.setCost(ran.nextInt(Rc));
			feature.setValue(feature.getCost()*vratio + ran.nextInt(Vc));
		}
	}

}