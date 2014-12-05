package still.data;

import java.util.ArrayList;

public class DimensionDescriptor {

	public String name;
	public String descriptor;
	public ArrayList<String> sub_values = null;
	
	public DimensionDescriptor( String name, String descriptor, ArrayList<String> sub_values ) {
		
		this.name = name;
		this.descriptor = descriptor;
		this.sub_values = sub_values;
	}
	
	public DimensionDescriptor( String name, String descriptor ) {
		
		this.name = name;
		this.descriptor = descriptor;
		this.sub_values = null;
	}
	
	
	public String toString() {
		String out = "";
		if( name != null ) {
			out += name;
			if( descriptor != null ) {
				
				out += (" : " + descriptor);
			}
		}
		return out;
	}
}
