package still.operators;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;

/**
 * 
 * A lazy do-nothing operator
 * 
 * @author sfingram
 *
 */
public class IdentityOp extends Operator {

	protected String description = "Identity Operator Description";

	public String toString() {
		
		return "[I]";
	}
	
	public static String getMenuName() {
		
		return "Data:Identity";
	}

	public IdentityOp( Table newTable ) {
		
		super( newTable );
		
		map 			= Map.generateDiagonalMap( newTable.columns() );
		function 		= new IdentityFunction( newTable );
		isLazy  		= true;
	}
	
	public void columnAdded(int dim) {

		this.columnChanged(dim);
	}

	@Override
	public void columnChanged(int dim) {
		
		map 			= Map.generateDiagonalMap( input.columns() );
	}

	@Override
	public void featureChanged(int point_idx, int dim) {
		
	}

	@Override
	public void featuresChanged(int[][] indices) {
		
	}

	@Override
	public void rowAdded(int point_idx) {
		
	}

	@Override
	public void rowChanged(int point_idx) {
		
	}

	@Override
	public void tableChanged() {

		function 	= new IdentityFunction( input );
		map 		= Map.generateDiagonalMap( input.columns() );
	}	
}
