package still.data;

public interface Function {

	public int[] outMap( );
//	public double compute( Group group, int row, int col );
	public double compute( int row, int col );
	public double[] invert( Map map, int row, int col, double value );
	public Table apply( Group group );
	public Group inverse( Table dims );
}
