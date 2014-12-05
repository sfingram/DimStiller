package still.flow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import still.data.Operator;
import still.data.OperatorFactory;
import still.data.Table;
import still.data.TableFactory;
import still.expression.Expression;
import still.gui.DimStiller;
import still.gui.ExpressionTreeView;

public class WorkflowPanel extends JPanel implements ActionListener, ListSelectionListener, TreeSelectionListener {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7922903180801478147L;

	public JList workflowList = null;
	public JList stepList = null;
	public JScrollPane workflowListScroll = null;
	public JScrollPane stepListScroll = null;
	public JPanel listPanel = null;
	public JPanel buttonPanel = null;
	public JButton prevButton = null;
	public JButton nextButton = null;
	public JButton addButton = null;
	public JButton applyButton = null;
	public Workflow currentFlow = null;
	public Expression expression = null;
	public Expression selExpression = null;
	public Table selTable = null;
	public DimStiller ds = null;
	
	public WorkflowPanel( 	DimStiller ds ) {
		
		super( new BorderLayout(5,5) );
		
		DefaultListModel listModel = new DefaultListModel();
		this.ds = ds;

		for( String workflow_name :ds.wkFactory.workflow_names ) {
			
			listModel.addElement( workflow_name );
		}
		
		workflowList = new JList( listModel );
		workflowList.addListSelectionListener(this);
		workflowList.setVisibleRowCount(4);
		workflowListScroll = new JScrollPane(workflowList);
		listPanel = new JPanel( new BorderLayout(5,5) );
		listPanel.add(workflowListScroll, BorderLayout.CENTER);
		listPanel.add(new JLabel("Workflows"), BorderLayout.NORTH);
		listPanel.add(new JSeparator( JSeparator.VERTICAL), BorderLayout.EAST);
		this.add( listPanel, BorderLayout.WEST );
		
		stepList = new JList();
		stepList.setVisibleRowCount(5);
		stepListScroll = new JScrollPane(stepList);
		JPanel tempPanel = new JPanel( new BorderLayout(5,5));
		tempPanel.add(new JLabel("Steps"), BorderLayout.NORTH);
		tempPanel.add(stepListScroll, BorderLayout.CENTER);
		this.add( tempPanel, BorderLayout.CENTER);
		stepList.setEnabled(true);
		
		addButton = new JButton("Add");
		addButton.addActionListener(this);
		applyButton = new JButton("Apply");
		applyButton.addActionListener(this);
		JPanel sideButtonPanel = new JPanel();
		sideButtonPanel.setLayout( new GridLayout(2,1) );
		sideButtonPanel.add(addButton);
		sideButtonPanel.add(applyButton);
		tempPanel.add(sideButtonPanel, BorderLayout.EAST);
		
//		this.setWorkflowEnbled( true );
		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
	}

	public void setWorkflowEnbled( boolean isTop ) {

		addButton.setEnabled(!isTop);
		applyButton.setEnabled(!isTop);
		stepList.setEnabled(!isTop);
		
		workflowList.setEnabled(isTop);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if( e.getSource() == addButton ) {
			if( 	stepList.getModel() != null && 
					stepList.getModel().getSize() > 0 && 
					selExpression != null ) {
			
				// add workflow steps to the existing operators
				Workflow w = ds.wkFactory.workflows.get( workflowList.getSelectedIndex() );
				
				// create a new expression
				Table t = TableFactory.csvLoader( this );
				if( t != null ) {
					
					Operator op = ds.opFactory.makeOperator( w.operators.get(0), t, true);
					for( int i = 1; i < w.operators.size(); i++ ) {
						
						op = ds.opFactory.makeOperator( w.operators.get(i), op, false);
					}
					Expression ex = new Expression( op );

					ds.insertNewExpression( ex );
				}			
			}
		}
		if( e.getSource() == applyButton ) {
			if( 	stepList.getModel() != null && 
					stepList.getModel().getSize() > 0 && 
					selExpression != null ) {
			
				// add workflow steps to the existing operators
				Workflow w = ds.wkFactory.workflows.get( workflowList.getSelectedIndex() );
				
				boolean first_operator = true; // only activate the first operator (if such a thing is possible) 
				
				for( String operator : w.operators ) {
				
					Operator op = null;
					
					if( selExpression.operators.size() <= 0 ) {
						
						op = ds.opFactory.makeOperator( 	operator, 
								selExpression.table, 
								first_operator);
					}
					else {

						// determine possibility of activating the first workflow operator 
						first_operator = first_operator && selExpression.operators.get(selExpression.operators.size()-1).isActive();

						op = ds.opFactory.makeOperator( 	operator, 
								selExpression.operators.get(selExpression.operators.size()-1), 
								first_operator);
					}

					first_operator = false;
					
					selExpression.addOperator(op);
				}
			}
		}
	}
	
	public static void main( String[] args ) {
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
            	JFrame frame = new JFrame("Workflow Demo");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                
                //Add content to the window.
//                frame.add(new WorkflowPanel(), BorderLayout.CENTER);
                
                //Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        } );
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {


		if( e.getSource() == workflowList ) {
			if( ! e.getValueIsAdjusting() ) {
				
				currentFlow = ds.wkFactory.workflows.get(workflowList.getSelectedIndex());
				DefaultListModel listModel = new DefaultListModel();
				for( int i = 0; i < currentFlow.menu_names.size(); i++ ) {
					
					listModel.addElement(currentFlow.menu_names.get(i));
				}
				stepList.setModel(listModel);
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		selExpression 	= ExpressionTreeView.getTreeExpression(e);
		selTable 		= ExpressionTreeView.getTreeOperator(e);		
	}
}
