package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import processing.core.PFont;
import still.data.Operator;
import still.operators.CAOp;

public class PCorrAPainter extends OPApplet  implements ChangeListener {

	public int BIG_BORDER_H = 75;
	public int BIG_BORDER_V = 25;
	public int CELL_SPACING = 40;
	public int ORIGIN_PIXELS = 50;
	public int cell_width = -1;
	public int cell_height = -1;
	public int margin_size = 30;
	public boolean split_components = false;
	public CAOp caop = null;
	
	
	public PCorrAPainter(Operator op) {
		super(op);
		
		if( op instanceof CAOp ) {
			
			caop = (CAOp)op;
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

	@Override
	public void actionPerformed(ActionEvent e) {

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
		    			
		heavyResize();

		// prevent thread from starving everything else
         noLoop();
         
         this.finished_setup = true;
         
		size(	OPAppletViewFrame.MINIMUM_VIEW_WIDTH, 
				OPAppletViewFrame.MINIMUM_VIEW_HEIGHT);//, P2D);
		this.setPreferredSize(new Dimension(OPAppletViewFrame.MINIMUM_VIEW_WIDTH, 
				OPAppletViewFrame.MINIMUM_VIEW_HEIGHT));
     }

	public void heavyResize() {

		this.cell_width = Math.round(this.width - 2*BIG_BORDER_H);
		this.cell_height = Math.round(this.height - 2*BIG_BORDER_V);
		if( split_components ) {
			
			this.cell_width = Math.round((this.width - CELL_SPACING - 2*BIG_BORDER_H) / 2.f);
		}
	}
	
    public synchronized void draw() {
   	 
	   	 background(128+64+32);
	
	   	 if( caop != null ) {
	
	   		if( caop.hasDiagram ) {
	   			
	   			return;
	   		}
	   		
			if( split_components ) {
				
			}
			else {
				
				fill(0);	
				
				if( caop.compDim1 < 0 || caop.compDim2 < 0 ) {
					
					return;
				}
				
				// label the axes
				
				translate( BIG_BORDER_H, BIG_BORDER_V );
				
				// draw the title to the histogram
				
				this.stroke(0);
				this.fill(0);
				this.textAlign(CENTER);
				text(caop.input.getColName(caop.compDim1) + " and " + caop.input.getColName(caop.compDim2), cell_width/2.0f, -CELL_SPACING/6.f );
				
				String[] category1 = ( caop.binner_col1 != null )?(caop.binner_col1.getBinStrings()):(caop.getCategories( caop.compDim1 ));
				String[] category2 = ( caop.binner_col2 != null )?(caop.binner_col2.getBinStrings()):(caop.getCategories( caop.compDim2 ));
				
				// draw the labels on the axes
				
				text("DIM1", cell_width/2.0f, cell_height + textAscent() + textDescent() );
				
				pushMatrix();
				textAlign(CENTER);
				rotate(-PI/2.f);
				translate(-cell_height/2.f, -(textAscent() + textDescent()));
				text("DIM2", 0.f, 0.f );
				popMatrix();
				
				// draw the white background
				
				stroke(128);
					fill(255);
					beginShape();
					vertex(0,0);
					vertex(cell_width,0);
					vertex(cell_width,cell_height);
					vertex(0,cell_height);
					endShape(CLOSE);
					
				// calculate and construct manual point transforms
				double min_cat_1_x = caop.getCACoord(1, 0)[0];
				double max_cat_1_x = min_cat_1_x;
				double min_cat_1_y = caop.getCACoord(1, 0)[1];
				double max_cat_1_y = min_cat_1_y;
				double min_cat_2_x = caop.getCACoord(2, 0)[0];
				double max_cat_2_x = min_cat_2_x;
				double min_cat_2_y = caop.getCACoord(2, 0)[1];
				double max_cat_2_y = min_cat_2_y;
				for( int k = 0; k < category1.length; k++ ) {
					
					min_cat_1_x = Math.min( caop.getCACoord(1, k)[0], min_cat_1_x );
					max_cat_1_x = Math.max( caop.getCACoord(1, k)[0], max_cat_1_x );
					min_cat_1_y = Math.min( caop.getCACoord(1, k)[1], min_cat_1_y );
					max_cat_1_y = Math.max( caop.getCACoord(1, k)[1], max_cat_1_y );
				}				
				for( int k = 0; k < category2.length; k++ ) {
					
					min_cat_2_x = Math.min( caop.getCACoord(2, k)[0], min_cat_2_x );
					max_cat_2_x = Math.max( caop.getCACoord(2, k)[0], max_cat_2_x );
					min_cat_2_y = Math.min( caop.getCACoord(2, k)[1], min_cat_2_y );
					max_cat_2_y = Math.max( caop.getCACoord(2, k)[1], max_cat_2_y );
				}
				double cat_1_range_x = max_cat_1_x - min_cat_1_x;
				double cat_1_range_y = max_cat_1_y - min_cat_1_y;
				double cat_2_range_x = max_cat_2_x - min_cat_2_x;
				double cat_2_range_y = max_cat_2_y - min_cat_2_y;

				// draw where the origin is
				
				stroke(0);
				
				line( 	margin_size + (float)( (0. - min_cat_1_x)/(cat_1_range_x) ) * (cell_width-margin_size*2) - ORIGIN_PIXELS,  
						margin_size + (float)( (0. - min_cat_1_y)/(cat_1_range_y) ) * (cell_width-margin_size*2),
						margin_size + (float)( (0. - min_cat_1_x)/(cat_1_range_x) ) * (cell_width-margin_size*2) + ORIGIN_PIXELS,  
						margin_size + (float)( (0. - min_cat_1_y)/(cat_1_range_y) ) * (cell_width-margin_size*2) );
				line( 	margin_size + (float)( (0. - min_cat_1_x)/(cat_1_range_x) ) * (cell_width-margin_size*2),  
						margin_size + (float)( (0. - min_cat_1_y)/(cat_1_range_y) ) * (cell_width-margin_size*2) - ORIGIN_PIXELS,
						margin_size + (float)( (0. - min_cat_1_x)/(cat_1_range_x) ) * (cell_width-margin_size*2),  
						margin_size + (float)( (0. - min_cat_1_y)/(cat_1_range_y) ) * (cell_width-margin_size*2)  + ORIGIN_PIXELS);
				
				
//				System.out.println("range_1 = (" + cat_1_range_x +"," + cat_1_range_y +")");
//				System.out.println("range_2 = (" + cat_2_range_x +"," + cat_2_range_y +")");
				
				textAlign( CENTER );
				fill(202, 0, 32);
				for( int k = 0; k < category1.length; k++ ) {
					
					float x = margin_size + (float)( (caop.getCACoord(1, k)[0] - min_cat_1_x)/(cat_1_range_x) ) * (cell_width-margin_size*2);					
					float y = margin_size + (float)( (caop.getCACoord(1, k)[1] - min_cat_1_y)/(cat_1_range_y) ) * (cell_height-margin_size*2);
					if( cat_1_range_x < 1e-5 ) {
						x = cell_width/2.f;
					}
					if( cat_1_range_y < 1e-5 ) {
						y = cell_height/2.f;
					}
					text( category1[k], x, y);
				}
				fill(5, 113, 176);
				for( int k = 0; k < category2.length; k++ ) {
					
					float x = margin_size + (float)( (caop.getCACoord(2, k)[0] - min_cat_2_x)/(cat_2_range_x) ) * (cell_width-margin_size*2);					
					float y = margin_size + (float)( (caop.getCACoord(2, k)[1] - min_cat_2_y)/(cat_2_range_y) ) * (cell_height-margin_size*2);
					if( cat_2_range_x < 1e-5 ) {
						x = cell_width/2.f;
					}
					if( cat_2_range_y < 1e-5 ) {
						y = cell_height/2.f;
					}
					text( category2[k], x, y);				
				}
				stroke(0);
			}
	    }
    }

    /**
	 * 
	 */
	private static final long serialVersionUID = -7816735200201227563L;


	@Override
	public void stateChanged(ChangeEvent e) {
		if( e.getSource() instanceof JSlider ) {
			
			JSlider source = (JSlider)e.getSource();
			
			if( source.getName().equalsIgnoreCase("slider")) {
			    if (!source.getValueIsAdjusting()) {
			        int val = ((int)source.getValue())*100 + 100;
			        			        					
					// compute the minimum size
					size(val, val );

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
		}
	}	
	
}
