package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PFont;
import processing.core.PVector;
import still.data.Operator;
import still.data.TableEvent;
import still.data.TableListener;
import still.data.Table.ColType;
import still.data.TableEvent.TableEventType;
import still.operators.HistogramOp;
import still.operators.PearsonCollectOp;
import still.operators.SplomOp;

public class PHistPainter extends OPApplet implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2888627251625526826L;

	public boolean selectionOn = false;
	public int minHistBound 	= 20;
	public int BIG_BORDER_H = 75;
	public int BIG_BORDER_V = 25;
	public int CELL_SPACING = 40;
	public double[][] plot_bounds = null;
	public int[][] hist_data = null;
	public int[][] sel_hist_data = null;
	public int[] max_y = null;
	public int hist_bins = 20;
	float cell_width = -1.f;
	float cell_height = -1;
	float axis_spacing_h =-1;
	float axis_length_h = -1;
	float axis_spacing_v = -1;
	float axis_length_v = -1;
	
	int selSplom = -1;
	int selSplom_y = -1;
	int selSplom_x = -1;
	boolean inSplom = false;
	public int selectionIndex = -1;
	public int axeslines_h = 4;
	public int axeslines_v = 4;
	public NumberFormat sf = new DecimalFormat("0.#E0");
	public NumberFormat nf = NumberFormat.getInstance();

	public PVector selBoxCoords = null;
	public PVector selBoxDims = null;
	boolean squareLayout = true;	
	
	public Dimension graphSize() {
		
		numerics = getNumerics();
		countNumerics();
		
		if( ! squareLayout ) {
		
			cell_width 	= this.width - CELL_SPACING - 2*BIG_BORDER_H;
			cell_height = (((this.height - (num_numerics-1)*CELL_SPACING) - 2*BIG_BORDER_V) / ((float)num_numerics));
		}
		else {
			
			int hists_across = (int) Math.ceil( Math.sqrt( num_numerics ) );
			int hists_down 	 = (int) Math.ceil( ((double)num_numerics) / ((double)hists_across));			
			cell_width 	= ((this.width - (hists_across-1.f)*CELL_SPACING) - 2*BIG_BORDER_H) / ((float)hists_across);
			cell_height = ((this.height - (hists_down-1.f)*CELL_SPACING) - 2*BIG_BORDER_V) / ((float)hists_down);
		}

		return new Dimension((int)cell_width,(int)cell_height);
	}

	public void setSquareLayout( boolean squareLayout ) {
		
		this.squareLayout = squareLayout;
	}
	
	public boolean isSquareLayout( ) {
		
		return squareLayout;
	}
	
	
	/**
	 * Perfome heavyweight recalculation of sizing terms and 
	 * histogram data
	 */
	public synchronized void heavyResize() {
		
		// calculate cell size
		numerics = getNumerics();
		countNumerics();
		updateSelectionIndex();
		
		if( ! squareLayout ) {
		
			cell_width 	= this.width - CELL_SPACING - 2*BIG_BORDER_H;
			cell_height = (((this.height - (num_numerics-1)*CELL_SPACING) - 2*BIG_BORDER_V) / ((float)num_numerics));
		}
		else {
			
			int hists_across = (int) Math.ceil( Math.sqrt( num_numerics ) );
			int hists_down 	 = (int) Math.ceil( ((double)num_numerics) / ((double)hists_across));			
			cell_width 	= ((this.width - (hists_across-1.f)*CELL_SPACING) - 2*BIG_BORDER_H) / ((float)hists_across);
			cell_height = ((this.height - (hists_down-1.f)*CELL_SPACING) - 2*BIG_BORDER_V) / ((float)hists_down);
		}		

		axis_spacing_h = cell_width/10.f;
		axis_length_h = ((8.f*cell_width)/10.f);
		axis_spacing_v = (cell_height/10.f);
		axis_length_v = ((8.f*cell_height)/10.f);		
		plot_bounds	= new double[num_numerics][2];		
		max_y = new int[num_numerics];
		hist_data	= new int[num_numerics][hist_bins];
		sel_hist_data	= new int[num_numerics][hist_bins];
		
		this.axeslines_h = (int)Math.floor(  axis_length_h / textWidth( "0.##E-0" ) );
		this.axeslines_v = (int)Math.floor(  axis_length_v / ( 2.f*( (float) textAscent() + textDescent() )  ) );
		
		for( int i = 0; i < num_numerics; i++ ) {

			double min_a = Double.POSITIVE_INFINITY, max_a = Double.NEGATIVE_INFINITY;

			for( int k = 0; k < getOp().rows(); k++ ) {
				
				min_a = Math.min(min_a, getOp().getMeasurement(k,numerics.get(i)));
				max_a = Math.max(max_a, getOp().getMeasurement(k,numerics.get(i)));
			}
			
			// maintain the bounds for fast calculation
			
			plot_bounds[i][0] = min_a;
			plot_bounds[i][1] = max_a;
		
			// split into hist_bins
			double hist_bin_width = (plot_bounds[i][1] - plot_bounds[i][0])/((double)hist_bins);
			if( hist_bin_width <= 1.e-8 ) {
				
				hist_bin_width = 1.0;
			}
			
			// tally up the membership into the bins
			max_y[i] = 0;
			for( int k = 0; k < getOp().rows(); k++ ) {

				int bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][0])/hist_bin_width)));							
				hist_data[i][bin_no]++;
				max_y[i] = Math.max(max_y[i], hist_data[i][bin_no]);
				if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
					bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][0])/hist_bin_width)));							
					sel_hist_data[i][bin_no]++;
				}
			}
		}
	}
	
	public void componentResized(ComponentEvent e) {

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
	    		heavyResize();
				redraw();
	        }
	      });
	}

	public void updateSelectionIndex() {
		
		int i = 0;
		
		for( ColType type : this.getOp().getColTypes() ) {
		
			
			if( type == ColType.ATTRIBUTE && this.getOp().getColName(i).equalsIgnoreCase("selection") ) {
				
				selectionIndex = i;
			}			
			i++;
		}
	}
	
	public PHistPainter(Operator op) {
		super(op);		
		
		nf.setMaximumFractionDigits(2);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if( e.getActionCommand().equalsIgnoreCase("square") ) {
			
			setSquareLayout( ((JCheckBox)e.getSource()).isSelected() );
		}

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
	        	heavyResize();
				redraw();
	        }
	      });
	}

	public synchronized void setup() {

		// added by HY
		String sFontFile = "SansSerif-10.vlw";
		if (!(new File(sFontFile)).exists())
			sFontFile = "../data/SansSerif-10.vlw";
		PFont myFont = loadFont(sFontFile);
		//PFont myFont = loadFont( "SansSerif-10.vlw" );
		textFont(myFont,10);

		smooth();
		
		if( this.getOp() instanceof HistogramOp ) {

			// count the number of dimensions
			
			countNumerics();
			int max_sqr_bound = (int)Math.ceil(Math.sqrt((double)num_numerics));
			int span_h = 2*BIG_BORDER_H + (max_sqr_bound * minHistBound) + (max_sqr_bound-1)*CELL_SPACING;
			int span_v = span_h;
			
			// compute the minimum size
			
			size(	Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT));//, P2D);
			this.setPreferredSize(new Dimension(Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT)));
		}
    			
		heavyResize();

		// prevent thread from starving everything else
         noLoop();
         
         this.finished_setup = true;
