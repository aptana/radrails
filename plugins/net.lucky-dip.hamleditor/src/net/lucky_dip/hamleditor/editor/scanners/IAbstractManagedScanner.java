package net.lucky_dip.hamleditor.editor.scanners;

import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * @author matt
 *
 */
public interface IAbstractManagedScanner extends ITokenScanner {

	public boolean affectsBehavior(PropertyChangeEvent event);
	
	public void adaptToPreferenceChange(PropertyChangeEvent event);
	
}
