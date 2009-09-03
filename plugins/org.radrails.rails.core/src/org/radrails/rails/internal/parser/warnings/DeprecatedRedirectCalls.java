package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedRedirectCalls extends DeprecationVisitor {

	private static final String[] DEPRECATED = new String[] {"redirect_to_path", "redirect_to_url"};
	
	public DeprecatedRedirectCalls() {
		super(DEPRECATED, "Deprecated method call, use redirect_to instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_REDIRECT_CALLS;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedRedirectCalls;
	}
}