//	    SwingUtilities.invokeLater(new Runnable() {
//	        public void run() {
//	          // Set the preferred size so that the layout managers can handle it
//	        	invalidate();
//	          getParent().getParent().validate();
//	        }
//	      });
     }
	
	public void nearestHist( ) {
		
		selSplom = -1;
		
		int xComp = mouseX - BIG_BORDER_H;
		int yComp = mouseY - BIG_BORDER_V;
				
		if( isSquareLayout( ) ) {
						
			int hists_across = (int) Math.ceil( Math.sqrt( num_numerics ) );
			int hists_down 	 = (int) Math.ceil( ((double)num_numerics) / ((double)hists_across));			

			selSplom_y = Math.min( hists_down-1, Math.max( 0, yComp ) / ((int)Math.round(cell_height+CELL_SPACING)) );
			selSplom_x = Math.min( hists_across-1, Math.max( 0, xComp ) / ((int)Math.round(cell_width+CELL_SPACING)) );
			selSplom = hists_across * selSplom_y + selSplom_x;
			
			// determine if it is IN the splom
			xComp = xComp % ((int)Math.round(cell_width+CELL_SPACING));
			yComp = yComp % ((int)Math.round(cell_height+CELL_SPACING));
			
			if (( 	xComp > 0 && 
					xComp < ((int)Math.round(cell_width+CELL_SPACING)) && 
					xComp % ((int)Math.round(cell_width+CELL_SPACING)) <= cell_width ) && 
				( 	yComp > 0 && 
					yComp < num_numerics*((int)Math.round(cell_height+CELL_SPACING)) && 
					yComp % ((int)Math.round(cell_height+CELL_SPACING)) <= cell_height )){
				
				inSplom = true;
			} 
			else {
				
				inSplom = false;
			}
		}
		else {
		
			selSplom = Math.min( num_numerics-1, Math.max( 0, yComp ) / ((int)Math.round(cell_height+CELL_SPACING)) );

			// determine if it is IN the splom
	
			if (( 	xComp > 0 && 
					xComp < ((int)Math.round(cell_width+CELL_SPACING)) && 
					xComp % ((int)Math.round(cell_width+CELL_SPACING)) <= cell_width ) && 
				( 	yComp > 0 && 
					yComp < num_numerics*((int)Math.round(cell_height+CELL_SPACING)) && 
					yComp % ((int)Math.round(cell_height+CELL_SPACING)) <= cell_height )){
				
				inSplom = true;
			} 
			else {
				
				inSplom = false;
			}
		}
	}

	
     public synchronized void draw() {
    	 
    	 background(128+64+32);

    	 if( this.getOp() instanceof HistogramOp) {

    		 
    		 if( this.isSquareLayout() ) {
    			 
	    		 
   				// label the axes
   				
   				fill(0);	
   				
//   				//textSize(10);
//   				for( int i = 0; i < num_numerics; i++ ) {
//   					
//   					int x_pos = 0;
//   					int y_pos = 0;
//   					textAlign(RIGHT);
//   					x_pos = BIG_BORDER_H - 10;
//   					y_pos = BIG_BORDER_V + i * (cell_height +CELL_SPACING) + cell_height/2;
//   					text(this.getOp().getColName(numerics.get(i)), x_pos, y_pos );
//   				}
   				
   				pushMatrix();
   	
   				translate( BIG_BORDER_H, BIG_BORDER_V );
   				   				
   				int hists_across = (int) Math.ceil( Math.sqrt( num_numerics ) );
   				int hists_down 	 = (int) Math.ceil( ((double)num_numerics) / ((double)hists_across));			

   				// find the proper locations for everything
   				int h = 0;
   				for( int i = 0; i < hists_down; i++ ) { 		// vertical
   					pushMatrix();
   					for( int j = 0; j < hists_across; j++ ) { 	// horizontal
   					
   						if( numerics.size() == h ) {
   							break;
   						}
   						
   						// draw the title to the histogram
   						
   						this.stroke(0);
   						this.fill(0);
   						this.textAlign(CENTER);
   						text(this.getOp().getColName(numerics.get(h)), cell_width/2.0f, -CELL_SPACING/6.f );
   						
   						// label the horizontal axes
   						
   						for( int k = 0; k <= axeslines_h-1; k++ ) {

   							float offs = 0.f;
   							if( k == 0 ) {
   								textAlign(LEFT);
   								double num = k*(plot_bounds[h][1]-plot_bounds[h][0])/(axeslines_h-1) + plot_bounds[h][0];
   								if(Math.abs(num)>100.){
   									offs = -textWidth( sf.format( num ) )/4f;
   								}
   								else {
   									offs = -textWidth( nf.format( num ) )/4f;
   								}
   							}
   							else if( k == axeslines_h - 1 ) { 
   								textAlign(RIGHT);
   								double num = k*(plot_bounds[h][1]-plot_bounds[h][0])/(axeslines_h-1) + plot_bounds[h][0];
   								if(Math.abs(num)>100.){
   									offs = textWidth( sf.format( num ) )/4f;
   								}
   								else {
   									offs = textWidth( nf.format( num ) )/4f;
   								}
   							}
   							else {
   								textAlign(CENTER);
   							}
   							
   							double num = k*(plot_bounds[h][1]-plot_bounds[h][0])/(axeslines_h-1) + plot_bounds[h][0];
   							String numstr="";
   							if(Math.abs(num)>100){
   								numstr=sf.format( num );
   							}
   							else {
   								numstr=nf.format( num );
   							}
   							text(	numstr, 
   									axis_spacing_h + k*(cell_width-2*axis_spacing_h)/((float)(axeslines_h-1)) + offs, 
   									cell_height+textAscent()+CELL_SPACING/6.f);					
   						}
   						
   						for( int k = 0; k <= axeslines_v-1; k++ ) {

							textAlign(RIGHT);
							double num = (axeslines_v-k-1)*((float)max_y[h])/(axeslines_v-1);
							String numstr = "";
   							if(Math.abs(num)>100){
   								numstr=sf.format( num );
   							}
   							else {
   								numstr=nf.format( num );
   							}
   							text(	numstr, 
   									-CELL_SPACING/6.f, 
   									axis_spacing_v + k*(cell_height-2*axis_spacing_v)/((float)(axeslines_v-1)));					
   						}

   						// draw the white background
	   					
	   					stroke(128);
	   					fill(255);
	   					beginShape();
	   					vertex(0,0);
	   					vertex(cell_width,0);
	   					vertex(cell_width,cell_height);
	   					vertex(0,cell_height);
	   					endShape(CLOSE);
	   	
	   					// draw a histogram of the dimension
	   	
	   					stroke(0);
	   					fill( 64+32 );
	   					for( int k = 0; k < hist_bins; k++ ) {
	   						
	   						rect(	axis_spacing_h + k*((float)axis_length_h/(float)hist_bins),
	   								(axis_spacing_v + axis_length_v)-(hist_data[h][k]*((float)axis_length_v / (float)max_y[h])),
	   								((float)axis_length_h/(float)hist_bins),
	   								hist_data[h][k]*((float)axis_length_v / (float)max_y[h]) );
	   					}
	   					fill( 255,0,0 );
	   					for( int k = 0; k < hist_bins; k++ ) {
	   						
	   						rect(	axis_spacing_h + k*((float)axis_length_h/(float)hist_bins),
	   								(axis_spacing_v + axis_length_v)-(sel_hist_data[h][k]*((float)axis_length_v / (float)max_y[h])),
	   								((float)axis_length_h/(float)hist_bins),
	   								sel_hist_data[h][k]*((float)axis_length_v / (float)max_y[h]) );
	   					}
	   	
	   						// is the selection on?
	   					if( selectionOn && selSplom_x==j && selSplom_y==i ) {
	   						
	   						// draw the slection box
	   						fill(32,32);
	   						rect(selBoxCoords.x,selBoxCoords.y,selBoxDims.x,selBoxDims.y);
	   						
	   					}
	   					h++;
	   					translate( cell_width + CELL_SPACING, 0 );
   					}   					
   					popMatrix();
   					translate( 0, cell_height + CELL_SPACING );
   				}
   				 				
   				popMatrix();
   				
   				// draw the dimension labels
    		 }
    		 else {
	    		 
   				// label the axes
   				
   				fill(0);	
   				
   				//textSize(10);
   				for( int i = 0; i < num_numerics; i++ ) {
   					
   					float x_pos = 0;
   					float y_pos = 0;
   					textAlign(RIGHT);
   					x_pos = BIG_BORDER_H - 10;
   					y_pos = BIG_BORDER_V + i * (cell_height +CELL_SPACING) + cell_height/2.f;
   					text(this.getOp().getColName(numerics.get(i)), x_pos, y_pos );
   					
					// label the axes
					
					for( int k = 0; k <= axeslines_h-1; k++ ) {

						float offs = 0.f;
						if( k == 0 ) {
							textAlign(LEFT);
							offs = -textWidth( sf.format( k*(plot_bounds[i][1]-plot_bounds[i][0])/(axeslines_h-1) + plot_bounds[i][0]) )/4f;
						}
						else if( k == axeslines_h - 1 ) { 
							textAlign(RIGHT);
							offs = textWidth( sf.format( k*(plot_bounds[i][1]-plot_bounds[i][0])/(axeslines_h-1) + plot_bounds[i][0]) )/4f;
						}
						else {
							textAlign(CENTER);
						}
						
						text(	sf.format( k*(plot_bounds[i][1]-plot_bounds[i][0])/(axeslines_h-1) + plot_bounds[i][0]), 
								BIG_BORDER_H + axis_spacing_h + k*(cell_width-2*axis_spacing_h)/((float)(axeslines_h-1)) + offs, 
								BIG_BORDER_V + i * (cell_height +CELL_SPACING) + cell_height+textAscent()+CELL_SPACING/6.f);					
					}
   					
   				}
   				
   				pushMatrix();
   	
   				translate( BIG_BORDER_H, BIG_BORDER_V );
   				
   				
   				// find the proper locations for everything
   				for( int i = 0; i < num_numerics; i++ ) {
   					
   					// draw the white background
   					
   					stroke(128);
   					fill(255);
   					beginShape();
   					vertex(0,0);
   					vertex(cell_width,0);
   					vertex(cell_width,cell_height);
   					vertex(0,cell_height);
   					endShape(CLOSE);
   	
   					// draw a histogram of the dimension
   	
   					stroke(0);
   					fill( 64+32 );
   					for( int k = 0; k < hist_bins; k++ ) {
   						
   						rect(	axis_spacing_h + k*((float)axis_length_h/(float)hist_bins),
   								(axis_spacing_v + axis_length_v)-(hist_data[i][k]*((float)axis_length_v / (float)max_y[i])),
   								((float)axis_length_h/(float)hist_bins),
   								hist_data[i][k]*((float)axis_length_v / (float)max_y[i]) );
   					}
   					fill( 255,0,0 );
   					for( int k = 0; k < hist_bins; k++ ) {
   						
   						rect(	axis_spacing_h + k*((float)axis_length_h/(float)hist_bins),
   								(axis_spacing_v + axis_length_v)-(sel_hist_data[i][k]*((float)axis_length_v / (float)max_y[i])),
   								((float)axis_length_h/(float)hist_bins),
   								sel_hist_data[i][k]*((float)axis_length_v / (float)max_y[i]) );
   					}
   	
   						// is the selection on?
   					if( selectionOn && selSplom==i ) {
   						
   						// draw the slection box
   						fill(32,32);
   						rect(selBoxCoords.x,selBoxCoords.y,selBoxDims.x,selBoxDims.y);
   						
   					}
   					translate( 0, cell_height + CELL_SPACING );
   					 					
   	
   				}
   				 				
   				popMatrix();
   				
   				// draw the dimension labels
      		 }
    	 }
     }

     public void mouseReleased() {
    	 
    	 // are we creating a box?
    	 if( selectionOn ) {
    		 
    		 selectionOn = false;    		 
    		 redraw();
    	 }
     }
     
     public void selectionUpdate() {
    	 
		 // histogram selection routine
		 double R_x = (plot_bounds[selSplom][1] - plot_bounds[selSplom][0]) / (cell_width - (2*axis_spacing_h));
		     		 
		 // scatterplot selection routine
		 double a = Math.min( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom][0];
		 double c = Math.max( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom][0];
		     		 
		 // determine membership
		for( int k = 0; k < getOp().rows(); k++ ) {
			
			boolean test_horz = (float)getOp().getMeasurement(k,numerics.get(selSplom)) >= a;
			test_horz = test_horz && ((float)getOp().getMeasurement(k,numerics.get(selSplom)) <= c);
			if( test_horz ) {
				
				getOp().setMeasurement(k, selectionIndex, 1.0);
			}
			else {
				
				getOp().setMeasurement(k, selectionIndex, 0.0);
			}
		}
		
		getOp().tableChanged(new TableEvent( getOp(), TableEvent.TableEventType.ATTRIBUTE_CHANGED, "selection", null, false), true);
		getOp().tableChanged(new TableEvent( getOp(), TableEvent.TableEventType.ATTRIBUTE_CHANGED, "selection", null, true), true);

		buildSelHist();
		
     }
     
     public void buildSelHist() {
    	 
 		sel_hist_data	= new int[num_numerics][hist_bins];

	   	 // update the selection histograms
	   	 for( int i = 0; i < num_numerics; i++ ) {
				// split into hist_bins
				double hist_bin_width = (plot_bounds[i][1] - plot_bounds[i][0])/((double)hist_bins);
				
				// tally up the membership into the bins
				for( int k = 0; k < getOp().rows(); k++ ) {
	
					if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
						int bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][0])/hist_bin_width)));							
						sel_hist_data[i][bin_no]++;
					}
				}
	   	 }			
     }
     
     public void mouseDragged() {
    	 
    	 // are we creating a box?
    	 if( selectionOn ) {
    		 
    	 	// update the box coordinates (with clipping)
    		 PVector localCoords =  null;
    		 if( isSquareLayout() ) {
    			 
	    		 localCoords =  new PVector(	mouseX - (BIG_BORDER_H + selSplom_x*((int)Math.round(cell_width+CELL_SPACING))) ,
												mouseY - (BIG_BORDER_V + selSplom_y*((int)Math.round(cell_height+CELL_SPACING))) );
    		 }
    		 else {
    			 
	    		 localCoords =  new PVector(	mouseX - BIG_BORDER_H ,
												mouseY - (BIG_BORDER_V + selSplom*((int)Math.round(cell_height+CELL_SPACING))) );
    		 }
 
    		 PVector diffCoords = PVector.sub(localCoords, selBoxCoords );
    		 selBoxDims = new PVector( 	Math.min( cell_width-selBoxCoords.x, Math.max(-selBoxCoords.x, diffCoords.x) ),
    				 					Math.min( cell_height-selBoxCoords.y, Math.max(-selBoxCoords.y, diffCoords.y) ) );
    		 
     	 	// update the selection state of the underlying data
    		 selectionUpdate();
    		 
    		 redraw();
    	 }
     }
     
     public void mouseMoved() {
    	 
    }
     
     public void mousePressed() {
    	 
    	 // find the nearest Splom
    	 nearestHist();
    	 
    	 // are we in the splom region?
    	 if( inSplom ) {
    	 
    		 selectionOn = true;
    		 
    	 	// record the start point in local coordinates
    		 if( isSquareLayout() ) {
    			 
	    		 selBoxCoords = new PVector(	mouseX - (BIG_BORDER_H + selSplom_x*((int)Math.round(cell_width+CELL_SPACING))),
	    				 						mouseY - (BIG_BORDER_V + selSplom_y*((int)Math.round(cell_height+CELL_SPACING))) );
    		 }
    		 else {

    			 selBoxCoords = new PVector(	mouseX - BIG_BORDER_H,
	 											mouseY - (BIG_BORDER_V + selSplom*((int)Math.round(cell_height+CELL_SPACING))) );
    		 }
    		 selBoxDims = new PVector(0,0);
    	 
         	// update the screen (run draw once)
         	redraw();
    	 }
     }

	@Override
	public void stateChanged(ChangeEvent e) {

		if( e.getSource() instanceof JSlider ) {
			
			JSlider source = (JSlider)e.getSource();
			
			if( source.getName().equalsIgnoreCase("slider")) {
			    if (!source.getValueIsAdjusting()) {
			        int val = ((int)source.getValue())*50 + 50;
			        minHistBound 	= val;

			        // count the number of dimensions
					
			        int span_h = 0;
					int span_v = 0;
					
					if( this.isSquareLayout() ) {
						
						int max_sqr_bound = (int)Math.ceil(Math.sqrt((double)num_numerics));
						span_h = 2*BIG_BORDER_H + (max_sqr_bound * minHistBound) + (max_sqr_bound-1)*CELL_SPACING;
						span_v = span_h;
					}
					else {
						
						span_h = 2*BIG_BORDER_H + minHistBound;
						span_v = 2*BIG_BORDER_V + (num_numerics * minHistBound) + (num_numerics-1)*CELL_SPACING;
					}
					
					// compute the minimum size
					size(span_h, span_v);

					SwingUtilities.invokeLater(new Runnable() {
				        public void run() {
				        	invalidate();
				          // Set the preferred size so that the layout managers can handle it
				          getParent().validate();
							heavyResize();
							redraw();
				        }
				      });
			    }
			    
			}
			else if (source.getName().equalsIgnoreCase("hist_slider")) {
			    if (!source.getValueIsAdjusting()) {

			    	hist_bins = ((int)source.getValue())+20;
			    	
					SwingUtilities.invokeLater(new Runnable() {
				        public void run() {
					    	heavyResize();
							redraw();
				        }
				      });
			    }
			}
		}
	}

}
