package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import processing.core.PFont;
import still.data.Operator;
import still.data.Table.ColType;
import still.operators.PearsonCollectOp;

public class PMatrixPainter extends OPApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1839561017032317393L;
	
	public static int MIN_CELL_SIZE = 10;
	public static int MAX_CELL_SIZE = 100;
	public static int CELL_SPACING  = 5;
	public static int FONT_SIZE		= 10;
	public static int TEXT_BORDER_H   = 50;
	public static int TEXT_BORDER_V   = 25;
	public NumberFormat nf = null;
	
    public PMatrixPainter(Operator op) {
	
		super(op);		
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);
    }
    
    /**
     * Handle when the operator potentially changed
     */
	public void actionPerformed(ActionEvent e) {

//		super.actionPerformed(e);

		numerics = getNumerics();
		countNumerics();

		// resize the window
//		PearsonCollectOp pcop = (PearsonCollectOp) this.getOp();
		
//		double[][] r = pcop.getPostCorrelationMatrix();
		
//		int span_h = TEXT_BORDER_H + (r.length * MIN_CELL_SIZE) + (r.length-1)*CELL_SPACING;
//		int span_v = TEXT_BORDER_V + (r.length * MIN_CELL_SIZE) + (r.length-1)*CELL_SPACING;
		
		// compute the minimum size
//		size(	span_h, 
//				span_v);
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
//	        	invalidate();
		          // Set the preferred size so that the layout managers can handle it
//		          getParent().validate();
//					heavyResize();
					redraw();
	        }
	      });
	}

	void countNumerics( ) {
		
		num_numerics = 0;
		for( ColType type : this.getOp().getColTypes() ) {
			
			if( type == ColType.NUMERIC ) {
				
				num_numerics++;
			}
		}
	}
	
	ArrayList<Integer> getNumerics( ) {
		
		ArrayList<Integer> numerics = new ArrayList<Integer>();
		int i = 0;
		
		for( ColType type : this.getOp().getColTypes() ) {
		
			
			if( type == ColType.NUMERIC ) {
				
				numerics.add(new Integer(i));
			}
			
			i++;
		}
		
		return numerics;
	}

	public Dimension graphSize() {
		
		PearsonCollectOp pcop = (PearsonCollectOp) this.getOp();			
		double[][] r = pcop.getPostCorrelationMatrix();
		
		// calculate cell size
		float cell_width  = Math.min( MAX_CELL_SIZE, (float)((this.width - (r.length-1.f)*CELL_SPACING) - TEXT_BORDER_H) / ((float)r.length-1.f) );
		float cell_height = Math.min( MAX_CELL_SIZE, (float)((this.height - (r.length-1.f)*CELL_SPACING) - TEXT_BORDER_V) / ((float)r.length-1.f) );

		return new Dimension((int)cell_width,(int)cell_height);
	}

	public void setup() {
		
		// added by HY
		String sFontFile = "SansSerif-10.vlw";
		if (!(new File(sFontFile)).exists())
			sFontFile = "../data/SansSerif-10.vlw";
		PFont myFont = loadFont(sFontFile);
		//PFont myFont = loadFont( "SansSerif-10.vlw" );
		textFont(myFont,10);
		numerics = getNumerics();
		countNumerics();

		if( this.getOp() instanceof PearsonCollectOp ) {

			PearsonCollectOp pcop = (PearsonCollectOp) this.getOp();
			
			double[][] r = pcop.getPostCorrelationMatrix();
			
			int span_h = TEXT_BORDER_H + (r.length * MIN_CELL_SIZE) + (r.length-1)*CELL_SPACING;
			int span_v = TEXT_BORDER_V + (r.length * MIN_CELL_SIZE) + (r.length-1)*CELL_SPACING;
			
			// compute the minimum size
			size(	Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT));
			this.setPreferredSize(new Dimension(Math.max( span_h, OPAppletViewFrame.MINIMUM_VIEW_WIDTH), 
					Math.max( span_v, OPAppletViewFrame.MINIMUM_VIEW_HEIGHT)));
		}
		
    	 
    	 // prevent thread from starving everything else
         noLoop();

         this.finished_setup = true;
     }

     public void draw() {
    	 

    	 if( this.getOp() instanceof PearsonCollectOp) {

        	 background(255);

        	 PearsonCollectOp pcop = (PearsonCollectOp) this.getOp();			
			double[][] r = pcop.getPostCorrelationMatrix();
			
			float max_title_width = 0;
			for( int i = 0; i < this.num_numerics; i++ ) {

				max_title_width = Math.max( textWidth( this.getOp().input.getColName(numerics.get(i)) ), max_title_width );
			}
			TEXT_BORDER_H = (int)max_title_width+25;

			// calculate cell size
			float cell_width  = Math.min( MAX_CELL_SIZE, (float)((this.width - (r.length-1.f)*CELL_SPACING) - TEXT_BORDER_H) / ((float)r.length-1.f) );
			float cell_height = Math.min( MAX_CELL_SIZE, (float)((this.height - (r.length-1.f)*CELL_SPACING) - TEXT_BORDER_V) / ((float)r.length-1.f) );

			fill(0);
						
			for( int i = 0; i < r.length-1; i++ ) {
				
				textAlign(LEFT);
				float x_pos = (float)TEXT_BORDER_H + i * ((float)(cell_width + CELL_SPACING)) + (float)cell_width/4.f;
				float y_pos = (3.f*TEXT_BORDER_V)/4.f + i * ((float)(cell_height+CELL_SPACING));
				text(this.getOp().getColName(numerics.get(i)), (float)x_pos, (float)y_pos );
				textAlign(RIGHT);
				x_pos = TEXT_BORDER_H - CELL_SPACING;
				y_pos = textAscent()/2.f + TEXT_BORDER_V + i * ((float)(cell_height+CELL_SPACING)) + cell_height/2.f;
				text(this.getOp().getColName(numerics.get(i+1)), (float)x_pos, (float)y_pos );
			}
			
			pushMatrix();
			translate( TEXT_BORDER_H, TEXT_BORDER_V);
			
			// find the proper locations for everything
			for( int i = 0 ; i < r.length ; i++ ) {		// horizontal
				
				pushMatrix();
				
				translate( 0, i*(cell_height + CELL_SPACING) );
				
				for( int j = i+1 ; j < r.length ; j++ ) {			// vertical
					
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
					
					translate( 0, cell_height + CELL_SPACING );
				}
				
				popMatrix();
				translate( cell_width + CELL_SPACING, 0 );
			}
			
			popMatrix();
			
			// draw the dimension labels
    	 }
    	 else {
    	 
	    	 // Draw gray box
	    	 stroke(0);
	    	 noFill();
	    	 beginShape();
	    	 vertex(5,5);
	    	 vertex(5, this.getHeight()-5);
	    	 vertex(this.getWidth()-5, this.getHeight()-5);
	    	 vertex(this.getWidth()-5, 5);
	    	 endShape(CLOSE);
    	 }
     }

     public void mousePressed() {
         // do something based on mouse movement

         // update the screen (run draw once)
         redraw();
     }
}
