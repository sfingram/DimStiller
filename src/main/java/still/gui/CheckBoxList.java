package still.gui;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CheckBoxList extends JPanel implements ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4554625632744490158L;
	
	ArrayList<String> model = null;
	JPanel main_panel = null;
	public ArrayList<JCheckBox> checkboxes = null;
	ArrayList<ItemListener> itemListeners = null;
	
	/**
	 * 
	 * @param model
	 */
	public CheckBoxList( ArrayList<String> model, boolean[] selecting ) {
		super();
		
		this.model = model; 
		this.itemListeners	= new ArrayList<ItemListener>();
		this.checkboxes 	= new ArrayList<JCheckBox>();
		
		if( model != null ) {
			
			setLayout( new GridLayout( model.size(), 1 ) );
			
			int k = 0;
			for( String s : model ) {
				
				JCheckBox jcb = new JCheckBox(s);
				if( selecting != null && !selecting[k] ) {
					
					jcb.setSelected(true);
				}
				jcb.addItemListener(this);
				checkboxes.add( jcb );
				add( jcb );
				k++;
			}
		}		
	}
	
	public CheckBoxList( ArrayList<String> model  ) {

		this( model, null );
	}

	public void updateModelState( boolean[] selecting ) {
		
		if( model != null ) {
			
			int k = 0;
			for( String s : model ) {
				
				if( selecting != null && !selecting[k] ) {
					
					this.checkboxes.get(k).setSelected(true);
				}
				k++;
			}
		}		
	}
	
	public void setModel( ArrayList<String> model ) {
		
		setModel( model, null );
	}
	/**
	 * 
	 * @param model
	 */
	public void setModel( ArrayList<String> model,  boolean[] selecting ) {

		this.model = model;
		for( JCheckBox jcb : checkboxes ) {
			
			jcb.removeItemListener(this);
		}

		this.removeAll();
		
		if( model != null ) {

			setLayout( new GridLayout( model.size(), 1 ) );
			checkboxes 	= new ArrayList<JCheckBox>();
			int k = 0;
			for( String s : model ) {
				
				JCheckBox jcb = new JCheckBox(s);
				if( selecting != null && !selecting[k] ) {
					
					jcb.setSelected(true);
				}
				jcb.addItemListener(this);
				checkboxes.add( jcb );
				add( jcb );
				k++;
			}
		}
		
		this.getParent().validate();
		this.repaint();
	}

	public ArrayList<String> getSelectedStrings() {

		ArrayList<String> selected = new ArrayList<String>();
		
		for( JCheckBox jcb : checkboxes ) {
			
			if( jcb.isSelected() ) {
				
				selected.add( jcb.getText() );
			}
		}
		
		return selected;
	}
	
	public ArrayList<Integer> getSelectedNums() {

		ArrayList<Integer> selected = new ArrayList<Integer>();
		
		int k = 0;
		for( JCheckBox jcb : checkboxes ) {
			
			if( jcb.isSelected() ) {
				
				selected.add( k );
			}
			k++;
		}
		
		return selected;
	}
	
	public void addItemListener( ItemListener il ) {
		
		if( il != null ) {
			
			itemListeners.add( il );
		}
	}
	
	public void removeItemListener( ItemListener il ) {
		
		if( il != null && itemListeners.contains(il) ) {
			
			itemListeners.remove(il);
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		for( ItemListener il : itemListeners ) {
			
			il.itemStateChanged( new ItemEvent( e.getItemSelectable(), ItemEvent.ITEM_STATE_CHANGED, this, e.getStateChange() ));
		}
	}
	
	public void setEnabled( boolean isEnabled ) {
		
		super.setEnabled(isEnabled);
		for( JCheckBox box : checkboxes ) {
			
			box.setEnabled(isEnabled);
		}
	}

}
