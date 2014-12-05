package still.data;

import java.util.ArrayList;

public class LazyGroup implements Group {

	private int rowCount 		= -1;
	private int colCount 		= -1;
	private Table dims			= null;
	private Function function	= null;
	private boolean isGrouping	= false;
	private Map map				= null;

	public LazyGroup( Table dims, Function function, Map map, boolean isGrouping ) {
		
		this.isGrouping	= isGrouping;
		this.dims 		= dims;
		this.function 	= function;		
		this.map		= map;

		if( isGrouping ) {
			
			this.rowCount 	= dims.rows();
			this.colCount 	= map.columns();
		}
		else {
			
			this.rowCount 	= dims.rows();
			this.colCount 	= function.outMap().length;
		}
	}

	public LazyGroup( Table dims, Function function ) {
		this( dims, function, null, false);
	}
	
	public LazyGroup( Table dims, Map map ) {
		this( dims, null, map, true);
	}
	
	@Override
	public void buildGroup(Map map, Table dims) {

		this.isGrouping	= true;
		this.dims 		= dims;
		this.function 	= null;		
		this.map		= map;
		this.rowCount 	= dims.rows();
		this.colCount 	= map.columns();
	}

	@Override
	public int columns() {

		return colCount;
	}

	@Override
	public double[] getMeasurements(int row, int column) {

		double[] retval = new double[ subcolumns(column) ];
		for(int i = 0; i < retval.length; i++) {

			retval[i] = getSubMeasurement(row, column, i);
		}		
		return retval;
	}

	@Override
	public double[][] getPoint(int row) {
		
		double[][] retval = new double[colCount][];
		for(int i = 0; i < retval.length; i++) {

			retval[i] = getMeasurements(row, i );
		}		
		return retval;
	}

	@Override
	public double getSubMeasurement(int row, int column, int subcolumn) {

		if( isGrouping ) {
			
			return dims.getMeasurement(row, map.getColumnSamples(column).get(subcolumn) );
		}
		return function.invert(dims, row, column)[subcolumn];
	}

	@Override
	public double[][][] getTable() {

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

	@Override
	public ArrayList<Integer> subcolumnList() {

		return null;
	}

	@Override
	public int subcolumns(int column) {
		
		if( isGrouping ) {
			
			return map.getColumnSamples(column).size();
		}
		return function.invert(dims, 0, column).length;
	}

}
