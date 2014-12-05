package still.gui;

import still.data.Operator;
import still.data.Table;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class ScatterView  extends Operator implements WindowListener {

	public ScatterView(Table newInput) {
		super(newInput);
		// TODO Auto-generated constructor stub
	}

    @Override
    public void activate() {

    }

    @Override
    public String getSaveString() {
        return null;
    }

    @Override
    public void updateMap() {

    }

    @Override
    public void updateFunction() {

    }

	public void columnAdded(int dim) {
		// TODO Auto-generated method stub
		
	}

	public void columnChanged(int dim) {
		// TODO Auto-generated method stub
		
	}

	public void featureChanged(int point_idx, int dim) {
		// TODO Auto-generated method stub
		
	}

	public void featuresChanged(int[][] indices) {
		// TODO Auto-generated method stub
		
	}

	public void rowAdded(int point_idx) {
		// TODO Auto-generated method stub
		
	}

	public void rowChanged(int point_idx) {
		// TODO Auto-generated method stub
		
	}

	public void tableChanged() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void windowOpened(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {

    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {

    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {

    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {

    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {

    }
}
