package net.lucky_dip.hamleditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class HamlDoctypeScanner extends RuleBasedScanner
{

	public HamlDoctypeScanner(IColorManager manager)
	{
		IToken doctype = new Token(new TextAttribute(manager.getColor(IHamlEditorColorConstants.HAML_DOCTYPE)));
		IRule[] rules = new IRule[1];
		rules[0] = new SingleLineRule("!!!", "", doctype, (char) 0, false);
		setRules(rules);
	}
}
