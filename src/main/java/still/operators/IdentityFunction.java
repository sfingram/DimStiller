package still.operators;

import java.io.Serializable;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Table;

public class IdentityFunction implements Function, Serializable {


	private Table table = null;
	
	public IdentityFunction( Table table ) {
		this.table = table;
	}
	
	@Override
	public Table apply(Group group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double compute(int row, int col) {
		
		return table.getMeasurement(row, col);
	}

	@Override
	public Group inverse(Table dims) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] invert( Map map, int row, int col, double value ) {
		
		double[] ret = new double[1];
		
		ret[0] = value;
		
		return ret;
	}

	@Override
	public int[] outMap() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
