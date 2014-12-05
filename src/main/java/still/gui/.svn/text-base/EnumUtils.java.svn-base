package still.gui;

import java.awt.event.ActionListener;
import javax.swing.JComboBox;

public class EnumUtils
{
    public static <E extends Enum<E>> JComboBox getComboBox(E[] values, E currSel, String name, ActionListener l)
    {
		String sComboStrings[] = new String[values.length];
		for (E e: values)
			sComboStrings[e.ordinal()] = e.toString();
		JComboBox comboBox = new JComboBox(sComboStrings);
		comboBox.setName(name);
		comboBox.setSelectedIndex(currSel.ordinal());
		comboBox.addActionListener(l);
		return comboBox;
    }
}

//// Usage Template:
//enum MyColors implements EnumSelection.Item
//{
//	WHITE,
//	BLACK,
//	RED,
//}
