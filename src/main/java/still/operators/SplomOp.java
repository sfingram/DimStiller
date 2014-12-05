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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PApplet;
import processing.core.PGraphics;

import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.Table.ColType;
import still.data.TableEvent.TableEventType;
import still.gui.OPAppletViewFrame;
import still.gui.OperatorView;
import still.gui.PLODSplomPainter;
import still.gui.PMatrixPainter;
import still.gui.PSplomPainter;
import still.operators.CutoffOp.CutoffFunction;
import still.operators.CutoffOp.CutoffView;

public class SplomOp extends Operator implements Serializable {

	int selectionCol 	= -1;
	boolean hasSelOp 	= false;
	double[][] selCol 	= null;
	
	public static String getMenuName() {
		
		return "View:SPLOM";
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
		
		return "[View:SPLOM]";
	}
	
	public String getSaveString( ) {
		
		return "";
	}
	
	public SplomOp( Table newTable, boolean isActive, String paramString ) {
		
		this( newTable, isActive );
	}
	
	public SplomOp( Table newTable, boolean isActive ) {
		
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
			setView( new SplomView( this ) );
		}
	}
	
	public void activate() {
		
		this.isActive = true;
		
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
		setView( new SplomView( this ) );
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

	public void tableChanged( TableEvent te ) {
		
		super.tableChanged(te);
		if( this.isActive() ) {
			SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	
	        	
	        	PLODSplomPainter plod = ((PLODSplomPainter)((OPAppletViewFrame)((SplomView)getView()).getViewFrame()).procApp); 
	    		Dimension d = plod.graphSize();
	    		
	    		// determine if there is a need for calculating the correlation matrix
	    		if( Math.min( d.getHeight(), d.getWidth() ) <= plod.LOD_cell_cutoff ) {
	    			
	    			plod.calcCorrelationMatrix();
	    		}
	    		
	    		// change the hilight or color status of the underlying visualization
	    		plod.redraw();
	        }
			});
		}
	}
