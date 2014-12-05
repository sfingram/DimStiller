package still.operators;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PApplet;

import still.data.DimensionDescriptor;
import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.gui.OPApplet;
import still.gui.OPAppletViewFrame;
import still.gui.OperatorView;
import still.gui.PMatrixPainter;
import still.gui.ViewFrame;
import still.operators.PCAOp.PCAFunction;
import still.operators.PCAOp.PCAView;
import still.operators.SplomOp.SplomView;

public class PearsonCollectOp extends Operator implements Serializable {

	protected double threshold = 1.0;
	protected String description = "Collect Operator Description";
	protected double[][] r = null;
	protected double[][] r_post = null;
	protected boolean recalc_rpost = true;
	
	public String getSaveString( ) {
		
		return ""+this.threshold;
	}
	
	public static String getMenuName() {
		
		return "Collect:Pearson's Correlation";
	}
	
	public double[][] getCorrelationMatrix() {
		
		return r;
	}
	
	public double[][] getPostCorrelationMatrix() {
		
		if( recalc_rpost) {

			recalc_rpost = false;
			double[] sums 	= new double[this.columns()];
			double[] sqSums	= new double[this.columns()];
			double[] var	= new double[this.columns()];
			for( int i = 0; i < this.rows(); i++ ) {
				for( int j = 0; j < this.columns(); j++ ) {
									
					sums[j] 	+= 	this.getMeasurement(i, j);
					sqSums[j]	+= 	this.getMeasurement(i, j) * 
									this.getMeasurement(i, j);				
				}
			}
			int num_numeric = this.columns();
			for( int j = 0; j < this.columns(); j++ ) {
	
				if( this.getColType(j) != ColType.NUMERIC ) {
					sums[j] = -1;
					sqSums[j] = -1;
					var[j] = -1;
					num_numeric--;
				}
				else {
					sums[j] 	= sums[j]	/((double) this.rows());
					sqSums[j] 	= sqSums[j]	/((double) this.rows());
					var[j] = sqSums[j] - (sums[j]*sums[j]);
				}
			}
			
			// compute the d^2 correlation matrix
			r_post = new double[num_numeric][num_numeric];
			int k = 0;
			int kk = 0;
			for( int i = 0; i < num_numeric; i++ ) {
				
				while(this.getColType(k) != ColType.NUMERIC )
					k++;
				
				kk = 0;
				for( int j = 0; j <= i; j++ ) {
					
					while(this.getColType(kk) != ColType.NUMERIC )
						kk++;
					
					// compute 1's on the diagonal
					
					if( i == j ) {
						
						r[i][j] = 1.0;
						break;
					}
					
					// compute pearson's correlation coefficient
					double sumNum = 0.0;
					for( int ind = 0; ind < this.rows(); ind ++ ) {
						
						sumNum += (this.getMeasurement(ind, k)-sums[k]) * 
								  (this.getMeasurement(ind, kk)-sums[kk]); 
					}
					r_post[i][j] = sumNum / ((this.rows()-1)*Math.sqrt(var[kk])*Math.sqrt(var[k]));
					r_post[j][i] = r_post[i][j];
					kk++;
				}
				k++;
			}
		}
		
		return r_post;
	}
	
	public String toString() {
		
		return "[Collect:Pearson]";
	}
	
	public String getParamString() {
		
		
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumIntegerDigits(4);
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(2);

		return "threshold = "+form.format(threshold);
	}
	
	public PearsonCollectOp( Table newTable, boolean isActive, String paramString ) {
		
		super( newTable );
		
		threshold = Double.parseDouble( paramString );
		
		this.isActive = isActive;
		if( isActive ) {
			map 			= Map.generateCovarianceMap( genCollectDims( ) );
			function 		= new CollectFunction( newTable, map );
			isLazy  		= true;
			setView( new PCollectView( this ) );
		}
	}

	public PearsonCollectOp( Table newTable, boolean isActive ) {
		
		super( newTable );
		
		this.isActive = isActive;
		if( isActive ) {
			map 			= Map.generateCovarianceMap( genCollectDims( ) );
			function 		= new CollectFunction( newTable, map );
			isLazy  		= true;
			setView( new PCollectView( this ) );
		}
	}
	
