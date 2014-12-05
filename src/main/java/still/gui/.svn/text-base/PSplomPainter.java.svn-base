package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PFont;
import processing.core.PVector;

import still.data.Operator;
import still.data.TableEvent;
import still.data.Table.ColType;
import still.operators.PearsonCollectOp;
import still.operators.SplomOp;

public class PSplomPainter extends OPApplet implements ChangeListener {

	public boolean hasColorCol = false;
	public int colorCol = 0;
	public int point_size = 4;
	public boolean selectionOn = false;
	public int minSplomBound 	= 20;
	public int BIG_BORDER_H_L = 75;
	public int BIG_BORDER_H_R = 25;
	public int BIG_BORDER_V = 25;
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
	public int axeslines = 5;
	
	public PVector selBoxCoords = null;
	public PVector selBoxDims = null;
	
	boolean drawHistograms = false;
	
	
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
		BIG_BORDER_H_L = (int)max_title_width+25;
		
		if( drawHistograms ) {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (double)num_numerics);
			cell_height = (int)Math.round(((this.height - (num_numerics-1)*CELL_SPACING) - 2*BIG_BORDER_V) / (double)num_numerics);
		}
		else {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (num_numerics-1.));
			cell_height = (int)Math.round(((this.height - (num_numerics-2)*CELL_SPACING) - 2*BIG_BORDER_V) / (num_numerics-1.));
		}

		return new Dimension((int)cell_width,(int)cell_height);
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
		
		int label_loop_bound = num_numerics;
		if( !drawHistograms ) {
			
			label_loop_bound--;
		}
		float max_title_width = 0;
		for( int i = 0; i < label_loop_bound; i++ ) {

			max_title_width = Math.max( textWidth( this.getOp().input.getColName(numerics.get(i)) ), max_title_width );
		}
		BIG_BORDER_H_L = (int)max_title_width+25;
		
		if( drawHistograms ) {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-1)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (double)num_numerics);
			cell_height = (int)Math.round(((this.height - (num_numerics-1)*CELL_SPACING) - 2*BIG_BORDER_V) / (double)num_numerics);
		}
		else {
			cell_width 	= (int)Math.round(((this.width - (num_numerics-2)*CELL_SPACING) - (BIG_BORDER_H_L+BIG_BORDER_H_R)) / (num_numerics-1.));
			cell_height = (int)Math.round(((this.height - (num_numerics-2)*CELL_SPACING) - 2*BIG_BORDER_V) / (num_numerics-1.));
		}
		axis_spacing_h = (int)Math.round(cell_width/10.);
		axis_length_h = (int)Math.round((8.*cell_width)/10.);
		axis_spacing_v = (int)Math.round(cell_height/10.);
		axis_length_v = (int)Math.round((8.*cell_height)/10.);		
		plot_bounds	= new double[num_numerics][num_numerics][4];
		
		max_y = new int[num_numerics];
		hist_data	= new int[num_numerics][hist_bins];
		sel_hist_data	= new int[num_numerics][hist_bins];
		
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
	
	public PSplomPainter(Operator op) {
		super(op);		
		updateColorCol();
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

		smooth();
		
		if( this.getOp() instanceof SplomOp ) {

			// count the number of dimensions
			
			countNumerics();
			int span_h = (BIG_BORDER_H_L+BIG_BORDER_H_R) + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
			int span_v = 2*BIG_BORDER_V + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
			if ( !drawHistograms ) {
				span_h = (BIG_BORDER_H_L+BIG_BORDER_H_R) + (num_numerics-1 * minSplomBound) + (num_numerics-2)*CELL_SPACING;
				span_v = 2*BIG_BORDER_V + (num_numerics-1 * minSplomBound) + (num_numerics-2)*CELL_SPACING;
			}
			
			// compute the minimum size
			
			size(	Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT),P2D);
		}
    			
		heavyResize();

		// prevent thread from starving everything else
         noLoop();
         
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
		int yComp = mouseY - BIG_BORDER_V;
				
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
				yComp % (cell_height+CELL_SPACING) <= cell_height )){
			
			inSplom = true;
		} 
		else {
			
			inSplom = false;
		}
		
//		System.out.println("("+selSplom[0]+","+selSplom[1]+") "+inSplom);
	}

	
     public synchronized void draw() {

    	 background(128+64+32);

    	 if( this.getOp() instanceof SplomOp) {

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
				float y_pos = (3.f*BIG_BORDER_V)/4.f + i * ((float)(cell_height+CELL_SPACING));
				text(this.getOp().input.getColName(numerics.get(i)), (float)x_pos, (float)y_pos );
				textAlign(RIGHT);
				x_pos = BIG_BORDER_H_L - 10;
				y_pos = textAscent()/2.f + BIG_BORDER_V + i * ((float)(cell_height+CELL_SPACING)) + cell_height/2.f;
				text(this.getOp().input.getColName(numerics.get(i+1)), (float)x_pos, (float)y_pos );
			}
			
			pushMatrix();

			translate( BIG_BORDER_H_L, BIG_BORDER_V );
			
			
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
														
							// draw the plain points

//							stroke(128);
							noStroke();
							fill(128);
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

							stroke(0);
							
							popMatrix();
							
						}
						
						// is the selection on?
						if( selectionOn && selSplom[0]==i && selSplom[1]+1==j) {
							
							// draw the slection box
							fill(32,32);
							rect(selBoxCoords.x,selBoxCoords.y,selBoxDims.x,selBoxDims.y);
							
						}
						
						translate( 0, cell_height + CELL_SPACING );
					}
					
					popMatrix();
					translate( cell_width + CELL_SPACING, 0 );
				}
			}
			
			
			popMatrix();
			
			// draw the dimension labels
			
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
													mouseY - (BIG_BORDER_V + selSplom[1]*(cell_height+CELL_SPACING)) );
 
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
    				 						mouseY - (BIG_BORDER_V + selSplom[1]*(cell_height+CELL_SPACING)) );
    		 selBoxDims = new PVector(0,0);
    	 
         	// update the screen (run draw once)
         	redraw();
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
					int span_v = 2*BIG_BORDER_V + (num_numerics * minSplomBound) + (num_numerics-1)*CELL_SPACING;
					
					// compute the minimum size
					size(span_h, span_v,P2D);

					SwingUtilities.invokeLater(new Runnable() {
				        public void run() {
				        	invalidate();
				          // Set the preferred size so that the layout managers can handle it
				          getParent().getParent().validate();
//							heavyResize();
//							redraw();
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
