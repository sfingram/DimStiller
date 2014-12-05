package still.operators;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jblas.DoubleMatrix;
import org.jblas.SimpleBlas;
import org.jblas.Singular;
import org.jblas.Solve;

import still.data.Binner;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.gui.OPAppletViewFrame;
import still.gui.OperatorView;
import still.gui.PCorrAPainter;

public class CAOp extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7580338181343563269L;
	
	int selectionCol 	= -1;
	boolean hasSelOp 	= false;
	double[][] selCol 	= null;
	public int compDim1 = -1;
	public int compDim2 = -1;
	public String compDim1Str = null;
	public String compDim2Str = null;
	public boolean hasDiagram = false;
	public DoubleMatrix coord1 = null;
	public DoubleMatrix coord2 = null;
	public Binner binner_col1 = null;
	public Binner binner_col2 = null;
	public int bin_count_1 = 10;
	public int bin_count_2 = 10;
	
	public float[] getCACoord( int dim, int category ) {
		
		float[] ret = null;
		
//		System.out.println("coord1 = (" + coord1.getRowDimension() + "x" + coord1.getColumnDimension() + ")" ); 
//		
		if( dim == 1 && coord1 != null ) {
			
			ret = new float[2];
			ret[0] = (float)coord1.get(category, 0);
			ret[1] = (float)coord1.get(category, 1);
		}
		
		if( dim == 2 && coord2 != null ) {
			
			ret = new float[2];
			ret[0] = (float)coord2.get(category, 0);
			ret[1] = (float)coord2.get(category, 1);
		}
		
		return ret;
	}

	public String getSaveString( ) {
		
		return "";
	}
	
	public static String getMenuName() {
		
		return "View:CorrespondenceAnalysis";
	}

	public void setMeasurement( int point_idx, int dim, double value ) {
		
		if( hasSelOp || (!hasSelOp && dim < map.columns()-1 ) ) {
			
			input.setMeasurement(point_idx, dim, value);
		}
		else {
			
			selCol[point_idx][0] = value;
		}
	}

	public ColType getColType( int dim ) {
		
		if( hasSelOp || (!hasSelOp && dim < map.columns()-1 ) ) {
			for( int i : map.getColumnSamples(dim) ) {
				if( input.getColType(i) == ColType.CATEGORICAL ) {
					
					return ColType.CATEGORICAL;
				}
				if( input.getColType(i) == ColType.NUMERIC ) {
					
					return ColType.NUMERIC;
				}
				if( input.getColType(i) == ColType.ORDINAL ) {
					
					return ColType.ORDINAL;
				}
				if( input.getColType(i) == ColType.ATTRIBUTE ) {
					
					return ColType.ATTRIBUTE;
				}
			}
		}
				
		return ColType.ATTRIBUTE;
	}

	public String getColName( int dim ) {

		if( hasSelOp || (!hasSelOp && dim < map.columns()-1 )) {
		
			ArrayList<Integer> colsamp = map.getColumnSamples(dim); 
			if( colsamp.size() > 1 ) {
				
				return (this.toString() + dim);
			}
			else if( colsamp.size() == 1 ){
				
				return input.getColName(colsamp.get(0));
			}
		}

		return "selection";
	}

	public String toString() {
		
		return "[View:CA]";
	}
	
	public void activate() {
		
		isActive = true;
		
		// handle the selection operator
		hasSelOp = false;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if( 	input.getColName(i).equalsIgnoreCase("selection")  &&
					input.getColType(i) == ColType.ATTRIBUTE ) {
				
				hasSelOp 		= true;
				selectionCol 	= i;
			}
		}
		
		if( ! hasSelOp ){
		
			selCol 		= new double[input.rows()][1];
			map 		= Map.generateDiagonalMap(input.columns()+1);
			function 	= new AppendFunction(input, selCol );
		}
		else {
			
			map 			= Map.generateDiagonalMap(input.columns());
			function 		= new IdentityFunction( input );
		}

		isLazy  		= true;
		setView( new CAView( this ) );		
	}
	
	public void loadOperatorView() {
		
		setView( new CAView( this ) );
	}

	public CAOp( Table newTable, boolean isActive, String paramString ) {
		
		this( newTable, isActive );
	}
	
	public CAOp( Table newTable, boolean isActive ) {
		
		super( newTable );
		
		
		this.isActive = isActive;
		if( isActive ) {
			
			// handle the selection operator
			hasSelOp = false;
			for( int i = 0; i < input.columns(); i++ ) {
				
				if( 	input.getColName(i).equalsIgnoreCase("selection")  &&
						input.getColType(i) == ColType.ATTRIBUTE ) {
					
					hasSelOp 		= true;
					selectionCol 	= i;
				}
			}
			
			computeCA();
			
			if( ! hasSelOp ){
			
				selCol 		= new double[input.rows()][1];
				map 		= Map.generateDiagonalMap(input.columns()+1);
				function 	= new AppendFunction(input, selCol );
			}
			else {
				
				map 			= Map.generateDiagonalMap(input.columns());
				function 		= new IdentityFunction( input );
			}
	
			isLazy  		= true;
			setView( new CAView( this ) );		
		}
	}

	public void computeCA() {

		// determine if there are appropriate number of dimensions to use
		
		int num_cols = 0;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if( input.getColType(i) == ColType.CATEGORICAL || 
					input.getColType(i) == ColType.ORDINAL || 
					input.getColType(i) == ColType.NUMERIC ) {
				
				num_cols++;
			}
		}
		
		if(num_cols < 2) {
			
			this.hasDiagram=false;			
		}

		this.compDim1 = -1;
		this.compDim2 = -1;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if( input.getColName(i) == this.compDim1Str &&
					( input.getColType(i) == ColType.CATEGORICAL || 
					  input.getColType(i) == ColType.ORDINAL || 
					  input.getColType(i) == ColType.NUMERIC ) ) {
					
					this.compDim1 = i;
			}
			
			if( input.getColName(i) == this.compDim2Str &&
					( input.getColType(i) == ColType.CATEGORICAL || 
					  input.getColType(i) == ColType.ORDINAL || 
					  input.getColType(i) == ColType.NUMERIC ) ) {
					
					this.compDim2 = i;
			}
		}
		
		// handle if the dimension is gone
		
		if( this.compDim1 < 0 ) {
			
			for( int i = 0; i < input.columns(); i++ ) {
				
				if( input.getColName(i) != this.compDim2Str &&
						( input.getColType(i) == ColType.CATEGORICAL || 
						  input.getColType(i) == ColType.ORDINAL || 
						  input.getColType(i) == ColType.NUMERIC ) ) {
						
						this.compDim1 = i;
						this.compDim1Str = input.getColName(i);
						break;
				}
			}
		}
		
		if( this.compDim2 < 0 ) {
			
			for( int i = 0; i < input.columns(); i++ ) {
				
				if( input.getColName(i) != this.compDim1Str &&
						( input.getColType(i) == ColType.CATEGORICAL || 
						  input.getColType(i) == ColType.ORDINAL || 
						  input.getColType(i) == ColType.NUMERIC ) ) {
						
						this.compDim2 = i;
						this.compDim2Str = input.getColName(i);
						break;
				}
			}
		}
		
		// get binners 
		
		binner_col1 = ( input.getColType(compDim1) == ColType.NUMERIC ) ? ( new Binner(input, compDim1) ) : null;
		binner_col2 = ( input.getColType(compDim2) == ColType.NUMERIC ) ? ( new Binner(input, compDim2) ) : null;
		if( binner_col1 != null ) {
			
			binner_col1.setBinCount( this.bin_count_1 );
		}
		if( binner_col2 != null ) {
			
			binner_col2.setBinCount( this.bin_count_2 );
		}
		
