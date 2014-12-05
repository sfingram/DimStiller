package still.operators;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import still.data.DimensionDescriptor;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.data.Table.ColType;
import still.gui.OperatorView;
import still.gui.ScreePlot;
import still.operators.PCAOp.PCAFunction;

public class GlimmerOp extends Operator implements Serializable {

	protected int outdims = -1;
	protected int savedOutdims = -1;
	protected String description = "Glimmer Operator Description";
	double[] s_vals = null;
	ProgressMonitor pm = null;
	
	public static String getMenuName() {
		
		return "Reduce:MDS";
	}

	public double[] getStressValues( ) {
		
		return s_vals;
	}
	
	public String toString() {
		
		return "[Reduce:MDS]";
	}
	
	public String getSaveString( ) {
		
		String saveString = "";

		saveString += outdims;
				
		return saveString;
	}

	public String getParamString() {
		
		return "Embedding Dimension = "+(outdims);
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

	public GlimmerOp( Table newTable, boolean isActive, String paramString ) {
		
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

			function 		= new GlimmerFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims);
			s_vals = ((GlimmerFunction)function).getStressValues();
			isLazy  		= true;
			setView( new GlimmerView( this ) );
		}
	}

	public GlimmerOp( Table newTable, boolean isActive ) {
		
		super( newTable );
		
		this.isActive = isActive;
		if( isActive ) {
			
			outdims 		= getNumericIndices( input ).size();
			map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
														numericIndicesFromOutdims(),			
														getNonNumericIndices( input ),
														nonNumericIndicesFromOutdims(),
														input.columns(), outdims+getNonNumericDims(input));

//			pm = new ProgressMonitor( null,"TEST","",0,100 );
//			
//			(new Thread() {
//		        public void run() {
//		        	pm.setProgress(50);
//		        }
//			}).start();
			function 		= new GlimmerFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims);
