package net.lucky_dip.hamleditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.HamlWordDetector;
import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

public class HamlElementScanner extends RuleBasedScanner
{

	public HamlElementScanner(IColorManager manager)
	{
		IToken element = new Token(new TextAttribute(manager.getColor(IHamlEditorColorConstants.HAML_ELEMENT)));
		IRule[] rules = new IRule[1];
		rules[0] = new WordPatternRule(new HamlWordDetector(), "%", "", element);
		setRules(rules);
	}
}