//		System.out.println("compDim1 = " + compDim1);
//		System.out.println("compDim2 = " + compDim2);
//		System.out.println("compDim1Str = " + compDim1Str);
//		System.out.println("compDim2Str = " + compDim2Str);
//		System.out.println("bin_count_1 = " + bin_count_1);
//		System.out.println("bin_count_2 = " + bin_count_2);
//		System.out.println("*******");
//		for( String s : binner_col1.getBinStrings() ) {
//			
//			System.out.println("\t"+s);
//		}
//		System.out.println("*******");
//		for( String s : binner_col2.getBinStrings() ) {
//			
//			System.out.println("\t"+s);
//		}
		
		
		// compute the two-way table
		
		int row_count = (binner_col1 == null)?input.getCategories(compDim1).length:binner_col1.getBinStrings().length;
		int col_count = (binner_col2 == null)?input.getCategories(compDim2).length:binner_col2.getBinStrings().length;
		DoubleMatrix twoWayTable = DoubleMatrix.zeros(row_count, col_count);		
		for( int i = 0; i < input.rows(); i++ ) {
			
			int row_idx = -1;
			int col_idx = -1;
			if( input.getColType(compDim1) == ColType.NUMERIC ) {
				
				row_idx = (int)binner_col1.binNum(input.getMeasurement(i, compDim1));
			}
			else {
				
				row_idx = (int)input.getMeasurement(i, compDim1);
			}
			if( input.getColType(compDim2) == ColType.NUMERIC ) {
				
				col_idx = (int)binner_col2.binNum(input.getMeasurement(i, compDim2));
			}
			else {
				
				col_idx = (int)input.getMeasurement(i, compDim2);
			}
			double val = twoWayTable.get(row_idx, col_idx);
//			System.out.println("(" + row_idx + ", " + col_idx + ") = "+ twoWayTable.get(row_idx, col_idx));
			twoWayTable.put(row_idx, col_idx, val+1.0);
		}
		
