package still.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
//import java.awt.geom.Line2D.Double;
//import java.awt.geom.Rectangle2D.Double;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * A ScreePlot is a chart which displays a univariate quantity 
 * associated with a list of dimensions.
 * 
 * @author sfingram
 *
 */
public class ScreePlot extends JPanel implements 	ChangeListener, 
													MouseListener, 
													MouseMotionListener,
													ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4761758813308874138L;

	Dimension prefSize = null;
	
	public int h_border = 5;
	public int v_border = 25;
	public int tick_size = 2;
	public int cutoff = 0;
	public int vertical_ticks = 4;
	public int horizontal_ticks = 10;
	public int cur_mouse_x = -1;
	public int cur_mouse_y = -1;
	
	public int fraction_digits = 3;
	
	public boolean isLog		= false;
	public boolean isSorted		= true;
	public boolean useBars 		= true;
	public boolean labelAxes 	= true;
	public boolean isCutoffLeft = false;
	public boolean useCutoff    = true;
	public boolean useLine 		= false;
	public boolean useDimensionNames = true;
	public int max_str_len 		= -1;

	ArrayList<Double> univarQuant 		= null;
	ArrayList<Integer> dimensionOrder 	= null;
	ArrayList<String> univarNames		= null;
	ArrayList<ChangeListener> changeListeners = null;
	
	Comparator<Double> c;
	
	Color bkgndColor 	= Color.WHITE;
	Color axesColor 	= Color.BLACK;
	Color lineColor 	= new Color(0xDF,0x3D,0x33);
	Color barColor		= new Color(0x25,0x8B,0xC1 );
	Color unselbarColor	= Color.LIGHT_GRAY;
	Color selbarColor	= new Color(0xC7,0xC5,0x2B);
	Color borderColor	= Color.BLACK;
	
	
	
	public ScreePlot( 	ArrayList<Double> univarQuant, 
						ArrayList<String> univarNames,
						boolean isSorted, 
						Comparator<Double> c, 
						Dimension prefSize ) {
		
		super();
				
		this.c = c;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.setUnivar(univarQuant, univarNames);
		this.changeListeners = new ArrayList<ChangeListener>();
		
		if( prefSize != null ) {
			
			this.prefSize = prefSize;						
		}
		else {
			
			this.prefSize = new Dimension( 100, 150 );
		}			
	}
	
	public void addChangeListener( ChangeListener cl ) {
		
		this.changeListeners.add( cl );
	}
	
	public class IndexComparator implements Comparator<IndexSorter> {

		Comparator<Double> internalComp = null;
		
		public IndexComparator( Comparator<Double> c ) {
			
			this.internalComp = c;			
		}
		@Override
		public int compare(IndexSorter o1, IndexSorter o2) {
			
			return internalComp.compare( o1.val, o2.val);
		}
	}
	
	public class IndexSorter {
		
		public double val = -1;
		public int index = -1;
		public String name = "";
		
		public IndexSorter( double val, int index, String name ) {
			
			this.val = val;
			this.index = index;
			this.name = name;
		}		
	}
	
	public void setUnivar( ArrayList<Double> univarQuant, ArrayList<String> univarNames ) {
		
		ArrayList<Double> univarQuantReal = null;
		
		// transform the data to log10 scale
		if( isLog ) {
			
			univarQuantReal = new ArrayList<Double>();
			for( double d : univarQuant ) {
				
				if( d < 1e-8) {
					
					univarQuantReal.add( -8. );
				}
				else {
					
					univarQuantReal.add( Math.log10( d ) );
				}
			}			
		}
		else {
			
			univarQuantReal = univarQuant;
		}
		
		// sort the dimensions according to the user supplied comparator
		// maintain an internal list of the ordering of the dimensions
		if( isSorted ) {
			
			ArrayList<IndexSorter> isort = new ArrayList<IndexSorter>();
			
			int index = 0;
			for( double d : univarQuantReal ) {
				
				isort.add(new IndexSorter(d,index,univarNames.get(index)));
				index++;
			}
			Collections.sort(isort, new IndexComparator( c ));
			this.univarQuant = new ArrayList<Double>();
			this.dimensionOrder = new ArrayList<Integer>();
			this.univarNames = new ArrayList<String>();
			for( IndexSorter is : isort ) {
				
				this.univarQuant.add( is.val );
				this.dimensionOrder.add(is.index);
				this.univarNames.add( is.name );
			}
		}
		else {
			
			this.univarQuant = univarQuantReal;
			this.dimensionOrder = new ArrayList<Integer>();
			this.univarNames = univarNames;
			int index = 0; 
			for( double d : univarQuant ) {
				
				this.dimensionOrder.add(index);
				index++;
			}
		}
		
		// update the cutoff to be within range
		if( cutoff >= this.univarQuant.size() ) {
			
			cutoff = this.univarQuant.size()-1;
		}

		this.repaint();
	}
	
	public void paintComponent( Graphics g ) {
				
		super.paintComponent(g);
		
		Graphics2D g2D = (Graphics2D) g;
		
		// clear screen
		
		g2D.setColor( bkgndColor );
		g2D.drawRect(0, 0, this.getWidth(), this.getHeight());
				
		// compute scaled lines and data points
		
		double max_uni = Double.MIN_VALUE;
		double min_uni = Double.MAX_VALUE;
		for( double d : univarQuant ) {
			
			max_uni = Math.max( max_uni, d );
			min_uni = Math.min( min_uni, d );
		}
		
		// calculate the labels and their sizes
		
		this.max_str_len = -1;
		horizontal_ticks = Math.min( 10, univarQuant.size());
		String[] labelStrs = new String[vertical_ticks];
		String[] h_labelStrs = new String[horizontal_ticks];
		int[] h_labelInts = new int[horizontal_ticks];
		NumberFormat nf = new DecimalFormat("0.##E0");//NumberFormat.getInstance();
		NumberFormat nf_i = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		
		FontMetrics fm = g2D.getFontMetrics();
		double scale = (max_uni-min_uni) / ((double) vertical_ticks-1.);
		double scale_h = Math.ceil( (univarQuant.size()) / ((double) horizontal_ticks) );
		int p = 0;
		for( int i = 0; i < univarQuant.size(); i += scale_h ) {
			
			h_labelStrs[p] = nf_i.format( (int)Math.floor( i+1 ) );
			h_labelInts[p] = (int)Math.floor( i );
			p++;
		}
		for( int i = 0; i < vertical_ticks; i++ ) {
			
			// compute and store the string
			if( isLog ) {

				labelStrs[i] = nf.format( Math.pow(10, scale*i + min_uni) );
			}
			else {
				
				labelStrs[i] = nf.format( scale*i + min_uni);
			}
			
			// measure its size
			max_str_len = Math.max(max_str_len, fm.stringWidth(labelStrs[i]) );			
		}

		if( ! labelAxes ) {
			
			max_str_len = 0;
		}
		
		int canvasHeight = this.getHeight() - 2*v_border;
		int canvasWidth = (this.getWidth() -  2*h_border) - max_str_len;
		double yscale =  ( (double) canvasHeight ) / (max_uni - min_uni);
		double barwidth = ((double)canvasWidth) / ((double)univarQuant.size());
		double xscale =  ( (double) canvasWidth-barwidth ) / ((double) univarQuant.size()-1.0);
		
		GeneralPath dataLine = 
			new GeneralPath(GeneralPath.WIND_EVEN_ODD, univarQuant.size());
		ArrayList<Rectangle2D> dataRects = new ArrayList<Rectangle2D>();
		
		if( useLine ) {
			
			dataLine.moveTo ((0.0*xscale) + h_border + max_str_len + barwidth/2.0, (canvasHeight - (univarQuant.get(0)-min_uni)*yscale) + v_border);
			
			for (int index = 1; index < univarQuant.size(); index++) {
				dataLine.lineTo( xscale*(index) + h_border + max_str_len + barwidth/2.0, (canvasHeight - yscale* (univarQuant.get(index)-min_uni)) + v_border );
			};
		}			
		
		if( useBars ) {
			
			for (int index = 0; index < univarQuant.size(); index++) {
				
				dataRects.add( new Rectangle2D.Double( 	(xscale*(index) + h_border + max_str_len), 
														(canvasHeight - yscale* (univarQuant.get(index)-min_uni)) + v_border, 
														barwidth, 
														yscale* (univarQuant.get(index)-min_uni) ) );
			}
		}
		
		Line2D xAxis = new Line2D.Double( 	h_border + max_str_len, 
											this.getHeight()-v_border, 
											this.getWidth() - h_border, 
											this.getHeight()-v_border );
		Line2D yAxis = new Line2D.Double( 	h_border + max_str_len, 
											v_border, 
											h_border + max_str_len, 
											this.getHeight()-v_border);
		ArrayList<Line2D> xTicks = new ArrayList<Line2D>();
		for( int q : h_labelInts ) {
			
			xTicks.add( new Line2D.Double(	(xscale*(q) + h_border + max_str_len)+barwidth/2., (this.getHeight()-v_border) ,
											(xscale*(q) + h_border + max_str_len)+barwidth/2.,(this.getHeight()-v_border)+h_border) );
		}
		ArrayList<Line2D> yTicks = new ArrayList<Line2D>();
		double tick_span = (double)canvasHeight / (double)(vertical_ticks-1.);
		for( int i = 0; i < vertical_ticks; i++ ) {
		
			yTicks.add( new Line2D.Double(	max_str_len, (int)Math.round( (v_border + canvasHeight ) - (i * tick_span) ),
											max_str_len+h_border,(int)Math.round( (v_border + canvasHeight ) - (i * tick_span) )) );
		}
		
		// draw the axes

		g2D.setColor( axesColor );
		g2D.draw( xAxis );
		g2D.draw( yAxis );
		for( Line2D tick : xTicks ) {
			
			g2D.draw(tick);
		}
		for(Line2D tick: yTicks ) {
			
			g2D.draw(tick);
		}
		
		// label the axes
		
		if( labelAxes ) {
			
			for( int i = 0; i < vertical_ticks; i++ ) {
				
				g2D.drawString(	labelStrs[i], max_str_len - fm.stringWidth(labelStrs[i]) , 
								(int)Math.round( (v_border + canvasHeight ) - (i * tick_span) + fm.getHeight()/3. ));
			}
			int t = 0;
			for( int q : h_labelInts ) {
				
				if( h_labelStrs[t]!=null) {
					g2D.drawString(	h_labelStrs[t], 
									(int)Math.round(((xscale*(q) + h_border + max_str_len)+barwidth/2.) - fm.stringWidth(h_labelStrs[t])/2.) , 
									(this.getHeight()-v_border)+fm.getHeight() );				
				}
				t++;
			}
		}
		
		// draw the data

		if( useBars ) {
		
			if( isCutoffLeft ) {
				g2D.setColor( barColor );
				int k = 0;
				for( Rectangle2D rect : dataRects ) {
							
					if( k == cutoff ) {
						
						g2D.setColor( selbarColor );
					}
					if( k > cutoff ) {
						
						g2D.setColor( unselbarColor );
					}
					g2D.draw( rect );				
					g2D.fill( rect );
					k++;
				}
			}
			else {
				g2D.setColor( unselbarColor );
				int k = 0;
				for( Rectangle2D rect : dataRects ) {
					
					if( k == cutoff ) {
						
						g2D.setColor( selbarColor );
					}
					if( k > cutoff ) {
						
						g2D.setColor( barColor );
					}
					g2D.draw( rect );				
					g2D.fill( rect );
					k++;
				}
			}
			g2D.setColor( axesColor );
			if( barwidth > 4 ) {
				for( Rectangle2D rect : dataRects ) {
					g2D.draw( rect );				
				}
			}
		}
		
		
		g2D.setColor( lineColor ); 
		g2D.setStroke( new BasicStroke(2.f) );

		if( useLine ) {
			g2D.draw( dataLine );
		}
		g2D.setStroke( new BasicStroke(1.f) );
		
		double polyspot = (xscale*cutoff) + h_border + max_str_len + barwidth/2.0;
		
//		// draw the little selection marker
//		g2D.setColor( lineColor ); 
//		GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 3 ); 
//		gp.moveTo( polyspot, this.getHeight()-((double)v_border));
//		gp.lineTo(polyspot - v_border/2.0, this.getHeight()-2*v_border/3.0);
//		gp.lineTo(polyspot + v_border/2.0, this.getHeight()-2*v_border/3.0);
//		g2D.fill( gp );

		// draw the selection lines
		{
			g2D.setColor( new Color( 0xDF,0x3D,0x33) ); 
			g2D.setStroke( new BasicStroke(2.f) );
			
			// vertical line
			double y_rise = (canvasHeight - yscale* (univarQuant.get(cutoff)-min_uni)) + v_border;
			g2D.draw( new Line2D.Double( 	polyspot,
											this.getHeight()-v_border,
											polyspot,
											y_rise ) );
			
			// horizontal line
			g2D.draw( new Line2D.Double(	h_border + max_str_len,
											y_rise,
											polyspot,
											y_rise) );
			
//			// find the values of the labels
			double v_label_value = univarQuant.get(cutoff);
			int h_label_value = cutoff+1;

			// compute the label statistics
			String v_label_string = null;
			if( isLog ) {
				
				v_label_string = nf.format( Math.pow(10, v_label_value) );
			}
			else {

				v_label_string = nf.format(v_label_value);
			}
			String h_label_string = ""+h_label_value;
			
			// label the lines
			
			g2D.setColor( new Color( 0xDF,0x3D,0x33 ) );
			g2D.fill( new Rectangle2D.Double( 0,y_rise-fm.getAscent(),h_border + max_str_len,fm.getAscent()+fm.getDescent()) );
			g2D.fill( new Rectangle2D.Double( polyspot-2,this.getHeight()-(v_border-1),fm.stringWidth( h_label_string )+2,fm.getAscent()+fm.getDescent() ) );
			g2D.setColor( Color.WHITE ); 
			g2D.setStroke( new BasicStroke(1.f) );			
			g2D.drawString(	v_label_string, 0, (int)y_rise );
			g2D.drawString(	h_label_string, (int)polyspot, this.getHeight()-(v_border-1)+fm.getAscent() );
		}
		
		
		// draw the crosshairs
		if( this.cur_mouse_x >= 0 ) {

			g2D.setColor( new Color( 0x91,0x90,0x90, 128 ) ); 
			g2D.setStroke( new BasicStroke(2.f) );
			
			// vertical line
			g2D.draw( new Line2D.Double( 	cur_mouse_x,
											this.getHeight()-v_border,
											cur_mouse_x,
											v_border) );
			
			// horizontal line
			g2D.draw( new Line2D.Double(	h_border + max_str_len,
											cur_mouse_y,
											this.getWidth() - h_border,
											cur_mouse_y) );
			
			// find the values of the labels
			double v_label_value = (max_uni-min_uni)*(1.0 - ((double)(cur_mouse_y - (v_border))) / ((double)(this.getHeight()-2*v_border))) + min_uni;
			int h_label_value = (int) Math.floor( univarQuant.size()*((double)(cur_mouse_x - (h_border + max_str_len))) / ((double)(this.getWidth() - (2*h_border + max_str_len))) );
			cur_mouse_x = (int)(xscale*((int) Math.floor( univarQuant.size()*((double)(cur_mouse_x - (h_border + max_str_len))) / ((double)(this.getWidth() - (2*h_border + max_str_len))) ) ) + h_border + max_str_len + barwidth/2.0);
			// compute the label statistics
			String v_label_string = null;
			if( isLog ) {
				
				v_label_string = nf.format( Math.pow(10, v_label_value) );
			}
			else {

				v_label_string = nf.format(v_label_value);
			}
			String h_label_string = ""+(h_label_value+1);
			
			// label the lines
			
			g2D.setColor( new Color( 0xFE,0x97,0x29 ) );
			g2D.fill( new Rectangle2D.Double( 0,cur_mouse_y-fm.getAscent(),h_border + max_str_len,fm.getAscent()+fm.getDescent()) );
			if( useDimensionNames ) {
			
				g2D.fill( new Rectangle2D.Double( Math.min( cur_mouse_x, this.getWidth()-fm.stringWidth(univarNames.get( h_label_value ))-this.h_border),this.getHeight()-(v_border-1),fm.stringWidth( univarNames.get( h_label_value ) ),fm.getAscent()+fm.getDescent() ) );
			}
			else {
			
				g2D.fill( new Rectangle2D.Double( Math.min( cur_mouse_x, this.getWidth()-fm.stringWidth(""+(h_label_value+1))-this.h_border),this.getHeight()-(v_border-1),fm.stringWidth( ""+(h_label_value+1)),fm.getAscent()+fm.getDescent() ) );
			}
			g2D.setColor( Color.BLACK ); 
			g2D.setStroke( new BasicStroke(1.f) );			
			g2D.drawString(	v_label_string, 0, cur_mouse_y );
			if( useDimensionNames ) {
				
				g2D.drawString(	univarNames.get( h_label_value ), Math.min( cur_mouse_x, this.getWidth()-fm.stringWidth(univarNames.get( h_label_value ))-this.h_border), this.getHeight()-(v_border-1)+fm.getAscent() );
			}
			else {

				g2D.drawString(	""+(h_label_value+1), Math.min( cur_mouse_x, this.getWidth()-fm.stringWidth(""+(h_label_value+1))-this.h_border), this.getHeight()-(v_border-1)+fm.getAscent() );
			}
		}
	}
	
	public Dimension getPreferredSize( ) {
		
		return prefSize;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider)e.getSource();
	    if (!source.getValueIsAdjusting()) {
	        int val = (int)source.getValue();
	        this.cutoff = val-1;
		    this.repaint();
	    }	    		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		 
		
		// compute selection dimension based on mouse click
		// check the bounds
		if(	(e.getX() > h_border + max_str_len && e.getX() < this.getWidth() - h_border ) && 
			(e.getY() > v_border && e.getY() < this.getHeight() - v_border ) ) {
			
			//record the coords
			this.cur_mouse_x = e.getX();
			this.cur_mouse_y = e.getY();

			// choose new dimension
			this.cutoff = (int) Math.floor( univarQuant.size()*((double)(cur_mouse_x - (h_border + max_str_len))) / ((double)(this.getWidth() - (2*h_border + max_str_len))) );
			
			// tell everyone about it
			for( ChangeListener cl : changeListeners ) {
				
				cl.stateChanged( new ChangeEvent(this) );
			}
			
			// update the view
			repaint();
		}		

	}

	@Override
	public void mouseEntered(MouseEvent e) {

		mouseMoved(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {

		mouseMoved(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e) {

		mouseMoved(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		// check the bounds
		if(	(e.getX() > h_border + max_str_len && e.getX() < this.getWidth() - h_border ) && 
			(e.getY() > v_border && e.getY() < this.getHeight() - v_border ) ) {
			
			//record the coords
			this.cur_mouse_x = e.getX();
			this.cur_mouse_y = e.getY();
			repaint();
		}	
		else {
			
			this.cur_mouse_x=-1;
			this.cur_mouse_y=-1;
			repaint();
		}
	}

	/**
	 * Add a log state checkbox to this
	 * 
	 * @param jcb
	 */
	public void addLogStateCheckbox( JCheckBox jcb ) {
		
		if( jcb != null ) {
			
			jcb.addItemListener( this );
		}
	}
	
	public void switchLogState( boolean newLogState ) {
		
		if( newLogState != isLog ) {
			
			isLog = newLogState;			

			// transform the data to log10 scale
			if( !isLog ) {
				
				ArrayList<Double> univarQuantReal = new ArrayList<Double>();
				for( double d : univarQuant ) {
					
					if( d <= -8.) {
						
						univarQuantReal.add( 0. );
					}
					else {
						
						univarQuantReal.add( Math.pow( 10, d ) );
					}
				}			
				this.setUnivar(univarQuantReal, univarNames);
			}
			else {
				
				this.setUnivar(this.univarQuant, univarNames);
			}			
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		// we only listen to checkboxes that tell us about our log state
		if( e.getSource() instanceof JCheckBox ) {
			
			switchLogState( ((JCheckBox)e.getSource()).isSelected() ); 
		}
	}
	

}