	public PearsonCollectOp( Table newTable, boolean isActive, double threshold ) {
		
		super( newTable );
		
		this.isActive = isActive;
		this.threshold 	= threshold;
		if( isActive ) {
			map 			= Map.generateCovarianceMap( genCollectDims( ) );
			function 		= new CollectFunction( newTable, map );
			isLazy  		= true;
			setView( new PCollectView( this ) );
		}
	}
	
	public void activate() {
		
		this.isActive = true;
		map 			= Map.generateCovarianceMap( genCollectDims( ) );
		function 		= new CollectFunction( input, map );
		isLazy  		= true;
		setView( new PCollectView( this ) );		
	}


	public void loadOperatorView() {
		
		setView( new PCollectView( this ) );
	}

	public int[] genCollectDims( ) {

//		Thread.dumpStack();
		recalc_rpost = true;
		
		// compute means and variances of each dimension
		
		double[] sums 	= new double[input.columns()];
		double[] sqSums	= new double[input.columns()];
		double[] var	= new double[input.columns()];
		for( int i = 0; i < input.rows(); i++ ) {
			for( int j = 0; j < input.columns(); j++ ) {
								
				sums[j] 	+= 	input.getMeasurement(i, j);
				sqSums[j]	+= 	input.getMeasurement(i, j) * 
								input.getMeasurement(i, j);				
			}
		}
		int num_numeric = input.columns();
		for( int j = 0; j < input.columns(); j++ ) {

			if( input.getColType(j) != ColType.NUMERIC ) {
				sums[j] = -1;
				sqSums[j] = -1;
				var[j] = -1;
				num_numeric--;
			}
			else {
				sums[j] 	= sums[j]	/((double) input.rows());
				sqSums[j] 	= sqSums[j]	/((double) input.rows());
				var[j] = sqSums[j] - (sums[j]*sums[j]);
			}
		}
		
		// compute the d^2 correlation matrix
		r = new double[num_numeric][num_numeric];
		int k = 0;
		int kk = 0;
		for( int i = 0; i < num_numeric; i++ ) {
			
			while(input.getColType(k) != ColType.NUMERIC )
				k++;
			
			kk = 0;
			for( int j = 0; j <= i; j++ ) {
				
				while(input.getColType(kk) != ColType.NUMERIC )
					kk++;
				
				// compute 1's on the diagonal
				
				if( i == j ) {
					
					r[i][j] = 1.0;
					break;
				}
				
				// compute pearson's correlation coefficient
				double sumNum = 0.0;
				for( int ind = 0; ind < input.rows(); ind ++ ) {
					
					sumNum += (input.getMeasurement(ind, k)-sums[k]) * 
							  (input.getMeasurement(ind, kk)-sums[kk]); 
				}
				r[i][j] = sumNum / ((input.rows()-1)*Math.sqrt(var[kk])*Math.sqrt(var[k]));
				r[j][i] = r[i][j];
				kk++;
				
				//System.out.print(""+r[i][j]+" ");
			}
			k++;
			//System.out.println(" ");
		}
		
		
		NumberFormat form = NumberFormat.getInstance();
//		form.setMaximumIntegerDigits(4);
//		form.setMaximumFractionDigits(3);
//		form.setMinimumFractionDigits(2);
//		for( int i = 0; i < num_numeric; i++ ) {
//			for( int j = 0; j < num_numeric; j++ ) {
//				
//				System.out.print( " " + form.format(r[i][j]) );				
//			}
//			System.out.println();
//		}

		// construct grouping based on r values
		
		int[] collectDims 	= new int[input.columns()];
		int lastGroup 		= 0;
		int biggestGroup	= 0;
		int numeric_count_row = 0;

		Arrays.fill( collectDims, -1 );		
		for( int i = 0; i < collectDims.length; i++ ) {
			
			if( collectDims[i] == -1 ) {
				
				biggestGroup++;
				collectDims[i] = biggestGroup;
			}
			
			lastGroup = collectDims[i];
			
			if( var[i] < 0 ) {
				
				if( input.getColType(i) == ColType.NUMERIC ) {
					
					numeric_count_row++;
				}
				continue;
			}
			
//			System.out.print( ""+i+": " );				

			int numeric_count_col = numeric_count_row+1;
			for( int j = i+1; j < collectDims.length; j++ ) {
				
				if( input.getColType(j) != ColType.NUMERIC ) 
					continue;
				
				if( Math.abs(r[numeric_count_row][numeric_count_col]) > threshold ) {
					
//					System.out.print( ""+j+", " );				
					collectDims[j] = lastGroup;
				}

				if( input.getColType(j) == ColType.NUMERIC ) {
					
					numeric_count_col++;
				}
			}
			
			if( input.getColType(i) == ColType.NUMERIC ) {
				
				numeric_count_row++;
			}
//			System.out.println( "" );				
		}
		
		
		return collectDims;
	}
			