//		for( int i = 0; i < twoWayTable.getRowDimension(); i++ ) {
//			for( int j = 0; j < twoWayTable.getRowDimension(); j++ ) {
//				
//				System.out.print(" "+twoWayTable.get(i,j));
//			}			
//			System.out.println( );
//		}
		
//		twoWayTable = new Matrix( 4, 4 );
//		twoWayTable.set(0,0,68);twoWayTable.set(0,1,119);twoWayTable.set(0,2,26);twoWayTable.set(0,3,7);
//		twoWayTable.set(1,0,20);twoWayTable.set(1,1,84);twoWayTable.set(1,2,17);twoWayTable.set(1,3,94);
//		twoWayTable.set(2,0,15);twoWayTable.set(2,1,54);twoWayTable.set(2,2,14);twoWayTable.set(2,3,10);
//		twoWayTable.set(3,0,5);twoWayTable.set(3,1,29);twoWayTable.set(3,2,14);twoWayTable.set(3,3,16);
		
		// compute the probability matrix

		DoubleMatrix Q = (twoWayTable.mmul(DoubleMatrix.ones(twoWayTable.columns,1))).transpose().mmul(DoubleMatrix.ones(twoWayTable.rows,1));
		double n = Q.get(0,0);
		
		//		System.out.println("marginal = " + Q.get(0,0));
		DoubleMatrix P = twoWayTable.mul(1.f/n);
		DoubleMatrix r = DoubleMatrix.zeros(P.rows); 
		r = SimpleBlas.gemv( 1.0, P, DoubleMatrix.ones(P.columns), 0.0, r ); // r = P*1
		DoubleMatrix c = DoubleMatrix.zeros(P.columns);
		c = SimpleBlas.gemv( 1.0, P.transpose(), DoubleMatrix.ones(P.rows), 0.0, c); // c = P'*1
		DoubleMatrix D_r = DoubleMatrix.zeros( r.rows, r.rows );
		DoubleMatrix D_c = DoubleMatrix.zeros( c.rows, c.rows );
		DoubleMatrix D_r_half = DoubleMatrix.zeros( r.rows, r.rows );
		DoubleMatrix D_c_half = DoubleMatrix.zeros( c.rows, c.rows );
		
		for( int i = 0; i < r.rows; i++ ) {
			
			D_r.put(i, i, r.get(i, 0));
			D_r_half.put(i, i, Math.sqrt(r.get(i, 0)));
		}
		for( int i = 0; i < c.rows; i++ ) {
			
			D_c.put(i, i, c.get(i, 0));
			D_c_half.put(i, i, Math.sqrt(c.get(i, 0)));
		}
		
		// (D_r_half^-1)(P - (r * c'))(D_c_half^-1)
		
		DoubleMatrix P_hat = Solve.solve(D_r_half, P.sub( r.mmul( c.transpose() ) ) ).mmul( Solve.solve(D_c_half, DoubleMatrix.eye(D_c_half.columns)) ); 
		
//		NumberFormat sf = new DecimalFormat("0.#E0");
//		for( int i = 0; i < P_hat.getRowDimension(); i++ ) {
//			for( int j = 0; j < P_hat.getRowDimension(); j++ ) {
//				
//				System.out.print(" "+sf.format(P_hat.get(i,j)));
//			}			
//			System.out.println( );
//		}
				
		// decompose the matrix

		DoubleMatrix U = null;
		DoubleMatrix V = null;
		DoubleMatrix S = null;
		if( P_hat.rows >= P_hat.columns ) {
			
			DoubleMatrix[] ADB = Singular.fullSVD(P_hat);
			U = ADB[0];
			V = ADB[2];			
			S = DoubleMatrix.zeros(U.columns, V.rows);
			for( int i = 0; i < ADB[1].rows; i++ ) {
				
				S.put(i, i, ADB[1].get(i));
			}
			//S = DoubleMatrix.diag(ADB[1]);
		}
		else {
			
			DoubleMatrix[] ADV = Singular.fullSVD(P_hat.transpose());
			U = ADV[2].transpose();
			V = ADV[0];			
			S = DoubleMatrix.zeros(U.columns, V.rows);
			for( int i = 0; i < ADV[1].rows; i++ ) {
				
				S.put(i, i, ADV[1].get(i));
			}
			//S = DoubleMatrix.diag(ADV[1]);
		}
				
		// compute the coordinates
		
