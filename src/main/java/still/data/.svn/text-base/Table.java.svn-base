package still.data;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JPanel;

public interface Table {

	public enum ColType { NUMERIC, ORDINAL, CATEGORICAL, ATTRIBUTE }
	
	public int rows();
	public int columns();
	public boolean hasDirectAccess();
	public double[][] getTable();
	public double[] getPoint( int point_idx );
	public double getMeasurement( int point_idx, int dim );
	public String[] getCategories( int dim );
	public String getColName( int dim );
//	public String getAttributeString( int dim );
	public ColType[] getColTypes( );
	public ColType getColType( int dim );
	public JPanel getInputControl();
	public boolean hasInputControl();
	public ArrayList<TableListener> getTableListeners();
	public ArrayList<ActionListener> getActionListeners();
	public void addTableListener( TableListener listener );
	public void addActionListener( ActionListener listener );
	public void setMeasurement( int point_idx, int dim, double value );
	public ArrayList<DimensionDescriptor> getConstructedDimensions();
	//public void buildSplit( Map map, Group group );
}
