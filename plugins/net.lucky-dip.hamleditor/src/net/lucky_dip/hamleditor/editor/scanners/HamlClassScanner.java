package net.lucky_dip.hamleditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.ColorManager;
import net.lucky_dip.hamleditor.editor.HamlWordDetector;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class HamlClassScanner extends RuleBasedScanner {

	public HamlClassScanner() {
		super();
	}

	protected String[] getColorKeys() {
		return new String[] { IHamlEditorColorConstants.HAML_CLASS };
	}

	public HamlClassScanner(ColorManager manager) {
		IToken classToken = new Token(new TextAttribute(manager
				.getColor(IHamlEditorColorConstants.HAML_CLASS)));

		IRule[] rules = new IRule[1];

		rules[0] = new WordPatternRule(new HamlWordDetector(), ".", "",
				classToken);

		setRules(rules);
	}
}