//					SwingUtilities.invokeLater(new Runnable() {
//	        public void run() {
//	        	
//	        	pm.close();
//	        }
//		});
			s_vals = ((GlimmerFunction)function).getStressValues();
			isLazy  		= true;
			setView( new GlimmerView( this ) );
		}
	}
	
	public GlimmerOp( Table newTable, boolean isActive, int outdims) {
		
		super( newTable );
		
		this.isActive = isActive;
		this.outdims 	= outdims;
		if( isActive ) {
			
			map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
														numericIndicesFromOutdims(),			
														getNonNumericIndices( input ),
														nonNumericIndicesFromOutdims(),
														input.columns(), outdims+getNonNumericDims(input));
			function 		= new GlimmerFunction( newTable, getNumericIndices( input ), getNonNumericIndices( input ), outdims );
			s_vals = ((GlimmerFunction)function).getStressValues();
			isLazy  		= true;
			setView( new GlimmerView( this ) );
		}
	}
	
	public void activate() {
		
		isActive = true;
		
		if(outdims < 0) {
			
			outdims 		= getNumericIndices( input ).size();
		}
		map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
													numericIndicesFromOutdims(),			
													getNonNumericIndices( input ),
													nonNumericIndicesFromOutdims(),
													input.columns(), outdims+getNonNumericDims(input));
		function 		= new GlimmerFunction( input, getNumericIndices( input ), getNonNumericIndices( input ), outdims );
		s_vals = ((GlimmerFunction)function).getStressValues();
		isLazy  		= true;
		setView( new GlimmerView( this ) );
	}
	
	public void updateMap() {

		if( outdims > getNumericIndices( input ).size() ) {
			
			outdims = getNumericIndices( input ).size();
		}
		
		map 			= Map.fullBipartiteExcept( 	getNumericIndices( input ), 
													numericIndicesFromOutdims(),			
													getNonNumericIndices( input ),
													nonNumericIndicesFromOutdims(),
													input.columns(), outdims+getNonNumericDims(input));
	}
	
	public void updateFunction() {
		
		
		function 		= new GlimmerFunction( input, getNumericIndices( input ),getNonNumericIndices( input ), outdims );

		s_vals = ((GlimmerFunction)function).getStressValues();
		
		ArrayList<Double> univarQuant = new ArrayList<Double>();
		ArrayList<String> univarNames = new ArrayList<String>();
		ArrayList<Integer> dtcols = this.getDimTypeCols(ColType.NUMERIC);
		int k = 0;
		for( double v : getStressValues() ) {
			
			univarQuant.add( v );
			univarNames.add( input.getColName( dtcols.get(k) ));
			k++;
		}
		((GlimmerView)view).scree.setUnivar(univarQuant, univarNames);
	}
		

	public class GlimmerView extends OperatorView implements ChangeListener {

		JSlider slider = null;
		ScreePlot scree = null;
		JCheckBox jcb = new JCheckBox( "Use Log Scale" );
		
		public GlimmerView(GlimmerOp o) {
			super(o);

			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int k = 1;
			
			for( int i : o.getNumericIndices(input) ) {
				labelTable.put(new Integer(k),new JLabel(""+k));
				k++;
			}
//			for( int i = 1; i <= o.input.columns(); i++) {				
//				labelTable.put(new Integer(k),new JLabel(""+i));
//				k++;
//			}
			slider = new JSlider( 	JSlider.HORIZONTAL, 
									1, 
									o.getNumericIndices( o.input ).size(), 
									o.outdims);
			slider.setMajorTickSpacing( 1 );
			slider.setMinorTickSpacing( 1 );
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			
			slider.addChangeListener(this);	
			//this.add(slider,"Center");

			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			ArrayList<Integer> dtcols = o.getDimTypeCols(ColType.NUMERIC);
			int p = 0;
			for( double v : o.getStressValues() ) {
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
			scree.isCutoffLeft = true;
			scree.useDimensionNames = false;

			scree.addLogStateCheckbox( jcb );
			scree.addChangeListener( this );
			slider.addChangeListener(scree);
			
			this.add(scree, "Center");
			this.add(jcb,	"South");
//			this.add(scree, "North");
		}

		public void actionPerformed(ActionEvent e) {

//			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
//			int k = 1;
//			for( int i = 1; i <= operator.getNumericIndices(operator.input ).size(); i++) {				
//				labelTable.put(new Integer(k),new JLabel(""+i));
//				k++;
//			}
//			slider.setMaximum(operator.getNumericIndices(operator.input ).size());
//			if( slider.getValue() != ((GlimmerOp)operator).outdims) {
//			
//				slider.setValue(((GlimmerOp)operator).outdims);
//			}
//			slider.setLabelTable(labelTable);
//			slider.validate();
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -4476127570420657983L;

		@Override
		public void stateChanged(ChangeEvent e) {

			if( e.getSource() instanceof ScreePlot ) {

				((GlimmerOp)operator).outdims = ((ScreePlot)e.getSource()).cutoff+1;
				updateMap();
				((GlimmerFunction)((GlimmerOp)operator).function).softReset(input, getNumericIndices( input ),getNonNumericIndices( input ), outdims );
		        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ), true);		    
			}
			if( e.getSource() instanceof JSlider ) {
				
				JSlider source = (JSlider)e.getSource();
			    if (!source.getValueIsAdjusting()) {
			        int val = (int)source.getValue();
			        ((GlimmerOp)operator).outdims = val;
			        ((GlimmerFunction)((GlimmerOp)operator).function).genEmbedding( val );
			        // signal a table changed event, but ONLY DOWNSTREAM
			        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ), true);		    
			    }
			}
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
		
		GlimmerOp testOp = new GlimmerOp( testTable, true, 2 );
		
		System.out.println("Glimmer table is " + testOp.rows() + " x " + testOp.columns() );

		System.out.print("Stresses are : ");
		for( double s : testOp.getStressValues() ) {
			
			System.out.print(""+s+" ");
		}
		System.out.println();
	}
	
	/**
	 * Return the constructed dimensions for this operator (use the map!)
	 */
	public ArrayList<DimensionDescriptor> getConstructedDimensions() {

//		NumberFormat nf = NumberFormat.getInstance();
//		nf.setMinimumFractionDigits(2);
//		nf.setMaximumFractionDigits(3);
//		
//		ArrayList<DimensionDescriptor> ret = new ArrayList<DimensionDescriptor>();
//		
//		for( int i = 0; i < this.outdims; i++ ) {
//			
//			String desc = "( ";
//			int rowd = ((PCAFunction)this.function).prinComp.getRowDimension();
//			for( int j = 0; j < rowd; j++ ) {
//				if( Math.abs(((PCAFunction)this.function).prinComp.get(j, i)) > 1.0e-5 ) {
//					desc += "("+nf.format( ((PCAFunction)this.function).prinComp.get(j, i) ) + "," + this.input.getColName( j )+")";
//				}
//			}
//			desc += " )";
//			ret.add( new DimensionDescriptor( this.toString() + i, desc ) );
//		}
//
//		if( ret.size() > 0 ) {
//			
//			return ret;
//		}
		
		return null;
	}

}