//		System.out.println("D_r = " + D_r.getRowDimension() + " x "+ D_r.getColumnDimension() );
//		System.out.println("D_c = " + D_c.getRowDimension() + " x "+ D_c.getColumnDimension() );
//		System.out.println("P = " + P.getRowDimension() + " x "+ P.getColumnDimension() );
//		System.out.println("U = " + U.rows + " x "+ U.columns );
//		System.out.println("S = " + S.rows + " x "+ S.columns );
//		System.out.println("V = " + V.rows + " x "+ V.columns );
		DoubleMatrix F = Solve.solve(D_r_half, U.mmul(S)); // D_r_half.inverse().times((U.times(S)));
		DoubleMatrix G = Solve.solve(D_c_half, V.mmul(S.transpose())); // D_c_half.inverse().times((V.times(S)));
//		Matrix G = D_c.inverse().times((V.times(S)));
		
//		System.out.println("S = [" + S.get(0,0) + " " + S.get(1,1) + " " + S.get(2,2) + " " + S.get(3,3) + "]");
		
		this.coord1 = DoubleMatrix.zeros(F.rows,2); //new Matrix(F.getRowDimension(), 2);
		this.coord2 = DoubleMatrix.zeros(G.rows, 2); //new Matrix(G.getRowDimension(), 2);
		for( int i = 0; i < F.rows; i++ ) {

			this.coord1.put(i, 0, F.get(i,0));
			this.coord1.put(i, 1, F.get(i,1));
//			System.out.println(" " + F.get(i,0) + " , "+ F.get(i,1) );
		}
