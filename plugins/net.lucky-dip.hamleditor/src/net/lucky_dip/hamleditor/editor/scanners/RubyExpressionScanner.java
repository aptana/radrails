/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.radrails.org/legal/cpl-v10.html
 *******************************************************************************/
package net.lucky_dip.hamleditor.editor.scanners;

import net.lucky_dip.hamleditor.editor.HamlUIColorProvider;
import net.lucky_dip.hamleditor.editor.IHamlEditorColorConstants;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.rubypeople.rdt.internal.ui.RubyPlugin;
import org.rubypeople.rdt.internal.ui.text.ruby.RubyColoringTokenScanner;

public class RubyExpressionScanner extends RubyColoringTokenScanner
{

	private Color bgColour;
	private HamlUIColorProvider fColorManager;

	public RubyExpressionScanner()
	{
		super(RubyPlugin.getDefault().getRubyTextTools().getColorManager(), RubyPlugin.getDefault()
				.getPreferenceStore());
		fColorManager = new HamlUIColorProvider();
		bgColour = fColorManager.getColorFromPreference(IHamlEditorColorConstants.HAML_RUBY_BACKGROUND);
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (fColorManager != null)
			fColorManager.dispose();
		fColorManager = null;
		super.finalize();
	}

	public IToken nextToken()
	{
		IToken res = super.nextToken();
		Object data = res.getData();

		if (data instanceof TextAttribute)
		{
			TextAttribute attr = (TextAttribute) res.getData();
			res = new Token(new TextAttribute(attr.getForeground(), bgColour, attr.getStyle()));
		}
		return res;
	}
}
