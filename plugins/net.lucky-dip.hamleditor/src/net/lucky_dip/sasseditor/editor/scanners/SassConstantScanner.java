package net.lucky_dip.sasseditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.sasseditor.editor.ISassEditorColorConstants;
import net.lucky_dip.sasseditor.editor.SassWordDetector;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class SassConstantScanner extends RuleBasedScanner
{

	public SassConstantScanner()
	{
		super();
	}

	protected String[] getColorKeys()
	{
		return new String[] { ISassEditorColorConstants.SASS_CONSTANT };
	}

	public SassConstantScanner(IColorManager manager)
	{
		IToken idToken = new Token(new TextAttribute(manager.getColor(ISassEditorColorConstants.SASS_CONSTANT)));
		IRule[] rules = new IRule[1];
		rules[0] = new WordPatternRule(new SassWordDetector(), "!", "", idToken);
		setRules(rules);
	}
}