	public void updateMap() {
		
		map 			= Map.generateCovarianceMap( genCollectDims( ) );
	}
	
	public void updateFunction() {
		
		function 		= new CollectFunction( input, map );
	}

	public class CollectFunction implements Function {

		private Table table 	= null;
		private int[] dimMap 	= null;
		
		public CollectFunction( Table table, Map cutoffMap ) {

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

	public class PCollectView extends OperatorView implements ChangeListener {

		JSlider slider = null;
				
		public PCollectView(PearsonCollectOp o) {
			super(o);
			
			JPanel jp = new JPanel();
			jp.setLayout( new BorderLayout(5,5) );
			
			vframe = new OPAppletViewFrame("E"+o.getExpressionNumber()+":"+o, new PMatrixPainter(o));
			vframe.addComponentListener(this);
			
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumIntegerDigits(4);
			form.setMaximumFractionDigits(3);
			form.setMinimumFractionDigits(2);

			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int k = 0;
			for( double i = 0.0; i <= 1.0; i += 0.2) {				
				labelTable.put(new Integer(k),new JLabel(""+form.format(i)));
				k+=20;
			}
			slider = new JSlider( 	JSlider.HORIZONTAL, 
									0, 
									100, 
									(int)(o.threshold*100));
			slider.setMajorTickSpacing( 20 );
			slider.setMinorTickSpacing( 1 );
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			
			slider.addChangeListener(this);		
			jp.add(slider,"Center");
			jp.add(new JLabel("Pearson's Coefficient Cutoff"),"North");
			jp.setBorder(BorderFactory.createEmptyBorder(50, 10, 50, 10));
			this.add(jp,"Center");
		}

		public void actionPerformed(ActionEvent e) {
			
			NumberFormat form = NumberFormat.getInstance();
			form.setMaximumIntegerDigits(4);
			form.setMaximumFractionDigits(3);
			form.setMinimumFractionDigits(2);

			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int k = 0;
			for( double i = 0.0; i <= 1.0; i += 0.2) {				
				labelTable.put(new Integer(k),new JLabel(""+form.format(i)));
				k+=20;
			}
//			slider.setMaximum(100);
			if( slider.getValue() != (int)(((PearsonCollectOp)operator).threshold*100) ) {
				
				slider.setValue((int)(((PearsonCollectOp)operator).threshold*100));
			}
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			slider.validate();
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -4476127570420657983L;

		@Override
		public void stateChanged(ChangeEvent e) {

			JSlider source = (JSlider)e.getSource();
		    if (!source.getValueIsAdjusting()) {
		        int val = (int)source.getValue();
		        ((PearsonCollectOp)operator).threshold = ((double)val)*0.01;
		        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ));		    }
		}
	}

	/**
	 * 
	 * Test routine loads a CSV table, displays the dimensions
	 * Applies the cutoff then the collect stage and displays the new dimensions
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
	}

	/**
	 * Return the constructed dimensions for this operator (use the map!)
	 */
	public ArrayList<DimensionDescriptor> getConstructedDimensions() {

		ArrayList<DimensionDescriptor> ret = new ArrayList<DimensionDescriptor>();
		for( int i = 0; i < this.columns(); i++ ) {
			
			ArrayList<Integer> colsamp = map.getColumnSamples(i);
			if( colsamp.size() > 1 ) {
				
				String desc = "( ";
				for( int j = 0; j < colsamp.size(); j++ ) {
					
					desc += this.input.getColName( colsamp.get(j) );
					if( j < colsamp.size()-1 ) {
						
						desc += ",";
					}
				}
				desc += " )";
				ret.add( new DimensionDescriptor( getColName(i), desc ) );
			}
		}

		if( ret.size() > 0 ) {
			
			return ret;
		}
		
		return null;
	}
}
