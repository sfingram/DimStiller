package still.gui;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class TornFrame extends Frame implements WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 239728989387336499L;
	public static int openFrameCount = 0;
	public static int xOffset = 10;
	public static int yOffset = 10;
	
	public OperatorView ov;
	
	public TornFrame( OperatorView ov ) {
		
		super();
		setTitle( ov.operator.toString() );
		setResizable(true);
		setLocation(300 + xOffset*openFrameCount, yOffset*openFrameCount);
		TornFrame.openFrameCount++;
		setSize(Math.max(300,ov.getWidth()),Math.max(200, ov.getHeight()));
		this.setLayout( new GridLayout(1,1) );
		this.add(ov);
		this.setVisible(true);
		this.addWindowListener(this);
		ov.isTorn = true;
		this.ov = ov;
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {

		ov.isTorn = false;
		this.dispose();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

}
