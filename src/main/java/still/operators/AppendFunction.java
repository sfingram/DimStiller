package still.operators;

import java.io.Serializable;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Table;

/**
 * 
 * Function appends the columns specified by newColumns to the existing
 * input table.
 * 
 * @author sfingram
 *
 */
public class AppendFunction implements Function, Serializable {

	Table table = null;
	double[][] newColumns = null;
	Map map = null;
	int[] remap = null;
	
	public AppendFunction( Table table, double[][] ns ) {
		
		this( table, ns, null );
	}
	
	public AppendFunction( Table table, double[][] ns, Map map ) {
		
		this.map = map;
		
		// if there is any remapping, available to the operator
		// build the remap for fast access
		if( (this.map != null) && (ns.length > 0)) {
			remap = new int[this.map.columns() - ns[0].length];
			for( int i = 0; i < remap.length; i++ ) {
				
				remap[i] = this.map.getColumnSamples(i).get(0);
			}
		}
		else {
			remap = new int[table.columns()];
			for( int i = 0; i < remap.length; i++ ) {
				
				remap[i] = i;
			}
		}
		
		this.table = table;
		this.newColumns = ns;
	}

	@Override
	public Table apply(Group group) {

		return null;
	}

	@Override
	public double compute(int row, int col) {

		if( col < remap.length ) {
			
			return table.getMeasurement(row, remap[col]);
		}
		
		return newColumns[row][col-remap.length];
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
