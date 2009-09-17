package net.lucky_dip.sasseditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.sasseditor.editor.ISassEditorColorConstants;
import net.lucky_dip.sasseditor.editor.SassWordDetector;
import net.lucky_dip.sasseditor.editor.rules.SassAttributeRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class SassAttributeScanner extends RuleBasedScanner {

	public SassAttributeScanner() {
		super();
	}

	protected String[] getColorKeys() {
		return new String[] { ISassEditorColorConstants.SASS_ATTRIBUTE };
	}

	public SassAttributeScanner(IColorManager manager) {
		IToken token = new Token(new TextAttribute(manager
				.getColor(ISassEditorColorConstants.SASS_ATTRIBUTE)));
		IRule[] rules = new IRule[2];
		rules[0] = new WordPatternRule(new SassWordDetector(), ":", "", token);
		rules[1] = new SassAttributeRule(token);
		setRules(rules);
	}
}
