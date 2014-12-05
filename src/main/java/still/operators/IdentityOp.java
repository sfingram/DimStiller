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

    @Override
    public void activate() {

    }

    @Override
    public String getSaveString() {
        return null;
    }

    @Override
    public void updateMap() {

    }

    @Override
    public void updateFunction() {

    }

    public void columnAdded(int dim) {

		this.columnChanged(dim);
	}

	public void columnChanged(int dim) {
		
		map 			= Map.generateDiagonalMap( input.columns() );
	}

	public void featureChanged(int point_idx, int dim) {
		
	}

	public void featuresChanged(int[][] indices) {
		
	}

	public void rowAdded(int point_idx) {
		
	}

	public void rowChanged(int point_idx) {
		
	}

	public void tableChanged() {

		function 	= new IdentityFunction( input );
		map 		= Map.generateDiagonalMap( input.columns() );
	}	
}
