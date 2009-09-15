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

public class SassClassScanner extends RuleBasedScanner
{

	public SassClassScanner()
	{
		super();
	}

	protected String[] getColorKeys()
	{
		return new String[] { ISassEditorColorConstants.SASS_CLASS };
	}

	public SassClassScanner(IColorManager manager)
	{
		IToken classToken = new Token(new TextAttribute(manager.getColor(ISassEditorColorConstants.SASS_CLASS)));
		IRule[] rules = new IRule[1];
		rules[0] = new WordPatternRule(new SassWordDetector(), "#", "", classToken);
		setRules(rules);
	}
}
