package still.expression;

import still.data.Table;

/**
 * 
 * Informs the expression about terms added or removed from the expression
 * 
 * @author sfingram
 *
 */
public class ExpressionEvent {

	public enum ExpressionEventType { 	INVALID, 
										TERM_ADDED, 
										TERM_REMOVED, 
										TERM_CHANGED, 
										TERM_ACTIVATED,
										NEW_EXPRESSION,
										NEW_INPUT,
										EXPRESSION_DELETED }
	
	public Object src 		= null;
	public ExpressionEventType command	= ExpressionEventType.INVALID;
	public Table term		= null;
	public Table additionalTerm = null;

	public ExpressionEvent( Object src, ExpressionEventType command, Table term, Table additionalTerm ) {
	
		this.src 		= src;
		this.command 	= command;
		this.term		= term;
		this.additionalTerm = additionalTerm;
	}
	
	public ExpressionEvent( Object src, ExpressionEventType command, Table term ) {
		
		this.src 		= src;
		this.command 	= command;
		this.term		= term;
		this.additionalTerm = null;
	}
	
	public ExpressionEvent( Object src, ExpressionEventType command ) {
		
		this.src 		= src;
		this.command 	= command;
		this.term		= null;
	}
	
	public ExpressionEventType getCommand( ) {
		return this.command;
	}
	
	public Object getSource( ) {
		return this.src;
	}

	public Table getTerm( ) {
		return this.term;
	}
}
