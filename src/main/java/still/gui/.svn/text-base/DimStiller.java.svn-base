package still.gui;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;


import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import still.data.Operator;
import still.data.OperatorFactory;
import still.data.ProcTableFactory;
import still.data.Table;
import still.data.TableFactory;
import still.expression.Expression;
import still.expression.ExpressionEvent;
import still.expression.ExpressionListener;
import still.flow.Workflow;
import still.flow.WorkflowFactory;
import still.flow.WorkflowPanel;
//import still.operators.ColorOp;
//import still.operators.NormalizeOp;
//import still.operators.CutoffOp;
//import still.operators.HistogramOp;
//import still.operators.IdentityOp;
//import still.operators.PCAOp;
//import still.operators.PearsonCollectOp;
//import still.operators.SplomOp;

public class DimStiller extends JFrame implements 	ActionListener,
													ExpressionListener,
													TreeSelectionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3987875783080457860L;
	private static final String wkflMenuPrefix = ":WORKFLOW:";
	
	/*
	 *  MENUS
	 */
	JMenuBar menuBar 				= null;
	JMenu fileMenu					= null;
	JMenuItem newMenuItem			= null;
	JMenuItem newProcMenuItem		= null;
	JMenuItem openMenuItem			= null;
	JMenuItem saveMenuItem			= null;
	JMenuItem saveWkMenuItem		= null;
	JMenuItem saveTabMenuItem		= null;
	JMenuItem quitMenuItem			= null;
	JMenu workflowMenu				= null;
	JMenuItem[] workflowMenus		= null;
	JMenu operatorMenu				= null;
	JMenuItem[] operatorMenus		= null;
	
	Expression selExpr				= null;
	public ArrayList<Expression> es 		= null;
	public ExpressionTreeView eTreePanel 	= null;
	JPanel sidePanel				= null;
	WorkflowPanel workflowPanel		= null;
	JFrame intFrame					= null;
	ControlView controlView			= null;
	JSplitPane splitPane			= null;
	public OperatorFactory opFactory		= null;
	public WorkflowFactory wkFactory		= null;
	
