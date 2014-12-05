package still.operators;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.gui.OPAppletViewFrame;
import still.gui.OperatorView;
import still.gui.PHistPainter;
import still.operators.PearsonCollectOp.PCollectView;
import still.operators.SplomOp.SplomView;

public class HistogramOp extends Operator implements Serializable {

	int selectionCol 	= -1;
	boolean hasSelOp 	= false;
	double[][] selCol 	= null;
		
	public String getSaveString( ) {
	
		return "";
	}
	
	public static String getMenuName() {
		
		return "View:Histogram";
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
		
		return "[View:Histo]";
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
		setView( new HistView( this ) );		
	}
	
	public void loadOperatorView() {
		
		setView( new HistView( this ) );
	}

	public HistogramOp( Table newTable, boolean isActive, String paramString ) {
		
		this( newTable, isActive );
	}
	
	public HistogramOp( Table newTable, boolean isActive ) {
		
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
			setView( new HistView( this ) );		
		}
	}

	public void tableChanged( TableEvent te ) {
		
		super.tableChanged(te);
		
		if( this.isActive() ) {
			SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	    		// change the hilight or color status of the underlying visualization
	    		((PHistPainter)((OPAppletViewFrame)((HistView)getView()).getViewFrame()).procApp).buildSelHist();
	    		((OPAppletViewFrame)((HistView)getView()).getViewFrame()).procApp.redraw();
	        }
			});
		}
	}
	
	@Override
	public void updateFunction() {

		if( ! hasSelOp ) {
			function	= new AppendFunction(input, selCol );
		}
		else {
			function 		= new IdentityFunction( input );
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

	public class HistView extends OperatorView implements ChangeListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1604261613966166897L;
		JSlider slider = null;
		JSlider hist_slider = null;
		JSlider size_slider = null;
		JCheckBox sq_layout_checkbox = null;
		PHistPainter php = null;
		
		public HistView(Operator o) {
			super(o);

			this.setLayout( new BorderLayout() );
//			this.setLayout( new GridLayout(3,1,5,5));
			
			php = new PHistPainter(o);
			vframe = new OPAppletViewFrame("E"+o.getExpressionNumber()+":"+o, php );			
			vframe.addComponentListener(this);
			
			JPanel slider_panel = new JPanel();
			slider_panel.setLayout(new BorderLayout(5,5));
			
			Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>();
			int splomsizer = 50;
			for( int i = splomsizer; i <= 500; i += splomsizer) {				
				labelTable.put(new Integer((i-splomsizer)/splomsizer),new JLabel(""+i));
			}
			slider = new JSlider( 	JSlider.HORIZONTAL, 
									0, 
									(500-splomsizer)/splomsizer, 
									0);
			slider.setName("slider");
			slider.setMajorTickSpacing( 5 );
			slider.setMinorTickSpacing( 1 );
			slider.setLabelTable(labelTable);
			slider.setPaintLabels(true);	
			
			slider.addChangeListener(this);			
			slider.addChangeListener(php);			
			//slider_panel.add(slider,"Center");
			//slider_panel.add(new JLabel("Min Plot Size"), "West");
			
			//this.add(slider_panel,"Center");
			
			JPanel hist_panel = new JPanel();
			hist_panel.setLayout(new BorderLayout(5,5));
			
			Hashtable<Integer,JLabel> labelTableHist = new Hashtable<Integer,JLabel>();
			for( int i = 20; i <= 100; i += 10) {				
				labelTableHist.put(new Integer((i-20)),new JLabel(""+i));
			}
			hist_slider = new JSlider( 	JSlider.HORIZONTAL, 
										0, 
										80, 
										0);
			hist_slider.setName("hist_slider");
			hist_slider.setMajorTickSpacing( 10 );
			hist_slider.setMinorTickSpacing( 1 );
			hist_slider.setLabelTable(labelTableHist);
			hist_slider.setPaintLabels(true);	
			
			hist_slider.addChangeListener(this);			
			hist_slider.addChangeListener(php);			
			hist_panel.add(hist_slider,"Center");
			hist_panel.add(new JLabel("Hist Bins"), "West");

			sq_layout_checkbox = new JCheckBox( "Use Square Layout" );
			sq_layout_checkbox.setActionCommand("square");
			sq_layout_checkbox.addActionListener( php );
			sq_layout_checkbox.setSelected( php.isSquareLayout() );
			
			this.add(hist_panel,BorderLayout.CENTER);
			this.add( sq_layout_checkbox, BorderLayout.NORTH );			
			this.setBorder(	BorderFactory.createEmptyBorder(10, 10, 10, 10));
			JCheckBox scrollable_button = new JCheckBox("Manual Plot Size");
			scrollable_button.setActionCommand("scroll");
			scrollable_button.addActionListener(this);
			JPanel sizingPanel = new JPanel();
			sizingPanel.setLayout( new BorderLayout() );
			sizingPanel.add(scrollable_button,BorderLayout.WEST);
			sizingPanel.add(slider,BorderLayout.CENTER);
			this.add(sizingPanel, BorderLayout.SOUTH );

		}
		
		public void actionPerformed(ActionEvent e) {

			if(e.getActionCommand().equalsIgnoreCase("scroll") ) {
				
				((OPAppletViewFrame)this.vframe).setScroll(!((OPAppletViewFrame)this.vframe).hasScroll);
				slider.setEnabled(((JCheckBox)e.getSource()).isSelected() );
				SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			          // Set the preferred size so that the layout managers can handle it
			        	php.invalidate();			        	
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height+1));
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height-1));
						php.heavyResize();
						php.redraw();
			        }
			      });
			}
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {

			
		}		
	}
}
