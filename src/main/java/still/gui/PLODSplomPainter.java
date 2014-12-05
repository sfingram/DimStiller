package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PFont;
import processing.core.PVector;
import still.data.Binner2D;
import still.data.FloatIndexer;
import still.data.Operator;
import still.data.TableEvent;
import still.data.Table.ColType;
import still.operators.SplomOp;

public class PLODSplomPainter extends OPApplet implements ChangeListener{

	public boolean [] cull_hilight = null;
	public double[][] r = null;
	public int LOD_cell_cutoff = 50; 
	public boolean isSPLOM = true;
	public boolean hasColorCol = false;
	public int colorCol = 0;
	public int point_size = 4;
	public boolean selectionOn = false;
	public int minSplomBound 	= 20;
	public int BIG_BORDER_H_L = 75;
	public int LABEL_SPACING_H = 5;
	public int BIG_BORDER_H_R = 25;
	public int BIG_BORDER_V_TOP = 10;//25;
	public int BIG_BORDER_V_BOT = 40;
	public int CELL_SPACING = 5; 
	public double[][][] plot_bounds = null;
	public int[][] hist_data = null;
	public int[][] sel_hist_data = null;
	public int[] max_y = null;
	public int hist_bins = 20;
	int cell_width = -1;
	int cell_height = -1;
	int axis_spacing_h =-1;
	int axis_length_h = -1;
	int axis_spacing_v = -1;
	int axis_length_v = -1;
	int[] selSplom = new int[2];
	boolean inSplom = false;
	public int selectionIndex = -1;
	public int axeslines = 4;
	public NumberFormat nf = null;	
	public NumberFormat sf = new DecimalFormat("0.#E0");
	public PVector selBoxCoords = null;
	public PVector selBoxDims = null;
	public boolean bubblePlot = false;
	public boolean freezeAxes = false;
	public Binner2D[][] b2ds = null;
	public double max_bubble_size = -1;
	public int max_bubble_count = -1;
	
	boolean drawHistograms = false;
	
	/**
	 * Compute the bubbles for the bubble scatterplot
	 */
	public void genBubbles( ) {
		
		if( !bubblePlot ) {
			
			return;
		}
		
		b2ds = new Binner2D[num_numerics][num_numerics];
		
		for( int i = 0; i < num_numerics; i++) {
			
			for( int j = i+1; j < num_numerics; j++ ) {
				
				if( ! hasColorCol ) {
					
					b2ds[i][j] = new Binner2D( op, numerics.get(i), numerics.get(j) );
				}
				else {
					
					b2ds[i][j] = new Binner2D( op, numerics.get(i), numerics.get(j), colorCol );
				}
			}
		}
		
		// calculate the maximum bubble dimensions
		
		max_bubble_size = Double.MAX_VALUE;
		max_bubble_count = -1;
		for( int i = 0; i < num_numerics; i++ ) {
			
			for( int j = i+1; j < num_numerics; j++ ) {
				
				max_bubble_size = Math.min( max_bubble_size, (cell_width-2*axis_spacing_h) / ((double)this.b2ds[i][j].bin1.getBinCount()) );
				max_bubble_size = Math.min( max_bubble_size, (cell_height-2*axis_spacing_v) / ((double)this.b2ds[i][j].bin2.getBinCount()) );				
//				System.out.println("" + ((double)this.b2ds[i][j].bin1.getBinCount()) + " and " + ((double)this.b2ds[i][j].bin2.getBinCount()) );
				max_bubble_count = Math.max(max_bubble_count, this.b2ds[i][j].getMax2DBin());
			}
		}		
		
//		max_bubble_size /= 2.;
//		System.out.println("" + (cell_width-2*axis_spacing_h) + " and " + (cell_height-2*axis_spacing_v) );
//		System.out.println("max_bubble_size = " + max_bubble_size);

	}
	
	/**
	 * Set whether to draw a bubble scatterplot
	 * 
	 * @param bubblePlot
	 */
	public void setFreezeAxes( boolean freezeAxes ) {

		this.freezeAxes = freezeAxes;
		if( ! freezeAxes ) {
			
    		heavyResize();
			redraw();
		}
	}
	
	/**
	 * Set whether to draw a bubble scatterplot
	 * 
	 * @param bubblePlot
	 */
	public void setBubblePlot( boolean bubblePlot ) {
		
		this.bubblePlot = bubblePlot;
		if( bubblePlot ) {
			
			genBubbles();
		}
		
		redraw();
	}
	
