package still.data;

public class TableEvent {

	
	public enum TableEventType { 	ATTRIBUTE_CHANGED,
									FEATURE_CHANGED,
									FEATURES_CHANGED,
									ROW_CHANGED,
									COLUMN_CHANGED,
									TABLE_CHANGED,
									COLUMN_ADDED,
									ROW_ADDED,
									REMOVE_ME,
									ADD_ME };

	public Object 			src 	= null;
	public TableEventType  type 	= null;
	public String			col_name = null;
	public int 			point_idx 	= -1;
	public int				col_idx   	= -1;
	public int[][] 		indices 	= null;
	public Operator 		op 			= null;
	public boolean			is_upstream = false;
	
	public TableEvent( 	Object src, 
						TableEventType type, 
						int point_idx, 
						int col_idx, 
						int[][] indices,
						Operator op,
						String col_name,
						boolean is_upstream) {
		
		this.src 			= src;
		this.type 			= type;
		this.point_idx 		= point_idx;
		this.col_idx		= col_idx;
		this.indices 		= indices;
		this.op				= op;
		this.col_name		= col_name;
		this.is_upstream	= is_upstream;
	}
	
	public TableEvent( Object src, TableEventType type, int single_idx ) {
		
		this.src = src;
		this.type = type;
		if( type == TableEventType.COLUMN_ADDED || type == TableEventType.COLUMN_CHANGED ) {
			
			this.col_idx = single_idx;
		}
		else if( type == TableEventType.ROW_ADDED || type == TableEventType.ROW_CHANGED ) {

			this.point_idx = single_idx;
		}
	}

	public TableEvent( Object src, TableEventType type ) {
		this( src, type, -1, -1, null, null,null,false);
	}
	
	public TableEvent( Object src, TableEventType type, Operator op) {
		this( src, type, -1, -1, null, op,null,false);		
	}
	
	public TableEvent( Object src, TableEventType type, int point_idx, int col_idx ) {
		this( src, type, point_idx, col_idx, null, null,null,false);		
	}

	public TableEvent( Object src, TableEventType type, int[][] indices ) {
		this( src, type, -1, -1, indices, null,null,false);		
	}
	public TableEvent( Object src, TableEventType type, int[][] indices, boolean is_upstream ) {
		this( src, type, -1, -1, indices, null,null,is_upstream);		
	}
	public TableEvent( Object src, TableEventType type, String col_name, int[][] indices, boolean is_upstream ) {
		this( src, type, -1, -1, indices, null,col_name,is_upstream);		
	}
}