//	JDesktopPane viewPane			= null;
	ArrayList<ViewFrameAlt> vframes	= null;
	
	public static final String VERSION_STRING = "0.2"; 
	
	public void insertNewExpression( Expression ex ) {
		
		es.add( ex );
		ex.addExpressionListener( eTreePanel );
		ex.initExpression();
	}
	
	public DimStiller( ) {
		this(null);
	}
	
	public DimStiller( DSArgs ds_args ) {
		
		super("DimStiller");
		
		if( ds_args.is_error ) {
			
			System.exit(0);
		}
		
		// construct an relevant factories
		
		opFactory = new OperatorFactory( ds_args.plugin_dirs );
		wkFactory = new WorkflowFactory( ds_args.wkflow_dirs );

		// load up a sample file
		
    	Expression[] es_in = null;

		if( ds_args.input_file == null ) {
			
//			System.out.println( "No Input file specified.  Use -I filename on command line." );
//			System.exit(0);
		}
		else {

		
	    	Table table = null;
	    	if( !TableFactory.hasTypes(ds_args.input_file, "[\\t\\n\\x0B\\f\\r,]+")) {
	    		
	    		table = TableFactory.fromCSV( ds_args.input_file, ds_args.skiplines );
	    	}
	    	else {
	    		
	    		table = TableFactory.fromCSVTypes( ds_args.input_file, ds_args.skiplines, true );
	    	}
	    	
	        es_in = new Expression[ 1 ];
	        for( int i = 0; i < es_in.length; i++ ) {
	    		es_in[i] = 	new Expression( table );
	    		es_in[i].locationString = ds_args.input_file;
	        }
		}
        		
		int inset = 50;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds( inset, inset, screenSize.width - inset*2, screenSize.height - inset*2);
		
		this.addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) { System.exit( 0 ); }
		});
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        es = new ArrayList<Expression>();
        
        if( es_in != null ) {
	        for( Expression e : es_in ) {
	        	
	        	e.addExpressionListener( this );
	        	es.add( e );
	        }
        }        
		// create
		
		eTreePanel 		= new ExpressionTreeView( es );
		sidePanel  		= new JPanel();
		workflowPanel 	= new WorkflowPanel( this );
		controlView		= new ControlView();
		
		eTreePanel.tree.addTreeSelectionListener(controlView);
		eTreePanel.tree.addTreeSelectionListener(workflowPanel);
		eTreePanel.tree.addTreeSelectionListener(this);
		eTreePanel.setPreferredSize( new Dimension(600,200));
		eTreePanel.reSelect();
		
		// lay out
		
		this.getContentPane().setLayout(new BorderLayout(5,5));
		
		this.getContentPane().add( sidePanel, "Center");
		sidePanel.setLayout(new BorderLayout(5,5));
		sidePanel.add(workflowPanel,"North");
		JPanel tempGridPanel = new JPanel();
		tempGridPanel.setLayout( new GridLayout(2,1,5,5));
		tempGridPanel.add(eTreePanel);
		tempGridPanel.add(controlView);
		sidePanel.add(tempGridPanel,"Center");
		sidePanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		// set up the menus
		
		menuBar 			= new JMenuBar();
		fileMenu			= new JMenu("File");
		newMenuItem			= new JMenuItem("New Expression...");
		newProcMenuItem		= new JMenuItem("New Procedural Expression");
		openMenuItem		= new JMenuItem("Open Expression...");
		saveMenuItem		= new JMenuItem("Save Expression...");
		saveWkMenuItem		= new JMenuItem("Save Expression as Workflow...");
		saveTabMenuItem		= new JMenuItem("Save Operator to File...");
		quitMenuItem		= new JMenuItem("Quit");
		workflowMenu		= new JMenu("Workflow");
		operatorMenu		= new JMenu("Operators");
		
		operatorMenus = new JMenuItem[ opFactory.menu_names.size() ];
		for( int i = 0; i < opFactory.menu_names.size(); i++ ) {
			
			operatorMenus[i] = new JMenuItem( opFactory.menu_names.get(i) );
			operatorMenus[i].setActionCommand( opFactory.classes.get(i) );
		}

		workflowMenus = new JMenuItem[ wkFactory.workflow_names.size() ];
		for( int i = 0; i < wkFactory.workflow_names.size(); i++ ) {
			
			workflowMenus[i] = new JMenuItem( wkFactory.workflow_names.get(i) );
			workflowMenus[i].setActionCommand( wkflMenuPrefix + wkFactory.workflow_names.get(i) );
		}
				
		menuBar.add(fileMenu);
		menuBar.add(workflowMenu);
		menuBar.add(operatorMenu);
		fileMenu.add(newMenuItem);
		fileMenu.add(newProcMenuItem);
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveWkMenuItem);
		fileMenu.add(saveTabMenuItem);
		fileMenu.add(quitMenuItem);
		
		for( JMenuItem opMenu : operatorMenus ) {
			
			operatorMenu.add(opMenu);
			opMenu.addActionListener(this);
		}
		
		for( JMenuItem wfMenu : workflowMenus ) {
			
			workflowMenu.add(wfMenu);
			wfMenu.addActionListener(this);
		}
		
		newMenuItem.addActionListener(this);
		newProcMenuItem.addActionListener(this);
		openMenuItem.addActionListener(this);
		saveMenuItem.addActionListener(this);
		saveWkMenuItem.addActionListener(this);
		quitMenuItem.addActionListener(this);
		
		this.setJMenuBar(menuBar);
		
		vframes = new ArrayList<ViewFrameAlt>();
		
        this.pack();
        this.setVisible(true);
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if( e.getSource() == newMenuItem ) {

			// create a new expression			
			Table t = TableFactory.csvLoader( this.getContentPane() );
			if( t != null ) {
				
				Expression ex = new Expression( t );
				ex.locationString = TableFactory.lastFName;
				this.es.add( ex );
				ex.addExpressionListener( this.eTreePanel );
				ex.initExpression();
			}			
		}
		else if( e.getSource() == newProcMenuItem ) {

			// create a new expression			
			Table t = ProcTableFactory.procTable( this.getContentPane() );
			if( t != null ) {
				
				Expression ex = new Expression( t );
				//ex.locationString = TableFactory.lastFName;
				this.es.add( ex );
				ex.addExpressionListener( this.eTreePanel );
				ex.initExpression();
			}			
		}
		else if( e.getSource() == openMenuItem ) {
			
			JFileChooser fc = new JFileChooser( );
			int returnVal = fc.showOpenDialog( this );
			if( returnVal == JFileChooser.APPROVE_OPTION ) {
				
				Expression ex = Expression.loadExpression( fc.getSelectedFile(), opFactory );
				this.es.add( ex );
				ex.addExpressionListener( this.eTreePanel );
				ex.initExpression();
			}
		}
		else if( e.getSource() == saveMenuItem ) {
			
			if( eTreePanel.selExpr != null ) {
				
				JFileChooser fc = new JFileChooser( );
				int returnVal = fc.showSaveDialog( this );
				if( returnVal == JFileChooser.APPROVE_OPTION ) {
					
					Expression.saveExpression(eTreePanel.selExpr, fc.getSelectedFile() );
				}
			}
		}
		else if( e.getSource() == saveWkMenuItem ) {

			if( selExpr != null ) {
				
				wkFactory.saveWorkflow( selExpr );
			}
		}
		else if( e.getSource() == saveTabMenuItem ) {

			if( eTreePanel.selTable != null ) {
				
				JFileChooser fc = new JFileChooser( );
				int returnVal = fc.showSaveDialog( this );
				if( returnVal == JFileChooser.APPROVE_OPTION ) {

					Operator.writeOpToFile( eTreePanel.selTable, fc.getSelectedFile() );
				}
			}
		}
		else if( e.getSource() == quitMenuItem ) {

			System.exit(0);
		}
		else if( opFactory.classes.contains( e.getActionCommand() ) ) {

			// add 
			if( eTreePanel.selTable != null ) {

				this.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
				Operator op = opFactory.makeOperator( e.getActionCommand(), eTreePanel.selTable, true);
				selExpr.addOperator(op, eTreePanel.selTable);
				this.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
			}
		}
		else if( e.getActionCommand().length() > wkflMenuPrefix.length() &&
					wkFactory.workflow_names.contains( e.getActionCommand().substring(wkflMenuPrefix.length()) ) ) {

			// create a new expression
			Workflow w = wkFactory.getWorkflow( e.getActionCommand().substring(wkflMenuPrefix.length()) );
			
			// create a new expression
			Table t = TableFactory.csvLoader( this.getContentPane() );
			if( t != null ) {
				

				Operator op = opFactory.makeOperator( w.operators.get(0), t, true);
				for( int i = 1; i < w.operators.size(); i++ ) {
					
					op = opFactory.makeOperator( w.operators.get(i), op, false);
				}
				Expression ex = new Expression( op );
				insertNewExpression(ex);
			}			
		}
	}
		
	public static void main( final String[] args ) {
				
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	
            	DimStiller dStiller = new DimStiller( new DSArgs( args ) );

            }

        } );
	}

	/**
	 * Checks if there are any active operators with views that aren't visible
	 * 
	 */
	public void updateViews( ) {
		
		for( Expression e : this.es ) {
			
			for( Operator o : e.operators ) {
				
				if( o.isActive() && o.getView() != null && o.getView().getViewFrame() != null ) {
					
					if( vframes.indexOf( o.getView().getViewFrame() ) < 0 ) {
						
						o.getView().getViewFrame().setVisible(true);
						vframes.add( o.getView().getViewFrame() );
					}
				}
			}
		}
	}
	
	public void setExpression( Expression e ) {

		updateViews();
		
//		// check if this is a new expression
//		if( e == selExpr ) {
//			
//			return;
//		}
//		
//		// close existing view frames
//		if( selExpr != null && vframes.size() > 0 ) {
//			
//			for( ViewFrameAlt vf : vframes ) {
//				
////				vf.dispose();
//				vf.setVisible(false);
////				viewPane.remove(vf);
//			}
//			vframes.clear();
//		}
//		
//		
//		if( e != null ) {
//		
//			for( Operator o: e.operators ) {
//
//				if( o.isActive() ) {
//					ViewFrameAlt vf = o.getView().getViewFrame();
//					if( vf != null ) {
//						
//						vframes.add(vf);
//						vf.setVisible(true);
//	//					viewPane.add( vf );
//						if( 	o.getView().last_vf_loc_x > 0 && 	o.getView().last_vf_loc_y > 0 ) {
//							vf.setLocation(	o.getView().last_vf_loc_x, 
//											o.getView().last_vf_loc_y );
//						}
//					}
//				}
//			}
//		}

		// mark as the selected expression
		selExpr = e;
		
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
		if( e != null ) {
			
			if( e.getPath().getLastPathComponent() instanceof DefaultMutableTreeNode ) {
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
				if( node.getUserObject() instanceof ExpressionTreeView.StillNode ) {
					
					if( ((ExpressionTreeView.StillNode)node.getUserObject()).op == null && 
							((ExpressionTreeView.StillNode)node.getUserObject()).table == null ) {
						
						// null out the operators menu
						if( operatorMenu != null )
							operatorMenu.setEnabled(false);
					}
					else {
						
						// enable the operators menu
						if( operatorMenu != null )
							operatorMenu.setEnabled(true);
					}
				}
			}
		}
		this.setExpression( ExpressionTreeView.getTreeExpression(e));
	}

	@Override
	public void expressionChanged(ExpressionEvent e) {

		if( e.command == ExpressionEvent.ExpressionEventType.TERM_ACTIVATED ) {
			
			// update the views
			updateViews();
		}
	}

}