	public Dimension graphSize() {
		
		// calculate cell size
		numerics = getNumerics();
		countNumerics();
		
		int label_loop_bound = num_numerics;
		if( !drawHistograms ) {
			
			label_loop_bound--;
		}
		float max_title_width = 0;
		for( int i = 0; i < label_loop_bound; i++ ) {

			max_title_width = Math.max( textWidth( this.getOp().input.getColName(numerics.get(i)) ), max_title_width );
		}
		//BIG_BORDER_H_L = (int)max_title_width+3*LABEL_SPACING_H+(int)textWidth( "0.##E0" );
		BIG_BORDER_H_L = (int)textWidth( "0.##E0" )+3*LABEL_SPACING_H+(int)textWidth( "0.##E0" );
		
		if( drawHistograms ) {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (double)num_numerics);
			cell_height = (int)Math.round(((this.height - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT)) / (double)num_numerics);
		}
		else {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (num_numerics-1.));
			cell_height = (int)Math.round(((this.height - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT)) / (num_numerics-1.));
		}

		return new Dimension((int)cell_width,(int)cell_height);
	}

	/**
	 * Perfome heavyweight recalculation of sizing terms and 
	 * histogram data
	 */
	public synchronized void heavyResize() {
		
		int old_numerics = this.num_numerics;
		
		// calculate cell size
		numerics = getNumerics();
		countNumerics();
		updateSelectionIndex();
				
		// update the cull hilighting
		
		if( cull_hilight == null ||  
			cull_hilight.length != num_numerics || 
			num_numerics != old_numerics ) {
			
			cull_hilight = new boolean[num_numerics];
		}
		
		int label_loop_bound = num_numerics;
		if( !drawHistograms ) {
			
			label_loop_bound--;
		}
		float max_title_width = 0;
		for( int i = 0; i < label_loop_bound; i++ ) {

			max_title_width = Math.max( textWidth( this.getOp().input.getColName(numerics.get(i)) ), max_title_width );
		}
		//BIG_BORDER_H_L = (int)max_title_width+3*LABEL_SPACING_H+(int)textWidth( "0.##E0" );
		BIG_BORDER_H_L = (int)textWidth( "0.##E0" )+3*LABEL_SPACING_H+(int)textWidth( "0.##E0" );
		
		if( drawHistograms ) {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (double)num_numerics);
			cell_height = (int)Math.round(((this.height - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT)) / (double)num_numerics);
		}
		else {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (num_numerics-1.));
			cell_height = (int)Math.round(((this.height - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT)) / (num_numerics-1.));
		}
				
		// determine if we will plot a splom or a bom
		
		this.isSPLOM = ( Math.min(cell_width,cell_height) > this.LOD_cell_cutoff );
		
		axis_spacing_h = (int)Math.round(cell_width/10.);
		axis_length_h = (int)Math.round((8.*cell_width)/10.);
		axis_spacing_v = (int)Math.round(cell_height/10.);
		axis_length_v = (int)Math.round((8.*cell_height)/10.);	
		
		boolean doFreezeAxes = false;
		
		if(!( freezeAxes && plot_bounds != null && plot_bounds.length == num_numerics )) {
			
			plot_bounds	= new double[num_numerics][num_numerics][4];
		}
		else {
			
			doFreezeAxes = true;
		}
		
		this.axeslines = (int)Math.floor(  axis_length_h / textWidth( "0.##E0" ) );
		
		// if we're bubbling, then bubble on
		
		genBubbles();
		

		max_y = new int[num_numerics];
		hist_data	= new int[num_numerics][hist_bins];
		sel_hist_data	= new int[num_numerics][hist_bins];
		
		if( ! doFreezeAxes ) {
			
			for( int i = 0; i < num_numerics; i++ ) {
					
				for( int j = i; j < num_numerics; j++ ) {
					
					double min_a = Double.POSITIVE_INFINITY, max_a = Double.NEGATIVE_INFINITY;
					double min_b = Double.POSITIVE_INFINITY, max_b = Double.NEGATIVE_INFINITY;
					for( int k = 0; k < getOp().rows(); k++ ) {
						
						min_a = Math.min(min_a, getOp().getMeasurement(k,numerics.get(i)));
						min_b = Math.min(min_b, getOp().getMeasurement(k,numerics.get(j)));
						max_a = Math.max(max_a, getOp().getMeasurement(k,numerics.get(i)));
						max_b = Math.max(max_b, getOp().getMeasurement(k,numerics.get(j)));
					}
					
					// maintain the bounds for fast calculation
					
					plot_bounds[i][j][0] = min_a;
					plot_bounds[i][j][1] = min_b;
					plot_bounds[i][j][2] = max_a;
					plot_bounds[i][j][3] = max_b;
					plot_bounds[j][i][0] = min_b;
					plot_bounds[j][i][1] = min_a;
					plot_bounds[j][i][2] = max_b;
					plot_bounds[j][i][3] = max_a;
					
					if( i == j  && drawHistograms) {
						// split into hist_bins
						double hist_bin_width = (plot_bounds[i][j][2] - plot_bounds[i][j][0])/((double)hist_bins);
						
						// tally up the membership into the bins
						max_y[i] = 0;
						for( int k = 0; k < getOp().rows(); k++ ) {
	
							int bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][j][0])/hist_bin_width)));							
							hist_data[i][bin_no]++;
							max_y[i] = Math.max(max_y[i], hist_data[i][bin_no]);
							if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
								bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][i][0])/hist_bin_width)));							
								sel_hist_data[i][bin_no]++;
							}
						}
					}
				}
			}
		}
	}
	
	public void componentResized(ComponentEvent e) {

//		System.out.println(""+e.getSource().getClass().getName());
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
	
	public void updateColorCol ( ) {
		
		this.hasColorCol = false;
		
		// check to see if there is an update to the color columns
		for( int i : getOp().getDimTypeCols(ColType.ATTRIBUTE) ) {
		
			if( getOp().getColName( i ).equalsIgnoreCase("color") ) {
				
				this.hasColorCol = true;
				this.colorCol = i;
			}
		}
	}
	
	public PLODSplomPainter(Operator op) {
		super(op);		

		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);
		
		updateColorCol();
		calcCorrelationMatrix();
	}

	public void calcCorrelationMatrix() {
		
		double[] sums 	= new double[op.columns()];
		double[] sqSums	= new double[op.columns()];
		double[] var	= new double[op.columns()];
		for( int i = 0; i < op.rows(); i++ ) {
			for( int j = 0; j < op.columns(); j++ ) {
								
				sums[j] 	+= 	op.getMeasurement(i, j);
				sqSums[j]	+= 	op.getMeasurement(i, j) * 
								op.getMeasurement(i, j);				
			}
		}
		int num_numeric = op.columns();
		for( int j = 0; j < op.columns(); j++ ) {

			if( op.getColType(j) != ColType.NUMERIC ) {
				sums[j] = -1;
				sqSums[j] = -1;
				var[j] = -1;
				num_numeric--;
			}
			else {
				sums[j] 	= sums[j]	/((double) op.rows());
				sqSums[j] 	= sqSums[j]	/((double) op.rows());
				var[j] = sqSums[j] - (sums[j]*sums[j]);
			}
		}
		
		// compute the d^2 correlation matrix
		r = new double[num_numeric][num_numeric];
		int k = 0;
		int kk = 0;
		for( int i = 0; i < num_numeric; i++ ) {
			
			while(op.getColType(k) != ColType.NUMERIC )
				k++;
			
			kk = 0;
			for( int j = 0; j <= i; j++ ) {
				
				while(op.getColType(kk) != ColType.NUMERIC )
					kk++;
				
				// compute 1's on the diagonal
				
				if( i == j ) {
					
					r[i][j] = 1.0;
					break;
				}
				
				// compute pearson's correlation coefficient
				double sumNum = 0.0;
				for( int ind = 0; ind < op.rows(); ind ++ ) {
					
					sumNum += (op.getMeasurement(ind, k)-sums[k]) * 
							  (op.getMeasurement(ind, kk)-sums[kk]); 
				}
				r[i][j] = sumNum / ((op.rows()-1)*Math.sqrt(var[kk])*Math.sqrt(var[k]));
				r[j][i] = r[i][j];
				kk++;
			}
			k++;
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		updateColorCol();

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
	        	heavyResize();
				redraw();
	        }
	      });
	}

	public void setup() {
		
		// added by HY
		String sFontFile = "SansSerif-10.vlw";
		if (!(new File(sFontFile)).exists())
			sFontFile = "../data/SansSerif-10.vlw";
		PFont myFont = loadFont(sFontFile);
		//PFont myFont = loadFont( "SansSerif-10.vlw" );
		
		textFont(myFont,10);

		//smooth();
		
		if( this.getOp() instanceof SplomOp ) {

			// count the number of dimensions
			
			countNumerics();
			int span_h = (BIG_BORDER_H_L+BIG_BORDER_H_R) + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
			int span_v = (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT) + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
			if ( !drawHistograms ) {
				span_h = (BIG_BORDER_H_L+BIG_BORDER_H_R) + (num_numerics-1 * minSplomBound) + (num_numerics-2)*CELL_SPACING;
				span_v = (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT) + (num_numerics-1 * minSplomBound) + (num_numerics-2)*CELL_SPACING;
			}
			
			// compute the minimum size
			
			size(	Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT));//,P2D);
			this.setPreferredSize(new Dimension(Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT)));
		}
    			
		heavyResize();
		
		// prevent thread from starving everything else
         noLoop();

         this.finished_setup = true;
         
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
	        	invalidate();
	          getParent().validate();
	        }
	      });
     }
	
	public void nearestSplom( ) {
		
		selSplom = new int[2];
		
		int xComp = mouseX - BIG_BORDER_H_L;
		int yComp = mouseY - BIG_BORDER_V_TOP;
				
		selSplom[0] = Math.min( num_numerics-1, Math.max( 0, xComp ) / (cell_width+CELL_SPACING)  );
		selSplom[1] = Math.min( num_numerics-1, Math.max( 0, yComp ) / (cell_height+CELL_SPACING) );

		if( ! drawHistograms ) {
			
			selSplom[0] = Math.min( num_numerics-2, selSplom[0]);
			selSplom[1] = Math.min( num_numerics-2, selSplom[1]);
		}		

		//selSplom[0] = ((num_numerics - 1) - selSplom[0]);
		
		
		// determine if it is IN the splom

		int inside_check_bound_h = num_numerics;
		int inside_check_bound_v = 1;
		if( !drawHistograms ) {
			
			inside_check_bound_h = num_numerics - 1;
			inside_check_bound_v = 0;
		}
		if (( 	xComp > 0 && 
				xComp < (selSplom[1]+1)*(cell_width+CELL_SPACING) && 
				xComp % (cell_width+CELL_SPACING) <= cell_width ) && 
			( 	yComp > 0 && 
				//yComp < ((num_numerics-1) - selSplom[0])*(cell_height+CELL_SPACING) && 
				yComp % (cell_height+CELL_SPACING) <= cell_height ) &&
				(yComp < (cell_height+CELL_SPACING)*(num_numerics-1)) ){
			
			inSplom = true;
		} 
		else {
			
			inSplom = false;
		}
		
//		System.out.println("("+selSplom[0]+","+selSplom[1]+") "+inSplom);
	}

	
     public synchronized void draw() {

    	 background(128+64+32);

    	 while( this.getOp().isUpdating() ) {
    		 
    		 try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	 
    	 if( this.getOp() instanceof SplomOp) {

    		// draw the cull filling
    		 
    		 if(cull_hilight == null || cull_hilight.length != num_numerics ) {
    		
    			 this.heavyResize();
    		 }
    		 
    		noStroke();
    		fill(255,255,0);
 			for( int i = 0; i < num_numerics; i++ ) {

 				// horizontal
 				if( i > 0 ) {
 					
 	 				if(cull_hilight[i]) {
 	 					
 	 					rect(	BIG_BORDER_H_L - ((float)CELL_SPACING)/2.f,
 	 							BIG_BORDER_V_TOP + (i-1) * ((float)(cell_height+CELL_SPACING)) - ((float)CELL_SPACING)/2.f,
 	 							(num_numerics-1) * ((float)(cell_width+CELL_SPACING)),
 	 							((float)(cell_height+CELL_SPACING)));
 	 				}
 				}
 				
 				//vertical
 				if( i < num_numerics-1 ) {
 					
 	 				if(cull_hilight[i]) {
 	 					
 	 					rect(	BIG_BORDER_H_L + i * ((float)(cell_width+CELL_SPACING)) - ((float)CELL_SPACING)/2.f,
 	 							BIG_BORDER_V_TOP - ((float)CELL_SPACING)/2.f,
 	 							((float)(cell_width+CELL_SPACING)),
 	 							(num_numerics-1) * ((float)(cell_height+CELL_SPACING)));
 	 				}
 				}
 			}    		 
    		 
			// label the axes
			
			fill(0);	
			
			//textSize(10);
			int label_loop_bound = num_numerics;
			if( !drawHistograms ) {
				
				label_loop_bound--;
			}

			for( int i = 0; i < label_loop_bound; i++ ) {
				
				textAlign(LEFT);
				float x_pos = (float)BIG_BORDER_H_L + i * ((float)(cell_width + CELL_SPACING)) + (float)cell_width/4.f;
				//float y_pos = (3.f*BIG_BORDER_V)/4.f + i * ((float)(cell_height+CELL_SPACING));
				float y_pos = BIG_BORDER_V_TOP + (label_loop_bound) * ((float)(cell_height+CELL_SPACING)) + 2.5f*textAscent();
				text(this.getOp().input.getColName(numerics.get(i)), (float)x_pos, (float)y_pos );

//				float labelTextWidth = this.textWidth(this.getOp().input.getColName(numerics.get(i)));				
//				rect( x_pos, y_pos-textAscent(), labelTextWidth, textAscent()+textDescent() );

				if( isSPLOM	) {

					if( bubblePlot ) {
						
						// draw the axes labels
						x_pos = (float)BIG_BORDER_H_L + i * ((float)(cell_width + CELL_SPACING)) + axis_spacing_h;
						y_pos = BIG_BORDER_V_TOP + textAscent() + label_loop_bound * ((float)(cell_height+CELL_SPACING));
						String[] alabels = b2ds[i][i+1].bin1.getBinStrings();
						for( int k = 0; k < alabels.length; k++ ) {

							String numstr = alabels[k];
							float offs = 0.f;
							if( k == 0 ) {
								textAlign(LEFT);
								offs = -textWidth( numstr )/4f;
							}
							else if( k == alabels.length - 1 ) { 
								textAlign(RIGHT);
								offs = textWidth( numstr )/4f;
							}
							else {
								textAlign(CENTER);
							}
							
							text(	numstr, 
									x_pos + k*(cell_width-2*axis_spacing_h)/((float)(alabels.length-1)) + offs, 
									y_pos);		
						}
					}
					else { 
						
						// draw the axes labels
						x_pos = (float)BIG_BORDER_H_L + i * ((float)(cell_width + CELL_SPACING)) + axis_spacing_h;
						y_pos = BIG_BORDER_V_TOP + textAscent() + label_loop_bound * ((float)(cell_height+CELL_SPACING));
						for( int k = 0; k <= axeslines-1; k++ ) {

							double num = k*(plot_bounds[i][0][2]-plot_bounds[i][0][0])/(axeslines-1) + plot_bounds[i][0][0];
							String numstr = null;
							if( Math.abs(num) > 100.) {
								
								numstr = sf.format( num );
							}
							else {
								
								numstr = nf.format( num );
							}
							float offs = 0.f;
							if( k == 0 ) {
								textAlign(LEFT);
								offs = -textWidth( numstr )/4f;
							}
							else if( k == axeslines - 1 ) { 
								textAlign(RIGHT);
								offs = textWidth( numstr )/4f;
							}
							else {
								textAlign(CENTER);
							}
							
							text(	numstr, 
									x_pos + k*(cell_width-2*axis_spacing_h)/((float)(axeslines-1)) + offs, 
									y_pos);	
						}
					}
				}
				

				
				pushMatrix();
				textAlign(CENTER);
				x_pos = BIG_BORDER_H_L - (2*LABEL_SPACING_H+(int)textWidth( "0.##E0" ));
				y_pos = textAscent()/2.f + BIG_BORDER_V_TOP + i * ((float)(cell_height+CELL_SPACING)) + cell_height/2.f;
				rotate(-this.PI/2.f);
				translate(-y_pos,x_pos);
				//text(this.getOp().input.getColName(numerics.get(i+1)), (float)x_pos, (float)y_pos );
				text(this.getOp().input.getColName(numerics.get(i+1)), 0.f, 0.f );
				popMatrix();
				textAlign(RIGHT);
				
				if( isSPLOM ) {
					
					// draw the axes labels
					
					if( bubblePlot ) {
						
						x_pos += (LABEL_SPACING_H+(int)textWidth( "0.##E0" ));
						String[] alabels = b2ds[0][i+1].bin2.getBinStrings();
						for( int k = 0; k < alabels.length; k++ ) {
							
							text(	alabels[(alabels.length-1)-k], 
									(float)x_pos, 
									textAscent()/2.f + BIG_BORDER_V_TOP + i * ((float)(cell_height+CELL_SPACING)) + axis_spacing_v + k*(cell_height-2*axis_spacing_v)/((float)alabels.length-1) );					
						}
					}
					else {
						
						x_pos += (LABEL_SPACING_H+(int)textWidth( "0.##E0" ));
						for( int k = 0; k <= axeslines; k++ ) {
							
							double num = (axeslines-k)*(plot_bounds[i+1][0][2]-plot_bounds[i+1][0][0])/axeslines + plot_bounds[i+1][0][0];
							String numstr = null;
							if( Math.abs(num) > 100.) {
								
								numstr = sf.format( num );
							}
							else {
								
								numstr = nf.format( num );
							}
							text(	numstr, 
									(float)x_pos, 
									textAscent()/2.f + BIG_BORDER_V_TOP + i * ((float)(cell_height+CELL_SPACING)) + axis_spacing_v + k*(cell_height-2*axis_spacing_v)/((float)axeslines) );					
						}
					}
				}
			}
			
			pushMatrix();

			translate( BIG_BORDER_H_L, BIG_BORDER_V_TOP );
			
			
			if( drawHistograms ) {
				
//				// find the proper locations for everything
//				for( int i = num_numerics-1; i >= 0; i-- ) {
//					
//					pushMatrix();
//					
//					for( int j = 0; j <= i; j++ ) {
//						
//						// draw the white background
//						
//						stroke(128);
//						fill(255);
//						beginShape();
//						vertex(0,0);
//						vertex(cell_width,0);
//						vertex(cell_width,cell_height);
//						vertex(0,cell_height);
//						endShape(CLOSE);
//						
//						if( i != j ) {
//							
//							// draw the axes
//													
//							stroke(0);
////							line( axis_spacing_h, axis_spacing_v + axis_length_v, axis_spacing_h + axis_length_h, axis_spacing_v + axis_length_v );
////							line( axis_spacing_h, axis_spacing_v, axis_spacing_h, axis_spacing_v + axis_length_v );
//													
//							// scale the transformation matrix to the appropriate bounds						
//							pushMatrix();
//													
//							// scale and translate (or is it the other way around?)
//							translate(axis_spacing_h, axis_spacing_v);
//							scale( 	(float)(axis_length_h/(plot_bounds[i][j][2] - plot_bounds[i][j][0])), 
//									-(float)(axis_length_v/(plot_bounds[i][j][3] - plot_bounds[i][j][1])) );
//							translate((float)plot_bounds[i][j][0],-(float)plot_bounds[i][j][3]);
//							
//							// pointsize scaling
//				    		 double R_x = (plot_bounds[i][j][2] - plot_bounds[i][j][0]) / (cell_width - (2*axis_spacing_h));
//				    		 double R_y = (plot_bounds[i][j][3] - plot_bounds[i][j][1]) / (cell_height - (2*axis_spacing_v));
//							float pointsize_x = (float)(point_size * R_x);
//							float pointsize_y = (float)(point_size * R_y);
//							
//							
//							stroke(0);
//							
//							// draw the plain points
//
////							stroke(128);
//							noStroke();							
//							fill(128);
//							//this.strokeWeight(arg0);
//							for( int k = 0; k < getOp().rows(); k++ ) {
//							
//								if( hasColorCol ) {
//								
//									fill( (int) getOp().getMeasurement(k, colorCol ) );
//								}
//								
//								if( getOp().getMeasurement(k, selectionIndex) < 1e-5 ) {
//									ellipse( (float)getOp().getMeasurement(k,numerics.get(i)),  
//											(float)getOp().getMeasurement(k,numerics.get(j)), 
//											pointsize_x, 
//											pointsize_y);
//								}
//							}
//							fill(255,0,0);
//							//this.strokeWeight(arg0);
//							beginShape(POINTS);
//							for( int k = 0; k < getOp().rows(); k++ ) {
//							
//								if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
//									ellipse( (float)getOp().getMeasurement(k,numerics.get(i)),  
//											(float)getOp().getMeasurement(k,numerics.get(j)), 
//											pointsize_x, 
//											pointsize_y);
//								}
//							}
//							endShape();
//
//							stroke(0);
//							
//							popMatrix();
//							
//						}
//						else {
//							
//							// draw a histogram of the dimension
//
//							stroke(0);
//							fill( 64+32 );
//							for( int k = 0; k < hist_bins; k++ ) {
//								
//								rect(	axis_spacing_h + k*Math.round((float)axis_length_h/(float)hist_bins),
//										(axis_spacing_v + axis_length_v)-(hist_data[i][k]*Math.round((float)axis_length_v / (float)max_y[i])),
//										Math.round((float)axis_length_h/(float)hist_bins),
//										hist_data[i][k]*Math.round((float)axis_length_v / (float)max_y[i]) );
//							}
//							fill( 255,0,0 );
//							for( int k = 0; k < hist_bins; k++ ) {
//								
//								rect(	axis_spacing_h + k*Math.round((float)axis_length_h/(float)hist_bins),
//										(axis_spacing_v + axis_length_v)-(sel_hist_data[i][k]*Math.round((float)axis_length_v / (float)max_y[i])),
//										Math.round((float)axis_length_h/(float)hist_bins),
//										sel_hist_data[i][k]*Math.round((float)axis_length_v / (float)max_y[i]) );
//							}
////							line( axis_spacing_h, axis_spacing_v + axis_length_v, axis_spacing_h + axis_length_h, axis_spacing_v + axis_length_v );
////							line( axis_spacing_h, axis_spacing_v, axis_spacing_h, axis_spacing_v + axis_length_v );
//							
//							// draw the axes
//							
//							
//							// label the axes
//							
//							
//						}
//						
//						// is the selection on?
//						if( selectionOn && selSplom[0]==i && selSplom[1]==j) {
//							
//							// draw the slection box
//							fill(32,32);
//							rect(selBoxCoords.x,selBoxCoords.y,selBoxDims.x,selBoxDims.y);
//							
//						}
//						
//						translate( 0, cell_height + CELL_SPACING );
//					}
//					
//					popMatrix();
//					translate( cell_width + CELL_SPACING, 0 );
//				}

			}
			else {
				
				// find the proper locations for everything
				for( int i = 0; i < num_numerics; i++ ) {
					
					pushMatrix();
					
					translate( 0, i*(cell_height + CELL_SPACING) );

					for( int j = i+1; j < num_numerics; j++ ) {
						
						if( isSPLOM ) {
							
							// draw the white background
							
							stroke(128);												
							fill(255);
							beginShape();
							vertex(0,0);
							vertex(cell_width,0);
							vertex(cell_width,cell_height);
							vertex(0,cell_height);
							endShape(CLOSE);
							
							if( i != j ) {
								
								stroke(255-32);
								
								// draw the label lines
								for( int k = 0; k < axeslines+1; k++ ) {
									
									
									line(  (float)cell_width-1.f, 
											(float)(k*((float)(cell_height-2*axis_spacing_v))/axeslines + axis_spacing_v),
											1.f,
											(float)(k*((float)(cell_height-2*axis_spacing_v))/axeslines + axis_spacing_v));
									line(  (float)(k*((float)(cell_width-2*axis_spacing_h))/(axeslines-1) + axis_spacing_h), 
											(float)cell_height-1.f,
											(float)(k*((float)(cell_width-2*axis_spacing_h))/(axeslines-1) + axis_spacing_h),
											1.f);
								}
	
								// draw the axes
														
								stroke(0);
	//							line( axis_spacing_h, axis_spacing_v + axis_length_v, axis_spacing_h + axis_length_h, axis_spacing_v + axis_length_v );
	//							line( axis_spacing_h, axis_spacing_v, axis_spacing_h, axis_spacing_v + axis_length_v );
														
								// scale the transformation matrix to the appropriate bounds						
								pushMatrix();
														
								// scale and translate (or is it the other way around?)
								translate(axis_spacing_h, axis_spacing_v);
								
								scale( 	(float)(axis_length_h/(plot_bounds[i][j][2] - plot_bounds[i][j][0])), 
										-(float)(axis_length_v/(plot_bounds[i][j][3] - plot_bounds[i][j][1])) );
								
								translate((float)-plot_bounds[i][j][0],-(float)plot_bounds[i][j][3]);
								
								// pointsize scaling
					    		 double R_x = (plot_bounds[i][j][2] - plot_bounds[i][j][0]) / (cell_width - (2*axis_spacing_h));
					    		 double R_y = (plot_bounds[i][j][3] - plot_bounds[i][j][1]) / (cell_height - (2*axis_spacing_v));
								float pointsize_x = (float)(point_size * R_x);
								float pointsize_y = (float)(point_size * R_y);
											
								noStroke();
								fill(128);
								
								if( ! bubblePlot ) {
									
									// draw the plain points
									
		//							stroke(128);
									//this.strokeWeight(arg0);
									for( int k = 0; k < getOp().rows(); k++ ) {
									
										if( hasColorCol ) {
											
											fill( (int) getOp().getMeasurement(k, colorCol ) );
										}
		
										if( getOp().getMeasurement(k, selectionIndex) < 1e-5 ) {
											ellipse( (float)getOp().getMeasurement(k,numerics.get(i)),  
													(float)getOp().getMeasurement(k,numerics.get(j)), 
													pointsize_x, 
													pointsize_y);
										}
									}
									fill(255,0,0);
									//this.strokeWeight(arg0);
									beginShape(POINTS);
									for( int k = 0; k < getOp().rows(); k++ ) {
									
										if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
											ellipse( (float)getOp().getMeasurement(k,numerics.get(i)),  
													(float)getOp().getMeasurement(k,numerics.get(j)), 
													pointsize_x, 
													pointsize_y);
										}
									}
									endShape();
								}
								else {
									
									pointsize_x = (float)(max_bubble_size * R_x);
									pointsize_y = (float)(max_bubble_size * R_y);
									
									// draw bubbles
									
									int bin_info[][][] = this.b2ds[i][j].get2DBins();
									int max_2dbin_count = this.b2ds[i][j].getMax2DBin();
									double range_x = (plot_bounds[i][j][2] - plot_bounds[i][j][0]);
									double range_y = (plot_bounds[i][j][3] - plot_bounds[i][j][1]);
									
									// handle the case with no color
									
									if( !hasColorCol ) {
										
										for( int b1s = 0; b1s < bin_info.length; b1s++ ) {
											
											for( int b2s = 0; b2s < bin_info[0].length; b2s++ ) {
												
												if( bin_info[b1s][b2s][0] > 0 ) {
													
													double ptsize = (Math.sqrt(bin_info[b1s][b2s][0]) / ((double)Math.sqrt(max_2dbin_count)))*(max_bubble_size-point_size) + point_size;
													
													ellipse((float) (plot_bounds[i][j][0] + b1s*range_x/((double)bin_info.length - 1)),  
															(float) (plot_bounds[i][j][1] + b2s*range_y/((double)bin_info[0].length - 1)), 
															(float)(ptsize * R_x), 
															(float)(ptsize * R_y));
												}
											}
										}
									}
									else {
										
										ArrayList<Double> colors = this.b2ds[i][j].bin3.getUniqueValues();
										
										for( int b1s = 0; b1s < bin_info.length; b1s++ ) {
											
											for( int b2s = 0; b2s < bin_info[0].length; b2s++ ) {
												
												
//												System.out.print("PRE:  ");												
												double [] temp_floats = new double[bin_info[0][0].length];
												for( int b3s = 0; b3s < bin_info[0][0].length; b3s++ ) {
													
													temp_floats[b3s] = bin_info[b1s][b2s][b3s];
//													System.out.print(" " + bin_info[b1s][b2s][b3s] );
													
												}
												int[] skimbox = FloatIndexer.sortFloats( temp_floats );
//												System.out.println();
//												System.out.print("POST: ");												
//												for( int b3s = 0; b3s < bin_info[0][0].length; b3s++ ) {
//													
//													System.out.print(" " + skimbox[b3s] );
//													
//												}
//												System.out.println();												
//												System.out.print("MAP: ");												
//												for( int b3s = 0; b3s < bin_info[0][0].length; b3s++ ) {
//													
//													System.out.print(" " + bin_info[b1s][b2s][(bin_info[0][0].length-1)-skimbox[b3s]] );
//													
//												}
//												System.out.println();												
												for( int b3s = 0; b3s < bin_info[0][0].length; b3s++ ) {
													
													if( bin_info[b1s][b2s][skimbox[(bin_info[0][0].length-1)-b3s]] > 0 ) {
														
														double ptsize = (Math.sqrt(bin_info[b1s][b2s][skimbox[(bin_info[0][0].length-1)-b3s]]) / ((double)Math.sqrt(max_2dbin_count)))*(max_bubble_size-point_size) + point_size;
														
//														System.out.println(""+colors);
//														System.out.println(""+skimbox);
//														System.out.println(""+bin_info);
														fill( colors.get( skimbox[(bin_info[0][0].length-1)-b3s]).intValue() );
														ellipse((float) (plot_bounds[i][j][0] + b1s*range_x/((double)bin_info.length - 1)),  
																(float) (plot_bounds[i][j][1] + b2s*range_y/((double)bin_info[0].length - 1)), 
																(float)(ptsize * R_x), 
																(float)(ptsize * R_y));
													}
												}
//												System.out.println();
											}
										}
									}
								}
	
								stroke(0);
								
								popMatrix();
								
							}
							
							// is the selection on?
							if( selectionOn && selSplom[0]==i && selSplom[1]+1==j) {
								
								// draw the slection box
								fill(32,32);
								rect(selBoxCoords.x,selBoxCoords.y,selBoxDims.x,selBoxDims.y);
								
							}
							
						}
						else { // isBOM
							
							// set the color
							int from 	= color(44, 123, 182);
							int z_col 	= color(255, 255, 191);
							int to 		= color(215, 25, 28);

							int is_color =  -1;
							float corr_val = (float) Math.min(Math.max(-1.0,r[i][j]),1.0);
							if( Float.isNaN(corr_val) || Float.isInfinite(corr_val)) {
								corr_val = 0.f;
							}
							if( corr_val < 0.f ) {
								is_color = lerpColor( z_col, to, corr_val*(-1.f) ); 
							}
							else {
								is_color = lerpColor( z_col, from, corr_val ); 
							}
							fill(is_color);
							
							// draw the rect
							beginShape();
							vertex( 0, 0 );
							vertex( cell_width, 0 );
							vertex( cell_width, cell_height );
							vertex( 0, cell_height );
							endShape(CLOSE);
							
							// label the data
							fill( 0 );
							textAlign(CENTER);
							int x_pos = (int)Math.round(cell_width/2);
							int y_pos = (int)Math.round(cell_height/2);
							String form_string = nf.format(Math.max(Math.min(1.0,r[i][j]),-1.0));
							if( Double.isNaN(Math.max(Math.min(1.0,r[i][j]),-1.0)) || Double.isInfinite(Math.max(Math.min(1.0,r[i][j]),-1.0))) {
								form_string = "N/A";
							}
							if( cell_width > this.textWidth(form_string) ) {
								text(form_string, x_pos, y_pos );
							}
						}

						translate( 0, cell_height + CELL_SPACING );
					}
					
					popMatrix();
					translate( cell_width + CELL_SPACING, 0 );
				}
			}			
			
			popMatrix();			
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
    	 
    	 // compute transformation from screen to data coordinates
    	 
    	 if( selSplom[0] != selSplom[1]+1 || !drawHistograms ) {
    		 
    		 double R_x = (plot_bounds[selSplom[0]][selSplom[1]+1][2] - plot_bounds[selSplom[0]][selSplom[1]+1][0]) / (cell_width - (2*axis_spacing_h));
    		 double R_y = (plot_bounds[selSplom[0]][selSplom[1]+1][3] - plot_bounds[selSplom[0]][selSplom[1]+1][1]) / (cell_height - (2*axis_spacing_v));
    		     		 
//    		 System.out.println("selSplom = " + selSplom[0] + "," + selSplom[1]);
//    		 System.out.println("R_x = " + R_x + ", R_y = " + R_y);

    		 // scatterplot selection routine
    		 double a = Math.min( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom[0]][selSplom[1]+1][0];
    		 double b = Math.min( ((cell_height - selBoxCoords.y) - axis_spacing_v)*R_y, ((cell_height - (selBoxCoords.y+selBoxDims.y)) - axis_spacing_v)*R_y) + plot_bounds[selSplom[0]][selSplom[1]+1][1];
    		 double c = Math.max( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom[0]][selSplom[1]+1][0];
    		 double d = Math.max( ((cell_height - selBoxCoords.y) - axis_spacing_v)*R_y, ((cell_height - (selBoxCoords.y+selBoxDims.y)) - axis_spacing_v)*R_y) + plot_bounds[selSplom[0]][selSplom[1]+1][1];
    		 
//    		 System.out.println("a = " + a + ", b = " + b + "c = " + c + ", d = " + d);
    		 
    		 // determine membership
			for( int k = 0; k < getOp().rows(); k++ ) {
				
				boolean test_horz = (float)getOp().getMeasurement(k,numerics.get(selSplom[0])) >= a;
				test_horz = test_horz && ((float)getOp().getMeasurement(k,numerics.get(selSplom[0])) <= c);
				boolean test_vert = (float)getOp().getMeasurement(k,numerics.get(selSplom[1]+1)) >= b;
				test_vert = test_vert && ((float)getOp().getMeasurement(k,numerics.get(selSplom[1]+1)) <= d);
				if( test_horz && test_vert ) {
					
					getOp().setMeasurement(k, selectionIndex, 1.0);
				}
				else {
					
					getOp().setMeasurement(k, selectionIndex, 0.0);
				}
			}

			getOp().tableChanged(new TableEvent( getOp(), TableEvent.TableEventType.ATTRIBUTE_CHANGED, "selection", null, false), true);
			getOp().tableChanged(new TableEvent( getOp(), TableEvent.TableEventType.ATTRIBUTE_CHANGED, "selection", null, true), true);
    		 
    	 }
    	 else {
    		 
    		 // histogram selection routine
    		 double R_x = (plot_bounds[selSplom[0]][selSplom[1]][2] - plot_bounds[selSplom[0]][selSplom[1]][0]) / (cell_width - (2*axis_spacing_h));
    		     		 
    		 // scatterplot selection routine
    		 double a = Math.min( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom[0]][selSplom[1]][0];
    		 double c = Math.max( (selBoxCoords.x - axis_spacing_h)*R_x, ((selBoxCoords.x+selBoxDims.x) - axis_spacing_h)*R_x ) + plot_bounds[selSplom[0]][selSplom[1]][0];
    		     		 
    		 // determine membership
			for( int k = 0; k < getOp().rows(); k++ ) {
				
				boolean test_horz = (float)getOp().getMeasurement(k,numerics.get(selSplom[0])) >= a;
				test_horz = test_horz && ((float)getOp().getMeasurement(k,numerics.get(selSplom[0])) <= c);
				if( test_horz ) {
					
					getOp().setMeasurement(k, selectionIndex, 1.0);
				}
				else {
					
					getOp().setMeasurement(k, selectionIndex, 0.0);
				}
			}
    	 }    

    	 if( drawHistograms ) {
	    	 
	    	 sel_hist_data	= new int[num_numerics][hist_bins];
	
	    	 // update the selection histograms
	    	 for( int i = 0; i < num_numerics; i++ ) {
				// split into hist_bins
				double hist_bin_width = (plot_bounds[i][i][2] - plot_bounds[i][i][0])/((double)hist_bins);
				
				// tally up the membership into the bins
				for( int k = 0; k < getOp().rows(); k++ ) {
	
					if( getOp().getMeasurement(k, selectionIndex) > 0 ) {
						int bin_no = Math.min(hist_bins-1, Math.max(0, (int)Math.floor(((float)getOp().getMeasurement(k,numerics.get(i)) - plot_bounds[i][i][0])/hist_bin_width)));							
						sel_hist_data[i][bin_no]++;
					}
				}
	    	 }			
    	 }
     }
     
     public void mouseDragged() {
    	 
    	 // are we creating a box?
    	 if( selectionOn ) {
    		 
    	 	// update the box coordinates (with clipping)
    		 PVector localCoords =  new PVector(	mouseX - (BIG_BORDER_H_L + selSplom[0]*(cell_width+CELL_SPACING)) ,
													mouseY - (BIG_BORDER_V_TOP + selSplom[1]*(cell_height+CELL_SPACING)) );
 
    		 PVector diffCoords = PVector.sub(localCoords, selBoxCoords );
    		 selBoxDims = new PVector( 	Math.min( cell_width-selBoxCoords.x, Math.max(-selBoxCoords.x, diffCoords.x) ),
    				 					Math.min( cell_height-selBoxCoords.y, Math.max(-selBoxCoords.y, diffCoords.y) ) );
    		 
     	 	// update the selection state of the underlying data
    		 selectionUpdate();
    		 
    		 redraw();
    	 }
     }
     
     public void mouseMoved() {

    	 //redraw();    	 
    }
     
     public void mousePressed() {
    	 
    	 // find the nearest Splom
    	 nearestSplom();
    	 
    	 // are we in the splom region?
    	 if( inSplom ) {
    	 
    		 selectionOn = true;
    		 
    	 	// record the start point in local coordinates
    		 selBoxCoords = new PVector(	mouseX - (BIG_BORDER_H_L + selSplom[0]*(cell_width+CELL_SPACING)) ,
    				 						mouseY - (BIG_BORDER_V_TOP + selSplom[1]*(cell_height+CELL_SPACING)) );
    		 selBoxDims = new PVector(0,0);
    	 
         	// update the screen (run draw once)
         	redraw();
    	 }
    	 else {
    		 
    		 // determine if we are over one of the labels
 			int label_loop_bound = num_numerics;
			if( !drawHistograms ) {
				
				label_loop_bound--;
			}

			boolean in_label = false;
			int in_label_index = -1;
			
			for( int i = 0; i < label_loop_bound; i++ ) {
				
				float x_pos = (float)BIG_BORDER_H_L + i * ((float)(cell_width + CELL_SPACING)) + (float)cell_width/4.f;
				float y_pos = BIG_BORDER_V_TOP + (label_loop_bound) * ((float)(cell_height+CELL_SPACING)) + 2.5f*textAscent();
				float labelTextWidth = this.textWidth(this.getOp().input.getColName(numerics.get(i)));
				if( ( mouseX >= x_pos  && mouseX <= (x_pos+labelTextWidth) ) &&
					( mouseY >= y_pos-textAscent()  && mouseY <= (y_pos+textDescent() ) ) ) {
					
					in_label = true;
					in_label_index = i;
					break;
				}
				
				x_pos = BIG_BORDER_H_L - (2*LABEL_SPACING_H+(int)textWidth( "0.##E0" ));
				y_pos = textAscent()/2.f + BIG_BORDER_V_TOP + i * ((float)(cell_height+CELL_SPACING)) + cell_height/2.f;
				//System.out.println("x:"+mouseX+" y:"+mouseY +" versus ("+(x_pos-textAscent())+","+(x_pos+textDescent())+") and ("+(y_pos+labelTextWidth/2.f)+","+(y_pos-labelTextWidth/2.f )+")");
				if( ( mouseX >= x_pos-textAscent()  && mouseX <= (x_pos+textDescent()) ) &&
					( mouseY <= y_pos+labelTextWidth/2.f  && mouseY >= (y_pos-labelTextWidth/2.f ) ) ) {
						
						in_label = true;
						in_label_index = i+1;
						break;
				}
			}    		 
			
			if( in_label ) {
				
				cull_hilight[in_label_index] = !cull_hilight[in_label_index];
				redraw();
			}
    	 }
     }

     /**
	 * 
	 */
	private static final long serialVersionUID = -8429666944296674336L;

	@Override
	public void stateChanged(ChangeEvent e) {

		if( e.getSource() instanceof JSlider ) {
			
			JSlider source = (JSlider)e.getSource();
			
			if( source.getName().equalsIgnoreCase("slider")) {
			    if (!source.getValueIsAdjusting()) {
			        int val = ((int)source.getValue())*50 + 50;
			        
//			        System.out.println( "val = " + val );	
			        
			        minSplomBound 	= val;

			        // count the number of dimensions
					
					int span_h = (BIG_BORDER_H_L+BIG_BORDER_H_R) + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
					int span_v = (BIG_BORDER_V_TOP+BIG_BORDER_V_BOT) + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
					
					// compute the minimum size
					size(span_h, span_v );

					SwingUtilities.invokeLater(new Runnable() {
				        public void run() {
				        	invalidate();
				          // Set the preferred size so that the layout managers can handle it
				          getParent().getParent().validate();
							heavyResize();
							redraw();
				        }
				      });
			    }
			    
			}
			else if (source.getName().equalsIgnoreCase("hist_slider")) {
			    if (!source.getValueIsAdjusting()) {

			    	hist_bins = ((int)source.getValue())+20;
			    	heavyResize();
					redraw();
			    }
			}
			else if (source.getName().equalsIgnoreCase("size_slider")) {
			    if (!source.getValueIsAdjusting()) {

			    	point_size = ((int)source.getValue())+3;
					redraw();
			    }
			}
			
			
		}
	}
}
