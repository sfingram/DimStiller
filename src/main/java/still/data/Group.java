package still.data;

import java.util.ArrayList;

/**
 * 
 * A group is an interface for Function objects to compute
 * a new set of dims.  
 * 
 * Groups computes a set (for each output dimensions) of many to 
 * one mappings for a set of input dimensions
 * to a single output dimension.
 * 
 * @author sfingram
 *
 */
public interface Group {

	public void buildGroup( Map map, Table dims );
	public int rows();
	public int columns();
	public int subcolumns(int column);
	public ArrayList<Integer> subcolumnList();
	public boolean hasDirectAccess();
	public double[][][] getTable();
	public double[][] getPoint( int row );
	public double[] getMeasurements( int row, int column );
	public double getSubMeasurement( int row, int column, int subcolumn );
}
