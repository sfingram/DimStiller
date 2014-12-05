package still.data;

import java.util.ArrayList;

/**
 * 
 * Creates a group as a stored table in memory
 * 
 * @author sfingram
 *
 */
public class MemoryGroup implements Group {

	double[][][] table = null;
	private int rowCount 		= -1;
	private int colCount 		= -1;

	public MemoryGroup( double[][][] table ) {

		rowCount 		= table.length;
		colCount 		= table[0].length;
		this.table 		= table;
	}
	
	@Override
	public void buildGroup(Map map, Table dims) {

		table = new double[dims.rows()][][];
		rowCount = dims.rows();
		colCount = map.columns();
		ArrayList<ArrayList<Integer>> samples = new ArrayList<ArrayList<Integer>>();
		for( int j = 0; j < colCount; j++ ) {
			
			samples.add( map.getColumnSamples(j) );
		}
		for( int i = 0; i < rowCount; i++ ) {
			
			table[i] = new double[colCount][];
			for( int j = 0; j < colCount; j++ ) {
				
				table[i][j] = new double[samples.get(j).size()];
			}			
		}
	}

	@Override
	public int columns() {
		// TODO Auto-generated method stub
		return colCount;
	}

	@Override
	public double[] getMeasurements(int row, int column) {
		// TODO Auto-generated method stub
		return table[row][column];
	}

	@Override
	public double[][] getPoint(int row) {
		// TODO Auto-generated method stub
		return table[row];
	}

	@Override
	public double getSubMeasurement(int row, int column, int subcolumn) {
		// TODO Auto-generated method stub
		return table[row][column][subcolumn];
	}

	@Override
	public double[][][] getTable() {
		// TODO Auto-generated method stub
		return table;
	}

	@Override
	public boolean hasDirectAccess() {

		return true;
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

		return table[0][column].length;
	}

}
