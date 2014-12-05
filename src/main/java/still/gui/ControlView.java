package still.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import still.data.Operator;
import still.data.Table;
import still.expression.Expression;

public class ControlView extends JPanel implements TreeSelectionListener, ActionListener, WindowListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5597742698572535842L;

	Expression e = null;
	JPanel inner_panel = null;
	JButton tear_button = null;
	ArrayList<Frame> torn_controls = null;
		
	public ControlView() {
		
		super();
		
		torn_controls = new ArrayList<Frame>();
		setLayout(new BorderLayout() );

		inner_panel = new JPanel();		
		inner_panel.setLayout(new GridLayout(1,1));
		inner_panel.add(new JPanel());
		inner_panel.setBorder(BorderFactory.createLineBorder(Color.black));		
		add(inner_panel, BorderLayout.CENTER);

		tear_button = new JButton("Tear Off");
		tear_button.setActionCommand("TEAR");
		tear_button.addActionListener(this);
		tear_button.setEnabled( false );
		add( tear_button, BorderLayout.SOUTH );
		
	}
	
	public void setExpression( Expression e ) {
		
		this.e = e;
		
		inner_panel.removeAll();		
		inner_panel.validate();
	}
	
	public void setOperator( Table o ) {
		
		if(o != null && this.e != null) {
			
			if( o instanceof Operator) {

				inner_panel.removeAll();

				if( 	((Operator)o).isActive() && 
						((Operator)o).getView() != null && 
						!((Operator)o).getView().isTorn ) {
					
					inner_panel.add( ((Operator)o).getView() );
					tear_button.setEnabled( true );
				}

				inner_panel.validate();
				inner_panel.repaint();
			}
			else {
				
				inner_panel.removeAll();
				if( this.e.table.hasInputControl()){

					inner_panel.add( this.e.table.getInputControl() );
					tear_button.setEnabled( false );
				}
				inner_panel.validate();
				inner_panel.repaint();
			}
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		// find the expression selected (if different)
		this.setExpression( ExpressionTreeView.getTreeExpression(e));
		this.setOperator(ExpressionTreeView.getTreeOperator(e));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if( e.getActionCommand().equalsIgnoreCase("TEAR") ) {
			
			if( inner_panel.getComponentCount() > 0 &&
				inner_panel.getComponent(0) instanceof OperatorView ) {

				OperatorView ov = (OperatorView) inner_panel.getComponent(0);
				TornFrame tf = new TornFrame(ov);
				tf.addWindowListener(this);
				this.torn_controls.add(tf);
				
				inner_panel.removeAll();		
				inner_panel.repaint();
				
				tear_button.setEnabled(false);
			}
		}
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		
		if( inner_panel.getComponentCount() == 0 && this.e != null ) {
			
			if( this.e.operators.contains(((TornFrame) e.getSource()).ov.operator)) {
				
				inner_panel.add( ((TornFrame) e.getSource()).ov );
				tear_button.setEnabled( true );			
				inner_panel.validate();
				inner_panel.repaint();
			}
		}		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}
