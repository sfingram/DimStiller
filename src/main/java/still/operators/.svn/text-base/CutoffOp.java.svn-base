package still.operators;

import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import still.operators.PCAOp.PCAFunction;

/**
 * 
 * 
 * @author sfingram
 *
 */
public class CutoffOp extends Operator implements Serializable {

	public int thresholdIndex = 0;
	protected String description = "Cutoff Operator Description";
	double[] var = null;
	int[] sortMap = null;
	public int savedThresholdIndex = 0;
	
	public static String getMenuName() {
		
		return "Cull:Variance";
	}

	/**
	 * The maximum variance computed from the table
	 */
	public double maxVar = 0.0;	// maximum variance	
	
	public String getParamString() {
		
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumIntegerDigits(4);
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(2);

		return "threshold = "+form.format( var[sortMap[thresholdIndex]] );
	}
	
	public String toString() {
		
		return "[Cull:Variance]";
	}
	
	public String getSaveString( ) {
		
		String saveString = "";
		
		saveString += thresholdIndex;
				
		return saveString;
	}
	
	public CutoffOp( Table newTable, boolean isActive, String paramString ) {
		
		super( newTable );
		
		savedThresholdIndex = Integer.parseInt( paramString );
		if( savedThresholdIndex >= getDimTypeCols( ColType.NUMERIC ).size() ) {
			savedThresholdIndex = 0;
		}
		
		this.isActive = isActive;
		if( isActive ) {
			
			thresholdIndex 	= getDimTypeCols( ColType.NUMERIC ).get(savedThresholdIndex);
			map 			= Map.generateCullMap( genCullDims( ) );
			function 		= new CutoffFunction( newTable, map );
			isLazy  		= true;
			setView( new CutoffView( this ) );
		}
	}

	public CutoffOp( Table newTable, boolean isActive ) {
		
		super( newTable );
		
		this.isActive = isActive;
		this.savedThresholdIndex = 0;
		if( isActive ) {
			
			thresholdIndex 	= getDimTypeCols( ColType.NUMERIC ).get(0);
			map 			= Map.generateCullMap( genCullDims( ) );
			function 		= new CutoffFunction( newTable, map );
			isLazy  		= true;
			setView( new CutoffView( this ) );
		}
	}
	public CutoffOp( Table newTable, boolean isActive, int thresholdIndex ) {
		
		super( newTable );
		this.savedThresholdIndex = thresholdIndex;
		this.isActive = isActive;
		if( isActive ) {
			
			this.thresholdIndex 	= getDimTypeCols( ColType.NUMERIC ).get(thresholdIndex);
			map 					= Map.generateCullMap( genCullDims( ) );
			function 				= new CutoffFunction( newTable, map );
			isLazy  				= true;
			setView( new CutoffView(this) );
		}
	}
	
	public void activate() {
		
		this.isActive = true;
		thresholdIndex 	= getDimTypeCols( ColType.NUMERIC ).get(this.savedThresholdIndex);
		map 			= Map.generateCullMap( genCullDims( ) );
		function 		= new CutoffFunction( this.input, map );
		isLazy  		= true;
		setView( new CutoffView( this ) );		
	}

	public double[] getVar() {
		
		return var;
	}
	
	public boolean[] genCullDims( ) {
		
		ArrayList<Integer> numeric_idxs = getDimTypeCols( ColType.NUMERIC );
		double[] sums 	= new double[input.columns()];
		double[] sqSums	= new double[input.columns()];
		var	= new double[input.columns()];
		sortMap = new int[numeric_idxs.size()];
		
		for( int i = 0; i < input.rows(); i++ ) {
						
			for( int k : numeric_idxs ) {
			
				sums[k] 	+= 	input.getMeasurement(i, k);
				sqSums[k]	+= 	input.getMeasurement(i, k) * 
								input.getMeasurement(i, k);				
			}
		}
		boolean[] cullDims = new boolean[input.columns()];
		Arrays.fill(cullDims, true);
		maxVar = -1;
		for( int j : numeric_idxs ) {

			sums[j] 	= sums[j]	/((double) input.rows());
			sqSums[j] 	= sqSums[j]	/((double) input.rows());
			var[j] = sqSums[j] - (sums[j]*sums[j]);
			maxVar = Math.max(maxVar, var[j]);
		}
		
		// sort the variance in ascending order

		sortMap = FloatIndexer.sortFloats( var, numeric_idxs );

		// cull the dims
		
		for( int j = 0; j < sortMap.length; j++ ) {
			
			if( j < thresholdIndex  ) {
				
				cullDims[sortMap[j]] = false;
			}
		}
		
		return cullDims;
	}
	
	public void updateMap() {
	
		map 			= Map.generateCullMap( genCullDims( ) );
	}
	
	public void updateFunction() {
		
		function 		= new CutoffFunction( input, map );
	}
		
	public class CutoffFunction implements Function {

		private Table table 	= null;
		private int[] dimMap 	= null;
		
		public CutoffFunction( Table table, Map cutoffMap ) {
			this.table 	= table;
			dimMap 		= new int[cutoffMap.columns()];
			for(int i = 0; i < dimMap.length; i++ ) {
				
				for( int j = 0; j < cutoffMap.rows(); j++ ) {
					
					if( cutoffMap.map[j][i] ) {
						
						dimMap[i] = j;
						break;
					}
				}
			}
		}
		