//		System.out.println();
		for( int i = 0; i < G.rows; i++ ) {

			this.coord2.put(i, 0, G.get(i,0));
			this.coord2.put(i, 1, G.get(i,1));
//			System.out.println(" " + G.get(i,0) + " , "+ G.get(i,1) );
		}
		
	}
	
	public void tableChanged( TableEvent te ) {
		
		super.tableChanged(te);
		
		if( this.isActive() ) {
			SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	
	    		// change the hilight or color status of the underlying visualization
	    		((OPAppletViewFrame)((CAView)getView()).getViewFrame()).procApp.redraw();
	        }
			});
		}
	}
	
	@Override
	public void updateFunction() {

		// recompute correspondence analysis
		
		computeCA();
		
		// compute the fuction
		
		if( ! hasSelOp ) {
			function	= new AppendFunction(input, selCol );
		}
		else {
			function 	= new IdentityFunction( input );
		}
	}

	@Override
	public void updateMap() {
		
		// handle the selection operator
		hasSelOp = false;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if( 	input.getColName(i).equalsIgnoreCase("selection")  &&
					input.getColType(i) == ColType.ATTRIBUTE ) {
				
				hasSelOp 		= true;
				selectionCol 	= i;
			}
		}
		
		if( ! hasSelOp ){
			
			selCol 		= new double[input.rows()][1];
			map 		= Map.generateDiagonalMap(input.columns()+1);
		}
		else {
			
			map 			= Map.generateDiagonalMap(input.columns());
		}
	}

	public class CAView extends OperatorView implements ChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1604261613966166897L;
		JSlider slider = null;
		JSlider size_slider = null;
		JCheckBox split_profile_checkbox = null;
		PCorrAPainter pcap = null;
		JComboBox pick_dim_1 = null;
		JComboBox pick_dim_2 = null;
		JComboBox choose_bins_1 = null;
		JComboBox choose_bins_2 = null;
		JCheckBox use_bins_1 = null;
		JCheckBox use_bins_2 = null;
		ArrayList<Boolean> is_numeric_list = null;
		ArrayList<Integer> idx_ref_list = null;
		
		public CAView(Operator o) {
			super(o);

			this.setLayout( new BorderLayout(5,5));
			
			pcap = new PCorrAPainter(o);
			vframe = new OPAppletViewFrame("E"+o.getExpressionNumber()+":"+o, pcap );			
			vframe.addComponentListener(this);
			
			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int splomsizer = 100;
			for( int i = splomsizer; i <= 1000; i += splomsizer) {				
				labelTable.put(new Integer((i-splomsizer)/splomsizer),new JLabel(""+i));
			}
			slider = new JSlider( 	JSlider.HORIZONTAL, 
									0, 
									(1000-splomsizer)/splomsizer, 
									0);
			slider.setName("slider");
			slider.setMajorTickSpacing( 5 );
			slider.setMinorTickSpacing( 1 );
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			slider.setEnabled(false);
			slider.addChangeListener(this);			
			slider.addChangeListener(pcap);			
			
			slider.addChangeListener(this);			
			
			JPanel dim_sel_panel = new JPanel();
			dim_sel_panel.setLayout( new GridLayout(1,2,5,5) );
			JPanel dim_sel_panel_1 = new JPanel();
			dim_sel_panel_1.setLayout( new GridLayout(3,1,5,5) );
			JPanel dim_sel_panel_2 = new JPanel();
			dim_sel_panel_2.setLayout( new GridLayout(3,1,5,5) );
			
			pick_dim_1 = new JComboBox();
			pick_dim_2 = new JComboBox();
			use_bins_1 = new JCheckBox("use bins");
			use_bins_2 = new JCheckBox("use_bins");
			choose_bins_1 = new JComboBox(new String[] { "5", "6", "7", "8", "9" , "10" });
			choose_bins_2 = new JComboBox(new String[] { "5", "6", "7", "8", "9" , "10" });
			populateBinBoxes( );
						
			pick_dim_1.addActionListener( this );
			pick_dim_2.addActionListener( this );
			use_bins_1.addActionListener( this );
			use_bins_2.addActionListener( this );
			choose_bins_1.addActionListener( this );
			choose_bins_2.addActionListener( this );
			
			dim_sel_panel_1.add( pick_dim_1 );
			dim_sel_panel_1.add( use_bins_1 );
			dim_sel_panel_1.add(choose_bins_1 );
			dim_sel_panel_1.setBorder( BorderFactory.createTitledBorder("Variable 1"));
			dim_sel_panel_2.add( pick_dim_2 );
			dim_sel_panel_2.add( use_bins_2 );
			dim_sel_panel_2.add(choose_bins_2 );
			dim_sel_panel_2.setBorder( BorderFactory.createTitledBorder("Variable 2"));
			dim_sel_panel.add( dim_sel_panel_1 );
			dim_sel_panel.add( dim_sel_panel_2 );
			
			JCheckBox scrollable_button = new JCheckBox("Manual Plot Size");
			scrollable_button.setActionCommand("scroll");
			scrollable_button.addActionListener(this);
			JPanel sizingPanel = new JPanel();
			sizingPanel.setLayout( new BorderLayout() );
			sizingPanel.add(scrollable_button,BorderLayout.WEST);
			sizingPanel.add(slider,BorderLayout.CENTER);
			this.add(sizingPanel, BorderLayout.SOUTH );
			this.add(dim_sel_panel, BorderLayout.CENTER);
			this.setBorder(	BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		
		public void populateBinBoxes( ) {

			is_numeric_list = new ArrayList<Boolean>();
			idx_ref_list = new ArrayList<Integer>();
			
			ArrayList<String> coltitles = new ArrayList<String>(); 
			for( int i = 0; i < operator.input.columns(); i++ ) {
				
				if(  operator.input.getColType(i) == ColType.CATEGORICAL || 
						operator.input.getColType(i) == ColType.ORDINAL || 
						operator.input.getColType(i) == ColType.NUMERIC  ) {
					
					coltitles.add( operator.input.getColName(i) );
					is_numeric_list.add( operator.input.getColType(i) == ColType.NUMERIC );
					idx_ref_list.add( i );
				}
			}

			String[] coltitle_arrays = new String[coltitles.size()];
			int k = 0;
			int sel_idx_1 = -1;
			int sel_idx_2 = -1;
			for(String s : coltitles) {
				
				coltitle_arrays[k] = s;
				if( coltitle_arrays[k] == operator.input.getColName(compDim1) )
					sel_idx_1 = k;
				if( coltitle_arrays[k] == operator.input.getColName(compDim2) )
					sel_idx_2 = k;
				k++;
			}
			
			pick_dim_1.setModel( new DefaultComboBoxModel( coltitle_arrays ) );
			pick_dim_2.setModel( new DefaultComboBoxModel( coltitle_arrays ) );
			
			pick_dim_1.setSelectedIndex( sel_idx_1 );
			pick_dim_2.setSelectedIndex( sel_idx_2 );
			
			if( binner_col1 != null ) {
				use_bins_1.setSelected( binner_col1.getUseBins() );
				if( use_bins_1.isSelected() )
					choose_bins_1.setSelectedIndex(binner_col1.getBinCount()-5);
			}
			else {
				use_bins_1.setSelected( false );				
			}
			if( binner_col2 != null ) {
				use_bins_2.setSelected( binner_col2.getUseBins() );
				if( use_bins_2.isSelected() )
					choose_bins_2.setSelectedIndex(binner_col2.getBinCount()-5);
			}
			else {
				use_bins_2.setSelected( false );				
			}

			// modify the enabled state
			
			choose_bins_1.setEnabled(is_numeric_list.get( pick_dim_1.getSelectedIndex() ));
			use_bins_1.setEnabled(is_numeric_list.get( pick_dim_1.getSelectedIndex() ));
			choose_bins_2.setEnabled(is_numeric_list.get( pick_dim_2.getSelectedIndex() ));
			use_bins_2.setEnabled(is_numeric_list.get( pick_dim_2.getSelectedIndex() ));
			choose_bins_1.setEnabled(use_bins_1.isSelected());
			choose_bins_2.setEnabled(use_bins_2.isSelected());
			
//			System.out.println("selected index 1 = " + sel_idx_1 );
//			System.out.println("selected index 2 = " + sel_idx_2 );
//			
		}
		
		public void actionPerformed(ActionEvent e) {

			if(e.getActionCommand().equalsIgnoreCase("scroll") ) {
				
				((OPAppletViewFrame)this.vframe).setScroll(!((OPAppletViewFrame)this.vframe).hasScroll);
				slider.setEnabled(((JCheckBox)e.getSource()).isSelected() );
				SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			          // Set the preferred size so that the layout managers can handle it
			        	pcap.invalidate();			        	
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height+1));
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height-1));
			        	pcap.heavyResize();
			        	pcap.redraw();
			        }
			      });
			}
			else if( e.getSource() == pick_dim_1 ) {

//				System.out.println("TEST");
				
				// adjust the model
				((CAOp)operator).compDim1 = idx_ref_list.get(pick_dim_1.getSelectedIndex());
				((CAOp)operator).compDim1Str = operator.getColName(((CAOp)operator).compDim1);	
				
//				System.out.println("compDim1 = " + ((CAOp)operator).compDim1 );
//				System.out.println("compDim1Str = " + ((CAOp)operator).compDim1Str );
//				
				// modify the gui
				choose_bins_1.setEnabled(is_numeric_list.get( pick_dim_1.getSelectedIndex() ));
				use_bins_1.setEnabled(is_numeric_list.get( pick_dim_1.getSelectedIndex() ));
				
				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else if( e.getSource() == pick_dim_2 ) {
				
				// adjust the model
				((CAOp)operator).compDim2 = idx_ref_list.get(pick_dim_2.getSelectedIndex());
				((CAOp)operator).compDim2Str = operator.getColName(((CAOp)operator).compDim2);	
				
				// modify the gui
				choose_bins_2.setEnabled(is_numeric_list.get( pick_dim_2.getSelectedIndex() ));
				use_bins_2.setEnabled(is_numeric_list.get( pick_dim_2.getSelectedIndex() ));
				
				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else if( e.getSource() == choose_bins_1 ) {
				
				((CAOp)operator).bin_count_1 = choose_bins_1.getSelectedIndex()+5;
				
				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else if( e.getSource() == choose_bins_2 ) {
				
				((CAOp)operator).bin_count_2 = choose_bins_2.getSelectedIndex()+5;
				
				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else if( e.getSource() == use_bins_1 ) {
				
				((CAOp)operator).binner_col1.setUseBins(use_bins_1.isSelected());
				choose_bins_1.setEnabled(use_bins_1.isSelected());

				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else if( e.getSource() == use_bins_2 ) {
				
				((CAOp)operator).binner_col2.setUseBins(use_bins_2.isSelected());
				choose_bins_2.setEnabled(use_bins_2.isSelected());

				// recalculate the correspondence analysis
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ) );
			}
			else {
				
				// update the gui
				populateBinBoxes( );				
			}
		}
		
		
		@Override
		public void stateChanged(ChangeEvent e) {

			
		}		
	}
}
