package still.operators;

import still.data.Operator;
import still.data.Table;

public class OperatorFactory {
	
	public static String[] operators = {"Cutoff","Collect","PCA","KMeans","Identity","Glimmer"};
	
	public static Operator getOperator( String opName, Table input ) {
		
		if( opName.equals("Cutoff" ) ) {
			
			return new CutoffOp(input, true);
		}
		if( opName.equals("Collect" ) ) {
			
			return new PearsonCollectOp(input, true);
		}
		if( opName.equals("PCA" ) ) {
			
			return new PCAOp(input, true);
		}
		if( opName.equals("KMeans" ) ) {
			
			return new KMeansOp(input);
		}
		if( opName.equals("Identity" ) ) {
			
			return new IdentityOp(input);
		}
		if( opName.equals("Glimmer" ) ) {
			
			return new GlimmerOp(input,true);
		}
		
		return null;
	}

}
