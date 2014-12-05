package still.operators;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import processing.core.PConstants;
import processing.core.PGraphics;

import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.gui.OperatorView;

public class ColorOp extends Operator implements Serializable {

	static int MAX_COLORS = 300;
	static int[] colorTable = {	
		
		0xFF258BC1, // blue
		0xFFFE9729, // orange
		0xFF32AB5A, // green
		0xFFDF3D33, // red
		0xFFA67FBE, // purple
		0xFF9D6A5E, // brown
		0xFFE897C7, // pink
		0xFF919090, // grey
		0xFFC7C52B, // gold 
		0xFF05C7D7 // teal
	};
	int[] color_list = {};
	
	int[] remap = null;

	public void setMeasurement( int point_idx, int dim, double value ) {
		
		if( hasColorCol || (!hasColorCol && dim < map.columns()-1 ) ) {
			
			if( cullColorBy ) {
				
				input.setMeasurement(point_idx, remap[dim], value);
			}
			else {
				input.setMeasurement(point_idx, dim, value);
			}
		}
		else {
			
			colorColVal[point_idx][0] = value;
		}
	}

	public ColType getColType( int dim ) {
		
		if( hasColorCol || (!hasColorCol && dim < map.columns()-1 ) ) {			
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

	public void activate() {

		isActive = true;
		
		updateMap();
		updateFunction();
		isLazy  		= true;
		setView( new ColorView( this ) );		
	}
	
	public String getSaveString( ) {
		
		String saveString = "";
		
		saveString += colorByCol;
		saveString += ",";
		saveString += cullColorBy;		
		saveString += ",";
		if( this.color_list != null ) {
			
			for( int i = 0; i < this.color_list.length; i++ ) {
				
				saveString += ""+this.color_list[i];
				if( i < this.color_list.length-1 ) {
					
					saveString += ":";
				}
			}
		}
		
		return saveString;
	}

	/**
	 * Parameterized Operator creation
	 * 
	 * @param newInput
	 * @param isActive
	 * @param paramString
	 */
	public ColorOp( Table newInput, boolean isActive, String paramString ) {
		
		super(newInput);
		
		// extract parameters
		
		String[] params = paramString.split(",");
		colorByCol = Integer.parseInt(params[0]);
		cullColorBy = Boolean.parseBoolean(params[1]);
		if( params[2].length() > 0 ) {
			
			String[] colors = params[2].split(":");
			color_list = new int[colors.length];
			int i = 0;
			for(String colorStr : colors) {
				
				color_list[i] = Integer.parseInt( colorStr );
			}
		}
		
		// handle if the parameters are inappropriate
		if( Operator.getNonAttributeDims(newInput) <= colorByCol ) {
			
			colorByCol = 0;	// reset column to zero
		}
		
		this.isActive = isActive;
		if( isActive ) {
			
			updateMap();
			updateFunction();
			isLazy  		= true;
			setView( new ColorView( this ) );		
		}
	}
	
	public ColorOp(Table newInput, boolean isActive) {
		super(newInput);
				
		this.isActive = isActive;
		if( isActive ) {
			
			updateMap();
			updateFunction();
			isLazy  		= true;
			setView( new ColorView( this ) );		
		}
	}
	
	public String getColName( int dim ) {

		if( hasColorCol || (!hasColorCol && dim < map.columns()-1 )) {
		
			ArrayList<Integer> colsamp = map.getColumnSamples(dim); 
			if( colsamp.size() > 1 ) {
				
				return (this.toString() + dim);
			}
			else if( colsamp.size() == 1 ){
				
				return input.getColName(colsamp.get(0));
			}
		}

		return "color";
	}

	public static String getMenuName() {
		
		return "Attrib:Color";
	}
	
	public String toString() {
		
		return "[Attrib:Color]";
	}

	public int colorByCol	= 0;
	public int colorCol 	= -1;
	double colorColVal[][] 	= null;
	boolean hasColorCol 	= false;
	boolean cullColorBy		= true;
	
	@Override
	public void updateFunction() {
		if( ! hasColorCol ) {
			function		= new AppendFunction(input, colorColVal, map );
		}
		else {
			function 		= new IdentityFunction( input );
		}
	}
	
	public void updateMap() {
		
		// handle the color operator
		hasColorCol = false;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if( 	input.getColName(i).equalsIgnoreCase("color")  &&
					input.getColType(i) == ColType.ATTRIBUTE ) {
				
				hasColorCol = true;
				colorCol 	= i;
			}
		}
		
		if( ! hasColorCol ){
			
			if( cullColorBy ) {
				
				colorCol = input.columns()-1;
			}
			else {
				
				colorCol 	= input.columns();
			}
			
			colorColVal = new double[input.rows()][1];
									
			if( cullColorBy ) {
				
				boolean[] map_vals 	= new boolean[input.columns() + 1];
				Arrays.fill(map_vals, true);
				map_vals[colorByCol] = false;
				map = Map.generateCullMap( map_vals );
				remap = new int[map.columns()];
				for( int i = 0; i < map.columns(); i++ ) {
					
					remap[i] = map.getColumnSamples(i).get(0);
				}
			}
			else {
				
				map 		= Map.generateDiagonalMap(input.columns()+1);
			}
		}
		else {
			
			if( cullColorBy ) {

				boolean[] map_vals 	= new boolean[input.columns()];
				Arrays.fill(map_vals, true);
				map_vals[colorByCol] = false;
				
				if( colorByCol < colorCol ) {
					colorCol--;
				}
				 
				map = Map.generateCullMap( map_vals );
				remap = new int[map.columns()];
				for( int i = 0; i < map.columns(); i++ ) {
					
					remap[i] = map.getColumnSamples(i).get(0);
				}
			}
			else {
				
				map 		= Map.generateDiagonalMap(input.columns());
			}
		}
	}
	
	public class ColorView extends OperatorView {
		
		Random myRandom = null;
		Hashtable<Double,Integer> uniqueHash = null;
		JCheckBox cullButton 			= null;
		JRadioButton categoricalButton 	= null;
		JRadioButton sequentialButton	= null;
		JButton permuteButton 			= null;
		JButton modifyButton 			= null;
		boolean isCategorical 			= true;		
		JPanel masterPanel = null;
		int maxColor = 0xFF258BC1;
		int minColor = 0xFFFFFFFF;
		JList colorList = null;
		JPanel highlowPanel = null;
		JScrollPane jsp = null;
		int[] permutation_map = null;
		boolean resetPerm = false;
		boolean resetColorList = false;

		/**
		 * 
		 */
		public void calculatePermutationMap( ) {
			
			permutation_map = new int[uniqueHash.size()];
			for( int i = 0; i < permutation_map.length; i++ ) {
				
				permutation_map[i] = i;
			}
			for( int i = 0; i < permutation_map.length; i++ ) {

				int shuffle_idx = i + myRandom.nextInt(permutation_map.length-i);
				int idx_temp 	= permutation_map[i];
				
				permutation_map[i] 				= permutation_map[shuffle_idx];
				permutation_map[shuffle_idx] 	= idx_temp;
			}
		}
		
		/**
		 *  actually calculate the color column values based on gui settings
		 */
		public void populateColorColumn( ) {
			
			ColorOp o = (ColorOp)operator;
			
			if( isCategorical ) {
				
				if( !resetColorList && 
						((color_list == null) || 
								(color_list.length != uniqueHash.keySet().size() ) ) ) {
					
					resetColorList = true;
				}
				
				if( resetColorList ) {

					resetColorList = false;
					color_list = new int[uniqueHash.keySet().size()];
					int i = 0;
					for( double d : uniqueHash.keySet() ) {
						
						int p = (permutation_map[uniqueHash.get(d)]);		
						//color_list[i] = ColorOp.colorTable[ ((int)d) % ColorOp.colorTable.length];
						color_list[uniqueHash.get(d)] = ColorOp.colorTable[ p % ColorOp.colorTable.length];
						i++;
					}
				}
				
				for( int i = 0; i < o.rows(); i++ ) {
					
					double v = o.input.getMeasurement(i, o.colorByCol);
					int nkeys = 0;
					for( Enumeration<Double> e = uniqueHash.keys(); e.hasMoreElements(); ) {
						e.nextElement();
						nkeys++;
					}

					//int p = (permutation_map[uniqueHash.get(v)]);
					o.setMeasurement(	i, 
										o.colorCol, 
										(double)color_list[uniqueHash.get(v)]);
//										(double)ColorOp.colorTable[ p % ColorOp.colorTable.length]);
				}
			}
			else {

				double min_v = o.input.getMeasurement(0, o.colorByCol);
				double max_v = o.input.getMeasurement(0, o.colorByCol);
				for( int i = 1; i < o.rows(); i++ ) {
					
					double v = o.input.getMeasurement(i, o.colorByCol);
					min_v = Math.min( min_v, v );
					max_v = Math.max( max_v, v );
				}				
				
				double scale = max_v - min_v;
				if( scale <= 0.) {
					scale = 1.0;
				}
				
				for( int i = 0; i < o.rows(); i++ ) {
					
					double v = o.input.getMeasurement(i, o.colorByCol);		
					double val = (double) PGraphics.lerpColor( minColor, maxColor, (float)((v-min_v)/scale), PConstants.RGB );
					o.setMeasurement(i, o.colorCol, val );
				}
			}
		}

		/**
		 *  Construct and populate the gui
		 */
		public void buildGui() {
			
			if( masterPanel != null ) {
				this.removeAll();
				masterPanel.removeAll();
			}

			masterPanel 			= new JPanel( new BorderLayout(5,5) );
			JPanel selectionPanel 	= new JPanel( new BorderLayout(5,5) );
			JPanel inputPanel 		= new JPanel( new BorderLayout(5,5) );
			JPanel typePanel		= new JPanel( new GridLayout(4,1,5,5) );
			JPanel buttonPanel 		= new JPanel( new GridLayout(1,2,5,5));
			
			Operator o = this.operator;
			
			String[] dimString = new String[o.countDimTypeInverse(ColType.ATTRIBUTE)];
			ArrayList<Integer> colIdxs = o.getDimTypeColsInverse(ColType.ATTRIBUTE);
			for( int i = 0; i < o.countDimTypeInverse(ColType.ATTRIBUTE); i++ ) {
				
				dimString[i] = o.input.getColName(colIdxs.get(i));
			}
			JComboBox dimBox = new JComboBox( dimString );
			
			if(((ColorOp)o).colorByCol >= colIdxs.size() ) {
				
				((ColorOp)o).colorByCol = colIdxs.size() - 1;
			}
			
			dimBox.setSelectedIndex(colIdxs.get(((ColorOp)o).colorByCol) );
			dimBox.addActionListener(this);

			permuteButton		= new JButton("Permute Colors");			
			modifyButton		= new JButton("Modify Color");			
			cullButton			= new JCheckBox("Cull Color-By Dimension");
			cullButton.setActionCommand("cull");
			cullButton.setSelected( ((ColorOp)o).cullColorBy );
			categoricalButton 	= new JRadioButton("Categorical");
			categoricalButton.setActionCommand("categorical");
			sequentialButton 	= new JRadioButton("Sequential");
			sequentialButton.setActionCommand("sequential");
			ButtonGroup bgroup = new ButtonGroup();	
			bgroup.add(categoricalButton);
			bgroup.add(sequentialButton);
			categoricalButton.addActionListener(this);
			sequentialButton.addActionListener(this);
			permuteButton.addActionListener(this);
			modifyButton.addActionListener(this);
			cullButton.addActionListener(this);
			
			masterPanel.add(selectionPanel,"West");
			selectionPanel.add(inputPanel,"North");
			selectionPanel.add(typePanel,"Center");
			inputPanel.add( new JLabel("Input Dimension:"),"West");
			inputPanel.add(dimBox,"Center");
			typePanel.add( categoricalButton );
			typePanel.add( sequentialButton );	
			typePanel.add( cullButton );
			buttonPanel.add(permuteButton);
			buttonPanel.add(modifyButton);
			typePanel.add( buttonPanel );
			
			// set up the initial coloring panels
			
			int u_vals = countUniqueValues( colIdxs.get(((ColorOp)o).colorByCol), ColorOp.MAX_COLORS );
			if( u_vals < 0 ) {
				
				isCategorical = false;
				categoricalButton.setEnabled(false);
			}
			if( isCategorical ) {
				
				categoricalButton.setSelected( true );
				sequentialButton.setSelected( false );
			}
			else {

				categoricalButton.setSelected( false );
				sequentialButton.setSelected( true );
			}
			
			adjustColorPanel();
			
			this.setLayout(new BorderLayout(5,5));
			this.add(masterPanel,"Center");
			this.setBorder(	BorderFactory.createEmptyBorder(10, 10, 10, 10));
		}
		
		public ColorView(Operator o) {
			super(o);

			myRandom = new Random();
			buildGui();
			resetColorList = ( color_list == null );
			if( uniqueHash != null && color_list != null & uniqueHash.keySet().size() != color_list.length ) {
				resetColorList = true;
			}
//			resetColorList = color_list.length
			populateColorColumn();
			
		}

		public class Swatch extends JPanel {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -8234423483584552126L;
			int color = 0;
			public Swatch( int color ) {
				
				super();
				this.setPreferredSize(new Dimension(25,25));
				this.color = color;
			}
			
			public void paintComponent( Graphics g ) {
				
				super.paintComponent(g);				
				Graphics2D g2D = (Graphics2D) g;
				g2D.setColor( new Color( color & 0xFFFFFF)  );
				g2D.fillRect(5, 5, this.getWidth()-10, this.getHeight()-10);				
			}
		}
		
		public class SwatchIcon implements Icon{
		    
			int color = 0;
			public SwatchIcon( int color ) {
				
				this.color = color;
			}
			
		    private int width = 32;
		    private int height = 32;
		    
		    public void paintIcon(Component c, Graphics g, int x, int y) {
		        Graphics2D g2d = (Graphics2D) g.create();
		        
		        g2d.setColor(new Color( color & 0xFFFFFF ));
		        g2d.fillRect(x +1 ,y + 1,width -2 ,height -2);
		        g2d.setColor( Color.BLACK );
		        g2d.drawRect(x +1 ,y + 1,width -2 ,height -2);
		        g2d.dispose();
		    }
		    
		    public int getIconWidth() {
		        return width;
		    }
		    
		    public int getIconHeight() {
		        return height;
		    }
		}

		public class ColorValRenderer implements ListCellRenderer {

			/**
			 * 
			 */
			private static final long serialVersionUID = -6924832754080505174L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				
				JPanel panel = new JPanel( new BorderLayout(20,5) );
				//JPanel swatch = new Swatch(ColorOp.colorTable[permutation_map[index] % ColorOp.colorTable.length]); 
				JPanel swatch = new Swatch(color_list[index]); 
				panel.add( swatch ,"West");
				JLabel text = new JLabel((String)value);
				panel.add(text,"Center");
				if( isSelected ) {
					
					swatch.setBackground(list.getSelectionBackground() );
					swatch.setForeground(list.getSelectionForeground());
					panel.setBackground( list.getSelectionBackground() );
					panel.setForeground(list.getSelectionForeground() );
					text.setBackground( list.getSelectionBackground() );
					text.setForeground(list.getSelectionForeground() );
					
				}
				else {
					swatch.setBackground(list.getBackground() );
					swatch.setForeground(list.getForeground());
					panel.setBackground( list.getBackground() );
					panel.setForeground(list.getForeground() );
					text.setBackground( list.getBackground() );
					text.setForeground(list.getForeground() );
				}
				
				return panel;
			}
			
		}
		public void adjustColorPanel( ) {
			
			ColorOp colorOp = (ColorOp) this.operator;
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(3);

			if( isCategorical ) {
			
				// build the Jtable with the different colors
				
				double[] vals = new double[uniqueHash.keySet().size()];
				String[] listVals = new String[uniqueHash.keySet().size()];
				int k = 0;
				for( double d : uniqueHash.keySet() ) {
					
					vals[k] = d;
					k++;
				}
				Arrays.sort(vals);
				k=0;
				
				for( double d: vals ) {
					
					ColorOp o = (ColorOp)operator;
					
					if( o.input.getColType( o.colorByCol ) == ColType.CATEGORICAL ) {
						
						listVals[k] = o.input.getCategories(o.colorByCol)[(int)d];
					}
					else {
						
						listVals[k] = ""+nf.format(d);
					}
					k++;
				}
				if(  jsp != null || colorList != null ) {
					
					masterPanel.remove(jsp);
//					masterPanel.remove(colorList);
					jsp = null;
					colorList = null;
				}
				else if( highlowPanel != null ) {
					
					masterPanel.remove(highlowPanel);
					highlowPanel = null;
				}
				colorList = new JList(listVals);
				ColorValRenderer renderer = new ColorValRenderer();
				colorList.setCellRenderer( renderer );
				jsp = new JScrollPane( colorList );
				this.masterPanel.add( jsp, "Center");
				permuteButton.setEnabled(true);
				permuteButton.setVisible(true);
			}
			else {
				
				// get the least and the most
				double cmin = colorOp.input.getMeasurement(0,colorOp.colorByCol);
				double cmax = colorOp.input.getMeasurement(0,colorOp.colorByCol);
				for( int i = 1; i < colorOp.input.rows(); i++ ) {
					
					cmin = Math.min(cmin,colorOp.input.getMeasurement(i,colorOp.colorByCol));
					cmax = Math.max(cmax,colorOp.input.getMeasurement(i,colorOp.colorByCol));
				}
								
				JButton leastButton = new JButton("Lowest", new SwatchIcon( this.minColor ) );
				JButton mostButton = new JButton("Highest", new SwatchIcon( this.maxColor ) );
				leastButton.setActionCommand("lowest");
				mostButton.setActionCommand("most");
				leastButton.addActionListener(this);
				mostButton.addActionListener(this);
				if( jsp != null || colorList != null ) {
					
					masterPanel.remove(jsp);
					jsp = null;
					colorList = null;
				}
				else if( highlowPanel != null ) {
					
					masterPanel.remove(highlowPanel);
					highlowPanel = null;
				}
				highlowPanel = new JPanel( new GridLayout(2,1,50,50) );
				highlowPanel.add(leastButton);
				highlowPanel.add(mostButton);
				masterPanel.add(highlowPanel,"Center");
				permuteButton.setEnabled(false);
				permuteButton.setVisible(false);
			}			
		}
		
		public int countUniqueValues( int dim, int limit ) {
			
			uniqueHash = new Hashtable<Double,Integer>();
			int num_unique = 0;
			ArrayList<Integer> colIdxs = operator.getDimTypeColsInverse(ColType.ATTRIBUTE);
			for( int i = 0; i < this.operator.rows(); i++ ) {
				if( ! uniqueHash.containsKey(this.operator.input.getMeasurement(i, colIdxs.get(dim)))) {
					
					num_unique++;
					if( num_unique > limit ) {
						
						return -1;
					}
					uniqueHash.put(this.operator.input.getMeasurement(i, colIdxs.get(dim)), 1);
				}
			}
			
			double[] vals = new double[uniqueHash.keySet().size()];
			int k = 0;
			for( double d : uniqueHash.keySet() ) {
				
				vals[k] = d;
				k++;
			}
			Arrays.sort(vals);
			k = 0;
			for( double d: vals ) {
				
				uniqueHash.put(d, k);
				k++;
			}

			if( permutation_map == null || (this.resetPerm) ) {
				
				this.resetPerm = false;
				this.calculatePermutationMap();
			}
			if(permutation_map.length < uniqueHash.size()) {
				
				int[] tempmap = new int[uniqueHash.size()];
				for( int i = 0; i < Math.min(permutation_map.length,tempmap.length);i++) {
					
					tempmap[i] = permutation_map[i];
				}
				if( tempmap.length > permutation_map.length ) {
					
					for( int i = permutation_map.length; i < tempmap.length;i++) {
						
						tempmap[i]=i;
					}
				}
				permutation_map = tempmap;
			}
			
			return num_unique;
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1751518097157582589L;
		
		public void actionPerformed(ActionEvent e) {

			// they've switched the input dimension
			if( e.getSource() instanceof JComboBox ) {
				
				JComboBox cb = (JComboBox)e.getSource();
				ArrayList<Integer> colIdxs = operator.getDimTypeColsInverse(ColType.ATTRIBUTE);
				((ColorOp) this.operator).colorByCol = colIdxs.get( cb.getSelectedIndex() );
				this.resetPerm = true;
				int uval = countUniqueValues( cb.getSelectedIndex(), ColorOp.MAX_COLORS );				
				if( uval < 0 ) {
					
					categoricalButton.setEnabled(false);
					sequentialButton.setSelected(true);
					isCategorical = false;
				}
				else {
					
					categoricalButton.setEnabled(true);					
				}
				
				if( ((ColorOp)this.operator).cullColorBy) {

					operator.tableChanged( new TableEvent(operator, TableEvent.TableEventType.TABLE_CHANGED ) );
				}
				else {

					adjustColorPanel();
					resetColorList = true;
					populateColorColumn( );
					operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
					this.validate();
					this.repaint();
				}
			}
			
			// they've switched whether it was categorical versus sequential
			if( e.getSource() instanceof JRadioButton ) {
				
				if( e.getActionCommand().equalsIgnoreCase("categorical")) {
					
					isCategorical = true;
				}
				else {
					isCategorical = false;
				}

				adjustColorPanel();
				resetColorList = true;
				populateColorColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
				this.validate();
				this.repaint();
			}
			
			// the underlying operator has changed
			if( e.getSource() instanceof Operator ) {

				buildGui();
//				resetColorList = true;
				populateColorColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
				this.validate();
				this.repaint();
			}

			// we have switched between culling and not-culling the coloring dimension
			if( e.getActionCommand().equalsIgnoreCase("cull")) {
				
				((ColorOp)operator).cullColorBy = cullButton.isSelected();
				operator.tableChanged( new TableEvent(operator, TableEvent.TableEventType.TABLE_CHANGED ) );
				this.validate();
				this.repaint();
			}
			
			// the spectrum endpoints are changing
			if( e.getActionCommand().equalsIgnoreCase("lowest") ) {
				Color passColor = new Color(	(this.minColor&0xFF0000)>>16,
												(this.minColor&0xFF00)>>8,
												this.minColor&0xFF);
				
				Color newColor = JColorChooser.showDialog(
	                     this,
	                     "Choose color for the lowest end of the spectrum.",
	                     passColor);
				this.minColor = 	0xFF000000 + 
									(newColor.getRed() << 16) + 
									(newColor.getGreen() << 8) + 
									(newColor.getBlue());
				adjustColorPanel();
				populateColorColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
				this.validate();
				this.repaint();
			}

			// the spectrum endpoints are changing
			if( e.getActionCommand().equalsIgnoreCase("most") ) {

				Color passColor = new Color(	(this.maxColor&0xFF0000)>>16,
												(this.maxColor&0xFF00)>>8,
												this.maxColor&0xFF);
				
				Color newColor = JColorChooser.showDialog(
	                     this,
	                     "Choose color for the lowest end of the spectrum.",
	                     passColor);
				this.maxColor = 	0xFF000000 + 
									(newColor.getRed() << 16) + 
									(newColor.getGreen() << 8) + 
									(newColor.getBlue());
				adjustColorPanel();
				populateColorColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
				this.validate();
				this.repaint();
			}
			
			// permute the colors
			if( e.getSource() == permuteButton ) {
				
				this.resetPerm = true;
				buildGui();
				resetColorList = true;
				populateColorColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
				this.validate();
				this.repaint();
			}
			
			if( e.getSource() == modifyButton ) {

				resetColorList = false;
				int selIdx = colorList.getSelectedIndex();
				if( selIdx != -1 ) {
					Color passColor = new Color(	(color_list[selIdx]&0xFF0000)>>16,
							(color_list[selIdx]&0xFF00)>>8,
							color_list[selIdx]&0xFF);
	
					Color newColor = JColorChooser.showDialog(
					 this,
					 "Choose color for the lowest end of the spectrum.",
					 passColor);
					color_list[selIdx] = 	0xFF000000 + 
								(newColor.getRed() << 16) + 
								(newColor.getGreen() << 8) + 
								(newColor.getBlue());
					adjustColorPanel();
					populateColorColumn( );
					operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, "color", null, false) );
					this.validate();
					this.repaint();
				}
			}
		}		
	}

}