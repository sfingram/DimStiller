package still.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import still.data.Operator;

public abstract class OperatorView extends JPanel implements ActionListener, ComponentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4170792436892412503L;
	protected Operator operator = null;
	protected ViewFrameAlt vframe = null;
	public int last_vf_loc_x = -1;
	public int last_vf_loc_y = -1;
	public boolean isTorn = false;
	
	public ViewFrameAlt getViewFrame() {
		
		return vframe;
	}
	
	public OperatorView( Operator o ) {
		
		super();		
		
		operator = o;
		
		this.setLayout(new BorderLayout(10,10) );
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		

		last_vf_loc_x = (int)vframe.getLocation().getX();
		last_vf_loc_y = (int)vframe.getLocation().getY();
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}
