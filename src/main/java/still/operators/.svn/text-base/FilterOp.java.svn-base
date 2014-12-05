package still.operators;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jblas.DoubleMatrix;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.Table.ColType;
import still.gui.CheckBoxList;
import still.gui.OperatorView;

public class FilterOp extends Operator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8841744200097618408L;
	boolean[] filtermap = null;

	String[] binStrings = { "5", "6", "7", "8", "9" , "10" };
	int selected_dimension = 0;
	ArrayList<Integer> selected_values = new ArrayList<Integer>();
	ArrayList<Double> unique_values = null;
	boolean has_selection_dimension = false;
	boolean use_selection_dimension = false;
	int selection_dimension = 0;
	ArrayList<Integer> nonAttributeDims = null;
	boolean use_bins = true;
	
	int bin_count = Integer.parseInt( binStrings[binStrings.length-1] );
	ArrayList<Integer> selected_bins = new ArrayList<Integer>();
	double bin_size = -1;	
	double min_v = Double.POSITIVE_INFINITY;
	boolean inFiltration = false;
	int row_count = -1;
	
	public String getSaveString( ) {
		
		String saveString = "";
		
		saveString += use_selection_dimension;
		saveString += ",";
		saveString += selected_dimension;
		if( selected_values != null && selected_values.size() > 0) {
			
			saveString += ",";
			int k = 0;
			for( int sel_val : selected_values ) {
				
				k++;
				saveString += sel_val;
				if( k >= selected_values.size() ) {
					
					saveString += ",";
				}
			}
		}
		
		return saveString;
	}

	@Override
	public void activate() {

		isActive = true;
		updateMap();
		updateFunction();
		this.setView( new FilterView( this ) );
	}
	
	public int rows( ) {
	
		if( row_count == -1 ) {
			int k = 0;
			
			for( boolean b : filtermap ) {
				
				if( b ) {
					
					k++;
				}
			}
			
			row_count = k;
		}
		
		return row_count;
	}

	public void setMeasurement( int point_idx, int dim, double value ) {
		
		double[] back_proj = function.invert(map, point_idx, dim, value);

		int k = 0;
		for( int i = 0; i < map.rows(); i++  ) {
			
			if( map.map[i][dim] ) {
				
				this.input.setMeasurement( ((FilterFunction)function).remap[point_idx], i, back_proj[k]);
				k++;
			}
		}
	}

	public void tableChanged( TableEvent te ) {
		
		super.tableChanged(te);
		
		if( this.isActive() ) {
		
			if( te.type == TableEvent.TableEventType.ATTRIBUTE_CHANGED && this.use_selection_dimension ) {
				
				updateFunction();
		        tableChanged( new TableEvent( this, TableEvent.TableEventType.TABLE_CHANGED ), true );
			}
		}
	}
	
	@Override
	public void updateFunction() {

		// update filtermap
		
		boolean[] filtration = new boolean[ input.rows() ];
		int num_filtered = 0;
		
		if( use_selection_dimension ) {
			
			for( int i = 0; i < input.rows(); i++ ) {
				
				filtration[i] = ( input.getMeasurement(i, selection_dimension ) > 0.0 ); 
				if( filtration[i] ) {
					
					num_filtered++;
				}
			}
		}
		else {
							
			if( 	input.getColType( selected_dimension) == ColType.CATEGORICAL || 
					input.getColType( selected_dimension) == ColType.ORDINAL ) {
				
				for( int i = 0; i < input.rows(); i++ ) {
					
					filtration[i] = selected_values.contains( ( (int)input.getMeasurement(i, selected_dimension ) ) ); 
					if( filtration[i] ) {
						
						num_filtered++;
					}
				}
			}
			else {
				
				if( use_bins ) {
					
					for( int i = 0; i < input.rows(); i++ ) {
						
						for( int selected_bin : selected_bins ) {
							
							filtration[i] = filtration[i] || ( input.getMeasurement(i, selected_dimension ) >= min_v+selected_bin*bin_size &&
									  input.getMeasurement(i, selected_dimension ) <= min_v+(selected_bin+1)*bin_size ); 
						}
						if( filtration[i] ) {
							
							num_filtered++;
						}
					}
				}
				else {
					
					for( int i = 0; i < input.rows(); i++ ) {
						
						ArrayList<Double> subset = new ArrayList<Double>();
						for( int selected_value : selected_values ) {
							
							subset.add( unique_values.get(selected_value) );
						}
						filtration[i] = subset.contains( input.getMeasurement(i, selected_dimension ) ); 
						if( filtration[i] ) {
							
							num_filtered++;
						}
					}
				}
			}
		}
		
		if( num_filtered == 0 ) { // if none are selected, then ALL are selected
			
			Arrays.fill( filtration, true );
		}

		// update the operator 
		
		filtermap = filtration;
		row_count = -1;
		
		this.function = new FilterFunction( this.input, filtermap );		
	}

	@Override
	public void updateMap() {

		this.map = Map.generateDiagonalMap( input.columns() );
	}
	
	public String toString() {
		
		return "[Filter:Value]";
	}

	public static String getMenuName() {
		
		return "Filter:Value";
	}

	public FilterOp( Table newInput, boolean isActive, String paramString ) {
		
		super(newInput);
		
		this.isLazy = true;
		this.isActive = isActive;
		
		String[] params =  paramString.split(",");

		use_selection_dimension = Boolean.parseBoolean( params[0] );
		selected_dimension = Integer.parseInt( params[1] );
		if( params.length > 2 ) {
			
			for( int k = 2; k < params.length; k++ ) {
				
				selected_values.add( Integer.parseInt( params[k] ) );
			}
		}

		this.filtermap = new boolean[newInput.rows()];
		Arrays.fill( filtermap, true );
		
		if( isActive ) {
			activate();
		}
	}
	
	public FilterOp(Table newInput, boolean isActive) {

		super(newInput);
		
		this.isLazy = true;
		this.isActive = isActive;
		
		this.filtermap = new boolean[newInput.rows()];
		Arrays.fill( filtermap, true );
		
		if( isActive ) {
			activate();
		}
	}

	public class FilterFunction implements Function {

		public Table input = null;
		public DoubleMatrix normalizedColumns = null;
		public ArrayList<Integer> numericDims = null;
		public boolean[] mappedColumn = null;
		public int[] remap = null;
		
		public FilterFunction( Table input, boolean[] filtermap ) {
		
			this.input = input;
			
			int passthrough_count = 0;
			for( boolean b : filtermap ) {
				
				if( b ) {
					
					passthrough_count++;
				}
			}

			remap = new int[passthrough_count];
			
			int k = 0, i = 0;
			for( boolean b : filtermap ) {
				
				if( b ) {
					
					remap[k] = i;
					k++;
				}
				i++;
			}			
		}
	
		@Override
		public Table apply(Group group) {
			return null;
		}

		@Override
		public double compute(int row, int col) {

			return input.getMeasurement(remap[row], col);
		}

		@Override
		public Group inverse(Table dims) {
			return null;
		}

		@Override
		public double[] invert(Map map, int row, int col, double value) {
			double[] ret = new double[1];
			
			ret[0] = value;
			
			return ret;
		}

		@Override
		public int[] outMap() {

			return null;
		}
	}		
		
	public class FilterView extends OperatorView implements ItemListener, ListSelectionListener {

		JList dimensionList = null;
		//JList valueList = null;
		JComboBox binList = null;
		JCheckBox useBinsCheckbox = null;
		JCheckBox selectCheckbox = null;		
		NumberFormat nf = new DecimalFormat("0.##E0");
		CheckBoxList checkboxList = null;
				
		public ArrayList<Double> countUniqueValues( int dim, int limit ) {
			
			Hashtable<Double,Integer> uniqueHash = new Hashtable<Double,Integer>();
			int num_unique = 0;
			for( int i = 0; i < this.operator.rows(); i++ ) {
				if( ! uniqueHash.containsKey(this.operator.input.getMeasurement(i, dim))) {
					
					num_unique++;
					if( num_unique > limit ) {
						
						return null;
					}
					uniqueHash.put(this.operator.input.getMeasurement(i, dim), 1);
				}
			}			
			
			ArrayList<Double> vals = new ArrayList<Double>();
			for( double d : uniqueHash.keySet() ) {
				
				vals.add(d);
			}
			Collections.sort(vals);
			
			return vals;
		}

		public FilterView(FilterOp o) {
			
			super(o);			
			
			nonAttributeDims = Operator.getInverseDimValues(operator.input, ColType.ATTRIBUTE);
			selected_dimension = nonAttributeDims.get( 0 );

			this.setLayout( new BorderLayout(5,5) );
			
			dimensionList 	= new JList();
			checkboxList 	= new CheckBoxList( null );
			dimensionList.addListSelectionListener( this );
			checkboxList.addItemListener( this );
			
			JPanel doubleListPanel = new JPanel();
			JPanel dimPanel = new JPanel();
			JPanel valuePanel = new JPanel();
			JPanel selectModPanel = new JPanel();

			selectModPanel.setLayout(new GridLayout(1,2));
			JButton selectAllButton = new JButton("Select All");
			JButton selectNoneButton = new JButton("Select None");
			selectAllButton.setActionCommand("SELECTALL");
			selectNoneButton.setActionCommand("SELECTNONE");
			selectAllButton.addActionListener(this);
			selectNoneButton.addActionListener(this);
			selectModPanel.add( selectAllButton );			
			selectModPanel.add( selectNoneButton );
			
			dimPanel.setLayout(new BorderLayout(5,5));
			valuePanel.setLayout(new BorderLayout(5,5));
			doubleListPanel.setLayout( new GridLayout( 1, 2 ) );
			dimPanel.add( 	new JScrollPane(dimensionList), BorderLayout.CENTER);
			valuePanel.add( selectModPanel, BorderLayout.SOUTH );
			valuePanel.add( new JScrollPane(checkboxList), BorderLayout.CENTER);
			dimPanel.add( 	new JLabel("Dimensions"), BorderLayout.NORTH);
			valuePanel.add( new JLabel("Value"), BorderLayout.NORTH);
			doubleListPanel.add( dimPanel );
			doubleListPanel.add( valuePanel );
			this.add( doubleListPanel, BorderLayout.CENTER);
			
			selectCheckbox = new JCheckBox( "Filter By Selection Attribute" );
			selectCheckbox.addActionListener(this);
			this.add( selectCheckbox, BorderLayout.NORTH );
			
			useBinsCheckbox = new JCheckBox("Use Bins");
			useBinsCheckbox.setSelected(((FilterOp)o).use_bins);
			useBinsCheckbox.addActionListener(this);
			JPanel binPanel = new JPanel();
			binList = new JComboBox( binStrings );
			binList.setSelectedIndex(5);
			binList.addActionListener(this);
			binPanel.setLayout( new BorderLayout(5,5) );
			binPanel.add(new JLabel("Bins"), BorderLayout.NORTH);
			binPanel.add( binList,BorderLayout.CENTER );
			binPanel.add( useBinsCheckbox,BorderLayout.SOUTH );
			this.add(binPanel,BorderLayout.EAST);

			resetGUI();
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -4476127570420657983L;
		
		public void actionPerformed(ActionEvent e) {

			if( e.getSource() == selectCheckbox ) { 
					
				if( selectCheckbox.isSelected() ) {
					
					use_selection_dimension = true;
					
					// make everything else disabled
					dimensionList.setEnabled( false );
					checkboxList.setEnabled( false );
					binList.setEnabled( false );
				}
				else {
					
					use_selection_dimension = false;
					
					// enable everything else
					dimensionList.setEnabled( true );
					checkboxList.setEnabled( true );
					binList.setEnabled( true );
				}
				
				applyListFiltration();
			}	
			else if( e.getSource() == useBinsCheckbox ) {
				
				((FilterOp)this.operator).use_bins = useBinsCheckbox.isSelected();
				
				if( ((FilterOp)this.operator).use_bins ) {
					
					// recalculate the bins
					
					min_v = Double.POSITIVE_INFINITY;
					double max_v = Double.NEGATIVE_INFINITY;
					
					for( int i = 0; i < operator.input.rows(); i++ ) { 
						
						min_v = Math.min( operator.input.getMeasurement( i, selected_dimension ), min_v);
						max_v = Math.max( operator.input.getMeasurement( i, selected_dimension ), max_v);
					}
					
					ArrayList<String> listValues = new ArrayList<String>();
					bin_size = (max_v - min_v) / ((double) bin_count );
					for( int i = 0; i < bin_count; i++ ) {
						
						listValues.add( ""+nf.format(min_v+i*bin_size)+" to "+nf.format(min_v+(i+1)*bin_size) );
					}
					checkboxList.setModel(listValues);
					selected_bins = selected_values = new ArrayList<Integer>();	
					binList.setEnabled( true );			
				}
				else {
					
					// calculate the unique values
					unique_values = countUniqueValues( selected_dimension, 100 );
					if( unique_values != null ) {
						
						ArrayList<String> listValues = new ArrayList<String>();						
						for( double unique_value : unique_values ) {
							
							listValues.add( nf.format(unique_value) );
						}
						checkboxList.setModel(listValues);
						selected_bins = selected_values = new ArrayList<Integer>();				
						binList.setEnabled( false );			
					}
					else {
						
						use_bins = true;
						binList.setEnabled( true );			
						useBinsCheckbox.setSelected(true);
					}
				}
				
				applyListFiltration();
			}
			else if( e.getSource() == binList && use_bins ) { 
				
				bin_count = binList.getSelectedIndex()+5;
				
				// recalculate the bins
				
				min_v = Double.POSITIVE_INFINITY;
				double max_v = Double.NEGATIVE_INFINITY;
				
				for( int i = 0; i < operator.input.rows(); i++ ) { 
					
					min_v = Math.min( operator.input.getMeasurement( i, selected_dimension ), min_v);
					max_v = Math.max( operator.input.getMeasurement( i, selected_dimension ), max_v);
				}
				
				ArrayList<String> listValues = new ArrayList<String>();
				bin_size = (max_v - min_v) / ((double) bin_count );
				for( int i = 0; i < bin_count; i++ ) {
					
					listValues.add( ""+nf.format(min_v+i*bin_size)+" to "+nf.format(min_v+(i+1)*bin_size) );
				}
				checkboxList.setModel(listValues);
				selected_bins = selected_values = new ArrayList<Integer>();				
			}
			else if( e.getActionCommand().equals("SELECTALL")) {
				
				for( JCheckBox checkbox : checkboxList.checkboxes ) {
					
					checkbox.setSelected(true);
				}
			}
			else if( e.getActionCommand().equals("SELECTNONE")) {
				
				for( JCheckBox checkbox : checkboxList.checkboxes ) {
					
					checkbox.setSelected(false);
				}
			}
			else { // table changed

				// rebuild the gui

				if( ! inFiltration ){//e.getSource() != operator ) {
				
					resetGUI();
					applyListFiltration();
				}				
			}
		}

		public void resetGUI() {
			
			nonAttributeDims = Operator.getInverseDimValues(operator.input, ColType.ATTRIBUTE);

			DefaultListModel listDims = new DefaultListModel();
			ArrayList<String> listValues = new ArrayList<String>();
			
			for( int i : nonAttributeDims) {
			
				listDims.addElement( operator.input.getColName(i) );
			}
			
			if( 	operator.input.getColType( selected_dimension) == ColType.CATEGORICAL || 
					operator.input.getColType( selected_dimension) == ColType.ORDINAL ) {
				
				for( String s : operator.input.getCategories( selected_dimension ) ) {
					
					listValues.add( s );
				}
			}
			else {
				
				if( use_bins || unique_values == null ) {
					min_v = Double.POSITIVE_INFINITY;
					double max_v = Double.NEGATIVE_INFINITY;
					
					for( int i = 0; i < operator.input.rows(); i++ ) { 
						
						min_v = Math.min( operator.input.getMeasurement( i, selected_dimension ), min_v);
						max_v = Math.max( operator.input.getMeasurement( i, selected_dimension ), max_v);
					}
					
					bin_size = (max_v - min_v) / ((double) bin_count );
					for( int i = 0; i < bin_count; i++ ) {
						
						listValues.add( ""+nf.format(min_v+i*bin_size)+" to "+nf.format(min_v+(i+1)*bin_size) );
					}				
				}
				else {
					
					for( double unique_value : unique_values ) {
						
						listValues.add( nf.format(unique_value) );
					}
					checkboxList.setModel(listValues);
					selected_bins = selected_values = new ArrayList<Integer>();				
					binList.setEnabled( false );							
				}
			}
			
			dimensionList.setModel( listDims );
			checkboxList.setModel( listValues );
			
			for( int i = 0; i < operator.input.columns(); i++ ) {
				
				if( operator.input.getColType(i) == ColType.ATTRIBUTE && operator.input.getColName(i).equalsIgnoreCase("selection") ) {
					
					has_selection_dimension = true;
					selection_dimension = i;
					break;
				}
			}
									
			selectCheckbox.setEnabled( has_selection_dimension );
			if( !has_selection_dimension && selectCheckbox.isSelected() ) {
				
				selectCheckbox.setSelected(false);
			}
			if( selectCheckbox.isSelected() ) {
				
				dimensionList.setEnabled(false);
				checkboxList.setEnabled(false);
				binList.setEnabled(false);
			}
			else {

				dimensionList.setEnabled(true);
				checkboxList.setEnabled(true);
				binList.setEnabled(true);
			}
			
			binList.setEnabled( ( operator.input.getColType(selected_dimension) == ColType.NUMERIC ) && (use_bins || unique_values == null) );			
		}
		
		public void applyListFiltration() {

			operator.updateFunction();
			inFiltration = true;
	        operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.TABLE_CHANGED ), true );
	        inFiltration = false;
		}
		
		@Override
		public void valueChanged(ListSelectionEvent e) {

			if( ! e.getValueIsAdjusting() ) {
				
				if( e.getSource() == this.dimensionList && ((JList)e.getSource()).getSelectedIndex() != -1 ) {
									
					selected_dimension = nonAttributeDims.get( ((JList)e.getSource()).getSelectedIndex() );
					ArrayList<String> listValues = new ArrayList<String>();
	
					if( 	this.operator.input.getColType( selected_dimension) == ColType.CATEGORICAL || 
							this.operator.input.getColType( selected_dimension) == ColType.ORDINAL ) {
						
						for( String s : this.operator.input.getCategories( selected_dimension ) ) {
							
							listValues.add( s );
						}
						
						binList.setEnabled( false );
					}
					else {
						
						unique_values = countUniqueValues( selected_dimension, 100 );

						if( use_bins || unique_values == null ) {
							
							use_bins = true;
							min_v = Double.POSITIVE_INFINITY;
							double max_v = Double.NEGATIVE_INFINITY;
							
							for( int i = 0; i < this.operator.input.rows(); i++ ) { 
								
								min_v = Math.min( this.operator.input.getMeasurement( i, selected_dimension ), min_v);
								max_v = Math.max( this.operator.input.getMeasurement( i, selected_dimension ), max_v);
							}
							
							bin_size = (max_v - min_v) / ((double) bin_count );
							for( int i = 0; i < bin_count; i++ ) {
								
								listValues.add( ""+nf.format(min_v+i*bin_size)+" to "+nf.format(min_v+(i+1)*bin_size) );
							}				
							
							binList.setEnabled( true );
							useBinsCheckbox.setSelected(true);

						}
						else {
							
							listValues = new ArrayList<String>();						
							for( double unique_value : unique_values ) {
								
								listValues.add( nf.format(unique_value) );
							}
							checkboxList.setModel(listValues);
							selected_bins = selected_values = new ArrayList<Integer>();				
							binList.setEnabled( false );							
						}
					}
					
					checkboxList.setModel(listValues);
					
					// set the selection back to zero
					selected_values = new ArrayList<Integer>();
					selected_bins = new ArrayList<Integer>();					
				}
				
				applyListFiltration();
			}
		}

		@Override
		public void itemStateChanged(ItemEvent e) {

			selected_bins = selected_values = checkboxList.getSelectedNums();
			applyListFiltration();
		}
	}	
}
