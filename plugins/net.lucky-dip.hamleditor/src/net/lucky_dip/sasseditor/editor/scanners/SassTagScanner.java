package net.lucky_dip.sasseditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.sasseditor.editor.ISassEditorColorConstants;
import net.lucky_dip.sasseditor.editor.rules.SassTagRule;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

public class SassTagScanner extends RuleBasedScanner
{

	public SassTagScanner()
	{
		super();
	}

	protected String[] getColorKeys()
	{
		return new String[] { ISassEditorColorConstants.SASS_TAG };
	}

	public SassTagScanner(IColorManager manager)
	{
		IToken token = new Token(new TextAttribute(manager.getColor(ISassEditorColorConstants.SASS_TAG)));
		IRule[] rules = new IRule[1];
		rules[0] = new SassTagRule(token);
		setRules(rules);
	}
}
