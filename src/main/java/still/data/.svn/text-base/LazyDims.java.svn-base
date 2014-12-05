package still.data;

import java.util.Iterator;

import com.mallardsoft.tuple.Pair;
import com.mallardsoft.tuple.Tuple;

/**
 * 
 * Dims class doesn't store extra points
 * Reaches back through the function to the group
 * to compute points on the fly as requested.
 * 
 * @author sfingram
 *
 */
public class LazyDims implements Table {

	private int rowCount 		= -1;
	private int colCount 		= -1;
	private Group group			= null;
	private Function function	= null;
	private boolean isSplit		= false;
	private Map map				= null;

	
	public LazyDims( Group group, Function function, Map map, boolean isSplit ) {
		
		this.isSplit	= isSplit;
		this.group 		= group;
		this.function 	= function;		
		this.map		= map;

		if( ! isSplit ) {
			
			this.rowCount 	= group.rows();
			this.colCount 	= group.columns();
		}
		else {
			
			this.rowCount 	= group.rows();
			this.colCount 	= map.rows();
		}
	}

	public LazyDims( Group group, Function function ) {
		this( group, function, null, false);
	}
	
	public LazyDims( Group group, Map map ) {
		this( group, null, map, true);
	}
	
	@Override
	public int columns() {

		return colCount;
	}

	@Override
	public double getMeasurement(int point_idx, int dim) {
		
		if( isSplit ) {
//			return function.compute(group, point_idx, dim );
			return function.compute( point_idx, dim );
		}
		
		double retVal = 0.0;
	    for ( 	Iterator<Pair<Integer, Integer>> colsIter = map.getSubColumnMap(dim).iterator(); 
				colsIter.hasNext(); ) {
	
			Pair<Integer, Integer> p 	= colsIter.next();
			int col 					= Tuple.get1(p);
			int subc 					= Tuple.get2(p);
		    retVal			 			+= group.getSubMeasurement(point_idx, col, subc);
		}
		return retVal;
	}

	@Override
	public double[] getPoint(int point_idx) {

		double[] row = new double[colCount];
		for( int i = 0; i < row.length; i++ ) {
			row[i] = this.getMeasurement( point_idx, i );
		}
		
		return row;
	}

	@Override
	public double[][] getTable() {

		return null;
	}

	@Override
	public boolean hasDirectAccess() {

		return false;
	}

	@Override
	public int rows() {

		return rowCount;
	}

	public void buildSplit(Map map, Group group) {

		this.isSplit = true;
		this.map = map;
		this.group = group;
		this.function = null;
	}
}
