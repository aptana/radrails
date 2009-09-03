package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedUpdateElementFunction extends DeprecationVisitor {
	
	private static final String[] DEPRECATED = new String[] {"update_element_function"};

	public DeprecatedUpdateElementFunction() {
		super(DEPRECATED, "Deprecated method call, use RJS instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_UPDATE_ELEMENT_FUNCTION;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedUpdateElementFunction;
	}
}