//	public void attributeChanged( String attribute, int[] indices, boolean isUpstream  ) {
//
//		super.attributeChanged(attribute, indices, isUpstream);
//		
//		SwingUtilities.invokeLater(new Runnable() {
//	        public void run() {
//	    		// change the hilight or color status of the underlying visualization
//	    		((OPAppletViewFrame)((SplomView)getView()).getViewFrame()).procApp.redraw();
//	        }
//	      });
//		
//		
//	}

	public void loadOperatorView() {
		
		setView( new SplomView( this ) );
	}

	public class SplomView extends OperatorView implements ChangeListener {

		JSlider slider = null;
		JSlider hist_slider = null;
		JSlider size_slider = null;
		PLODSplomPainter psp = null;
		
		public SplomView(Operator o) {
			
			super(o);

			psp = new PLODSplomPainter(o);
			vframe = new OPAppletViewFrame("E"+o.getExpressionNumber()+":"+o, psp );			
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
			slider.addChangeListener(psp);			
			slider.setEnabled(false);
			//slider_panel.add(slider,"Center");
			//slider_panel.add(new JLabel("Min Plot Size"), "West");
			
			this.add(slider_panel,"Center");
			
//			JPanel hist_panel = new JPanel();
//			hist_panel.setLayout(new BorderLayout(5,5));
//			
//			Hashtable<Integer,JLabel> labelTableHist = new Hashtable<Integer,JLabel>();
//			for( int i = 20; i <= 100; i += 10) {				
//				labelTableHist.put(new Integer((i-20)),new JLabel(""+i));
//			}
//			hist_slider = new JSlider( 	JSlider.HORIZONTAL, 
//									0, 
//									80, 
//									0);
//			hist_slider.setName("hist_slider");
//			hist_slider.setMajorTickSpacing( 10 );
//			hist_slider.setMinorTickSpacing( 1 );
//			hist_slider.setLabelTable(labelTableHist);
//			hist_slider.setPaintLabels(true);	
//			
//			hist_slider.addChangeListener(this);			
//			hist_slider.addChangeListener(psp);			
//			hist_panel.add(hist_slider,"Center");
//			hist_panel.add(new JLabel("Hist Bins"), "West");
//
//			this.add(hist_panel,"South");
			
			JPanel size_panel = new JPanel();
			size_panel.setLayout(new BorderLayout(5,5));
			
			Hashtable<Integer,JLabel> labelTableSize = new Hashtable<Integer,JLabel>();
			for( int i = 1; i <= 15; i++) {				
				labelTableSize.put(new Integer(i),new JLabel(""+i));
			}
			size_slider = new JSlider( 	JSlider.HORIZONTAL, 
									1, 
									15, 
									1);
			size_slider.setName("size_slider");
			size_slider.setMajorTickSpacing( 2 );
			size_slider.setMinorTickSpacing( 1 );
			size_slider.setLabelTable(labelTableSize);
			size_slider.setPaintLabels(true);	
			
			size_slider.addChangeListener(this);			
			size_slider.addChangeListener(psp);			
			size_panel.add(size_slider,"Center");
			size_panel.add(new JLabel("Point Size"), "West");
			JPanel cbox_panel = new JPanel();
			cbox_panel.setLayout(new GridLayout(2,1));
			JCheckBox bubble_button = new JCheckBox( "Bubble Plot" );
			JCheckBox freeze_button = new JCheckBox( "Freeze Axes" );
			cbox_panel.add(bubble_button);
			cbox_panel.add(freeze_button);
			size_panel.add( cbox_panel, "East" );
			bubble_button.setActionCommand("BUBBLE");
			bubble_button.addActionListener(this);
			freeze_button.setActionCommand("FREEZE");
			freeze_button.addActionListener(this);
			
			this.add(size_panel,"Center");
			this.setBorder(	BorderFactory.createEmptyBorder(10, 10, 10, 10));
			
			JButton cull_but = new JButton("Cull Highlighted");
			JButton cull_non_but = new JButton("Cull NonHighlighted");
			cull_but.setActionCommand("CULLHIGH");
			cull_non_but.setActionCommand("CULLNONHIGH");
			cull_but.addActionListener(this);
			cull_non_but.addActionListener(this);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout( new GridLayout(1,2) );
			buttonPanel.add( cull_but );
			buttonPanel.add( cull_non_but );
			JCheckBox scrollable_button = new JCheckBox("Manual Plot Size");
			scrollable_button.setActionCommand("scroll");
			scrollable_button.addActionListener(this);
			JPanel sizingPanel = new JPanel();
			sizingPanel.setLayout( new BorderLayout() );
			sizingPanel.add(scrollable_button,BorderLayout.WEST);
			sizingPanel.add(slider,BorderLayout.CENTER);
			this.add( buttonPanel, "North" );
			this.add( sizingPanel, "South" );
		}
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 8557769161790649887L;
		
		public void actionPerformed(ActionEvent e) {

			if(e.getActionCommand().equalsIgnoreCase("BUBBLE") ) {
				
				psp.setBubblePlot( ((JCheckBox)e.getSource()).isSelected() );
			}			
			if(e.getActionCommand().equalsIgnoreCase("FREEZE") ) {
				
				psp.setFreezeAxes( ((JCheckBox)e.getSource()).isSelected() );
			}			
			if(e.getActionCommand().equalsIgnoreCase("scroll") ) {
				
				((OPAppletViewFrame)this.vframe).setScroll(!((OPAppletViewFrame)this.vframe).hasScroll);
				slider.setEnabled(((JCheckBox)e.getSource()).isSelected() );
				SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			          // Set the preferred size so that the layout managers can handle it
			        	psp.invalidate();			        	
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height+1));
			        	vframe.setSize(new Dimension(vframe.getSize().width,vframe.getSize().height-1));
						psp.heavyResize();
						psp.redraw();
			        }
			      });
			}
			if( e.getActionCommand().equalsIgnoreCase("CULLHIGH") ) {
				
				String paramString = "";
				PLODSplomPainter psp = ((PLODSplomPainter)((OPAppletViewFrame)vframe).procApp);
				for( int i = 0; i < psp.numerics.size(); i++ ) {
					
					if( psp.cull_hilight[i] ) {
						
						paramString += this.operator.input.getColName(psp.numerics.get(i));
						paramString += ",";
					}
				}
				if( paramString.length() > 0 ) {
					
					paramString = paramString.substring(0,paramString.length()-1);
				}
				CullByNameOp cbnOp = new CullByNameOp( this.operator.input, true, paramString );
				this.operator.tableChanged( new TableEvent( this.operator, TableEventType.ADD_ME, cbnOp), true);
			}
			if( e.getActionCommand().equalsIgnoreCase("CULLNONHIGH") ) {
				
				String paramString = "";
				PLODSplomPainter psp = ((PLODSplomPainter)((OPAppletViewFrame)vframe).procApp);
				for( int i = 0; i < psp.numerics.size(); i++ ) {
					
					if( !psp.cull_hilight[i] ) {
						
						paramString += this.operator.input.getColName(psp.numerics.get(i));
						paramString += ",";
					}
				}
				if( paramString.length() > 0 ) {
					
					paramString = paramString.substring(0,paramString.length()-1);
				}
				CullByNameOp cbnOp = new CullByNameOp( this.operator.input, true, paramString );
				this.operator.tableChanged( new TableEvent( this.operator, TableEventType.ADD_ME, cbnOp), true);
			}
		}

		@Override
		public void stateChanged(ChangeEvent e) {

			
		}
		
	}
}
