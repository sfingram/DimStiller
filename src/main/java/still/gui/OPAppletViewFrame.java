package still.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.ScrollPane;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import processing.core.PApplet;

public class OPAppletViewFrame extends ViewFrameAlt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20468039754356861L;

	public OPApplet procApp = null;
	
	public static int MINIMUM_VIEW_WIDTH = 500;
	public static int MINIMUM_VIEW_HEIGHT = 500;
	
	public boolean hasScroll = false;
	
	public OPAppletViewFrame(String s ) {
		
		super(s);
	}
	
	public void setScroll( boolean hasScroll ) {
		
		if( this.hasScroll == hasScroll ) {
			
			return;
		}
		
		this.hasScroll = hasScroll;
		if( hasScroll ) {

			this.remove(procApp);
			ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
			sp.add(procApp);
			this.add( sp,"Center");
			this.validate();
			this.repaint();
		}
		else {
			
			this.removeAll();
			this.add( procApp,"Center");
			procApp.invalidate();
			this.validate();
			this.repaint();
		}				
	}
	
	public OPAppletViewFrame( String s, OPApplet pa ) {
		
		super(s);
		setLayout( new BorderLayout());
		//this.add( new BottomBuffer() , "South");
				
		procApp = pa;
		
//		ScrollPane sp = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
//		sp.add(procApp);
		
		this.add( procApp,"Center");
//		this.add( sp,"Center");
		
		procApp.init();
		
		while( !procApp.finished_setup ) {
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		this.addComponentListener(procApp);
		pack();
	}
	
//	public void paintComponent( Graphics g ) {
//		
//		super.paintComponent(g);
//		procApp.redraw();
//	}

//	public class BottomBuffer extends JPanel {
//		
//		public Dimension getPreferredSize( ) {
//			
//			return new Dimension(300,25);
//		}
//	}
}