		@Override
		public Table apply(Group group) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double compute(int row, int col) {
			
			return table.getMeasurement(row, dimMap[col]);
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
	
	public class CutoffView extends OperatorView implements ChangeListener {

		JSlider slider = null;
		ScreePlot scree = null;
		JCheckBox jcb = new JCheckBox( "Use Log Scale" );
		
		public CutoffView(CutoffOp o) {
			super(o);			
			
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumIntegerDigits(4);
			form.setMaximumFractionDigits(3);
			form.setMinimumFractionDigits(2);
			
			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			
			for( 	double i = 0.; 
					i < o.countDimType(ColType.NUMERIC)-1.; 
					i +=  Math.max(1., (((double)o.countDimType(ColType.NUMERIC)-1.)/5.0)) ) {
				
				labelTable.put(
						new Integer( (int)Math.round(i)+1),
						new JLabel(  form.format( o.var[ sortMap[ (int)Math.round(i) ] ])));
			}
			labelTable.put(new Integer(o.countDimType(ColType.NUMERIC)),
					new JLabel(form.format(o.var[ sortMap[ sortMap.length - 1] ])));
			
			slider = new JSlider( 	JSlider.HORIZONTAL, 
									1, 
									o.countDimType(ColType.NUMERIC), 
									o.thresholdIndex+1 );
			
			slider.setMajorTickSpacing( (int)Math.round(Math.max(1., (((double)o.countDimType(ColType.NUMERIC)-1.)/5.0)) ) );
			slider.setMinorTickSpacing( 1 );	
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			
			slider.addChangeListener(this);			
//			this.add(slider,"Center");
			
			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			for( int vIdx : o.getDimTypeCols(ColType.NUMERIC)) {
				
				univarQuant.add( o.getVar()[vIdx] );
				univarNames.add( o.input.getColName(vIdx) );
			}
			scree = new ScreePlot(	univarQuant,
									univarNames,
									true,
									new Comparator<Double>() {
										public int compare(Double o1, Double o2) {
											if( o1 < o2 ) {
												return -1;
											}
											else if( o1 > o2 ) {
												return 1;
											}
											return 0;
										}
									},
									null);
			
			scree.cutoff = o.thresholdIndex;
			scree.addLogStateCheckbox( jcb );
			
			this.add(jcb,	"South");
			this.add(scree,	"Center");
			
			scree.addChangeListener(this);
			slider.addChangeListener( scree );
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
			        ((CutoffOp)operator).thresholdIndex = val-1;
			        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));
			    }
			}
			if( e.getSource() instanceof ScreePlot ) {
				
		        ((CutoffOp)operator).thresholdIndex = ((ScreePlot)e.getSource()).cutoff;
		        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));
			}
		}
		
		public void actionPerformed(ActionEvent e) {

			// update the gui based on anything that's happened
			
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumIntegerDigits(4);
			form.setMaximumFractionDigits(3);
			form.setMinimumFractionDigits(2);
			
//			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
//
//			for( 	double i = 0.; 
//					i < ((CutoffOp)operator).countDimType(ColType.NUMERIC)-1.; 
//					i +=  Math.max(1., (((double)((CutoffOp)operator).countDimType(ColType.NUMERIC)-1.)/5.0)) ) {
//				labelTable.put(
//						new Integer( (int)Math.round(i)+1),
//						new JLabel(  form.format( ((CutoffOp)operator).var[ ((CutoffOp)operator).sortMap[(int)Math.round(i)]])));
//			}
//			labelTable.put(new Integer(((CutoffOp)operator).countDimType(ColType.NUMERIC)),
//					new JLabel(form.format(((CutoffOp)operator).var[ ((CutoffOp)operator).sortMap[((CutoffOp)operator).sortMap.length-1] ])));
//			
			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			for( int vIdx : this.operator.getDimTypeCols(ColType.NUMERIC)) {
				
				univarNames.add(((CutoffOp)this.operator).input.getColName(vIdx));
				univarQuant.add( ((CutoffOp)this.operator).getVar()[vIdx] );
			}
			scree.setUnivar(univarQuant, univarNames);
			
//			slider.setMinimum(1);
//			slider.setMaximum( ((CutoffOp)operator).countDimType(ColType.NUMERIC) );
//			slider.setValue( ((CutoffOp)operator).thresholdIndex+1 );
//			slider.setMajorTickSpacing( (int)Math.round(Math.max(1., (((double)((CutoffOp)operator).countDimType(ColType.NUMERIC)-1.)/5.0)) ) );
//			slider.setMinorTickSpacing( 1 );	
//			slider.setLabelTable(labelTable);
//			slider.setPaintLabels(true);				
//			slider.validate();
		}
	}
	
	public ArrayList<DimensionDescriptor> getConstructedDimensions() {
		
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(3);
		
		ArrayList<DimensionDescriptor> ret = new ArrayList<DimensionDescriptor>();
		
		for( int i = 0; i < this.map.rows(); i++ ) {
			
			boolean isCulled = true;
			for( int j = 0; j < this.map.columns(); j++ ) {
				
				if( this.map.map[i][j] ) {
					
					isCulled = false;
					break;
				}
			}
			
			if( isCulled ) {
				ret.add( new DimensionDescriptor( input.getColName(i), " "+this.var[i] ) );
			}			
		}

		if( ret.size() > 0 ) {
			
			return ret;
		}
		
		return null;
	}

	/**
	 * 
	 * Test routine loads a CSV table, displays the dimensions
	 * Applies the cutoff and displays the new dimensions
	 * 
	 * @param args
	 */
	public static void main( String[] args ) {
		
		System.out.println("Loading file " + args[0] );
		
		Table testTable = TableFactory.fromCSV( args[0] );
		
		System.out.println("Table is : " + testTable.rows() + " x " + testTable.columns() );
		
		CutoffOp testOp = new CutoffOp( TableFactory.fromCSV( args[0] ), true, 0 );
		
		System.out.println("Cutoff table is " + testOp.rows() + " x " + testOp.columns() );
	}

}
