package still.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import processing.core.PApplet;
import still.data.Operator;
import still.data.Table.ColType;

public abstract class OPApplet extends PApplet implements ActionListener, ComponentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5668530068126327015L;
	Operator op = null;
	public ArrayList<Integer> numerics = null;
	int num_numerics = -1;
	public boolean finished_setup = false;
	
	void countNumerics( ) {
		
		this.num_numerics = 0;
		for( ColType type : this.getOp().getColTypes() ) {
			
			if( type == ColType.NUMERIC ) {
				
				this.num_numerics++;
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


	public OPApplet( Operator op ) {
		super();
		this.op = op;
		op.addActionListener(this);
//		addComponentListener(this);
	}
	
	public Operator getOp( ) {
		return op;
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {


//		System.out.println(""+e.getSource().getClass().getName());
	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
				redraw();
	        }
	      });
	}
	
	public void componentHidden(ComponentEvent e) {
	
	}
	
	public void componentMoved(ComponentEvent e) {

	}
	
	public void componentResized(ComponentEvent e) {

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	          // Set the preferred size so that the layout managers can handle it
				redraw();
	        }
	      });
	}

	public void componentShown(ComponentEvent e)  {
	}
	
	public Dimension graphSize() {
		
		return new Dimension(1000,1000);
	}
}
