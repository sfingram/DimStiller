package still.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import still.data.DimensionDescriptor;
import still.data.Operator;
import still.data.Table;
import still.data.TableEvent;
import still.data.TableFactory;
import still.data.TableListener;
import still.expression.Expression;
import still.expression.ExpressionEvent;
import still.expression.ExpressionListener;
import still.operators.CutoffOp;
import still.operators.PCAOp;
import still.operators.PearsonCollectOp;

public class ExpressionTreeView extends JPanel implements 	ActionListener, 
															ExpressionListener,
															TreeSelectionListener,
															TreeModelListener,
															TableListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8513944058034774182L;
	public JTree tree = null;
	
	public Expression selExpr  	= null;
	public Table selTable 		= null;
	public int selDim 			= -1;
	JButton removeOpButton 		= null;
	JButton stepOpButton 		= null;
	ArrayList<ActionListener> actionListeners = null;
	NumberFormat nf = NumberFormat.getInstance();
	
	public void addActionListener( ActionListener al ) {
		
		actionListeners.add( al );
	}
	
	public ExpressionTreeView( ArrayList<Expression> es ) {
		
		super( new BorderLayout() );
		
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(0);
		
		actionListeners = new ArrayList<ActionListener>();
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode( "Expressions" );
		DefaultMutableTreeNode first_exp = null;		
		
		if( es != null ) {
		
			for( int i = 0; i < es.size(); i++ ) {
				
				es.get(i).addExpressionListener(this);
				
				DefaultMutableTreeNode expr_node = new DefaultMutableTreeNode( new StillNode(es.get(i),i+1));
				DefaultMutableTreeNode input_node = new DefaultMutableTreeNode( new StillNode(es.get(i).table));
				if( input_node != null && first_exp == null ) {
					
					first_exp = input_node;
				}
				
				es.get(i).table.addActionListener(this);
				
				expr_node.add(input_node);
				int opnum = 0;
				for( Operator op :  es.get(i).operators ) {
					
					opnum++;
					
					op.setExpressionNumber(i+1);
					op.setTermNumber(opnum);
					
					DefaultMutableTreeNode op_node = new DefaultMutableTreeNode(new StillNode(op,opnum));
					expr_node.add(op_node);
					op.addActionListener(this);
					op.addTableListener(this);
					ArrayList<DimensionDescriptor> descriptors = op.getConstructedDimensions();
					if( descriptors != null ) {
						for( DimensionDescriptor dd : descriptors ) {
							
							DefaultMutableTreeNode dd_node = new DefaultMutableTreeNode( dd );
							op_node.add( dd_node );
							if( dd.sub_values != null ) {
								
								for( String s : dd.sub_values ) {
									
									DefaultMutableTreeNode sv_node = new DefaultMutableTreeNode( s );
									dd_node.add( sv_node );
								}
							}
						}
					}
				}
				top.add( expr_node );
			}
		}
		
		tree = new JTree( top );
		tree.setCellRenderer( new ExpressionTreeCellRenderer() );
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		tree.addTreeSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(tree);
		this.add(scrollPane, BorderLayout.CENTER);
		
		// add buttons
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new GridLayout(0,3,5,5));
		removeOpButton = new JButton("Remove Operator");
		stepOpButton = new JButton("Step Operator");
		buttonPanel.add( removeOpButton );
		buttonPanel.add( stepOpButton );
		removeOpButton.addActionListener( this );
		stepOpButton.addActionListener(this);
		this.add(buttonPanel,BorderLayout.SOUTH);
		tree.getModel().addTreeModelListener(this);
		
		// if there is an expression to load, then set the first to be selected
		
		if( first_exp == null)  {
						
			tree.setSelectionPath( new TreePath(top.getPath()) );
		}
		else {
			
			tree.setSelectionPath( new TreePath(first_exp.getPath()) );
		}
	}

	/**
	 * 
	 * Re-fires a selection event for the selected nodes.
	 */
	public void reSelect() {
		
		TreePath path = tree.getSelectionPath();
		tree.setSelectionPath( null );
		tree.setSelectionPath( path );
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if( e.getSource() == stepOpButton ) {
		
			if( selExpr != null ) {
				
				this.setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
				selExpr.activateNextOperator();
				this.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
			}			
		}
		if( e.getSource() == removeOpButton ) {
			
			
			if( selTable != null && selTable instanceof Operator ) {
				
				selExpr.delOperator((Operator)selTable);
			}
		}
		
		if( e.getSource() instanceof Operator || e.getSource() instanceof Table ) {
			
			DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());			
			model.nodeChanged(findOpNodes((Table)e.getSource()));
		}				
	}


	/**
	 * Find a node in a tree given that it is an operator or table
	 * 
	 * @param t
	 * @return
	 */
	public DefaultMutableTreeNode findOpNodes( Table t ) {
		
		DefaultMutableTreeNode node = null;
		
		// get the appropriate expression node
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		int expression_count = tree.getModel().getChildCount(root);
		if( expression_count < 1) {
			return null;
		}
		for( int i = 0; i < expression_count; i++ ) {
			
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) tree.getModel().getChild(root, i);
			for( int j = 0; j < child.getChildCount(); j++ ) {

				DefaultMutableTreeNode gchild = (DefaultMutableTreeNode) child.getChildAt(j);
				if( ( (StillNode) gchild.getUserObject()).table == t  ||
				    ( (StillNode) gchild.getUserObject()).op == t  ) {
					
			    	return gchild;
				}
			}
		}
		
		return node;
	}

	/**
	 * Find a node in a tree given that it is an expression
	 * 
	 * @param e
	 * @return
	 */
	public DefaultMutableTreeNode findExNode( Expression e ) {
		
		DefaultMutableTreeNode node = null;
		
		// get the appropriate expression node
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree.getModel().getRoot();
		int expression_count = tree.getModel().getChildCount(root);
		if( expression_count < 1) {
		
			return null;
		}
		
		for( int i = 0; i < expression_count; i++ ) {
			
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) tree.getModel().getChild(root, i);
			if( ( (StillNode) child.getUserObject()).ex == e ) {
			
				return child;
			}
		}
		
		return node;
	}

	/**
	 * Renumber the expressions and update the tree strings
	 */
	public void renumberExpressions( ) {
		
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot();
		Enumeration enumeration = root.children();
		int opnum = 0;
		while( enumeration.hasMoreElements() ) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
			if(((StillNode)node.getUserObject()).ex != null) {

				opnum++;
				((StillNode)node.getUserObject()).opnum = opnum;
			}
		}
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(root);
	}
	
	/**
	 * Renumber the operators and update the tree strings
	 * 
	 * @param e
	 */
	public void renumberOps( Expression e ) {
		
		DefaultMutableTreeNode child = findExNode( e );
		Enumeration enumeration = child.children();
		int opnum = 0;
		while( enumeration.hasMoreElements() ) {
			
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)enumeration.nextElement();
			if(((StillNode)node.getUserObject()).op != null) {

				opnum++;
				((StillNode)node.getUserObject()).opnum = opnum;
			}
		}
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(child);
	}

	@Override
	public void expressionChanged(ExpressionEvent e) {

		if( e.command == ExpressionEvent.ExpressionEventType.TERM_ADDED ) {

			DefaultMutableTreeNode child = findExNode( (Expression) e.getSource() );
			if( child != null ) {
				
				int opnum = 0;
				DefaultMutableTreeNode op_node = null;
				for( Operator op :  ((Expression) e.getSource()).operators ) {
					
					opnum++;
					if( op == e.getTerm() ) {
	
						op.addActionListener(this);
						op.addTableListener(this);
						op.setTermNumber(opnum);
						op.setExpressionNumber( ((StillNode)child.getUserObject()).exnum );
						op_node = new DefaultMutableTreeNode(new StillNode(op, opnum));
						if( op.isActive() ) {

							ArrayList<DimensionDescriptor> descriptors = op.getConstructedDimensions();
							if( descriptors != null ) {
								for( DimensionDescriptor dd : descriptors ) {
									
									DefaultMutableTreeNode dd_node = new DefaultMutableTreeNode( dd );
									op_node.add( dd_node );
									if( dd.sub_values != null ) {
										
										for( String s : dd.sub_values ) {
											
											DefaultMutableTreeNode sv_node = new DefaultMutableTreeNode( s );
											dd_node.add( sv_node );
										}
									}
								}
							}
						}
						break;
					}
				}
				if( op_node != null ) {
				
					((DefaultTreeModel)tree.getModel()).insertNodeInto(op_node, child, opnum);
					renumberOps( (Expression) e.getSource() );
					tree.setSelectionPath( new TreePath( new Object[] { tree.getModel().getRoot(), child, op_node } ) );
				
				}				
			}
		}	
		else if( e.command == ExpressionEvent.ExpressionEventType.TERM_ACTIVATED || 
				e.command == ExpressionEvent.ExpressionEventType.TERM_CHANGED ) {
			// repaint everything
			DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());
			model.nodeChanged(findOpNodes( e.term ));
		}
		else if( e.command == ExpressionEvent.ExpressionEventType.NEW_INPUT ) {

			// repaint everything
			DefaultTreeModel model = ((DefaultTreeModel)tree.getModel());
			DefaultMutableTreeNode treeNode = findOpNodes( e.additionalTerm );
			StillNode stillNode = ((StillNode)treeNode.getUserObject());
			stillNode.table = e.term;
			model.nodeChanged( findOpNodes( e.term ));
			if( selTable == e.additionalTerm ) {
				
				selTable = e.term;
			}
		}
		else if( e.command == ExpressionEvent.ExpressionEventType.TERM_REMOVED ) { 
			
			DefaultMutableTreeNode child = findOpNodes( e.getTerm() );
			if( child != null ) {
				
				((DefaultTreeModel)tree.getModel()).removeNodeFromParent(child);
				renumberOps( (Expression) e.getSource() );
			}
		}
		else if( e.command == ExpressionEvent.ExpressionEventType.EXPRESSION_DELETED ) {
			
			DefaultMutableTreeNode child = findExNode( (Expression) e.getSource() );
			if( child != null ) {
				
				((DefaultTreeModel)tree.getModel()).removeNodeFromParent(child);
				renumberExpressions( );
			}
		}
		else if( e.command == ExpressionEvent.ExpressionEventType.NEW_EXPRESSION ) {
			
			Expression es = (Expression)e.getSource();
			//es.addExpressionListener(this);
			
			DefaultMutableTreeNode expr_node  = new DefaultMutableTreeNode( new StillNode(es,((DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot()).getChildCount()+1));
			DefaultMutableTreeNode input_node = new DefaultMutableTreeNode( new StillNode(es.table));
			DefaultMutableTreeNode last_node = input_node;
			
			((DefaultTreeModel)tree.getModel()).insertNodeInto( expr_node,
																((DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot()), 
																((DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot()).getChildCount());
			((DefaultTreeModel)tree.getModel()).insertNodeInto( input_node, expr_node, 0);

			es.table.addActionListener(this);
			
			expr_node.add(input_node);
			int opnum = 0;
			for( Operator op :  es.operators ) {
				
				opnum++;
				op.setTermNumber(opnum);
				op.setExpressionNumber(((DefaultMutableTreeNode)((DefaultTreeModel)tree.getModel()).getRoot()).getChildCount()+1);
				DefaultMutableTreeNode op_node = new DefaultMutableTreeNode(new StillNode(op,opnum));
				last_node = op_node;
				op.addActionListener(this);
				op.addTableListener(this);
				ArrayList<DimensionDescriptor> descriptors = op.getConstructedDimensions();
				if( descriptors != null ) {
					for( DimensionDescriptor dd : descriptors ) {
						
						DefaultMutableTreeNode dd_node = new DefaultMutableTreeNode( dd );
						op_node.add( dd_node );
						if( dd.sub_values != null ) {
							
							for( String s : dd.sub_values ) {
								
								DefaultMutableTreeNode sv_node = new DefaultMutableTreeNode( s );
								dd_node.add( sv_node );
							}
						}
					}
				}
				((DefaultTreeModel)tree.getModel()).insertNodeInto( op_node, expr_node, opnum);
			}
			renumberExpressions( );
			tree.setSelectionPath( new TreePath(last_node.getPath()) );
		}
	}

	public static Expression getTreeExpression( TreeSelectionEvent e ) {
		
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
	    
		if (node == null || node.isRoot()) {

			return null;
		}
		
		if( node.getUserObject() instanceof DimensionDescriptor ) {
			
			node  = (DefaultMutableTreeNode) node.getParent();
		}
		
		if( node.isLeaf() && node.getUserObject() instanceof String ) {
			
			node  = (DefaultMutableTreeNode) ( node.getParent()).getParent();
		}

		if (node == null || node.isRoot()) {

			return null;
		}
		
		// determine the expression, the 
		if( node.getUserObject() instanceof StillNode ) {
			
			StillNode myNode = (StillNode) node.getUserObject();
			if( myNode.ex != null ) {
			
				return myNode.ex;
			}
			if( (myNode.op != null || myNode.table != null ) && myNode.ncol < 0 ) {
				
				return ( (StillNode) ( (DefaultMutableTreeNode) node.getParent() ).getUserObject() ).ex;
			}
			
			if (node.isLeaf()) {
	
				DefaultMutableTreeNode parent  = (DefaultMutableTreeNode) node.getParent();
				
				return ( (StillNode) ( parent ).getUserObject() ).ex;
			}
		}
		
		return null;
	}
	
	public static Table getTreeOperator( TreeSelectionEvent e ) {
		
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
	    
	    if (node == null || node.isRoot()) {

			return null;
		}
		
		if( node.getUserObject() instanceof DimensionDescriptor ) {
			
			node  = (DefaultMutableTreeNode) node.getParent();
		}
		
		if( node.isLeaf() && node.getUserObject() instanceof String ) {
			
			node  = (DefaultMutableTreeNode) ( node.getParent()).getParent();
		}

		if (node == null || node.isRoot()) {

			return null;
		}

		// determine the expression, the 
		
		if( node.getUserObject() instanceof StillNode ) {
			
			StillNode myNode = (StillNode) node.getUserObject();
			if( myNode.ex != null ) {
			
				return null;
			}
			if( (myNode.op != null || myNode.table != null ) && myNode.ncol < 0 ) {
				
				if( myNode.op != null ) {
					return myNode.op;
				}
				if( myNode.table != null ) {
					return myNode.table;
				}
				return null;
			}
		}	
		
		
		return null;
	}

	public void signalActionListeners( ) {
		
		for( ActionListener al : actionListeners ) {
			
			al.actionPerformed( new ActionEvent( selExpr, ActionEvent.ACTION_PERFORMED, "" ) );
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		
	    DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();

		if (node == null || node.isRoot()) {

			selExpr  	= null;
			selTable 	= null;
			selDim 		= -1;
			signalActionListeners( );
			return;
		}
		
		// determine the expression, the 
		if( node.isLeaf() == false ) {
			
			if( node.getUserObject() instanceof StillNode ) {
				
				StillNode myNode = (StillNode) node.getUserObject();
				if( myNode.ex != null ) {
				
					selExpr  	= myNode.ex;
					selTable 	= null;
					selDim 		= -1;
					signalActionListeners( );
					return;
				}
				
				if( (myNode.op != null || myNode.table != null ) && myNode.ncol < 0 ) {
					
					selExpr  	= ( (StillNode) ( (DefaultMutableTreeNode) node.getParent() ).getUserObject() ).ex;
					if( myNode.op == null ) {
						selTable 	= myNode.table;
					}
					else {
						selTable 	= myNode.op;
					}
					selDim 		= -1;
					signalActionListeners( );
					return;
				}
			}			
		}
		
		if (node.isLeaf()) {

			DefaultMutableTreeNode parent  = (DefaultMutableTreeNode) node.getParent();
			
			if( ( parent ).getUserObject() instanceof StillNode ) {
				
				StillNode myNode  =  (StillNode) ( parent ).getUserObject();
				selExpr  	= ( (StillNode) ( parent ).getUserObject() ).ex;
				if( selExpr != null ) {
					myNode = (StillNode) node.getUserObject();
				}
				
				if( myNode.op == null ) {
					selTable 	= myNode.table;
				}
				else {
					selTable 	= myNode.op;
				}
				selDim 		= myNode.ncol;			
				signalActionListeners( );
			}
		}
	}

	public static void main( final String[] args ) {
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
            	JFrame frame = new JFrame("TreeDemo");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLayout(new BorderLayout());
                
                // load up a sample file
                Table table = TableFactory.fromCSV( args[0] );
                
                // set up a really simple expression list
                ArrayList<Expression> e = new ArrayList<Expression>();
                for( int i = 0; i < 1; i++ ) {
                	
                	e.add( 	new Expression( 
							new PCAOp( 
							new PearsonCollectOp( 
							new CutoffOp( table, true, 0 ), true, 0.8 ), true, 1 ) ) );
                }
                
                //Add content to the window.
                frame.add(new ExpressionTreeView(e), BorderLayout.CENTER);
                
                //Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        } );
	}
	
	public class StillNode  {
		
		public Operator op 	= null;
		public Expression ex 	= null;
		public Table table		= null;
		public int ncol = -1;
		public int opnum = -1;
		public int exnum = -1;
		
		
		public StillNode( Operator op, int opnum ) {
			
			this.opnum = opnum;
			this.op = op;
		}
		
		public StillNode( Expression ex, int exnum ) {
			
			this.exnum = exnum;
			this.ex = ex;
		}
		
		public StillNode( Table table ) {
			
			this.table = table;
		}

		public StillNode( Table table, int col ) {
			
			this.table = table;
			this.ncol = col;
		}
		
		public String toString() {
			
			String str = "";
			
			if( op != null ) {
				
				if( op.isActive() ) {

					String foo = "";
					if( op.getParamString().length() > 0 ) {
						
						foo = " (" + op.getParamString() +")";
					}
					String rows_str = "" + op.rows();
					if( op.rows() > 100 ) {
						
						rows_str = nf.format( 100 * op.rows() / op.input.rows() ) + "%";
					}
					str = "S" + opnum + ":" + op + " p=" + rows_str + " d=" + Operator.getNonAttributeDims(op) + foo;
				}
				else {
					str = "S" + opnum + ":" + op ;
				}
			}
			if( ex != null ) {
				
				str = "E" + exnum + ": " + ex ; 
			}
			if( table != null ) {
				
				str = "[Input:File]" + " p=" + table.rows() + " d=" + Operator.getNonAttributeDims(table) + " (" + table + ")" ; 
			}
			if( table != null && ncol >= 0 ) {
				
				str = "" + ncol + " : " + table.getColType(ncol);
			}
			
			return str;
		}
		
		
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e) {

	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tableChanged(TableEvent te) {
		
		// find the appropriate operator
		DefaultMutableTreeNode node = findOpNodes( (Table) te.src );

		// adjust selection to the table instead of any of its subnodes
		
		if( tree.getSelectionPath() != null) {
			
			boolean is_in_selection_path = false;
			int path_count = 0;
					
			Object[] path = tree.getSelectionPath().getPath();
			for( Object o : path ) {
				
				path_count++;
				
				if( o == node ) {
					
					is_in_selection_path = true;
					break;
				}
			}
			if( is_in_selection_path ) {
	
				Object[] new_path = new Object[path_count];
				int i = 0;
				for( Object o : path ) {
					
					new_path[i] = o;
					i++;
					
					if( o == node ) {
						
						break;
					}
				}			
				
				tree.setSelectionPath( new TreePath(new_path) );
			}
		}
		
		ArrayList<DefaultMutableTreeNode> old_nodes = new ArrayList<DefaultMutableTreeNode>();
		
		// remove the existing nodes
		for( int j = 0; j < node.getChildCount(); j++ ) {

			DefaultMutableTreeNode gchild = (DefaultMutableTreeNode) node.getChildAt(j);
			old_nodes.add( gchild );
		}
		for( DefaultMutableTreeNode dmtn : old_nodes ) {
			((DefaultTreeModel)tree.getModel()).removeNodeFromParent(dmtn);
		}
		
		
		// update its dimension descriptors
		if( node != null ) {
						
			ArrayList<DimensionDescriptor> descriptors = ((Table)te.src).getConstructedDimensions();
			if( descriptors != null ) {
				int k = 0;
				for( DimensionDescriptor dd : descriptors ) {
					
					DefaultMutableTreeNode dd_node = new DefaultMutableTreeNode( dd );
					if( dd.sub_values != null ) {
						
						for( String s : dd.sub_values ) {
							
							DefaultMutableTreeNode sv_node = new DefaultMutableTreeNode( s );
							dd_node.add( sv_node );
						}
					}

					((DefaultTreeModel)tree.getModel()).insertNodeInto(dd_node, node, k);
					k++;
				}
			}
		}
	}
	
	class ExpressionTreeCellRenderer extends DefaultTreeCellRenderer {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2147342463298722375L;

		public Component getTreeCellRendererComponent(	JTree tree, 
														Object value, 
														boolean sel,   
														boolean expanded, 
														boolean leaf, 
														int row, 
														boolean hasFocus) {
			
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);  
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;  
				if( node.getUserObject() instanceof StillNode ) {
					
					StillNode snode = (StillNode) node.getUserObject();
					if( snode.op != null && !snode.op.isActive() ) {

						this.setEnabled(false);
						this.setDisabledIcon( this.getClosedIcon() );
					}
				}
				if( node.getUserObject() instanceof DimensionDescriptor ) {
					
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
					if( parent.getUserObject() instanceof StillNode ) {
						
						StillNode snode = (StillNode) parent.getUserObject();
						if( snode.op != null && !snode.op.isActive() ) {

							this.setEnabled(false);
							this.setDisabledIcon( this.getClosedIcon() );
						}
					}
				}
				if( node.getUserObject() instanceof String && !node.isRoot()) {
					
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)(node.getParent().getParent());
					if( parent.getUserObject() instanceof StillNode ) {
						
						StillNode snode = (StillNode) parent.getUserObject();
						if( snode.op != null && !snode.op.isActive() ) {

							this.setEnabled(false);
							this.setDisabledIcon( this.getClosedIcon() );
						}
					}
				}

				return this;  
		}
	}
}
