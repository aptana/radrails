/**
 * 
 */
package net.lucky_dip.hamleditor.editor.scanners;

import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * Used to scan/color regular text.
 * 
 * @author kyle
 * 
 * @version 0.4
 */
public class DefaultScanner extends RuleBasedScanner {
	
	public DefaultScanner() {
		super();
	}
	
	protected String[] getColorKeys() {
		return new String[] {AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND};
	}

} // DefaultScanner
