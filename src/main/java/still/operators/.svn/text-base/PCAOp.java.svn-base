package still.operators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jblas.DoubleMatrix;
import org.jblas.Eigen;

import still.data.DimensionDescriptor;
import still.data.FloatIndexer;
import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.gui.OperatorView;
import still.gui.ScreePlot;

public class PCAOp extends Operator implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2136289045510700058L;
	protected int outdims = -1;
	protected String description = "PCA Operator Description";
	double[] e_vals = null;
	protected boolean append_pc;
	
	public double[] getEigenValues( ) {
		
		return e_vals;
	}
	
	public String toString() {
		
		return "[Reduce:PCA]";
	}
	
	public String getParamString() {
		
		return "PC's = "+(outdims);
	}
	
	public ArrayList<Integer> numericIndicesFromOutdims() {
		
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for( int i = 0; i < outdims; i++ ) {
			
			retVal.add(i);
		}
		
		return retVal;
	}
	
	public ArrayList<Integer> nonNumericIndicesFromOutdims() {
		
		ArrayList<Integer> retVal = new ArrayList<Integer>();
		for( int i = outdims; i < outdims+getNonNumericIndices( input ).size(); i++ ) {
			
			retVal.add(i);
		}
		
		return retVal;
	}
	

	public String getSaveString( ) {
		
		String saveString = "";

		saveString += outdims;
				
		return saveString;
	}

	public PCAOp( Table newTable, boolean isActive, String paramString ) {
		
		super( newTable );
		
		outdims = Integer.parseInt( paramString );
		if( outdims > getNumericIndices( input ).size() ) {
			
			outdims = getNumericIndices( input ).size();
		}
		
		this.isActive = isActive;
		if( isActive ) {
			
			map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
														numericIndicesFromOutdims(),			
														getNonNumericIndices( input ),
														nonNumericIndicesFromOutdims(),
														input.columns(), outdims+getNonNumericDims(input));
			function 		= new PCAFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims);
			e_vals = ((PCAFunction)function).getEigenValues();
			isLazy  		= true;
			setView( new PCAView( this ) );
		}
	}
	
	public PCAOp( Table newTable, boolean isActive ) {
		
		super( newTable );
		
		this.isActive = isActive;
		if( isActive ) {
			
			outdims 		= getNumericIndices( input ).size();
			if( append_pc ) {
				
				map 			= Map.fullBipartiteAppend( 	getNumericIndices( input ), 
															getNonNumericIndices( input ),									
															input.columns(),
															outdims);
			}
			else {
				
				map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
															numericIndicesFromOutdims(),			
															getNonNumericIndices( input ),
															nonNumericIndicesFromOutdims(),
															input.columns(), outdims+getNonNumericDims(input));
			}
			function 		= new PCAFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims);
			e_vals = ((PCAFunction)function).getEigenValues();
			isLazy  		= true;
			setView( new PCAView( this ) );
		}
	}
	
	public PCAOp( Table newTable, boolean isActive, int outdims) {
		
		super( newTable );
		
		this.isActive = isActive;
		this.outdims 	= outdims;
		if( isActive ) {
			
			if( append_pc ) {
				
				map 			= Map.fullBipartiteAppend( 	getNumericIndices( input ), 
															getNonNumericIndices( input ),									
															input.columns(),
															outdims);
			}
			else {
				
				map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
															numericIndicesFromOutdims(),			
															getNonNumericIndices( input ),
															nonNumericIndicesFromOutdims(),
															input.columns(), outdims+getNonNumericDims(input));
			}
			function 		= new PCAFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims );
			e_vals = ((PCAFunction)function).getEigenValues();
			isLazy  		= true;
			setView( new PCAView( this ) );
		}
	}
	
	public void activate() {
	
		this.isActive = true;
		
		if( this.outdims < 0 ) {
		
			this.outdims = getNumericIndices( input ).size();
		}
		
		if( append_pc ) {
			
			map 			= Map.fullBipartiteAppend( 	getNumericIndices( input ), 
														getNonNumericIndices( input ),									
														input.columns(),
														outdims);
		}
		else {
			
			map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
														numericIndicesFromOutdims(),			
														getNonNumericIndices( input ),
														nonNumericIndicesFromOutdims(),
														input.columns(), outdims+getNonNumericDims(input));
		}
		function 		= new PCAFunction( input, getNumericIndices( input ), getNonNumericIndices( input ), outdims );
		e_vals = ((PCAFunction)function).getEigenValues();
		isLazy  		= true;
		setView( new PCAView( this ) );
	}
	
	public void updateMap() {
		
		if( outdims > getNumericIndices( input ).size() ) {
			
			outdims = getNumericIndices( input ).size();
		}
		
		if( append_pc ) {
			
			map 			= Map.fullBipartiteAppend( 	getNumericIndices( input ), 
														getNonNumericIndices( input ),									
														input.columns(),
														outdims);
		}
		else {
			
			map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
														numericIndicesFromOutdims(),			
														getNonNumericIndices( input ),
														nonNumericIndicesFromOutdims(),
														input.columns(), outdims+getNonNumericDims(input));
		}
	}
	
	public void updateFunction() {
		
		function 		= new PCAFunction( input, getNumericIndices( input ),getNonNumericIndices( input ), outdims );
		e_vals = ((PCAFunction)function).getEigenValues();
		
		ArrayList<Double> univarQuant = new ArrayList<Double>();
		ArrayList<String> univarNames = new ArrayList<String>();
		ArrayList<Integer> dtcols = getDimTypeCols(ColType.NUMERIC);
		int p = 0;
		for( double v : getEigenValues() ) {
			univarQuant.add( v );
			univarNames.add( input.getColName(dtcols.get(p)));
			p++;
		}
		((PCAView)view).scree.setUnivar(univarQuant,univarNames);
	}
		

	public class PCAView extends OperatorView implements ChangeListener {

//		JSlider slider = null;
		ScreePlot scree = null;
		JCheckBox jcb = new JCheckBox( "Use Log Scale" );
		JCheckBox jcbAppend = new JCheckBox( "Append Components" );
		JPanel optionPanel = new JPanel();
		
		public PCAView(PCAOp o) {
			super(o);

			jcbAppend.addActionListener(this);
			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int k = 1;
			
			for( int i : o.getNumericIndices(input) ) {
				labelTable.put(new Integer(k),new JLabel(""+k));
				k++;
			}

			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			ArrayList<Integer> dtcols = o.getDimTypeCols(ColType.NUMERIC);
			int p = 0;
			for( double v : o.getEigenValues() ) {
				
				univarQuant.add( v );
				univarNames.add( o.input.getColName( dtcols.get(p) ) );
				p++;
			}
			scree = new ScreePlot(	univarQuant,
									univarNames,
									true,
									new Comparator<Double>() {
										public int compare(Double o1, Double o2) {
											if( o1 < o2 ) {
												return 1;
											}
											else if( o1 > o2 ) {
												return -1;
											}
											return 0;
										}
									},
									null);
			scree.cutoff = o.outdims - 1;
			scree.useDimensionNames = false;
			scree.isCutoffLeft = true;
			
			scree.addLogStateCheckbox( jcb );
			scree.addChangeListener( this );
			
			this.add(scree, "Center");
			optionPanel.setLayout( new GridLayout(1,2) );
			optionPanel.add( jcb );
			optionPanel.add( jcbAppend );
			this.add(optionPanel,	"South");
		}

		public void actionPerformed(ActionEvent e) {

			if( e.getSource() instanceof JCheckBox ) {
				
				append_pc = this.jcbAppend.isSelected();
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else {
				
				NumberFormat form = NumberFormat.getInstance();
				form.setMaximumIntegerDigits(4);
				form.setMaximumFractionDigits(3);
				form.setMinimumFractionDigits(2);
				
				ArrayList<Double> univarQuant = new ArrayList<Double>();
				ArrayList<String> univarNames = new ArrayList<String>();
				ArrayList<Integer> dtcols = ((PCAOp)this.operator).getDimTypeCols(ColType.NUMERIC);
				
				int p = 0;
				for( double v : ((PCAOp)this.operator).getEigenValues() ) {
					
					univarQuant.add( v );
					univarNames.add( ((PCAOp)this.operator).input.getColName( dtcols.get(p) ) );
					p++;
				}
	
				scree.setUnivar(univarQuant, univarNames);
			}
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -4476127570420657983L;

		@Override
		public void stateChanged(ChangeEvent e) {

			if( e.getSource() instanceof JSlider ) {
				JSlider source = (JSlider)e.getSource();
			    if (!source.getValueIsAdjusting()) {
			        int val = (int)source.getValue();
			        ((PCAOp)operator).outdims = val;
			        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));		    
			    }
			}
			if( e.getSource() instanceof ScreePlot ) {

				((PCAOp)operator).outdims = ((ScreePlot)e.getSource()).cutoff+1;
		        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));		    
			}
		}
	}

	public class PCAFunction implements Function {

		public DoubleMatrix prinComp 	= null;
		private DoubleMatrix newCoords	= null;
		private int numericDims = 0;
		private Table input = null;
		ArrayList<Integer> nonNumericIdxs = null;
		double[] eigenValues;
		int outDims = -1;
		
		public double[] getEigenValues() {
			
			return eigenValues;
		}
		
		public class EigValVecPair {
			
			public double value = 0.0;
			public double[] vec = null;
			
			public EigValVecPair( double val, double[] v ) {
				
				value = val;
				vec = v;
			}
			
		}
		
		public class EigComparator implements Comparator<EigValVecPair> {

			public int compare(EigValVecPair o1, EigValVecPair o2) {

				if( o1.value >=o2.value ) {
					return -1;
				}

				return 1;
			}
			
			
		}
		
		public DoubleMatrix eigSort( DoubleMatrix[] eig, int cutoff ) {
			
			double [][] vCopy 		= eig[0].transpose().toArray2();//.getArrayCopy();
			eigenValues 	= eig[1].diag().data;//eig.getRealEigenvalues();
			EigValVecPair[] p = new EigValVecPair[eigenValues.length];
			
			for( int i = 0; i < p.length; i++ ) {
				
				p[i] = new EigValVecPair( eigenValues[i], vCopy[i] );
			}
			Arrays.sort(p, new EigComparator());
			
			double[][] vCopyNew = new double[cutoff][];
			for( int i = 0; i < cutoff; i++ ) {
				
				vCopyNew[i] = p[i].vec;
			}
			
			DoubleMatrix retval = new DoubleMatrix( vCopyNew );
			return retval.transpose();
		}
		
		public PCAFunction( Table table, ArrayList<Integer> numericIdxs, ArrayList<Integer> nonNumericIdxs, int dims ) {

			// Convert data to a zero means matrix
			DoubleMatrix zeroMeans = DoubleMatrix.zeros(table.rows(), numericIdxs.size());
			double[] sums 	= new double[numericIdxs.size()];
			this.numericDims = numericIdxs.size();
			this.nonNumericIdxs = nonNumericIdxs;
			this.outDims = dims;
			this.input = table;
			
			
			for( int i = 0; i < table.rows(); i++ ) {
				for( int j = 0; j < numericIdxs.size(); j++ ) {
					
					sums[j] 	+= 	table.getMeasurement(i, numericIdxs.get(j));
				}
			}
			for( int j = 0; j < numericIdxs.size(); j++ ) {

				sums[j] 	= sums[j]	/((double) table.rows());
				for( int i = 0; i < table.rows(); i++ ) {
					zeroMeans.put(i, j, table.getMeasurement(i, numericIdxs.get(j))-sums[j]);
				}
			}

			// Get normalized covariance matrix
			DoubleMatrix covariance = zeroMeans.transpose().mmul(zeroMeans);
			double normalizationConstant = 1.0 /((double)zeroMeans.rows - 1.0);
			covariance = covariance.mmul(normalizationConstant);
			
			
			// Get Eigenvectors and Eigenvalues
			DoubleMatrix[] eig = Eigen.symmetricEigenvectors(covariance);//.eig();
			prinComp 	= eigSort( eig, dims );
			newCoords 	= zeroMeans.mmul(prinComp);
			
		}
		
		@Override
		public Table apply(Group group) {

			return null;
		}

		@Override
		public double compute(int row, int col) {
									
			if( append_pc ) {
				
				if( col >= input.columns() ) {
					
					return newCoords.get( row, col - input.columns() );
				}
				
				return input.getMeasurement(row, col);
			}
			
			if( col < outDims ) 
				return newCoords.get(row, col);
//			if( col < numericDims ) 
//				return newCoords.get(row, col);
			
			if( input == null ) {
				System.out.println("ALARM1");
			}
			if( nonNumericIdxs == null ) {
				System.out.println("ALARM2");
			}
			return input.getMeasurement(row, nonNumericIdxs.get(col-outDims));
		}

		@Override
		public Group inverse(Table dims) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double[] invert( Map map, int row, int col, double value ) {
			
			// TODO HELP ME, properly invert, puh lease
			
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
	/**
	 * 
	 * Test routine loads a CSV table, displays the dimensions
	 * Applies the cutoff then the collect stage then the PCA stage 
	 * and displays the new dimensions
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		System.out.println("Loading file " + args[0] );
		
		Table testTable = TableFactory.fromCSV( args[0] );
		
		System.out.println("Table is : " + testTable.rows() + " x " + testTable.columns() );
		
		CutoffOp testOp = new CutoffOp( testTable, true, 0 );
		
		System.out.println("Cutoff table is " + testOp.rows() + " x " + testOp.columns() );

		PearsonCollectOp test2Op = new PearsonCollectOp( testOp, true, 0.8 );
		
		System.out.println("Collect table is " + test2Op.rows() + " x " + test2Op.columns() );

		PCAOp test3Op = new PCAOp( test2Op, true, 1 );
		
		System.out.println("Reduce table is " + test3Op.rows() + " x " + test3Op.columns() );
	}
	
	/**
	 * Return the constructed dimensions for this operator (use the map!)
	 */
	public ArrayList<DimensionDescriptor> getConstructedDimensions() {

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(3);
		
		ArrayList<DimensionDescriptor> ret = new ArrayList<DimensionDescriptor>();
		
		if( append_pc ) {
		
			for( int i = 0; i < this.outdims; i++ ) {
				
				String desc = "( ";
				int rowd = ((PCAFunction)this.function).prinComp.rows;
				double[] row_vals = new double[rowd];
				ArrayList<String> sub_values = new ArrayList<String>();
				for( int j = 0; j < rowd; j++ ) {
					
					row_vals[j] = ((PCAFunction)this.function).prinComp.get(j, i);
				}
				FloatIndexer[] eval_indexer = FloatIndexer.sortEVals(row_vals);
				ArrayList<Integer> numIdxs = getNumericIndices( input );
				
				for( FloatIndexer fi: eval_indexer ) {
									
					if( Math.abs( fi.val ) > 1e-1 ) {
						
						sub_values.add(this.input.getColName( numIdxs.get(fi.idx) ) + " : " + nf.format( fi.val ));
					}
				}
				desc += " )";
				ret.add( new DimensionDescriptor( getColName(input.columns()+i), "", sub_values ) );
			}
		}
		else {
			
			for( int i = 0; i < this.outdims; i++ ) {
				
				String desc = "( ";
				int rowd = ((PCAFunction)this.function).prinComp.rows;
				double[] row_vals = new double[rowd];
				ArrayList<String> sub_values = new ArrayList<String>();
				for( int j = 0; j < rowd; j++ ) {
					
					row_vals[j] = ((PCAFunction)this.function).prinComp.get(j, i);
				}
				FloatIndexer[] eval_indexer = FloatIndexer.sortEVals(row_vals);
				ArrayList<Integer> numIdxs = getNumericIndices( input );
				
				for( FloatIndexer fi: eval_indexer ) {
									
					if( Math.abs( fi.val ) > 1e-1 ) {
						
						sub_values.add(this.input.getColName( numIdxs.get(fi.idx) ) + " : " + nf.format( fi.val ));
					}
				}
				desc += " )";
				ret.add( new DimensionDescriptor( getColName(i), "", sub_values ) );
			}
		}
		
		if( ret.size() > 0 ) {
			
			return ret;
		}
		
		return null;
	}

	public static String getMenuName() {
		
		return "Reduce:PCA";
	}
}
